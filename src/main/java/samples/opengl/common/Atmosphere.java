/*
 *
 * Copyright (c) 1999-2016 Luciad All Rights Reserved.
 *
 * Luciad grants you ("Licensee") a non-exclusive, royalty free, license to use,
 * modify and redistribute this software in source and binary code form,
 * provided that i) this copyright notice and license appear on all copies of
 * the software; and ii) Licensee does not utilize the software in a manner
 * which is disparaging to Luciad.
 *
 * This software is provided "AS IS," without a warranty of any kind. ALL
 * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING ANY
 * IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
 * NON-INFRINGEMENT, ARE HEREBY EXCLUDED. LUCIAD AND ITS LICENSORS SHALL NOT BE
 * LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING
 * OR DISTRIBUTING THE SOFTWARE OR ITS DERIVATIVES. IN NO EVENT WILL LUCIAD OR ITS
 * LICENSORS BE LIABLE FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT,
 * INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER
 * CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF
 * OR INABILITY TO USE SOFTWARE, EVEN IF LUCIAD HAS BEEN ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGES.
 */
package samples.opengl.common;

import com.luciad.geodesy.ILcdEllipsoid;
import com.luciad.gui.TLcdImageIcon;
import com.luciad.reference.ILcdGeocentricReference;
import com.luciad.shape.ILcdPoint;
import com.luciad.view.opengl.ALcdGLViewAdapter;
import com.luciad.view.opengl.ILcdGLCamera;
import com.luciad.view.opengl.ILcdGLView;
import com.luciad.view.opengl.TLcdGLCamera;
import com.luciad.view.opengl.TLcdGLViewEvent;
import com.luciad.view.opengl.binding.ALcdGLBinding;
import com.luciad.view.opengl.binding.ILcdGL;
import com.luciad.view.opengl.binding.ILcdGLDrawable;
import com.luciad.view.opengl.binding.ILcdGLFloatBuffer;
import com.luciad.view.opengl.style.TLcdGLTextureObject;
import org.hiranabe.vecmath.AxisAngle4d;
import org.hiranabe.vecmath.Matrix4d;
import org.hiranabe.vecmath.Vector3d;

import java.awt.Image;

/**
 * Draws an atmospheric glow effect around the globe. The glow is drawn as a
 * cone, with its apex at the origin and its center line pointed towards the
 * camera. The opening angle of the cone is chosen such that the cone passes
 * through the silhouette of the globe as seen from the current camera
 * position. The part of the cone that is inside the globe is drawn in a solid
 * color. The part that is outside the globe is drawn with a gradient going
 * from opaque to fully transparent, to create the illusion of a glow.
 * <p>
 * The glow (i.e. the part of the cone that's outside the globe) has a constant
 * thickness of 200km. When the camera approaches to within 200km of the
 * Earth's surface, however, the entire background is gradually fade to a
 * constant color, to create the illusion of "entering the atmosphere".
 */
public class Atmosphere extends ALcdGLViewAdapter {

  /**
   * RGB values for the color of the atmosphere.
   */
  private static final float[] ATMOSPHERE_COLOR = new float[] { 0.6f, 0.8f, 1.0f };
  /**
   * Height above the ellipsoid up to which the atmospheric glow is fully opaque.
   */
  private final double THICKNESS_INNER = 20000;
  /**
   * Height above the ellipsoid at which the atmospheric glow is fully transparent.
   */
  private final double THICKNESS_OUTER = 200000;
  /**
   * Number of sides used to draw the atmospheric glow cone.
   */
  private static final int SEGMENTS = 180;

  private ILcdGLFloatBuffer fVertices;
  private ILcdGLFloatBuffer fColors;
  private int[] fIndicesInner;
  private int[] fIndicesOuter;
  private Vector3d fEyeVector;
  private Vector3d fViewVector;
  private Vector3d fOrtho;
  private Vector3d fConeVectorInner;
  private Vector3d fConeVectorOuter;
  private Vector3d fAxisVector;
  private AxisAngle4d fAxisAngleRotation;
  private Matrix4d fMatrixRotation;
  private Vector3d fVertex0;
  private Vector3d fVertex1;

  private TLcdGLTextureObject fStarsTexture;
  private TLcdGLCamera fCamera = new TLcdGLCamera();

  public Atmosphere() {
    // The cone has 1 vertex in the center of the globe, and two vertices per
    // segment on the silhouette of the globe. Each vertex three components,
    // each color has four components.
    fVertices = ALcdGLBinding.getInstance().getBufferFactory().allocateFloatBuffer(3*(SEGMENTS*2 + 1));
    fColors = ALcdGLBinding.getInstance().getBufferFactory().allocateFloatBuffer(4*(SEGMENTS*2 + 1));
    // The first vertex never changes: it's the apex of the cone.
    fColors.put(ATMOSPHERE_COLOR);
    fVertices.put(0);
    fVertices.put(0);
    fVertices.put(0);
    // The vertex colors also never change.
    for (int i = 0; i < SEGMENTS; i++) {
      int iv = 1+i;
      fColors.put(iv*4, ATMOSPHERE_COLOR[0]);
      fColors.put(iv*4+1, ATMOSPHERE_COLOR[1]);
      fColors.put(iv*4+2, ATMOSPHERE_COLOR[2]);
      fColors.put(iv*4+3, 1.0f);

      iv = 1+SEGMENTS+i;
      fColors.put(iv*4, ATMOSPHERE_COLOR[0]);
      fColors.put(iv*4+1, ATMOSPHERE_COLOR[1]);
      fColors.put(iv*4+2, ATMOSPHERE_COLOR[2]);
      fColors.put(iv*4+3, 0.0f);
    }
    fColors.rewind();
    fVertices.rewind();
    // The inner part of the cone is drawn as a triangle fan.
    fIndicesInner = new int[SEGMENTS + 2];
    for (int i = 0; i < SEGMENTS+1; i++) {
      fIndicesInner[i] = i;
    }
    fIndicesInner[SEGMENTS+1] = 1;
    // The outer part of the cone is drawn as a triangle strip.
    fIndicesOuter = new int[(SEGMENTS+1)*2];
    for (int i = 0; i <= SEGMENTS; i++) {
      fIndicesOuter[i*2] = 1 + (i % SEGMENTS);
      fIndicesOuter[i*2 + 1] = 1 + SEGMENTS + (i % SEGMENTS);
    }

    // Temporary values used during computation of the cone's vertices.
    fEyeVector = new Vector3d();
    fViewVector = new Vector3d();
    fOrtho = new Vector3d();
    fConeVectorInner = new Vector3d();
    fConeVectorOuter = new Vector3d();
    fAxisVector = new Vector3d();
    fAxisAngleRotation = new AxisAngle4d();
    fMatrixRotation = new Matrix4d();
    fVertex0 = new Vector3d();
    fVertex1 = new Vector3d();

    // Load an image for the starry backdrop.
    Image image = TLcdImageIcon.getImage("background/geocentric/stars.png");
    fStarsTexture = new TLcdGLTextureObject();
    fStarsTexture.setTexture(image);
  }

  public void preObjectRender(TLcdGLViewEvent aViewEvent) {
    ILcdGLView view = aViewEvent.getView();
    ILcdGL gl = view.getGLDrawable().getGL();
    ILcdGLCamera camera = view.getCamera();
    ILcdGeocentricReference reference = (ILcdGeocentricReference) view.getXYZWorldReference();
    ILcdEllipsoid e = reference.getGeodeticDatum().getEllipsoid();

    // Set up some required OpenGL state (some of these settings may have been
    // changed by the view or by other painters).
    gl.glPushAttrib(ILcdGL.GL_ALL_ATTRIB_BITS);
    gl.glPushClientAttrib(ILcdGL.GL_ALL_ATTRIB_BITS);
    gl.glDisable(ILcdGL.GL_LIGHTING);
    gl.glEnable(ILcdGL.GL_BLEND);
    gl.glBlendFunc(ILcdGL.GL_SRC_ALPHA, ILcdGL.GL_ONE_MINUS_SRC_ALPHA);
    gl.glDisable(ILcdGL.GL_DEPTH_TEST);
    gl.glDisable(ILcdGL.GL_FOG);
    gl.glTexEnvi(ILcdGL.GL_TEXTURE_ENV, ILcdGL.GL_TEXTURE_ENV_MODE, ILcdGL.GL_REPLACE);
    gl.glColor3f(1, 1, 1);

    // Reset the projection and modelview matrices
    gl.glMatrixMode(ILcdGL.GL_PROJECTION);
    gl.glPushMatrix();
    gl.glLoadIdentity();
    gl.glMatrixMode(ILcdGL.GL_MODELVIEW);
    gl.glPushMatrix();
    gl.glLoadIdentity();

    // Draw the starry backdrop using the classic skybox technique
    // Copy the camera parameters, so we don't have to modify the real camera
    gl.glEnable(ILcdGL.GL_TEXTURE_2D);
    fCamera.setFov(camera.getFov());
    fCamera.setWidth(view.getWidth());
    fCamera.setHeight(view.getHeight());
    fCamera.setNear(1000.0);
    fCamera.setFar(100000.0);
    fCamera.setEyePoint(view.getCamera().getEyePoint());
    fCamera.setReferencePoint(view.getCamera().getReferencePoint());
    fCamera.setUpVector(view.getCamera().getUpVector());
    fCamera.applyCamera(view.getGLDrawable());
    drawStarField(view.getGLDrawable(), gl, fCamera.getEyePoint(), 50000);

    gl.glDisable(ILcdGL.GL_TEXTURE_2D);

    // Compute the camera's altitude above the surface of the Earth (using a spherical approximation). 
    ILcdEllipsoid ell = ((ILcdGeocentricReference) view.getXYZWorldReference()).getGeodeticDatum().getEllipsoid();
    double radius = Math.max(ell.getA(), ell.getB());
    ILcdPoint eye = camera.getEyePoint();
    double x = eye.getX();
    double y = eye.getY();
    double z = eye.getZ();
    double alt = Math.sqrt(x * x + y * y + z * z) - radius;

    // Draw a constant color background fill if the camera is close enough to the Earth.
    double rmin = (THICKNESS_INNER + THICKNESS_OUTER) / 2.0;
    double rmax = THICKNESS_OUTER;
    double alpha = 1.0 - Math.min(Math.max((alt - rmin) / (rmax - rmin), 0.0), 1.0);
    if (alpha > 0) {
      drawFill(gl, alpha);
    }

    // Revert to the original camera view.
    gl.glMatrixMode(ILcdGL.GL_PROJECTION);
    gl.glPopMatrix();
    gl.glMatrixMode(ILcdGL.GL_MODELVIEW);
    gl.glPopMatrix();

    // Compute and draw the cone that represents the atmospheric glow.
    drawRing(camera, gl, radius);

    // Restore the rest of the OpenGL state.
    gl.glPopAttrib();
    gl.glPopClientAttrib();
  }

  /**
   * Draws the starry background using the standard skybox technique.
   */
  private void drawStarField(ILcdGLDrawable aGlDrawable, ILcdGL aGL, ILcdPoint aEyePoint, float aBoxSize) {
    aGL.glTranslated(aEyePoint.getX(), aEyePoint.getY(), aEyePoint.getZ());

    fStarsTexture.bind(aGlDrawable);
    aGL.glTexParameteri(ILcdGL.GL_TEXTURE_2D, ILcdGL.GL_TEXTURE_WRAP_S, ILcdGL.GL_CLAMP_TO_EDGE);
    aGL.glTexParameteri(ILcdGL.GL_TEXTURE_2D, ILcdGL.GL_TEXTURE_WRAP_T, ILcdGL.GL_CLAMP_TO_EDGE);

    aGL.glBegin(ILcdGL.GL_QUADS);
    aGL.glTexCoord2f(1.0f, 0.0f);
    aGL.glVertex3f(aBoxSize, aBoxSize, -aBoxSize);
    aGL.glTexCoord2f(1.0f, 1.0f);
    aGL.glVertex3f(aBoxSize, aBoxSize, aBoxSize);
    aGL.glTexCoord2f(0.0f, 1.0f);
    aGL.glVertex3f(-aBoxSize, aBoxSize, aBoxSize);
    aGL.glTexCoord2f(0.0f, 0.0f);
    aGL.glVertex3f(-aBoxSize, aBoxSize, -aBoxSize);

    aGL.glTexCoord2f(1.0f, 0.0f);
    aGL.glVertex3f(-aBoxSize, -aBoxSize, -aBoxSize);
    aGL.glTexCoord2f(1.0f, 1.0f);
    aGL.glVertex3f(-aBoxSize, -aBoxSize, aBoxSize);
    aGL.glTexCoord2f(0.0f, 1.0f);
    aGL.glVertex3f(aBoxSize, -aBoxSize, aBoxSize);
    aGL.glTexCoord2f(0.0f, 0.0f);
    aGL.glVertex3f(aBoxSize, -aBoxSize, -aBoxSize);

    aGL.glTexCoord2f(1.0f, 0.0f);
    aGL.glVertex3f(-aBoxSize, aBoxSize, aBoxSize);
    aGL.glTexCoord2f(1.0f, 1.0f);
    aGL.glVertex3f(-aBoxSize, -aBoxSize, aBoxSize);
    aGL.glTexCoord2f(0.0f, 1.0f);
    aGL.glVertex3f(aBoxSize, -aBoxSize, aBoxSize);
    aGL.glTexCoord2f(0.0f, 0.0f);
    aGL.glVertex3f(aBoxSize, aBoxSize, aBoxSize);

    aGL.glTexCoord2f(1.0f, 0.0f);
    aGL.glVertex3f(-aBoxSize, aBoxSize, -aBoxSize);
    aGL.glTexCoord2f(1.0f, 1.0f);
    aGL.glVertex3f(-aBoxSize, aBoxSize, aBoxSize);
    aGL.glTexCoord2f(0.0f, 1.0f);
    aGL.glVertex3f(-aBoxSize, -aBoxSize, aBoxSize);
    aGL.glTexCoord2f(0.0f, 0.0f);
    aGL.glVertex3f(-aBoxSize, -aBoxSize, -aBoxSize);

    aGL.glTexCoord2f(1.0f, 0.0f);
    aGL.glVertex3f(aBoxSize, -aBoxSize, -aBoxSize);
    aGL.glTexCoord2f(1.0f, 1.0f);
    aGL.glVertex3f(aBoxSize, -aBoxSize, aBoxSize);
    aGL.glTexCoord2f(0.0f, 1.0f);
    aGL.glVertex3f(aBoxSize, aBoxSize, aBoxSize);
    aGL.glTexCoord2f(0.0f, 0.0f);
    aGL.glVertex3f(aBoxSize, aBoxSize, -aBoxSize);

    aGL.glTexCoord2f(1.0f, 0.0f);
    aGL.glVertex3f(aBoxSize, aBoxSize, -aBoxSize);
    aGL.glTexCoord2f(1.0f, 1.0f);
    aGL.glVertex3f(-aBoxSize, aBoxSize, -aBoxSize);
    aGL.glTexCoord2f(0.0f, 1.0f);
    aGL.glVertex3f(-aBoxSize, -aBoxSize, -aBoxSize);
    aGL.glTexCoord2f(0.0f, 0.0f);
    aGL.glVertex3f(aBoxSize, -aBoxSize, -aBoxSize);
    aGL.glEnd();
  }

  /**
   * Draws a full-screen quadrilateral with the sky color and the specified opacity.
   */
  private void drawFill(ILcdGL aGL, double aAlpha) {
    aGL.glMatrixMode(ILcdGL.GL_PROJECTION);
    aGL.glLoadIdentity();
    aGL.glMatrixMode(ILcdGL.GL_MODELVIEW);
    aGL.glLoadIdentity();

    aGL.glColor4f(ATMOSPHERE_COLOR[0], ATMOSPHERE_COLOR[1], ATMOSPHERE_COLOR[2], (float) aAlpha);
    aGL.glBegin(ILcdGL.GL_QUADS);
    aGL.glVertex2f(-1, -1);
    aGL.glVertex2f(1, -1);
    aGL.glVertex2f(1, 1);
    aGL.glVertex2f(-1, 1);
    aGL.glEnd();
  }

  /**
   * Draws the cone that represents the atmospheric glow.
   */
  private void drawRing(ILcdGLCamera aCamera, ILcdGL aGL, double aRadius) {
    ILcdPoint eye = aCamera.getEyePoint();
    ILcdPoint ref = aCamera.getReferencePoint();

    // Create a vector representing the camera's view direction.
    fEyeVector.set(eye.getX(), eye.getY(), eye.getZ());
    fViewVector.set(ref.getX(), ref.getY(), ref.getZ());
    fViewVector.sub(fEyeVector);
    // The principal axis of the cone goes from the origin (the center of the
    // Earth) to the eye point.
    fConeVectorInner.set(fEyeVector);
    fConeVectorInner.negate();

    /* Where they intersect, the surfaces of the glow cone and the Earth for a
    right angle. The intersection of the cone and the Earth is also the
    silhouette of the Earth as seen from the current camera. We're going to
    compute points on the silhouette by considering three triangles.
    The first has these three points:
      E is the current camera eye point
      C is the center of the globe
      S is a point on the silhouette of the globe

    The second triangle has these three points:
      C is the center of the globe
      S is the point on the silhouette of the globe
      D is a point on the line EC perpendicular to the line EC

    The third triangle has these three points (it is the union of the above two triangles):
      E is the current camera eye point
      D is a point on the line EC perpendicular to the line EC
      C is the center of the globe

    The direction of DC is arbitrary and is computed in a robust way by taking a vector
    orthogonal to EC.

    The above three triangles are right angle triangles and using the Pythagorean theorem
    following equalities hold on the edge lengths of these triangles:
      EC 	^2 = 	ES 	^2 + 	CS 	^2
      DC 	^2 = 	SD 	^2 + 	CS 	^2
      ED 	^2 = ( 	ES 	+ 	SD 	)^2 = 	EC 	^2 + 	DC 	^2

    Using some simple algebra, it can be easily seen that
      DC 	= 	CS 	* sqrt(1+1/ ES 	)

    Having computed this distance, we can find the point on the silhouette by simple scaling
    and vector addition.
    */

    final double CS = aRadius;
    final double EC = fConeVectorInner.length();
    final double ES = Math.sqrt(EC*EC - CS*CS);
    final double DC = CS*Math.sqrt(1.0+1.0/ES);

    getSafeOrtho(fConeVectorInner, fOrtho);
    fOrtho.normalize();
    fOrtho.scale(DC);
    fConeVectorInner.add(fOrtho);
    fConeVectorInner.normalize();
    fConeVectorInner.scale(ES);
    fConeVectorInner.add(fEyeVector);

    /* We will compute the rest of the silhouette points by revolving the
    previously computed point around the cone's principal axis. We need two
    points for every direction, though: one on the surface of the globe where
    the glow is fully opaque, and one at a higher elevation where the glow is
    fully transparent. Hence, we now create two versions of the silhouette
    point at different distances from the origin. */
    fConeVectorInner.normalize();
    fConeVectorOuter.set(fConeVectorInner);
    fConeVectorInner.scale(aRadius + THICKNESS_INNER);
    fConeVectorOuter.scale(aRadius + THICKNESS_OUTER);
    // The new rotation axis is the principal axis of the cone.
    fAxisVector.set(fEyeVector);

    // For each segment, compute two points on the cone and store them in fVertices.
    // Create an appropriate rotation matrix.
    fAxisAngleRotation.set(fAxisVector, Math.toRadians(360.0/SEGMENTS));
    fMatrixRotation.set(fAxisAngleRotation);
    fVertex0.set(fConeVectorInner);
    fVertex1.set(fConeVectorOuter);
    for (int i = 0; i < SEGMENTS; i++) {
      // Put the vertices in the vertex buffer.
      int vi = 1+i;
      fVertices.put(vi*3, (float) fVertex0.x);
      fVertices.put(vi*3+1, (float) fVertex0.y);
      fVertices.put(vi*3+2, (float) fVertex0.z);
      vi = 1+SEGMENTS+i;
      fVertices.put(vi*3, (float) fVertex1.x);
      fVertices.put(vi*3+1, (float) fVertex1.y);
      fVertices.put(vi*3+2, (float) fVertex1.z);
      // Rotate the two cone vertices.
      fMatrixRotation.transform(fVertex0);
      fMatrixRotation.transform(fVertex1);
    }
    fVertices.rewind();

    // Finally, with all points computed, we can draw the cone.
    aGL.glEnableClientState(ILcdGL.GL_COLOR_ARRAY);
    aGL.glEnableClientState(ILcdGL.GL_VERTEX_ARRAY);
    aGL.glColorPointer(4, ILcdGL.GL_FLOAT, 0, fColors);
    aGL.glVertexPointer(3, ILcdGL.GL_FLOAT, 0, fVertices);

    aGL.glDrawElements(ILcdGL.GL_TRIANGLE_FAN, SEGMENTS + 2, ILcdGL.GL_UNSIGNED_INT, fIndicesInner);
    aGL.glDrawElements(ILcdGL.GL_TRIANGLE_STRIP, (SEGMENTS+1)*2, ILcdGL.GL_UNSIGNED_INT, fIndicesOuter);
  }

/**
   * Computes an orthogonal vector in a robust way.
   */
  private void getSafeOrtho(Vector3d aVector, Vector3d aOrthoSFCT) {
    final double x = Math.abs(aVector.x);
    final double y = Math.abs(aVector.y);
    final double z = Math.abs(aVector.z);

    if (x<=y && x<=z) {
      aOrthoSFCT.set(0, -aVector.z, aVector.y);
    }
    else if (y<=x && y<=z) {
      aOrthoSFCT.set(-aVector.z, 0, aVector.x);
    }
    else {
      aOrthoSFCT.set(-aVector.y, aVector.x, 0);
    }
  }
  
}

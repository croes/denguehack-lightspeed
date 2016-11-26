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

import com.luciad.gui.TLcdImageIcon;
import com.luciad.shape.ILcdPoint;
import com.luciad.view.opengl.*;
import com.luciad.view.opengl.binding.*;
import com.luciad.view.opengl.style.TLcdGLTextureObject;

import java.awt.*;

/**
 * Class that displays a skybox in the 3D view. A skybox is a cube that remains
 * centered around the camera position at all times. It is textured with six
 * adjoining images, each of which is created with a 90 degree field of view.
 * When the box is viewed exactly from its center, the seams between the images
 * are invisible and the illusion of a continuous background is created.
 */
public class Skybox extends ALcdGLViewAdapter {

  /**
   * Index for the front image.
   */
  public static final int FRONT = 0;
  /**
   * Index for the back image.
   */
  public static final int BACK = 1;
  /**
   * Index for the left image.
   */
  public static final int LEFT = 2;
  /**
   * Index for the right image.
   */
  public static final int RIGHT = 3;
  /**
   * Index for the top image.
   */
  public static final int UP = 4;
  /**
   * Index for the bottom image.
   */
  public static final int DOWN = 5;

  private static int NUM_SIDES = 6;

  private TLcdGLTextureObject[] fTextures = new TLcdGLTextureObject[NUM_SIDES];
  private String[] fImageFileNames = new String[NUM_SIDES];
  private TLcdGLCamera fCamera = new TLcdGLCamera();

  /**
   * Creates a skybox around a view.
   *
   * @param aImageFileNames the filenames of the images, given in order:
   *                        front, back, left, right, up, down.
   */
  public Skybox(String[] aImageFileNames) {
    for (int index = 0; index < NUM_SIDES; index++) {
      String image_file_name = aImageFileNames[index];
      fImageFileNames[index] = image_file_name;
      updateTexture(index);
    }
  }

  /**
   * Create a TLcdGLTextureObject for the image at the specified index.
   *
   * @param aIndex the index of the skybox face to be updated
   */
  private void updateTexture(int aIndex) {
    String image_file_name = fImageFileNames[aIndex];
    if (image_file_name != null) {
      Image image = TLcdImageIcon.getImage(image_file_name);
      TLcdGLTextureObject texture = new TLcdGLTextureObject();
      texture.setTexture(image);
      fTextures[aIndex] = texture;
    }
    else {
      fTextures[aIndex] = null;
    }
  }

  /**
   * Renders the skybox.
   */
  public void preObjectRender(TLcdGLViewEvent aViewEvent) {
    ILcdGLView view = aViewEvent.getView();
    ILcdGLDrawable glDrawable = view.getGLDrawable();
    ILcdGL gl = glDrawable.getGL();

    // Store current OpenGL state
    gl.glPushAttrib(ILcdGL.GL_ALL_ATTRIB_BITS);

    // Make sure not to draw the box in wireframe mode
    gl.glPolygonMode(ILcdGL.GL_FRONT_AND_BACK, ILcdGL.GL_FILL);

    // Reset the projection and modelview matrices
    gl.glMatrixMode(ILcdGL.GL_PROJECTION);
    gl.glPushMatrix();
    gl.glLoadIdentity();
    gl.glMatrixMode(ILcdGL.GL_MODELVIEW);
    gl.glPushMatrix();
    gl.glLoadIdentity();

    // Copy the camera parameters, so we don't have to modify the real camera
    fCamera.setFov(view.getCamera().getFov());
    fCamera.setWidth(view.getWidth());
    fCamera.setHeight(view.getHeight());
    fCamera.setNear(1000.0);
    fCamera.setFar(100000.0);
    fCamera.setEyePoint(view.getCamera().getEyePoint());
    fCamera.setReferencePoint(view.getCamera().getReferencePoint());
    fCamera.setUpVector(view.getCamera().getUpVector());
    fCamera.applyCamera(glDrawable);

    // Disable settings that might affect the rendering
    gl.glDisable(ILcdGL.GL_DEPTH_TEST);
    gl.glDisable(ILcdGL.GL_FOG);
    gl.glDisable(ILcdGL.GL_LIGHTING);
    gl.glEnable(ILcdGL.GL_TEXTURE_2D);
    gl.glTexEnvi(ILcdGL.GL_TEXTURE_ENV, ILcdGL.GL_TEXTURE_ENV_MODE, ILcdGL.GL_REPLACE);
    gl.glColor3f(1, 1, 1);

    // Draw the box
    paintSkyBox(gl, glDrawable, fCamera.getEyePoint(), 50000);

    // Restore modified state
    gl.glMatrixMode(ILcdGL.GL_PROJECTION);
    gl.glPopMatrix();
    gl.glMatrixMode(ILcdGL.GL_MODELVIEW);
    gl.glPopMatrix();

    gl.glPopAttrib();
  }

  /**
   * Draws a skybox with the specified location and size.
   */
  private void paintSkyBox(ILcdGL gl, ILcdGLDrawable glDrawable, ILcdPoint aEyePoint, float aBoxSize) {
    gl.glTranslated(aEyePoint.getX(), aEyePoint.getY(), aEyePoint.getZ());

    if (fTextures[FRONT] != null) {
      fTextures[FRONT].bind(glDrawable);
      gl.glTexParameteri(ILcdGL.GL_TEXTURE_2D, ILcdGL.GL_TEXTURE_WRAP_S, ILcdGL.GL_CLAMP_TO_EDGE);
      gl.glTexParameteri(ILcdGL.GL_TEXTURE_2D, ILcdGL.GL_TEXTURE_WRAP_T, ILcdGL.GL_CLAMP_TO_EDGE);
      gl.glBegin(ILcdGL.GL_QUADS);
      gl.glTexCoord2f(1.0f, 0.0f);
      gl.glVertex3f(aBoxSize, aBoxSize, -aBoxSize);
      gl.glTexCoord2f(1.0f, 1.0f);
      gl.glVertex3f(aBoxSize, aBoxSize, aBoxSize);
      gl.glTexCoord2f(0.0f, 1.0f);
      gl.glVertex3f(-aBoxSize, aBoxSize, aBoxSize);
      gl.glTexCoord2f(0.0f, 0.0f);
      gl.glVertex3f(-aBoxSize, aBoxSize, -aBoxSize);
      gl.glEnd();
    }

    if (fTextures[BACK] != null) {
      fTextures[BACK].bind(glDrawable);
      gl.glTexParameteri(ILcdGL.GL_TEXTURE_2D, ILcdGL.GL_TEXTURE_WRAP_S, ILcdGL.GL_CLAMP_TO_EDGE);
      gl.glTexParameteri(ILcdGL.GL_TEXTURE_2D, ILcdGL.GL_TEXTURE_WRAP_T, ILcdGL.GL_CLAMP_TO_EDGE);
      gl.glBegin(ILcdGL.GL_QUADS);
      gl.glTexCoord2f(1.0f, 0.0f);
      gl.glVertex3f(-aBoxSize, -aBoxSize, -aBoxSize);
      gl.glTexCoord2f(1.0f, 1.0f);
      gl.glVertex3f(-aBoxSize, -aBoxSize, aBoxSize);
      gl.glTexCoord2f(0.0f, 1.0f);
      gl.glVertex3f(aBoxSize, -aBoxSize, aBoxSize);
      gl.glTexCoord2f(0.0f, 0.0f);
      gl.glVertex3f(aBoxSize, -aBoxSize, -aBoxSize);
      gl.glEnd();
    }

    if (fTextures[UP] != null) {
      fTextures[UP].bind(glDrawable);
      gl.glTexParameteri(ILcdGL.GL_TEXTURE_2D, ILcdGL.GL_TEXTURE_WRAP_S, ILcdGL.GL_CLAMP_TO_EDGE);
      gl.glTexParameteri(ILcdGL.GL_TEXTURE_2D, ILcdGL.GL_TEXTURE_WRAP_T, ILcdGL.GL_CLAMP_TO_EDGE);
      gl.glBegin(ILcdGL.GL_QUADS);
      gl.glTexCoord2f(1.0f, 0.0f);
      gl.glVertex3f(-aBoxSize, aBoxSize, aBoxSize);
      gl.glTexCoord2f(1.0f, 1.0f);
      gl.glVertex3f(-aBoxSize, -aBoxSize, aBoxSize);
      gl.glTexCoord2f(0.0f, 1.0f);
      gl.glVertex3f(aBoxSize, -aBoxSize, aBoxSize);
      gl.glTexCoord2f(0.0f, 0.0f);
      gl.glVertex3f(aBoxSize, aBoxSize, aBoxSize);
      gl.glEnd();
    }

    if (fTextures[LEFT] != null) {
      fTextures[LEFT].bind(glDrawable);
      gl.glTexParameteri(ILcdGL.GL_TEXTURE_2D, ILcdGL.GL_TEXTURE_WRAP_S, ILcdGL.GL_CLAMP_TO_EDGE);
      gl.glTexParameteri(ILcdGL.GL_TEXTURE_2D, ILcdGL.GL_TEXTURE_WRAP_T, ILcdGL.GL_CLAMP_TO_EDGE);
      gl.glBegin(ILcdGL.GL_QUADS);
      gl.glTexCoord2f(1.0f, 0.0f);
      gl.glVertex3f(-aBoxSize, aBoxSize, -aBoxSize);
      gl.glTexCoord2f(1.0f, 1.0f);
      gl.glVertex3f(-aBoxSize, aBoxSize, aBoxSize);
      gl.glTexCoord2f(0.0f, 1.0f);
      gl.glVertex3f(-aBoxSize, -aBoxSize, aBoxSize);
      gl.glTexCoord2f(0.0f, 0.0f);
      gl.glVertex3f(-aBoxSize, -aBoxSize, -aBoxSize);
      gl.glEnd();
    }

    if (fTextures[RIGHT] != null) {
      fTextures[RIGHT].bind(glDrawable);
      gl.glTexParameteri(ILcdGL.GL_TEXTURE_2D, ILcdGL.GL_TEXTURE_WRAP_S, ILcdGL.GL_CLAMP_TO_EDGE);
      gl.glTexParameteri(ILcdGL.GL_TEXTURE_2D, ILcdGL.GL_TEXTURE_WRAP_T, ILcdGL.GL_CLAMP_TO_EDGE);
      gl.glBegin(ILcdGL.GL_QUADS);
      gl.glTexCoord2f(1.0f, 0.0f);
      gl.glVertex3f(aBoxSize, -aBoxSize, -aBoxSize);
      gl.glTexCoord2f(1.0f, 1.0f);
      gl.glVertex3f(aBoxSize, -aBoxSize, aBoxSize);
      gl.glTexCoord2f(0.0f, 1.0f);
      gl.glVertex3f(aBoxSize, aBoxSize, aBoxSize);
      gl.glTexCoord2f(0.0f, 0.0f);
      gl.glVertex3f(aBoxSize, aBoxSize, -aBoxSize);
      gl.glEnd();
    }

    if (fTextures[DOWN] != null) {
      fTextures[DOWN].bind(glDrawable);
      gl.glTexParameteri(ILcdGL.GL_TEXTURE_2D, ILcdGL.GL_TEXTURE_WRAP_S, ILcdGL.GL_CLAMP_TO_EDGE);
      gl.glTexParameteri(ILcdGL.GL_TEXTURE_2D, ILcdGL.GL_TEXTURE_WRAP_T, ILcdGL.GL_CLAMP_TO_EDGE);
      gl.glBegin(ILcdGL.GL_QUADS);
      gl.glTexCoord2f(1.0f, 0.0f);
      gl.glVertex3f(aBoxSize, aBoxSize, -aBoxSize);
      gl.glTexCoord2f(1.0f, 1.0f);
      gl.glVertex3f(-aBoxSize, aBoxSize, -aBoxSize);
      gl.glTexCoord2f(0.0f, 1.0f);
      gl.glVertex3f(-aBoxSize, -aBoxSize, -aBoxSize);
      gl.glTexCoord2f(0.0f, 0.0f);
      gl.glVertex3f(aBoxSize, -aBoxSize, -aBoxSize);
      gl.glEnd();
    }
  }
}

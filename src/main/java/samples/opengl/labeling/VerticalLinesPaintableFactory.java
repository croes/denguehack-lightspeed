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
package samples.opengl.labeling;

import com.luciad.shape.*;
import com.luciad.shape.shape3D.*;
import com.luciad.transformation.ILcdModelXYZWorldTransformation;
import com.luciad.util.TLcdOutOfBoundsException;
import com.luciad.view.opengl.ILcdGLContext;
import com.luciad.view.opengl.binding.ILcdGLDrawable;
import com.luciad.view.opengl.paintable.*;
import com.luciad.view.opengl.paintablefactory.*;

/**
 * A factory to create paintables from point lists. The paintable will contain a set of vertical lines,
 * one line for each point in the point list. Each vertical line goes from the actual
 * point to a second point with Z=0 (in model coordinates).
 */
class VerticalLinesPaintableFactory implements ILcdGLPaintableFactory {

  public boolean isModeSupported( TLcdGLPaintableFactoryMode aMode ) {
    return aMode == TLcdGLPaintableFactoryMode.OUTLINE;
  }

  public ILcdGLPaintable createPaintable(
          ILcdGLDrawable aGLDrawable,
          Object aObject,
          TLcdGLPaintableFactoryMode aMode, ILcdGLContext aContext
  ) {

    if ( !isCompatibleObject( aObject, aContext ) )
      return null;

    // We'll add all height lines directly to a mesh object
    TLcdGLMesh mesh = new TLcdGLMesh( TLcdGLMesh.VERTICES3 );

    // Some world points to be reused for all vertical lines
    TLcdXYZPoint world_point1 = new TLcdXYZPoint();
    TLcdXYZPoint world_point2 = new TLcdXYZPoint();
    ILcdModelXYZWorldTransformation transfo = aContext.getModelXYZWorldTransformation();

    ILcdPointList point_list = (ILcdPointList) aObject;

    // All lines will be added in a single LINES primitive
    mesh.startPrimitive( TLcdGLPrimitiveType.LINES );

    for ( int index = 0; index < point_list.getPointCount() ; index++ ) {

      // Create begin and end point in model coordinates
      ILcdPoint point = point_list.getPoint( index );
      ILcd3DEditablePoint zero_z_point = point.cloneAs3DEditablePoint();
      zero_z_point.move3D( point.getX(), point.getY(), 0 );

      try {
        // Transform both to world coordinates
        transfo.modelPoint2worldSFCT( point, world_point1 );
        transfo.modelPoint2worldSFCT( zero_z_point, world_point2 );

        mesh.addPoint( new double[] { world_point1.getX(),
                                    world_point1.getY(),
                                    world_point1.getZ() } );
        mesh.addPoint( new double[] { world_point2.getX(),
                                    world_point2.getY(),
                                    world_point2.getZ() } );
      } catch ( TLcdOutOfBoundsException e ) {
        // Do nothing: the line will not be added to the mesh
      }
    }
    return mesh;
  }

  protected boolean isCompatibleObject( Object aObject, ILcdGLContext aContext ) {
    return ( aObject instanceof ILcdPointList );
  }
}

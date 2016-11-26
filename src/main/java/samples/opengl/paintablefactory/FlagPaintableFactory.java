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

package samples.opengl.paintablefactory;

import com.luciad.reference.ILcdGridReference;
import com.luciad.shape.ILcdPoint;
import com.luciad.shape.shape3D.TLcdXYZPoint;
import com.luciad.util.TLcdOutOfBoundsException;
import com.luciad.view.ILcdXYZWorldReference;
import com.luciad.view.opengl.ILcdGLContext;
import com.luciad.view.opengl.binding.ILcdGLDrawable;
import com.luciad.view.opengl.paintable.*;
import com.luciad.view.opengl.paintablefactory.*;

/**
 * FlagPaintableFactory.
 */
class FlagPaintableFactory implements ILcdGLPaintableFactory {
  private ILcdGLPaintable fBasePaintable;
  private float fUnitOfMeasure;


  private ILcdGLPaintable createBasePaintable( float aUnitOfMeasure ) {
    // Here we create the base paintable for all the flags
    // We create only one object containing the actual geometry to save memory
    // In this case we will use a mesh containing only vertices
    TLcdGLMesh mesh = new TLcdGLMesh( TLcdGLMesh.VERTICES3 );
    // First we add the flag pole as a line going from height 0 to
    // about 100 km. Note that we take the unit of measure into account
    // because the values contained in the paintable are expressed as world
    // coordinates
    mesh.startPrimitive( TLcdGLPrimitiveType.LINES );
    mesh.addPoint( new double[] { 0, 0, 0 } );
    mesh.addPoint( new double[] { 0, 0, 100000 / aUnitOfMeasure } );
    // Then we add the flag itself as a quadrilateral.
    mesh.startPrimitive( TLcdGLPrimitiveType.QUADS );
    mesh.addPoint( new double[] { 0, 0, 100000 / aUnitOfMeasure } );
    mesh.addPoint( new double[] { 100000 / aUnitOfMeasure, 0, 100000 / aUnitOfMeasure } );
    mesh.addPoint( new double[] { 100000 / aUnitOfMeasure, 0, 200000 / aUnitOfMeasure } );
    mesh.addPoint( new double[] { 0, 0, 200000 / aUnitOfMeasure } );

    return mesh;
  }

  public boolean isModeSupported( TLcdGLPaintableFactoryMode aMode ) {
    return true;
  }


  public ILcdGLPaintable createPaintable(
          ILcdGLDrawable aGLDrawable,
          Object aObject,
          TLcdGLPaintableFactoryMode aMode, ILcdGLContext aContext
  ) {
    // First we test if we can do something useful with the specified object
    // In this case it must implement ILcdPoint. If the object does not
    // implement this interface, we cannot handle it so we return null.
    if ( !( aObject instanceof ILcdPoint ) )
      return null;

    // Test if the unit of measure has changed
    ILcdXYZWorldReference worldReference =
            aContext.getModelXYZWorldTransformation().getXYZWorldReference();
    float aUnitOfMeasure = 1f;
    // Only grid references have a unit of measure. If the world reference is not
    // a grid reference we assume the unit of measure is 1
    if ( worldReference instanceof ILcdGridReference )
      aUnitOfMeasure = (float) ( (ILcdGridReference) worldReference ).getUnitOfMeasure();
    // If the unit of measure has changed or there is no base paintable yet,
    // we (re)create it.
    if ( aUnitOfMeasure != fUnitOfMeasure || fBasePaintable == null ) {
      fBasePaintable = createBasePaintable( aUnitOfMeasure );
      // We store the unit of measure value so we can compare with it next time
      // a paintable object is requested.
      fUnitOfMeasure = aUnitOfMeasure;
    }

    ILcdPoint modelPoint = ( (ILcdPoint) aObject );
    TLcdXYZPoint worldPoint = new TLcdXYZPoint();
    try {
      aContext.getModelXYZWorldTransformation().modelPoint2worldSFCT( modelPoint, worldPoint );
      // Here we wrap the base paintable which is located at 0,0 in a translating
      // geometry. This will ensure our flag gets rendered at the correct
      // location. The non scaling paintable ensures our flag will not resize
      // when the z scale of the view changes.
      return new TLcdGLTranslatingPaintable(
              (float) worldPoint.getX(),
              (float) worldPoint.getY(),
              (float) worldPoint.getZ(),
              new TLcdGLNonScalingPaintable( fBasePaintable )
      );
    } catch ( TLcdOutOfBoundsException e ) {
      return null;
    }
  }
}

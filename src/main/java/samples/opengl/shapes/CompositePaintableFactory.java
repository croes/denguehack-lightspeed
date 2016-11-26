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

package samples.opengl.shapes;

import com.luciad.shape.*;
import com.luciad.shape.shape2D.TLcdLonLatBuffer;
import com.luciad.shape.shape3D.ILcdExtrudedShape;
import com.luciad.view.opengl.ILcdGLContext;
import com.luciad.view.opengl.binding.ILcdGLDrawable;
import com.luciad.view.opengl.paintable.ILcdGLPaintable;
import com.luciad.view.opengl.paintablefactory.*;

/**
 * A paintable factory that can handle most LuciadLightspeed 2D shapes and their respective extruded versions.
 */
class CompositePaintableFactory implements ILcdGLPaintableFactory {

  private TLcdGLExtrudedCirclePaintableFactory fExtrudedCirclePaintableFactory = new TLcdGLExtrudedCirclePaintableFactory();
  private TLcdGLExtrudedEllipsePaintableFactory fExtrudedEllipsePaintableFactory = new TLcdGLExtrudedEllipsePaintableFactory();
  private TLcdGLExtrudedComplexPolygonPaintableFactory fExtrudedComplexPolygonPaintableFactory = new TLcdGLExtrudedComplexPolygonPaintableFactory();
  private TLcdGLExtrudedArcPaintableFactory fExtrudedArcPaintableFactory = new TLcdGLExtrudedArcPaintableFactory();
  private TLcdGLExtrudedArcBandPaintableFactory fExtrudedArcBandPaintableFactory = new TLcdGLExtrudedArcBandPaintableFactory();
  private TLcdGLExtrudedLonLatBufferPaintableFactory fExtrudedLonLatBufferPaintableFactory = new TLcdGLExtrudedLonLatBufferPaintableFactory();
  private TLcdGLExtrudedPolygonPaintableFactory fExtrudedPolygonPaintableFactory = new TLcdGLExtrudedPolygonPaintableFactory();
  private TLcdGLExtrudedPolylinePaintableFactory fExtrudedPolylinePaintableFactory = new TLcdGLExtrudedPolylinePaintableFactory();
  private TLcdGLExtrudedCurvePaintableFactory fExtrudedCurvePaintableFactory = new TLcdGLExtrudedCurvePaintableFactory();
  private TLcdGLExtrudedRingPaintableFactory fExtrudedRingPaintableFactory = new TLcdGLExtrudedRingPaintableFactory();
  private TLcdGLExtrudedSurfacePaintableFactory fExtrudedSurfacePaintableFactory = new TLcdGLExtrudedSurfacePaintableFactory();

  private TLcdGLCirclePaintableFactory fCirclePaintableFactory = new TLcdGLCirclePaintableFactory();
  private TLcdGLEllipsePaintableFactory fEllipsePaintableFactory = new TLcdGLEllipsePaintableFactory();
  private TLcdGLComplexPolygonPaintableFactory fComplexPolygonPaintableFactory = new TLcdGLComplexPolygonPaintableFactory();
  private TLcdGLArcPaintableFactory fArcPaintableFactory = new TLcdGLArcPaintableFactory();
  private TLcdGLArcBandPaintableFactory fArcBandPaintableFactory = new TLcdGLArcBandPaintableFactory();
  private TLcdGLLonLatBufferPaintableFactory fLonLatBufferPaintableFactory = new TLcdGLLonLatBufferPaintableFactory();
  private TLcdGLPolygonPaintableFactory fPolygonPaintableFactory = new TLcdGLPolygonPaintableFactory();
  private TLcdGLPolylinePaintableFactory fPolylinePaintableFactory = new TLcdGLPolylinePaintableFactory();
  private TLcdGLDomePaintableFactory fDomePaintableFactory = new TLcdGLDomePaintableFactory();
  private TLcdGLSpherePaintableFactory fSpherePaintableFactory = new TLcdGLSpherePaintableFactory();
  private TLcdGLCurvePaintableFactory fCurvePaintableFactory = new TLcdGLCurvePaintableFactory();
  private TLcdGLRingPaintableFactory fRingPaintableFactory = new TLcdGLRingPaintableFactory();
  private TLcdGLSurfacePaintableFactory fSurfacePaintableFactory = new TLcdGLSurfacePaintableFactory();

  private TLcdGLLonLatHeightVariableGeoBufferPaintableFactory fVariableGeoBufferPaintableFactory = new TLcdGLLonLatHeightVariableGeoBufferPaintableFactory();

  public CompositePaintableFactory() {
  }

  public ILcdGLPaintable createPaintable( ILcdGLDrawable aGLDrawable, Object aObject, TLcdGLPaintableFactoryMode aMode, ILcdGLContext aContext ) {
    // If the object is an extruded shape
    if ( aObject instanceof ILcdExtrudedShape ) {
      // we check the type of its base shape to determine which factory to delegate to.
      // Since the object we are passing to the factory is an ILcdExtrudedShape, the
      // factory already nows how to retrieve the minimum and maximum Z value from the object.
      // If we would want to make an extruded version of an object that does not implement
      // ILcdExtrudedShape, we could call setDefaultMin(Max)imumZ on the factory
      // before creating the paintable. The object would then be extruded using these
      // values.
      ILcdShape baseShape = ( (ILcdExtrudedShape) aObject ).getBaseShape();
      if ( baseShape instanceof ILcdCircle ) {
        return fExtrudedCirclePaintableFactory.createPaintable( aGLDrawable, aObject, aMode, aContext );
      } else if ( baseShape instanceof ILcdEllipse ) {
        return fExtrudedEllipsePaintableFactory.createPaintable( aGLDrawable, aObject, aMode, aContext );
      } else if ( baseShape instanceof ILcdComplexPolygon ) {
        return fExtrudedComplexPolygonPaintableFactory.createPaintable( aGLDrawable, aObject, aMode, aContext );
      } else if ( baseShape instanceof ILcdArc ) {
        return fExtrudedArcPaintableFactory.createPaintable( aGLDrawable, aObject, aMode, aContext );
      } else if ( baseShape instanceof ILcdArcBand ) {
        return fExtrudedArcBandPaintableFactory.createPaintable( aGLDrawable, aObject, aMode, aContext );
      } else if ( baseShape instanceof TLcdLonLatBuffer ) {
        return fExtrudedLonLatBufferPaintableFactory.createPaintable( aGLDrawable, aObject, aMode, aContext );
      } else if ( baseShape instanceof ILcdPolygon ) {
        return fExtrudedPolygonPaintableFactory.createPaintable( aGLDrawable, aObject, aMode, aContext );
      } else if ( baseShape instanceof ILcdPolyline ) {
        return fExtrudedPolylinePaintableFactory.createPaintable( aGLDrawable, aObject, aMode, aContext );
      } else if ( baseShape instanceof ILcdRing ) {
        return fExtrudedRingPaintableFactory.createPaintable( aGLDrawable, aObject, aMode, aContext );
      } else if ( baseShape instanceof ILcdCurve ) {
        return fExtrudedCurvePaintableFactory.createPaintable( aGLDrawable, aObject, aMode, aContext );
      } else if ( baseShape instanceof ILcdSurface ) {
        return fExtrudedSurfacePaintableFactory.createPaintable( aGLDrawable, aObject, aMode, aContext );
      }
    } else {
      // otherwise we check the type of the object itself
      if ( aObject instanceof LonLatSphere ) {
        return fSpherePaintableFactory.createPaintable( aGLDrawable, aObject, aMode, aContext );
      } else if ( aObject instanceof LonLatDome ) {
        return fDomePaintableFactory.createPaintable( aGLDrawable, aObject, aMode, aContext );
      } else if ( aObject instanceof ILcdCircle ) {
        return fCirclePaintableFactory.createPaintable( aGLDrawable, aObject, aMode, aContext );
      } else if ( aObject instanceof ILcdEllipse ) {
        return fEllipsePaintableFactory.createPaintable( aGLDrawable, aObject, aMode, aContext );
      } else if ( aObject instanceof ILcdComplexPolygon ) {
        return fComplexPolygonPaintableFactory.createPaintable( aGLDrawable, aObject, aMode, aContext );
      } else if ( aObject instanceof ILcdArc ) {
        return fArcPaintableFactory.createPaintable( aGLDrawable, aObject, aMode, aContext );
      } else if ( aObject instanceof ILcdArcBand ) {
        return fArcBandPaintableFactory.createPaintable( aGLDrawable, aObject, aMode, aContext );
      } else if ( aObject instanceof TLcdLonLatBuffer ) {
        return fLonLatBufferPaintableFactory.createPaintable( aGLDrawable, aObject, aMode, aContext );
      } else if ( aObject instanceof ILcdVariableGeoBuffer ) {
        return fVariableGeoBufferPaintableFactory.createPaintable( aGLDrawable, aObject, aMode, aContext );
      } else if ( aObject instanceof ILcdPolygon ) {
        return fPolygonPaintableFactory.createPaintable( aGLDrawable, aObject, aMode, aContext );
      } else if ( aObject instanceof ILcdPolyline ) {
        return fPolylinePaintableFactory.createPaintable( aGLDrawable, aObject, aMode, aContext );
      } else if ( aObject instanceof ILcdRing ) {
        return fRingPaintableFactory.createPaintable( aGLDrawable, aObject, aMode, aContext );
      } else if ( aObject instanceof ILcdCurve ) {
        return fCurvePaintableFactory.createPaintable( aGLDrawable, aObject, aMode, aContext );
      } else if ( aObject instanceof ILcdSurface ) {
        return fSurfacePaintableFactory.createPaintable( aGLDrawable, aObject, aMode, aContext );
      }
    }
    return null;
  }

  public boolean isModeSupported( TLcdGLPaintableFactoryMode aMode ) {
    return true;
  }
}

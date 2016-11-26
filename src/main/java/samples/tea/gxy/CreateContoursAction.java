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
package samples.tea.gxy;

import java.awt.Component;
import java.awt.event.ActionEvent;

import javax.swing.JDialog;

import com.luciad.contour.ILcdContourBuilder;
import com.luciad.contour.TLcdComplexPolygonContourFinder;
import com.luciad.contour.TLcdLonLatComplexPolygonContourBuilder;
import com.luciad.contour.TLcdXYComplexPolygonContourBuilder;
import com.luciad.format.raster.ILcdRaster;
import com.luciad.format.raster.ILcdTile;
import com.luciad.format.raster.TLcdDTEDTileDecoder;
import com.luciad.geodesy.TLcdGeodeticDatum;
import com.luciad.gui.ALcdAction;
import com.luciad.gui.TLcdImageIcon;
import com.luciad.model.ILcdModel;
import com.luciad.model.TLcd2DBoundsIndexedModel;
import com.luciad.model.TLcdModelDescriptor;
import com.luciad.reference.TLcdGeodeticReference;
import com.luciad.shape.ILcdBounds;
import com.luciad.shape.shape2D.ILcd2DEditableBounds;
import com.luciad.shape.shape2D.TLcdLonLatBounds;
import com.luciad.shape.shape3D.TLcdLonLatHeightBounds;
import com.luciad.tea.ILcdLineOfSightCoverage;
import com.luciad.tea.TLcdRasterMatrix;
import com.luciad.util.ILcdFireEventMode;
import com.luciad.util.ILcdFunction;
import com.luciad.util.ILcdStatusListener;
import com.luciad.view.gxy.ILcdGXYView;

import samples.gxy.common.ProgressUtil;
import samples.gxy.common.layers.GXYLayerUtil;

/**
 * This is an extension of <code>ALcdAction</code> that allows the user to
 * create the contour lines of a DTED.
 *
 * @see <code>ALcdAction</code>
 * @see <code>ILcdLineOfSightCoverage</code>
 */
class CreateContoursAction extends ALcdAction implements Runnable {

  private TLcdComplexPolygonContourFinder fContourFinder = new TLcdComplexPolygonContourFinder() {
    @Override
    protected boolean isSpecialValue( double aValue ) {
      //Use values smaller than or equal to return aValue <= TLcdDTEDTileDecoder.UNKNOWN_ELEVATION + 10 as special values, such as "unknown"
      return aValue <= TLcdDTEDTileDecoder.UNKNOWN_ELEVATION + 10;
    }
  };

  private ILcdGXYView          fGXYView;
  private ILcdBounds           fBounds;
  private ILcd2DEditableBounds f2DEditableBounds;
  private TeaLayerFactory      fLayerFactory = new TeaLayerFactory();
  private ILcdRaster           fRaster;

  public CreateContoursAction() {
    setIcon(new TLcdImageIcon("images/gui/i16_eyes.gif"));
    setShortDescription( "Create contours over the view area" );
  }

  /**
   * Sets the <code>ILcdGXYView</code> this action acts on.
   *
   * @param aGXYView The <code>ILcdGXYView</code> to act on.
   */
  public void setGXYView( ILcdGXYView aGXYView ) {
    fGXYView = aGXYView;
  }

  /**
   * Sets the bounds to compute the contours in.
   *
   * @param aBounds The bounds to compute the contours in.
   */
  public void setBounds( ILcdBounds aBounds ) {
    fBounds = aBounds;

    f2DEditableBounds = fBounds.cloneAs2DEditableBounds();
    double factor = 0.2;
    f2DEditableBounds.move2D( fBounds.getLocation().getX() - ( factor * fBounds.getWidth () ),
                              fBounds.getLocation().getY() - ( factor * fBounds.getHeight() ) );
    f2DEditableBounds.setWidth( ( 1 + ( 2 * factor ) ) * fBounds.getWidth() );
    f2DEditableBounds.setHeight( ( 1 + ( 2 * factor ) ) * fBounds.getHeight() );
  }

  /**
   * Sets the raster from which altitude information will be retrieved.
   *
   * @param aRaster the raster from which to retrieve altitude information. The bounds
   *                should be contained in this raster.
   */
  public void setRaster( ILcdRaster aRaster ) {
    fRaster = aRaster;
  }

  public void actionPerformed( ActionEvent aActionEvent ) {
    if ( fRaster != null && fBounds != null ) {
      Thread thread = new Thread( this );
      thread.setPriority( Thread.MIN_PRIORITY );
      thread.start();
    }
  }

  public void run() {

    JDialog dialog = ProgressUtil.createProgressDialog(
            (Component) fGXYView, "Creating contour levels."
    );
    try {
      ProgressUtil.showDialog( dialog );
      fContourFinder.addStatusListener( (ILcdStatusListener) dialog );

      // make the bounds somewhat bigger.
      TLcd2DBoundsIndexedModel model = new TLcd2DBoundsIndexedModel( f2DEditableBounds );
      model.setModelReference( new TLcdGeodeticReference( new TLcdGeodeticDatum() ) );
      model.setModelDescriptor( new TLcdModelDescriptor( "", "", "Contour" ) );

      // Computation of dimension
      ILcdTile tile = fRaster.retrieveTile( 0, 0 );

      double raster_factor = 1.0;
      double dimension_x_double = raster_factor * fBounds.getWidth() * ( tile.getWidth() / fRaster.getTileWidth() );
      double dimension_y_double = raster_factor * fBounds.getHeight() * ( tile.getHeight() / fRaster.getTileHeight() );

      TLcdRasterMatrix zm = new TLcdRasterMatrix( fRaster, fBounds, (int) dimension_x_double, (int) dimension_y_double );
      zm.setDefaultValue( ILcdLineOfSightCoverage.LOS_UNKNOWN_VALUE );

      ILcdContourBuilder contourBuilder;
      if ( fBounds instanceof TLcdLonLatBounds || fBounds instanceof TLcdLonLatHeightBounds ) {
        contourBuilder = new TLcdLonLatComplexPolygonContourBuilder( new MyFunction( model ));
      }
      else {
        contourBuilder = new TLcdXYComplexPolygonContourBuilder( new MyFunction( model ));
      }
      /*
      Use the interval mode HIGHER, this creates overlapping complex polygons. If you want to draw
      the complex polygons translucent, then interval mode INTERVAL can be used to create disjoint
      complex polygons, but since each complex polygon will then have holes in it and more segments,
      it will be drawn slower.
      */
      fContourFinder.findContours( contourBuilder, zm, TLcdComplexPolygonContourFinder.IntervalMode.HIGHER, TeaLayerFactory.getContourLevelsInterval(), TeaLayerFactory.getContourLevelsSpecial());

      GXYLayerUtil.addGXYLayer( fGXYView, fLayerFactory.createGXYLayer( model ) );
    }
    finally {
      fContourFinder.removeStatusListener( (ILcdStatusListener) dialog );
      ProgressUtil.hideDialog( dialog );
    }
  }
  private static class MyFunction implements ILcdFunction {

    private ILcdModel fModel;

    public MyFunction( ILcdModel aModel ) {
      fModel = aModel;
    }

    public boolean applyOn( Object aObject ) throws IllegalArgumentException {
      fModel.addElement( aObject, ILcdFireEventMode.NO_EVENT );
      return true;
    }
  }
}

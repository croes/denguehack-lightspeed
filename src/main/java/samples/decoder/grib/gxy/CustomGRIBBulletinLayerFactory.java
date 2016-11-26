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
package samples.decoder.grib.gxy;

import com.luciad.format.grib.TLcdGRIBBulletinModelDescriptor;
import com.luciad.model.ILcdModelContainer;
import com.luciad.view.gxy.ILcdGXYLayerFactory;
import com.luciad.view.gxy.ILcdGXYLayer;
import com.luciad.view.gxy.TLcdGXYLayer;
import com.luciad.model.ILcdModel;
import com.luciad.format.grib.TLcdGRIBModelDescriptor;
import com.luciad.format.raster.TLcdRasterPainter;
import com.luciad.view.gxy.TLcdGXYLayerTreeNode;
import samples.decoder.grib.GRIBCustomization;
import samples.gxy.decoder.MapSupport;

import java.awt.*;

/**
 * This is an example of a ILcdGXYLayerFactory for GRIB models
 * contained in a GRIB bulletin file. The models typically contain
 * the same type of data but at different levels, times, ...
 */
public class CustomGRIBBulletinLayerFactory implements ILcdGXYLayerFactory {

  // Colors associated with min/max levels of relative humidity.
  private static final Color MIN_COLOR = new Color(0, 0, 255);
  private static final Color MAX_COLOR = new Color(200, 0, 0);

  public ILcdGXYLayer createGXYLayer(ILcdModel aModel) {

    if (aModel.getModelDescriptor() instanceof TLcdGRIBBulletinModelDescriptor ) {

      if (aModel instanceof ILcdModelContainer ) {

        TLcdGXYLayerTreeNode bulletin = new TLcdGXYLayerTreeNode( "Relative humidity" );
        ILcdModelContainer models = (ILcdModelContainer ) aModel;

        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;

        for (int i = 0; i < models.modelCount(); ++i) {
          ILcdModel model = models.getModel( i );
          TLcdGRIBModelDescriptor descriptor = (TLcdGRIBModelDescriptor) model.getModelDescriptor();
          min = Math.min( min, descriptor.getLevelValue() );
          max = Math.max( max, descriptor.getLevelValue() );
        }

        for (int i = 0; i < models.modelCount(); ++i) {

          ILcdModel model = models.getModel( i );
          // Get the descriptor.
          TLcdGRIBModelDescriptor descriptor = (TLcdGRIBModelDescriptor) model.getModelDescriptor();

          // Only accept relative humidity
          if (descriptor.getParameterAbbreviation().equals("R H")) {

            // Create a raster painter.
            TLcdRasterPainter painter = new TLcdRasterPainter();
            painter.setFillOutlineArea( true );

            // Set a color model. At each level a different basic color is used.
            Color basicColor = getColorForLevel( descriptor.getLevelValue(), min, max );
            painter.setColorModel( GRIBCustomization.createPercentageColorModel( descriptor, basicColor ) );

            // Create the layer.
            TLcdGXYLayer layer = new TLcdGXYLayer();
            layer.setModel( model );
            layer.setSelectable( false );
            layer.setEditable( false );
            layer.setLabeled( false );
            layer.setVisible( true );
            layer.setGXYPainterProvider( painter );

            // Creating GRIB layers that contain humidity data for different levels.
            layer.setLabel( descriptor.getParameterAbbreviation() + " at " + descriptor.getLevelValue() );

            // Set a suitable pen on the layer.
            layer.setGXYPen( MapSupport.createPen( model.getModelReference() ) );

            bulletin.addLayer( layer );

            // ...

          }

        }
        return bulletin;
      }

    }
    return null;
  }

  /**
   * Gets the base color for displaying humidity values at a certain level.
   *@param aLevel the level for which a Color must be returned .  @return the <CODE>Color</CODE> for displaying humidity values at a certain level.
   */
  public Color getColorForLevel( float aLevel, int aMin, int aMax ) {
    double fraction = (aLevel - aMin)/(aMax - aMin);
    if (fraction < 0) fraction = 0;
    if (fraction > 1) fraction = 1;
    return getColorGradient(MIN_COLOR,MAX_COLOR,fraction);
  }

  /**
   * Gets a color which is an interpolation between the start and end color at the specified fraction.
   *
   * @param aStartColor  the start color for the interpolation.
   * @param aEndColor  the end color for the interpolation.
   * @param aFraction  the fraction, must be in range [0,1].
   * @return the interpolated color.
   */
  public Color getColorGradient(Color aStartColor, Color aEndColor, double aFraction) {
    float[] start_HSB = Color.RGBtoHSB(aStartColor.getRed(),aStartColor.getGreen(),aStartColor.getBlue(),null);
    float[] end_HSB   = Color.RGBtoHSB(aEndColor.getRed(),aEndColor.getGreen(),aEndColor.getBlue(),null);
    float f0 = (float)(start_HSB[0] + (end_HSB[0] - start_HSB[0]) * aFraction);
    float f1 = (float)(start_HSB[1] + (end_HSB[1] - start_HSB[1]) * aFraction);
    float f2 = (float)(start_HSB[2] + (end_HSB[2] - start_HSB[2]) * aFraction);
    return Color.getHSBColor(f0,f1,f2);
  }

}

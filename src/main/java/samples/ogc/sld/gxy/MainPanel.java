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
package samples.ogc.sld.gxy;

import javax.swing.JPanel;

import com.luciad.shape.ILcdBounds;
import com.luciad.shape.shape2D.TLcdLonLatBounds;

import samples.common.SampleData;
import samples.gxy.common.GXYSample;
import samples.gxy.common.layers.GXYDataUtil;
import samples.ogc.sld.SLDFeatureTypeStyleStore;

/**
 * A sample to demonstrate OGC's Styled Layer Descriptor (SLD) styles in an ILcdGXYView view,
 * The sample loads various vector and raster layers, styling them using SLD.
 * Styles are created using the {@link SLDFeatureTypeStyleStore} sample class.
 * The created styles are applied to an existing LuciadLightspeed layer by creating and
 * setting a {@link com.luciad.ogc.sld.view.gxy.TLcdSLDGXYPainterFactory}.
 * This is done in the StylePanel class.
 */
public class MainPanel extends GXYSample {

  @Override
  protected ILcdBounds getInitialBounds() {
    return new TLcdLonLatBounds(-110, 20, 40, 30);
  }

  @Override
  protected JPanel createSettingsPanel() {
    SLDFeatureTypeStyleStore stylesStore = new SLDFeatureTypeStyleStore("");
    return new StylePanel(getView(), getSelectedLayers(), stylesStore);
  }

  @Override
  protected void addData() {
    // load the background data
    // these are the original layers to which the style will be applied
    GXYDataUtil.instance().model(SampleData.COUNTRIES).layer().label("Countries").selectable(false).addToView(getView());
    GXYDataUtil.instance().model(SampleData.ALPS_ELEVATION).layer().label("Alps").addToView(getView());
    GXYDataUtil.instance().model(SampleData.US_STATES).layer().label("States").addToView(getView());
    GXYDataUtil.instance().model(SampleData.US_RIVERS).layer().label("Rivers").addToView(getView());
    GXYDataUtil.instance().model(SampleData.US_CITIES).layer().label("Cities").addToView(getView());
    GXYDataUtil.instance().model(SampleData.LAS_VEGAS_2003).layer().label("Las Vegas").addToView(getView());
  }

  public static void main(final String[] aArgs) {
    startSample(MainPanel.class, "Styled Layer Descriptor (SLD)");
  }
}

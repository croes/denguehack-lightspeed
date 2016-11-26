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
package samples.lightspeed.style.raster.gui;

import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JPanel;

import com.luciad.gui.swing.TLcdColorMapCustomizer;
import com.luciad.text.TLcdAltitudeFormat;
import com.luciad.util.TLcdAltitudeUnit;
import com.luciad.util.TLcdColorMap;

import samples.gxy.common.TitledPanel;
import samples.lightspeed.style.raster.RasterLayerFactory;
import samples.lightspeed.style.raster.RasterStyler;

/**
 * Panel to configure the color mapping for elevation data.
 * <p/>
 * This panel contains a TLcdColorMapCustomizer (the gui to edit the colors)
 * and an apply button. When the apply button is pressed, a new color map is set to the raster styler.
 */
public class ColorMapCustomizerPanel extends JPanel implements PropertyChangeListener {

  private RasterStyler fStyler;
  private TLcdColorMapCustomizer fCustomizer = new TLcdColorMapCustomizer();
  private JPanel fColorPanel = new JPanel(new BorderLayout());
  private TLcdColorMap fDefaultColorMap = RasterLayerFactory.createDefaultElevationColorMap();

  public ColorMapCustomizerPanel() {
    // Set a format to the customizer that nicely formats the values. The
    // program unit is meters, because dmed data is internally stored in
    // meters (cfr. Grib remark in createDefaultDMEDColorMap). The display
    // unit is also meters, but it could be something else.
    TLcdAltitudeFormat level_format = new TLcdAltitudeFormat(
        TLcdAltitudeUnit.METRE, TLcdAltitudeUnit.METRE
    );
    level_format.setFractionDigits(0);
    fCustomizer.setLevelFormat(level_format);
    fCustomizer.addPropertyChangeListener(this);

    TitledPanel titled_color_panel = TitledPanel.createTitledPanel(
        "Raster Elevation Style", fColorPanel, TitledPanel.NORTH
    );

    setLayout(new BorderLayout());
    add(titled_color_panel, BorderLayout.CENTER);
  }

  public void setStyler(RasterStyler aStyler) {
    fStyler = null;

    Util.setEnabledRecursive(this, aStyler != null);
    if (aStyler == null) {
      return;
    }

    TLcdColorMap colorMap = aStyler.getColorMap();

    Util.setEnabledRecursive(this, colorMap != null);
    if (colorMap == null) {
      return;
    }

    // Set a clone of the TLcdColorMap to avoid possible interference.
    fCustomizer.setDefaultColorMap(fDefaultColorMap);
    fCustomizer.setObject(colorMap.clone());
    fCustomizer.setMasterTransparencyVisible(false);
    fCustomizer.invalidate();
    fColorPanel.add(fCustomizer, BorderLayout.CENTER);
    fColorPanel.invalidate();

    fStyler = aStyler;
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
    if (fStyler == null) {
      return;
    }
    // Set a new color map to the style that reflects the changes made by the user.
    fStyler.setColorMap(
        //make a clone of the object to avoid possible interference
        (TLcdColorMap) fCustomizer.getColorMap().clone()
    );
  }
}

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
package samples.decoder.ecdis.gxy;

import java.io.IOException;

import javax.swing.JPanel;

import com.luciad.format.s52.TLcdS52DisplaySettings;
import com.luciad.format.s57.TLcdS57UnifiedModelDecoder;
import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelDecoder;
import com.luciad.view.gxy.ILcdGXYLayer;
import com.luciad.view.gxy.ILcdGXYLayerFactory;

import samples.decoder.ecdis.common.S52DisplaySettingsCustomizer;
import samples.decoder.ecdis.common.S52DisplaySettingsSingleton;
import samples.gxy.common.layers.GXYLayerUtil;

/**
 * This sample demonstrates how to load and display S-57 data on a map using the S-52 symbology.
 * Both ENC and AML data are supported.
 */
public class MainPanel extends samples.gxy.decoder.MainPanel {

  private final TLcdS52DisplaySettings fS52DisplaySettings = S52DisplaySettingsSingleton.getSettings();
  private final ILcdModelDecoder fModelDecoder = createModelDecoder();
  private final ILcdGXYLayerFactory fLayerFactory = createLayerFactory();

  protected ILcdModelDecoder createModelDecoder() {
    return new TLcdS57UnifiedModelDecoder();
  }

  protected ILcdGXYLayerFactory createLayerFactory() {
    return new S52GXYLayerFactory();
  }

  @Override
  protected JPanel createSettingsPanel() {
    return new S52DisplaySettingsCustomizer(fS52DisplaySettings, false);
  }

  @Override
  protected void addData() throws IOException {
    super.addData();
    loadSampleData();
  }

  protected void loadSampleData() throws IOException {
    // Load the ECDIS model.
    ILcdModel model = fModelDecoder.decode("" + "Data/Ecdis/Unencrypted/US5WA51M/US5WA51M.000");
    ILcdGXYLayer layer = fLayerFactory.createGXYLayer(model);
    GXYLayerUtil.addGXYLayer(getView(), layer);
    GXYLayerUtil.fitGXYLayer(getView(), layer);
  }

  protected ILcdModelDecoder getModelDecoder() {
    return fModelDecoder;
  }

  protected ILcdGXYLayerFactory getLayerFactory() {
    return fLayerFactory;
  }

  public TLcdS52DisplaySettings getS52DisplaySettings() {
    return fS52DisplaySettings;
  }

  public static void main(final String[] aArgs) {
    startSample(MainPanel.class, "Decoding ECDIS S-57");
  }
}

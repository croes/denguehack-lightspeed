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
package samples.decoder.ecdis.s63.gxy;

import static java.util.Arrays.asList;

import java.io.IOException;

import com.luciad.format.s63.TLcdS63UnifiedModelDecoder;
import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelDecoder;
import com.luciad.view.gxy.ILcdGXYLayer;

import samples.common.serviceregistry.ServiceRegistry;
import samples.decoder.ecdis.s63.SelectPermitFileAction;
import samples.decoder.ecdis.s63.ShowSAPublicKeyAction;
import samples.decoder.ecdis.s63.ShowUserPermitAction;
import samples.gxy.common.layers.GXYLayerUtil;
import samples.gxy.common.toolbar.ToolBar;

/**
 * This sample demonstrates how to load and display ECDIS data on a map using the S-52 symbology.
 * <p/>
 * The LuciadLightspeed distribution contains no multileveled ECDIS data. NOAA, the U.S. National
 * Oceanic
 * & Atmospheric Administration provides a large dataset on its website, which can be found at
 * http://chartmaker.ncd.noaa.gov/mcd/enc/download.htm
 */
public class MainPanel extends samples.decoder.ecdis.gxy.MainPanel {

  private TLcdS63UnifiedModelDecoder fModelDecoder;

  @Override
  protected ILcdModelDecoder createModelDecoder() {
    if (fModelDecoder == null) {
      fModelDecoder = new TLcdS63UnifiedModelDecoder();
      ServiceRegistry.getInstance().register(fModelDecoder, ServiceRegistry.HIGH_PRIORITY);
    }
    return fModelDecoder;
  }

  @Override
  protected void createGUI() {
    super.createGUI();
    ToolBar toolBar = getToolBars()[0];
      toolBar.addAction(new SelectPermitFileAction(fModelDecoder));
      toolBar.addAction(new ShowUserPermitAction(fModelDecoder));
      toolBar.addAction(new ShowSAPublicKeyAction(fModelDecoder));
  }

  @Override
  protected void loadSampleData() throws IOException {
    try {
      // Load the ECDIS model.
      String permit = System.getProperty("com.luciad.format.s63.permit", "Data/Ecdis/Encrypted/NO5F1615/ENC.PMT");
      if (permit.endsWith("PERMIT.TXT") || permit.endsWith("permit.txt")) {
        fModelDecoder.setMetaPermitSources(asList(permit));
      } else if (permit.endsWith("ENC.PMT") || permit.endsWith("enc.pmt")) {
        fModelDecoder.setBasicPermitSources(asList(permit)); // not strictly necessary as TLcdS63UnifiedModelDecoder automatically picks this up.
      }
      ILcdModel model = fModelDecoder.decode("" + "Data/Ecdis/Encrypted/NO5F1615/NO5F1615.000");
      ILcdGXYLayer layer = getLayerFactory().createGXYLayer(model);
      GXYLayerUtil.addGXYLayer(getView(), layer);
      GXYLayerUtil.fitGXYLayer(getView(), layer);
    } catch (IOException e) {
      System.out.println("Could not load S-63 data.");
    }
  }

  public static void main(final String[] aArgs) {
    startSample(MainPanel.class, "Decoding ECDIS S-63");
  }
}

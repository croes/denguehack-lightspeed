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
package samples.gxy.decoder.raster.custom;

import java.io.IOException;
import java.util.Collections;

import com.luciad.view.gxy.ILcdGXYLayerFactory;

import samples.common.formatsupport.GXYOpenSupport;
import samples.gxy.common.GXYSample;
import samples.gxy.common.layers.factories.RasterLayerFactory;

/**
 * This sample demonstrates how to decode raster data that is encoded in a format that LuciadMap
 * does not support out of the box.
 *
 * The sample does not actually decode data from disk. Instead it generates elevation data in
 * memory.
 */
public class MainPanel extends GXYSample {

  private ILcdGXYLayerFactory fLayerFactory = new RasterLayerFactory();
  private CustomRasterDecoder fModelDecoder = new CustomRasterDecoder();

  @Override
  protected void addData() throws IOException {
    super.addData();
    GXYOpenSupport openSupport = new GXYOpenSupport(
        getView(),
        Collections.singletonList(fModelDecoder),
        Collections.singletonList(fLayerFactory));
    openSupport.openSource("DUMMY");
  }

  public static void main(final String[] aArgs) {
    startSample(MainPanel.class, "Decoding a custom raster");
  }
}

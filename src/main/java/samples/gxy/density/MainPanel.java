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
package samples.gxy.density;

import java.io.IOException;
import java.util.Collections;

import com.luciad.format.shp.TLcdSHPModelDecoder;

import samples.common.formatsupport.GXYOpenSupport;
import samples.gxy.common.GXYSample;

/**
 * This sample demonstrates how to display color-coded densities of data, by
 * means of a TLcdGXYDensityLayer.
 * <p/>
 * The sample shows a set of flight trajectories above USA, with colors ranging
 * from blue, for quiet regions, to red, for busy regions.
 */
public class MainPanel extends GXYSample {

  @Override
  protected void addData() throws IOException {
    super.addData();

    // Create a model decoder for our trajectories.
    TLcdSHPModelDecoder modelDecoder = new TLcdSHPModelDecoder();

    // Add the data to the view.
    GXYOpenSupport openSupport = new GXYOpenSupport(getView(),
                                                    Collections.singletonList(modelDecoder),
                                                    Collections.singletonList(new DensityLayerFactory())
    );
    openSupport.addStatusListener(getStatusBar());
    openSupport.openSource("" + "Data/Shp/Usa/trajectories.shp");
  }

  public static void main(final String[] aArgs) {
    startSample(MainPanel.class, "Element densities");
  }
}

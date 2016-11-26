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
package samples.lightspeed.lidar;

import java.io.IOException;

import com.luciad.format.las.TLcdLASModelDecoder;
import samples.lightspeed.lidar.LASStyler;
import samples.lightspeed.lidar.StyleModel;
import samples.lightspeed.lidar.StylePanel;
import samples.lightspeed.lidar.SupportedLayerIconListener;
import com.luciad.view.lightspeed.ILspAWTView;
import com.luciad.view.lightspeed.ILspView;

import samples.common.serviceregistry.ServiceRegistry;
import samples.gxy.common.TitledPanel;

/**
 * This sample illustrates how Lidar data from .las files can be visualized in a Lightspeed view.
 * <p>
 * This sample uses:
 * <ul>
 *   <li>{@link LASStyler}: a styler for .las data that supports various styling options.
 *   <li>{@link LASLayerFactory}: a layer factory for .las data that uses the styler above.
 *   <li>{@link StylePanel}: GUI to select different styling options.
 *   <li>{@link StyleModel}: a object responsible to apply the different styling options to the layers
 * </ul>
 * </p>
 * <p>
 * See http://www.asprs.org/Committee-General/LASer-LAS-File-Format-Exchange-Activities.html for details on the LASer format.
 * </p>
 */
public class MainPanel extends samples.lightspeed.decoder.MainPanel {

  public MainPanel(String[] aArgs) {
    super(aArgs);
  }

  @Override
  protected ILspAWTView createView() {
    return super.createView(ILspView.ViewType.VIEW_3D);
  }

  @Override
  protected void createGUI() {
    super.createGUI();
    StyleModel model = new StyleModel(getView());
    StylePanel stylePanel = new StylePanel(model);
    TitledPanel titledPanel = TitledPanel.createTitledPanel("Styling", stylePanel);

    SupportedLayerIconListener.plug(model);
    addComponentToRightPanel(titledPanel);
  }

  @Override
  protected void addData() throws IOException {
    super.addData();

    // Make sure we load the full dataset (which contains about 3.8 million points). When this value is increased, it
    // allows to see a denser version of the data at the expense of reduced painting performance and increased
    // (CPU or GPU) memory usage. Lowering this value allows to visualize models that don't fit in memory or larger
    // collections of models. See also performance note in TLspLIDARLayerBuilder.
    TLcdLASModelDecoder modelDecoder = new TLcdLASModelDecoder();
    modelDecoder.setMaxNumberOfPoints(5000000);
    ServiceRegistry.getInstance().register(modelDecoder);

    openSourceOnMap("Data/LAS/terrain_with_buildings.las");
  }

  public static void main(String[] args) {
    startSample(MainPanel.class, args, "LASer decoder");
  }
}

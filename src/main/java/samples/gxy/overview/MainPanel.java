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
package samples.gxy.overview;

import java.io.IOException;

import javax.swing.JPanel;

import com.luciad.shape.ILcdBounds;
import com.luciad.shape.shape2D.TLcdLonLatBounds;

import samples.common.SampleData;
import samples.gxy.common.GXYSample;
import samples.gxy.common.layers.GXYDataUtil;

/**
 * This sample demonstrates the use of a TLcdGXYOverviewController and the
 * sharing of ILcdGXYLayer objects between multiple ILcdGXYView objects.
 * <p/>
 * It consists of one main view in the center and an overview map in the top right.
 * Dragging a Rectangle in the
 * overview makes the main view displaying data that are in the dragged
 * Rectangle. The overview displays also a Rectangle that corresponds as much as
 * possible to the area visible in the main view. This Rectangle is updated each
 * time the visible area changes (e.g. when panning or zooming, etc).
 * The overview displays the same ILcdModels using the same ILcdGXYLayers used
 * in the main view ( OverviewPanelSW.isSynchronizeLayers() == true ). This is achieved
 * by adding MyLayeredListener in OverviewPanelSW to the main view as an
 * ILcdLayeredListener. This class is notified whenever an ILcdGXYLayer is added
 * to the main view or moved up or down in the main view and will then apply the
 * the same action in the overview.
 * Since an ILcdGXYView automatically listens to any property change of its
 * ILcdGXYLayer objects, any change of those properties via the layercontrol
 * will show in both views.
 */
public class MainPanel extends GXYSample {

  private OverviewPanelSW fOverviewPanel = new OverviewPanelSW();

  @Override
  protected ILcdBounds getInitialBounds() {
    return new TLcdLonLatBounds(-95.00, 27.50, 25.00, 20.00);
  }

  @Override
  protected void createGUI() {
    super.createGUI();
    fOverviewPanel.setSlaveGXYView(getView());
  }

  @Override
  protected JPanel createSettingsPanel() {
    return fOverviewPanel;
  }

  @Override
  protected void addData() throws IOException {
    super.addData();
    GXYDataUtil.instance().model(SampleData.US_RIVERS).layer().label("Rivers").addToView(getView());
  }

  public static void main(final String[] aArgs) {
    startSample(MainPanel.class, "Overview map");
  }
}

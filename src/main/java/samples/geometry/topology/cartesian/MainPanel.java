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
package samples.geometry.topology.cartesian;

import javax.swing.JPanel;

import com.luciad.view.map.TLcdMapJPanel;

import samples.common.SampleData;
import samples.geometry.util.TopologyLegend;
import samples.geometry.util.TopologyRiversLayerFactory;
import samples.geometry.util.TopologySelectionListener;
import samples.geometry.util.TopologyStatesLayerFactory;
import samples.gxy.common.GXYSample;
import samples.gxy.common.layers.GXYDataUtil;

/**
 * This sample demonstrates topological relations between states and rivers using cartesian topology.
 */
public class MainPanel extends GXYSample {

  @Override
  protected JPanel createSettingsPanel() {
    return TopologyLegend.createTopologyLegend();
  }

  protected void addData() {
    TopologySelectionListener listener = new TopologySelectionListener();
    TLcdMapJPanel view = getView();
    GXYDataUtil.instance().model(SampleData.COUNTRIES).layer().label("Countries").selectable(false).addToView(view).getLayer();
    GXYDataUtil.instance().model(SampleData.US_STATES).layer(new TopologyStatesLayerFactory(listener)).label("States").addToView(view).fit();
    GXYDataUtil.instance().model(SampleData.US_RIVERS).layer(new TopologyRiversLayerFactory(listener)).label("Rivers").addToView(view);
  }

  public static void main( final String[] aArgs ) {
    startSample(MainPanel.class, "Cartesian Geometry");
  }
}

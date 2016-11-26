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
package samples.lightspeed.touch.touchEvents;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.JButton;

import com.luciad.geodesy.TLcdGeodeticDatum;
import com.luciad.gui.TLcdIconFactory;
import com.luciad.projection.TLcdEquidistantCylindrical;
import com.luciad.reference.TLcdGridReference;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.util.TLspViewTransformationUtil;

import samples.common.SampleData;
import samples.gxy.common.touch.TouchUtil;
import samples.lightspeed.common.FitUtil;
import samples.lightspeed.common.LightspeedSample;
import samples.lightspeed.common.LspDataUtil;

/**
 * This sample demonstrates the effect of touch events on a touch enabled Lightspeed view. It
 * contains a setup that simulates touch events and passes them to the Lightspeed view. The touch
 * controller set on the view is a navigation controller by default and will interpret the incoming
 * events by panning and zooming the view.
 * <p/>
 * The simulation can be started by pressing the space bar.
 */
public class MainPanel extends LightspeedSample {

  public MainPanel() {
    super(true); // we want a touch toolbar
  }

  @Override
  protected void createGUI() {
    super.createGUI();

    // Once everything is connected to the sample, adapt the look and feel.
    TouchUtil.setTouchLookAndFeel(this);
  }

  protected void addData() throws IOException {
    super.addData();

    final ILspLayer countries = LspDataUtil.instance()
                                           .model(SampleData.COUNTRIES)
                                           .layer().label("Countries")
                                           .addToView(getView())
                                           .fit()
                                           .getLayer();

    // Retrieve the host component of the view
    Component component = getView().getHostComponent();

    // Create a new TouchDevice that simulates touch hardware by creating a series of DeviceEvents.
    final TouchDevice simulation_device = new TouchDevice(12345);
    simulation_device.setRegisteredComponent(component);

    // Add a touch event factory as listener to the simulation device
    // (factory will convert device events to TLcdTouchEvents)
    TouchEventFactory event_factory = new TouchEventFactory(simulation_device.getDeviceID());
    simulation_device.addDeviceEventListener(event_factory);

    JButton button = new JButton("Simulate touch events");
    button.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        TLspViewTransformationUtil.setup2DView(getView(), new TLcdGridReference(new TLcdGeodeticDatum(), new TLcdEquidistantCylindrical()));
        FitUtil.fitOnLayers(MainPanel.this, countries);
        simulation_device.start();
      }
    });
    getTouchToolBars()[0].add(button, 7);
  }

  public static void main(final String[] aArgs) {
    TLcdIconFactory.setDefaultSize(TLcdIconFactory.Size.SIZE_32);
    startSample(MainPanel.class, "Touch events");
  }

}

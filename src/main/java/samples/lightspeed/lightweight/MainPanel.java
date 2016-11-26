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
package samples.lightspeed.lightweight;

import java.awt.BorderLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;

import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.OverlayLayout;

import com.luciad.gui.swing.TLcdOverlayLayout;
import com.luciad.util.TLcdSystemPropertiesUtil;
import com.luciad.view.lightspeed.ILspAWTView;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.TLspSwingView;
import com.luciad.view.lightspeed.TLspViewBuilder;

import samples.common.SampleData;
import samples.lightspeed.common.LightspeedSample;
import samples.lightspeed.common.LspDataUtil;

/**
 * This sample demonstrates how Swing components can be overlaid on an ILspView.
 * Specifically, the sample shows a JInternalFrame which contains a secondary
 * map view, alongside with navigation controls for the main view.
 */
public class MainPanel extends LightspeedSample {

  private TLspSwingView fInternalView;

  @Override
  protected ILspAWTView createView() {
    // Create a lightweight Swing view.
    return TLspViewBuilder.newBuilder().buildSwingView();
  }

  @Override
  protected void addData() throws IOException {
    super.addData();
    LspDataUtil.instance().model(SampleData.COUNTRIES).layer().label("Countries").addToView(getView());
  }

  /**
   * Creates components to be overlaid on the view. This method creates a JLayeredPane. In it,
   * the first layer is a JDesktopPane, on which another TLspSwingView is shown in a
   * JInternalFrame. The second layer is a JPanel with a TLcdOverlayLayout, which shows
   * navigation controls for the main view (as can also be seen in other samples).
   *
   * @param aOverlayPanel the view's overlay component
   */
  @Override
  protected void addOverlayComponents(JComponent aOverlayPanel) {
    // Create another Swing view to be overlaid on the main view
    fInternalView = TLspViewBuilder.newBuilder()
                                   .viewType(ILspView.ViewType.VIEW_3D)
                                   .buildSwingView();
    LspDataUtil.instance().model(SampleData.SAN_FRANCISCO).layer().addToView(fInternalView);
    LspDataUtil.instance().grid().addToView(fInternalView).fit();

    // Put the second view in an internal frame
    JInternalFrame internalFrame = new JInternalFrame("Internal frame", true, false, true, true);
    internalFrame.add(fInternalView.getHostComponent());
    internalFrame.setBounds(10, 10, 250, 250);
    internalFrame.setVisible(true);

    // Create a desktop and add the internal frame
    JDesktopPane desktop = new JDesktopPane();
    desktop.setOpaque(false);
    desktop.add(internalFrame, JDesktopPane.MODAL_LAYER);

    boolean isMac = TLcdSystemPropertiesUtil.isMacOS();
    //In Mac OS X, JDesktop does not let some awt events to be dispatched
    //to the view. therefore we dispatch those two events manually
    if (isMac) {
      MouseAdapter mouseAdapter = new MouseAdapter() {
        @Override
        public void mousePressed(MouseEvent e) {
          getView().getHostComponent().dispatchEvent(e);
        }

        @Override
        public void mouseReleased(MouseEvent e) {
          getView().getHostComponent().dispatchEvent(e);
        }

        @Override
        public void mouseDragged(MouseEvent e) {
          getView().getHostComponent().dispatchEvent(e);
        }
      };
      
      desktop.addMouseListener(mouseAdapter);
      desktop.addMouseMotionListener(mouseAdapter);
    }

    // Create a panel with the navigation controls
    JPanel standardOverlays = new JPanel(new TLcdOverlayLayout());
    standardOverlays.setOpaque(false);
    super.addOverlayComponents(standardOverlays);

    // Add the desktop and the navigation controls to a JLayeredPane
    JLayeredPane layered = new JLayeredPane();
    layered.setOpaque(false);
    layered.setLayout(new OverlayLayout(layered));
    layered.add(standardOverlays, BorderLayout.CENTER, 0);
    layered.add(desktop, BorderLayout.CENTER, 1);

    // Put the JLayeredPane on the main view
    aOverlayPanel.setLayout(new BorderLayout());
    aOverlayPanel.add(layered, BorderLayout.CENTER);
  }

  @Override
  protected void tearDown() {
    if (fInternalView != null) {
      fInternalView.removeAllLayers();
      fInternalView.destroy();
      fInternalView = null;
    }
    super.tearDown();
  }

  public static void main(String[] aArgs) {
    startSample(MainPanel.class, "Lightweight view");
  }
}

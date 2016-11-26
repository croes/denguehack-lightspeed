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
package samples.lightspeed.navigationcontrols;

import java.awt.BorderLayout;
import java.awt.Component;
import java.io.IOException;

import javax.swing.JComponent;
import javax.swing.JPanel;

import com.luciad.format.magneticnorth.ALcdMagneticNorthModelDescriptor;
import com.luciad.gui.swing.TLcdOverlayLayout;
import samples.common.HaloLabel;
import com.luciad.model.ILcdModel;
import com.luciad.view.lightspeed.swing.navigationcontrols.TLspNavigationControlsBuilder;

import samples.common.SampleData;
import samples.gxy.magneticnorth.MagneticNorthModelFactory;
import samples.lightspeed.common.LightspeedSample;
import samples.lightspeed.common.LspDataUtil;

/**
 * This sample demonstrates how to use TLspNavigationControlsBuilder to create
 * Swing-based controls for view navigation and overlay these controls on a view.
 */
public class MainPanel extends LightspeedSample {

  @Override
  protected void addOverlayComponents(JComponent aOverlayPanel) {
    super.addOverlayComponents(aOverlayPanel);

    // Add a compass pointing to the magnetic north
    ILcdModel model = MagneticNorthModelFactory.createIGRFMagneticNorthModel("", this, null);
    ALcdMagneticNorthModelDescriptor modelDescriptor =
        (ALcdMagneticNorthModelDescriptor) model.getModelDescriptor();

    Component compassControl = TLspNavigationControlsBuilder.newBuilder(getView())
                                                            .compassNavigationControl()
                                                            .alwaysActive(true)
                                                            .magneticNorthMap(modelDescriptor.getMagneticNorthMap())
                                                            .build();

    JPanel compassPanel = new JPanel(new BorderLayout());
    compassPanel.setOpaque(false);
    compassPanel.add(new HaloLabel("Magnetic north"), BorderLayout.SOUTH);
    compassPanel.add(compassControl, BorderLayout.CENTER);

    aOverlayPanel.add(compassPanel, TLcdOverlayLayout.Location.NORTH_WEST);

    // Add pan navigation control
    Component panControl = TLspNavigationControlsBuilder.newBuilder(getView())
                                                        .panNavigationControl()
                                                        .alwaysActive(true)
                                                        .build();
    aOverlayPanel.add(panControl);
    TLcdOverlayLayout layout = (TLcdOverlayLayout) aOverlayPanel.getLayout();
    layout.putConstraint(panControl, TLcdOverlayLayout.Location.SOUTH_WEST, TLcdOverlayLayout.ResolveClash.VERTICAL);

    // Add zoom navigation control
    Component zoomControl = TLspNavigationControlsBuilder.newBuilder(getView())
                                                         .zoomNavigationControl()
                                                         .alwaysActive(true)
                                                         .build();
    aOverlayPanel.add(zoomControl, TLcdOverlayLayout.Location.WEST);
  }

  protected void addData() throws IOException {
    super.addData();
    LspDataUtil.instance().model(SampleData.COUNTRIES).layer().label("Countries").addToView(getView()).fit();
  }

  public static void main(final String[] aArgs) {
    startSample(MainPanel.class, "Navigation controls");
  }

}

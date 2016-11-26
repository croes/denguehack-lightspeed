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
package samples.lightspeed.limitnavigation;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

import samples.lightspeed.limitnavigation.CameraConstraintMediator;
import samples.lightspeed.limitnavigation.LimitNavigationConstraint2D;
import samples.lightspeed.limitnavigation.LimitNavigationConstraint3D;
import com.luciad.model.ILcdModel;
import com.luciad.model.TLcdModelDescriptor;
import com.luciad.model.TLcdVectorModel;
import com.luciad.reference.TLcdGeodeticReference;
import com.luciad.shape.ILcdBounds;
import com.luciad.shape.shape2D.TLcdLonLatBounds;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.style.TLspLineStyle;

import samples.gxy.common.TitledPanel;
import samples.lightspeed.common.LightspeedSample;
import samples.lightspeed.common.LspDataUtil;
import samples.lightspeed.decoder.UnstyledLayerFactory;

/**
 * This sample demonstrates how the view-world transformation can be customized in order to
 * constrain the navigation freedom.
 */
public class MainPanel extends LightspeedSample {

  private static final ILcdBounds AREA_OF_INTEREST = new TLcdLonLatBounds(-90.00, 25.00, 20.00, 15.00);

  private CameraConstraintMediator fCameraConstraintMediator;

  @Override
  protected void createGUI() {
    super.createGUI();

    LimitNavigationConstraint2D constraint2D = createConstraint2D(getView());
    LimitNavigationConstraint3D constraint3D = createConstraint3D(getView());

    fCameraConstraintMediator = new CameraConstraintMediator(getView(), constraint2D, constraint3D);

    // Add a panel containing a checkbox to enable/disable navigation constraints
    JPanel customPanel = new JPanel();
    customPanel.setLayout(new BoxLayout(customPanel, BoxLayout.X_AXIS));
    customPanel.add(createLimitNavigationCheckbox());

    JPanel titledPanel = TitledPanel.createTitledPanel("Options", customPanel);
    addComponentToRightPanel(titledPanel);
  }

  /**
   * Creates a new 2D world transformation constraint.
   * @param aView the view for which to get a constraint.
   * @return the 2D world transformation constraint.
   */
  private LimitNavigationConstraint2D createConstraint2D(ILspView aView) {
    LimitNavigationConstraint2D constraint2D = new LimitNavigationConstraint2D(aView);

    // Make sure north always faces up
    constraint2D.setUseAlwaysNorthUp(true);

    // Configure an area of interest
    constraint2D.setUseAreaOfInterest(true);
    constraint2D.setAreaOfInterest(AREA_OF_INTEREST, new TLcdGeodeticReference());

    // Make sure zooming in/out is limited
    constraint2D.setUseMinMaxScale(true);
    constraint2D.setMinScale(1e-5);
    constraint2D.setMaxScale(1e-3);

    return constraint2D;
  }

  /**
   * Creates a new 3D world transformation constraint.
   * @param aView the view for which to get a constraint.
   * @return the 3D world transformation constraint.
   */
  private LimitNavigationConstraint3D createConstraint3D(ILspView aView) {
    LimitNavigationConstraint3D constraint3D = new LimitNavigationConstraint3D(aView);

    // Make sure north always faces up
    constraint3D.setUseAlwaysNorthUp(true);

    // Make sure the the counties layer is always within the view
    constraint3D.setUseAreaOfInterest(true);
    constraint3D.setAreaOfInterest(AREA_OF_INTEREST, new TLcdGeodeticReference());

    // Make sure zooming in/out is limited
    constraint3D.setUseMinMaxHeight(true);
    constraint3D.setMinHeight(200000);
    constraint3D.setMaxHeight(10000000);

    return constraint3D;
  }

  /**
   * Creates a checkbox to enable or disable limited navigation.
   * @return the checkbox.
   */
  private Component createLimitNavigationCheckbox() {
    JCheckBox limitNavigationCheckBox = new JCheckBox("Limit Navigation", true);
    limitNavigationCheckBox.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        fCameraConstraintMediator.setConstrain(!fCameraConstraintMediator.isConstrain());
        getView().invalidate(true, this, "Limit Navigation");
      }
    });

    return limitNavigationCheckBox;
  }

  protected void addData() throws IOException {
    super.addData();

    // Display the area of interest outline.
    UnstyledLayerFactory areaOfInteresetLayerFactory = new UnstyledLayerFactory();
    areaOfInteresetLayerFactory.setFillStyle(null);
    areaOfInteresetLayerFactory.setLineStyle(TLspLineStyle.newBuilder().color(Color.ORANGE).build());

    LspDataUtil.instance().model(createAreaOfInterestModel()).layer(areaOfInteresetLayerFactory).addToView(getView()).fit();
  }

  private ILcdModel createAreaOfInterestModel() {
    ILcdModel areaModel = new TLcdVectorModel(new TLcdGeodeticReference(), new TLcdModelDescriptor("bounds", "bounds", "Area of interest"));
    areaModel.addElement(AREA_OF_INTEREST, ILcdModel.FIRE_NOW);
    return areaModel;
  }

  public static void main(final String[] aArgs) {
    startSample(MainPanel.class, "Limit Navigation");
  }

}

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
package samples.lightspeed.style.icon;

import java.awt.Component;
import java.io.IOException;
import java.util.Enumeration;

import javax.swing.JComponent;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.luciad.geodesy.TLcdGeodeticDatum;
import com.luciad.gui.swing.TLcdOverlayLayout;
import com.luciad.model.ILcdModel;
import com.luciad.model.TLcdModelDescriptor;
import com.luciad.model.TLcdVectorModel;
import com.luciad.reference.TLcdGeodeticReference;
import com.luciad.shape.shape3D.TLcdLonLatHeightPoint;
import com.luciad.util.ILcdFireEventMode;
import com.luciad.view.lightspeed.ILspAWTView;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.swing.navigationcontrols.TLspNavigationControlsBuilder;

import samples.lightspeed.common.LightspeedSample;
import samples.lightspeed.common.LabeledSliderPanel;
import samples.lightspeed.common.LspDataUtil;

/**
 * The icon style sample demonstrates the functionality of the TLspIconPainter, i.e.: rotation and
 * transparency of icons, drawing icons with a vertical line and drawing icons either with a size in
 * world coordinates or in pixel coordinates.
 */
public class MainPanel extends LightspeedSample {

  private ILcdModel fRotatableIconModel;

  @Override
  protected ILspAWTView createView() {
    return createView(ILspView.ViewType.VIEW_3D);
  }

  @Override
  protected void createGUI() {
    super.createGUI();
    fRotatableIconModel = createRotatableIconModel();
    getOverlayPanel().add(createRotationSlider(), TLcdOverlayLayout.Location.NORTH_WEST);
  }

  protected void addAltitudeExaggerationControl(JComponent aOverlayPanel) {
    getOverlayPanel().add(TLspNavigationControlsBuilder.newBuilder(getView()).altitudeExaggerationControl().build(), TLcdOverlayLayout.Location.SOUTH_WEST);
  }

  @Override
  protected void addData() throws IOException {
    super.addData();
    LayerFactory layerFactory = new LayerFactory();
    LspDataUtil.instance().model(createViewIconPointModel()).layer(layerFactory).addToView(getView()).fit();
    LspDataUtil.instance().model(createWorldIconPointModel()).layer(layerFactory).addToView(getView());
    LspDataUtil.instance().model(fRotatableIconModel).layer(layerFactory).addToView(getView());
  }

  private Component createRotationSlider() {
    JSlider slider = new JSlider(JSlider.VERTICAL, 0, 360, 0);
    slider.setMajorTickSpacing(90);
    slider.setMinorTickSpacing(10);
    slider.setPaintLabels(true);
    slider.setPaintTicks(true);
    slider.addChangeListener(new ChangeListener() {

      @Override
      public void stateChanged(ChangeEvent e) {
        JSlider source = (JSlider) e.getSource();
        double orientation = (double) source.getValue();
        Enumeration objects = fRotatableIconModel.elements();
        while (objects.hasMoreElements()) {
          OrientedLonLatHeightPoint point = (OrientedLonLatHeightPoint) objects.nextElement();
          point.setOrientation(orientation);
          fRotatableIconModel.elementChanged(point, ILcdFireEventMode.FIRE_LATER);
        }
        fRotatableIconModel.fireCollectedModelChanges();
      }
    });
    LabeledSliderPanel rotationSliderPanel = new LabeledSliderPanel("Rotation", slider);
    rotationSliderPanel.setOpaque(false);
    rotationSliderPanel.setFocusable(false);
    return rotationSliderPanel;
  }

  /**
   * Creates the model whose points will be used to draw icons with a fixed world size.
   *
   * @return the model whose points will be used to draw icons with a fixed world size.
   */
  private ILcdModel createWorldIconPointModel() {
    TLcdVectorModel model = new TLcdVectorModel();
    TLcdGeodeticDatum datum = new TLcdGeodeticDatum();
    model.setModelReference(new TLcdGeodeticReference(datum));
    model.setModelDescriptor(new TLcdModelDescriptor(
        "Sample code",    // source name
        "MyPoint",        // data type
        "World sized icons"      // display name (user)
    ));

    model.addElement(new TLcdLonLatHeightPoint(-122.447392, 37.753369, 800), ILcdFireEventMode.NO_EVENT);
    model.addElement(new TLcdLonLatHeightPoint(-122.447821, 37.751461, 800), ILcdFireEventMode.NO_EVENT);

    return model;
  }

  /**
   * Creates the model whose points will be used to draw icons with a fixed view size.
   *
   * @return the model whose points will be used to draw icons with a fixed view size.
   */
  private ILcdModel createViewIconPointModel() {
    TLcdVectorModel model = new TLcdVectorModel();
    TLcdGeodeticDatum datum = new TLcdGeodeticDatum();
    model.setModelReference(new TLcdGeodeticReference(datum));
    model.setModelDescriptor(new TLcdModelDescriptor(
        "Sample code",    // source name
        "MyPoint",        // data type
        "View sized icons"      // display name (user)
    ));

    model.addElement(new OrientedLonLatHeightPoint(-122.45, 37.693303, 600, 0), ILcdFireEventMode.NO_EVENT);
    model.addElement(new OrientedLonLatHeightPoint(-122.595346, 37.765976, 600, 90), ILcdFireEventMode.NO_EVENT);
    model.addElement(new OrientedLonLatHeightPoint(-122.45, 37.838649, 600, 180), ILcdFireEventMode.NO_EVENT);
    model.addElement(new OrientedLonLatHeightPoint(-122.304654, 37.765976, 600, 270), ILcdFireEventMode.NO_EVENT);

    return model;
  }

  /**
   * Creates the model whose points will be used to draw rotatable icons with a fixed world size.
   * @return the model whose points will be used to draw rotatable icons with a fixed world size.
   */
  private static ILcdModel createRotatableIconModel() {
    TLcdVectorModel model = new TLcdVectorModel();
    TLcdGeodeticDatum datum = new TLcdGeodeticDatum();
    model.setModelReference(new TLcdGeodeticReference(datum));
    model.setModelDescriptor(new TLcdModelDescriptor(
        "Sample code",    // source name
        "MyPoint",        // data type
        "Rotatable icons"      // display name (user)
    ));

    model.addElement(new OrientedLonLatHeightPoint(-122.4116, 37.7946, 600, 0), ILcdFireEventMode.NO_EVENT);
    model.addElement(new OrientedLonLatHeightPoint(-122.4192, 37.8117, 600, 0), ILcdFireEventMode.NO_EVENT);
    model.addElement(new OrientedLonLatHeightPoint(-122.4482, 37.8030, 600, 0), ILcdFireEventMode.NO_EVENT);
    model.addElement(new OrientedLonLatHeightPoint(-122.4337, 37.7765, 600, 0), ILcdFireEventMode.NO_EVENT);

    return model;

  }

  public static void main(final String[] aArgs) {
    startSample(MainPanel.class, "Icon styles");
  }

}

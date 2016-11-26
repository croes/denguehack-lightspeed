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
package samples.realtime.gxy.offsetIcon;

import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

import com.luciad.realtime.gxy.labeling.TLcdGXYContinuousLabelingAlgorithm;
import com.luciad.shape.ILcdBounds;
import com.luciad.shape.shape2D.TLcdLonLatBounds;
import com.luciad.util.TLcdInterval;
import com.luciad.view.gxy.ILcdGXYContext;
import com.luciad.view.gxy.ILcdGXYLayer;
import com.luciad.view.gxy.labeling.TLcdGXYLabelPlacer;
import com.luciad.view.gxy.labeling.algorithm.TLcdGXYCompositeLabelingAlgorithm;
import com.luciad.view.labeling.algorithm.ILcdCollectedLabelInfoDependencyProvider;
import com.luciad.view.labeling.algorithm.TLcdCollectedLabelInfo;
import com.luciad.view.labeling.algorithm.TLcdCollectedLabelInfoList;

import samples.common.SampleData;
import samples.common.serviceregistry.ServiceRegistry;
import samples.gxy.common.GXYSample;
import samples.gxy.common.TitledPanel;
import samples.gxy.common.labels.GXYLabelingAlgorithmProvider;
import samples.gxy.common.labels.LayerBasedGXYLabelingAlgorithmProvider;
import samples.gxy.common.layers.GXYDataUtil;
import samples.gxy.labels.offset.OffsetLabelLayerFactory;

/**
 * This sample demonstrates how to combine offset label placement and continuous label decluttering.
 * It also shows how to customize the labeling algorithm by
 * <ul>
 * <li>adjusting the desired label locations.</li>
 * <li>configuring the master-slave dependency.</li>
 * <li>Adding an option to drop overlapping labels.</li>
 * </ul>
 */
public class MainPanel extends GXYSample {

  private OverlappingLabelsRemovalWrapper fRemoveOverlappingLabelsWrapper;

  @Override
  protected ILcdBounds getInitialBounds() {
    return new TLcdLonLatBounds(-90.00, 25.00, 20.00, 15.00);
  }

  @Override
  protected JPanel createSettingsPanel() {
    return new LabelingModeControl();
  }

  @Override
  protected void addData() {
    // Background data
    GXYDataUtil.instance().model(SampleData.COUNTRIES).layer().label("Countries").selectable(false).addToView(getView());

    OffsetLabelLayerFactory factory = new OffsetLabelLayerFactory(getView());
    factory.setLabelScaleRange(new TLcdInterval(0.0002, Double.MAX_VALUE));

    // Offset city layer
    ILcdGXYLayer cities = GXYDataUtil.instance().model(SampleData.US_CITIES).layer(factory).label("Cities").addToView(getView()).getLayer();
    setupLabelPlacer(cities);
  }

  private void setupLabelPlacer(final ILcdGXYLayer aCitiesLayer) {
    TLcdGXYContinuousLabelingAlgorithm declutteringAlgorithm = new TLcdGXYContinuousLabelingAlgorithm() {
      protected void retrieveDesiredLabelLocation(Graphics aGraphics, ILcdGXYContext aGXYContext, Object aObject, int aLabelIndex, int aSubLabelIndex, Point aRelativeLocationSFCT) {
        boolean offsetIcon = aLabelIndex == 0 && aSubLabelIndex == 0;
        if (offsetIcon) {
          aRelativeLocationSFCT.setLocation(20, -15);
        } else {
          aRelativeLocationSFCT.setLocation(40, -25);
        }
      }
    };

    // Define the master-slave dependency as follows : for a labeled object, all labels are
    // a slave of the label with the same label index and sublabel index 0. This is the same
    // dependency as the offset dependency defined in OffsetLabelLayerFactory. So this means
    // that labels that depend on an other label will always have the same distance from their
    // parent label.
    declutteringAlgorithm.setMasterSlaveDependencyProvider(new ILcdCollectedLabelInfoDependencyProvider() {
      public void getDependingLabels(TLcdCollectedLabelInfo aLabel, TLcdCollectedLabelInfoList aLabelInfoList, List<TLcdCollectedLabelInfo> aDependingLabelsSFCT) {
        if (aLabel.getLabelIdentifier().getSubLabelIndex() != 0) {
          TLcdCollectedLabelInfo label = aLabelInfoList.getLabel(aLabel.getLabeledObject().getLayer(), aLabel.getLabelIdentifier().getDomainObject(), aLabel.getLabelIdentifier().getLabelIndex(), 0);
          if (label != null) {
            aDependingLabelsSFCT.add(label);
          }
        }
      }
    });

    // This wrapper makes sure that overlapping labels are omitted, and not painted. This wrapper can
    // be enabled and disabled.
    declutteringAlgorithm.setReuseLocationsScaleRatioInterval(new TLcdInterval(0.0, Double.MAX_VALUE));
    fRemoveOverlappingLabelsWrapper = new OverlappingLabelsRemovalWrapper(declutteringAlgorithm);
    fRemoveOverlappingLabelsWrapper.setEnabled(false);

    // Create a composite labeling algorithm that returns the continuous labeling algorithm
    // for this layer, and a default algorithm for other layers.
    ServiceRegistry.getInstance().register(new LayerBasedGXYLabelingAlgorithmProvider(aCitiesLayer, fRemoveOverlappingLabelsWrapper));
    TLcdGXYCompositeLabelingAlgorithm algorithm = new TLcdGXYCompositeLabelingAlgorithm(new GXYLabelingAlgorithmProvider());
    TLcdGXYLabelPlacer label_placer = new TLcdGXYLabelPlacer(algorithm);
    getView().setGXYViewLabelPlacer(label_placer);
  }

  /**
   * Panel that allows to configure if overlapping labels should be removed or not.
   */
  private class LabelingModeControl extends JPanel {

    public LabelingModeControl() {
      // Create new panel for the check box.
      JPanel bp = new JPanel(new GridLayout(1, 1));
      bp.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));

      JCheckBox check_box = new JCheckBox("Remove overlapping labels", false);
      bp.add(check_box);
      check_box.addItemListener(new ItemListener() {
        public void itemStateChanged(ItemEvent e) {
          boolean enabled = e.getStateChange() == ItemEvent.SELECTED;
          fRemoveOverlappingLabelsWrapper.setEnabled(enabled);
          getView().invalidate(true, this, "Set remove overlapping labels to " + enabled);
        }
      });

      // Add the newly created panel
      setLayout(new BorderLayout());
      add(BorderLayout.CENTER, TitledPanel.createTitledPanel("Labeling Options", bp));
    }
  }

  public static void main(final String[] aArgs) {
    startSample(MainPanel.class, "Dynamically offset icons");
  }
}

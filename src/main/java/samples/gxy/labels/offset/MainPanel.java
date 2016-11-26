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
package samples.gxy.labels.offset;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

import com.luciad.shape.ILcdBounds;
import com.luciad.shape.shape2D.TLcdLonLatBounds;
import com.luciad.view.gxy.labeling.ILcdGXYViewLabelPlacer;
import com.luciad.view.gxy.labeling.TLcdGXYLabelPlacer;
import com.luciad.view.gxy.labeling.algorithm.discrete.TLcdGXYDependantLabelsRemovalWrapper;
import com.luciad.view.gxy.labeling.algorithm.discrete.TLcdGXYLocationListLabelingAlgorithm;
import com.luciad.view.labeling.algorithm.TLcdCollectedLabelInfo;
import com.luciad.view.labeling.algorithm.TLcdCollectedLabelInfoList;

import samples.common.SampleData;
import samples.gxy.common.GXYSample;
import samples.gxy.common.TitledPanel;
import samples.gxy.common.layers.GXYDataUtil;

/**
 * This sample demonstrates how to setup offset icons using label dependencies and
 * a special layer implementation. It also shows how the used labeling algorithm can
 * be configured to drop labels together.
 */
public class MainPanel extends GXYSample {

  @Override
  protected ILcdBounds getInitialBounds() {
    return new TLcdLonLatBounds(-90.00, 25.00, 20.00, 15.00);
  }

  @Override
  protected void createGUI() {
    super.createGUI();
    getView().setGXYViewLabelPlacer(createViewLabelPlacer(false));
  }

  @Override
  protected JPanel createSettingsPanel() {
    return new LabelingModeControl();
  }

  private ILcdGXYViewLabelPlacer createViewLabelPlacer(boolean aDropLabelsTogether) {
    TLcdGXYLocationListLabelingAlgorithm algorithm = new TLcdGXYLocationListLabelingAlgorithm() {
      private boolean fOffsetLabel = false;
      private Location[] fOffsetPositions = {Location.SOUTH_EAST, Location.NORTH_WEST, Location.NORTH_EAST, Location.SOUTH_WEST};
      private Location[] fLabelPositions = {Location.EAST, Location.WEST, Location.NORTH, Location.SOUTH};

      @Override
      protected double getLocationBounds(TLcdCollectedLabelInfo aLabelInfo, int aLocationIndex, TLcdCollectedLabelInfoList aLabelInfoList, Rectangle aBoundsSFCT) throws IllegalArgumentException {
        fOffsetLabel = aLabelInfo.getLabelIdentifier().getLabelIndex() == 0 && aLabelInfo.getLabelIdentifier().getSubLabelIndex() == 0;
        return super.getLocationBounds(aLabelInfo, aLocationIndex, aLabelInfoList, aBoundsSFCT);
      }

      @Override
      public Location[] getLocationList() {
        if (fOffsetLabel) {
          return fOffsetPositions;
        } else {
          return fLabelPositions;
        }
      }

      @Override
      public int getShiftLabelLocation() {
        if (fOffsetLabel) {
          return super.getShiftLabelLocation();
        } else {
          return 15;
        }
      }
    };
    if (aDropLabelsTogether) {
      SubLabelLabelInfoDependencyProvider dependency_provider = new SubLabelLabelInfoDependencyProvider();
      return new TLcdGXYLabelPlacer(new TLcdGXYDependantLabelsRemovalWrapper(algorithm, dependency_provider));
    }

    return new TLcdGXYLabelPlacer(algorithm);
  }

  protected void addData() throws IOException {
    super.addData();
    GXYDataUtil.instance().model(SampleData.US_CITIES).layer(new OffsetLabelLayerFactory(getView())).addToView(getView());
  }

  /**
   * Panel that allows to configure if labels should be dropped together or not.
   */
  private class LabelingModeControl extends JPanel {

    public LabelingModeControl() {
      // Create new panel for the check boxes/
      JPanel bp = new JPanel(new GridLayout(1, 1));
      bp.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));

      JCheckBox check_box = new JCheckBox("Drop unlabeled icons", false);
      check_box.setToolTipText("If checked, a text label will always be dropped together with its offset icon, and vice versa.");
      bp.add(check_box);
      check_box.addItemListener(new ItemListener() {
        public void itemStateChanged(ItemEvent e) {
          boolean drop_labels_together = e.getStateChange() == ItemEvent.SELECTED;
          getView().setGXYViewLabelPlacer(createViewLabelPlacer(drop_labels_together));
        }
      });

      // Add the newly created panel
      setLayout(new BorderLayout());
      add(BorderLayout.CENTER, TitledPanel.createTitledPanel("Labeling Options", bp));
    }
  }

  public static void main(final String[] aArgs) {
    startSample(MainPanel.class, "Offset icons");
  }
}

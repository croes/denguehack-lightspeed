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
package samples.realtime.gxy.clusterLabeling;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.EventQueue;
import java.util.Random;

import com.luciad.geodesy.TLcdGeodeticDatum;
import samples.common.LuciadFrame;
import com.luciad.model.ILcdModel;
import com.luciad.model.TLcdModelDescriptor;
import com.luciad.model.TLcdVectorModel;
import com.luciad.reference.TLcdGeodeticReference;
import com.luciad.shape.shape2D.TLcdLonLatBounds;
import com.luciad.util.ILcdFilter;
import com.luciad.util.ILcdFireEventMode;
import com.luciad.view.TLcdLabelLocation;
import com.luciad.view.gxy.ILcdGXYEditableLabelsLayer;
import com.luciad.view.gxy.TLcdGXYCompositeLabelObstacleProvider;
import com.luciad.view.gxy.labeling.ILcdGXYViewLabelPlacer;
import com.luciad.view.gxy.labeling.TLcdGXYLabelPlacer;
import com.luciad.view.gxy.labeling.algorithm.TLcdGXYCompositeLabelingAlgorithm;
import com.luciad.view.map.TLcdMapJPanel;

import samples.common.SampleData;
import samples.common.SamplePanel;
import samples.common.layerControls.swing.LayerControlPanel;
import samples.gxy.common.SampleMapJPanelFactory;
import samples.gxy.common.TitledPanel;
import samples.common.layerControls.swing.LayerControlPanelFactory2D;
import samples.gxy.common.layers.GXYDataUtil;
import samples.gxy.common.layers.GXYLayerUtil;
import samples.gxy.common.toolbar.ToolBar;
import samples.realtime.common.LabelObstacleProvider;

/**
 * This sample demonstrates how to combine offset icons and continuous label decluttering.
 * Upon hovering the mouse on a group of objects, the objects are interactively decluttered
 * based on concentric circle positions.
 * This is done by a mouse listener (DeclutteringMouseListener) that works with an advanced label
 * placement algorithm provider (AnimatedDeclutterLabelingAlgorithmProvider).
 */
public class MainPanel extends SamplePanel {

  private static final String TRACK_MODEL_TYPE_NAME = "ClusteredTracks";

  /**
   * This flag determines whether cluttered icons should be decluttered automatically on mouse
   * over, or whether the user must select the objects to declutter and click the declutter
   * action on the toolbar.
   */
  private static final boolean AUTOMATIC_DECLUTTERING = true;

  private static Color[] COLORS = {Color.orange, Color.green, Color.blue};
  public static double[][] CLUSTERS = {
      {53.0d, 59.0d},
      {49.0d, 53.0d},
      {60.0d, 52.0d}
  };

  private TLcdMapJPanel fMapJPanel = SampleMapJPanelFactory.createMapJPanel(new TLcdLonLatBounds(48.00, 48.00, 14.00, 14.00), false);
  private ToolBar fToolBar;

  protected void createGUI() {
    // Create the default toolbar and layer control.
    fToolBar = new ToolBar(fMapJPanel, true, this);
    LayerControlPanel layer_control = LayerControlPanelFactory2D.createDefaultGXYLayerControlPanel(fMapJPanel);

    // Create a titled panel around the map panel
    TitledPanel map_panel = TitledPanel.createTitledPanel(
        "Map", fMapJPanel, TitledPanel.NORTH | TitledPanel.EAST
                                                         );

    setLayout(new BorderLayout());
    add(BorderLayout.NORTH, fToolBar);
    add(BorderLayout.CENTER, map_panel);
    add(BorderLayout.EAST, layer_control);
  }

  protected void addData() {
    // Create the composite label declutter algorithm
    final AnimatedDeclutterLabelingAlgorithmProvider animated_algorithm_provider = new AnimatedDeclutterLabelingAlgorithmProvider(fMapJPanel);
    fMapJPanel.setGXYViewLabelPlacer(createGXYViewLabelPlacer(animated_algorithm_provider));

    // Add a background layer
    GXYDataUtil.instance().model(SampleData.COUNTRIES).layer().label("Countries").selectable(false).addToView(fMapJPanel);

    for (int i = 0; i < CLUSTERS.length; i++) {
      final TLcdLabelLocation defaultLabelLocation = new TLcdLabelLocation(20, 20, 0.0, -1);
      final ILcdGXYEditableLabelsLayer track_layer = createTrackLayer(defaultLabelLocation, i);

      GXYLayerUtil.addGXYLayer(fMapJPanel, track_layer);
      GXYLayerUtil.moveGXYLayer(fMapJPanel, 2, track_layer);
    }

    EventQueue.invokeLater(new Runnable() {
      public void run() {
        if (AUTOMATIC_DECLUTTERING) {
          //add the mouse listener to implement the decluttering
          DeclutteringMouseListener listener = new DeclutteringMouseListener(new TrackModelFilter(), fMapJPanel, animated_algorithm_provider);
          fMapJPanel.addMouseMotionListener(listener);
          fMapJPanel.addMouseListener(listener);
        } else {
          fToolBar.addAction(new DeclutterSelectionAction(new TrackModelFilter(), fMapJPanel, animated_algorithm_provider));
        }
      }
    });
  }

  private ILcdGXYViewLabelPlacer createGXYViewLabelPlacer(AnimatedDeclutterLabelingAlgorithmProvider aAlgorithmProvider) {
    TLcdGXYCompositeLabelingAlgorithm composite_algorithm = new TLcdGXYCompositeLabelingAlgorithm(aAlgorithmProvider);

    TLcdGXYLabelPlacer label_placer = new TLcdGXYLabelPlacer(composite_algorithm);

    // Add the label obstacle providers
    TLcdGXYCompositeLabelObstacleProvider composite_label_obstacle_provider = new TLcdGXYCompositeLabelObstacleProvider();
    composite_label_obstacle_provider.addGXYLabelObstacleProvider(new LabelObstacleProvider(new TrackModelFilter()));
    composite_label_obstacle_provider.addGXYLabelObstacleProvider(aAlgorithmProvider);
    label_placer.setLabelObstacleProvider(composite_label_obstacle_provider);

    return label_placer;
  }

  private ILcdGXYEditableLabelsLayer createTrackLayer(TLcdLabelLocation aDefaultLabelLocation, int aClusterIndex) {
    TLcdVectorModel model = createTrackModel(aClusterIndex);
    return LayerFactory.createGXYLayer(model, aDefaultLabelLocation, fMapJPanel);
  }

  public static TLcdVectorModel createTrackModel(int aClusterIndex) {
    Random rand = new Random(271828 * (aClusterIndex + 1));
    TLcdVectorModel model = new TLcdVectorModel(new TLcdGeodeticReference(new TLcdGeodeticDatum()));
    model.setModelDescriptor(new TracksModelDescriptor());
    double[] coordinates = CLUSTERS[aClusterIndex];
    for (int i = 0; i < 10; i++) {
      ColoredLonLatPoint point = new ColoredLonLatPoint(coordinates[0] + rand.nextDouble() / 2.0,
                                                        coordinates[1] + rand.nextDouble() / 2.0,
                                                        COLORS[rand.nextInt(COLORS.length)]);
      model.addElement(point, ILcdFireEventMode.NO_EVENT);
    }
    return model;
  }

  public static class TrackModelFilter implements ILcdFilter<ILcdModel> {
    @Override
    public boolean accept(ILcdModel aModel) {
      return TRACK_MODEL_TYPE_NAME.equals(aModel.getModelDescriptor().getTypeName());
    }
  }

  // Main method
  public static void main(final String[] aArgs) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        new LuciadFrame(new MainPanel(), "Dynamically offset icons");
      }
    });
  }

  private static class TracksModelDescriptor extends TLcdModelDescriptor {
    public TracksModelDescriptor() {
      super("Generated", TRACK_MODEL_TYPE_NAME, "Tracks");
    }
  }
}

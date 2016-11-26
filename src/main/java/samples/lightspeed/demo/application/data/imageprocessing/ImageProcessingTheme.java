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
package samples.lightspeed.demo.application.data.imageprocessing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JPanel;
import javax.swing.ToolTipManager;

import com.luciad.model.ILcdModel;
import com.luciad.view.lightspeed.ILspAWTView;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.layer.ILspLayer;

import samples.lightspeed.demo.framework.application.Framework;
import samples.lightspeed.demo.framework.data.themes.AbstractTheme;

/**
 * <p>Main class for the Image Processing theme.</p>
 * <p/>
 * <p>This theme shows the real-time image processing capabilities of LuciadLightspeed.</p>
 */
public class ImageProcessingTheme extends AbstractTheme {

  private ImageCustomizerPanel fCurvesPanel;
  private List<InstructionPanel> fInstructionPanels;
  private int fDefaultTooltipDelay;
  private List<ILspLayer> fThemeLayers = new ArrayList<>();

  public ImageProcessingTheme() {
    fInstructionPanels = new ArrayList<>();
    setName("Image Processing");
    setCategory("Terrain");
  }

  @Override
  public void destroy() {
    super.destroy();
    if (fCurvesPanel != null) {
      fCurvesPanel.dispose();
    }
  }

  @Override
  public void activate() {
    super.activate();
    fDefaultTooltipDelay = ToolTipManager.sharedInstance().getDismissDelay();
    ToolTipManager.sharedInstance().setDismissDelay(3 * fDefaultTooltipDelay);
    // Only activate main layers
    for (ILspLayer layer : getLayers()) {
      layer.setVisible(fThemeLayers.contains(layer));
    }

    for (InstructionPanel panel : fInstructionPanels) {
      panel.activate();
    }

    fCurvesPanel.activate();
  }

  @Override
  public void deactivate() {
    ToolTipManager.sharedInstance().setDismissDelay(fDefaultTooltipDelay);
    for (InstructionPanel panel : fInstructionPanels) {
      panel.deactivate();
    }
    fCurvesPanel.deactivate();
    super.deactivate();
  }

  @Override
  protected List<ILspLayer> createLayers(List<ILspView> aViews) {
    List<ILspLayer> result = new ArrayList<>(1);
    Framework framework = Framework.getInstance();
    Set<String> existingLayerIDs = new HashSet<>(framework.getLayerIDs());
    ILcdModel model1 = framework.getModelWithID("model.id.imaging.landsat1");
    ILcdModel model2 = framework.getModelWithID("model.id.imaging.landsat2");
    ILcdModel landSat7Model = null;
    try {
      landSat7Model = framework.getModelWithID("model.id.imaging.landsat7");
    } catch (IllegalArgumentException ex) {
      //do nth if not found
    }

    for (ILspView view : aViews) {
      ILspLayer landSat7Layer = null;
      if (landSat7Model != null) {
        landSat7Layer = new ImageLayerFactory().createLayer(landSat7Model);
        landSat7Layer.setLabel("LandSat7");
        fThemeLayers.add(landSat7Layer);
        view.addLayer(landSat7Layer);
        result.add(landSat7Layer);
        framework.registerLayers("layer.id.landsat7", view, Collections.singletonList(landSat7Layer));
      }

      ILspLayer layer1 = new ImageLayerFactory().createLayer(model1);
      fThemeLayers.add(layer1);
      view.addLayer(layer1);
      result.add(layer1);
      framework.registerLayers("layer.id.lasvegas1", view, Collections.singletonList(layer1));

      ILspLayer layer2 = new ImageLayerFactory().createLayer(model2);
      fThemeLayers.add(layer2);
      view.addLayer(layer2);
      result.add(layer2);
      framework.registerLayers("layer.id.lasvegas2", view, Collections.singletonList(layer2));

      //put landsat7 model at bottom
      int index = view.indexOf(layer1);
      if (landSat7Layer != null) {
        index = Math.min(view.indexOf(layer1), view.indexOf(layer2));
        view.moveLayerAt(index - 1, landSat7Layer);
        index = index - 1;
      }
      // Move bing layers, if existing, below the las vegas layer
      if (existingLayerIDs.contains("layer.id.bing")) {
        index = Math.min(Math.min(view.indexOf(layer1), view.indexOf(layer2)), index);
        List<ILspLayer> bingLayers = framework.getLayersWithID("layer.id.bing", view);
        for (ILspLayer bing : bingLayers) {
          view.moveLayerAt(index - 1, bing);
        }
      }
    }

    List<ILspAWTView> views = (List<ILspAWTView>) (List<?>) aViews;
    fCurvesPanel = new ImageCustomizerPanel(views, model1, model2, landSat7Model);

    // Add places layers, if available
    if (existingLayerIDs.contains("layer.id.fusion.osm.places")) {
      List<ILspLayer> placesLayers = framework.getLayersWithID("layer.id.fusion.osm.places");
      result.addAll(placesLayers);
      fThemeLayers.addAll(placesLayers);
    }

    if (existingLayerIDs.contains("layer.id.bing")) {
      result.addAll(framework.getLayersWithID("layer.id.bing"));
    }

    if (existingLayerIDs.contains("layer.id.imaging.lasvegas.golfclubs")) {
      result.addAll(framework.getLayersWithID("layer.id.imaging.lasvegas.golfclubs"));
    }

    if (existingLayerIDs.contains("layer.id.imaging.lasvegas.poi")) {
      List<ILspLayer> layers = framework.getLayersWithID("layer.id.imaging.lasvegas.poi");
      fThemeLayers.addAll(layers);
      result.addAll(layers);
      for (ILspView view : aViews) {
        fInstructionPanels.add(new InstructionPanel(view, layers.get(0)));
      }
    }

    return result;
  }

  @Override
  public List<JPanel> getThemePanels() {
    return Collections.singletonList(fCurvesPanel.getPanel());
  }

}

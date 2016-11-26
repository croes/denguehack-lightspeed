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
package samples.lightspeed.demo.application.data.highlighting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JPanel;

import com.luciad.gui.TLcdAWTUtil;
import com.luciad.model.ILcdModel;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.controller.ILspController;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.TLspPaintRepresentation;

import samples.lightspeed.customization.style.highlighting.AnimatedHighlightSHPLayerFactory;
import samples.lightspeed.customization.style.highlighting.HighlightController;
import samples.lightspeed.demo.framework.application.Framework;
import samples.lightspeed.demo.framework.data.themes.AbstractTheme;

/**
 * Represents the highlighting theme.
 * <p>
 * The highlighting is a custom theme because its layers are created by the
 * <code>AnimatedHighlightSHPLayerFactory</code> in the style.highlighting sample.
 * This layer factory needs a view to function, which is not yet available when
 * the normal layers (defined in the index.xml file of the data set) are created
 * (see <code>LayerManager</code>).
 */
public class HighlightingTheme extends AbstractTheme {

  private AnimatedHighlightSHPLayerFactory fLayerFactory;
  final Map<ILspView, HighlightController> fHighlightControllers = new HashMap<ILspView, HighlightController>();
  final Map<ILspView, ILspController> fOriginalControllers = new HashMap<ILspView, ILspController>();

  /**
   * Default constructor.
   */
  public HighlightingTheme() {
    setName("Highlighting");
    setCategory("Shapes");
    fLayerFactory = new AnimatedHighlightSHPLayerFactory();
  }

  @Override
  protected List<ILspLayer> createLayers(final List<ILspView> aViews) {

    // Make sure all the views are registered with the layer factory
    TLcdAWTUtil.invokeAndWait(new Runnable() {
      @Override
      public void run() {
        for (ILspView view : aViews) {
          ILspController controller = view.getController();
          HighlightController highlightController;
          highlightController = new HighlightController();
          highlightController.appendController(controller);
          fLayerFactory.addHighlightController(highlightController);
          fHighlightControllers.put(view, highlightController);
        }
      }
    });

    ILcdModel model = Framework.getInstance().getModelWithID("model.id.countries");

    List<ILspLayer> layers = new ArrayList<ILspLayer>();
    for (ILspView view : aViews) {
      ILspLayer layer = fLayerFactory.createLayer(model);
      layer.setLabel("Highlighting");
      view.addLayer(layer);
      layers.add(layer);

      HighlightController highlightController = fHighlightControllers.get(view);
      highlightController.registerLayer(layer, TLspPaintRepresentation.BODY);

      Framework.getInstance().registerLayers("layer.id.highlighting", view, Collections.singletonList(layer));
    }
    return layers;
  }

  @Override
  public List<JPanel> getThemePanels() {
    return Collections.emptyList();
  }

  @Override
  public void activate() {
    super.activate();
    for (ILspView view : fHighlightControllers.keySet()) {
      HighlightController controller = fHighlightControllers.get(view);
      fOriginalControllers.put(view, view.getController());
      view.setController(controller);
    }
  }

  @Override
  public void deactivate() {
    super.deactivate();
    for (ILspView view : fOriginalControllers.keySet()) {
      ILspController controller = fOriginalControllers.get(view);
      view.setController(controller);
    }
  }
}

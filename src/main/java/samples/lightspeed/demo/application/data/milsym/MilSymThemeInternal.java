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
package samples.lightspeed.demo.application.data.milsym;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;

import com.luciad.gui.ILcdIcon;
import com.luciad.gui.TLcdImageIcon;
import com.luciad.gui.swing.TLcdGreyIcon;
import com.luciad.gui.swing.TLcdSWIcon;
import samples.common.SwingUtil;
import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelListener;
import com.luciad.model.TLcdModelChangedEvent;
import com.luciad.model.TLcdVectorModel;
import com.luciad.reference.TLcdGeodeticReference;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.controller.ILspController;
import com.luciad.view.lightspeed.layer.ILspLayer;

import samples.lightspeed.common.controller.ControllerFactory;
import samples.lightspeed.demo.application.data.support.layerfactories.MilSymLayerFactory;
import samples.lightspeed.demo.application.data.support.layerfactories.MilSymTracksLayerFactory;
import samples.lightspeed.demo.application.data.support.los.LOSSupport;
import samples.lightspeed.demo.framework.application.Framework;
import samples.lightspeed.demo.simulation.SimulationSupport;

/**
 * Extends the milsym theme with LOS capabilities and some additional layers (dynamic tracks and
 * neighborhoods).
 */
public class MilSymThemeInternal extends MilSymTheme {

  private double fPreviousTimeFactor;
  private LOSSupport fLOSSupport;
  private Map<ILspView, RouteCreateController> fView2RouteCreateController = new IdentityHashMap<ILspView, RouteCreateController>();
//  private Map<ILspView, ILspStyler> fView2FocusHandleStyler = new IdentityHashMap<ILspView, ILspStyler>();
//  private Map<ILspView, ILspStyler> fView2HandleStyler = new IdentityHashMap<ILspView, ILspStyler>();

  public MilSymThemeInternal() {
    setName("Military Symbology");
    fLOSSupport = new LOSSupport();
  }

  @Override
  public void activate() {
    super.activate();
    // Set neighborhoods layer and friendly tracks layer to invisible by default.
    // These can be enabled from the layers drop down by the demo person.

    Framework framework = Framework.getInstance();
    List<ILspLayer> layers = framework.getLayersWithID("layer.id.milsym.neighborhoods");
    for (ILspLayer l : layers) {
      l.setVisible(false);
    }

    layers = framework.getLayersWithID("layer.id.friendlyTracks");
    for (ILspLayer l : layers) {
      l.setVisible(false);
    }

    fPreviousTimeFactor = SimulationSupport.getInstance().getTimeFactor();
    SimulationSupport.getInstance().setTimeFactor(2.0); // Almost real-time

    // Hide the outline handle styler for editing LOS coverages
//    List<ILspView> views = framework.getFrameworkContext().getViews();
//    fView2FocusHandleStyler.clear();
//    fView2HandleStyler.clear();
//    for ( ILspView view : views ) {
//      ILspController controller = view.getController();
//      while ( controller != null && !( controller instanceof TLspEditController ) ) {
//        controller = controller.getNextController();
//      }
//      if ( controller != null ) {
//        TLspEditController editController = ( TLspEditController ) controller;
//        TLspEditHandleStyler editHandleStyler = ( TLspEditHandleStyler ) editController.getFocusHandleStyler();
//        fView2HandleStyler.put( view, editController.getHandleStyler() );
//        fView2FocusHandleStyler.put( view, editHandleStyler );
//        TLspEditHandleStyler newStyler = new TLspEditHandleStyler();
//        newStyler.setStyles( TLspHandleGeometryType.OUTLINE, ( List<ALspStyle> ) null ); // Removes existing styles
//        newStyler.setStyles( TLspHandleGeometryType.OUTLINE, TLspLineStyle.newBuilder().color( new Color( 0f, 0f, 0f, 0f ) ).build() );
//        editController.setHandleStyler( newStyler );
//        editController.setFocusHandleStyler( newStyler );
//      }
//    }
  }

  @Override
  public void deactivate() {
    // Make sure original controller is set
    Framework framework = Framework.getInstance();
    Map<ILspView, ILspController> view2controller = (Map<ILspView, ILspController>) framework
        .getSharedValue("view.default.controllers");
    for (ILspView view : view2controller.keySet()) {
      view.setController(view2controller.get(view));
    }

    // Make sure original outline handle styler is restored.
//    List<ILspView> views = framework.getFrameworkContext().getViews();
//    for ( ILspView view : views ) {
//      ILspController controller = view.getController();
//      while ( controller != null && !( controller instanceof TLspEditController ) ) {
//        controller = controller.getNextController();
//      }
//      if ( controller != null ) {
//        TLspEditController editController = ( TLspEditController ) controller;
//        editController.setHandleStyler( fView2HandleStyler.get( view ) );
//        editController.setFocusHandleStyler( fView2FocusHandleStyler.get( view ) );
//      }
//    }
//    fView2HandleStyler.clear();
//    fView2FocusHandleStyler.clear();

    // Restore simulation time factor
    SimulationSupport.getInstance().setTimeFactor(fPreviousTimeFactor);
    super.deactivate();
  }

  /**
   * Creates the theme's layers. <p/> This method is called from {@link #initialize(List,
   * java.util.Properties)}. Note that the implementer is responsible for correctly adding the
   * layers to the views. <p/> Note also that if theme properties are required for layer creation,
   * you can use the appropriate getter to retrieve the properties: see
   * <code>getThemeProperties()</code>.
   *
   * @param aViews the views that are part of the application
   *
   * @return the list of layers that was created
   */
  @Override
  protected List<ILspLayer> createLayers(List<ILspView> aViews) {
    Framework framework = Framework.getInstance();
    List<ILspLayer> layers = new ArrayList<ILspLayer>(super.createLayers(aViews));

    layers.addAll(framework.getLayersWithID("layer.id.milsym.neighborhoods"));

    TLcdGeodeticReference reference = new TLcdGeodeticReference();
    TLcdVectorModel losModel = new TLcdVectorModel(reference);

    losModel.addModelListener(new ILcdModelListener() {
      @Override
      public void modelChanged(TLcdModelChangedEvent aEvent) {
        MilSymLayerFactory.getRegularStyler().fireStyleChangeEvent();
        MilSymLayerFactory.getSelectedStyler().fireStyleChangeEvent();
      }
    });
    for (ILspView view : aViews) {
      ILspLayer layer = fLOSSupport.createLayer(view, "MilSym LOS");
      framework.registerLayers("layer.id.milsym.los", view, Collections.<ILspLayer>singleton(layer));
      view.addLayer(layer);
      view.moveLayerAt(chooseLOSLayerIndex(view), layer);
      layers.add(layer);

      // Dynamic tracks layer
      ILcdModel tracksModel = Framework.getInstance().getModelWithID("model.id.airway");
      if (tracksModel != null) {
        MilSymTracksLayerFactory factory = new MilSymTracksLayerFactory();
        Collection<ILspLayer> tracksLayers = factory.createLayers(tracksModel);
        Framework.getInstance().registerLayers("layer.id.friendlyTracks", view, tracksLayers);
        for (final ILspLayer trackLayer : tracksLayers) {
          view.addLayer(trackLayer);
          layers.add(trackLayer);
          trackLayer.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
              if ("visible".equals(evt.getPropertyName())) {
                if (trackLayer.isVisible()) {
                  SimulationSupport.getInstance().activateTheme(MilSymThemeInternal.this);
                  SimulationSupport.getInstance().startSimulator();
                } else {
                  SimulationSupport.getInstance().stopSimulator();
                }
              }
            }
          });
        }
      }

      RouteCreateController routeCreateController = new RouteCreateController();
      fView2RouteCreateController.put(view, routeCreateController);
      ILspLayer routeLayer = routeCreateController.getLayer();

      view.addLayer(routeLayer);
      view.moveLayerAt(chooseLOSLayerIndex(view), layer);
      layers.add(routeCreateController.getLayer());
    }

    return layers;
  }

  private int chooseLOSLayerIndex(ILspView aView) {
    for (int idx = 0; idx < aView.layerCount(); idx++) {
      ILspLayer.LayerType layerType = aView.getLayer(idx).getLayerType();
      // LOS layer should be above all background layers so all bottom raster layers can be merged
      if (layerType != ILspLayer.LayerType.BACKGROUND) {
        return idx;
      }
    }
    return aView.layerCount() - 1;
  }

  @Override
  public JComponent getSouthDockedComponent() {
    JComponent milsymbar = super.getSouthDockedComponent();

    Framework app = Framework.getInstance();
    boolean isTouchEnabled = Boolean.parseBoolean(app.getProperty("controllers.touch.enabled", "false"));
    AbstractButton losbutton = fLOSSupport.createLOSCreationButton(isTouchEnabled);

    AbstractButton routingButton = createRoutingCreationButton(
        isTouchEnabled ?
        new TLcdImageIcon("samples/lightspeed/demo/icons/routing_64.png") :
        new TLcdImageIcon("samples/lightspeed/demo/icons/routing_32.png"),
        "Crosscountry routing"
    );

    JPanel losPanel = getPanelFactory().createTitledPanel(losbutton, "LoS");
    JPanel routingPanel = getPanelFactory().createTitledPanel(routingButton, "Route");
    JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT));
    bar.add(milsymbar);
    bar.add(losPanel);
    bar.add(routingPanel);
//    AbstractButton routingButton = createLOSCreationButton(
//        fView2LOSLayer,
//        isTouchEnabled ?
//        new TLcdImageIcon( "samples/lightspeed/demo/icons/los_64.png" ) :
//        new TLcdImageIcon( "samples/lightspeed/demo/icons/los_32.png" ),
//        "Create a LOS coverage"
//    );

    bar.setOpaque(false);

    return bar;
  }

  @Override
  public List<JPanel> getThemePanels() {
    List<JPanel> panels = super.getThemePanels();

    JButton button = new JButton("Route");
    button.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        List<ILspView> views = Framework.getInstance().getFrameworkContext().getViews();
        Map<ILspView, ILspController> view2controller = (Map<ILspView, ILspController>) Framework.getInstance()
                                                                                                 .getSharedValue("view.default.controllers");

        for (ILspView view : views) {
          ILspController controller = view.getController();
          if (controller instanceof RouteCreateController) {
            view.setController(view2controller.get(view));
          } else {
            // TODO: deadlock?
            ILspController routeController = fView2RouteCreateController.get(view);
            view.setController(routeController);
            routeController.appendController(ControllerFactory.createNavigationController());
          }
        }
      }
    });
    return panels;
  }

  private AbstractButton createRoutingCreationButton(final ILcdIcon aIcon, String aToolTipText) {
    JButton button = new JButton();
    button.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        List<ILspView> views = Framework.getInstance().getFrameworkContext().getViews();
        for (ILspView view : views) {
          ILspController controller = view.getController();
          if (!(controller instanceof RouteCreateController)) {
            ILspController routeController = fView2RouteCreateController.get(view);
            view.setController(null);
            routeController.appendController(ControllerFactory.createNavigationController());
            view.setController(routeController);
          }
        }
      }
    });

    SwingUtil.makeSquare(button);

    button.setIcon(new TLcdSWIcon(aIcon));
    button.setDisabledIcon(new TLcdSWIcon(new TLcdGreyIcon(aIcon)));
    button.setToolTipText(aToolTipText);
    //button.addMouseListener(this);

    return button;
  }

  @Override
  public boolean isSimulated() {
    return false;
  }
}

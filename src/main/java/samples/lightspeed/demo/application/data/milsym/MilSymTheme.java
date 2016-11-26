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

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;

import com.luciad.gui.swing.TLcdOverlayLayout;
import com.luciad.model.ILcdModel;
import com.luciad.util.ILcdChangeListener;
import com.luciad.util.ILcdFilter;
import com.luciad.util.TLcdChangeEvent;
import com.luciad.view.ILcdLayerTreeNode;
import com.luciad.view.TLcdDomainObjectContext;
import com.luciad.view.lightspeed.ILspAWTView;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.layer.ALspLayer;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.swing.TLspBalloonManager;
import com.luciad.view.swing.ALcdBalloonDescriptor;
import com.luciad.view.swing.ALcdBalloonManager;
import com.luciad.view.swing.ILcdBalloonContentProvider;
import com.luciad.view.swing.TLcdBalloonGUIFactory;
import com.luciad.view.swing.TLcdModelElementBalloonDescriptor;

import samples.common.BalloonViewSelectionListener;
import samples.lightspeed.demo.application.gui.menu.MilSymPanelFactory;
import samples.lightspeed.demo.framework.application.Framework;
import samples.lightspeed.demo.framework.data.themes.AbstractTheme;
import samples.symbology.common.EMilitarySymbology;
import samples.symbology.common.app6.APP6ModelDescriptor;
import samples.symbology.common.gui.SymbolCustomizerFactory;
import samples.symbology.common.gui.customizer.AbstractSymbolCustomizer;
import samples.symbology.common.util.MilitarySymbolFacade;

public class MilSymTheme extends AbstractTheme {

  private final MilSymPanelFactory fPanelFactory;
  private final Map<ILspView, MilSymFilter> fMilSymFilters;
  private final List<TLspBalloonManager> fBalloonManagers;

  public MilSymTheme() {
    setName("Military Symbology");
    setCategory("Shapes");
    loadRequiredClassForQuickFail(APP6ModelDescriptor.class);
    fMilSymFilters = new HashMap<ILspView, MilSymFilter>();
    fPanelFactory = new MilSymPanelFactory(fMilSymFilters);
    fBalloonManagers = new ArrayList<TLspBalloonManager>();
  }

  protected MilSymPanelFactory getPanelFactory() {
    return fPanelFactory;
  }

  @Override
  protected ILcdFilter<TLcdDomainObjectContext> createShowPropertiesActionFilter() {
    return new ILcdFilter<TLcdDomainObjectContext>() {
      @Override
      public boolean accept(TLcdDomainObjectContext aObject) {
        return false;
      }
    };
  }

  /**
   * Creates the theme's layers.
   * <p/>
   * This method is called from {@link #initialize(List, java.util.Properties)}.
   * Note that the implementer is responsible for correctly adding the layers to the views.
   * <p/>
   * Note also that if theme properties are required for layer creation, you can use the
   * appropriate getter to retrieve the properties: see <code>getThemeProperties()</code>.
   *
   * @param aViews the views that are part of the application
   *
   * @return the list of layers that was created
   */
  @Override
  protected List<ILspLayer> createLayers(List<ILspView> aViews) {
    Framework framework = Framework.getInstance();
    List<ILspLayer> layers = new ArrayList<ILspLayer>();

    layers.addAll(framework.getLayersWithID("layer.id.milsym.sf"));
    layers.addAll(framework.getLayersWithID("layer.id.milsym.infrastructure"));
    layers.addAll(framework.getLayersWithID("layer.id.milsym.neutral"));
    layers.addAll(framework.getLayersWithID("layer.id.milsym.forces"));

    for (ILspView view : aViews) {
      ILspAWTView awtView = (ILspAWTView) view;
      registerBalloons(awtView);
    }

    layers.addAll(framework.getLayersWithID("layer.id.mgrs"));
    layers.addAll(framework.getLayersWithID("layer.id.milsym.claimed"));

    ILcdModel symbolShapeModel = new SymbolShapeModelFactory().createModel();
    List<ILspLayer> viewLayers = new ArrayList<>();
    for (ILspView view : aViews) {
      viewLayers.clear();
      viewLayers.addAll(framework.getLayersWithID("layer.id.milsym.sf", view));
      viewLayers.addAll(framework.getLayersWithID("layer.id.milsym.infrastructure", view));
      viewLayers.addAll(framework.getLayersWithID("layer.id.milsym.neutral", view));
      viewLayers.addAll(framework.getLayersWithID("layer.id.milsym.forces", view));
      viewLayers.addAll(framework.getLayersWithID("layer.id.milsym.claimed", view));
      MilSymFilter filter = getMilSymFilter(view);
      for (ILspLayer layer : viewLayers) {
        ((ALspLayer) layer).setFilter(filter);
      }
      ILspLayer shapeLayer = new SymbolShapeLayerFactory().createLayer(symbolShapeModel, filter);
      view.addLayer(shapeLayer);
      framework.registerLayers("layer.id.milsym.shape", view, Collections.singletonList(shapeLayer));
      layers.add(shapeLayer);
    }

    for (ILspView view : aViews) {
      fMilSymFilters.get(view).attachToView(view);
    }

    return layers;
  }

  private void registerBalloons(ILspAWTView aAwtView) {
    TLspBalloonManager balloonManager = new TLspBalloonManager(aAwtView,
                                                               aAwtView.getOverlayComponent(),
                                                               TLcdOverlayLayout.Location.NO_LAYOUT,
                                                               new MilitarySymbolBalloonProvider(aAwtView),
                                                               new TLcdBalloonGUIFactory() {
                                                                 @Override
                                                                 protected JComponent createResizeComponent(ALcdBalloonManager aBalloonManager, JComponent aBalloonContent) {
                                                                   return null;
                                                                 }
                                                               }
    ) {
    };
    BalloonViewSelectionListener listener = new BalloonViewSelectionListener(aAwtView, balloonManager);
    ILcdLayerTreeNode rootNode = aAwtView.getRootNode();
    rootNode.addHierarchySelectionListener(listener);
    rootNode.addHierarchyPropertyChangeListener(listener);
    rootNode.addHierarchyLayeredListener(listener);
    fBalloonManagers.add(balloonManager);
  }

  private MilSymFilter getMilSymFilter(ILspView aView) {
    MilSymFilter milSymFilter = fMilSymFilters.get(aView);
    if (milSymFilter == null) {
      milSymFilter = new MilSymFilter();
      fMilSymFilters.put(aView, milSymFilter);
    }
    return milSymFilter;
  }

  @Override
  public JComponent getSouthDockedComponent() {
    return fPanelFactory.createSymbolCreationPanel();
  }

  @Override
  public List<JPanel> getThemePanels() {
    return fPanelFactory.createThemePanels(this);
  }

  @Override
  public void activate() {
    super.activate();
    for (TLspBalloonManager manager : fBalloonManagers) {
      manager.setBalloonsEnabled(true);
    }
  }

  @Override
  public void deactivate() {
    super.deactivate();
    for (TLspBalloonManager manager : fBalloonManagers) {
      manager.setBalloonsEnabled(false);
    }
  }

  @Override
  public void destroy() {
    fPanelFactory.destroy();
  }

  private class MilitarySymbolBalloonProvider implements ILcdBalloonContentProvider, ILcdChangeListener {

    private final ILspView fView;
    private AbstractSymbolCustomizer fPanel;

    public MilitarySymbolBalloonProvider(ILspView aView) {
      fView = aView;
    }

    @Override
    public boolean canGetContent(ALcdBalloonDescriptor aBalloonDescriptor) {
      return MilitarySymbolFacade.isMilitarySymbol(aBalloonDescriptor.getObject());
    }

    @Override
    public JComponent getContent(ALcdBalloonDescriptor aBalloonDescriptor) {
      if (canGetContent(aBalloonDescriptor)) {
        if (fPanel == null) {
          fPanel = SymbolCustomizerFactory.createCustomizer(
              EnumSet.of(SymbolCustomizerFactory.Part.REGULAR), true, fPanelFactory.getFavorites(), false, null, null);
          fPanel.addChangeListener(this);
        }
        TLcdModelElementBalloonDescriptor balloonDescriptor = (TLcdModelElementBalloonDescriptor) aBalloonDescriptor;
        EMilitarySymbology symbology = MilitarySymbolFacade.retrieveTopmostCompatibleLayerSymbology(fView);
        fPanel.setSymbol(symbology, balloonDescriptor.getModel(), balloonDescriptor.getObject());
        JComponent component = fPanel.getComponent();
        Color affiliationColor = MilitarySymbolFacade.
                                                         getAffiliationColor(balloonDescriptor.getObject(),
                                                                             MilitarySymbolFacade.getAffiliationValue(balloonDescriptor.getObject()));
        component.setBorder(BorderFactory.createMatteBorder(0, 10, 0, 0, affiliationColor));
        return component;
      }
      return null;
    }

    @Override
    public void stateChanged(TLcdChangeEvent aChangeEvent) {
      Object symbol = ((AbstractSymbolCustomizer) aChangeEvent.getSource()).getSymbol();
      Color affiliationColor = MilitarySymbolFacade.
                                                       getAffiliationColor(symbol,
                                                                           MilitarySymbolFacade.getAffiliationValue(symbol));
      fPanel.getComponent().setBorder(BorderFactory.createMatteBorder(0, 10, 0, 0, affiliationColor));
    }
  }
}

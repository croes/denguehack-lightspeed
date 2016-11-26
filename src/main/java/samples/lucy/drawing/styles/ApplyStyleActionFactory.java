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
package samples.lucy.drawing.styles;

import static com.luciad.lucy.addons.drawing.gui.ALcyDrawingToolBarFactory.DRAWING_TOOL_BAR_ID;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import com.luciad.gui.ILcdAction;
import com.luciad.lucy.ILcyLucyEnv;
import com.luciad.lucy.addons.drawing.TLcyDrawingAddOn;
import com.luciad.lucy.addons.drawing.format.ALcyDrawingSettings;
import com.luciad.lucy.addons.drawing.model.TLcyDrawingModelDescriptor;
import com.luciad.lucy.addons.drawing.model.TLcyDrawingStyleRepository;
import com.luciad.lucy.gui.ILcyActionBar;
import com.luciad.lucy.gui.TLcyActionBarManager;
import com.luciad.lucy.gui.TLcyGroupDescriptor;
import com.luciad.lucy.gui.TLcyUserInterfaceManager;
import com.luciad.lucy.map.ILcyGenericMapComponent;
import com.luciad.ogc.sld.model.TLcdSLDDescription;
import com.luciad.ogc.sld.model.TLcdSLDFeatureTypeStyle;
import com.luciad.ogc.sld.model.TLcdSLDFill;
import com.luciad.ogc.sld.model.TLcdSLDParameterValue;
import com.luciad.ogc.sld.model.TLcdSLDPolygonSymbolizer;
import com.luciad.ogc.sld.model.TLcdSLDRule;
import com.luciad.ogc.sld.model.TLcdSLDStroke;
import com.luciad.view.ILcdLayer;

/**
 * <p>This factory class adds menu items to apply a style for each style in the drawing
 * style repository.</p>
 *
 * <p>We do this each time the drawing layer changes because the available styles are model dependent.</p>
 */
public class ApplyStyleActionFactory {
  private final ILcyLucyEnv fLucyEnv;

  private List<ILcdAction> fCurrentActions;
  private ALcyDrawingSettings<?, ?> fDrawingSettings;
  private ILcyGenericMapComponent<?, ?> fMapComponent;
  private PropertyChangeListener fChangeDrawingLayerListener;

  protected ApplyStyleActionFactory(ILcyLucyEnv aLucyEnv, ILcyGenericMapComponent<?, ?> aMapComponent, ALcyDrawingSettings<?, ?> aDrawingSettings) {
    fLucyEnv = aLucyEnv;
    fMapComponent = aMapComponent;
    fDrawingSettings = aDrawingSettings;
    fChangeDrawingLayerListener = new PropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent evt) {
        updateMenus();
      }
    };
    fDrawingSettings.addPropertyChangeListener("drawingLayer", fChangeDrawingLayerListener);
    fCurrentActions = new ArrayList<ILcdAction>();
    updateMenus();
  }

  public void cleanup() {
    fDrawingSettings.removePropertyChangeListener("drawingLayer", fChangeDrawingLayerListener);
  }

  private void removeCurrentActions(ILcyActionBar aActionBar) {
    for (ILcdAction action : fCurrentActions) {
      aActionBar.removeAction(action);
    }
    fCurrentActions.clear();
  }

  private TLcyDrawingStyleRepository getStyleRepository(ILcdLayer aDrawingLayer) {
    return ((TLcyDrawingModelDescriptor) aDrawingLayer.getModel().getModelDescriptor()).getDrawingStyleRepository();
  }

  private void updateMenus() {
    TLcyUserInterfaceManager userInterfaceManager = fLucyEnv.getUserInterfaceManager();
    TLcyActionBarManager actionBarManager = userInterfaceManager.getActionBarManager();

    ILcyActionBar actionBar =
        actionBarManager.getActionBar(DRAWING_TOOL_BAR_ID, fMapComponent);
    removeCurrentActions(actionBar);
    if (fDrawingSettings.getDrawingLayer() == null) {
      return;
    }
    String[] menus = new String[]{"Apply style"};
    TLcyGroupDescriptor[] groups = new TLcyGroupDescriptor[]{new TLcyGroupDescriptor("StyleGroup"), new TLcyGroupDescriptor("ApplyStyle")};
    TLcyDrawingStyleRepository repo = getStyleRepository(fDrawingSettings.getDrawingLayer());
    if (repo.getStyleCount() == 0) {
      addDefaultStyles(repo);
    }
    // adds a apply style action for each style in the repo
    for (int i = 0; i < repo.getStyleCount(); i++) {
      TLcdSLDFeatureTypeStyle style = (TLcdSLDFeatureTypeStyle) repo.getStyle(i);
      ApplyStyleAction action = new ApplyStyleAction(fDrawingSettings.getDrawingLayer(), style, fLucyEnv.retrieveAddOnByClass(TLcyDrawingAddOn.class).getDrawingFormat());
      action.setName(style.getDescription().getTitle());
      action.addUndoableListener(fLucyEnv.getUndoManager());
      actionBar.insertAction(action, TLcyGroupDescriptor.DEFAULT, menus, groups);
      fCurrentActions.add(action);
    }
  }

  // adds three hardcoded styles
  // this is just a very simple way to make sure that there is at least
  // one style in the repository
  // note that if you save a model, these styles will be saved as well
  private void addDefaultStyles(TLcyDrawingStyleRepository aRepo) {
    aRepo.addStyle(createStyle("Red", "#ff0000", "#bb0000"));
    aRepo.addStyle(createStyle("Green", "#00ff00", "#00bb00"));
    aRepo.addStyle(createStyle("Blue", "#0000ff", "#0000bb"));
  }

  private static TLcdSLDFeatureTypeStyle createStyle(String aTitle, String aFillColor, String aStrokeColor) {
    TLcdSLDFeatureTypeStyle style = new TLcdSLDFeatureTypeStyle();
    style.setDescription(new TLcdSLDDescription(aTitle, ""));
    TLcdSLDRule rule = new TLcdSLDRule();
    rule.addSymbolizer(new TLcdSLDPolygonSymbolizer(createStroke(aFillColor, 1, 0.8f),
                                                    createFill(aStrokeColor, 0.5f), null));
    style.addRule(rule);
    return style;
  }

  private static TLcdSLDStroke createStroke(String aColor, int aStrokeWidth, float aStrokeOpacity) {
    TLcdSLDStroke stroke = new TLcdSLDStroke();
    stroke.setCssParameter("stroke", new TLcdSLDParameterValue(aColor));
    stroke.setCssParameter("stroke-width", new TLcdSLDParameterValue(Integer.toString(aStrokeWidth)));
    stroke.setCssParameter("stroke-opacity", new TLcdSLDParameterValue(Float.toString(aStrokeOpacity)));
    return stroke;
  }

  private static TLcdSLDFill createFill(String aColor, float aOpacity) {
    TLcdSLDFill fill = new TLcdSLDFill();
    fill.setCssParameter("fill", new TLcdSLDParameterValue(aColor));
    fill.setCssParameter("fill-opacity", new TLcdSLDParameterValue(Float.toString(aOpacity)));
    return fill;
  }
}

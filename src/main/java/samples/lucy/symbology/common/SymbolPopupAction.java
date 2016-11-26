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
package samples.lucy.symbology.common;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;

import com.luciad.gui.ALcdAction;
import com.luciad.gui.ILcdAction;
import com.luciad.gui.swing.TLcdSWAction;
import samples.common.gui.PopupLocation;
import samples.common.gui.PopupPanelButton;
import com.luciad.lucy.ILcyLucyEnv;
import com.luciad.lucy.gui.ILcyActionBar;
import com.luciad.lucy.gui.ILcyCustomizableRepresentationAction;
import com.luciad.lucy.gui.ILcyToolBar;
import com.luciad.lucy.gui.TLcyActionBarUtil;
import com.luciad.lucy.gui.TLcyToolBar;
import com.luciad.lucy.map.ILcyGenericMapComponent;
import com.luciad.lucy.map.ILcyGenericMapManagerListener;
import com.luciad.lucy.map.TLcyGenericMapManagerEvent;
import com.luciad.lucy.util.properties.ALcyProperties;
import com.luciad.util.ILcdDisposable;
import com.luciad.view.ILcdLayer;
import com.luciad.view.ILcdView;

/**
 * Action which opens a dialog containing UI to select a new military symbology instance
 * to create.
 */
public final class SymbolPopupAction extends ALcdAction implements ILcyCustomizableRepresentationAction {

  private final ILcyGenericMapComponent fMapComponent;
  private final String fFavoritesToolbarName;
  private final String fSearchToolbarName;
  private final ILcyLucyEnv fLucyEnv;
  private final ALcyProperties fProperties;
  private final String fPropertiesPrefix;
  private ILcdDisposable fSearchToolBarDisposable;
  private ILcdDisposable fFavoritesBarDisposable;
  private final JComponent fContent;

  private final List<PopupPanelButton> fPopupPanelButtons = new ArrayList<>();

  public SymbolPopupAction(ILcyGenericMapComponent aMapComponent,
                           ALcyProperties aProperties,
                           String aPropertiesPrefix,
                           String aFavoritesToolbarName,
                           String aSearchToolbarName,
                           ILcyLucyEnv aLucyEnv) {
    fMapComponent = aMapComponent;
    fFavoritesToolbarName = aFavoritesToolbarName;
    fSearchToolbarName = aSearchToolbarName;
    fLucyEnv = aLucyEnv;
    fProperties = aProperties;
    fPropertiesPrefix = aPropertiesPrefix;
    fLucyEnv.getCombinedMapManager().addMapManagerListener(new DisposeWhenMapRemovedListener(this));
    fContent = createContent();
  }

  @Override
  public Component customizeRepresentation(Component aDefaultComponent, ILcdAction aWrapperAction, ILcyActionBar aActionBar) {
    if (aActionBar instanceof ILcyToolBar) {
      PopupPanelButton popupButton = new PopupPanelButton(fContent, true, PopupLocation.DEFAULT);
      popupButton.setAction(new TLcdSWAction(aWrapperAction));
      fPopupPanelButtons.add(popupButton);
      return popupButton;
    } else {
      return aDefaultComponent;
    }
  }

  @Override
  public void actionPerformed(ActionEvent e) {
  }

  public final void closePopups() {
    for (PopupPanelButton popupPanelButton : fPopupPanelButtons) {
      popupPanelButton.getPopup().setPopupVisible(false);
    }
  }

  private JComponent createContent() {
    JPanel contentPane = new JPanel();
    contentPane.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 5));
    contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.PAGE_AXIS));

    TLcyToolBar searchBar = new TLcyToolBar();
    fSearchToolBarDisposable = TLcyActionBarUtil.setupAsConfiguredActionBar(searchBar,
                                                                            fSearchToolbarName,
                                                                            fMapComponent,
                                                                            fProperties,
                                                                            fPropertiesPrefix,
                                                                            (JComponent) fMapComponent.getComponent(),
                                                                            fLucyEnv.getUserInterfaceManager().getActionBarManager());
    contentPane.add(searchBar.getComponent());

    TLcyToolBar favoritesBar = new TLcyToolBar();
    fFavoritesBarDisposable = TLcyActionBarUtil.setupAsConfiguredActionBar(favoritesBar,
                                                                           fFavoritesToolbarName,
                                                                           fMapComponent,
                                                                           fProperties,
                                                                           fPropertiesPrefix,
                                                                           (JComponent) fMapComponent.getComponent(),
                                                                           fLucyEnv.getUserInterfaceManager().getActionBarManager());
    contentPane.add(favoritesBar.getComponent());

    return contentPane;
  }

  private void disposeContent() {
    if (fSearchToolBarDisposable != null) {
      fSearchToolBarDisposable.dispose();
    }
    if (fFavoritesBarDisposable != null) {
      fFavoritesBarDisposable.dispose();
    }
  }

  private static class DisposeWhenMapRemovedListener implements ILcyGenericMapManagerListener<ILcdView, ILcdLayer> {
    private final WeakReference<SymbolPopupAction> fAction; // use weak ref to avoid memory leaks

    public DisposeWhenMapRemovedListener(SymbolPopupAction aAction) {
      fAction = new WeakReference<>(aAction);
    }

    @Override
    public void mapManagerChanged(TLcyGenericMapManagerEvent<? extends ILcdView, ? extends ILcdLayer> aMapManagerEvent) {
      SymbolPopupAction symbolPopupAction = fAction.get();
      if (symbolPopupAction == null) {
        // The action was GC'ed, this listener is no longer needed
        aMapManagerEvent.getMapManager().removeMapManagerListener(this);
        return;
      }
      if (aMapManagerEvent.getId() == TLcyGenericMapManagerEvent.MAP_COMPONENT_REMOVED &&
          aMapManagerEvent.getMapComponent() == symbolPopupAction.fMapComponent) {
        // The relevant map was removed, this listener is no longer needed
        aMapManagerEvent.getMapManager().removeMapManagerListener(this);

        symbolPopupAction.disposeContent();
      }
    }
  }
}

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
package samples.lucy.frontend.mapcentric.formatbar;

import static samples.lucy.frontend.mapcentric.gui.MapCentricUtil.SIDE_BAR_TOOL_BAR;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import com.luciad.gui.TLcdAWTUtil;
import com.luciad.lucy.ILcyLucyEnv;
import com.luciad.lucy.gui.TLcyActionBarMediatorBuilder;
import com.luciad.lucy.gui.formatbar.ALcyFormatBar;
import com.luciad.lucy.gui.formatbar.ALcyFormatBarFactory;
import com.luciad.lucy.map.ILcyGenericMapComponent;
import com.luciad.lucy.util.properties.ALcyProperties;
import com.luciad.view.ILcdLayer;
import com.luciad.view.ILcdLayeredListener;
import com.luciad.view.ILcdTreeLayered;
import com.luciad.view.ILcdView;
import com.luciad.view.TLcdLayeredEvent;

/**
 * <p>
 *   Class which ensures that all available format bars are created for a certain map.
 *   The format bars are created without any specific layers.
 *   After that, they are linked with the selected and/or topmost compatible layers (if any).
 * </p>
 *
 * <p>
 *   The created format bars are not added to the UI.
 *   Instead, action bar mediation is used to show the contents of the format bar in the side bar tool bar.
 * </p>
 *
 * @see ALcyFormatBar
 */
final class FormatBarMediator<S extends ILcdView & ILcdTreeLayered, T extends ILcdLayer> {

  private final ILcyLucyEnv fLucyEnv;
  private final ILcyGenericMapComponent<S, T> fMapComponent;
  private final List<ALcyFormatBar> fAvailableFormatBars;

  public FormatBarMediator(ILcyLucyEnv aLucyEnv,
                           ILcyGenericMapComponent<S, T> aMapComponent,
                           ALcyProperties aProperties,
                           String aPropertiesPrefix) {
    fLucyEnv = aLucyEnv;
    fMapComponent = aMapComponent;
    fAvailableFormatBars = createBars(aMapComponent);

    for (ALcyFormatBar formatBar : fAvailableFormatBars) {
      initializeBar(formatBar);
    }
    mediateBetweenFormatBarsAndSideBar(aMapComponent, aProperties, aPropertiesPrefix, aLucyEnv);

    // keep the layers linked
    aMapComponent.addPropertyChangeListener(new PropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent evt) {
        if ("selectedLayersAsList".equals(evt.getPropertyName())) {
          //Allow other listeners to be invoked
          TLcdAWTUtil.invokeLater(new Runnable() {
            @Override
            public void run() {
              updateBarsForSelection();
            }
          });
        }
      }
    });
    aMapComponent.getMainView().addLayeredListener(new ILcdLayeredListener() {
      @Override
      public void layeredStateChanged(final TLcdLayeredEvent e) {
        //Allow other listeners to be invoked
        TLcdAWTUtil.invokeLater(new Runnable() {
          @Override
          public void run() {
            updateBarsForLayeredEvent(e);
          }
        });
      }
    });
  }

  private List<ALcyFormatBar> createBars(ILcyGenericMapComponent<S, T> aMapComponent) {
    List<ALcyFormatBarFactory> factories = fLucyEnv.getServices(ALcyFormatBarFactory.class);
    List<ALcyFormatBar> result = new ArrayList<>();
    for (ALcyFormatBarFactory bf : factories) {
      S view = aMapComponent.getMainView();
      if (bf.canCreateFormatBar(view, null)) {
        ALcyFormatBar bar = bf.createFormatBar(view, null);
        result.add(bar);
      }
    }
    return result;
  }

  private void mediateBetweenFormatBarsAndSideBar(ILcyGenericMapComponent<S, T> aMapComponent,
                                                  ALcyProperties aProperties,
                                                  String aPropertiesPrefix,
                                                  ILcyLucyEnv aLucyEnv) {
    String[] formatBarNames = aProperties.getStringArray(aPropertiesPrefix + "formatBarNames", new String[0]);
    for (String formatBarName : formatBarNames) {
      TLcyActionBarMediatorBuilder.newInstance(aLucyEnv)
                                  .sourceActionBar(formatBarName, aMapComponent)
                                  .targetActionBar(SIDE_BAR_TOOL_BAR, aMapComponent)
                                  .mediate();
    }
  }

  private void initializeBar(ALcyFormatBar aBar) {
    // first try linking with one of the selected layers
    boolean updated = updateBarForSelection(aBar);
    if (updated) {
      return;
    }
    S view = fMapComponent.getMainView();
    // if that fails, pick the topmost compatible layer
    for (Enumeration layers = view.layersBackwards(); layers.hasMoreElements(); ) {
      ILcdLayer layer = (ILcdLayer) layers.nextElement();
      if (aBar.canSetLayer(layer)) {
        aBar.setLayer(layer);
        return;
      }
    }
    // finally, fall back to null
    aBar.setLayer(null);
  }

  private void updateBarsForSelection() {
    for (ALcyFormatBar bar : fAvailableFormatBars) {
      updateBarForSelection(bar);
    }
  }

  private boolean updateBarForSelection(ALcyFormatBar bar) {
    for (T layer : fMapComponent.getSelectedLayersAsList()) {
      if (bar.canSetLayer(layer)) {
        bar.setLayer(layer);
        return true;
      }
    }
    return false;
  }

  private void updateBarsForLayeredEvent(TLcdLayeredEvent e) {
    ILcdLayer layer = e.getLayer();
    switch (e.getID()) {
    case TLcdLayeredEvent.LAYER_ADDED:
      for (ALcyFormatBar bar : fAvailableFormatBars) {
        if (bar.canSetLayer(layer)) {
          bar.setLayer(layer);
        }
      }
      break;
    case TLcdLayeredEvent.LAYER_REMOVED:
      for (ALcyFormatBar bar : fAvailableFormatBars) {
        if (bar.getLayer() == layer) {
          bar.setLayer(null);
          // try to find another layer
          initializeBar(bar);
        }
      }
      break;
    }
  }
}

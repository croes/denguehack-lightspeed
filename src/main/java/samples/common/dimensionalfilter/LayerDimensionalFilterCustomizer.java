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
package samples.common.dimensionalfilter;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;

import com.luciad.gui.swing.TLcdOverlayLayout;
import samples.common.action.LayerCustomizerSupport;
import samples.common.dimensionalfilter.model.DimensionalFilter;
import samples.common.dimensionalfilter.model.DimensionalFilterManager;
import samples.common.dimensionalfilter.model.DimensionalFilterProvider;
import samples.common.dimensionalfilter.ui.DimensionalFilterManagerUI;
import com.luciad.util.collections.ILcdCollection;
import com.luciad.view.ILcdLayer;
import com.luciad.view.ILcdLayered;

/**
 * Shows an overlay panel to filter visible layers with (multi-)dimensional data.
 *
 * @see DimensionalFilterProvider
 */
public class LayerDimensionalFilterCustomizer extends LayerCustomizerSupport<ILcdLayered, ILcdLayer> {

  private final DimensionalFilterManager fDimensionalFilterManager;
  private final DimensionalFilterManagerUI fDimensionalFilterManagerUI;
  private boolean fisFilterVisible = false;

  public LayerDimensionalFilterCustomizer(ILcdLayered aView, ILcdCollection aSelectedLayers, final JComponent aOverlayPanel, final Iterable<DimensionalFilterProvider> aFilterProviderList) {
    super(aView, aSelectedLayers);

    DimensionalFilterProvider dimensionalFilterProvider = new DimensionalFilterProvider() {
      @Override
      public void addPropertyChangeListener(PropertyChangeListener aPropertyChangeListener) {
        for (DimensionalFilterProvider provider : aFilterProviderList) {
          provider.addPropertyChangeListener(aPropertyChangeListener);
        }
      }

      @Override
      public void removePropertyChangeListener(PropertyChangeListener aPropertyChangeListener) {
        for (DimensionalFilterProvider provider : aFilterProviderList) {
          provider.removePropertyChangeListener(aPropertyChangeListener);
        }
      }

      @Override
      public boolean canHandleLayer(ILcdLayer aLayer, ILcdLayered aLayered) {
        for (DimensionalFilterProvider provider : aFilterProviderList) {
          if (provider.canHandleLayer(aLayer, aLayered)) {
            return true;
          }
        }
        return false;
      }

      @Override
      public List<DimensionalFilter> createFilters(ILcdLayer aLayer, ILcdLayered aLayered) {
        for (DimensionalFilterProvider provider : aFilterProviderList) {
          if (provider.canHandleLayer(aLayer, aLayered)) {
            return provider.createFilters(aLayer, aLayered);
          }
        }
        return null;
      }
    };

    fDimensionalFilterManager = new DimensionalFilterManager(dimensionalFilterProvider);
    fDimensionalFilterManager.addPropertyChangeListener(new PropertyChangeListener() {
      @Override
      public void propertyChange(final PropertyChangeEvent evt) {
        SwingUtilities.invokeLater(new Runnable() {
          @Override
          public void run() {
            if (DimensionalFilterManager.FILTER_GROUPS_PROPERTY_NAME.equals(evt.getPropertyName())) {
              if (fisFilterVisible && !fDimensionalFilterManager.hasVisibleFilters()) {
                aOverlayPanel.remove(fDimensionalFilterManagerUI);
                aOverlayPanel.revalidate();
                aOverlayPanel.repaint();
                fisFilterVisible = false;
              }
              if (!fisFilterVisible && fDimensionalFilterManager.hasVisibleFilters()) {
                aOverlayPanel.add(fDimensionalFilterManagerUI, TLcdOverlayLayout.Location.NORTH_WEST);
                aOverlayPanel.revalidate();
                aOverlayPanel.repaint();
                fisFilterVisible = true;
              }
            }
          }
        });

      }
    });
    fDimensionalFilterManagerUI = new DimensionalFilterManagerUI(fDimensionalFilterManager, aOverlayPanel);
  }

  @Override
  public void layerAdded(ILcdLayered aView, ILcdLayer aLayer) {
    fDimensionalFilterManager.registerLayer(aLayer, aView);
  }

  @Override
  public void layerRemoved(ILcdLayered aView, ILcdLayer aLayer) {
    fDimensionalFilterManager.unregisterLayer(aLayer, aView);
  }

  @Override
  public void layerSelected(ILcdLayered aView, ILcdLayer aLayer) {
  }
}

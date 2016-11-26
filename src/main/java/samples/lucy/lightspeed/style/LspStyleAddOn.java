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
package samples.lucy.lightspeed.style;

import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeListener;

import com.luciad.lucy.ILcyLucyEnv;
import com.luciad.lucy.addons.ALcyAddOn;
import com.luciad.lucy.gui.ILcyToolBar;
import com.luciad.lucy.gui.TLcyGroupDescriptor;
import com.luciad.lucy.map.ILcyGenericMapComponent;
import com.luciad.lucy.map.ILcyGenericMapManagerListener;
import com.luciad.lucy.map.TLcyGenericMapManagerEvent;
import com.luciad.lucy.map.lightspeed.TLcyLspMapManager;
import com.luciad.view.ILcdLayer;
import com.luciad.view.ILcdView;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.layer.ILspLayer;

/**
 * This AddOn adds a slider to each Lightspeed view that allows to change the
 * transparency of rasters or fills for all layers at once. It uses the
 * Lightspeed style API to control the transparency, so other style properties
 * can be controlled in a similar way.
 *
 * @since 2012.0
 */
public class LspStyleAddOn extends ALcyAddOn {

  public LspStyleAddOn() {
  }

  @Override
  public void plugInto(final ILcyLucyEnv aLucyEnv) {

    final TLcyLspMapManager mapManager = aLucyEnv.getService(TLcyLspMapManager.class);
    if (mapManager != null) {
      mapManager.addMapManagerListener(new ILcyGenericMapManagerListener<ILspView, ILspLayer>() {
        @Override
        public void mapManagerChanged(TLcyGenericMapManagerEvent<? extends ILspView, ? extends ILspLayer> aMapManagerEvent) {
          if (aMapManagerEvent.getId() == TLcyGenericMapManagerEvent.MAP_COMPONENT_ADDED) {
            initMapComponent(aMapManagerEvent.getMapComponent(), aLucyEnv);
          }
        }
      }, true);
    } else {
      throw new RuntimeException("Lightspeed map manager not found, please make sure that the Lightspeed addon is loaded before this addon.");
    }

  }

  private void initMapComponent(final ILcyGenericMapComponent<? extends ILcdView, ? extends ILcdLayer> aMapComponent, final ILcyLucyEnv aLucyEnv) {

    final ILcyToolBar actionBar = aMapComponent.getToolBar();
    if (actionBar != null) {
      final JSlider slider = new JSlider();
      slider.setValue(0);
      slider.setOpaque(false);
      slider.setMaximumSize(new Dimension(100, (int) slider.getMaximumSize().getHeight()));
      JPanel sliderPanel = new JPanel(new FlowLayout(FlowLayout.LEADING));
      sliderPanel.add(new JLabel("opaque"));
      sliderPanel.add(slider);
      sliderPanel.add(new JLabel("transparent"));
      sliderPanel.setOpaque(false);
      ChangeListener sliderUpdater = new OpacityUpdater((ILspView) aMapComponent.getMainView(), slider);
      slider.addChangeListener(sliderUpdater);
      actionBar.insertComponent(sliderPanel, TLcyGroupDescriptor.DEFAULT);

    }
  }

  @Override
  public void unplugFrom(ILcyLucyEnv aLucyEnv) {
  }

}

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
package samples.lightspeed.common.layercontrols;

import com.luciad.gui.TLcdIconFactory;
import com.luciad.view.ILcdLayer;
import com.luciad.view.ILcdTreeLayered;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.services.terrain.ILspTerrainSupport;

import samples.common.layerControls.actions.AbstractLayerTreeToggleAction;

/**
 * Action that toggles the elevation of the view's global terrain layer.
 */
public class ToggleTerrainElevationAction extends AbstractLayerTreeToggleAction {
  private ILspView fView;

  public ToggleTerrainElevationAction(ILcdTreeLayered aLayered, ILspView aView) {
    super(aLayered, TLcdIconFactory.create(TLcdIconFactory.TERRAIN_LAYER_ICON));
    fView = aView;
    setShortDescription("Switch the view's terrain elevation on and off");
  }

  @Override
  protected boolean layerSupported(ILcdLayer aLayer) {
    return true;
  }

  @Override
  protected boolean getLayerStatus(ILcdLayer aLayer) {
    if (fView.getServices() != null) {
      ILspTerrainSupport terrainSupport = fView.getServices().getTerrainSupport();
      return terrainSupport != null && terrainSupport.isElevationEnabled();
    } else {
      return true;
    }
  }

  @Override
  protected void setLayerStatus(boolean aStatus, ILcdLayer aLayer) {
    ILspTerrainSupport terrainSupport = fView.getServices().getTerrainSupport();
    if (terrainSupport != null) {
      terrainSupport.setElevationEnabled(aStatus);
      if (fView.isAutoUpdate()) {
        fView.invalidate(true, this, "Terrain layer elevation");
      }
    }
  }
}

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
package samples.lightspeed.demo.application.data.osm;

import java.awt.Color;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Properties;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.luciad.gui.swing.TLcdOverlayLayout;
import com.luciad.view.lightspeed.ILspAWTView;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.label.TLspLabelPlacer;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.TLspLayer;

import samples.common.HaloLabel;
import samples.lightspeed.demo.framework.application.Framework;
import samples.lightspeed.demo.framework.data.themes.AbstractTheme;

public class OpenStreetMapTheme extends AbstractTheme {

  private ArrayList<ILspView> fViews;
  private IdentityHashMap<ILspAWTView, JComponent> fCopyright = new IdentityHashMap<ILspAWTView, JComponent>();
  private boolean fAddCopyright = true;

  public OpenStreetMapTheme() {
    super();
    setName("Streets");
    setCategory("Shapes");
  }

  @Override
  protected List<ILspLayer> createLayers(List<ILspView> aViews) {
    Framework framework = Framework.getInstance();
    List<ILspLayer> layers = new ArrayList<ILspLayer>();
    // Even places looks quite cluttered
    layers.addAll(framework.getLayersWithID("layer.id.world"));
    layers.addAll(framework.getLayersWithID("layer.id.fusion.osm.places"));
    layers.addAll(framework.getLayersWithID("layer.id.fusion.osm.pointsofinterest"));
    layers.addAll(framework.getLayersWithID("layer.id.fusion.osm.buildings"));
    List<ILspLayer> roadLayers = framework.getLayersWithID("layer.id.fusion.osm.roads");
    layers.addAll(roadLayers);
    layers.addAll(framework.getLayersWithID("layer.id.fusion.osm.railways"));
    layers.addAll(framework.getLayersWithID("layer.id.fusion.osm.landuse"));
    layers.addAll(framework.getLayersWithID("layer.id.fusion.osm.waterways"));
    layers.addAll(framework.getLayersWithID("layer.id.fusion.osm.coastline2"));

    for (ILspView view : aViews) {
      ((TLspLabelPlacer) view.getLabelPlacer()).removePlacementGroup(TLspLabelPlacer.DEFAULT_DECLUTTER_GROUP);
      ((TLspLabelPlacer) view.getLabelPlacer()).addPlacementGroup(TLspLabelPlacer.DEFAULT_DECLUTTER_GROUP, new HighwayLabelConflictChecker(roadLayers));
    }

    // Filter out far-away poi's, rivers, and buildings in 3D as quickly as possible using a filter on the layer.
    for (ILspView view : aViews) {
      List<ILspLayer> filteredLayers = new ArrayList<ILspLayer>();
      filteredLayers.addAll(framework.getLayersWithID("layer.id.fusion.osm.pointsofinterest", view));
      filteredLayers.addAll(framework.getLayersWithID("layer.id.fusion.osm.buildings", view));
      filteredLayers.addAll(framework.getLayersWithID("layer.id.fusion.osm.waterways", view));
      for (ILspLayer layer : filteredLayers) {
        if (layer instanceof TLspLayer) {
          TLspLayer l = (TLspLayer) layer;
          l.setFilter(new OpenStreetMapDistanceFilter(10000, l, view));
        }
      }
    }

    // Filter out small roads
    for (ILspView view : aViews) {
      List<ILspLayer> filteredLayers = framework.getLayersWithID("layer.id.fusion.osm.roads", view);

      for (ILspLayer layer : filteredLayers) {
        if (layer instanceof TLspLayer) {
          TLspLayer l = (TLspLayer) layer;
          l.setFilter(new OpenStreetMapRoadDistanceFilter(10000, l, view));
        }
      }
    }

    return layers;
  }

  @Override
  public void initialize(List<ILspView> aViews, Properties aProps) {
    super.initialize(aViews, aProps);
    fViews = new ArrayList<ILspView>(aViews);
    fAddCopyright = Boolean.parseBoolean(aProps.getProperty("copyright", "false"));
  }

  @Override
  public void activate() {
    super.activate();
    if (fAddCopyright) {
      addCopyright(fViews);
    }
  }

  @Override
  public void deactivate() {
    if (fAddCopyright) {
      removeCopyright(fViews);
    }
    super.deactivate();
  }

  private void addCopyright(List<ILspView> aViews) {
    for (ILspView view_ : aViews) {
      ILspAWTView view = (ILspAWTView) view_;

      JLabel osmLabel = new HaloLabel(
          "© OpenStreetMap contributors", 12, true
      );
      JLabel iconLabel = new HaloLabel(
          "Map icons CC-0 from SJJB Management", 12, true
      );

      JPanel panel = new JPanel(new GridLayout(2, 1, 4, 4));
      panel.add(osmLabel);
      panel.add(iconLabel);
      panel.setOpaque(false);
      panel.setBackground(new Color(0, 0, 0, 0));

      view.getOverlayComponent().add(panel, TLcdOverlayLayout.Location.SOUTH_WEST);
      view.getOverlayComponent().validate();

      fCopyright.put(view, panel);
    }
  }

  private void removeCopyright(List<ILspView> aViews) {
    for (ILspView view_ : aViews) {
      final ILspAWTView view = (ILspAWTView) view_;

      JComponent copyright = fCopyright.get(view);
      if (copyright != null) {
        view.getOverlayComponent().remove(copyright);
      }
    }
  }
}

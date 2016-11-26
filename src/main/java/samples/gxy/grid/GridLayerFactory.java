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
package samples.gxy.grid;

import java.awt.Color;
import java.awt.Font;
import java.awt.Insets;

import com.luciad.geodesy.TLcdGeodeticDatum;
import com.luciad.gui.ILcdIcon;
import com.luciad.gui.TLcdIconFactory;
import com.luciad.view.gxy.ILcdGXYLayer;
import com.luciad.view.gxy.TLcdGXYLayer;
import com.luciad.view.gxy.TLcdGXYLayerTreeNode;
import com.luciad.view.map.TLcdMapGeorefGridLayer;
import com.luciad.view.map.TLcdMapLonLatGridLayer;
import com.luciad.view.map.painter.TLcdLonLatBorderGridPainter;
import com.luciad.view.map.painter.TLcdLonLatGridPainter;

import samples.common.gxy.DynamicLonLatGridLayer;
import samples.gxy.grid.multilevel.cgrs.CGRSCoordinateLayer;
import samples.gxy.grid.multilevel.cgrs.CGRSGrid;
import samples.gxy.grid.multilevel.cgrs.CGRSGridLayer;
import samples.gxy.grid.multilevel.gars.GARSCoordinateLayer;
import samples.gxy.grid.multilevel.gars.GARSGridLayer;
import samples.gxy.grid.multilevel.osgr.OSGRGridLayer;

public class GridLayerFactory {

  public static final ILcdIcon GRID_ICON = TLcdIconFactory.create(TLcdIconFactory.GRID_ICON);
  public static final Color GRID_COLOR = new Color(110, 110, 110, 110);
  public static final Color GRID_SUB_COLOR = new Color(110, 110, 110, 80);
  public static final Color GRID_LABEL_COLOR = new Color(180, 180, 180);
  public static final Color GRID_LABEL_HALO_COLOR = new Color(110, 110, 110);
  public static final Font GRID_LABEL_FONT = new Font("Dialog", Font.PLAIN, 12);
  public static final Insets CORNER_LABEL_INSETS = new Insets(5, 5, 35, 5);

  public static ILcdGXYLayer createLonLatGridLayer() {
    DynamicLonLatGridLayer layer = new DynamicLonLatGridLayer();
    layer.setIcon(GRID_ICON);
    TLcdLonLatGridPainter painter = ((TLcdLonLatGridPainter) layer.getGXYPainter());
    painter.setColor(GRID_COLOR);
    painter.setLabelColor(GRID_LABEL_COLOR);
    painter.setLabelHaloEnabled(true);
    painter.setLabelHaloColor(GRID_LABEL_HALO_COLOR);
    painter.setLabelFont(GRID_LABEL_FONT);
    layer.setGXYPainter(painter);
    return layer;
  }

  public static ILcdGXYLayer createGeoRefGridLayer() {
    TLcdMapGeorefGridLayer layer = new TLcdMapGeorefGridLayer("Grid");
    layer.setIcon(GRID_ICON);
    layer.setColor(GRID_COLOR);
    layer.setLabelColor(GRID_LABEL_COLOR);
    layer.setLabelHaloEnabled(true);
    layer.setLabelHaloColor(GRID_LABEL_HALO_COLOR);
    layer.setLabelFont(GRID_LABEL_FONT);
    layer.setSlantedLabels(true);
    layer.setCornerLabelInsets(CORNER_LABEL_INSETS);
    return layer;
  }

  public static ILcdGXYLayer createBorderGridLayer() {
    TLcdMapLonLatGridLayer layer = new TLcdMapLonLatGridLayer();
    layer.setIcon(GRID_ICON);
    layer.setGXYPainter(new TLcdLonLatBorderGridPainter());
    return layer;
  }

  public static ILcdGXYLayer createCGRSLayer() {
    CGRSGrid grid = new CGRSGrid(0, 40, 15, 15);

    CGRSGridLayer cgrsGridLayer = new CGRSGridLayer(grid, new TLcdGeodeticDatum());
    TLcdGXYLayer cgrsAreaLayer = new CGRSCoordinateLayer(cgrsGridLayer);

    TLcdGXYLayerTreeNode cgrsLayer = new TLcdGXYLayerTreeNode("Grid");
    cgrsLayer.setIcon(GRID_ICON);

    cgrsLayer.addLayer(cgrsAreaLayer);
    cgrsLayer.addLayer(cgrsGridLayer);
    return cgrsLayer;
  }

  public static ILcdGXYLayer createGARSLayer() {
    TLcdGeodeticDatum datum = new TLcdGeodeticDatum();

    GARSGridLayer garsGridLayer = new GARSGridLayer(datum);
    TLcdGXYLayer garsAreaLayer = new GARSCoordinateLayer(garsGridLayer);

    TLcdGXYLayerTreeNode garsLayer = new TLcdGXYLayerTreeNode("Grid");
    garsLayer.setIcon(GRID_ICON);

    garsLayer.addLayer(garsAreaLayer);
    garsLayer.addLayer(garsGridLayer);

    return garsLayer;
  }

  public static ILcdGXYLayer createOSGRLayer() {
    OSGRGridLayer layer = new OSGRGridLayer();
    layer.setIcon(GRID_ICON);
    return layer;
  }
}

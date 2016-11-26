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
package samples.realtime.gxy.clusterLabeling;

import java.awt.Color;
import java.util.List;

import com.luciad.gui.ILcdIcon;
import com.luciad.gui.TLcdSymbol;
import samples.common.gxy.GXYScaleSupport;
import com.luciad.model.ILcdModel;
import com.luciad.util.TLcdInterval;
import com.luciad.view.ILcdLabelDependencyProvider;
import com.luciad.view.ILcdView;
import com.luciad.view.TLcdLabelIdentifier;
import com.luciad.view.TLcdLabelLocation;
import com.luciad.view.TLcdLabelLocations;
import com.luciad.view.gxy.ILcdGXYContext;
import com.luciad.view.gxy.ILcdGXYEditableLabelsLayer;
import com.luciad.view.gxy.ILcdGXYLayer;
import com.luciad.view.gxy.ILcdGXYView;
import com.luciad.view.gxy.TLcdGXYLabelPainter;
import com.luciad.view.gxy.TLcdGXYLayer;
import com.luciad.view.gxy.painter.TLcdGXYCompositeLabelPainter;
import com.luciad.view.gxy.painter.TLcdGXYIconPainter;
import com.luciad.view.gxy.painter.TLcdGXYLabelPainterAdapter;
import com.luciad.view.map.TLcdGeodeticPen;

import samples.gxy.common.AntiAliasedIcon;
import samples.gxy.labels.offset.BodyLabelsLayerWrapper;

/**
 * This layer factory creates the layer for the sample.
 */
class LayerFactory {

  private static int sLayerCount = 0;
  private static final String[] LABELS = new String[]{"Label"};

  public static ILcdGXYEditableLabelsLayer createGXYLayer(ILcdModel aModel, TLcdLabelLocation aDefaultLabelLocation, ILcdGXYView aGXYView) {
    TLcdGXYLayer layer = new TLcdGXYLayer();

    layer.setLabel("Tracks" + sLayerCount);
    sLayerCount++;

    layer.setModel(aModel);
    layer.setGXYPen(new TLcdGeodeticPen());

    setupPainter(layer);
    setupLabelPainter(layer);

    layer.setVisible(true);
    layer.setLabeled(true);
    layer.setSelectable(true);
    layer.setLabelsEditable(true);
    layer.setSelectionLabeled(true);
    layer.setEditable(true);

    // Don't paint the labels when zoomed out (small scale)
    double screenResolution = -1.0;
    layer.setLabelScaleRange(new TLcdInterval(GXYScaleSupport.mapScale2InternalScale(1.0 / 50000000.0, screenResolution, null), Double.MAX_VALUE));

    TLcdLabelLocations label_locations = createLabelLocations(layer, aDefaultLabelLocation);
    return BodyLabelsLayerWrapper.createLayerWrapper(layer, label_locations, aGXYView);
  }

  /*
  * Create a new ALcdLabelLocations which defines
  * - the parent label dependency : every label has label [0, 0] as parent.
  * - the body labels : label [0, 0] is used as body representation
  */
  private static TLcdLabelLocations createLabelLocations(ILcdGXYLayer aGXYLayer, TLcdLabelLocation aDefaultLocation) {
    ILcdLabelDependencyProvider offset_dependency_provider = new ILcdLabelDependencyProvider() {
      public void getDependingLabelsSFCT(TLcdLabelIdentifier aLabel, List<TLcdLabelIdentifier> aDependingLabelsSFCT) {
        if (aLabel.getLabelIndex() != 0 || aLabel.getSubLabelIndex() != 0) {
          aDependingLabelsSFCT.add(new TLcdLabelIdentifier(aLabel.getLayer(), aLabel.getDomainObject(), 0, 0));
        }
      }
    };
    return new TLcdLabelLocations(aGXYLayer, aDefaultLocation, offset_dependency_provider) {
      public void getDefaultLabelLocationSFCT(Object aObject, int aLabelIndex, int aSubLabelIndex, ILcdView aView, TLcdLabelLocation aLocationSFCT) {
        super.getDefaultLabelLocationSFCT(aObject, aLabelIndex, aSubLabelIndex, aView, aLocationSFCT);
        if (aLabelIndex == 0 && aSubLabelIndex == 0) {
          aLocationSFCT.setBodyLabel(true);
        }
      }
    };
  }

  private static void setupPainter(TLcdGXYLayer aLayer) {
    TLcdGXYIconPainter anchor_painter = new TLcdGXYIconPainter();
    anchor_painter.setIcon(new TLcdSymbol(TLcdSymbol.FILLED_RECT, 3, Color.black, Color.black));
    anchor_painter.setSelectionIcon(new TLcdSymbol(TLcdSymbol.FILLED_RECT, 3, Color.red, Color.red));
    aLayer.setGXYPainterProvider(anchor_painter);
    aLayer.setGXYEditorProvider(anchor_painter);
  }

  private static void setupLabelPainter(TLcdGXYLayer aLayer) {
    // Note that we could as well use a TLcdGXYStampLabelPainter here, offering
    // in place label editing capabilities etc.
    TLcdGXYLabelPainter label_painter = new TLcdGXYLabelPainter() {
      protected String[] retrieveLabels(int aMode, ILcdGXYContext aGXYContext) {
        return LABELS;
      }
    };
    label_painter.setShiftLabelPosition(15);
    label_painter.setWithPin(true);
    label_painter.setFilled(true);
    label_painter.setFrame(true);
    label_painter.setBackground(new Color(255, 255, 255, 64));
    label_painter.setPositionList(new int[]{
        TLcdGXYLabelPainter.SOUTH_EAST,
        TLcdGXYLabelPainter.SOUTH_WEST,
        TLcdGXYLabelPainter.NORTH_WEST,
        TLcdGXYLabelPainter.NORTH_EAST,
    });

    TLcdGXYIconPainter track_painter = new TLcdGXYIconPainter() {
      protected ILcdIcon getIcon(int aMode) {
        AntiAliasedIcon icon = (AntiAliasedIcon) super.getIcon(aMode);
        TLcdSymbol symbol = (TLcdSymbol) icon.getDelegate();
        ColoredLonLatPoint point = (ColoredLonLatPoint) getObject();
        symbol.setFillColor(point.getColor());
        return icon;
      }
    };
    track_painter.setIcon(new AntiAliasedIcon(new TLcdSymbol(TLcdSymbol.FILLED_CIRCLE, 16, Color.black, Color.red)));
    track_painter.setSelectionIcon(new AntiAliasedIcon(new TLcdSymbol(TLcdSymbol.FILLED_CIRCLE, 16, Color.red, Color.red)));

    TLcdGXYLabelPainterAdapter track_label_painter = new TLcdGXYLabelPainterAdapter(track_painter, track_painter);
    track_label_painter.setPositionList(new int[]{TLcdGXYLabelPainterAdapter.CENTER});

    TLcdGXYCompositeLabelPainter composite = new TLcdGXYCompositeLabelPainter();
    composite.addLabelPainter(track_label_painter, track_label_painter);
    composite.addLabelPainter(label_painter, label_painter);

    aLayer.setGXYLabelPainterProvider(composite);
    aLayer.setGXYLabelEditorProvider(composite);
  }
}

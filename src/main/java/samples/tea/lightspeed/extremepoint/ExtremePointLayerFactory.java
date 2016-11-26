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
package samples.tea.lightspeed.extremepoint;

import java.awt.Color;

import com.luciad.geodesy.TLcdGeodeticDatum;
import com.luciad.gui.ILcdIcon;
import com.luciad.gui.TLcdSymbol;
import com.luciad.model.TLcdModelDescriptor;
import com.luciad.model.TLcdVectorModel;
import com.luciad.reference.TLcdGeodeticReference;
import com.luciad.shape.ILcdPoint;
import com.luciad.shape.shape3D.TLcd3DEditablePointList;
import com.luciad.shape.shape3D.TLcdLonLatHeightPoint;
import com.luciad.shape.shape3D.TLcdLonLatHeightPolygon;
import com.luciad.util.ILcdFireEventMode;
import com.luciad.view.lightspeed.TLspContext;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.TLspPaintState;
import com.luciad.view.lightspeed.layer.shape.TLspShapeLayerBuilder;
import com.luciad.view.lightspeed.painter.label.style.ALspLabelTextProviderStyle;
import com.luciad.view.lightspeed.style.TLspIconStyle;
import com.luciad.view.lightspeed.style.TLspLineStyle;

import samples.tea.InvertedTriangleIcon;

/**
 * A simple factory class which creates a layer that will contain extreme minima and
 * extreme maxima points.
 * This factory also creates a layer containing a single polygon.
 */
class ExtremePointLayerFactory {

  private static final ILcdIcon ICON_MAXIMUM = new TLcdSymbol(TLcdSymbol.FILLED_TRIANGLE, 10, Color.BLACK, Color.RED);
  private static final ILcdIcon ICON_MINIMUM = new InvertedTriangleIcon(TLcdSymbol.FILLED_TRIANGLE, 10, Color.BLACK, Color.GREEN);

  /**
   * Returns a layer with an empty model that can contain extreme minimum points.
   * @return a layer with an empty model that can contain extreme minimum points.
   */
  public static ILspLayer createExtremeMinimaLayer() {
    // The longitude/latitude are based on WGS-84 (default TLcdGeodeticDatum)
    TLcdVectorModel model = new TLcdVectorModel();
    model.setModelReference(new TLcdGeodeticReference(new TLcdGeodeticDatum()));
    model.setModelDescriptor(new TLcdModelDescriptor("LowestPoints", "LowestPoints", "LowestPoints"));

    return TLspShapeLayerBuilder.newBuilder().model(model)
                                      .selectable(false)
                                      .bodyStyles(TLspPaintState.REGULAR, TLspIconStyle.newBuilder().icon(ICON_MINIMUM).build())
                                      .labelStyles(TLspPaintState.REGULAR, new ExtremeLabelStyler())
                                      .build();
  }

  /**
   * Returns a layer with an empty model that can contain extreme maximum points.
   * @return a layer with an empty model that can contain extreme maximum points.
   */
  public static ILspLayer createExtremeMaximaLayer() {
    // The longitude/latitude are based on WGS-84 (default TLcdGeodeticDatum)
    TLcdVectorModel model = new TLcdVectorModel();
    model.setModelReference( new TLcdGeodeticReference( new TLcdGeodeticDatum() ) );
    model.setModelDescriptor(new TLcdModelDescriptor("HighestPoints", "HighestPoints", "HighestPoints"));

    return TLspShapeLayerBuilder.newBuilder().model(model)
                                .selectable(false)
                                .bodyStyles(TLspPaintState.REGULAR, TLspIconStyle.newBuilder().icon(ICON_MAXIMUM).build())
                                .labelStyles(TLspPaintState.REGULAR, new ExtremeLabelStyler())
                                .build();
  }

  /**
   * Returns a layer with a model containing one polygon.
   * @return a layer with a model containing one polygon.
   */
  public static ILspLayer createPolygonLayer() {
    // The longitude/latitude are based on WGS-84 (default TLcdGeodeticDatum)
    TLcdVectorModel model = new TLcdVectorModel();
    TLcdGeodeticReference modelReference = new TLcdGeodeticReference(new TLcdGeodeticDatum());
    model.setModelReference(modelReference);
    model.setModelDescriptor(new TLcdModelDescriptor("Polygon", "Polygon", "Polygon"));

    // Add a polygon.
    TLcdLonLatHeightPoint[] points = new TLcdLonLatHeightPoint[] {
            new TLcdLonLatHeightPoint( 10.50, 47.00, 0 ),
            new TLcdLonLatHeightPoint( 11.25, 47.00, 0 ),
            new TLcdLonLatHeightPoint( 11.25, 45.50, 0 ),
            new TLcdLonLatHeightPoint( 11.00, 45.50, 0 ),
            new TLcdLonLatHeightPoint( 11.00, 46.50, 0 ),
    };
    TLcd3DEditablePointList pointList = new TLcd3DEditablePointList( points, false );
    TLcdLonLatHeightPolygon polygon = new TLcdLonLatHeightPolygon( pointList );
    model.addElement(polygon, ILcdFireEventMode.NO_EVENT);

    return TLspShapeLayerBuilder.newBuilder().model(model)
                                      .selectable(true)
                                      .editableSupported(true)
                                      .bodyEditable(true)
                                      .bodyStyles(TLspPaintState.REGULAR, TLspLineStyle.newBuilder().color(Color.BLUE).width(2).build())
                                      .bodyStyles(TLspPaintState.SELECTED, TLspLineStyle.newBuilder().color(Color.RED).width(2).build())
                                      .build();
  }

  private static class ExtremeLabelStyler extends ALspLabelTextProviderStyle {

    @Override
    public String[] getText(Object aDomainObject, Object aSubLabelID, TLspContext aContext) {
      if (aDomainObject instanceof ILcdPoint) {
        return new String[] { ((int)( (ILcdPoint) aDomainObject ).getZ()) + "m" };
      }
      return super.getText(aDomainObject, aSubLabelID, aContext);
    }
  }

}

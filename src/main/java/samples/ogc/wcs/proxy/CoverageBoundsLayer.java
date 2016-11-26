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
package samples.ogc.wcs.proxy;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;

import com.luciad.geodesy.TLcdGeodeticDatum;
import com.luciad.gui.TLcdAWTUtil;
import com.luciad.model.TLcdVectorModel;
import com.luciad.ogc.wcs.common.model.TLcdWCSCapabilities;
import com.luciad.ogc.wcs.common.model.TLcdWCSCoverageOfferingBrief;
import com.luciad.ogc.wcs.common.model.TLcdWCSLonLatEnvelope;
import com.luciad.reference.TLcdGeodeticReference;
import com.luciad.shape.shape2D.TLcdLonLatBounds;
import com.luciad.util.ILcdFireEventMode;
import com.luciad.util.TLcdNoBoundsException;
import com.luciad.view.gxy.ILcdGXYContext;
import com.luciad.view.gxy.ILcdGXYPainter;
import com.luciad.view.gxy.ILcdGXYPainterStyle;
import com.luciad.view.gxy.TLcdGXYLabelPainter;
import com.luciad.view.gxy.TLcdGXYLayer;
import com.luciad.view.gxy.painter.TLcdGXYBoundsPainter;
import com.luciad.view.map.TLcdGeodeticPen;

/**
 * An extension of TLcdGXYLayer that displays the bounds of all coverages
 * defined in a TLcdWCSCapabilities.
 */
class CoverageBoundsLayer extends TLcdGXYLayer {

  private TLcdGXYBoundsPainter fGXYBoundsPainter = new TLcdGXYBoundsPainter() {
    public void anchorPointSFCT(Graphics aGraphics, int aMode, ILcdGXYContext aGXYContext, Point aPointSFCT) throws TLcdNoBoundsException {
      boundsSFCT(aGraphics,
                 aMode,
                 aGXYContext,
                 fWorkBounds);
      aPointSFCT.x = (int) fWorkBounds.getLocation().getX();
      aPointSFCT.y = (int) fWorkBounds.getLocation().getY();
    }
  };
  private int fLineWidth = 2;
  private Color fDefaultColor = Color.blue;
  private Color fSelectionColor = Color.red;

  public CoverageBoundsLayer(TLcdWCSCapabilities aCapabilities) {

    TLcdVectorModel model = new TLcdVectorModel();
    model.setModelReference(new TLcdGeodeticReference(new TLcdGeodeticDatum()));
    for (int i = 0; i < aCapabilities.getContentMetadata().getCoverageOfferingBriefCount(); i++) {
      TLcdWCSCoverageOfferingBrief brief = aCapabilities.getContentMetadata().getCoverageOfferingBrief(i);
      TLcdWCSLonLatEnvelope envelope = brief.getLonLatEnvelope();
      double x0 = envelope.getLowerCorner().getCoordinate(0);
      double y0 = envelope.getLowerCorner().getCoordinate(1);
      double x1 = envelope.getUpperCorner().getCoordinate(0);
      double y1 = envelope.getUpperCorner().getCoordinate(1);
      CoverageBounds b = new CoverageBounds(x0, y0, x1 - x0, y1 - y0);
      b.setName(brief.getName());

      model.addElement(b, ILcdFireEventMode.NO_EVENT);
    }

    this.setModel(model);
    this.setLabel("Coverages");
    this.setSelectable(true);
    this.setEditable(false);
    this.setGXYPen(new TLcdGeodeticPen(false));

    fGXYBoundsPainter.setLineStyle(new MyLineStyle());
    this.setGXYPainterProvider(fGXYBoundsPainter);
    TLcdGXYLabelPainter labelPainter = new TLcdGXYLabelPainter() {
      protected String[] retrieveLabels(int aIndex, ILcdGXYContext aContext) {
        CoverageBounds cb = (CoverageBounds) getObject();
        return new String[]{cb.getName()};
      }
    };
    this.setGXYLabelPainterProvider(labelPainter);
    this.setLabeled(true);
    this.setGXYLayerLabelPainter(null);
  }

  private class MyLineStyle implements ILcdGXYPainterStyle {

    public void setupGraphics(Graphics aGraphics,
                              Object aObject,
                              int aMode,
                              ILcdGXYContext aGXYContext) {
      TLcdAWTUtil.changeStrokeToPlainLine(aGraphics, fLineWidth);
      if (((aMode & ILcdGXYPainter.SELECTED) != 0) &&
          (fSelectionColor != null)) {
        aGraphics.setColor(fSelectionColor);
      } else if (fDefaultColor != null) {
        aGraphics.setColor(fDefaultColor);
      }
    }
  }

  private class CoverageBounds extends TLcdLonLatBounds {
    private String fName = "";

    public CoverageBounds(double aX, double aX1, double aX2, double aX3) {
      super(aX, aX1, aX2, aX3);
    }

    public String getName() {
      return fName;
    }

    public void setName(String aName) {
      fName = aName;
    }

    public String toString() {
      return fName;
    }
  }
}

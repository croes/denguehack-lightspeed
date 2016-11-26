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
package samples.lightspeed.nongeoreferenced;

import com.luciad.model.ILcdModel;
import com.luciad.model.TLcdModelDescriptor;
import com.luciad.model.TLcdVectorModel;
import com.luciad.shape.shape2D.ILcd2DEditablePoint;
import com.luciad.shape.shape2D.TLcd2DEditablePointList;
import com.luciad.shape.shape2D.TLcdXYPoint;
import com.luciad.shape.shape2D.TLcdXYPolygon;
import com.luciad.shape.shape2D.TLcdXYPolyline;

import samples.common.model.CartesianReference;

/**
 * Model factory for the sample.
 */
class ModelFactory {

  private ModelFactory() {
  }

  public static ILcdModel createAnnotationsModel() {
    TLcdVectorModel model = new TLcdVectorModel(CartesianReference.getInstance(), new TLcdModelDescriptor("Annotations", "Annotations", "Annotations"));
    model.addElement(
        new TLcdXYPolyline(
            new TLcd2DEditablePointList(
                new ILcd2DEditablePoint[]{
                    new TLcdXYPoint(236.84, 6.82),
                    new TLcdXYPoint(261.61, 147.74),
                    new TLcdXYPoint(235.13, 219.48),
                    new TLcdXYPoint(195.84, 255.35),
                    new TLcdXYPoint(160.83, 313.43),
                    new TLcdXYPoint(146.31, 331.36),
                    new TLcdXYPoint(77.13, 347.59),
                    new TLcdXYPoint(53.21, 366.38),
                    new TLcdXYPoint(35.28, 402.25),
                }, false
            )
        ),
        ILcdModel.NO_EVENT
    );
    model.addElement(
        new TLcdXYPolygon(
            new TLcd2DEditablePointList(
                new ILcd2DEditablePoint[]{
                    new TLcdXYPoint(207.22, 226.75),
                    new TLcdXYPoint(210.72, 223.83),
                    new TLcdXYPoint(226.19, 243.39),
                    new TLcdXYPoint(223.86, 246.60),
                }, false
            )
        ), ILcdModel.NO_EVENT
    );
    model.addElement(
        new TLcdXYPolygon(
            new TLcd2DEditablePointList(
                new ILcd2DEditablePoint[]{
                    new TLcdXYPoint(168.68, 268.79),
                    new TLcdXYPoint(166.35, 272.88),
                    new TLcdXYPoint(189.99, 281.93),
                    new TLcdXYPoint(191.45, 276.96),
                }, false
            )
        ), ILcdModel.NO_EVENT
    );
    model.addElement(
        new TLcdXYPolygon(
            new TLcd2DEditablePointList(
                new ILcd2DEditablePoint[]{
                    new TLcdXYPoint(154.67, 295.06),
                    new TLcdXYPoint(155.25, 291.56),
                    new TLcdXYPoint(181.53, 295.06),
                    new TLcdXYPoint(180.07, 298.86),
                }, false
            )
        ), ILcdModel.NO_EVENT
    );
    model.addElement(new TLcdXYPoint(273.08, 165.74), ILcdModel.NO_EVENT);
    return model;
  }
}

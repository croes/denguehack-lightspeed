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
package samples.lightspeed.demo.tools;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import com.luciad.format.shp.TLcdSHPModelDecoder;
import com.luciad.format.shp.TLcdSHPModelEncoder;
import com.luciad.model.ILcdModel;
import com.luciad.shape.ILcdPoint;
import com.luciad.shape.ILcdShape;
import com.luciad.shape.TLcdFeaturedShapeList;
import com.luciad.shape.shape2D.TLcdLonLatBounds;
import com.luciad.shape.shape2D.TLcdLonLatPolyline;

/**
 * @author christiaanv
 * @since 2012.0
 */
public class InverseClipLAStreets {

  private static final String SRC = "C:\\Shp\\roadtrl020.shp";
  private static final String DST = "Data/internal.data/lastreetsclipped.shp";

  private static final double CX = -118.31;
  private static final double CY = 34;
  private static final double W = 0.75;
  private static final double H = 0.75;

  private static final TLcdLonLatBounds CLIP_BOUNDS = new TLcdLonLatBounds(CX - W / 2.0, CY - H / 2.0, W, H);


  private static void invertClipPolylinesSFCT(List<TLcdLonLatPolyline> aPolylineListSFCT) {
    ArrayList<TLcdLonLatPolyline> newLines = new ArrayList<TLcdLonLatPolyline>();
    for (int i = 0; i < aPolylineListSFCT.size(); i++) {
      TLcdLonLatPolyline polyline = aPolylineListSFCT.get(i);
      TLcdLonLatPolyline tempLine = new TLcdLonLatPolyline();
      newLines.add(tempLine);
      int j = 0;
      int max = polyline.getPointCount();
      while (polyline.getPointCount() != 0) {
        if (isWithinBounds(polyline.getPoint(0))) {
          tempLine.insert2DPoint(tempLine.getPointCount(), polyline.getPoint(0).getX(), polyline.getPoint(0).getY());
          polyline.removePointAt(0);
          j++;
        } else {
          if ((j == 0) || (j == max - 1)) {
            polyline.removePointAt(0);
            j++;
          } else {
            tempLine = new TLcdLonLatPolyline();
            newLines.add(tempLine);
            j++;
            polyline.removePointAt(0);
          }
        }
      }
    }
    aPolylineListSFCT.clear();
    aPolylineListSFCT.addAll(newLines);
  }

  private static boolean isWithinBounds(ILcdPoint point) {
    return !CLIP_BOUNDS.contains2D(point);
  }
}

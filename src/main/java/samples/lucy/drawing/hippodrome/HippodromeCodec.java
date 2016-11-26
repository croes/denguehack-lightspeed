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
package samples.lucy.drawing.hippodrome;

import java.util.Map;

import com.luciad.lucy.addons.drawing.format.ALcyShapeCodec;
import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelReference;
import com.luciad.shape.ILcdPoint;
import com.luciad.shape.ILcdShape;
import com.luciad.shape.shape3D.ILcd3DEditablePoint;
import com.luciad.transformation.TLcdGeoReference2GeoReference;
import com.luciad.util.TLcdOutOfBoundsException;
import com.luciad.util.logging.ILcdLogger;
import com.luciad.util.logging.TLcdLoggerFactory;

import samples.gxy.hippodromePainter.IHippodrome;

/**
 * Codec for a hippodrome. This codec stores the geometry of a hippodrome
 * (i.e. startpoint, endpoint and width) in a map. It can also restore
 * the state of a hippodrome from that map. If necessary, a model conversion
 * is applied.
 */
public class HippodromeCodec extends ALcyShapeCodec {

  private static ILcdLogger sLogger = TLcdLoggerFactory.getLogger(HippodromeCodec.class.getName());

  private static TLcdGeoReference2GeoReference sTransformer = new TLcdGeoReference2GeoReference();

  protected final String fPrefix;

  public HippodromeCodec(String aPrefix) {
    fPrefix = aPrefix;
  }

  public boolean canStore(ILcdShape aShape) {
    return aShape instanceof IHippodrome;
  }

  public boolean canRestore(ILcdShape aShape) {
    return (aShape instanceof IHippodrome);
  }

  @Override
  public void storeState(ILcdShape aShape, ILcdModel aSourceModel, Map aMapSFCT) throws IllegalArgumentException {
    aMapSFCT.put(fPrefix + "sourceModelReference", aSourceModel.getModelReference());
    IHippodrome aHippodrome = (IHippodrome) aShape;
    storePointLocation(
        aSourceModel.getModelReference(), aHippodrome.getStartPoint(), aMapSFCT, fPrefix + "startpoint.");
    storePointLocation(
        aSourceModel.getModelReference(), aHippodrome.getEndPoint(), aMapSFCT, fPrefix + "endpoint.");
    aMapSFCT.put(fPrefix + "width", new Double(aHippodrome.getWidth()));
  }

  @Override
  public void restoreState(ILcdShape aShape, ILcdModel aTargetModel, Map aMap) throws IllegalArgumentException, TLcdOutOfBoundsException {
    ILcdModelReference source_reference = (ILcdModelReference) aMap.get(fPrefix + "sourceModelReference");
    ILcdPoint point = calculateTargetPoint(aMap, fPrefix + "startpoint.", source_reference, aTargetModel.getModelReference());
    IHippodrome aHippodrome = (IHippodrome) aShape;
    if (point != null) {
      aHippodrome.moveReferencePoint(point, IHippodrome.START_POINT);
    }
    point = calculateTargetPoint(aMap, fPrefix + "endpoint.", source_reference, aTargetModel.getModelReference());
    if (point != null) {
      aHippodrome.moveReferencePoint(point, IHippodrome.END_POINT);
    }
    Double width = (Double) aMap.get(fPrefix + "width");
    if (width != null) {
      aHippodrome.setWidth(width.doubleValue());
    }
  }

  public static ILcdPoint calculateTargetPoint(Map aMap, String aPrefix, ILcdModelReference aSourceModelReference, ILcdModelReference aTargetModelReference) throws TLcdOutOfBoundsException {
    ILcdPoint stored_point = (ILcdPoint) aMap.get(aPrefix + "location");
    return calculateTargetPoint(aTargetModelReference, aSourceModelReference, stored_point);
  }

  public static ILcdPoint calculateTargetPoint(
      ILcdModelReference aTargetModelReference,
      ILcdModelReference aSourceModelReference, ILcdPoint aStoredPoint) throws TLcdOutOfBoundsException {

    ILcd3DEditablePoint target_point = aTargetModelReference.makeModelPoint().cloneAs3DEditablePoint();
    if (aStoredPoint != null) {
      if (aSourceModelReference == null ||
          aSourceModelReference.equals(aTargetModelReference)) {
        //no fancy calculations required
        target_point.move3D(aStoredPoint);
      } else {
        //convert from different source model reference
        sTransformer.setSourceReference(aSourceModelReference);
        sTransformer.setDestinationReference(aTargetModelReference);

        if (sLogger.isTraceEnabled()) {
          sLogger.trace("Converting from source to target reference");
        }
        sTransformer.sourcePoint2destinationSFCT(aStoredPoint, target_point);
      }

      return target_point;
    } else {
      return null;
    }
  }

  public static void storePointLocation(
      ILcdModelReference aSourceModelReference, ILcdPoint aPoint,
      Map aMap, String aPrefix) {

    ILcd3DEditablePoint point_to_store = aSourceModelReference.makeModelPoint().cloneAs3DEditablePoint();
    point_to_store.move3D(aPoint);
    aMap.put(aPrefix + "location", point_to_store);
  }

  public static void restorePointLocation(
      ILcdModelReference aSourceModelReference, ILcdPoint aStoredPoint,
      ILcdModelReference aTargetModelReference, ILcd3DEditablePoint aPointToMove) throws TLcdOutOfBoundsException {

    if (aStoredPoint != null) {
      ILcdPoint target_point = calculateTargetPoint(
          aTargetModelReference, aSourceModelReference, aStoredPoint);

      if (target_point != null) {
        aPointToMove.move3D(target_point);
      }
    }
  }
}

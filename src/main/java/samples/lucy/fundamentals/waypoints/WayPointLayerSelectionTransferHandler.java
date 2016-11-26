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
package samples.lucy.fundamentals.waypoints;

import com.luciad.datamodel.ILcdDataObject;
import com.luciad.lucy.datatransfer.ALcyDefaultLayerSelectionTransferHandler;
import com.luciad.model.ILcdModel;
import com.luciad.shape.ALcdShape;
import com.luciad.shape.ILcdPoint;
import com.luciad.shape.ILcdShape;
import com.luciad.shape.shape3D.TLcdLonLatHeightPoint;
import com.luciad.transformation.TLcdGeoReference2GeoReference;
import com.luciad.util.TLcdOutOfBoundsException;

import samples.gxy.fundamentals.step3.WayPointDataTypes;
import samples.gxy.fundamentals.step3.WayPointModelDecoder;

/**
 * <p>
 *   This {@code ALcyLayerSelectionTransferHandler} extension allows to:
 * </p>
 *
 * <ul>
 *   <li>Cut/Copy-paste way points in the same layer.</li>
 *   <li>Cut/Copy-paste way points between different layers.</li>
 *   <li>Cut/Copy-paste points from another layer into the way points layer.</li>
 * </ul>
 *
 * Note that copying way points to another layer, for example a drawing layer, is taken
 * care of by that format.
 */
public class WayPointLayerSelectionTransferHandler extends ALcyDefaultLayerSelectionTransferHandler<ILcdDataObject> {

  private final TLcdGeoReference2GeoReference fTransformer = new TLcdGeoReference2GeoReference();

  public WayPointLayerSelectionTransferHandler() {
    super(null);
  }

  @Override
  protected ILcdDataObject createDomainObjectCopy(ILcdDataObject aDomainObject, ILcdModel aSourceModel, ILcdModel aTargetModel) {
    if (aSourceModel.getModelReference().equals(aTargetModel.getModelReference())) {
      //no transformation needed as we only copy between models of the same reference
      TLcdLonLatHeightPoint originalLocation = (TLcdLonLatHeightPoint) ALcdShape.fromDomainObject(aDomainObject);
      return WayPointModelDecoder.createWayPoint(
          (String) aDomainObject.getValue(WayPointDataTypes.NAME),
          originalLocation.getX(),
          originalLocation.getY(),
          originalLocation.getZ()
      );
    }
    return null;
  }

  @Override
  protected ILcdDataObject createDomainObjectForShape(ILcdShape aShape, ILcdModel aSourceModel, ILcdModel aTargetModel) {
    if (aShape instanceof ILcdPoint) {
      fTransformer.setSourceReference(aSourceModel.getModelReference());
      fTransformer.setDestinationReference(aTargetModel.getModelReference());

      ILcdDataObject wayPoint = WayPointModelDecoder.createWayPoint();
      try {
        fTransformer.sourcePoint2destinationSFCT((ILcdPoint) aShape, (TLcdLonLatHeightPoint) ALcdShape.fromDomainObject(wayPoint));
        return wayPoint;
      } catch (TLcdOutOfBoundsException aE) {
        getLogListener().fail("Could not copy shape from source to destination reference");
        return null;
      }
    }
    getLogListener().fail("Only point shapes can be pasted into a way points layer.");
    return null;
  }

  @Override
  protected ILcdShape createShapeCopy(ILcdShape aShape, ILcdModel aSourceModel) {
    ILcdPoint originalLocation = (ILcdPoint) aShape;
    TLcdLonLatHeightPoint copy = new TLcdLonLatHeightPoint();
    copy.move3D(originalLocation);
    return copy;
  }

}

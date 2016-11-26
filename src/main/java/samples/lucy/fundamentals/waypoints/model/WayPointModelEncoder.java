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
package samples.lucy.fundamentals.waypoints.model;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Enumeration;

import com.luciad.datamodel.ILcdDataObject;
import com.luciad.io.ILcdOutputStreamFactory;
import com.luciad.io.TLcdFileOutputStreamFactory;
import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelEncoder;
import com.luciad.model.TLcdModelDescriptor;
import com.luciad.shape.shape3D.TLcdLonLatHeightPoint;
import com.luciad.util.ILcdFilter;
import com.luciad.util.concurrent.TLcdLockUtil;

import samples.gxy.fundamentals.step3.WayPointDataTypes;
import samples.gxy.fundamentals.step3.WayPointModelDecoder;
import samples.lucy.fundamentals.waypoints.WayPointsModelFilter;

/**
 * <p>
 *   {@code ILcdModelEncoder} implementation for the way point models.
 * </p>
 *
 * <p>
 *   See the class javadoc of the {@link WayPointModelDecoder}
 *   for documentation on the format of the waypoint files.
 * </p>
 */
final class WayPointModelEncoder implements ILcdModelEncoder {

  private final ILcdFilter<ILcdModel> fWayPointModelFilter = new WayPointsModelFilter();
  private final ILcdOutputStreamFactory fOutputStreamFactory = new TLcdFileOutputStreamFactory();

  @Override
  public String getDisplayName() {
    return WayPointModelDecoder.DISPLAY_NAME;
  }

  @Override
  public boolean canSave(ILcdModel aModel) {
    return canExport(aModel, aModel.getModelDescriptor().getSourceName());
  }

  @Override
  public void save(ILcdModel aModel) throws IllegalArgumentException, IOException {
    export(aModel, aModel.getModelDescriptor().getSourceName());
  }

  @Override
  public boolean canExport(ILcdModel aModel, String aDestinationName) {
    return fWayPointModelFilter.accept(aModel) && aDestinationName.endsWith(".cwp");
  }

  @Override
  public void export(ILcdModel aModel, String aDestinationName) throws IllegalArgumentException, IOException {
    try (PrintWriter writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(fOutputStreamFactory.createOutputStream(aDestinationName))))) {
      try (TLcdLockUtil.Lock autoUnlock = TLcdLockUtil.readLock(aModel)) {
        Enumeration elements = aModel.elements();
        while (elements.hasMoreElements()) {
          ILcdDataObject wayPoint = (ILcdDataObject) elements.nextElement();
          writeRecord(wayPoint, writer);
        }
      }
    }
    try (TLcdLockUtil.Lock autoUnlock = TLcdLockUtil.writeLock(aModel)) {
      TLcdModelDescriptor waypointModelDescriptor = (TLcdModelDescriptor) aModel.getModelDescriptor();
      waypointModelDescriptor.setSourceName(aDestinationName);
    }
  }

  private void writeRecord(ILcdDataObject aWayPoint, PrintWriter aWriter) throws IOException {
    aWriter.println(aWayPoint.getValue(WayPointDataTypes.NAME));

    TLcdLonLatHeightPoint point =
        (TLcdLonLatHeightPoint) aWayPoint.getValue(WayPointDataTypes.POINT);

    double lon = point.getX();
    double lat = point.getY();
    double height = point.getZ();

    aWriter.print(lon);
    aWriter.print(" ");

    aWriter.print(lat);
    aWriter.print(" ");

    aWriter.print(height);

    aWriter.println();
  }
}

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
package samples.lightspeed.demo.application.data.maritime;

import static samples.lightspeed.demo.application.data.maritime.ExactAISModelDescriptor.NavigationalStatus;

import com.luciad.shape.ALcdPoint;
import com.luciad.shape.shape2D.ILcd2DEditableBounds;
import com.luciad.shape.shape2D.ILcd2DEditablePoint;
import com.luciad.shape.shape2D.TLcdLonLatBounds;
import com.luciad.shape.shape2D.TLcdLonLatPoint;
import com.luciad.shape.shape3D.ILcd3DEditableBounds;
import com.luciad.shape.shape3D.ILcd3DEditablePoint;
import com.luciad.shape.shape3D.TLcdLonLatHeightBounds;
import com.luciad.shape.shape3D.TLcdLonLatHeightPoint;
import com.luciad.util.ILcdOriented;

/**
 * An AIS plot.
 *
 * @see ExactAISModelDescriptor
 */
public class AISPlot extends ALcdPoint implements ILcdOriented {
  private final long fTimeStamp;
  private final int fID;
  private final int fNavigationalStatus;
  private final double fLon;
  private final double fLat;
  private final double fOrientation;
  private double fOceanFloorDepth;

  public AISPlot(
      double aLon,
      double aLat,
      double aOrientation,
      long aTimeStamp,
      int aID,
      int aNavigationalStatus
  ) {
    fLon = aLon;
    fLat = aLat;
    fOrientation = aOrientation;
    fTimeStamp = aTimeStamp;
    fID = aID;
    fNavigationalStatus = aNavigationalStatus;
  }

  @Override
  public ILcd2DEditableBounds cloneAs2DEditableBounds() {
    return new TLcdLonLatBounds(getX(), getY(), 0, 0);
  }

  @Override
  public ILcd3DEditableBounds cloneAs3DEditableBounds() {
    return new TLcdLonLatHeightBounds(getX(), getY(), getZ(), 0, 0, 0);
  }

  @Override
  public double getX() {
    return fLon;
  }

  @Override
  public double getY() {
    return fLat;
  }

  @Override
  public double getZ() {
    return 0.0;
  }

  @Override
  public ILcd2DEditablePoint cloneAs2DEditablePoint() {
    return new TLcdLonLatPoint(getX(), getY());
  }

  @Override
  public ILcd3DEditablePoint cloneAs3DEditablePoint() {
    return new TLcdLonLatHeightPoint(getX(), getY(), getZ());
  }

  @Override
  public double getOrientation() {
    return fNavigationalStatus == NavigationalStatus.ANCHORED ? 0.0 : fOrientation; // we ignore the rotation for anchored icons;
  }

  public double getActualOrientation() {
    return fOrientation;
  }

  public long getTimeStamp() {
    return fTimeStamp;
  }

  public int getID() {
    return fID;
  }

  public int getNavigationalStatus() {
    return fNavigationalStatus;
  }

  public void setOceanFloorDepth(double aOceanFloorDepth) {
    fOceanFloorDepth = aOceanFloorDepth;
  }

  public double getOceanFloorDepth() {
    return fOceanFloorDepth;
  }

}

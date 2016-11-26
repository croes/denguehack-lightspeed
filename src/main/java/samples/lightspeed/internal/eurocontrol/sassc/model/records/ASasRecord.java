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
package samples.lightspeed.internal.eurocontrol.sassc.model.records;

import com.luciad.shape.ILcdBounds;
import com.luciad.shape.ILcdPoint;
import com.luciad.shape.shape2D.ILcd2DEditableBounds;
import com.luciad.shape.shape2D.ILcd2DEditablePoint;
import com.luciad.shape.shape3D.ILcd3DEditableBounds;
import com.luciad.shape.shape3D.ILcd3DEditablePoint;
import com.luciad.shape.shape3D.TLcdLonLatHeightBounds;
import com.luciad.shape.shape3D.TLcdLonLatHeightPoint;
import com.luciad.util.TLcdLonLatCoord;

/**
 * A SASS-C record.
 */
public abstract class ASasRecord implements ILcdPoint, ILcdBounds {

  private final double fLon;
  private final double fLat;
  private final double fZ;
  private final int fDataSourceId;
  private final int fReportType;
  private final int fMode3ACode;
  private final long fTimeOfDetection;
  private final String fCallsign;

  protected ASasRecord(double aLon, double aLat, double aHeight,
                       int aDataSourceId, int aReportType, int aMode3ACode, long aTimeOfDetection,
                       String aCallsign) {
    fLon = aLon;
    fLat = aLat;
    fZ = aHeight + 1000;  // hack some additional some height to avoid tracks under terrain
    fDataSourceId = aDataSourceId;
    fReportType = aReportType;
    fMode3ACode = aMode3ACode;
    fTimeOfDetection = aTimeOfDetection;
    fCallsign = aCallsign == null ? null : aCallsign.intern();
  }

  public int getDataSourceId() {
    return fDataSourceId;
  }

  public int getReportType() {
    return fReportType;
  }

  public int getMode3ACode() {
    return fMode3ACode;
  }

  public long getTimeOfDetection() {
    return fTimeOfDetection;
  }

  public String getCallsign() {
    return fCallsign;
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "(code " + getMode3ACode() + ", ds " + getDataSourceId() + ", t " + fTimeOfDetection + ")";
  }

  @Override
  public ILcd3DEditablePoint cloneAs3DEditablePoint() {
    return new TLcdLonLatHeightPoint(fLon, fLat, fZ);
  }

  @Override
  public ILcd2DEditablePoint cloneAs2DEditablePoint() {
    return cloneAs3DEditablePoint();
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
    return fZ;
  }

  @Override
  public double getCosX() {
    return Math.cos(getX());
  }

  @Override
  public double getCosY() {
    return Math.cos(getY());
  }

  @Override
  public double getSinX() {
    return Math.sin(getX());
  }

  @Override
  public double getSinY() {
    return Math.sin(getY());
  }

  @Override
  public double getTanX() {
    return Math.tan(getX());
  }

  @Override
  public double getTanY() {
    return Math.tan(getY());
  }

  @Override
  public ILcdPoint getFocusPoint() {
    return this;
  }

  @Override
  public boolean contains2D(ILcdPoint aPoint) {
    return contains2D(aPoint.getX(), aPoint.getY());
  }

  @Override
  public boolean contains2D(double aX, double aY) {
    return TLcdLonLatCoord.normalizeLon(aX) == fLon &&
           aY == fLat;
  }

  @Override
  public boolean contains3D(ILcdPoint aPoint) {
    return contains3D(aPoint.getX(), aPoint.getY(), aPoint.getZ());
  }

  @Override
  public boolean contains3D(double aX, double aY, double aZ) {
    return contains2D(aX, aY) && aZ == fZ;
  }

  @Override
  public boolean isDefined() {
    return true;
  }

  @Override
  public ILcdPoint getLocation() {
    return this;
  }

  @Override
  public double getWidth() {
    return 0;
  }

  @Override
  public double getHeight() {
    return 0;
  }

  @Override
  public double getDepth() {
    return 0;
  }

  @Override
  public boolean interacts2D(ILcdBounds aBounds) {
    return contains2D(aBounds.getLocation().getX(), aBounds.getLocation().getY(), aBounds.getWidth(), aBounds.getHeight());
  }

  @Override
  public boolean interacts2D(double aX, double aY, double aWidth, double aHeight) {
    double x = TLcdLonLatCoord.normalizeLon(aX);
    if (x > aX) {
      x -= 360.0;
    }
    return (fLon >= x && fLon <= x + aWidth) &&
           fLat >= aY && fLat <= aY + aHeight;
  }

  @Override
  public boolean contains2D(ILcdBounds aBounds) {
    return contains2D(aBounds.getLocation().getX(), aBounds.getLocation().getY(), aBounds.getWidth(), aBounds.getHeight());
  }

  @Override
  public boolean contains2D(double aX, double aY, double aWidth, double aHeight) {
    return false;
  }

  @Override
  public boolean interacts3D(ILcdBounds aBounds) {
    return interacts3D(aBounds.getLocation().getX(), aBounds.getLocation().getY(), aBounds.getLocation().getY(), aBounds.getWidth(), aBounds.getHeight(), aBounds.getDepth());
  }

  @Override
  public boolean interacts3D(double aX, double aY, double aZ, double aWidth, double aHeight, double aDepth) {
    return interacts2D(aX, aY, aWidth, aHeight) &&
           fZ >= aZ && fZ <= aZ + aDepth;
  }

  @Override
  public boolean contains3D(ILcdBounds aBounds) {
    return contains3D(aBounds.getLocation()) &&
           aBounds.getWidth() == 0 && aBounds.getHeight() == 0 && aBounds.getDepth() == 0;
  }

  @Override
  public boolean contains3D(double aX, double aY, double aZ, double aWidth, double aHeight, double aDepth) {
    return contains3D(aX, aY, aZ) &&
           aWidth == 0 && aHeight == 0 && aDepth == 0;
  }

  @Override
  public ILcd2DEditableBounds cloneAs2DEditableBounds() {
    return cloneAs3DEditableBounds();
  }

  @Override
  public ILcd3DEditableBounds cloneAs3DEditableBounds() {
    return new TLcdLonLatHeightBounds(fLon, fLat, fZ, 0, 0, 0);
  }

  @Override
  public ILcdBounds getBounds() {
    return this;
  }

  @Override
  public Object clone() {
    return this; // immutable
  }
}

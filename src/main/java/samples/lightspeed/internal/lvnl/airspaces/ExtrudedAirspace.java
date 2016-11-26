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
package samples.lightspeed.internal.lvnl.airspaces;

import com.luciad.ais.model.airspace.ILcdAirspace;
import com.luciad.ais.model.airspace.ILcdAirspaceSegment;
import com.luciad.ais.model.util.ILcdDefaultDisplayNameSettable;
import com.luciad.ais.shape.ILcdGeoPathLeg;
import com.luciad.datamodel.TLcdDataProperty;
import com.luciad.datamodel.TLcdDataType;
import com.luciad.shape.ILcdBounded;
import com.luciad.shape.ILcdPoint;
import com.luciad.shape.ILcdShape;
import com.luciad.shape.shape3D.TLcdExtrudedShape;
import com.luciad.util.ILcdFeatured;

/**
 * Date: Feb 3, 2006
 * Time: 5:09:53 PM
 *
 * @author Tom Nuydens
 */
class ExtrudedAirspace
    extends TLcdExtrudedShape
    implements ILcdAirspace, ILcdFeatured, ILcdDefaultDisplayNameSettable, ILcdBounded {

  private ILcdAirspace fAirspace;
  private ILcdFeatured fFeatures;
  private ILcdDefaultDisplayNameSettable fDisplayNameSettable;
  public static final int DAFIF = 1;
  public static final int ARINC = 0;
  private int fType = 0;

  public ExtrudedAirspace() {
    super();
  }

  public ExtrudedAirspace(ILcdShape aILcdShape) {
    super(aILcdShape);
  }

  public ExtrudedAirspace(ILcdShape aBaseShape, double aMinZ, double aMaxZ, int type) {//type param added by Sofie, to quickly see what is the source, Arinc or Dafif. todo: check properly on features later.
    super(aBaseShape, aMinZ, aMaxZ);
    fType = type;
  }

  public void setBaseShape(ILcdShape aShape) {
    if (!(aShape instanceof ILcdAirspace)) {
      throw new IllegalArgumentException("Can only extrude ILcdAirspace objects!");
    }
    if (!(aShape instanceof ILcdFeatured)) {
      throw new IllegalArgumentException("Can only extrude ILcdFeatured objects!");
    }
    if (!(aShape instanceof ILcdDefaultDisplayNameSettable)) {
      throw new IllegalArgumentException("Can only extrude ILcdDefaultDisplayNameSettable objects!");
    }

    fAirspace = (ILcdAirspace) aShape;
    fFeatures = (ILcdFeatured) aShape;
    fDisplayNameSettable = (ILcdDefaultDisplayNameSettable) aShape;

    super.setBaseShape(aShape);
  }

  public ILcdAirspaceSegment getSegmentBySegmentNumber(int aIndex) {
    return fAirspace.getSegment(aIndex);
  }

  public ILcdAirspaceSegment getSegment(int aIndex) {
    return fAirspace.getSegment(aIndex);
  }

  public int getSegmentCount() {
    return fAirspace.getSegmentCount();
  }

  public int getOrientation() {
    return fAirspace.getOrientation();
  }

  public int getPointCount() {
    return fAirspace.getPointCount();
  }

  public ILcdPoint getPoint(int aIndex) throws IndexOutOfBoundsException {
    return fAirspace.getPoint(aIndex);
  }

  public int getLegCount() {
    return fAirspace.getLegCount();
  }

  public ILcdGeoPathLeg getLeg(int aIndex) {
    return fAirspace.getLeg(aIndex);
  }

  public int getFeatureCount() {
    return fFeatures.getFeatureCount();
  }

  public Object getFeature(int aIndex) throws IndexOutOfBoundsException {
    return fFeatures.getFeature(aIndex);
  }

  public void setFeature(int aIndex, Object aObject) throws IllegalArgumentException {
    fFeatures.setFeature(aIndex, aObject);
  }

  public boolean canSetFeature(int aIndex) {
    return fFeatures.canSetFeature(aIndex);
  }

  public void setDefaultDisplayNameIndex(int aIndex) {
    fDisplayNameSettable.setDefaultDisplayNameIndex(aIndex);
  }

  public int getDefaultDisplayNameIndex() {
    return fDisplayNameSettable.getDefaultDisplayNameIndex();
  }

  public String getDefaultDisplayName() {
    return fDisplayNameSettable.getDefaultDisplayName();
  }

  public int getType() {
    return fType;
  }

  @Override
  public TLcdDataType getDataType() {
    return fAirspace.getDataType();
  }

  @Override
  public Object getValue(TLcdDataProperty aProperty) {
    return fAirspace.getValue(aProperty);
  }

  @Override
  public Object getValue(String aPropertyName) {
    return fAirspace.getValue(aPropertyName);
  }

  @Override
  public void setValue(TLcdDataProperty aProperty, Object aValue) {
    fAirspace.setValue(aProperty, aValue);
  }

  @Override
  public void setValue(String aPropertyName, Object aValue) {
    fAirspace.setValue(aPropertyName, aValue);
  }

  @Override
  public boolean hasValue(TLcdDataProperty aProperty) {
    return fAirspace.hasValue(aProperty);
  }

  @Override
  public boolean hasValue(String aPropertyName) {
    return fAirspace.hasValue(aPropertyName);
  }
}

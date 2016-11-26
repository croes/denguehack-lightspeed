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
package samples.common.dimensionalfilter.model;

import java.util.Objects;

import com.luciad.util.iso19103.ILcdISO19103UnitOfMeasure;

/**
 * <p>Represents the dimensional filters who have the same type and unit (e.g.<!-- --> depth in meters). This class is used
 * as an identifier for groups of filter. It is typically created by {@link DimensionalFilterManager FilterManager}.</p>
 *
 * <p>For more information, see {@link DimensionalFilter Filter}.</p>
 */
public class DimensionalFilterGroup implements Comparable {
  private final String fName;
  private final Class<? extends Comparable> fType;
  private final ILcdISO19103UnitOfMeasure fUnit;
  private final boolean fPositive;

  DimensionalFilterGroup(String aName, Class<? extends Comparable> aType, ILcdISO19103UnitOfMeasure aUnit, boolean aPositive) {
    fName = aName;
    fType = aType;
    fUnit = aUnit;
    fPositive = aPositive;
  }

  public String getName() {
    return fName;
  }

  public Class<? extends Comparable> getType() {
    return fType;
  }

  public ILcdISO19103UnitOfMeasure getUnit() {
    return fUnit;
  }

  public boolean isPositive() {
    return fPositive;
  }

  public boolean isCompatibleWith(DimensionalFilter aDimensionalFilter) {
    return aDimensionalFilter.getType().equals(getType()) && Objects.equals(aDimensionalFilter.getUnit(), getUnit());
  }

  @Override
  public String toString() {
    StringBuilder buffer = new StringBuilder(getName());
    if (getUnit() != null) {
      buffer.append("(");
      buffer.append(getUnit().getUOMSymbol());
      buffer.append(")");
    }
    return buffer.toString();
  }

  @Override
  public int compareTo(Object o) {
    return toString().compareTo(o.toString());
  }

  @Override
  public boolean equals(Object obj) {
    if(obj == this) {
      return true;
    }
    if(!(obj instanceof DimensionalFilterGroup)) {
      return false;
    }
    DimensionalFilterGroup dimensionalFilterGroup = (DimensionalFilterGroup) obj;
    return fName.equals(dimensionalFilterGroup.fName)
           && fUnit.equals(dimensionalFilterGroup.fUnit);
  }
}

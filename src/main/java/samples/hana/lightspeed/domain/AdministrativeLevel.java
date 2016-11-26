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
package samples.hana.lightspeed.domain;

import com.luciad.datamodel.ILcdDataObject;

/**
 * Enumeration describing different administrative levels.
 */
public enum AdministrativeLevel {

  country,
  state,
  county;

  /**
   * Determines the administrative level the given domain object is part of.
   */
  public static AdministrativeLevel getAdministrativeLevel(ILcdDataObject aObject) {
    if (aObject.getDataType().getName().contains("states")) {
      return AdministrativeLevel.state;
    } else if (aObject.getDataType().getName().contains("counties")) {
      return AdministrativeLevel.county;
    } else {
      throw new IllegalArgumentException("Unknown data type " + aObject.getDataType());
    }
  }

  /**
   * Provides a unique key for a data object.
   */
  public static String getAdministrativeLevelKey(ILcdDataObject aObject) {
    if (aObject.getDataType().getName().contains("states")) {
      return Integer.parseInt((String) aObject.getValue("STATE_FIPS")) + "";
    } else if (aObject.getDataType().getName().contains("counties")) {
      return Integer.parseInt((String) aObject.getValue("STATE_FIPS")) + "-" + Integer.parseInt((String) aObject.getValue("CNTY_FIPS"));
    } else {
      throw new IllegalArgumentException("Unknown data type " + aObject.getDataType());
    }
  }

  /**
   * Provides a display name for the given domain object.
   */
  public static String getName(ILcdDataObject aObject) {
    if (aObject.getDataType().getName().contains("states")) {
      return aObject.getValue("STATE_NAME").toString();
    } else if (aObject.getDataType().getName().contains("counties")) {
      return aObject.getValue("NAME").toString();
    } else {
      throw new IllegalArgumentException("Unknown data type " + aObject.getDataType());
    }
  }
}

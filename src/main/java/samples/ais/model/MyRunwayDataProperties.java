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
package samples.ais.model;

import static samples.ais.model.CustomAISDataTypes.MyRunwayType;

import com.luciad.datamodel.TLcdDataProperty;

/**
 * This class provides easy access to the properties of the custom AIS Runway type.
 * @see CustomAISDataTypes#MyRunwayType
 */
public final class MyRunwayDataProperties {

  private MyRunwayDataProperties() {
    // Private constructor - class is only used to define constants.
    super();
  }

  /**
   * The width of a runway.
   * This property is of the type Double (<code>TLcdCoreDataTypes#DOUBLE_TYPE</code>).
   */
  public static final TLcdDataProperty WIDTH = MyRunwayType.getProperty( "Width" );

  /**
   * The surface type of a runway.
   * This property is of the type <code>TLcdAISDataTypes#RunwaySurfaceType</code>.
   */
  public static final TLcdDataProperty SURFACE_TYPE = MyRunwayType.getProperty( "SurfaceType" );
}

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
package samples.decoder.asterix.lightspeed.radarvideo.radar;

import com.luciad.shape.ILcdPoint;

/**
 * Class that holds the properties of a radar that is simulated.
 */
public class SimulatedRadarProperties {
  private final String fName;
  private final ILcdPoint fPosition;
  private final double fRange;
  private final double fCellRange;
  private final double fAngularResolution;
  private final double fSecondsPerRotation;

  public SimulatedRadarProperties(String aName, ILcdPoint aPosition, double aRange, double aCellRange, double aAngularResolution, double aSecondsPerRotation) {
    fName = aName;
    fPosition = aPosition;
    fRange = aRange;
    fAngularResolution = aAngularResolution;
    fCellRange = aCellRange;
    fSecondsPerRotation = aSecondsPerRotation;
  }

  public String getName() {
    return fName;
  }

  public ILcdPoint getPosition() {
    return fPosition;
  }

  public double getRange() {
    return fRange;
  }

  public double getCellRange() {
    return fCellRange;
  }

  public double getAngularResolution() {
    return fAngularResolution;
  }

  public double getSecondsPerRotation() {
    return fSecondsPerRotation;
  }
}

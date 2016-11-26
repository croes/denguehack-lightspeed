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
package samples.lightspeed.demo.application.data.weather;

import com.luciad.contour.TLcdIntervalContour;

class IcingSeverityContour extends IntervalContour {

  private final double fProbability;
  private final Severity fSeverity;

  public IcingSeverityContour(TLcdIntervalContour aContour, MultiDimensionalValue aAssociatedValue, double aProbability) {
    super(aContour, aAssociatedValue);
    fProbability = aProbability;
    double averageIntervalValue = (aContour.getInterval().getMin() + aContour.getInterval().getMax()) / 2;
    fSeverity = Severity.fromCode(WeatherUtil.round(averageIntervalValue, 0));
  }

  public double getProbability() {
    return fProbability;
  }

  public Severity getSeverity() {
    return fSeverity;
  }

  enum Severity {

    LIGHT(2, "Light"),
    MODERATE(3, "Moderate"),
    SEVERE(4, "Severe"),
    HEAVY(5, "Heavy");

    private final int fCode;
    private final String fMeaning;

    Severity(int aCode, String aMeaning) {
      fCode = aCode;
      fMeaning = aMeaning;
    }

    public int getCode() {
      return fCode;
    }

    public String getMeaning() {
      return fMeaning;
    }

    public boolean isEqualOrLessWorseThan(Severity aSeverity) {
      return getCode() <= aSeverity.getCode();
    }

    public static Severity fromCode(int code) {
      for (Severity severity : Severity.values()) {
        if (severity.getCode() == code) {
          return severity;
        }
      }

      throw new IllegalArgumentException("No Severity with code " + code);
    }

    public static int getLowestCode() {
      int result = Integer.MAX_VALUE ;
      for (Severity severity : values()) {
        int code = severity.getCode();
        if (code < result) {
          result = code;
        }
      }
      return result;
    }

    public static int getHighestCode() {
      int result = Integer.MIN_VALUE ;
      for (Severity severity : values()) {
        int code = severity.getCode();
        if (code > result) {
          result = code;
        }
      }
      return result;
    }

  }

}

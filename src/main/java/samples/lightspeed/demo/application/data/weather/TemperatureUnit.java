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

enum TemperatureUnit {

  CELSIUS("\u00b0C") {
    @Override
    double fromKelvin(double aKelvin) {
      return aKelvin - Constants.ZERO_DEGREES_CELSIUS_IN_KELVIN;
    }

    @Override
    double toKelvin(double aValue) {
      return aValue + Constants.ZERO_DEGREES_CELSIUS_IN_KELVIN;
    }
  },
  KELVIN("K") {
    @Override
    double fromKelvin(double aKelvin) {
      return aKelvin;
    }

    @Override
    double toKelvin(double aValue) {
      return aValue;
    }
  },
  FAHRENHEIT("\u00b0F") {
    @Override
    double fromKelvin(double aKelvin) {
      return aKelvin * 9 / 5 - 459.67;
    }

    @Override
    double toKelvin(double aValue) {
      return (aValue + 459.67) * 5 / 9;
    }
  };

  private final String fUnit;

  TemperatureUnit(String aUnit) {
    fUnit = aUnit;
  }

  abstract double fromKelvin(double aKelvin);
  abstract double toKelvin(double aValue);

  @Override
  public String toString() {
    return fUnit;
  }

  private static class Constants {
    private static final double ZERO_DEGREES_CELSIUS_IN_KELVIN = 273.15;
  }

}

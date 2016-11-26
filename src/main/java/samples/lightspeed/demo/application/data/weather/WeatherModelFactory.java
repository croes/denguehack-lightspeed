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

import java.io.IOException;

import com.luciad.format.netcdf.TLcdNetCDFFilteredModel;
import com.luciad.format.netcdf.TLcdNetCDFModelDecoder;
import com.luciad.model.ILcdModel;

import samples.lightspeed.demo.framework.data.AbstractModelFactory;

public class WeatherModelFactory extends AbstractModelFactory {

  private static final String TEMPERATURE_AT_ISOBARIC_LEVELS_DISPLAY_NAME = "Temperature at isobaric levels";
  private static final String WIND_AT_ISOBARIC_LEVELS_DISPLAY_NAME = "u,v-component of wind at isobaric levels";

  public WeatherModelFactory(String aType) {
    super(aType);
  }

  public ILcdModel createModel(String aSource) throws IOException {
    ILcdModel model = new TLcdNetCDFModelDecoder().decode(aSource);

    TLcdNetCDFFilteredModel temperatureModel =  WeatherUtil.getNetCDFFilteredModel(model, TEMPERATURE_AT_ISOBARIC_LEVELS_DISPLAY_NAME);
    TLcdNetCDFFilteredModel windModel =  WeatherUtil.getNetCDFFilteredModel(model, WIND_AT_ISOBARIC_LEVELS_DISPLAY_NAME);

    ContourModel contourModel = new ContourModel(temperatureModel);

    return new WeatherModel(temperatureModel, contourModel, windModel);
  }

}

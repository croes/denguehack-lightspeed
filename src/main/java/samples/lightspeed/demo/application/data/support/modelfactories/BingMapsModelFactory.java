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
package samples.lightspeed.demo.application.data.support.modelfactories;

import java.io.IOException;
import java.util.Properties;

import com.luciad.format.bingmaps.ELcdBingMapsMapStyle;
import com.luciad.format.bingmaps.ILcdBingMapsDataSource;
import com.luciad.format.bingmaps.TLcdBingMapsDataSourceBuilder;
import com.luciad.format.bingmaps.TLcdBingMapsModelDecoder;
import com.luciad.model.ILcdModel;

import samples.lightspeed.demo.framework.data.AbstractModelFactory;

/**
 * Model factory for Bing Maps.
 */
public class BingMapsModelFactory extends AbstractModelFactory {

  private String fBingKey;
  private ELcdBingMapsMapStyle fStyle = ELcdBingMapsMapStyle.AERIAL;

  public BingMapsModelFactory(String aType) {
    super(aType);
  }

  @Override
  public ILcdModel createModel(String aSource) throws IOException {
    if (fBingKey == null || fBingKey.length() == 0) {
      throw new IOException("Couldn't decode Bing Maps model because the application Id is missing.");
    }

    ILcdBingMapsDataSource source = new TLcdBingMapsDataSourceBuilder(fBingKey).mapStyle(fStyle).build();
    TLcdBingMapsModelDecoder decoder = new TLcdBingMapsModelDecoder();
    return decoder.decodeSource(source);
  }

  @Override
  public void configure(Properties aProperties) {
    String style = aProperties.getProperty("style");
    for (ELcdBingMapsMapStyle possibleStyle : ELcdBingMapsMapStyle.values()) {
      if (possibleStyle.toString().equalsIgnoreCase(style)) {
        fStyle = possibleStyle;
      }
    }
    fBingKey = aProperties.getProperty("applicationId");
  }
}

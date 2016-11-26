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
package samples.decoder.netcdf;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.luciad.format.netcdf.TLcdNetCDFDataSource;
import com.luciad.format.netcdf.TLcdNetCDFModelDecoder;
import com.luciad.format.netcdf.TLcdNetCDFModelDescriptor;
import com.luciad.format.netcdf.TLcdNetCDFMultiBandDataSource;
import com.luciad.model.ILcdModel;
import com.luciad.model.TLcdModelDescriptor;
import com.luciad.model.TLcdModelTreeNode;

public class NetCDFModelFactory {

  private String fCodeBase;

  public NetCDFModelFactory(String aCodeBase) {
    fCodeBase = aCodeBase;
  }

  public ILcdModel createMedModel() throws IOException {
    return new TLcdNetCDFModelDecoder().decode(fCodeBase + "Data/NetCDF/mediterranean.nc");
  }

  public ILcdModel createModel() throws IOException {
    TLcdNetCDFModelDecoder modelDecoder = new TLcdNetCDFModelDecoder();

    TLcdModelTreeNode modelTreeNode = new TLcdModelTreeNode();
    modelTreeNode.setModelDescriptor(new TLcdModelDescriptor("", TLcdNetCDFModelDescriptor.TYPE_NAME, "Weather"));

    // Visualized using an image

    ILcdModel temperatureModel = modelDecoder.decode(fCodeBase + "Data/NetCDF/temperature.nc");
    modelTreeNode.addModel(temperatureModel);
    modelTreeNode.addModel(modelDecoder.decode(fCodeBase + "Data/NetCDF/geopotential.nc"));
    modelTreeNode.addModel(modelDecoder.decode(fCodeBase + "Data/NetCDF/cloud_cover.nc"));

    return modelTreeNode;
  }

  public ILcdModel createTemperatureModel() throws IOException {
    TLcdNetCDFModelDecoder modelDecoder = new TLcdNetCDFModelDecoder();
    return modelDecoder.decode(fCodeBase + "Data/NetCDF/temperature.nc");
  }

  public ILcdModel createWindModel() throws IOException {
    TLcdNetCDFModelDecoder modelDecoder = new TLcdNetCDFModelDecoder();
    List<TLcdNetCDFDataSource> dataSourcesWindUV = new ArrayList<>();
    dataSourcesWindUV.addAll(modelDecoder.discoverDataSources(fCodeBase + "Data/NetCDF/wind_u.nc"));
    dataSourcesWindUV.addAll(modelDecoder.discoverDataSources(fCodeBase + "Data/NetCDF/wind_v.nc"));
    TLcdNetCDFMultiBandDataSource dataSourceWind = new TLcdNetCDFMultiBandDataSource(dataSourcesWindUV);
    return modelDecoder.decodeSource(dataSourceWind);
  }

}

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
package samples.lightspeed.demo.application.data.uav;

import java.io.IOException;
import java.util.Properties;

import com.luciad.model.ILcdModel;
import com.luciad.model.TLcdModelDescriptor;
import com.luciad.model.TLcdVectorModel;
import com.luciad.reference.TLcdGeocentricReference;
import com.luciad.shape.shape2D.TLcdLonLatBounds;
import com.luciad.util.logging.ILcdLogger;
import com.luciad.util.logging.TLcdLoggerFactory;

import samples.lightspeed.demo.framework.application.Framework;
import samples.lightspeed.demo.framework.data.AbstractModelFactory;
import samples.lightspeed.demo.framework.util.video.stream.GStreamerVideoStream;

/**
 * Model factory for the UAV region.
 */
public class UAVRegionModelFactory extends AbstractModelFactory {

  private static ILcdLogger sLogger = TLcdLoggerFactory.getLogger(UAVRegionModelFactory.class);

  private TLcdLonLatBounds fBounds;

  public UAVRegionModelFactory(String aType) {
    super(aType);
  }

  @Override
  public ILcdModel createModel(String aSource) throws IOException {
    GStreamerVideoStream videoStream = (GStreamerVideoStream) Framework.getInstance()
                                                                       .getSharedValue("videoStream");
    if (videoStream == null) {
      try {
        videoStream = new GStreamerVideoStream(aSource);
      } catch (Exception e) {
        throw new IOException(e.getMessage(), e);
      }
      Framework.getInstance().storeSharedValue("videoStream", videoStream);
    }

    TLcdVectorModel result = new TLcdVectorModel(
        new TLcdGeocentricReference(),
        new TLcdModelDescriptor(aSource, "UAVRegion", "UAVRegion")
    );

    result.addElement(fBounds, ILcdModel.NO_EVENT);
    return result;
  }

  @Override
  public void configure(Properties aProperties) {
    super.configure(aProperties);

    String lonlatBounds = aProperties.getProperty("uav.lonlatbounds", "-125 30 10 10");
    try {
      String[] values = lonlatBounds.split(" ");
      double lon = Double.parseDouble(values[0]);
      double lat = Double.parseDouble(values[1]);
      double width = Double.parseDouble(values[2]);
      double height = Double.parseDouble(values[3]);
      fBounds = new TLcdLonLatBounds(lon, lat, width, height);
    } catch (Exception e) {
      fBounds = new TLcdLonLatBounds(-125, 30, 10, 10);
    }
  }
}

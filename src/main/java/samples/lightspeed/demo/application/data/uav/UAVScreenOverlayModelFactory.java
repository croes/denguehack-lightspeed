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
import com.luciad.model.ILcdModelListener;
import com.luciad.model.TLcdModelChangedEvent;
import com.luciad.model.TLcdModelDescriptor;
import com.luciad.model.TLcdVectorModel;
import com.luciad.reference.TLcdGridReference;
import com.luciad.util.logging.ILcdLogger;
import com.luciad.util.logging.TLcdLoggerFactory;

import samples.lightspeed.demo.framework.application.Framework;
import samples.lightspeed.demo.framework.data.AbstractModelFactory;
import samples.lightspeed.demo.framework.data.IOUtil;
import samples.lightspeed.demo.framework.util.video.stream.GStreamerVideoStream;

/**
 * Model factory for the UAV video in screen space.
 */
public class UAVScreenOverlayModelFactory extends AbstractModelFactory {

  private static final ILcdLogger sLogger = TLcdLoggerFactory.getLogger(UAVScreenOverlayModelFactory.class);

  private Properties p;
  private String fSource;

  public UAVScreenOverlayModelFactory(String aType) {
    super(aType);
  }

  @Override
  public ILcdModel createModel(String aSource) {
    GStreamerVideoStream videoStream = (GStreamerVideoStream) Framework.getInstance()
                                                                       .getSharedValue("videoStream");
    if (videoStream == null) {
      throw new RuntimeException("Could not find a video stream");
    }

    TLcdVectorModel model = new TLcdVectorModel(
        new TLcdGridReference(),
        new TLcdModelDescriptor(aSource, "UAVScreenOverlay", "UAVScreenOverlay")
    );

    fSource = aSource;
    ScreenSpaceBounds screenSpaceBounds;
    try {
      p = new Properties();
      p.load(IOUtil.createReader(aSource));
      double x = Double.parseDouble(p.getProperty("x"));
      double y = Double.parseDouble(p.getProperty("y"));
      double width = Double.parseDouble(p.getProperty("w"));
      double height = Double.parseDouble(p.getProperty("h"));
      ScreenSpaceBounds.ScreenAnchor anchor = parseAnchor(p.getProperty("a", ""));
      screenSpaceBounds = new ScreenSpaceBounds(x, y, width, height, anchor);
    } catch (Exception e) {
      screenSpaceBounds = new ScreenSpaceBounds(0.01, 0.01, 100, 100, ScreenSpaceBounds.ScreenAnchor.TOP_LEFT);
      sLogger.warn("Error while loading " + aSource + " (" + e.getMessage() + ")");
    }

    model.addElement(screenSpaceBounds, ILcdModel.NO_EVENT);

    model.addModelListener(new ILcdModelListener() {
      @Override
      public void modelChanged(TLcdModelChangedEvent aModelChangedEvent) {
        if ((aModelChangedEvent.getCode() & TLcdModelChangedEvent.ALL_OBJECTS_CHANGED) != 0 ||
            (aModelChangedEvent.getCode() & TLcdModelChangedEvent.OBJECTS_CHANGED) != 0) {
          ScreenSpaceBounds bounds = (ScreenSpaceBounds) aModelChangedEvent.getModel().elements().nextElement();
          try {
            updateProperties(bounds);
          } catch (IOException ioe) {
            sLogger.warn("Error while updating " + fSource + " (" + ioe.getMessage() + ")");
          }
        }
      }
    });
    return model;
  }

  private void updateProperties(ScreenSpaceBounds aBounds) throws IOException {
    p.setProperty("x", String.valueOf(aBounds.getRelX()));
    p.setProperty("y", String.valueOf(aBounds.getRelY()));
    p.setProperty("w", String.valueOf(aBounds.getWidth()));
    p.setProperty("h", String.valueOf(aBounds.getHeight()));
    p.setProperty("a", formatAnchor(aBounds.getAnchor()));
    p.store(IOUtil.createWriter(fSource), "");
  }

  private ScreenSpaceBounds.ScreenAnchor parseAnchor(String aAnchor) {
    boolean right = aAnchor.toLowerCase().contains("right");
    boolean bottom = aAnchor.toLowerCase().contains("bottom");
    return ScreenSpaceBounds.ScreenAnchor.getAnchor(!right, !bottom);
  }

  private String formatAnchor(ScreenSpaceBounds.ScreenAnchor aAnchor) {
    return (aAnchor.getVerticalPosition() == 0 ? "top" : "bottom") +
           " " +
           (aAnchor.getHorizontalPosition() == 0 ? "left" : "right");
  }

//  @Override
//  public void configure( Properties aProperties ) {
//    super.configure( aProperties );
//     String xyBounds = aProperties.getProperty( "uav.xybounds", "10 10 300 300" );
//    try {
//      String[] values = xyBounds.split( " " );
//      double x = Double.parseDouble( values[ 0 ] );
//      double y = Double.parseDouble( values[ 1 ] );
//      double width = Double.parseDouble( values[ 2 ] );
//      double height = Double.parseDouble( values[ 3 ] );
//      fBounds = new TLcdXYBounds( x, y, width, height );
//    } catch ( Exception e ) {
//      fBounds = new TLcdXYBounds( -125, 30, 10, 10 );
//    }
//  }

}

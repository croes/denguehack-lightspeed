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
package samples.lightspeed.internal.havelsan.radar;

import com.luciad.model.ILcdModel;
import com.luciad.model.TLcdVectorModel;
import com.luciad.reference.TLcdGeocentricReference;
import com.luciad.shape.shape3D.TLcdXYZBounds;

/**
 * @author tomn
 * @since 2012.0
 */
public class RadarModelFactory {

  private RadarModelFactory() {
  }

  public static ILcdModel createRadarModel() throws Exception {
    TLcdVectorModel model = new TLcdVectorModel(
        new TLcdGeocentricReference(),
        new RadarModelDescriptor()
    );

    VideoStream stream = new VideoStream(
        "Data/internal/radarsweep/sweep_512.avi",
        new TLcdXYZBounds(-1e7, -1e7, -1e7, 2e7, 2e7, 2e7)
    );
    model.addElement(stream, ILcdModel.NO_EVENT);

    stream.play();

    return model;
  }
}

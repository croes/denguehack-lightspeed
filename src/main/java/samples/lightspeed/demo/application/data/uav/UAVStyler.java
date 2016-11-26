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
import java.util.Collection;

import com.luciad.format.object3d.obj.TLcdOBJMeshDecoder;
import com.luciad.format.object3d.openflight.TLcdOpenFlightFileDecoder;
import com.luciad.format.object3d.openflight.lightspeed.TLspOpenFlight3DIcon;
import com.luciad.format.object3d.openflight.model.TLcdOpenFlightHeaderNode;
import com.luciad.io.ILcdInputStreamFactory;
import com.luciad.io.TLcdInputStreamFactory;
import com.luciad.view.lightspeed.TLspContext;
import com.luciad.view.lightspeed.painter.mesh.ILsp3DIcon;
import com.luciad.view.lightspeed.painter.mesh.TLspMesh3DIcon;
import com.luciad.view.lightspeed.style.TLsp3DIconStyle;
import com.luciad.view.lightspeed.style.styler.ALspStyleCollector;
import com.luciad.view.lightspeed.style.styler.ALspStyler;

import samples.lightspeed.demo.framework.application.Framework;

/**
 * Styler that sets the same 3D icon style for each UAV object.
 */
class UAVStyler extends ALspStyler {

  private ILcdInputStreamFactory fInputStreamFactory;
  private TLsp3DIconStyle fIconStyle;

  /**
   * Creates a new UAV styler that sets a 3D icon with given
   * file name and given scale for each object.
   *
   * @param aFileName the file name of the 3D icon image that is to be set
   * @param aScale    the scale of the 3D
   */
  public UAVStyler(String aFileName, float aScale) {
    String iconFileName = Framework.getInstance().getDataPath(aFileName);
    fInputStreamFactory = new TLcdInputStreamFactory();

    fIconStyle = new TLsp3DIconStyle.Builder()
        .icon(loadIcon(iconFileName))
        .worldSize(aScale)
        .iconSizeMode(TLsp3DIconStyle.ScalingMode.WORLD_SCALING)
        .build();
  }

  @Override
  public void style(Collection<?> aObjects, ALspStyleCollector aStyleCollector, TLspContext aContext) {
    aStyleCollector.objects(aObjects).style(fIconStyle).submit();
  }

  private ILsp3DIcon loadIcon(String aIconSource) {
    try {
      String extension = aIconSource.substring(aIconSource.length() - 4);
      if (extension.equalsIgnoreCase(".obj")) {
        TLcdOBJMeshDecoder decoder = new TLcdOBJMeshDecoder();
        decoder.setInputStreamFactory(fInputStreamFactory);
        return new TLspMesh3DIcon(decoder.decodeMesh(aIconSource));
      } else if (extension.equalsIgnoreCase(".flt")) {
        TLcdOpenFlightFileDecoder decoder = new TLcdOpenFlightFileDecoder();
        decoder.setInputStreamFactory(fInputStreamFactory);
        TLcdOpenFlightHeaderNode scene = decoder.decode(aIconSource);
        return new TLspOpenFlight3DIcon(scene);
      } else {
        throw new IllegalArgumentException("Could not load icon, unsupported file format: " + extension);
      }
    } catch (IOException e) {
      throw new IllegalArgumentException("Could not load icon", e);
    }
  }

}

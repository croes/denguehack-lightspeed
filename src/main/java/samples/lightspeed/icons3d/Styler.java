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
package samples.lightspeed.icons3d;

import com.luciad.util.logging.ILcdLogger;
import com.luciad.util.logging.TLcdLoggerFactory;
import com.luciad.view.lightspeed.style.TLsp3DIconStyle;
import com.luciad.view.lightspeed.style.TLspVerticalLineStyle;
import com.luciad.view.lightspeed.style.styler.TLspStyler;

/**
 * Styler for the 3D icon painter used in the sample.
 */
class Styler extends TLspStyler {

  private static ILcdLogger sLogger = TLcdLoggerFactory.getLogger(Styler.class);

  private double fWorldMargin;

  public Styler() {
    setIcon("Data/3d_icons/plane.dae");
  }

  public void setIcon(String aFileName) {
    try {
      TLsp3DIconStyle iconStyle = TLsp3DIconStyle.newBuilder()
                                                 .icon(aFileName)
                                                 .worldSize(100000)
                                                 .verticalOffsetFactor(1.0)
                                                 .iconSizeMode(TLsp3DIconStyle.ScalingMode.WORLD_SCALING)
                                                 .build();

      super.setStyles(iconStyle, TLspVerticalLineStyle.newBuilder().build());
      fWorldMargin = iconStyle.getWorldSize();
    } catch (IllegalArgumentException e) {
      sLogger.warn("Could not load icon " + aFileName + ": " + e.getMessage());
      return;
    }
  }

  public double getWorldMargin() {
    return fWorldMargin;
  }
}

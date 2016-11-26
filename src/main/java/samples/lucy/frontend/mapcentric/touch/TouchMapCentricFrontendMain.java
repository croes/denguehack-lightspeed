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
package samples.lucy.frontend.mapcentric.touch;

import java.util.concurrent.Callable;

import com.luciad.gui.TLcdAWTUtil;
import com.luciad.gui.TLcdIconFactory;
import samples.lucy.frontend.mapcentric.MapCentricFrontendMain;

/**
 * Alternative version of the {@link samples.lucy.frontend.mapcentric.MapCentricFrontendMain MapCentricFrontendMain} sample
 * which uses a workspace with a Lightspeed touch map instead of a regular Lightspeed map.
 *
 * @since 2012.0
 */
public class TouchMapCentricFrontendMain {

  public static void main(final String[] aArgs) {
    MapCentricFrontendMain frontEnd = TLcdAWTUtil.invokeAndWait(new Callable<MapCentricFrontendMain>() {
      @Override
      public MapCentricFrontendMain call() throws Exception {
        MapCentricFrontendMain frontEnd = new MapCentricFrontendMain(
            aArgs,
            "samples/frontend/mapcentric/touch/touch_map_centric_addons.xml",
            "samples/frontend/mapcentric/map_centric_frontend.cfg");

        //Use large-sized icons for touch
        TLcdIconFactory.setDefaultSize(TLcdIconFactory.Size.SIZE_32);

        return frontEnd;
      }
    });
    frontEnd.startup();
  }

}

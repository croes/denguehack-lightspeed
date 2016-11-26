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
package samples.lucy.lightspeed.style.newiconstyle;

import java.io.IOException;

import com.luciad.lucy.ILcyLucyEnv;
import com.luciad.lucy.ILcyLucyEnvListener;
import com.luciad.lucy.TLcyLucyEnvEvent;
import com.luciad.lucy.addons.lightspeed.ALcyLspFormatAddOn;
import com.luciad.lucy.format.lightspeed.ALcyLspFormat;
import com.luciad.lucy.format.lightspeed.TLcyLspSafeGuardFormatWrapper;
import com.luciad.lucy.map.ILcyGenericMapComponent;
import com.luciad.lucy.util.ALcyTool;
import com.luciad.lucy.util.TLcyVetoException;
import com.luciad.util.TLcdNoBoundsException;
import com.luciad.util.TLcdOutOfBoundsException;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.layer.ILspLayer;

import samples.lucy.lightspeed.common.MapComponentUtil;

/**
 * <p>Add-on which provides support for visualizing SHP point data in a Lightspeed view.</p>
 *
 * <p>It uses a {@link com.luciad.lucy.format.lightspeed.ALcyLspStyleFormat ALcyLspStyleFormat} to visualize SHP data, and makes sure the
 * <code>TLspIconStyle</code> uses a <code>TLcdImageIcon</code>. Furthermore, it replaces the layer
 * customizer panel with a custom implementation which allows to adjust the image in the
 * <code>TLcdImageIcon</code> instance.</p>
 */
public class SHPFormatAddOn extends ALcyLspFormatAddOn {
  /**
   * Create a new add-on
   */
  public SHPFormatAddOn() {
    super(ALcyTool.getLongPrefixWithClassName(SHPFormatAddOn.class),
          ALcyTool.getShortPrefix(SHPFormatAddOn.class));
  }

  @Override
  public void plugInto(ILcyLucyEnv aLucyEnv) {
    super.plugInto(aLucyEnv);

    //make sure a Lightspeed map is created on start-up and a city_125.shp layer is loaded
    aLucyEnv.addLucyEnvListener(new ILcyLucyEnvListener() {
      @Override
      public void lucyEnvStatusChanged(TLcyLucyEnvEvent aEvent) throws TLcyVetoException {
        if (aEvent.getID() == TLcyLucyEnvEvent.INITIALIZED) {
          try {
            ILcyLucyEnv lucyEnv = aEvent.getLucyEnv();
            lucyEnv.removeLucyEnvListener(this);

            //make sure a Lightspeed map is active
            ILcyGenericMapComponent<ILspView, ILspLayer> mapComponent = MapComponentUtil.activateLightspeedMap(lucyEnv);
            //add the cities data source to the view and fit on the created layer
            MapComponentUtil.addDataSourceAndFit(mapComponent, MapComponentUtil.CITIES_MODEL_SOURCE_NAME, lucyEnv, true);
          } catch (IOException e) {
            throw new RuntimeException(e);
          } catch (TLcdNoBoundsException e) {
            throw new RuntimeException(e);
          } catch (TLcdOutOfBoundsException e) {
            throw new RuntimeException(e);
          }
        }
      }
    });
  }

  @Override
  protected ALcyLspFormat createBaseFormat() {
    return new SHPFormat(getLongPrefix(), getShortPrefix(), getPreferences(), getLucyEnv());
  }

  @Override
  protected final ALcyLspFormat createFormatWrapper(ALcyLspFormat aBaseFormat) {
    return new TLcyLspSafeGuardFormatWrapper(aBaseFormat);
  }
}

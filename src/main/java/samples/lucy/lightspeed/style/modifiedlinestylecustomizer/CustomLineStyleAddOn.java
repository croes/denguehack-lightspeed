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
package samples.lucy.lightspeed.style.modifiedlinestylecustomizer;

import java.io.IOException;

import com.luciad.lucy.ILcyLucyEnv;
import com.luciad.lucy.ILcyLucyEnvListener;
import com.luciad.lucy.TLcyLucyEnvEvent;
import com.luciad.lucy.addons.ALcyAddOn;
import com.luciad.lucy.map.ILcyGenericMapComponent;
import com.luciad.lucy.util.TLcyVetoException;
import com.luciad.util.TLcdNoBoundsException;
import com.luciad.util.TLcdOutOfBoundsException;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.layer.ILspLayer;

import samples.lucy.lightspeed.common.MapComponentUtil;

/**
 * <p>Add-on which plugs in an <code>ILcyCustomizerPanelFactory</code> which provides
 * a customizer panel for line styles.</p>
 *
 * <p>The customizer panel factory is registered as a service on the Lucy back-end, which will make
 * sure the {@link com.luciad.lucy.addons.layercustomizer.lightspeed.TLcyLspLayerCustomizerAddOn} picks
 * it up and uses it when creating a customizer panel for a <code>TLcyLayerContext</code>.</p>
 *
 * <p>Furthermore, this add-on makes sure a Lightspeed map is created during the initialization of Lucy,
 * and the "rivers" layer is added to that map.</p>
 */
public class CustomLineStyleAddOn extends ALcyAddOn {

  @Override
  public void plugInto(ILcyLucyEnv aLucyEnv) {
    aLucyEnv.addService(new CustomizableLineStyleCustomizerPanelFactory());
    aLucyEnv.addLucyEnvListener(new ILcyLucyEnvListener() {
      @Override
      public void lucyEnvStatusChanged(TLcyLucyEnvEvent aEvent) throws TLcyVetoException {
        if (aEvent.getID() == TLcyLucyEnvEvent.INITIALIZED) {
          try {
            ILcyLucyEnv lucyEnv = aEvent.getLucyEnv();
            lucyEnv.removeLucyEnvListener(this);

            //make sure a Lightspeed map is active
            ILcyGenericMapComponent<ILspView, ILspLayer> mapComponent = MapComponentUtil.activateLightspeedMap(lucyEnv);
            //add the rivers data source to the view and fit on the created layer
            MapComponentUtil.addDataSourceAndFit(mapComponent, MapComponentUtil.RIVERS_MODEL_SOURCE_NAME, lucyEnv, true);
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
  public void unplugFrom(ILcyLucyEnv aLucyEnv) {
    //do nothing
  }
}

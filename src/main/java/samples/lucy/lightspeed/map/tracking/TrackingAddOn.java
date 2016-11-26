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
package samples.lucy.lightspeed.map.tracking;

import com.luciad.lucy.ILcyLucyEnv;
import com.luciad.lucy.ILcyLucyEnvListener;
import com.luciad.lucy.TLcyLucyEnvEvent;
import com.luciad.lucy.addons.cameralinking.lightspeed.TLcyLspCameraLinkAddOn;
import com.luciad.lucy.addons.previewer.TLcyPreviewAddOn;
import com.luciad.lucy.map.cameralinking.TLcyCompositeCameraLinkerFactory;
import com.luciad.lucy.util.TLcyVetoException;

/**
 * <p>Addon for the tracking sample which plugs in an additional custom linker factory.</p>
 *
 * <p>For more information on how to plug-in your custom tracking camera, see the
 * {@link TLcyLspCameraLinkAddOn TLcyLspCameraLinkAddOn}'s documentation.</p>
 *
 */
public class TrackingAddOn extends TLcyLspCameraLinkAddOn {

  private FreeViewLinkerFactory fLinkerFactory;

  @Override
  public void plugInto(final ILcyLucyEnv aLucyEnv) {
    super.plugInto(aLucyEnv);
    //add a custom camera linker factory
    fLinkerFactory = new FreeViewLinkerFactory();
    new TLcyCompositeCameraLinkerFactory(aLucyEnv).add(fLinkerFactory);
    //make sure the plane starts moving when Lucy is fully initialized
    aLucyEnv.addLucyEnvListener(new ILcyLucyEnvListener() {

      @Override
      public void lucyEnvStatusChanged(TLcyLucyEnvEvent aEvent) throws TLcyVetoException {
        if (TLcyLucyEnvEvent.INITIALIZED == aEvent.getID()) {
          TLcyPreviewAddOn previewAddOn = aLucyEnv.retrieveAddOnByClass(TLcyPreviewAddOn.class);
          if (previewAddOn == null) {
            throw new NullPointerException("The TLcyPreviewAddOn is not loaded. Check the log files to see what went wrong.");
          }
          previewAddOn.run();
        }
      }

    });
  }

  @Override
  public void unplugFrom(ILcyLucyEnv aLucyEnv) {
    super.unplugFrom(aLucyEnv);
    new TLcyCompositeCameraLinkerFactory(aLucyEnv).remove(fLinkerFactory);
  }

}

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
package samples.lucy.lightspeed.map.guifactory;

import com.luciad.lucy.TLcyMain;

/**
 * <p>This sample demonstrates how to adapt the gui factory of the <code>TLcyLspMapAddOn</code>.</p>
 *
 * <p>The {@link LspMapAddOn} is an extension of the {@link com.luciad.lucy.addons.lspmap.TLcyLspMapAddOn
 * TLcyLspMapAddOn} which overrides the <code>createGUIFactory</code> method to create a
 * <code>LspMapComponentFactory</code> instead of the default factory. By using this factory, the
 * GUI of every created map component can be modified. In particular, the ruler controller has been
 * removed, the toolbar is put below the map, an (interactive) overlay panel is added to the top
 * left and an extra layer is added to the map.</p>
 */
public class LspGUIFactorySampleMain {
  public static void main(String[] args) {
    TLcyMain.main(args, "-addons", "samples/lightspeed/map/guifactory/addons_lightspeed_map_guifactory_sample.xml");
  }
}

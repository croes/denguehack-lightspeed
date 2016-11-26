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
package samples.lucy.gxy.custommapui;

import com.luciad.lucy.TLcyMain;

/**
 * This sample demonstrates how to adapt the gui factory of the
 * <code>TLcyMapAddOn</code>. The same principle can be applied to the
 * <code>TLcyVerticalViewAddOn</code> and the <code>TLcyPreviewAddOn</code>. To
 * modify the gui factory, another configuration file
 * (lucy_guifactory_sample_map_addon.cfg) for the <code>TLcyMapAddon</code> is
 * specified in the addons_guifactory_sample.xml file. This configuration file
 * specifies that the <code>MapComponentFactory</code> should be used
 * instead of the default factory. By using this factory, the GUI of every
 * created <code>ILcyMapComponent</code>s can be modified. In particular, the
 * icon of the File|Open action has been modified to a globe, the ruler
 * controller has been removed and the toolbar is put below the map.  The scale
 * combo box is no longer inside the toolbar, but at the far right side.
 *
 */
public class CustomMapUISampleMain {
  public static void main(String[] aArgs) {
    TLcyMain.main(aArgs, "-addons", "samples/gxy/custommapui/addons_custommapui_sample.xml");
  }
}

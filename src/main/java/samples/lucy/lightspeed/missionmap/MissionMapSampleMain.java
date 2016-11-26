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
package samples.lucy.lightspeed.missionmap;

import com.luciad.lucy.TLcyMain;

/**
 * <p>This sample demonstrates how multiple {@linkplain com.luciad.lucy.map.lightspeed.ILcyLspMapComponent#getType()
 * types} of map components can be created. The sample starts with a "Mission Preparation" map, and
 * a "Mission Preview" map can be created through the UI. The "Mission Preview" map is kept in sync
 * with the preparation map, but contains a simplified UI as it would be displayed during the
 * mission.</p>
 *
 * <p>In order to achieve this the <code>TLcyLspMapAddOn</code> uses a custom configuration file,
 * specifying two types of map components. An extra add-on (<code>MissionMapAddOn</code>) is used to
 * keep the preparation and preview maps in sync.</p>
 */
public class MissionMapSampleMain {
  public static void main(String[] args) {
    TLcyMain.main(args, "-addons", "samples/lightspeed/missionmap/addons_lightspeed_missionmap_sample.xml");
  }
}

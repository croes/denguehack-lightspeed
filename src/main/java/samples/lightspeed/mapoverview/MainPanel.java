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
package samples.lightspeed.mapoverview;

import javax.swing.JComponent;

import com.luciad.gui.swing.TLcdOverlayLayout;

import samples.lightspeed.common.LightspeedSample;

/**
 * This sample shows how a map overview can be implemented for an ILspView.
 * The map overview is overlaid on the main view. It shows a polygon which
 * bounds the area of the world that is visible in the main view.
 */
public class MainPanel extends LightspeedSample {

  private MapOverview fMapOverview;

  public static void main(String[] args) {
    startSample(MainPanel.class, "Map Overview");
  }

  @Override
  protected void addOverlayComponents(JComponent aOverlayPanel) {
    super.addOverlayComponents(aOverlayPanel);

    // Create the map overview and add it to the overlay panel.
    fMapOverview = new MapOverview(getView());
    aOverlayPanel.add(fMapOverview, TLcdOverlayLayout.Location.NORTH_WEST);
  }

  @Override
  protected void tearDown() {
    if (fMapOverview != null) {
      fMapOverview.tearDown();
      fMapOverview = null;
    }
    super.tearDown();
  }
}

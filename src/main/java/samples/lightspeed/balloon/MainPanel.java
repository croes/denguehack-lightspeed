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
package samples.lightspeed.balloon;

import java.io.IOException;

import com.luciad.gui.swing.TLcdOverlayLayout;
import samples.common.BalloonViewSelectionListener;
import com.luciad.view.lightspeed.swing.TLspBalloonManager;

import samples.lightspeed.common.LightspeedSample;
import samples.lightspeed.common.LspDataUtil;

/**
 * This sample illustrates how to configure balloons, provide content to them and
 * link them to domain objects.
 */
public class MainPanel extends LightspeedSample {

  private TLspBalloonManager fBalloonManager;

  @Override
  protected void createGUI() {
    super.createGUI();
    fBalloonManager = new TLspBalloonManager(getView(),
                                             getOverlayPanel(),
                                             TLcdOverlayLayout.Location.NO_LAYOUT,
                                             new BalloonContentProvider());
  }

  protected void addData() throws IOException {
    super.addData();

    LspDataUtil.instance().model(ModelFactory.createBalloonModel()).layer().addToView(getView()).fit();

    BalloonViewSelectionListener listener = new BalloonViewSelectionListener(getView(), fBalloonManager);
    getView().addLayeredListener(listener);
    getView().addLayerSelectionListener(listener);
    getView().getRootNode().addHierarchyPropertyChangeListener(listener);
  }

  public static void main(final String[] aArgs) {
    startSample(MainPanel.class, "Balloon");
  }

}

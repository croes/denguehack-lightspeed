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
package samples.lightspeed.demo.application.data.imageprocessing;

import java.util.Enumeration;

import com.luciad.format.gml32.model.TLcdGML32AbstractFeature;
import com.luciad.util.ILcdSelectionListener;
import com.luciad.util.TLcdSelectionChangedEvent;
import com.luciad.view.lightspeed.ILspAWTView;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.swing.TLspBalloonManager;
import com.luciad.view.swing.TLcdModelElementBalloonDescriptor;

/**
 * Class to manage the POI balloons.
 */
class InstructionPanel {

  private TLspBalloonManager fBalloonManager;
  private ILspLayer fLayer;

  public InstructionPanel(ILspView aView, ILspLayer aLayer) {
    fLayer = aLayer;
    fBalloonManager = new TLspBalloonManager((ILspAWTView) aView, new InstructionBalloonContentProvider());
    fLayer.addSelectionListener(new POISelectionListener());
  }

  public void activate() {
    fBalloonManager.setBalloonsEnabled(true);
  }

  public void deactivate() {
    fBalloonManager.setBalloonsEnabled(false);
  }

  private class POISelectionListener implements ILcdSelectionListener {

    @Override
    public void selectionChanged(TLcdSelectionChangedEvent aTLcdSelectionChangedEvent) {
      Enumeration selectedElements = aTLcdSelectionChangedEvent.selectedElements();
      TLcdGML32AbstractFeature selectedPOI = null;
      while (selectedElements.hasMoreElements()) {
        Object nextElement = selectedElements.nextElement();
        if (nextElement instanceof TLcdGML32AbstractFeature) {
          selectedPOI = (TLcdGML32AbstractFeature) nextElement;
        }
      }
      if (selectedPOI != null) {
        fBalloonManager.setBalloonDescriptor(new TLcdModelElementBalloonDescriptor(selectedPOI, fLayer.getModel(), fLayer));
      }
    }
  }
}

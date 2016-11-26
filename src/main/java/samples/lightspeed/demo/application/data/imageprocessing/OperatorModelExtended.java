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

import java.beans.PropertyChangeEvent;

import com.luciad.imaging.ALcdImage;
import com.luciad.model.ILcdModel;
import com.luciad.view.lightspeed.services.asynchronous.ILspTaskExecutor;

import samples.lightspeed.imaging.multispectral.OperatorModel;

/**
 * Extended operator model used to also compute the normalized difference vegetation index.
 */
class OperatorModelExtended extends OperatorModel {

  public static final String NORMALIZEDVI_CHANGE_EVENT = "normalized";

  private boolean fNormalizedDifference;

  public OperatorModelExtended(ILcdModel aModel, ALcdImage aImage, ILspTaskExecutor aTaskExecutor) {
    super(aModel, aImage, aTaskExecutor, true);
  }

  public OperatorModelExtended(ILcdModel aModel, ALcdImage aImage, ILspTaskExecutor aTaskExecutor, boolean aNormalize) {
    super(aModel, aImage, aTaskExecutor, aNormalize);
    fNormalizedDifference = false;
  }

  //create labels specific for landsat bands
  @Override
  protected String[] createBandNames(int aNbBands) {
    return new String[]{
        "Band 1: Blue",
        "Band 2: Green",
        "Band 3: Red",
        "Band 4: Near Infrared",
        "Band 5: Shortwave Infrared",
        "Band 6: Thermal Infrared",
        "Band 7: Shortwave Infrared"
    };
  }

  public boolean isNormalizedDifference() {
    return fNormalizedDifference;
  }

  public void setNormalizedDifference(boolean aNormalizedDifference) {
    fNormalizedDifference = aNormalizedDifference;
    if (fNormalizedDifference) {
      setSelectedBands(new int[]{0});
    }
    fireChangeEvent(new PropertyChangeEvent(this, NORMALIZEDVI_CHANGE_EVENT, null, aNormalizedDifference));
  }
}

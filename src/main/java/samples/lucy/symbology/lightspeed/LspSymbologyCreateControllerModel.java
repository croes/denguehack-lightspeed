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
package samples.lucy.symbology.lightspeed;

import samples.lucy.formatbar.ObservableLspCreateControllerModel;
import samples.lucy.symbology.common.SymbologyStatusMessageUtil;
import samples.symbology.common.EMilitarySymbology;
import samples.symbology.common.util.MilitarySymbolFacade;
import com.luciad.lucy.ILcyLucyEnv;
import com.luciad.lucy.map.action.lightspeed.TLcyLspCreateLayerAction;
import com.luciad.lucy.map.lightspeed.ILcyLspMapComponent;
import com.luciad.util.ILcdCloneable;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.editor.TLspEditContext;
import com.luciad.view.lightspeed.layer.ILspLayer;

/**
 * Lightspeed create controller model for a certain symbology.
 */
class LspSymbologyCreateControllerModel extends ObservableLspCreateControllerModel implements ILcdCloneable {

  /**
   * Determines which symbol should be created.
   */
  public interface CodedSelector {

    Object selectCoded(ILspView aView, ILspLayer aLayer);

  }

  private final EMilitarySymbology fSymbology;
  private final ILcyLucyEnv fLucyEnv;
  private CodedSelector fCodedSelector;

  public LspSymbologyCreateControllerModel(TLcyLspCreateLayerAction aCreateLayerAction,
                                           ILcyLspMapComponent aMapComponent,
                                           EMilitarySymbology aSymbology,
                                           ILcyLucyEnv aLucyEnv) {
    super(aMapComponent, aCreateLayerAction);
    fSymbology = aSymbology;
    fLucyEnv = aLucyEnv;
  }

  @Override
  public Object create(ILspView aView, ILspLayer aLayer) {
    Object result;
    if (fCodedSelector != null) {
      result = fCodedSelector.selectCoded(aView, aLayer);
    } else {
      result = MilitarySymbolFacade.newElement(fSymbology, false);
    }

    if (result == null) {
      return null; // bail-out
    }

    SymbologyStatusMessageUtil.showStatusMessage(result, fLucyEnv, getMapComponent());
    return result;
  }

  public void setCodedSelector(CodedSelector aCodedSelector) {
    fCodedSelector = aCodedSelector;
  }

  @Override
  public LspSymbologyCreateControllerModel clone() {
    return (LspSymbologyCreateControllerModel) super.clone();
  }

  @Override
  public void canceled(TLspEditContext aEditContext) {
    super.canceled(aEditContext);
    SymbologyStatusMessageUtil.removeStatusMessage(fLucyEnv, getMapComponent());
  }

  @Override
  public void finished(TLspEditContext aEditContext) {
    super.finished(aEditContext);
    SymbologyStatusMessageUtil.removeStatusMessage(fLucyEnv, getMapComponent());
  }
}

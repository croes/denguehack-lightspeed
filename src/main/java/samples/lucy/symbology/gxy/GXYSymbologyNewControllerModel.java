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
package samples.lucy.symbology.gxy;

import java.awt.Graphics;
import java.awt.event.MouseEvent;

import samples.lucy.formatbar.ObservableGXYNewControllerModel;
import samples.lucy.symbology.common.SymbologyStatusMessageUtil;
import samples.symbology.common.EMilitarySymbology;
import samples.symbology.common.util.MilitarySymbolFacade;
import com.luciad.lucy.ILcyLucyEnv;
import com.luciad.lucy.map.ILcyMapComponent;
import com.luciad.lucy.map.action.TLcyCreateGXYLayerAction;
import com.luciad.util.ILcdCloneable;
import com.luciad.view.gxy.ILcdGXYContext;
import com.luciad.view.gxy.ILcdGXYLayerSubsetList;

/**
 * {@link ObservableGXYNewControllerModel} that creates domain objects for a certain symbology.
 */
class GXYSymbologyNewControllerModel extends ObservableGXYNewControllerModel implements ILcdCloneable {

  /**
   * Determines which symbol should be created.
   */
  public interface CodedSelector {

    Object selectCoded(ILcdGXYContext aContext);

  }

  private final EMilitarySymbology fSymbology;
  private final ILcyLucyEnv fLucyEnv;
  private CodedSelector fCodedSelector;

  public GXYSymbologyNewControllerModel(TLcyCreateGXYLayerAction aCreateLayerAction,
                                        ILcyMapComponent aMapComponent, EMilitarySymbology aSymbology, ILcyLucyEnv aLucyEnv) {
    super(aCreateLayerAction, aMapComponent);
    fSymbology = aSymbology;
    fLucyEnv = aLucyEnv;
  }

  @Override
  public GXYSymbologyNewControllerModel clone() {
    return (GXYSymbologyNewControllerModel) super.clone();
  }

  public void setCodedSelector(CodedSelector aCodedSelector) {
    fCodedSelector = aCodedSelector;
  }

  @Override
  public Object create(int aEditCount, Graphics aGraphics, MouseEvent aMouseEvent, ILcdGXYLayerSubsetList aSnappables, ILcdGXYContext aContext) {
    Object result;
    if (fCodedSelector != null) {
      result = fCodedSelector.selectCoded(aContext);
    } else {
      result = MilitarySymbolFacade.newElement(fSymbology, true);
    }
    if (result == null) {
      return null; // bail-out
    }
    SymbologyStatusMessageUtil.showStatusMessage(result, fLucyEnv, getMapComponent());
    return result;
  }

  @Override
  public void cancel(Object aObject, int aEditCount, Graphics aGraphics, MouseEvent aMouseEvent, ILcdGXYLayerSubsetList aSnappables, ILcdGXYContext aContext) {
    super.cancel(aObject, aEditCount, aGraphics, aMouseEvent, aSnappables, aContext);
    SymbologyStatusMessageUtil.removeStatusMessage(fLucyEnv, getMapComponent());
  }

  @Override
  public void commit(Object aObject, int aEditCount, Graphics aGraphics, MouseEvent aMouseEvent, ILcdGXYLayerSubsetList aSnappables, ILcdGXYContext aContext) {
    super.commit(aObject, aEditCount, aGraphics, aMouseEvent, aSnappables, aContext);
    SymbologyStatusMessageUtil.removeStatusMessage(fLucyEnv, getMapComponent());
  }

  @Override
  public CreationStatus getCreationStatus(Object aObject, int aEditCount, Graphics aGraphics, MouseEvent aMouseEvent, ILcdGXYLayerSubsetList aSnappables, ILcdGXYContext aContext) {
    int pointCount = MilitarySymbolFacade.getPointCount(aObject);
    int minPointCount = MilitarySymbolFacade.getRequiredNumberOfClicks(aObject);
    if (pointCount < Math.abs(minPointCount)) {
      return CreationStatus.UNCOMMITTABLE;
    }

    if (minPointCount <= 0) {
      return CreationStatus.COMMITTABLE;
    }

    return CreationStatus.FINISHED; // The exact number of points is reached
  }
}

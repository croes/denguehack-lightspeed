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
package samples.lightspeed.imaging.multispectral;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import com.luciad.imaging.ALcdImage;
import com.luciad.imaging.operator.ALcdImageOperatorChain;
import com.luciad.view.ILcdLayer;
import com.luciad.view.lightspeed.layer.ILspEditableStyledLayer;
import com.luciad.view.lightspeed.layer.TLspPaintRepresentationState;
import com.luciad.view.lightspeed.style.styler.ILspStyler;

/**
 * Listener that will set the new operators if it receives an event from the operator model.
 */
class OperatorModelChangeListener implements PropertyChangeListener {

  private final ILcdLayer fLayer;

  public OperatorModelChangeListener(ILcdLayer aLayer) {
    fLayer = aLayer;
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
    if (evt.getSource() instanceof OperatorModel) {
      if (!evt.getPropertyName().equals(OperatorModel.STYLER_PROPERTY_CHANGE_EVENT)) {
        OperatorModel operatorModel = (OperatorModel) evt.getSource();
        List<ALcdImageOperatorChain> chains = operatorModel.getImageOperators();
        setImageOperatorChains(fLayer, chains.toArray(new ALcdImageOperatorChain[chains.size()]));
      } else {
        invalidateStyle(fLayer);
      }
    }
  }

  /**
   * Appends all chains and sets the resulting chain on the current styler of the layer
   *
   * @param aLayer the layer on which the chains are to be applied
   * @param aImageOperatorChains the chains that need to be applied
   */
  private void setImageOperatorChains(ILcdLayer aLayer, ALcdImageOperatorChain... aImageOperatorChains) {
    if (aLayer instanceof ILspEditableStyledLayer) {
      ILspEditableStyledLayer layer = (ILspEditableStyledLayer) aLayer;
      ILspStyler styler = layer.getStyler(TLspPaintRepresentationState.REGULAR_BODY);
      // If we can find an ImageOperatorStyler, replace the chain
      if (styler instanceof MultispectralOperatorStyler) {
        ((MultispectralOperatorStyler) styler).setImageOperatorChain(chain(aImageOperatorChains));
        layer.invalidate();
      }
    }
  }

  /**
   * Creates an operator chain that appends a number of other chains, i.e., applies the operators from first to last.
   *
   * @param aOperatorChains the chains to be appended
   *
   * @return a chain which combines the given chains
   */
  private ALcdImageOperatorChain chain(ALcdImageOperatorChain... aOperatorChains) {
    final ALcdImageOperatorChain[] operatorChains = aOperatorChains;
    return new ALcdImageOperatorChain() {
      @Override
      public ALcdImage apply(ALcdImage aInput) {
        for (ALcdImageOperatorChain operatorChain : operatorChains) {
          aInput = operatorChain.apply(aInput);
        }
        return aInput;
      }
    };
  }

  private void invalidateStyle(ILcdLayer aLayer) {
    if (aLayer instanceof ILspEditableStyledLayer) {
      ILspEditableStyledLayer layer = (ILspEditableStyledLayer) aLayer;
      ILspStyler styler = layer.getStyler(TLspPaintRepresentationState.REGULAR_BODY);
      // If we can find an ImageOperatorStyler, replace the chain
      if (styler instanceof MultispectralOperatorStyler) {
        ((MultispectralOperatorStyler) styler).fireStyleChangeEvent();
        layer.invalidate();
      }
    }
  }
}

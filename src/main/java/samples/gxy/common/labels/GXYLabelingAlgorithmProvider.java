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
package samples.gxy.common.labels;

import com.luciad.view.TLcdLabelIdentifier;
import com.luciad.view.gxy.labeling.algorithm.ILcdGXYLabelLabelingAlgorithmProvider;
import com.luciad.view.gxy.labeling.algorithm.ILcdGXYLabelingAlgorithm;

import samples.common.serviceregistry.ServiceRegistry;

/**
 * This ILcdGXYLabelLabelingAlgorithmProvider implementation uses the ServiceRegistry mechanism to retrieve delegate
 * labeling algorithm providers. This allows code to plug-in its own ILcdGXYLabelLabelingAlgorithmProvider instance
 * that is picked up by this class.
 *
 * Delegate ILcdGXYLabelLabelingAlgorithmProvider instances should make sure that they return a {@code null}
 * labeling algorithm when they can/should not handle the given label.
 */
public class GXYLabelingAlgorithmProvider implements ILcdGXYLabelLabelingAlgorithmProvider<ILcdGXYLabelingAlgorithm> {

  private final Iterable<ILcdGXYLabelLabelingAlgorithmProvider> fDelegates;

  public GXYLabelingAlgorithmProvider() {
    fDelegates = ServiceRegistry.getInstance().query(ILcdGXYLabelLabelingAlgorithmProvider.class);
  }

  @Override
  public ILcdGXYLabelingAlgorithm getLabelingAlgorithm(TLcdLabelIdentifier aLabel) {
    for (ILcdGXYLabelLabelingAlgorithmProvider delegate : fDelegates) {
      ILcdGXYLabelingAlgorithm labelingAlgorithm = delegate.getLabelingAlgorithm(aLabel);
      if (labelingAlgorithm != null) {
        return labelingAlgorithm;
      }
    }
    return null;
  }
}

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
package samples.gxy.labels.offset;

import java.util.List;

import com.luciad.view.labeling.algorithm.ILcdCollectedLabelInfoDependencyProvider;
import com.luciad.view.labeling.algorithm.TLcdCollectedLabelInfo;
import com.luciad.view.labeling.algorithm.TLcdCollectedLabelInfoList;
import com.luciad.view.labeling.algorithm.TLcdCollectedLabeledObjectInfo;

/**
 * This label dependency provider models the dependency where sublabels depend on each other.
 * I.e. all labels with the same layer, domain object and label index depend on each other.
 * <p>
 * This dependency provider can for example be used to specify the behavior that sublabels
 * are dropped together by a labeling algorithm. 
 */
public class SubLabelLabelInfoDependencyProvider implements ILcdCollectedLabelInfoDependencyProvider {

  public void getDependingLabels(TLcdCollectedLabelInfo aLabel, TLcdCollectedLabelInfoList aLabelInfoList, List<TLcdCollectedLabelInfo> aDependingLabelsSFCT) {
    TLcdCollectedLabeledObjectInfo labeled_object = aLabel.getLabeledObject();
    List<TLcdCollectedLabelInfo> labels = aLabelInfoList.getLabelsForLabeledObject(labeled_object.getLayer(), labeled_object.getDomainObject());
    for (TLcdCollectedLabelInfo label : labels) {
      if (label.getLabelIdentifier().getLabelIndex() == aLabel.getLabelIdentifier().getLabelIndex() &&
          label.getLabelIdentifier().getSubLabelIndex() != aLabel.getLabelIdentifier().getSubLabelIndex()) {
        aDependingLabelsSFCT.add(label);
      }
    }
  }
}

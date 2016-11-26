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
package samples.lightspeed.clustering;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.luciad.model.transformation.clustering.TLcdCluster;
import com.luciad.view.lightspeed.TLspContext;
import com.luciad.view.lightspeed.style.styler.ALspLabelStyleCollector;
import com.luciad.view.lightspeed.style.styler.ILspCustomizableStyler;
import com.luciad.view.lightspeed.style.styler.ILspStyler;
import com.luciad.view.lightspeed.style.styler.TLspCustomizableStyle;

import samples.lightspeed.common.LabelStylerWrapper;

/**
 * <p>
 *   Decorator around a label {@code ILspStyler} which ignores the clusters,
 *   and does not try to paint any labels for them.
 * </p>
 */
public final class ClusterIgnoringLabelStylerWrapper extends LabelStylerWrapper implements ILspCustomizableStyler {

  private final ILspStyler fDelegate;

  public ClusterIgnoringLabelStylerWrapper(ILspStyler aDelegate) {
    super(aDelegate);
    fDelegate = aDelegate;
  }

  @Override
  public void style(Collection<?> aObjects, ALspLabelStyleCollector aStyleCollector, TLspContext aContext) {
    List<Object> nonClusteredObjects = new ArrayList<>();
    for (Object object : aObjects) {
      if (!(object instanceof TLcdCluster)) {
        nonClusteredObjects.add(object);
      }
    }
    super.style(nonClusteredObjects, aStyleCollector, aContext);
  }

  @Override
  public Collection<TLspCustomizableStyle> getStyles() {
    if (fDelegate instanceof ILspCustomizableStyler) {
      return ((ILspCustomizableStyler) fDelegate).getStyles();
    }
    return Collections.emptySet();
  }
}

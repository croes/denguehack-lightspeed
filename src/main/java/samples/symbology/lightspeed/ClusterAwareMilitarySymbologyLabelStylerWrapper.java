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
package samples.symbology.lightspeed;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.luciad.model.transformation.clustering.TLcdCluster;
import com.luciad.view.lightspeed.TLspContext;
import com.luciad.view.lightspeed.label.algorithm.TLspLabelLocationProvider;
import com.luciad.view.lightspeed.layer.TLspPaintState;
import com.luciad.view.lightspeed.painter.label.style.ALspLabelTextProviderStyle;
import com.luciad.view.lightspeed.style.ALspStyle;
import com.luciad.view.lightspeed.style.TLspTextStyle;
import com.luciad.view.lightspeed.style.TLspViewDisplacementStyle;
import com.luciad.view.lightspeed.style.styler.ALspLabelStyleCollector;
import com.luciad.view.lightspeed.style.styler.ALspLabelStyleCollectorWrapper;
import com.luciad.view.lightspeed.style.styler.ILspStyler;

import samples.common.UIColors;
import samples.lightspeed.common.LabelStylerWrapper;
import samples.symbology.common.util.MilitarySymbolClusteringUtil;

/**
 * Military symbology specific label styling for clusters.
 */
public class ClusterAwareMilitarySymbologyLabelStylerWrapper extends LabelStylerWrapper {

  /**
   * ADD_CHILD_STYLE adds the styling of a child element of the cluster to the styling of the cluster.
   */
  public enum Option {
    ADD_CHILD_STYLE, NONE
  }

  protected static final String MULTIPLICITY_SUBLABEL_ID = "MULTIPLICITY_LABEL";

  private final ILspStyler fStyler;
  private final TLspPaintState fPaintState;
  private final Option fOption;

  public ClusterAwareMilitarySymbologyLabelStylerWrapper(ILspStyler aStyler, TLspPaintState aPaintState) {
    this(aStyler, aPaintState, Option.NONE);
  }

  public ClusterAwareMilitarySymbologyLabelStylerWrapper(ILspStyler aStyler, TLspPaintState aPaintState, Option aOption) {
    super(aStyler);
    fStyler = aStyler;
    fPaintState = aPaintState;
    fOption = aOption;
  }

  protected boolean isNotRegular() {
    return fPaintState != TLspPaintState.REGULAR;
  }

  protected boolean addChildStyle() {
    return fOption == Option.ADD_CHILD_STYLE;
  }

  @Override
  public void style(Collection<?> aObjects, ALspLabelStyleCollector aStyleCollector, TLspContext aContext) {
    for (Object object : aObjects) {
      if (object instanceof TLcdCluster) {
        TLcdCluster<?> cluster = (TLcdCluster<?>) object;

        if (addChildStyle()) {
          addStylingOfARepresentativeElementToClusterStyling(cluster, aStyleCollector, aContext);
        }

        styleCluster(cluster, aStyleCollector, aContext);
      } else {
        fStyler.style(Collections.singletonList(object), aStyleCollector, aContext);
      }
    }
  }

  protected void styleCluster(TLcdCluster<?> aCluster, ALspLabelStyleCollector aStyleCollector, TLspContext aContext) {
    List<ALspStyle> styles = new ArrayList<>();
    styles.add(new ClusterMultiplicityProviderStyle());
    boolean bodyStyledAsMilitarySymbol = isBodyStyledAsMilitarySymbol(aCluster);
    float haloThickness = 1;
    if (bodyStyledAsMilitarySymbol) {
      styles.add(TLspViewDisplacementStyle.newBuilder().viewDisplacement(0, 36).build());
    } else {
      styles.add(TLspViewDisplacementStyle.newBuilder().viewDisplacement(0, 16).build());
      haloThickness = 0;
    }
    styles.add(TLspTextStyle.newBuilder()
                            .textColor(getColor(Color.BLACK))
                            .haloColor(getColor(Color.WHITE))
                            .haloThickness(haloThickness)
                            .build());
    aStyleCollector.object(aCluster)
                   .styles(styles)
                   .label(MULTIPLICITY_SUBLABEL_ID)
                   .locations(bodyStyledAsMilitarySymbol ? 30 : 0,
                              bodyStyledAsMilitarySymbol ? TLspLabelLocationProvider.Location.SOUTH : TLspLabelLocationProvider.Location.CENTER)
                   .submit();
  }

  protected boolean isBodyStyledAsMilitarySymbol(TLcdCluster<?> aCluster) {
    return MilitarySymbolClusteringUtil.allSameSymbol(aCluster);
  }

  private Color getColor(Color aColor) {
    return isNotRegular() ? UIColors.alpha(aColor, 111) : aColor;
  }

  private void addStylingOfARepresentativeElementToClusterStyling(TLcdCluster<?> aCluster, ALspLabelStyleCollector aStyleCollector, TLspContext aContext) {
    FixedObjectLabelStyleCollector collector = new FixedObjectLabelStyleCollector(aCluster, aStyleCollector);
    fStyler.style(Collections.singletonList(getRepresentativeElement(aCluster)), collector, aContext);
  }

  protected Object getRepresentativeElement(TLcdCluster<?> aCluster) {
    return aCluster.getComposingElements().iterator().next();
  }

  private static class ClusterMultiplicityProviderStyle extends ALspLabelTextProviderStyle {

    @Override
    public String[] getText(Object aDomainObject, Object aSubLabelID, TLspContext aContext) {
      if (aDomainObject instanceof TLcdCluster) {
        return new String[]{"" + ((TLcdCluster<?>) aDomainObject).getComposingElements().size()};
      }
      return super.getText(aDomainObject, aSubLabelID, aContext);
    }

  }

  /**
   * An ALspLabelStyleCollector that uses as objects the ones you configure beforehand.
   */
  private static class FixedObjectLabelStyleCollector extends ALspLabelStyleCollectorWrapper {

    private final TLcdCluster<?> fCluster;

    public FixedObjectLabelStyleCollector(TLcdCluster<?> aCluster, ALspLabelStyleCollector aDelegate) {
      super(aDelegate);
      fCluster = aCluster;
    }

    @Override
    public ALspLabelStyleCollector objects(Collection<?> aObjects) {
      return super.objects(Collections.singletonList(fCluster));
    }

  }

}

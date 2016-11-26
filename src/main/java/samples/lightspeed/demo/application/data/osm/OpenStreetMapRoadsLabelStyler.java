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
package samples.lightspeed.demo.application.data.osm;

import static com.luciad.fusion.client.view.lightspeed.TLspFusionGeometryProvider.LINE;

import static samples.lightspeed.demo.application.data.osm.RoadUtil.*;

import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.swing.SwingUtilities;

import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.TLspContext;
import com.luciad.view.lightspeed.camera.ALspViewXYZWorldTransformation;
import com.luciad.view.lightspeed.label.algorithm.discrete.TLspCurvedPathLabelingAlgorithm;
import com.luciad.view.lightspeed.painter.label.style.TLspDataObjectLabelTextProviderStyle;
import com.luciad.view.lightspeed.style.TLspTextStyle;
import com.luciad.view.lightspeed.style.styler.ALspLabelStyleCollector;
import com.luciad.view.lightspeed.style.styler.ALspLabelStyler;

class OpenStreetMapRoadsLabelStyler extends ALspLabelStyler {

  private final Set<ILspView> fWithPropertyChangeListener = new HashSet<ILspView>();

  // Highway icon styler
  private HighwayIconStyler fHighwayIconStyler = new HighwayIconStyler();

  // Other street labeling
  private TLspCurvedPathLabelingAlgorithm fLabelingAlgorithm = new TLspCurvedPathLabelingAlgorithm();

  private TLspTextStyle fTextStyle;
  private TLspDataObjectLabelTextProviderStyle fTextProviderStyle;

  public OpenStreetMapRoadsLabelStyler() {
    fLabelingAlgorithm.setQuality(0.5);
    fLabelingAlgorithm.setReusePreviousLocations(true);

   /* fPlacesLabelingAlgorithm.setMaxCoverage( 0.1 );
    fPlacesLabelingAlgorithm.setMoveLabelsOnlyOnConflict( true );*/

    fTextStyle = TLspTextStyle.newBuilder()
                              .textColor(Color.WHITE)
                              .haloColor(Color.BLACK)
                              .build();
    fTextProviderStyle = TLspDataObjectLabelTextProviderStyle.newBuilder()
                                                             .expressions("name")
                                                             .build();
  }

  @Override
  public void style(Collection<?> aObjects, ALspLabelStyleCollector aStyleCollector, TLspContext aContext) {
    int lod = getLevelOfDetail(aContext.getView());

    fHighwayIconStyler.style(aObjects, aStyleCollector, aContext);

    for (Object object : aObjects) {
      if (!isHighway(object)) {
        int priority = getPriority(object, 0, 1000, lod);
        if (isVisible(object, lod)) {
          aStyleCollector.object(object)
                         .algorithm(fLabelingAlgorithm)
                         .geometry(LINE)
                         .priority(priority)
                         .styles(fTextStyle, fTextProviderStyle)
                         .submit();
        }
      }
    }
  }

  private int getLevelOfDetail(ILspView aView) {
    checkView(aView);
    return RoadUtil.getLevelOfDetail(aView.getViewXYZWorldTransformation());
  }

  private synchronized void checkView(ILspView aView) {
    if (!(fWithPropertyChangeListener.contains(aView))) {
      fWithPropertyChangeListener.add(aView);
      new StyleInvalidator(aView);
    }
  }

  /**
   * Class that listens for changes in the level of detail of a given view and
   * triggers a style change event when such a change occurs.
   */
  private class StyleInvalidator implements PropertyChangeListener {
    private final ILspView fView;
    private ALspViewXYZWorldTransformation fTransformation;
    private int fLevelOfDetail;

    /**
     * Creates a new style invalidator for the given view.
     *
     * @param aView
     *          the view for which to create a style invalidator.
     */
    public StyleInvalidator(ILspView aView) {
      fView = aView;
      fTransformation = fView.getViewXYZWorldTransformation();
      fLevelOfDetail = getLevelOfDetail(fView);
      fView.removePropertyChangeListener(this);
      fView.addPropertyChangeListener(this);
      fTransformation.addPropertyChangeListener(this);
    }

    /**
     * Checks whether a style change event should be fired.
     *
     * @return true if a style change event should be fired, false otherwise.
     */
    private boolean shouldFireStyleChangeEvent() {
      int levelOfDetail = getLevelOfDetail(fView);
      boolean result = (fLevelOfDetail != levelOfDetail);
      fLevelOfDetail = levelOfDetail;
      return result;
    }

    @Override
    public void propertyChange(PropertyChangeEvent aEvent) {
      if (fView == aEvent.getSource() && fTransformation != fView.getViewXYZWorldTransformation()) {
        final ALspViewXYZWorldTransformation oldTransformation = fTransformation;
        fTransformation = fView.getViewXYZWorldTransformation();
        fLevelOfDetail = getLevelOfDetail(fView);
        SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            oldTransformation.removePropertyChangeListener(StyleInvalidator.this);
            fTransformation.removePropertyChangeListener(StyleInvalidator.this);
            fTransformation.addPropertyChangeListener(StyleInvalidator.this);
          }
        });
      }
      if (shouldFireStyleChangeEvent()) {
        fireStyleChangeEvent();
      }
    }
  }
}

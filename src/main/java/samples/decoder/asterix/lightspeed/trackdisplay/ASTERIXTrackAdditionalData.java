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
package samples.decoder.asterix.lightspeed.trackdisplay;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import com.luciad.util.ILcdPropertyChangeSource;
import com.luciad.util.collections.TLcdWeakIdentityHashMap;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.layer.TLspPaintState;
import com.luciad.view.lightspeed.painter.label.TLspLabelID;

/**
 * Object that contains additional data that is stored outside the TLcdASTERIXTrack objects. Currently it contains
 * - A comment
 * - A flag that determines if the track is highlighted or not.
 *
 * This object makes sure that all access to this additional data is properly locked, since it can be read and
 * modified from multiple threads.
 */
class ASTERIXTrackAdditionalData implements ILcdPropertyChangeSource {

  private final PropertyChangeSupport fSupport = new PropertyChangeSupport(this);

  private final Object fHighlightLock = new Object(); // Accessed from multiple threads => needs locking
  private final Set<Object> fHighlightedObjects = Collections.newSetFromMap(new TLcdWeakIdentityHashMap<Object, Boolean>());

  private final Object fCommentsLock = new Object(); // Accessed from multiple threads => needs locking
  private final Map<Object, String> fComments = new TLcdWeakIdentityHashMap<>();

  private final Object fHoveredLabelLock = new Object();
  private HoveredLabel fHoveredLabel;

  @Override
  public void addPropertyChangeListener(PropertyChangeListener aPropertyChangeListener) {
    fSupport.addPropertyChangeListener(aPropertyChangeListener);
  }

  @Override
  public void removePropertyChangeListener(PropertyChangeListener aPropertyChangeListener) {
    fSupport.removePropertyChangeListener(aPropertyChangeListener);
  }

  public String getComment(Object aObject) {
    synchronized (fCommentsLock) {
      return fComments.get(aObject);
    }
  }

  public String removeComment(Object aObject) {
    synchronized (fCommentsLock) {
      return fComments.remove(aObject);
    }
  }

  public String putComment(Object aObject, String aComment) {
    synchronized (fCommentsLock) {
      return fComments.put(aObject, aComment);
    }
  }

  public void clearHighlighting() {
    synchronized (fHighlightLock) {
      fHighlightedObjects.clear();
    }
  }

  public void invertHighlighting(Object aObject) {
    synchronized (fHighlightLock) {
      if (!fHighlightedObjects.remove(aObject)) {
        fHighlightedObjects.add(aObject);
      }
    }
  }

  public boolean isHighlighted(Object aObject) {
    synchronized (fHighlightLock) {
      return fHighlightedObjects.contains(aObject);
    }
  }

  public void clearHoveredLabel() {
    HoveredLabel oldHoveredLabel;
    synchronized (fHoveredLabelLock) {
      oldHoveredLabel = fHoveredLabel;
      fHoveredLabel = null;
    }
    fireHoveredLabelChangeEvent(oldHoveredLabel, null);
  }

  public void setHoveredLabel(TLspLabelID aLabel, TLspPaintState aPaintState, ILspView aView) {
    HoveredLabel oldHoveredLabel;
    HoveredLabel newHoveredLabel;
    synchronized (fHoveredLabelLock) {
      oldHoveredLabel = fHoveredLabel;
      fHoveredLabel = new HoveredLabel(aLabel, aPaintState, aView);
      newHoveredLabel = fHoveredLabel;
    }
    fireHoveredLabelChangeEvent(oldHoveredLabel, newHoveredLabel);
  }

  public boolean isHoveredLabel(TLspLabelID aLabel, TLspPaintState aPaintState, ILspView aView) {
    synchronized (fHoveredLabelLock) {
      if (fHoveredLabel == null) {
        return false;
      }
      return fHoveredLabel.getLabelID().equals(aLabel) &&
             fHoveredLabel.getPaintState().equals(aPaintState) &&
             fHoveredLabel.getView() == aView;
    }
  }

  public HoveredLabel getHoveredLabel() {
    synchronized (fHoveredLabelLock) {
      return fHoveredLabel;
    }
  }

  private void fireHoveredLabelChangeEvent(HoveredLabel aOld, HoveredLabel aNew) {
    fSupport.firePropertyChange("hoveredLabel", aOld, aNew);
  }

  public static class HoveredLabel {

    private final TLspLabelID fLabelID;
    private final TLspPaintState fPaintState;
    private final ILspView fView;

    public HoveredLabel(TLspLabelID aLabelID, TLspPaintState aPaintState, ILspView aView) {
      fLabelID = aLabelID;
      fPaintState = aPaintState;
      fView = aView;
    }

    public TLspLabelID getLabelID() {
      return fLabelID;
    }

    public TLspPaintState getPaintState() {
      return fPaintState;
    }

    public ILspView getView() {
      return fView;
    }
  }
}

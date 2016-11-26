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
package samples.lucy.frontend.mapcentric.gui.onmappanel;

import java.awt.Component;
import java.awt.Container;
import java.awt.KeyboardFocusManager;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.SwingUtilities;

/**
 * Tracks which OnMapPanel's are most important to the user. Panels are considered important if:
 * - they are added (while showing on screen, not during application init)
 * - they are expanded
 * - focus is transferred to it (or one of its children)
 *
 * This information is used to decided which panels can be collapsed, and which should preferably
 * remain open.
 */
class ImportantPanelTracker {
  /**
   * List with the panels the most important to the user. The most important ones are at the end.
   */
  private List<OnMapPanel> fImportant = new ArrayList<OnMapPanel>();

  private final ExpandListener fExpandListener = new ExpandListener();
  private Container fParent;

  public void parent(Container aParent) {
    if (fParent == null) {
      fParent = aParent;
      WeakFocusListener.install(this);
    }
    checkParent(aParent);
  }

  public void addLayoutComponent(Component comp) {
    // If a panel is added while we're on screen, it is likely important for the user
    if (comp.isShowing()) {
      markImportant((OnMapPanel) comp);
    }
    comp.addPropertyChangeListener(fExpandListener);
  }

  public void removeLayoutComponent(Component comp) {
    comp.removePropertyChangeListener(fExpandListener);
    removeImportantPanel((OnMapPanel) comp);
  }

  private void checkParent(Container aParent) {
    if (fParent != aParent) {
      throw new IllegalArgumentException("Layout is working on [" + fParent + "], cannot be reused for panel " + aParent);
    }
  }

  /**
   * Returns the panels in the order of importance. Most important ones are at the end, so that
   * best candidate to collapse is in the beginning.
   * @param parent parent
   * @return The ordered list.
   */
  public List<OnMapPanel> getPanelsInOrderOfImportance(Container parent) {
    checkParent(parent);

    // Make a list of candidates to collapse, the most important ones at the end. So prefer
    // closing bottom panels over top panels
    List<OnMapPanel> ordered = OnMapPanelLayout.getVisibleChildren(parent);
    Collections.reverse(ordered);

    // The ones known to be important are now at the end, in correct order
    ordered.removeAll(fImportant);
    ordered.addAll(fImportant);

    return ordered;
  }

  private void markImportant(OnMapPanel aPanel) {
    removeImportantPanel(aPanel);
    fImportant.add(aPanel);
  }

  private void removeImportantPanel(OnMapPanel aPanel) {
    fImportant.remove(aPanel);
  }

  /**
   * When a panel is expanded, it is likely important for the user and shouldn't be automatically
   * collapsed.
   */
  private class ExpandListener implements PropertyChangeListener {
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
      if ("collapsed".equals(evt.getPropertyName())) {
        if (Boolean.FALSE.equals(evt.getNewValue())) {
          markImportant((OnMapPanel) evt.getSource());
        }
      }
    }
  }

  void focusChanged(Component aNewFocusOwner) {
    if (fParent != null) {
      OnMapPanel focused = getFocusedPane(aNewFocusOwner, fParent);
      if (focused != null) {
        markImportant(focused);
      }
    }
  }

  private OnMapPanel getFocusedPane(Component aFocused, Container aOnMapPanelContainer) {
    if (aFocused != null && SwingUtilities.isDescendingFrom(aFocused, aOnMapPanelContainer)) {
      OnMapPanel focusedPanel = (OnMapPanel) SwingUtilities.getAncestorOfClass(OnMapPanel.class, aFocused);
      return focusedPanel;
    }
    return null;
  }

  /**
   * Tracks focus to identify which panel is being used, it is likely important. It uses a weak
   * listener so that memory leaks are avoided.
   */
  private static class WeakFocusListener implements PropertyChangeListener {
    private final KeyboardFocusManager fMgr;
    private WeakReference<ImportantPanelTracker> fLayout;

    public static void install(ImportantPanelTracker aLayout) {
      KeyboardFocusManager mgr = KeyboardFocusManager.getCurrentKeyboardFocusManager();
      mgr.addPropertyChangeListener(new WeakFocusListener(aLayout, mgr));
    }

    private WeakFocusListener(ImportantPanelTracker aLayout, KeyboardFocusManager aMgr) {
      fMgr = aMgr;
      fLayout = new WeakReference<ImportantPanelTracker>(aLayout);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
      ImportantPanelTracker onMapPanelLayout = fLayout.get();
      if (onMapPanelLayout == null) {
        fMgr.removePropertyChangeListener(this);
        return;
      }

      if ("permanentFocusOwner".equals(evt.getPropertyName())) {
        onMapPanelLayout.focusChanged((Component) evt.getNewValue());
      }
    }
  }
}

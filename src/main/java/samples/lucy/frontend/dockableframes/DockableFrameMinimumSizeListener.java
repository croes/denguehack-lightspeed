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
package samples.lucy.frontend.dockableframes;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.ContainerAdapter;
import java.awt.event.ContainerEvent;

import javax.swing.SwingUtilities;

import com.jidesoft.swing.JideSplitPane;

/**
 * When the first component is added, this listener will try to ensure that the dockable frame
 * is given enough space to display its components.
 *
 * To achieve this it traverses the Swing containment hierarchy and adjusts the slider positions
 * of all encountered JideSplitPanes as necessary.
 */
public class DockableFrameMinimumSizeListener extends ContainerAdapter {

  @Override
  public void componentAdded(ContainerEvent e) {
    Container dockable_frame = e.getContainer();

    //we only want to do this once, so remove the listener
    dockable_frame.removeContainerListener(this);

    final DockableFrameAppPane frame = (DockableFrameAppPane) SwingUtilities.getAncestorOfClass(DockableFrameAppPane.class, dockable_frame);

    if (frame != null) {
      Runnable runnable = new Runnable() {
        @Override
        public void run() {
          // The parent can be null if the application pane is created and closed immediately.
          if (frame.getAppContentPane().getParent() != null) {
            Dimension contentMin = frame.getAppContentPane().getMinimumSize();
            contentMin = new Dimension(contentMin.width + 5, contentMin.height + 5);

            Dimension contentParentMin = frame.getAppContentPane().getParent().getMinimumSize();
            int dx = Math.max(0, contentMin.width - contentParentMin.width);
            int dy = Math.max(0, contentMin.height - contentParentMin.height);

            Container current = frame;
            JideSplitPane split = getNextSplitPane(current);
            while (split != null) {
              Component direct_child = getDirectChild(split, current);

              // Adjust the minimum size with the portion that is added by the parent
              Dimension directChildMin = direct_child.getMinimumSize();
              directChildMin.width += dx;
              directChildMin.height += dy;

              int index = split.indexOfPane(direct_child);
              ensureMinSize(split, index, directChildMin);
              current = split;
              split = getNextSplitPane(current);
            }
          }
        }
      };
      EventQueue.invokeLater(runnable);
    }
  }

  private JideSplitPane getNextSplitPane(Container aCurrent) {
    return (JideSplitPane) SwingUtilities.getAncestorOfClass(JideSplitPane.class, aCurrent);
  }

  private Component getDirectChild(Container aAncestor, Component aChild) {
    for (int i = 0; i < aAncestor.getComponentCount(); i++) {
      Component direct_child = aAncestor.getComponent(i);
      if (SwingUtilities.isDescendingFrom(aChild, direct_child)) {
        return direct_child;
      }
    }
    throw new IllegalArgumentException("aChild does not seem to be in the containment hierarchy of aAncestor");
  }

  private void ensureMinSize(JideSplitPane aSplitPane, int aPaneIndex, Dimension aMinimumDimension) {
    int orientation = aSplitPane.getOrientation();
    if (orientation == JideSplitPane.HORIZONTAL_SPLIT) {
      updateDividerLocation(aSplitPane, aPaneIndex, aMinimumDimension.width, aSplitPane.getWidth());
    }
    if (orientation == JideSplitPane.VERTICAL_SPLIT) {
      updateDividerLocation(aSplitPane, aPaneIndex, aMinimumDimension.height, aSplitPane.getHeight());
    }
  }

  private void updateDividerLocation(JideSplitPane aSplitPane, int aPaneIndex, int aMinimum, int aTotal) {
    if (aSplitPane.getPaneCount() <= 1) {
      return; //we can't do anything here, as we cannot take the space of another component
    }
    if (aPaneIndex < aSplitPane.getPaneCount() - 1) {
      //adjust the location of the next divider
      int previous_divider_location = aPaneIndex == 0 ? 0 : aSplitPane.getDividerLocation(aPaneIndex - 1);
      int next_divider_location = aSplitPane.getDividerLocation(aPaneIndex);

      int current_height = next_divider_location - previous_divider_location;
      if (current_height < aMinimum) {
        aSplitPane.setDividerLocation(aPaneIndex, previous_divider_location + aMinimum);
      }
    } else {
      //for the last pane the previous divider should be adjusted
      int previous_divider_location = aSplitPane.getDividerLocation(aPaneIndex - 1);

      int current_height = aTotal - previous_divider_location;
      if (current_height < aMinimum) {
        aSplitPane.setDividerLocation(aPaneIndex - 1, aTotal - aMinimum);
      }
    }
  }
}

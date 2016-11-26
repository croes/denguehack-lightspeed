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
package samples.common;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.LayoutManager;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

/**
 * A container which can collapse or expand its content.
 * <p>Requires its parent container to have a {@link LayoutManager} using {@link #getPreferredSize()} when calculating its layout
 * (example {@link BorderLayout}).
 */
public class CollapsiblePane extends JPanel {

  public static enum Orientation {
    HORIZONTAL,
    VERTICAL
  }

  private boolean fCollapsed = false;
  private Container fContentPanel;

  /**
   * Constructs a new collapsible pane with a {@link JPanel} as content pane and the specified
   * orientation.
   */
  public CollapsiblePane(Orientation orientation) {
    super.setLayout(new BorderLayout(0, 0));
    JPanel panel = new JPanel();
    if (orientation == Orientation.VERTICAL) {
      panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
    } else {
      panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
    }
    setContentPane(panel);
  }

  /**
   * @return true if the pane is collapsed, false if expanded
   */
  public boolean isCollapsed() {
    return fCollapsed;
  }

  /**
   * <p>Expands or collapses this collapsible pane.
   * @param val true to make the content invisible, false to make it visible
   */
  public void setCollapsed(boolean val) {
    if (fCollapsed != val) {
      fCollapsed = val;
      fContentPanel.setVisible(!fCollapsed);
      invalidate();
      doLayout();
      repaint();
      firePropertyChange("collapsed", !fCollapsed, fCollapsed);
    }
  }

  /**
   * Sets the content pane of this collapsible pane.
   * @param aContentPanel the new content pane
   * @throws IllegalArgumentException if aContentPanel is null
   */
  public void setContentPane(Container aContentPanel) {
    if (aContentPanel == null) {
      throw new IllegalArgumentException("Content pane can't be null");
    }

    if (fContentPanel != null) {
      //these next two lines are as they are because if I try to remove
      //the content panel directly, then super.remove(comp) ends up
      //calling remove(int), which is overridden in this class, leading to
      //improper behavior.
      assert super.getComponent(0) == fContentPanel;
      super.remove(0);
    }

    fContentPanel = aContentPanel;
    super.addImpl(fContentPanel, BorderLayout.CENTER, -1);
  }

  /**
   * @return the content pane
   */
  public Container getContentPane() {
    return fContentPanel;
  }

  /**
   * Overridden to redirect call to the content pane.
   */
  public void setLayout(LayoutManager mgr) {
    // wrapper can be null when setLayout is called by "super()" constructor
    if (getContentPane() != null) {
      getContentPane().setLayout(mgr);
    }
  }

  /**
   * Overridden to redirect call to the content pane.
   */
  protected void addImpl(Component comp, Object constraints, int index) {
    getContentPane().add(comp, constraints, index);
  }

  /**
   * Overridden to redirect call to the content pane
   */
  public void remove(Component comp) {
    getContentPane().remove(comp);
  }

  /**
   * Overridden to redirect call to the content pane.
   */
  public void remove(int index) {
    getContentPane().remove(index);
  }

  /**
   * Overridden to redirect call to the content pane.
   */
  public void removeAll() {
    getContentPane().removeAll();
  }

}

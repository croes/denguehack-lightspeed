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
package samples.lucy.frontend.mapcentric.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.HierarchyBoundsAdapter;
import java.awt.event.HierarchyEvent;

import javax.swing.JPanel;

/**
 * Panel that calculates its preferred width as a ratio of its parents width.
 */
public class RelativePreferredSizePanel extends JPanel {
  private final double fMinWidthRatio;
  private final double fMaxWidthRatio;

  /**
   * Creates a panel that calculates its preferred width as a ratio of the actual width of its parent.
   *
   * @param aMinWidthRatio The minimum width of this panel as a ratio (between 0 and 1) of its parent.
   * @param aMaxWidthRatio The maximum width of this panel as a ratio (between 0 and 1) of its parent.
   */
  public RelativePreferredSizePanel(double aMinWidthRatio, double aMaxWidthRatio) {
    setOpaque(false);
    fMinWidthRatio = aMinWidthRatio;
    fMaxWidthRatio = aMaxWidthRatio;

    // Listen for size changes in the parent. Using a HierarchyBoundsListener makes sure this
    // component can be re-parented without issues.
    addHierarchyBoundsListener(new HierarchyBoundsAdapter() {
      @Override
      public void ancestorResized(HierarchyEvent e) {
        revalidate();
      }
    });
  }

  /**
   * Same as the other constructor, but it wraps the given content.
   * @param aMinWidthRatio See above.
   * @param aMaxWidthRatio See above.
   * @param aContent The content for this panel, added as the CENTER of a BorderLayout.
   */
  public RelativePreferredSizePanel(double aMinWidthRatio, double aMaxWidthRatio, Component aContent) {
    this(aMinWidthRatio, aMaxWidthRatio);
    setLayout(new BorderLayout());
    add(aContent, BorderLayout.CENTER);
  }

  @Override
  public Dimension getPreferredSize() {
    Dimension pref = super.getPreferredSize();
    Container parent = getParent();
    if (parent != null) {
      pref.width = calculatePrefWidth(pref.width, parent.getWidth(), fMinWidthRatio, fMaxWidthRatio);
    }
    return pref;
  }

  static int calculatePrefWidth(int aPrefWidth, int aParentWidth, double aMinRatio, double aMaxRatio) {
    int prefWidth = aPrefWidth;
    prefWidth = Math.max(prefWidth, (int) (aParentWidth * aMinRatio));
    prefWidth = Math.min(prefWidth, (int) (aParentWidth * aMaxRatio));
    return prefWidth;
  }
}

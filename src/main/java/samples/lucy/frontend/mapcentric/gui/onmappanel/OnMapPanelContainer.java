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

import javax.swing.BorderFactory;
import javax.swing.JPanel;

/**
 * Container for OnMapPanel's. It keeps a list of such panels, one below the other. It is meant
 * to be overlayed on the map, and as such offers some auxiliary panels for the end-user.
 *
 * Whenever the contents of panels changes so that their size is affected, they animate gently to
 * their new size. This allows to keep track of what is going on, instead of abrupt layout changes.
 *
 * It has some things in common with the JXTaskPane/JXTaskPaneContainer pair. The differences
 * are that the animations are managed more centrally, to avoid unexpected behavior. Also, when
 * panes are collapsed, they are only as wide as the title, ignoring the (collapsed) content.
 * The behavior of expanding/collapsing panels has some similarities with an accordion (much more
 * than with JXTaskPaneContainer). The difference being that multiple panels can be expanded if
 * space permits (e.g. both layer control and object properties). It is also possible to collapse
 * all panels, to maximize the map space.
 *
 * @see OnMapPanel
 */
public class OnMapPanelContainer extends JPanel {
  public OnMapPanelContainer(boolean aAutoCollapseOnMapPanels) {
    setOpaque(false);
    setLayout(new OnMapPanelLayout(aAutoCollapseOnMapPanels));
    setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
  }
}

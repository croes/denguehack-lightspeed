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
package samples.lucy.frontend.mapcentric.applicationpane;

import java.awt.BorderLayout;

import javax.swing.JComponent;

import samples.lucy.frontend.mapcentric.gui.HideableTabbedPane;
import samples.lucy.frontend.mapcentric.gui.onmappanel.OnMapPanelContainer;
import com.luciad.lucy.ILcyLucyEnv;
import com.luciad.lucy.gui.ILcyApplicationPane;
import com.luciad.lucy.gui.ILcyApplicationPaneFactory;
import com.luciad.lucy.gui.ILcyApplicationPaneOwner;

/**
 * Implementation of <code>ILcyApplicationPaneFactory</code> that creates different types of panels,
 * based on the location index:
 * - For the the map location, it uses an <code>ILcyApplicationPane</code> that is based on a
 *   regular <code>JPanel</code>: <code>MapAppPane</code>.
 * - For the horizontal pane location (e.g. table view), it uses a <code>HideableTabAppPane</code>.
 *   It makes sure only one such pane is visible at a time, and users can collapse easily collapse
 *   them.
 * - For the other locations, it uses <code>ILcyApplicationPane</code>'s that are on-map panes:
 *   <code>OnMapAppPane</code>. These act as (small) auxiliary panels that are overlayed on the
 *   map and that can be collapsed by the user.
 */
public class MapCentricAppPaneFactory implements ILcyApplicationPaneFactory {
  /**
   * Property that is specific to this application pane factory implementation. For tabs located
   * at the bottom, a TLcyGroupDescriptor can be provided used to insert the tab into. This is used
   * to position the tab to show/hide the error log at the far right, instead of together with
   * the other tabs.
   */
  public static final String TAB_GROUP_DESCRIPTOR_KEY = "TabGroupDescriptor";

  private final JComponent fMapArea;
  private final OnMapPanelContainer fOnMapPanelContainer;
  private final HideableTabbedPane fTabs;
  private ILcyLucyEnv fLucyEnv;

  public MapCentricAppPaneFactory(JComponent aMapArea, OnMapPanelContainer aOnMapPanelContainer, HideableTabbedPane aTabs) {
    fMapArea = aMapArea;
    fOnMapPanelContainer = aOnMapPanelContainer;
    fTabs = aTabs;
  }

  public void setLucyEnv(ILcyLucyEnv aLucyEnv) {
    fLucyEnv = aLucyEnv;
  }

  @Override
  public ILcyApplicationPane createApplicationPane(ILcyApplicationPaneOwner aOwner) {
    return createApplicationPane(ILcyApplicationPaneFactory.VERTICAL_PANE, aOwner);
  }

  @Override
  public ILcyApplicationPane createApplicationPane(int aLocationIndex, ILcyApplicationPaneOwner aOwner) {
    ILcyApplicationPane pane;

    // Put the map panes in a separate area, that can only contain one pane.
    if (aLocationIndex == ILcyApplicationPaneFactory.MAP_PANE) {
      if (fMapArea.getComponentCount() > 0) {
        throw new IllegalArgumentException("This front-end only supports one content panel in the map area.");
      }

      MapAppPane mapPane = new MapAppPane(fMapArea, aOwner, fLucyEnv);
      fMapArea.add(mapPane, BorderLayout.CENTER);
      fMapArea.revalidate();
      pane = mapPane;
    }
    // Put the horizontal panes in the hideable tabbed area at the bottom
    else if (aLocationIndex == ILcyApplicationPaneFactory.HORIZONTAL_PANE) {
      HideableTabAppPane tab = new HideableTabAppPane(fTabs, aOwner, fLucyEnv);
      fTabs.addTab(tab);
      pane = tab;
    }
    // Put the others in a on-map pane.
    else {
      final OnMapAppPane mapCentricPane = new OnMapAppPane(fOnMapPanelContainer, aOwner, fLucyEnv, aLocationIndex);
      fOnMapPanelContainer.add(mapCentricPane, getInsertionIndex(aLocationIndex));
      fOnMapPanelContainer.revalidate();
      pane = mapCentricPane;
    }

    fLucyEnv.getUserInterfaceManager().getApplicationPaneManager().applicationPaneAdded(pane);

    return pane;
  }

  /**
   * Finds the insertion index, keeping the on-map panes sorted according to their location index.
   * If multiple panes have the same location index, new ones are added below.
   * @param aLocationIndex The location index of the on-map pane to add.
   * @return The insertion index to insert the new on-map pane.
   */
  private int getInsertionIndex(int aLocationIndex) {
    //binary search the index to insert the new item at.  Not using Collections.binarySearch because
    //it does not define its behavior for identical values.
    int low = 0;
    int high = fOnMapPanelContainer.getComponentCount();
    int insert = high;
    while (low < high) {
      int mid = low + ((high - low) / 2); // = (low + high) / 2, but avoid overflow
      if (getSortKey(mid) > aLocationIndex) {
        high = mid;
        insert = high;
      } else { // <= (tend to add new items with same sort key at the right)
        low = mid + 1;
        insert = low;
      }
    }
    return insert;
  }

  private int getSortKey(int aIndex) {
    return ((OnMapAppPane) fOnMapPanelContainer.getComponent(aIndex)).getOriginalLocationIndex();
  }
}

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

import java.awt.Component;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JComponent;

import com.luciad.gui.TLcdAWTUtil;
import samples.lucy.frontend.mapcentric.gui.HideableTabbedPane;
import samples.lucy.frontend.mapcentric.gui.onmappanel.OnMapPanelContainer;
import com.luciad.lucy.util.properties.ALcyProperties;
import com.luciad.lucy.util.properties.TLcyStringProperties;
import com.luciad.lucy.util.properties.codec.TLcyStringPropertiesCodec;
import com.luciad.lucy.workspace.ALcyWorkspaceCodec;
import com.luciad.lucy.workspace.ALcyWorkspaceCodecDelegate;
import com.luciad.util.logging.ILcdLogger;
import com.luciad.util.logging.TLcdLoggerFactory;

/**
 * Implementation of <code>ALcyWorkspaceCodecDelegate</code> that (re)stores all on-map panes and the
 * map pane, as created by <code>MapCentricAppPaneFactory</code>.  For the on-map panes, it also stores
 * their order and whether they are collapsed or not.
 */
public class MapCentricWorkspaceCodecDelegate extends ALcyWorkspaceCodecDelegate {

  private static final ILcdLogger LOGGER = TLcdLoggerFactory.getLogger(MapCentricWorkspaceCodecDelegate.class.getName());

  private static final String MAP_PANE = "mapPane";

  private static final String ON_MAP_PANES = "onMapPanes";
  private static final String COLLAPSED = "collapsed";

  private static final String BOTTOM_TABS = "bottomTabs";
  private static final String SELECTED_TAB = "bottomTabSelected";

  private final String fUID;
  private final String fPrefix;
  private final JComponent fMapArea;
  private final OnMapPanelContainer fOnMapPanelContainer;
  private final HideableTabbedPane fBottomTabs;

  public MapCentricWorkspaceCodecDelegate(String aUID, String aPrefix,
                                          JComponent aMapArea,
                                          OnMapPanelContainer aOnMapPanelContainer,
                                          HideableTabbedPane aBottomTabs) {
    fUID = aUID;
    fPrefix = aPrefix;
    fMapArea = aMapArea;
    fOnMapPanelContainer = aOnMapPanelContainer;
    fBottomTabs = aBottomTabs;
  }

  @Override
  public String getUID() {
    return fUID;
  }

  @Override
  public void encode(ALcyWorkspaceCodec aWSCodec, OutputStream aOut) throws IOException {
    TLcyStringProperties props = new TLcyStringProperties();

    encodeMapArea(aWSCodec, props);
    encodeOnMapPanes(aWSCodec, props);
    encodeBottomTabs(aWSCodec, props);

    new TLcyStringPropertiesCodec().encode(props, aOut);
  }

  @Override
  public void decode(final ALcyWorkspaceCodec aWSCodec, InputStream aIn) throws IOException {
    final ALcyProperties props = new TLcyStringPropertiesCodec().decode(aIn);

    decodeMapArea(aWSCodec, props);
    decodeOnMapPanes(aWSCodec, props);
    decodeBottomTabs(aWSCodec, props);
  }

  private void encodeMapArea(ALcyWorkspaceCodec aWSCodec, TLcyStringProperties aProps) throws IOException {
    //There can only be one component in the map area, store it.
    aProps.putString(fPrefix + MAP_PANE, aWSCodec.encodeReference(fMapArea.getComponent(0)));
  }

  private void decodeMapArea(ALcyWorkspaceCodec aWSCodec, ALcyProperties aProps) throws IOException {
    final MapAppPane mapPane = (MapAppPane) aWSCodec.decodeReference(aProps.getString(fPrefix + MAP_PANE, null));
    if (mapPane != null) {
      TLcdAWTUtil.invokeAndWait(new Runnable() {
        @Override
        public void run() {
          fMapArea.add(mapPane);
        }
      });
    } else {
      aWSCodec.getLogListener().warn(this, "Could not restore the map!");
    }
  }

  private void encodeOnMapPanes(ALcyWorkspaceCodec aWSCodec, TLcyStringProperties aProps) throws IOException {
    //Store all on-map panes overlayed on the map
    Component[] allPanes = fOnMapPanelContainer.getComponents();
    ArrayList<String> paneRefs = new ArrayList<String>();
    for (Component component : allPanes) {
      OnMapAppPane pane = (OnMapAppPane) component;
      if (aWSCodec.canEncodeReference(pane)) {
        int insertIndex = paneRefs.size();
        paneRefs.add(aWSCodec.encodeReference(pane));

        //Store collapsed state of every ILcyApplicationPane
        aProps.putBoolean(fPrefix + COLLAPSED + insertIndex, pane.isCollapsed());
      } else {
        LOGGER.warn("ILcyApplicationPane [" + pane + "] could not be encoded to the workspace");
      }
    }
    aProps.putStringArray(fPrefix + ON_MAP_PANES, paneRefs.toArray(new String[paneRefs.size()]));
  }

  private void decodeOnMapPanes(ALcyWorkspaceCodec aWSCodec, final ALcyProperties aProps) throws IOException {
    //Restore all panes
    List<String> allPaneRefs = Arrays.asList(aProps.getStringArray(fPrefix + ON_MAP_PANES, new String[0]));
    for (int i = 0; i < allPaneRefs.size(); i++) {
      final OnMapAppPane pane = (OnMapAppPane) aWSCodec.decodeReference(allPaneRefs.get(i));

      if (pane != null) {
        final String key = fPrefix + COLLAPSED + i;
        TLcdAWTUtil.invokeAndWait(new Runnable() {
          @Override
          public void run() {
            pane.setCollapsed(aProps.getBoolean(key, pane.isCollapsed()));
          }
        });
      }
    }
  }

  private void encodeBottomTabs(ALcyWorkspaceCodec aWSCodec, TLcyStringProperties aProps) throws IOException {
    //Store all application panes at the bottom
    ArrayList<String> tabRefs = new ArrayList<String>();
    for (HideableTabbedPane.Tab tab : fBottomTabs.getTabs()) {
      String reference = aWSCodec.encodeReference(tab);
      if (reference == null) {
        LOGGER.warn("HideableTabbedPane.Tab [" + tab + "] could not be encoded to the workspace");
      } else {
        tabRefs.add(reference);
      }
    }
    aProps.putStringArray(fPrefix + BOTTOM_TABS, tabRefs.toArray(new String[tabRefs.size()]));
    aProps.putString(fPrefix + SELECTED_TAB, aWSCodec.encodeReference(fBottomTabs.getSelectedTab()));
  }

  private void decodeBottomTabs(final ALcyWorkspaceCodec aWSCodec, ALcyProperties aProps) throws IOException {
    List<String> allTabRefs = Arrays.asList(aProps.getStringArray(fPrefix + BOTTOM_TABS, new String[0]));
    for (String tabRef : allTabRefs) {
      final HideableTabAppPane pane = (HideableTabAppPane) aWSCodec.decodeReference(tabRef);
      if (pane != null) {
        TLcdAWTUtil.invokeAndWait(new Runnable() {
          @Override
          public void run() {
            // Remove and re-add the tab. The only effect is that the order can possibly be different.
            // After this loop is complete, the order matches the order in which they were encoded.
            fBottomTabs.removeTab(pane);
            fBottomTabs.addTab(pane);
          }
        });

      }
    }
    // Restore selected tab. No checks are needed if this happens to be null as all code can deal with it.
    final String selectedRef = aProps.getString(fPrefix + SELECTED_TAB, null);
    final HideableTabAppPane selectedTab = (HideableTabAppPane) aWSCodec.decodeReference(selectedRef);
    TLcdAWTUtil.invokeAndWait(new Runnable() {
      @Override
      public void run() {
        fBottomTabs.setSelectedTab(selectedTab);
      }
    });
  }
}

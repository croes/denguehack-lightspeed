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

import static org.apache.commons.codec.binary.Base64.decodeBase64;
import static org.apache.commons.codec.binary.Base64.encodeBase64String;

import java.beans.PropertyVetoException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

import com.jidesoft.docking.DockContext;
import com.jidesoft.docking.DockableFrame;
import com.jidesoft.docking.DockableHolder;
import com.jidesoft.docking.DockingManager;
import com.luciad.gui.TLcdAWTUtil;
import com.luciad.lucy.ILcyLucyEnv;
import com.luciad.lucy.ILcyLucyEnvListener;
import com.luciad.lucy.TLcyLucyEnvEvent;
import com.luciad.lucy.gui.ILcyApplicationPane;
import com.luciad.lucy.gui.TLcyApplicationPaneManager;
import com.luciad.lucy.util.TLcyVetoException;
import com.luciad.lucy.util.language.TLcyLang;
import com.luciad.lucy.util.properties.ALcyProperties;
import com.luciad.lucy.util.properties.TLcyStringProperties;
import com.luciad.lucy.util.properties.codec.TLcyStringPropertiesCodec;
import com.luciad.lucy.workspace.ALcyWorkspaceCodec;
import com.luciad.lucy.workspace.ALcyWorkspaceCodecDelegate;
import com.luciad.lucy.workspace.ILcyWorkspaceManagerListener;
import com.luciad.lucy.workspace.TLcyWorkspaceManagerEvent;
import com.luciad.util.logging.ILcdLogger;
import com.luciad.util.logging.TLcdLoggerFactory;

/**
 * Implementation of {@code ALcyWorkspaceCodecDelegate} that (re)stores all ILcyApplicationPane's of
 * a given {@code DefaultDockableHolder}. When decoding a workspace, the layout remains invisible
 * until the decoding process has finished.
 */
public class DockableWorkspaceCodecDelegate extends ALcyWorkspaceCodecDelegate implements ILcyWorkspaceManagerListener,
                                                                                          ILcyLucyEnvListener {

  private static final ILcdLogger LOGGER = TLcdLoggerFactory.getLogger(DockableWorkspaceCodecDelegate.class.getName());

  private static final String REGULAR_PREFIX = "DockableFrameAppPane ";

  private static final String LAYOUT = "layout";
  private static final String APPLICATION_PANES = "applicationPaneCount";
  private static final String APPLICATION_PANE = "applicationPane";
  private static final String REFERENCE = "reference";
  private static final String KEY = "key";
  private static final String DOCKID = "dockid";
  private static final String SELECTED = "selected";

  private final String fUID;
  private final ILcyLucyEnv fLucyEnv;
  private final String fPrefix;
  private final DockableHolder fDockableHolder;
  private boolean fLayoutDataLoaded = false;

  public DockableWorkspaceCodecDelegate(ILcyLucyEnv aLucyEnv, String aUID,
                                        String aPrefix, DockableHolder aDockableHolder) {
    fLucyEnv = aLucyEnv;
    fUID = aUID;
    fPrefix = aPrefix;
    fDockableHolder = aDockableHolder;

    aLucyEnv.getWorkspaceManager().addWorkspaceManagerListener(this);
    aLucyEnv.addLucyEnvListener(this);
  }

  @Override
  public String getUID() {
    return fUID;
  }

  @Override
  public void encode(ALcyWorkspaceCodec aWSCodec, OutputStream aOut) throws IOException {
    TLcyStringProperties configuration = new TLcyStringProperties();
    // Store all ILcyApplicationPane's
    Collection<String> all_panes = fDockableHolder.getDockingManager().getAllFrames();

    int count = 0;
    for (String frame_name : all_panes) {
      DockableFrameAppPane pane = (DockableFrameAppPane) fDockableHolder.getDockingManager().getFrame(frame_name);
      if (aWSCodec.canEncodeReference(pane)) {
        count++;
      } else {
        LOGGER.warn("ILcyApplicationPane [" + pane + "] could not be encoded to the workspace");
      }
    }
    configuration.putInt(fPrefix + APPLICATION_PANES, count);

    // Store dockableframe key of every ILcyApplicationPane
    int index = 0;
    for (String frame_name : all_panes) {
      DockableFrameAppPane pane = (DockableFrameAppPane) fDockableHolder.getDockingManager().getFrame(frame_name);
      if (aWSCodec.canEncodeReference(pane)) {
        configuration.putString(fPrefix + APPLICATION_PANE + index + REFERENCE, aWSCodec.encodeReference(pane));
        configuration.putString(fPrefix + APPLICATION_PANE + index + KEY, pane.getKey());
        configuration.putInt(fPrefix + APPLICATION_PANE + index + DOCKID, pane.getDockID());
        index++;
      }
    }

    // Store selected pane.
    DockableFrameAppPane selected = (DockableFrameAppPane) fDockableHolder.getDockingManager().getFrame(fDockableHolder.getDockingManager().getActiveFrameKey());
    if (selected != null) {
      if (aWSCodec.canEncodeReference(selected)) {
        configuration.putString(fPrefix + SELECTED, aWSCodec.encodeReference(selected));
      }
    }

    // Store JIDE layout workspace.
    final ByteArrayOutputStream[] jide_workspace = new ByteArrayOutputStream[]{new ByteArrayOutputStream()};
    final IOException[] exception = new IOException[]{null};
    //saving the layout triggers a repaint, so perform it on the EDT
    TLcdAWTUtil.invokeAndWait(new Runnable() {
      @Override
      public void run() {
        try {
          fDockableHolder.getDockingManager().saveLayoutTo(jide_workspace[0]);
        } catch (IOException e) {
          exception[0] = null;
        }
      }
    });
    if (exception[0] != null) {
      throw exception[0];
    }
    String jide_workspace_as_string = encodeBase64String(jide_workspace[0].toByteArray());
    configuration.putString(fPrefix + LAYOUT, jide_workspace_as_string);

    // Store properties.
    new TLcyStringPropertiesCodec().encode(configuration, aOut);
  }

  @Override
  public void decode(final ALcyWorkspaceCodec aWSCodec, InputStream aIn) throws IOException {
    // In this method, we must take care to perform all Swing related code on the EDT thread,
    // using TLcdAWTUtil.invokeAndWait.

    final ALcyProperties props = new TLcyStringPropertiesCodec().decode(aIn);

    // Decode all application pane references and set dockable frame keys.
    final ArrayList<DockableFrame> panes_known_in_workspace = new ArrayList<DockableFrame>();
    int count = props.getInt(fPrefix + APPLICATION_PANES, 0);
    for (int i = 0; i < count; i++) {
      String pane_reference = props.getString(fPrefix + APPLICATION_PANE + i + REFERENCE, null);
      final DockableFrameAppPane pane = (DockableFrameAppPane) aWSCodec.decodeReference(pane_reference);
      if (pane != null) {
        panes_known_in_workspace.add(pane);

        // Retrieve the key and dock id stored in the workspace.
        final String key = props.getString(fPrefix + APPLICATION_PANE + i + KEY, null);
        final int dockid = props.getInt(fPrefix + APPLICATION_PANE + i + DOCKID, i);
        TLcdAWTUtil.invokeAndWait(new Runnable() {
          @Override
          public void run() {
            if (key != null) {
              // Set the key, but first check if it is already used.
              DockingManager dockingManager = fDockableHolder.getDockingManager();
              DockableFrameAppPane old_frame_for_key = (DockableFrameAppPane) dockingManager.getFrame(key);
              if (old_frame_for_key != null) {
                // There is already a pane with the same key: because we want to use this key for the
                // decoded pane, we give the other pane a new unique key.
                int index = 0;
                while ((REGULAR_PREFIX + index).equals(key) ||
                       dockingManager.getFrame(REGULAR_PREFIX + index) != null) {
                  index++;
                }
                old_frame_for_key.setKey(REGULAR_PREFIX + index);

              }
              pane.setKey(key);

              // Set the dock id.
              pane.setDockID(dockid);
            }
          }
        });
      }
    }

    final List<DockableFrame> panes_unknown_in_workspace = new ArrayList<DockableFrame>();
    TLcdAWTUtil.invokeAndWait(new Runnable() {
      @Override
      public void run() {
        // There can be some panes for which no information is stored in the workspace.
        // JIDE resets their layout in a very jarring way.
        // As a workaround, those panes are temporarily removed, and are added again
        // after JIDE has restored the layout.
        DockingManager dockingManager = fDockableHolder.getDockingManager();
        panes_unknown_in_workspace.addAll(getAndRemoveUnknownPanes(dockingManager, panes_known_in_workspace));

        // Decode JIDE layout workspace.
        String jide_layout_as_string = props.getString(fPrefix + LAYOUT, null);
        if (jide_layout_as_string != null) {
          byte[] jide_layout = decodeBase64(jide_layout_as_string);
          ByteArrayInputStream jide_layout_inputstream = new ByteArrayInputStream(jide_layout);
          dockingManager.loadLayoutFrom(jide_layout_inputstream);
          fLayoutDataLoaded = true;
        }

        if (!dockingManager.isLoadDataSuccessful()) {
          aWSCodec.getLogListener().warn(
              TLcyLang.getString("The layout couldn't be restored correctly, resetting to default"));
        }

        //Flush any extra contexts that might exist because of addons that were loading during workspace saving,
        //but not during workspace loading
        dockingManager.removeExtraContexts();

        // JIDE has now restored the layout, now again add the unknown panes.
        restoreUnknownPanes(dockingManager, panes_unknown_in_workspace);
      }
    });

    // Select pane that used to be selected.
    final DockableFrameAppPane selected = (DockableFrameAppPane)
        aWSCodec.decodeReference(props.getString(fPrefix + SELECTED, null));
    if (selected != null) {
      TLcdAWTUtil.invokeAndWait(new Runnable() {
        @Override
        public void run() {
          try {
            selected.setActive(true);
          } catch (PropertyVetoException e) {
            //somebody did not want this frame to be selected... ignore.
          }
        }
      });
    }
  }

  @Override
  public void workspaceStatusChanged(TLcyWorkspaceManagerEvent aEvent) {
    if (aEvent.getID() == TLcyWorkspaceManagerEvent.STARTING_WORKSPACE_DECODING) {
      // during the initialisation of Lucy, we call beginLoadLayoutData
      // (see samples.lucy.frontend.dockableframes.DockableHolderFactory.createDockableHolder() )
      // if we load a workspace before Lucy is initialized (e.g. loading a default workspace from
      // the workspace add-on ) the beginLoadLayoutData call is never terminated and JIDE gets
      // confused when we do another beginLoadLayoutData() call. Therefore we first terminate
      // the first call
      if (fLucyEnv.getLucyEnvState() == ILcyLucyEnv.STATE_INITIALIZING) {
        TLcdAWTUtil.invokeAndWait(new Runnable() {
          @Override
          public void run() {
            DockableHolderFactory.resetToDefault(fDockableHolder.getDockingManager());
          }
        });
      }
      TLcdAWTUtil.invokeAndWait(new Runnable() {
        @Override
        public void run() {
          fLayoutDataLoaded = false;

          // This indicates to JIDE that we're about to load layout data.  It is terminated by a
          // call to either resetToDefault or loadLayoutFrom.
          fDockableHolder.getDockingManager().beginLoadLayoutData();

          // Make sure that the layout is not visible during workspace decoding.
          fDockableHolder.getDockingManager().getContentContainer().setVisible(false);
        }
      });
    } else if (aEvent.getID() == TLcyWorkspaceManagerEvent.WORKSPACE_DECODING_ENDED) {
      if (fDockableHolder != null) {
        TLcdAWTUtil.invokeAndWait(new Runnable() {
          @Override
          public void run() {
            resetLayoutIfNeeded();

            // Change tab selection in tabbed panes after decoding workspace, to avoid blank areas
            // This might be the case for ILcyApplicationPanes which did not have workspace support
            TLcyApplicationPaneManager appMan = fLucyEnv.getUserInterfaceManager().getApplicationPaneManager();
            for (int i = 0; i < appMan.getApplicationPaneCount(); i++) {
              ILcyApplicationPane app = appMan.getApplicationPane(i);
              JTabbedPane tab = (JTabbedPane) SwingUtilities.getAncestorOfClass(JTabbedPane.class, app.getAppContentPane());
              if (tab != null) {
                if (tab.getSelectedIndex() == -1 && tab.getTabCount() > 0) {
                  tab.setSelectedIndex(0);
                }
              }
            }

            // Workspace decoding has finished: make the layout visible again..
            fDockableHolder.getDockingManager().getContentContainer().setVisible(true);
          }
        });
      }
    }
  }

  @Override
  public void lucyEnvStatusChanged(TLcyLucyEnvEvent aEvent) throws TLcyVetoException {
    if (aEvent.getID() == TLcyLucyEnvEvent.INITIALIZED) {
      aEvent.getLucyEnv().removeLucyEnvListener(this);
      //after calling beginLoadLayoutData() in the DockableHolderFactory, we should still mark the loading is done
      resetLayoutIfNeeded();
    }
  }

  /**
   * Jide requires to call resetToDefault if no layout data was loaded.  This can happen because of
   * two reasons:
   * - the workspace that was loaded doesn't contain any layout data.  This is the case
   * when the workspace was saved with a different front-end, for example the tabbed panes front-end.
   * - no workspace was loaded when starting the application
   * See also LUCY-2148
   */
  private void resetLayoutIfNeeded() {
    if (!fLayoutDataLoaded) {
      DockableHolderFactory.resetToDefault(fDockableHolder.getDockingManager());
    }
  }

  private static List<DockableFrame> getAndRemoveUnknownPanes(DockingManager aDockingManager, ArrayList<DockableFrame> aKnownPanes) {
    List<DockableFrame> unknown_frames = new ArrayList<DockableFrame>();
    List<String> names = aDockingManager.getAllFrameNames();
    for (String name : names) {
      DockableFrame frame = aDockingManager.getFrame(name);
      if (!aKnownPanes.contains(frame)) {
        unknown_frames.add(frame);
        aDockingManager.removeFrame(frame.getKey());
        flushDockContext(frame);
      }
    }
    return unknown_frames;
  }

  /**
   * Flushes the context of the given DockableFrame, to let it forget about its layout information.
   * @param aFrame The frame to flush the context of.
   */
  private static void flushDockContext(DockableFrame aFrame) {
    DockContext new_context = new DockContext();
    DockContext old_context = aFrame.getContext();
    new_context.setInitIndex(old_context.getInitIndex());
    new_context.setInitSide(old_context.getInitSide());
    new_context.setInitMode(old_context.getInitMode());
    aFrame.setContext(new_context);
  }

  private static void restoreUnknownPanes(DockingManager aDockingManager, List<DockableFrame> aExistingFrames) {
    for (DockableFrame aExistingFrame : aExistingFrames) {
      DockableFrameAppPane frame = (DockableFrameAppPane) aExistingFrame;
      aDockingManager.addFrame(frame);
    }
  }
}

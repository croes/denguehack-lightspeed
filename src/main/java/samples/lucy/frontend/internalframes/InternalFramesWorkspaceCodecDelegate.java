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
package samples.lucy.frontend.internalframes;

import java.awt.Rectangle;
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;

import com.luciad.lucy.util.properties.ALcyProperties;
import com.luciad.lucy.util.properties.TLcyStringProperties;
import com.luciad.lucy.util.properties.codec.TLcyStringPropertiesCodec;
import com.luciad.lucy.workspace.ALcyWorkspaceCodec;
import com.luciad.lucy.workspace.ALcyWorkspaceCodecDelegate;
import com.luciad.util.logging.ILcdLogger;
import com.luciad.util.logging.TLcdLoggerFactory;

/**
 * Implementation of <code>ALcyWorkspaceCodecDelegate</code> that (re)stores all ILcyApplicationPane's of
 * a given <code>JDesktopPane</code>.
 */
public class InternalFramesWorkspaceCodecDelegate extends ALcyWorkspaceCodecDelegate {

  private static final ILcdLogger LOGGER = TLcdLoggerFactory.getLogger(InternalFramesWorkspaceCodecDelegate.class.getName());

  private static final String APPLICATION_PANES = "applicationPanes";
  private static final String BOUNDS = "bounds";
  private static final String SELECTED = "selected";

  private final String fUID;
  private String fPrefix;
  private JDesktopPane fDesktopPane;

  public InternalFramesWorkspaceCodecDelegate(String aUID, String aPrefix, JDesktopPane aDesktopPane) {
    fUID = aUID;
    fPrefix = aPrefix;
    fDesktopPane = aDesktopPane;
  }

  @Override
  public String getUID() {
    return fUID;
  }

  @Override
  public void encode(ALcyWorkspaceCodec aWSCodec, OutputStream aOut) throws IOException {
    TLcyStringProperties props = new TLcyStringProperties();

    //Store all ILcyApplicationPane's
    JInternalFrame[] all_panes = fDesktopPane.getAllFrames();
    ArrayList<String> pane_refs = new ArrayList<String>();
    for (JInternalFrame all_pane : all_panes) {
      InternalFrameAppPane pane = (InternalFrameAppPane) all_pane;
      if (aWSCodec.canEncodeReference(pane)) {
        int insert_index = pane_refs.size();
        pane_refs.add(aWSCodec.encodeReference(pane));

        //Store bounds of every ILcyApplicationPane
        Rectangle bounds = pane.getBounds();
        props.putIntArray(fPrefix + BOUNDS + insert_index,
                          new int[]{bounds.x, bounds.y, bounds.width, bounds.height});
      } else {
        LOGGER.warn("ILcyApplicationPane [" + pane + "] could not be encoded to the workspace");
      }
    }
    props.putStringArray(fPrefix + APPLICATION_PANES, pane_refs.toArray(new String[pane_refs.size()]));

    //Store selected pane
    InternalFrameAppPane selected = (InternalFrameAppPane) fDesktopPane.getSelectedFrame();
    if (selected != null) {
      if (aWSCodec.canEncodeReference(selected)) {
        props.putString(fPrefix + SELECTED, aWSCodec.encodeReference(selected));
      }
    }
    new TLcyStringPropertiesCodec().encode(props, aOut);
  }

  @Override
  public void decode(ALcyWorkspaceCodec aWSCodec, InputStream aIn) throws IOException {
    ALcyProperties props = new TLcyStringPropertiesCodec().decode(aIn);

    //Restore all panes and their bounds
    List all_pane_refs = Arrays.asList(props.getStringArray(fPrefix + APPLICATION_PANES, new String[0]));
    if (all_pane_refs != null) {
      for (int i = 0; i < all_pane_refs.size(); i++) {
        InternalFrameAppPane pane = (InternalFrameAppPane)
            aWSCodec.decodeReference((String) all_pane_refs.get(i));

        if (pane != null) {
          int[] bounds = props.getIntArray(fPrefix + BOUNDS + i, null);
          if (bounds != null && bounds.length == 4) {
            pane.setBounds(bounds[0], bounds[1], bounds[2], bounds[3]);
          }
        }
      }
    }

    //Select pane that used to be selected
    InternalFrameAppPane selected = (InternalFrameAppPane)
        aWSCodec.decodeReference(props.getString(fPrefix + SELECTED, null));
    if (selected != null) {
      try {
        selected.setSelected(true);
      } catch (PropertyVetoException e) {
        //somebody did not want this frame to be selected... ignore.
      }
    }
  }
}

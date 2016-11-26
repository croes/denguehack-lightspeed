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
package samples.lucy.frontend.mapcentric.status;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Frame;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.luciad.gui.TLcdAWTUtil;
import com.luciad.gui.TLcdIconFactory;
import com.luciad.gui.TLcdResizeableIcon;
import com.luciad.gui.TLcdSymbol;
import com.luciad.gui.swing.TLcdSWIcon;
import samples.lucy.util.HTMLWordWrappingEditorKit;
import com.luciad.lucy.ILcyLucyEnv;
import com.luciad.lucy.map.ILcyGenericMapComponent;
import com.luciad.lucy.util.hyperlink.TLcyCompositeHyperlinkListener;
import com.luciad.lucy.util.properties.ALcyProperties;
import com.luciad.util.ILcdStatusListener;
import com.luciad.util.TLcdStatusEvent;

/**
 * <p>
 *   Class which will display the {@link TLcdStatusEvent#getMessage() status messages} send by the Lucy back-end
 *   on the currently active map.
 * </p>
 */
final class StatusMessageDisplayer implements ILcdStatusListener<Object> {
  /**
   * Vertical spacing between two messages which are shown at the same time.
   * Spacing is expressed in pixels.
   */
  private static final int VERTICAL_SPACING_BETWEEN_MESSAGES = 10;
  /**
   * Time it takes before the message is automatically hidden
   */
  private static final int REMAIN_VISIBLE_TIME_IN_SECONDS = 10;
  /**
   * The width of the text pane, expressed in pixels
   */
  private static final int MESSAGE_WINDOW_WIDTH = 250;
  /**
   * The spacing between the border of the map, and the border of the
   * message window, expressed in pixels
   */
  private static final int BORDER_SPACING = 5;

  private final ILcyLucyEnv fLucyEnv;
  private int fCurrentYPosition = VERTICAL_SPACING_BETWEEN_MESSAGES;

  private final Location fLocation;

  StatusMessageDisplayer(String aPropertiesPrefix, ALcyProperties aProperties, ILcyLucyEnv aLucyEnv) {
    fLucyEnv = aLucyEnv;
    fLucyEnv.addStatusListener(this);
    fLocation = Location.valueOf(aProperties.getString(aPropertiesPrefix + "notifications.location", Location.SOUTH_EAST.name()));
  }

  @Override
  public void statusChanged(final TLcdStatusEvent<Object> aStatusEvent) {
    final String message = aStatusEvent.getMessage();
    if (aStatusEvent.getID() == TLcdStatusEvent.MESSAGE &&
        message != null &&
        !message.isEmpty() &&
        !fLucyEnv.getWorkspaceManager().isDecodingWorkspace()) {
      TLcdAWTUtil.invokeNowOrLater(new Runnable() {
        @Override
        public void run() {
          showMessage(message, aStatusEvent);
        }
      });
    }
  }

  /**
   * <p>
   *   Shows the specified message in a pop-up on the active map.
   *   The pop-up can be closed by the user, or will automatically disappear of some time.
   * </p>
   *
   * <p>
   *   This method can be called multiple times in a row.
   *   When the previous message is still being shown on the map, the next message will appear
   *   next to the previous message to ensure that they are both readable.
   * </p>
   *
   * <p>
   *   This method must be called on the EDT.
   * </p>
   *
   * @param aMessage The message to show
   */
  private void showMessage(String aMessage, final TLcdStatusEvent aStatusEvent) {
    ILcyGenericMapComponent activeMapComponent = fLucyEnv.getCombinedMapManager().getActiveMapComponent();
    if (activeMapComponent == null) {
      return;
    }
    Component mapComponentComponent = activeMapComponent.getComponent();
    Frame parentFrame = TLcdAWTUtil.findParentFrame(mapComponentComponent);

    if (parentFrame == null || !parentFrame.isShowing()) {
      return;
    }

    final JWindow window = new JWindow(parentFrame);

    JEditorPane editorPane = new JEditorPane();
    editorPane.setContentType("text/html");
    editorPane.setEditable(false);
    editorPane.setEditorKit(new HTMLWordWrappingEditorKit());
    editorPane.setText(aMessage);
    editorPane.setOpaque(false);
    editorPane.setBackground(new Color(0, 0, 0, 0)); // fully transparent
    editorPane.setFocusable(false); //avoid blinking cursor
    editorPane.setSize(MESSAGE_WINDOW_WIDTH, 10000);

    editorPane.addHyperlinkListener(new HyperlinkListener() {
      @Override
      public void hyperlinkUpdate(HyperlinkEvent e) {
        HyperlinkEvent.EventType eventType = e.getEventType();
        if (eventType == HyperlinkEvent.EventType.ACTIVATED) {
          new TLcyCompositeHyperlinkListener(fLucyEnv).hyperlinkUpdate(e);
        }
      }
    });

    Icon closeIcon = (Icon) UIManager.get("InternalFrame.closeIcon");
    if (closeIcon == null) {
      int size = TLcdIconFactory.getDefaultSize().getSize();
      closeIcon = new TLcdSWIcon(new TLcdResizeableIcon(new TLcdSymbol(TLcdSymbol.CROSS, size / 2, Color.WHITE), size, size));
    }

    final JButton closeButton = new JButton(closeIcon);
    closeButton.setBorder(null);

    final Timer autoCloseTimer = new Timer(REMAIN_VISIBLE_TIME_IN_SECONDS * 1000, new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        closeButton.doClick();
      }
    });
    autoCloseTimer.setRepeats(false);
    autoCloseTimer.start();
    editorPane.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseEntered(MouseEvent e) {
        autoCloseTimer.restart();
      }
    });

    JPanel closeButtonPanel = new JPanel(new BorderLayout());
    closeButtonPanel.setOpaque(false);
    closeButtonPanel.add(closeButton, BorderLayout.EAST);

    FormLayout layout = new FormLayout("5px, right:pref, 10px, left:" + MESSAGE_WINDOW_WIDTH + "px, pref, 5px",
                                       "5px, top:pref:grow, 5px");
    window.getContentPane().setLayout(layout);
    CellConstraints cc = new CellConstraints();
    window.getContentPane().add(closeButtonPanel, cc.xy(5, 2));
    window.getContentPane().add(new JLabel(getIcon(aStatusEvent)), cc.xy(2, 2));
    window.getContentPane().add(editorPane, cc.xy(4, 2));

    window.pack();
    final int height = window.getHeight() + VERTICAL_SPACING_BETWEEN_MESSAGES;
    int width = window.getWidth();

    Point location;
    switch (fLocation) {
    case NORTH_WEST:
      location = new Point(BORDER_SPACING, fCurrentYPosition);
      break;
    case NORTH_EAST:
      location = new Point(mapComponentComponent.getWidth() - BORDER_SPACING - width, fCurrentYPosition);
      break;
    case SOUTH_WEST:
      location = new Point(BORDER_SPACING, mapComponentComponent.getHeight() - fCurrentYPosition - window.getHeight());
      break;
    case SOUTH_EAST:
    default:
      location = new Point(mapComponentComponent.getWidth() - BORDER_SPACING - width, mapComponentComponent.getHeight() - fCurrentYPosition - window.getHeight());
      break;
    }

    SwingUtilities.convertPointToScreen(location, mapComponentComponent);
    window.setLocation(location);
    window.setVisible(true);
    fCurrentYPosition += height;

    closeButton.addActionListener(new ActionListener() {
      private boolean fDisposed = false;

      @Override
      public void actionPerformed(ActionEvent e) {
        autoCloseTimer.stop();
        if (!fDisposed) {
          fDisposed = true;
          window.dispose();
          fCurrentYPosition -= height;
        }
      }
    });
  }

  private Icon getInfoIcon() {
    return UIManager.getIcon("OptionPane.informationIcon");
  }

  private Icon getIcon(TLcdStatusEvent aStatusEvent) {
    TLcdStatusEvent.Severity severity = aStatusEvent.getSeverity();
    switch (severity) {
    case ERROR:
      return UIManager.getIcon("OptionPane.errorIcon");
    case WARNING:
      return UIManager.getIcon("OptionPane.warningIcon");
    case INFO:
      return getInfoIcon();
    default:
      return getInfoIcon();
    }
  }

  private enum Location {
    NORTH_EAST,
    NORTH_WEST,
    SOUTH_EAST,
    SOUTH_WEST
  }

}

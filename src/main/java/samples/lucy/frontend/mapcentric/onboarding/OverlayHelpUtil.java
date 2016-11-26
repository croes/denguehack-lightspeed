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
package samples.lucy.frontend.mapcentric.onboarding;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.HierarchyBoundsListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.RootPaneContainer;
import javax.swing.SwingUtilities;

import com.luciad.gui.ALcdAction;
import com.luciad.gui.TLcdAWTUtil;
import com.luciad.gui.swing.TLcdOverlayLayout;
import com.luciad.gui.swing.TLcdSWAction;
import com.luciad.io.TLcdInputStreamFactory;
import com.luciad.lucy.ILcyLucyEnv;

/**
 * Class containing utility methods to show an overlay help message
 */
public final class OverlayHelpUtil {

  private OverlayHelpUtil() {
  }

  private static final double IMAGE_DPI = 192; //double of the standard 96 dpi, a compromise between file size and resolution

  public static void showOverlayHelp(ILcyLucyEnv aLucyEnv, final String aPath, final String aButtonText) {
    final Window mainWindow = TLcdAWTUtil.findParentWindow(aLucyEnv.getTopLevelComponent(0));
    if (mainWindow.isShowing() && mainWindow instanceof RootPaneContainer) {
      showOverlayHelp(mainWindow, aPath, aButtonText);
    }
  }

  public static void showOverlayHelp(final Window aMainWindow, final String aPath, final String aButtonText) {
    EventQueue.invokeLater(new Runnable() {
      @Override
      public void run() {
        // If there is a modal dialog, that is parented to our main window, it may block user interaction
        // with our help dialog. Handle this corner case by simply not showing the help.
        if (modalDialogParentedTo(aMainWindow)) {
          return;
        }

        // Use a transparent dialog to show the help.
        // Do not use a JWindow, it doesn't work well on Linux. Do not use a glass pane either, that doesn't work
        // with the heavy weight components such as TLspAWTView.
        final JDialog helpWindow = new JDialog(aMainWindow, Dialog.ModalityType.DOCUMENT_MODAL);
        helpWindow.setBackground(new Color(0, 0, 0, 70)); //partially transparent, appears darker on Linux than on Windows.
        helpWindow.setUndecorated(true);
        helpWindow.getRootPane().setWindowDecorationStyle(JRootPane.NONE);
        helpWindow.add(createHelpContent(helpWindow, aPath, aButtonText), BorderLayout.CENTER);
        syncHelpWindowBounds(helpWindow, aMainWindow);
        helpWindow.setVisible(true);
      }
    });
  }

  private static boolean modalDialogParentedTo(Window aMainWindow) {
    for (Window window : Window.getWindows()) {
      if (window.getParent() == aMainWindow &&
          window.isShowing() &&
          window instanceof Dialog &&
          ((Dialog) window).getModalityType() != Dialog.ModalityType.MODELESS) {
        return true;
      }
    }
    return false;
  }

  private static JComponent createHelpContent(final JDialog aHelpWindow, String aPath, String aButtonText) {
    JPanel content = new JPanel(new TLcdOverlayLayout());
    content.setOpaque(false);

    JButton gotItButton = new JButton(new TLcdSWAction(new CloseAction(aHelpWindow, aButtonText)));
    Font normalFont = gotItButton.getFont();
    gotItButton.setFont(normalFont.deriveFont(Font.BOLD, normalFont.getSize() * 3));
    content.add(gotItButton, TLcdOverlayLayout.Location.CENTER);
    aHelpWindow.getRootPane().setDefaultButton(gotItButton);

    for (TLcdOverlayLayout.Location location : TLcdOverlayLayout.Location.values()) {
      // Search for images named NORTH_WEST.png etc. in the configured path. Not using java.io.File to also
      // make it work when the files are in a jar.
      String fileName = aPath + "/" + location.toString() + ".png"; //note that / works on both Windows and Linux.

      try (InputStream in = new TLcdInputStreamFactory().createInputStream(fileName)) {
        BufferedImage image = ImageIO.read(in);
        content.add(new ImagePanel(image), location);
      } catch (IOException e) {
        //ignore, no image for this location
      }
    }

    return content;
  }

  /**
   * Changes in the window size may come through asynchronously, at least on Linux and Mac. Therefore, listen for those
   * events, and update the help window size accordingly.
   */
  private static void syncHelpWindowBounds(final JDialog aHelpWindow, final Window aMainWindow) {
    updateHelpWindowBounds(aHelpWindow, aMainWindow);

    // Note that listening to the bounds of the main window itself doesn't work on Mac. The root pane _does_ send out
    // events when it changes size.
    final JRootPane rootPane = ((RootPaneContainer) aMainWindow).getRootPane();

    final HierarchyBoundsListener boundsListener = new HierarchyBoundsListener() {
      @Override
      public void ancestorMoved(HierarchyEvent e) {
        updateHelpWindowBounds(aHelpWindow, aMainWindow);
      }

      @Override
      public void ancestorResized(HierarchyEvent e) {
        updateHelpWindowBounds(aHelpWindow, aMainWindow);
      }
    };
    rootPane.addHierarchyBoundsListener(boundsListener);

    // Cut the link from main window to help window to avoid a memory leak.
    aHelpWindow.addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosed(WindowEvent e) {
        rootPane.removeHierarchyBoundsListener(boundsListener);
      }
    });
  }

  private static void updateHelpWindowBounds(JDialog aHelpWindowSFCT, Window aMainWindow) {
    // Overlay the help window on top of the content of the main window (excl. its decorations)
    JRootPane rootPane = ((RootPaneContainer) aMainWindow).getRootPane();
    Point onScreen = new Point(0, 0);
    SwingUtilities.convertPointToScreen(onScreen, rootPane);
    aHelpWindowSFCT.setBounds(onScreen.x, onScreen.y, rootPane.getWidth(), rootPane.getHeight());
  }

  private static class CloseAction extends ALcdAction {
    private final JDialog fHelpWindow;

    public CloseAction(JDialog aHelpWindow, String aContent) {
      super(aContent); //not translating this string, the entire on-boarding help is in English.
      fHelpWindow = aHelpWindow;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      fHelpWindow.dispose();
    }
  }

  /**
   * Panel that shows an image. The image is assumed to be at {@link #IMAGE_DPI} dpi, it is up or down scaled to match
   * the current screen dpi.
   */
  private static class ImagePanel extends JPanel {
    private final BufferedImage fImage;

    public ImagePanel(BufferedImage aImage) {
      fImage = aImage;
      double factor = Toolkit.getDefaultToolkit().getScreenResolution() / IMAGE_DPI;
      setPreferredSize(new Dimension((int) (factor * fImage.getWidth()),
                                     (int) (factor * fImage.getHeight())));
      setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {

      super.paintComponent(g);

      Graphics2D graphics2D = (Graphics2D) g.create();
      graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);

      // Stretch the image to take up all space
      graphics2D.drawImage(fImage,
                           0, 0, getWidth(), getHeight(),
                           0, 0, fImage.getWidth(null), fImage.getHeight(null),
                           null);
    }
  }
}

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
package samples.wms.server.config.editor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.IOException;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;

import com.luciad.gui.ILcdDialogManager;
import com.luciad.gui.TLcdUserDialog;
import com.luciad.gui.swing.TLcdDialogManagerSW;
import samples.common.SwingUtil;
import com.luciad.util.logging.ILcdLogger;
import com.luciad.util.logging.TLcdLoggerFactory;
import com.luciad.wms.server.ILcdWMSCapabilitiesDecoder;
import com.luciad.wms.server.model.TLcdWMSCapabilities;
import com.luciad.wms.server.model.TLcdWMSServiceMetaData;

import samples.common.SamplePanel;
import samples.wms.server.config.editor.layer.WMSLayerSelector;
import samples.wms.server.config.xml.WMSCapabilitiesXMLDecoder;

/**
 * The main WMS configuration editor class.
 */
public class MainPanel extends SamplePanel {

  private static ILcdLogger sLogger = TLcdLoggerFactory.getLogger(MainPanel.class.getName());
  private static MainPanel sInstance = null;

  private JFrame fFrame;
  private TLcdWMSCapabilities fCapabilities;
  private String fCapabilitiesSrc = "WMS/editor/default.xml";
  private JTextArea fStatusBar;

  private boolean fModified = false;
  private WMSCapabilitiesValidator fValidator;

  private static final int MENU_OPEN = 1;
  private static final int MENU_SAVE = 2;
  private static final int MENU_SAVE_AS = 3;
  private static final int MENU_QUIT = 4;

  private static final String FRAME_CAPTION = " - Luciad Web Map Server configuration";
  private static final int FRAME_WIDTH = 800;
  private static final int FRAME_HEIGHT = 600;

  public MainPanel() {
    super();
    fFrame = new JFrame();
    fFrame.getContentPane().setPreferredSize(new Dimension(FRAME_WIDTH, FRAME_HEIGHT));
    fFrame.getContentPane().setLayout(new BorderLayout());
    fFrame.setIconImages(SwingUtil.sLuciadFrameImage);
    Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
    fFrame.setLocation((d.width - FRAME_WIDTH) / 2, (d.height - FRAME_HEIGHT) / 2);
    fFrame.pack();
    fFrame.setVisible(true);
    fFrame.addWindowListener(new FrameWindowListener());
  }

  private HelpListener fHelpListener = new HelpListener();

  public MouseMotionListener getHelpMouseListener() {
    return fHelpListener;
  }

  /**
   * Refresh the window's title bar.
   */
  private void updateFrameTitle() {
    fFrame.setTitle(fCapabilitiesSrc + (fModified ? "*" : "") + FRAME_CAPTION);
  }

  /**
   * Load a WMS configuration from the given file.
   * @param aFilename the configuration filename.
   * @return the WMS capabilities.
   */
  private TLcdWMSCapabilities loadConfig(String aFilename) {
    ILcdWMSCapabilitiesDecoder decoder = new WMSCapabilitiesXMLDecoder();
    try {
      TLcdWMSCapabilities caps = (TLcdWMSCapabilities) decoder.decodeWMSCapabilities(aFilename);
      fCapabilitiesSrc = aFilename;
      fValidator.validate(caps);
      return caps;
    } catch (Exception e) {
      JOptionPane.showMessageDialog(fFrame, "Invalid capabilities file. Cannot decode file.", "Cannot decode", JOptionPane.WARNING_MESSAGE);
      sLogger.trace(e.getMessage(), e);
      return fCapabilities;
    }
  }

  /**
   * Build the application's menu bar.
   */
  private void buildMenuBar() {
    JMenuBar bar = new JMenuBar();
    JMenu file = new JMenu("File");

    JMenuItem open = new JMenuItem("Open...");
    open.addActionListener(new MenuActionListener(MENU_OPEN));
    open.setAccelerator(KeyStroke.getKeyStroke("control O"));
    WMSEditorHelp.registerComponent(open, "menu.open");

    JMenuItem save = new JMenuItem("Save");
    save.addActionListener(new MenuActionListener(MENU_SAVE));
    save.setAccelerator(KeyStroke.getKeyStroke("control S"));
    WMSEditorHelp.registerComponent(save, "menu.save");

    JMenuItem saveas = new JMenuItem("Save as...");
    saveas.addActionListener(new MenuActionListener(MENU_SAVE_AS));
    saveas.setAccelerator(KeyStroke.getKeyStroke("control alt S"));
    WMSEditorHelp.registerComponent(saveas, "menu.saveas");

    JMenuItem quit = new JMenuItem("Quit");
    quit.addActionListener(new MenuActionListener(MENU_QUIT));
    quit.setAccelerator(KeyStroke.getKeyStroke("control Q"));
    WMSEditorHelp.registerComponent(quit, "menu.quit");

    file.add(open);
    file.add(save);
    file.add(saveas);
    file.add(quit);
    bar.add(file);

    fFrame.setJMenuBar(bar);
  }

  /**
   * Build the application's main GUI.
   */
  public void createGUI() {
    try {
      WMSEditorHelp.loadHelpData("WMS/editor/help.txt");
    } catch (IOException ioe) {
      sLogger.error("Error loading help data: ", ioe);
    }
    fValidator = new WMSCapabilitiesValidator();
    fCapabilities = loadConfig(fCapabilitiesSrc);
    createOrReplaceEditor();
    fFrame.add(this);
    buildMenuBar();
  }

  private void createOrReplaceEditor() {
    removeAll();
    setLayout(new BorderLayout());

    JTabbedPane tabs = new JTabbedPane();
    WMSEditorHelp.registerComponent(tabs, "tabs");

    WMSEditorPanel editorPanel;
    EditListener listener = new EditListener();

    // Add the layer editor.
    editorPanel = new WMSLayerSelector(fCapabilities);
    editorPanel.addEditListener(listener);
    WMSEditorHelp.registerComponent(editorPanel, "layers");
    tabs.addTab("Layers", editorPanel);

    // Add the map data editor.
    editorPanel = new WMSMapDataEditor(fCapabilities);
    editorPanel.addEditListener(listener);
    WMSEditorHelp.registerComponent(editorPanel, "mapdata");
    tabs.addTab("Map data", editorPanel);

    // Add the service metadata editor.
    editorPanel = new WMSServiceDataEditor((TLcdWMSServiceMetaData) fCapabilities.getWMSServiceMetaData());
    editorPanel.addEditListener(listener);
    WMSEditorHelp.registerComponent(editorPanel, "servicedata");
    tabs.addTab("Service metadata", editorPanel);

    // Add the status bar.
    fStatusBar = new JTextArea();
    fStatusBar.setEditable(false);
    fStatusBar.setBorder(BorderFactory.createLineBorder(Color.black));
    fStatusBar.setText(fValidator.getMessages());
    fStatusBar.setLineWrap(true);
    fStatusBar.setWrapStyleWord(true);
    WMSEditorHelp.registerComponent(fStatusBar, "messagebar");

    JScrollPane scroll = new JScrollPane(
        fStatusBar,
        JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
        JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
    );
    scroll.setPreferredSize(new Dimension(800, 60));
    add(BorderLayout.SOUTH, scroll);
    add(BorderLayout.CENTER, tabs);

    updateUI();
  }

  public void start() {
    updateFrameTitle();
    fFrame.setVisible(true);
  }

  public TLcdWMSCapabilities getCapabilities() {
    return fCapabilities;
  }

  public static MainPanel get() {
    /* This currently serves only to provide access to the map data folder in
       other parts of the code. */
    return sInstance;
  }

  // Main method
  public static void main(String[] aArgs) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        JPopupMenu.setDefaultLightWeightPopupEnabled(false);
        sInstance = new MainPanel();
        sInstance.init();
        sInstance.start();
      }
    });
  }

  private static class XMLFileFilter extends FileFilter {
    public boolean accept(File f) {
      return (f.isDirectory() || f.getName().toLowerCase().endsWith(".xml"));
    }

    public String getDescription() {
      return "XML files (*.xml)";
    }
  }

  private class MenuActions {
    protected void menuOpen() {
      int save = JOptionPane.YES_OPTION;
      if (fModified) {
        save = JOptionPane.showConfirmDialog(
            fFrame,
            "The configuration file has been modified. Do you want to save the changes?",
            "Save changes?",
            JOptionPane.YES_NO_CANCEL_OPTION,
            JOptionPane.WARNING_MESSAGE
        );
        if (save == JOptionPane.YES_OPTION) {
          menuSave();
        }
      }

      if (save != JOptionPane.CANCEL_OPTION) {
        // Pop up a file chooser and open the selected configuration file.
        JFileChooser fileChooser = new JFileChooser("./");
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setFileFilter(new XMLFileFilter());
        int returnValue = fileChooser.showOpenDialog(fFrame);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
          fCapabilities = loadConfig(fileChooser.getSelectedFile().getAbsolutePath());
          fModified = false;
          updateFrameTitle();
          createOrReplaceEditor();
        }
      }
    }

    protected void menuSave() {
      // Check whether the capabilities are valid.
      fValidator.validate(fCapabilities);
      String messages = fValidator.getMessages();
      fStatusBar.setText(messages);
      if (messages.length() > 0) {
        TLcdUserDialog.setDialogManager(new TLcdDialogManagerSW());
        int confirm = TLcdUserDialog.confirm("The capabilities are not valid: there are still warning messages. Save anyway?", ILcdDialogManager.YES_NO_OPTION, ILcdDialogManager.WARNING_MESSAGE, this, fFrame);
        if (confirm == ILcdDialogManager.YES_OPTION) {
          save();
        }
      } else {
        save();
      }
    }

    protected void save() {
      // Save the current configuration file to disk.
      WMSCapabilitiesXMLEncoder enc = new WMSCapabilitiesXMLEncoder();
      try {
        enc.save(fCapabilities, fCapabilitiesSrc);
        fModified = false;
        updateFrameTitle();
      } catch (IOException e1) {
        sLogger.error("Error while saving", e1);
        /* If anything goes wrong (e.g. the file is read-only), ask the user to
           save to a different filename. */
        int saveas = JOptionPane.showConfirmDialog(
            fFrame,
            "The configuration could not be saved to " + fCapabilitiesSrc +
            ". Do you want to save to a different filename?",
            "Error",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.ERROR_MESSAGE
        );
        if (saveas == JOptionPane.YES_OPTION) {
          menuSaveAs();
        }
      }
    }

    protected void menuSaveAs() {
      // Pop up a file chooser and save the configuration to the chosen filename.
      JFileChooser fileChooser = new JFileChooser("./");
      fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
      int returnValue = fileChooser.showSaveDialog(fFrame);
      if (returnValue == JFileChooser.APPROVE_OPTION) {
        fCapabilitiesSrc = fileChooser.getSelectedFile().getAbsolutePath();
        menuSave();
      }
    }

    protected void menuQuit() {
      // If the configuration was modified, ask the user to save it first.
      int save = JOptionPane.YES_OPTION;
      if (fModified) {
        save = JOptionPane.showConfirmDialog(
            fFrame,
            "The configuration file has been modified. Do you want to save the changes?",
            "Save changes?",
            JOptionPane.YES_NO_CANCEL_OPTION,
            JOptionPane.WARNING_MESSAGE
        );
        if (save == JOptionPane.YES_OPTION) {
          menuSave();
        }
      }
      // When done, quit.
      if (save != JOptionPane.CANCEL_OPTION) {
        System.exit(0);
      }
    }
  }

  /**
   * Action listener for the menu bar.
   */
  private class MenuActionListener extends MenuActions implements ActionListener {

    private int fAction;

    public MenuActionListener(int aAction) {
      fAction = aAction;
    }

    public void actionPerformed(ActionEvent e) {

      switch (fAction) {
      case MENU_OPEN:
        menuOpen();
        break;
      case MENU_SAVE:
        menuSave();
        break;
      case MENU_SAVE_AS:
        menuSaveAs();
        break;
      case MENU_QUIT:
        menuQuit();
        break;
      }
    }
  }

  /**
   * Configuration edit listener.
   */
  private class EditListener implements WMSEditListener {
    public void editPerformed(Object aEditedObject) {
      // Flag the file as modified.
      fModified = true;
      // Validate the contents and display warnings in the status bar.
      fValidator.validate(fCapabilities);
      fStatusBar.setText(fValidator.getMessages());
      // Update the window's title bar.
      updateFrameTitle();
    }
  }

  /**
   * Mouse movement listener for the help system.
   */
  private class HelpListener implements MouseMotionListener {
    public void mouseMoved(MouseEvent e) {

      String msg = fValidator.getMessages();
      if (msg.trim().length() == 0) {
        String help = WMSEditorHelp.getHelpString(e.getComponent());
        if (help != null) {
          fStatusBar.setText(help);
          return;
        }
      }
      fStatusBar.setText(msg);
    }

    public void mouseDragged(MouseEvent e) {
    }
  }

  private class FrameWindowListener extends MenuActions implements WindowListener {

    public void windowOpened(WindowEvent e) {
    }

    public void windowClosing(WindowEvent e) {
      // If the configuration was modified, ask the user to save it first.
      int save = JOptionPane.YES_OPTION;
      if (fModified) {
        save = JOptionPane.showConfirmDialog(
            fFrame,
            "The configuration file has been modified. Do you want to save the changes?",
            "Save changes?",
            JOptionPane.YES_NO_CANCEL_OPTION,
            JOptionPane.WARNING_MESSAGE
        );
        if (save == JOptionPane.YES_OPTION) {
          menuSave();
        }
      }
      // When done, quit.
      if (save != JOptionPane.CANCEL_OPTION) {
        System.exit(0);
      }
    }

    public void windowClosed(WindowEvent e) {
    }

    public void windowIconified(WindowEvent e) {
    }

    public void windowDeiconified(WindowEvent e) {
    }

    public void windowActivated(WindowEvent e) {
    }

    public void windowDeactivated(WindowEvent e) {
    }
  }
}

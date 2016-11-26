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

import java.awt.EventQueue;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.UIManager;
import javax.swing.plaf.metal.MetalLookAndFeel;

import com.luciad.gui.TLcdAWTUtil;
import com.luciad.gui.TLcdIconFactory;
import com.luciad.util.TLcdLicenseError;
import com.luciad.util.TLcdSystemPropertiesUtil;
import com.luciad.util.logging.ILcdLogger;
import com.luciad.util.logging.TLcdLoggerFactory;
import com.luciad.view.map.TLcdAdvancedMapRulerController;

import samples.common.gui.blacklime.BlackLimeLookAndFeel;

/**
 * Abstract panel for Luciad Swing samples, offering the following functionality:
 * - it performs a license check
 * - it sets the native look-and-feel on Windows systems
 * - it can start the sample and display it in a JFrame
 */
public abstract class SamplePanel extends JPanel {

  private static final ILcdLogger LOGGER = TLcdLoggerFactory.getLogger(SamplePanel.class);

  /**
   * Call this in your sample's static initializer to get the {@link BlackLimeLookAndFeel} and white icons.
   */
  public static void useBlackLime() {
    TLcdAWTUtil.invokeAndWait(new Runnable() {
      @Override
      public void run() {
        BlackLimeLookAndFeel.install(true);
        TLcdIconFactory.setDefaultTheme(TLcdIconFactory.Theme.WHITE_THEME);
      }
    });
  }

  static {
    if (TLcdSystemPropertiesUtil.isWindows()) {
      try {
        // Use the System look and feel on Windows, unless someone else has already changed it.
        // The touch samples for example use a touch specific look & feel.
        if (UIManager.getLookAndFeel() instanceof MetalLookAndFeel) {
          UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }
      } catch (Exception aException) {
        System.out.println("Error while setting the native LookAndFeel: " + aException.getMessage());
      }
    } else {
      // Java does not work well with some modern Linux Window managers.
      XWindowUtil.workAroundWindowManagerProblems();
    }
    // Work around various OS X specific bugs
    MacUtil.installWorkarounds();
    MacUtil.initMacSystemProperties();
  }

  private List<String> fErrorLines = new ArrayList<>();

  /**
   * Starts the given SamplePanel class by showing it in a JFrame.
   *
   * @param aClass The panel to instantiate, must have a default constructor
   * @param aTitle The sample title
   */
  protected static void startSample(final Class<? extends SamplePanel> aClass, final String aTitle) {
    startSample(aClass, null, aTitle);
  }

  /**
   * Starts the given SamplePanel class by showing it in a JFrame and passing it the given arguments.
   *
   * @param aClass The panel to instantiate, must have a constructor accepting {@code aArgs}
   * @param aArgs  The sample arguments
   * @param aTitle The sample title
   */
  protected static void startSample(final Class<? extends SamplePanel> aClass, final String[] aArgs, final String aTitle) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        try {
          if (aArgs == null) {
            new LuciadFrame(aClass.newInstance(), aTitle);
          } else {
            Constructor<? extends SamplePanel> constructor = aClass.getConstructor(String[].class);
            new LuciadFrame(constructor.newInstance((Object) aArgs), aTitle);
          }
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
          throw new IllegalStateException(e);
        }
      }
    });
  }

  public SamplePanel() {
    performChecks();
  }

  private void performChecks() {
    if (!EventQueue.isDispatchThread()) {
      throw new RuntimeException("Swing classes need to be instantiated on the Event Dispatch Thread (EDT)");
    }
    if (!isJavaOk()) {
      fErrorLines.addAll(Arrays.asList("Java error", "This sample requires JRE " + getRequiredJavaVersion() + " or higher."));
    }
    try {
      triggerClass();
    } catch (TLcdLicenseError aError) {
      fErrorLines.add("License error");
      // license error -> show message in init
      String message = aError.getMessage();
      if (message != null) {
        int endOfFirstSentence = message.indexOf('.') + 1;
        fErrorLines.add(message.substring(0, endOfFirstSentence));
        String content = message.substring(endOfFirstSentence);
        fErrorLines.addAll(Arrays.asList(content.split("\n")));
      }
      fErrorLines.add("Please check if the correct license is in your class path.");
    } catch (UnsatisfiedLinkError e) {
      e.printStackTrace();
      fErrorLines.addAll(Arrays.asList(DefaultExceptionHandler.UNSATISFIED_LINK_ERROR_MESSAGE.split("\n")));
      fErrorLines.add(e.getMessage());
    }
  }

  protected void triggerClass() {
    new TLcdAdvancedMapRulerController();
  }

  public final void init() {
    TLcdAWTUtil.invokeAndWait(new Runnable() {
      public void run() {
        if (!fErrorLines.isEmpty()) {
          showError(fErrorLines);
        } else {
          installUncaughtExceptionHandler();
          createGUI();
          loadData();
        }
      }
    });
  }

  private void loadData() {
    try {
      addData();
    } catch (final Exception e) {
      LOGGER.error("Exception while loading the sample data.", e);
      StringBuilder message = new StringBuilder();
      message.append("The following error occurred while loading the sample data.\r\n").append(e.getMessage()).append("\n");
      if (e instanceof FileNotFoundException) {
        message.append("Please make sure you have unzipped the \"Worlddata.zip\" package.");
      }
      JOptionPane.showMessageDialog(SamplePanel.this, message.toString(), "Error while loading data", JOptionPane.ERROR_MESSAGE);
    }
  }


  protected void tearDown() {
  }


  /**
   * Builds the sample GUI data.
   */
  protected void createGUI() {
  }

  /**
   * Loads the sample data, this method is called from a different Thread.
   * By default, no data is loaded.
   * @throws IOException typically thrown if a file cannot be found or opened
   */
  protected void addData() throws IOException {
  }

  protected boolean isJavaOk() {
    return TLcdSystemPropertiesUtil.isJava7();
  }

  protected String getRequiredJavaVersion() {
    return "1.7";
  }

  private void installUncaughtExceptionHandler() {
    //Show a popup dialog when an uncaught exception occurs.
    try {
      Thread.setDefaultUncaughtExceptionHandler(new DefaultExceptionHandler());
    } catch (SecurityException ignored) {
      // give up if we don't have permission
    }
  }

  protected void showError(List<String> aErrorLines) {
    JLabel title = new JLabel(aErrorLines.get(0));
    title.setFont(title.getFont().deriveFont(Font.BOLD, 16));

    JPanel errorPanel = new JPanel(new GridLayout(aErrorLines.size() - 1, 1));
    errorPanel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
    for (int i = 1; i < aErrorLines.size(); i++) {
      JLabel error = new JLabel(aErrorLines.get(i));
      error.setFont(error.getFont().deriveFont(Font.PLAIN, 12));
      errorPanel.add(error);
    }

    setLayout(new GridBagLayout());
    setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    GridBagConstraints gbc = new GridBagConstraints(
        0, 0, 1, 1, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0
    );
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    add(title, gbc);
    gbc.gridwidth = 1;

    // add white space
    gbc.gridy++;
    add(Box.createVerticalStrut(5), gbc);

    // show separator line
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.gridy++;
    add(new JSeparator(JSeparator.HORIZONTAL), gbc);
    gbc.gridwidth = 1;
    gbc.fill = GridBagConstraints.NONE;

    // add white space
    gbc.gridy++;
    add(Box.createVerticalStrut(5), gbc);

    // add error message
    gbc.gridy++;
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    add(errorPanel, gbc);
    gbc.gridwidth = 1;

  }
}

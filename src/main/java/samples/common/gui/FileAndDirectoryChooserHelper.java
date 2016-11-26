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
package samples.common.gui;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.concurrent.Executor;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;

import com.luciad.util.ILcdFilter;
import com.luciad.util.ILcdStringTranslator;

/**
 * Adds the capability to open directories (for example DAFIF or DTED directories) using
 * an {@linkplain JFileChooser#setAccessory(JComponent) accessory}.
 *
 * It's recommended to use FileAndDirectoryChooser to integrate this with a JFileChooser.
 */
public abstract class FileAndDirectoryChooserHelper {

  private boolean fDirectoriesEnabled = false;
  private File fCurrentDirectory = null;
  private File fSelectedDirectory = null;
  private ILcdFilter<File> fDirectoryFilter = null;

  private final JComponent fContent;
  private final JFileChooser fFileChooser;

  public FileAndDirectoryChooserHelper(final JFileChooser aFileChooser, ILcdStringTranslator aStringTranslator) {
    fFileChooser = aFileChooser;
    // Add label + 'Open folder' button in the preview area
    JButton button = new JButton(aStringTranslator.translate("Open folder"));

    String string = aStringTranslator.translate("Current folder is recognized. You can load it as a whole.");
    int width = (int) (button.getPreferredSize().width * 0.75);// Make sure the text is wrapped properly
    JComponent label = new JLabel("<html><body style='width: " + width + "px'>" + string + "<html>");

    button.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        fSelectedDirectory = fCurrentDirectory;
        aFileChooser.cancelSelection();
      }
    });
    fContent = Box.createVerticalBox();
    fContent.add(label);
    fContent.add(Box.createVerticalStrut(10));
    fContent.add(button);
    fContent.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
    fContent.setVisible(false);
    JComponent accessory = new AnimatedResizingPanel(new BorderLayout());
    accessory.add(fContent, BorderLayout.EAST);

    aFileChooser.setAccessory(accessory);
    aFileChooser.addPropertyChangeListener(new PropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent evt) {
        if (JFileChooser.DIRECTORY_CHANGED_PROPERTY.equals(evt.getPropertyName())) {
          update(aFileChooser);
        }
      }
    });
    button.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        fSelectedDirectory = fCurrentDirectory;
        aFileChooser.cancelSelection();
      }
    });
  }

  public void update(JFileChooser aFileChooser) {
    if (isDirectoriesEnabled()) {
      final File currentDirectory = aFileChooser.getCurrentDirectory();
      getBackgroundExecutor().execute(new Runnable() {
        @Override
        public void run() {
          final boolean visible = fDirectoryFilter == null || fDirectoryFilter.accept(currentDirectory);
          EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
              fContent.setVisible(visible);
              fCurrentDirectory = visible ? currentDirectory : null;
              fSelectedDirectory = null;
            }
          });
        }
      });
    } else {
      fContent.setVisible(false);
      fCurrentDirectory = null;
      fSelectedDirectory = null;
    }
  }

  protected abstract Executor getBackgroundExecutor();

  public void reset(boolean aDirectoriesEnabled, ILcdFilter<File> aDirectoryFilter) {
    fDirectoriesEnabled = aDirectoriesEnabled;
    fDirectoryFilter = aDirectoryFilter;
    fSelectedDirectory = null;
    update(fFileChooser);
  }

  public boolean isDirectoriesEnabled() {
    return fDirectoriesEnabled;
  }

  public File getSelectedDirectory() {
    return fSelectedDirectory;
  }

  public ILcdFilter<File> getDirectoryFilter() {
    return fDirectoryFilter;
  }

  public JComponent getContent() {
    return fContent;
  }

}

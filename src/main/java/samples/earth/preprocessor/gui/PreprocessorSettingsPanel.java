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
package samples.earth.preprocessor.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * A panel for configuring the preprocessor.
 */
public class PreprocessorSettingsPanel extends JPanel implements PreprocessorSettings, PreprocessorSettingsProvider {

  private JComboBox fCoverageComboBox;
  private static final String TEXTURE_COVERAGE = "Texture";
  private static final String ELEVATION_COVERAGE = "Elevation";

  private JSpinner fTexOversampleSpinner;
  private JCheckBox fTexBackgroundTransparentCheckBox;

  private JTextField fRepositoryTextField;
  private JButton fRepositorySelectButton;

  public static final String REPOSITORY_PROPERTY = "repository";
  public static final String TEXTURE_OVERSAMPLING_FACTOR_PROPERTY = "textureOversamplingFactor";
  public static final String IS_TEXTURE_COVERAGE_SELECTED_PROPERTY = "isTextureCoverageSelected";
  public static final String IS_ELEVATION_COVERAGE_SELECTED_PROPERTY = "isElevationCoverageSelected";

  public static final String IS_VALID_SETTINGS_PROPERTY = "isValidSettings";

  public PreprocessorSettingsPanel() {
    fCoverageComboBox = new JComboBox(new Object[]{TEXTURE_COVERAGE, ELEVATION_COVERAGE});
    ActionListener textureSettingsUpdater = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        updateTextureSettingsState();
      }
    };
    fCoverageComboBox.addActionListener(textureSettingsUpdater);

    fTexOversampleSpinner = new JSpinner(new SpinnerNumberModel(2, 1, 10, 1));
    fTexBackgroundTransparentCheckBox = new JCheckBox();
    fTexBackgroundTransparentCheckBox.setSelected(true);

    fRepositoryTextField = new JTextField();
    fRepositoryTextField.setColumns(24);
    fRepositorySelectButton = new JButton("...");
    int defaultH = (int) fRepositorySelectButton.getPreferredSize().getHeight();
    fRepositorySelectButton.setPreferredSize(new Dimension(20, defaultH));
    fRepositorySelectButton.setMinimumSize(new Dimension(20, defaultH));
    fRepositorySelectButton.setToolTipText("Select a repository");
    fRepositorySelectButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        selectRepository();
      }
    });

    setLayout(new GridBagLayout());

    add(new JLabel("Coverage"), new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.LAST_LINE_START, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
    add(fCoverageComboBox, new GridBagConstraints(1, 0, 2, 1, 0, 0, GridBagConstraints.LAST_LINE_START, GridBagConstraints.NONE, new Insets(0, 5, 5, 5), 0, 0));

    add(new JLabel("Texture oversampling"), new GridBagConstraints(0, 1, 1, 1, 0, 0, GridBagConstraints.LAST_LINE_START, GridBagConstraints.NONE, new Insets(0, 5, 5, 5), 0, 0));
    add(fTexOversampleSpinner, new GridBagConstraints(1, 1, 2, 1, 0, 0, GridBagConstraints.LAST_LINE_START, GridBagConstraints.NONE, new Insets(0, 5, 5, 5), 0, 0));
    add(new JLabel("Texture transparency"), new GridBagConstraints(0, 2, 1, 1, 0, 0, GridBagConstraints.LAST_LINE_START, GridBagConstraints.NONE, new Insets(0, 5, 5, 5), 0, 0));
    add(fTexBackgroundTransparentCheckBox, new GridBagConstraints(1, 2, 2, 1, 0, 0, GridBagConstraints.LAST_LINE_START, GridBagConstraints.NONE, new Insets(0, 5, 5, 5), 0, 0));

    add(new JLabel("Repository"), new GridBagConstraints(0, 3, 1, 1, 0, 0, GridBagConstraints.LAST_LINE_START, GridBagConstraints.NONE, new Insets(0, 5, 5, 5), 0, 0));
    add(fRepositoryTextField, new GridBagConstraints(1, 3, 1, 1, 0, 0, GridBagConstraints.LAST_LINE_START, GridBagConstraints.HORIZONTAL, new Insets(0, 5, 5, 0), 0, 0));
    add(fRepositorySelectButton, new GridBagConstraints(2, 3, 1, 1, 0, 0, GridBagConstraints.LAST_LINE_START, GridBagConstraints.NONE, new Insets(0, 5, 5, 5), 0, 0));

    setMaximumSize(getPreferredSize());

    // Listen to changes of the settings
    fRepositoryTextField.getDocument().addDocumentListener(new DocumentListener() {
      private String fOldRepository = "";

      public void insertUpdate(DocumentEvent e) {
        repositoryChanged();
      }

      public void removeUpdate(DocumentEvent e) {
        repositoryChanged();
      }

      public void changedUpdate(DocumentEvent e) {
        repositoryChanged();
      }

      private void repositoryChanged() {
        String oldRepository = fOldRepository;
        fOldRepository = getRepository();
        firePropertyChange(REPOSITORY_PROPERTY, oldRepository, fOldRepository);
        fRepositoryTextField.getInputVerifier().verify(fRepositoryTextField);
      }
    });
    fRepositoryTextField.setInputVerifier(new InputVerifier() {

      public boolean verify(JComponent input) {
        boolean isValid = isRepositoryValid();
        fRepositoryTextField.setForeground(isValid ? Color.BLACK : Color.RED);
        return isValid;
      }
    });
    fTexOversampleSpinner.addChangeListener(new ChangeListener() {
      private int fOldValue = getTextureOversamplingFactor();

      public void stateChanged(ChangeEvent e) {
        int oldValue = fOldValue;
        fOldValue = getTextureOversamplingFactor();
        firePropertyChange(TEXTURE_OVERSAMPLING_FACTOR_PROPERTY, oldValue, fOldValue);
      }
    });
    fCoverageComboBox.addItemListener(new ItemListener() {
      private boolean fOldTextureValue = isTextureCoverageSelected();
      private boolean fOldElevationValue = isElevationCoverageSelected();

      @Override
      public void itemStateChanged(ItemEvent e) {
        boolean oldTextureValue = fOldTextureValue;
        fOldTextureValue = isTextureCoverageSelected();
        firePropertyChange(IS_TEXTURE_COVERAGE_SELECTED_PROPERTY, oldTextureValue, fOldTextureValue);
        boolean oldElevationValue = fOldElevationValue;
        fOldElevationValue = isElevationCoverageSelected();
        firePropertyChange(IS_ELEVATION_COVERAGE_SELECTED_PROPERTY, oldElevationValue, fOldElevationValue);
      }
    });

    addPropertyChangeListener(new PropertyChangeListener() {
      private boolean fOldValue = isValidSettings();

      public void propertyChange(PropertyChangeEvent evt) {
        boolean oldValue = fOldValue;
        fOldValue = isValidSettings();
        firePropertyChange(IS_VALID_SETTINGS_PROPERTY, oldValue, fOldValue);
      }
    });
    addPropertyChangeListener(new PropertyChangeListener() {
      private boolean fOldValue = hasPreprocessorSettings();

      public void propertyChange(PropertyChangeEvent evt) {
        boolean oldValue = fOldValue;
        fOldValue = hasPreprocessorSettings();
        firePropertyChange(HAS_PREPROCESSOR_SETTINGS_PROPERTY, oldValue, fOldValue);
      }
    });

    updateTextureSettingsState();
  }

  public boolean hasPreprocessorSettings() {
    return isValidSettings();
  }

  public PreprocessorSettings getPreprocessorSettings() {
    return this;
  }

  public boolean isValidSettings() {
    return isRepositoryValid() &&
           (isTextureCoverageSelected() ||
            isElevationCoverageSelected());
  }

  private boolean isRepositoryValid() {
    String repository = getRepository();
    if (repository == null) {
      return false;
    }
    repository = repository.trim();
    if (repository.length() == 0) {
      return false;
    }
    File file = new File(repository);
    if (!file.isDirectory() || !file.exists()) {
      return false;
    }

    return true;
  }

  public String getRepository() {
    return fRepositoryTextField.getText();
  }

  public int getTextureOversamplingFactor() {
    return (Integer) fTexOversampleSpinner.getValue();
  }

  public Color getTextureBackgroundColor() {
    return fTexBackgroundTransparentCheckBox.isSelected() ? new Color(0, 0, 0, 0) : Color.BLACK;
  }

  public boolean isTextureCoverageSelected() {
    return TEXTURE_COVERAGE.equals(fCoverageComboBox.getSelectedItem());
  }

  public boolean isElevationCoverageSelected() {
    return ELEVATION_COVERAGE.equals(fCoverageComboBox.getSelectedItem());
  }

  public void setRepository(String aRepositoryDirectory) {
    fRepositoryTextField.setText(aRepositoryDirectory);
  }

  private void selectRepository() {
    JFileChooser chooser = new JFileChooser(".");
    File currRepo = new File(fRepositoryTextField.getText());
    if (currRepo.exists()) {
      chooser.setCurrentDirectory(currRepo);
    }
    chooser.setMultiSelectionEnabled(false);
    chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
    chooser.setFileFilter(new TileRepositoryFileFilter());

    int choice = chooser.showOpenDialog(this);
    if (choice != JFileChooser.APPROVE_OPTION) {
      return;
    }

    File selectedFile = chooser.getSelectedFile();
    if (!selectedFile.isDirectory()) {
      selectedFile = selectedFile.getParentFile();
    }

    try {
      fRepositoryTextField.setText(selectedFile.getCanonicalPath());
    } catch (IOException e) {
      // ignore
    }
  }

  private void updateTextureSettingsState() {
    boolean settingsEnabled = isTextureCoverageSelected();
    fTexOversampleSpinner.setEnabled(settingsEnabled);
    fTexBackgroundTransparentCheckBox.setEnabled(settingsEnabled);
  }

  public void setActive(boolean aActive) {
    fCoverageComboBox.setEnabled(aActive);
    updateTextureSettingsState();

    fRepositoryTextField.setEditable(aActive);
    fRepositorySelectButton.setEnabled(aActive);
  }

}

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

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Enumeration;

import javax.imageio.ImageIO;
import javax.swing.*;

import com.luciad.earth.metadata.ILcdEarthAsset;
import com.luciad.earth.metadata.preprocessor.TLcdEarthMetadataTerrainTileSet;
import com.luciad.earth.metadata.preprocessor.TLcdEarthTileRepositoryPreprocessor;
import com.luciad.earth.metadata.preprocessor.combiner.TLcdEarthElevationDataCombiner;
import com.luciad.earth.metadata.preprocessor.combiner.TLcdEarthImageCombiner;
import com.luciad.earth.metadata.preprocessor.combiner.TLcdEarthTerrainTileVertexArrayCombiner;
import com.luciad.earth.model.ILcdEarthAssetBasedModelDescriptor;
import com.luciad.earth.model.TLcdEarthRepositoryModelDecoder;
import com.luciad.earth.model.TLcdEarthRepositoryModelFactory;
import com.luciad.earth.repository.TLcdEarthTileRepository;
import com.luciad.earth.repository.codec.TLcdEarthElevationDataTileDataCodec;
import com.luciad.earth.repository.codec.TLcdEarthImageTileDataCodec;
import com.luciad.earth.repository.codec.TLcdEarthSeparateAlphaImageTileDataCodec;
import com.luciad.earth.tileset.ALcdEarthTile;
import com.luciad.earth.tileset.ILcdEarthEditableTileSet;
import com.luciad.earth.tileset.ILcdEarthTileSetCoverage;
import com.luciad.earth.tileset.TLcdEarthTileFormat;
import com.luciad.earth.tileset.datatype.TLcdEarthElevationData;
import com.luciad.earth.tileset.util.ALcdEarthTileSetWrapper;
import com.luciad.gui.swing.TLcdMemoryCheckPanel;
import com.luciad.model.ILcdIntegerIndexedModel;
import com.luciad.model.ILcdModel;
import com.luciad.model.TLcd2DBoundsIndexedModel;
import com.luciad.reference.ILcdGeoReference;
import com.luciad.reference.TLcdGeodeticReference;
import com.luciad.shape.ILcdBounds;
import com.luciad.shape.shape2D.TLcdLonLatBounds;
import com.luciad.shape.shape3D.ILcd3DEditableBounds;
import com.luciad.shape.shape3D.TLcdXYZBounds;
import com.luciad.transformation.TLcdGeoReference2GeoReference;
import com.luciad.util.TLcdNoBoundsException;
import com.luciad.view.gxy.ILcdGXYLayerFactory;
import com.luciad.view.gxy.ILcdGXYView;

import samples.earth.common.RasterLayerFactory;

/**
 * A panel for configuring and executing the preprocessor.
 */
public class PreprocessorPanel extends JPanel {

  public static final String IS_PREPROCESSING_PROPERTY = "isPreprocessing";
  public static final String CURRENT_ASSET_PROPERTY = "currentAsset";

  private JProgressBar fProgressBar;

  private JButton fStartButton;
  private JButton fStopButton;

  private ImageLabel fImageLabel;

  private ElapsedTimeLabel fElapsedTimeLabel;
  private TimeLabel fLeftTimeLabel;
  private ProgressLabel fLevelProgressLabel;

  private AssetLabel fAssetNameLabel;
  private ProgressLabel fAssetProgressLabel;

  private ILcdGXYView fView;
  private MetadataProvider fMetadataProvider;
  private PreprocessorSettingsProvider fSettingsProvider;

  private boolean fPreprocessing;
  private PreprocessorProgressListener fCurrProgressListener;
  private ILcdEarthAsset fCurrentAsset;

  /**
   * Creates a new preprocessor panel that uses the given view for visualizing preprocessing
   * progress and given provider to retrieve the metadata and settings for preprocessing.
   *
   * @param aView                         The view that should be used for visualizing the
   *                                      progress.
   * @param aMetadataProvider             The metadata provider to retrieve the metadata for
   *                                      preprocessing from.
   * @param aPreprocessorSettingsProvider The settings provider for the preprocessing.
   */
  public PreprocessorPanel(ILcdGXYView aView,
                           MetadataProvider aMetadataProvider,
                           PreprocessorSettingsProvider aPreprocessorSettingsProvider) {
    fView = aView;
    fMetadataProvider = aMetadataProvider;
    fSettingsProvider = aPreprocessorSettingsProvider;
    fPreprocessing = false;

    fStartButton = new JButton("Start");
    fStartButton.setToolTipText("Start preprocessing the selected metadata");
    fStartButton.setEnabled(false);
    fStartButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        startPreprocessing();
      }
    });
    fStopButton = new JButton("Stop");
    fStopButton.setToolTipText("Stops preprocessing");
    fStopButton.setEnabled(false);
    fStopButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        stopPreprocessing();
      }
    });

    fElapsedTimeLabel = new ElapsedTimeLabel();
    fLeftTimeLabel = new TimeLabel();
    fLevelProgressLabel = new ProgressLabel();
    fLevelProgressLabel.setToolTipText("The number of levels done");

    fAssetNameLabel = new AssetLabel();
    fAssetNameLabel.setEnabled(false);
    fAssetNameLabel.setToolTipText("The name of the asset currently being processed");

    fAssetProgressLabel = new ProgressLabel();
    fAssetProgressLabel.setToolTipText("The number of tiles done for the current asset at the current level");

    fImageLabel = new ImageLabel(128);

    JPanel southPanel = new JPanel();
    southPanel.setLayout(new GridBagLayout());
    GridBagConstraints gbc;

    gbc = new GridBagConstraints();
    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.gridwidth = 4;
    JPanel startStopPanel = new JPanel();
    startStopPanel.setLayout(new FlowLayout());
    startStopPanel.add(fStartButton);
    startStopPanel.add(fStopButton);
    southPanel.add(startStopPanel, gbc);

    gbc = new GridBagConstraints();
    gbc.gridx = 0;
    gbc.gridy = 1;
    gbc.gridheight = 7;
    gbc.insets.left = 5;
    gbc.insets.right = 5;
    gbc.insets.top = 5;
    gbc.insets.bottom = 5;
    southPanel.add(fImageLabel, gbc);

    gbc = new GridBagConstraints();
    gbc.gridx = 1;
    gbc.gridy = 1;
    gbc.weightx = 1;
    gbc.anchor = GridBagConstraints.WEST;
    gbc.insets.left = 5;
    gbc.insets.bottom = 5;

    {
      JPanel overallPanel = new JPanel();
      overallPanel.setLayout(new GridBagLayout());
      GridBagConstraints gbc2 = new GridBagConstraints();
      gbc2.anchor = GridBagConstraints.WEST;
      gbc2.gridx = 0;
      gbc2.gridy = 0;
      gbc2.gridwidth = 2;
      overallPanel.add(new JLabel("Overall"), gbc2);
      gbc2.gridwidth = 1;
      gbc2.gridy++;
      gbc2.insets.left = 5;
      overallPanel.add(new JLabel("Elapsed: "), gbc2);
      gbc2.gridy++;
      overallPanel.add(new JLabel("Left: "), gbc2);
      gbc2.gridy++;
      overallPanel.add(new JLabel("Levels: "), gbc2);
      gbc2.gridy++;

      gbc2.insets.left = 0;
      gbc2.gridx = 1;
      gbc2.gridy = 1;
      gbc2.anchor = GridBagConstraints.EAST;
      overallPanel.add(fElapsedTimeLabel, gbc2);
      gbc2.gridy++;
      overallPanel.add(fLeftTimeLabel, gbc2);
      gbc2.gridy++;
      gbc2.anchor = GridBagConstraints.WEST;
      overallPanel.add(fLevelProgressLabel, gbc2);
      gbc2.gridy++;

      gbc2.gridx = 2;
      gbc2.gridy = 0;
      gbc2.gridheight = 4;
      gbc2.weightx = 1;
      overallPanel.add(Box.createHorizontalGlue(), gbc2);

      southPanel.add(overallPanel, gbc);
      gbc.gridy++;
    }
    {
      JPanel assetPanel = new JPanel();
      assetPanel.setLayout(new GridBagLayout());
      GridBagConstraints gbc2 = new GridBagConstraints();
      gbc2.anchor = GridBagConstraints.WEST;
      gbc2.gridx = 0;
      gbc2.gridy = 0;
      gbc2.gridwidth = 2;
      assetPanel.add(new JLabel("Current asset"), gbc2);
      gbc2.gridwidth = 1;
      gbc2.gridy++;
      gbc2.insets.left = 5;
      assetPanel.add(new JLabel("Name: "), gbc2);
      gbc2.gridy++;
      assetPanel.add(new JLabel("Tiles: "), gbc2);
      gbc2.gridy++;

      gbc2.insets.left = 0;
      gbc2.gridx = 1;
      gbc2.gridy = 1;
      assetPanel.add(fAssetNameLabel, gbc2);
      gbc2.gridy++;
      assetPanel.add(fAssetProgressLabel, gbc2);
      gbc2.gridy++;

      gbc2.gridx = 2;
      gbc2.gridy = 0;
      gbc2.gridheight = 3;
      gbc2.weightx = 1;
      assetPanel.add(Box.createHorizontalGlue(), gbc2);

      southPanel.add(assetPanel, gbc);
      gbc.gridy++;
    }

    gbc.weighty = 1;
    southPanel.add(Box.createVerticalGlue(), gbc);
    gbc.weighty = 0;
    gbc.gridy++;

    southPanel.add(new TLcdMemoryCheckPanel(), gbc);

    southPanel.setMaximumSize(southPanel.getPreferredSize());

    fProgressBar = new JProgressBar(JProgressBar.HORIZONTAL);
    fProgressBar.setEnabled(false);
    fProgressBar.setToolTipText("The progress of the metadata preprocessing");

    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    add(fProgressBar);
    add(southPanel);
    add(Box.createVerticalGlue());

    fMetadataProvider.addPropertyChangeListener(new PropertyChangeListener() {

      public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(MetadataProvider.HAS_METADATA_PROPERTY)) {
          updateStartButtonState();
        }
      }

    });
    fSettingsProvider.addPropertyChangeListener(new PropertyChangeListener() {

      public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(PreprocessorSettingsPanel.HAS_PREPROCESSOR_SETTINGS_PROPERTY)) {
          updateStartButtonState();
        }
      }
    });
  }

  private void updateStartButtonState() {
    fStartButton.setEnabled(!isPreprocessing() &&
                            fMetadataProvider.hasMetadata() &&
                            fSettingsProvider.hasPreprocessorSettings());
  }

  private ILcdModel getMetadataModel() {
    return fMetadataProvider.getMetadata();
  }

  private void setCurrentAsset(ILcdEarthAsset aAsset) {
    ILcdEarthAsset oldCurrentAsset = fCurrentAsset;
    fCurrentAsset = aAsset;
    firePropertyChange(CURRENT_ASSET_PROPERTY, oldCurrentAsset, fCurrentAsset);
  }

  public void startPreprocessing() {
    if (isPreprocessing()) {
      return;
    }

    setPreprocessing(true);

    PreprocessorSettings settings = fSettingsProvider.getPreprocessorSettings();
    final String reposDest = settings.getRepository();
    final int texOversampleFactor = settings.getTextureOversamplingFactor();
    final Color texBackgroundColor = settings.getTextureBackgroundColor();
    int tmpCoverages = 0;
    if (settings.isTextureCoverageSelected()) {
      tmpCoverages |= TLcdEarthMetadataTerrainTileSet.TEXTURE_COVERAGE;
    }
    if (settings.isElevationCoverageSelected()) {
      tmpCoverages |= TLcdEarthMetadataTerrainTileSet.ELEVATION_COVERAGE;
    }
    final int coverages = tmpCoverages;

    final ILcdModel assetModel = getMetadataModel();

    Thread preProcessThread = new Thread(new Runnable() {
      public void run() {
        preprocessMetadata(assetModel, reposDest, texOversampleFactor, texBackgroundColor, coverages);

        SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            setPreprocessing(false);
          }
        });
      }
    });
    preProcessThread.setName("Preprocess thread");
    preProcessThread.setDaemon(true);
    preProcessThread.setPriority(Thread.MIN_PRIORITY);
    preProcessThread.start();
  }

  private void stopPreprocessing() {
    fProgressBar.setString("Waiting for preprocessing to stop...");
    if (fCurrProgressListener != null) {
      fCurrProgressListener.stop();
    }
    fStopButton.setEnabled(false);
  }

  private void preprocessMetadata(ILcdModel aAssetModel, String reposDest,
                                  int aTexOversampleFactor, Color aTexBackgroundColor, int aCoverages) {
    final PreprocessorProgressListener progressListener =
        new PreprocessorProgressListener(this);
    fCurrProgressListener = progressListener;

    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        fProgressBar.setModel(new DefaultBoundedRangeModel(0, 0, 0, 1000));
        fProgressBar.setEnabled(true);
        fProgressBar.setStringPainted(true);
        fProgressBar.setString("Initializing...");
        fProgressBar.setIndeterminate(true);

        setCurrentAsset(null);
      }
    });

    // Filter the asset model based on the coverages we are going to process
    TLcd2DBoundsIndexedModel filteredAssetModel = new TLcd2DBoundsIndexedModel(aAssetModel.getModelReference(), aAssetModel.getModelDescriptor());
    Enumeration en = aAssetModel.elements();
    while (en.hasMoreElements()) {
      ILcdEarthAsset asset = (ILcdEarthAsset) en.nextElement();
      if (asset.getCoverageType() == ILcdEarthTileSetCoverage.CoverageType.IMAGE &&
          (aCoverages & TLcdEarthMetadataTerrainTileSet.TEXTURE_COVERAGE) != 0) {
        filteredAssetModel.addElement(asset, ILcdModel.NO_EVENT);
      } else if (asset.getCoverageType() == ILcdEarthTileSetCoverage.CoverageType.ELEVATION &&
                 (aCoverages & TLcdEarthMetadataTerrainTileSet.ELEVATION_COVERAGE) != 0) {
        filteredAssetModel.addElement(asset, ILcdModel.NO_EVENT);
      }
    }

    TLcdEarthMetadataTerrainTileSet tileSet = null;
    TLcdEarthTileRepository repository = null;
    try {
      if (ImageIO.getUseCache()) {
        ImageIO.setUseCache(false);
      }

      TLcdGeodeticReference geodeticRef = new TLcdGeodeticReference();
      ILcdBounds geodeticBounds = new TLcdLonLatBounds(-180, -90, 360, 180);
      ILcdBounds bounds;
      int level0Rows;
      int level0Columns;
      int levels = 24;
      if (aAssetModel.getModelReference().equals(geodeticRef)) {
        bounds = geodeticBounds;
        level0Rows = 2;
        level0Columns = 4;
      } else {
        ILcd3DEditableBounds editableBounds = new TLcdXYZBounds();
        TLcdGeoReference2GeoReference g2g = new TLcdGeoReference2GeoReference(
            geodeticRef,
            (ILcdGeoReference) aAssetModel.getModelReference()
        );
        try {
          g2g.sourceBounds2destinationSFCT(geodeticBounds, editableBounds);
        } catch (TLcdNoBoundsException e) {
          setPreprocessing(false);
          e.printStackTrace();
          return;
        }
        bounds = editableBounds;
        level0Rows = 2;
        level0Columns = 2;
      }

      // 1. set up tilesets
      ILcdGXYLayerFactory layerFactory = new RasterLayerFactory(true);
      tileSet = new TLcdEarthMetadataTerrainTileSet(
          bounds,
          levels, level0Rows, level0Columns,
          filteredAssetModel,
          layerFactory,
          aTexOversampleFactor,
          aCoverages,
          PreprocessorSettings.ELEVATION_TILE_RESOLUTION + 1,
          PreprocessorSettings.IMAGE_TILE_RESOLUTION
      );
      boolean texturesHaveTransparency = aTexBackgroundColor.getAlpha() != 255;
      if (texturesHaveTransparency) {
        tileSet.getRasterTileSet().setImageType(BufferedImage.TYPE_INT_ARGB);
      }
      tileSet.getRasterTileSet().setBackgroundColor(aTexBackgroundColor);

      // 2. open/create and configure the repository
      ILcdModel repositoryModel;
      try {
        // Try to open the repository
        TLcdEarthRepositoryModelDecoder repositoryModelDecoder = new TLcdEarthRepositoryModelDecoder();
        repositoryModelDecoder.setOpenRepositoriesReadOnly(false);
        repositoryModel = repositoryModelDecoder.decode(reposDest);
      } catch (IOException e) {
        // Try to create the repository
        TLcdEarthRepositoryModelFactory repositoryModelFactory = new TLcdEarthRepositoryModelFactory();
        repositoryModel = repositoryModelFactory.createRepositoryModel(reposDest, tileSet, aAssetModel.getModelReference());
      }
      repository = (TLcdEarthTileRepository) repositoryModel.elements().nextElement();

      // add the missing codecs
      repository.addTileDataCodec("Image",
                                  texturesHaveTransparency ?
                                  new TLcdEarthSeparateAlphaImageTileDataCodec() :
                                  new TLcdEarthImageTileDataCodec());
      repository.addTileDataCodec("Elevation",
                                  new TLcdEarthElevationDataTileDataCodec());

      // update the asset model
      ILcdModel repositoryAssetModel = ((ILcdEarthAssetBasedModelDescriptor) repositoryModel.getModelDescriptor()).getAssetModel(repository);
      if (repositoryAssetModel != null) {
        Enumeration assets = aAssetModel.elements();
        boolean assetModelChanged = false;
        while (assets.hasMoreElements()) {
          ILcdEarthAsset asset = (ILcdEarthAsset) assets.nextElement();
          if (!contains(repositoryAssetModel, asset)) {
            repositoryAssetModel.addElement(asset, ILcdModel.FIRE_LATER);
            assetModelChanged = true;
          }
        }
        repositoryAssetModel.fireCollectedModelChanges();
        if (assetModelChanged) {
          repositoryAssetModel.getModelEncoder().save(repositoryAssetModel);
        }
      }

      ImageLabelEditableTileSet imageLabelRepository = new ImageLabelEditableTileSet(repository);

      // 3. Create and configure the preprocessor
      TLcdEarthTileRepositoryPreprocessor tpPreprocessor = new TLcdEarthTileRepositoryPreprocessor();

      tpPreprocessor.addTileCombiner(new TLcdEarthImageCombiner());
      tpPreprocessor.addTileCombiner(new TLcdEarthElevationDataCombiner());
      tpPreprocessor.addTileCombiner(new TLcdEarthTerrainTileVertexArrayCombiner());

      int nCoverages = tileSet.getTileSetCoverageCount();
      ILcdEarthTileSetCoverage[] coverages = new ILcdEarthTileSetCoverage[nCoverages];
      String[] names = new String[nCoverages];
      ILcdGeoReference[] references = new ILcdGeoReference[nCoverages];
      TLcdEarthTileFormat[] formats = new TLcdEarthTileFormat[nCoverages];
      for (int i = nCoverages - 1; i >= 0; i--) {
        ILcdEarthTileSetCoverage currCoverage = tileSet.getTileSetCoverage(i);
        coverages[i] = currCoverage;
        names[i] = currCoverage.getName();
        if (currCoverage.getName().equals("Image")) {
          references[i] = (ILcdGeoReference) aAssetModel.getModelReference();
          formats[i] = new TLcdEarthTileFormat(BufferedImage.class);
        } else if (currCoverage.getName().equals("Elevation")) {
          references[i] = (ILcdGeoReference) aAssetModel.getModelReference();
          formats[i] = new TLcdEarthTileFormat(TLcdEarthElevationData.class);
        } else {
          references[i] = currCoverage.getNativeGeoReference();
          formats[i] = currCoverage.getNativeFormat();
        }
      }

      // 4. Start preprocessing
      tpPreprocessor.synchronizeRepository(
          filteredAssetModel,
          tileSet,
          coverages,
          names,
          references,
          formats,
          imageLabelRepository,
          reposDest,
          fCurrProgressListener
      );
    } catch (final Throwable t) {
      SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          JOptionPane.showMessageDialog(PreprocessorPanel.this,
                                        "Preprocessing error: " + t.getMessage(),
                                        "Error",
                                        JOptionPane.ERROR_MESSAGE);
        }
      });
      t.printStackTrace();
    } finally {
      // 5. Clean up
      if (repository != null) {
        try {
          repository.dispose();
        } catch (Throwable t) {
          t.printStackTrace();
        }
      }
      if (tileSet != null) {
        try {
          tileSet.dispose();
        } catch (Throwable t) {
          t.printStackTrace();
        }
      }
    }

    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        fProgressBar.setEnabled(false);
        fProgressBar.setStringPainted(false);
        fProgressBar.setIndeterminate(false);
        fProgressBar.setModel(new DefaultBoundedRangeModel());
      }
    });

    fCurrProgressListener = null;
  }

  private boolean contains(ILcdModel aModel, Object aElement) {
    if (aModel instanceof ILcdIntegerIndexedModel) {
      return ((ILcdIntegerIndexedModel) aModel).indexOf(aElement) >= 0;
    }
    Enumeration elems = aModel.elements();
    while (elems.hasMoreElements()) {
      if (elems.nextElement().equals(aElement)) {
        return true;
      }
    }
    return false;
  }

  public boolean isPreprocessing() {
    return fPreprocessing;
  }

  private void setPreprocessing(boolean b) {
    if (fPreprocessing == b) {
      return;
    }
    fPreprocessing = b;

    if (fPreprocessing) {
      fStartButton.setEnabled(false);
    } else {
      updateStartButtonState();
    }
    fStopButton.setEnabled(fPreprocessing);

    fImageLabel.clearImage();
    if (fPreprocessing) {
      fImageLabel.startUpdateTimer();
    } else {
      fImageLabel.stopUpdateTimer();
    }

    if (fPreprocessing) {
      fElapsedTimeLabel.start();
    } else {
      fElapsedTimeLabel.stop();
    }
    fLeftTimeLabel.reset();

    fLevelProgressLabel.setEnabled(fPreprocessing);

    fAssetNameLabel.setEnabled(fPreprocessing);
    fAssetProgressLabel.setEnabled(fPreprocessing);

    firePropertyChange(IS_PREPROCESSING_PROPERTY, !fPreprocessing, fPreprocessing);
  }

  /**
   * Sets the preprocessing progress on the current asset.
   *
   * @param aAsset             The asset being processed.
   * @param aNumAssetTiles     The total number of tiles for this asset.
   * @param aNumAssetTilesDone The number of tiles that have been processed for this asset.
   */
  public void setAssetProgress(ILcdEarthAsset aAsset, long aNumAssetTiles, long aNumAssetTilesDone) {
    fAssetNameLabel.setAsset(aAsset);

    fAssetProgressLabel.setProgress(aNumAssetTiles, aNumAssetTilesDone);
  }

  /**
   * Sets the preprocessing progress of the levels.
   *
   * @param aNumLevels     The total number of levels.
   * @param aNumLevelsDone The number of levels that have been processed.
   */
  public void setLevelProgress(int aNumLevels, int aNumLevelsDone) {
    fLevelProgressLabel.setProgress(aNumLevels, aNumLevelsDone);
  }

  /**
   * Sets the overall preprocessing progress.
   *
   * @param aOverallNumSteps     The overall number of steps that will need to be performed.
   * @param aOverallNumStepsDone The number of steps overall that are done.
   * @param aNumSteps            The number of steps that still needed to be performed when the
   *                             current preprocessing started.
   * @param aNumStepsDone        The number of steps that have been done since the current
   *                             preprocessing started.
   */
  public void setOverallProgress(long aOverallNumSteps, long aOverallNumStepsDone,
                                 long aNumSteps, long aNumStepsDone) {
    double percentDone = (double) aNumStepsDone / (double) aNumSteps;
    long elapsedTime = fElapsedTimeLabel.getTime();
    long timeLeft = Math.round(elapsedTime * ((1.0 - percentDone) / percentDone));
    fLeftTimeLabel.setTime(timeLeft);

    if (fProgressBar.isIndeterminate()) {
      fProgressBar.setIndeterminate(false);
      fProgressBar.setStringPainted(true);
    }
    final int minVal = fProgressBar.getMinimum();
    final int maxVal = fProgressBar.getMaximum();
    double overallPercentDone = (double) aOverallNumStepsDone / (double) aOverallNumSteps;
    int newValue = (int) Math.round(minVal + overallPercentDone * (maxVal - minVal));
    fProgressBar.setValue(newValue);
    int overallPercent = (int) (overallPercentDone * 100.0);
    int overallPercentFraction = ((int) (overallPercentDone * 10000.0)) % 100;
    String percentStr = overallPercent +
                        (overallPercentFraction < 10 ? ".0" : ".") +
                        overallPercentFraction + "%";
    fProgressBar.setString(percentStr);
  }

  private static class TimeLabel extends JLabel {

    private DecimalFormat twoDigitFormat;
    private DecimalFormat dayDigitFormat;
    private Dimension fPreferredSize;

    public TimeLabel() {
      setHorizontalAlignment(SwingConstants.RIGHT);

      twoDigitFormat = new DecimalFormat("00");
      dayDigitFormat = new DecimalFormat("000");

      reset();
    }

    public void setTime(long aTimeMs) {
      setText(getTimeString(aTimeMs));
    }

    public void reset() {
      setTime(0);
    }

    private String getTimeString(long timeMs) {
      long seconds = (timeMs / 1000) % 60;
      long minutes = (timeMs / (1000 * 60)) % 60;
      long hours = (timeMs / (1000 * 60 * 60)) % 24;
      long days = timeMs / (1000 * 60 * 60 * 24);

      String timeStr = twoDigitFormat.format(hours) + "h " +
                       twoDigitFormat.format(minutes) + "m " +
                       twoDigitFormat.format(seconds) + "s";

      if (days > 1000) {
        timeStr = new DecimalFormatSymbols().getInfinity();
      } else if (days > 0) {
        timeStr = dayDigitFormat.format(days) + "d " + timeStr;
      }

      return timeStr;
    }

    public Dimension getPreferredSize() {
      if (fPreferredSize == null) {
        String longestStr = "000d 00h 00m 00s";
        String oldText = super.getText();
        super.setText(longestStr);
        fPreferredSize = new Dimension(super.getPreferredSize());
        super.setText(oldText);

        setMinimumSize(fPreferredSize);
        setMaximumSize(fPreferredSize);
      }
      return fPreferredSize;
    }
  }

  private class ElapsedTimeLabel extends TimeLabel implements ActionListener {

    private Timer timer;
    private long startMs;

    public ElapsedTimeLabel() {
      timer = new Timer(1000, this);
      timer.setCoalesce(true);

      reset();
    }

    public void start() {
      reset();
      startMs = System.currentTimeMillis();
      timer.start();
    }

    public void stop() {
      timer.stop();
    }

    public void actionPerformed(ActionEvent e) {
      setTime(getTime());
    }

    public long getTime() {
      return System.currentTimeMillis() - startMs;
    }
  }

  private class AssetLabel extends JLabel {

    private ILcdEarthAsset fAsset;
    private String fAssetText;
    private static final int MAX_TEXT_LEN = 16;
    private Dimension fPreferredSize;

    private AssetLabel() {
      setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
      addMouseListener(new MouseListener() {
        public void mouseClicked(MouseEvent e) {
          setCurrentAsset(null);
          setCurrentAsset(fAsset);
        }

        public void mousePressed(MouseEvent e) {
        }

        public void mouseReleased(MouseEvent e) {
        }

        public void mouseEntered(MouseEvent e) {
          if (isEnabled()) {
            setText("<u>" + fAssetText + "</u>");
          } else {
            mouseExited(e);
          }
        }

        public void mouseExited(MouseEvent e) {
          setText(fAssetText);
        }
      });
    }

    public void setAsset(ILcdEarthAsset aAsset) {
      if (aAsset != fAsset) {
        setAssetPrivate(aAsset);
      }
    }

    private void setAssetPrivate(ILcdEarthAsset aAsset) {
      fAsset = aAsset;
      if (fAsset != null) {
        String srcName = fAsset.getSourceName();
        if (srcName.length() <= MAX_TEXT_LEN) {
          fAssetText = srcName;
        } else {
          fAssetText = "..." + srcName.substring(srcName.length() - MAX_TEXT_LEN, srcName.length());
        }
      } else {
        fAssetText = "<i>none</i>";
      }
      setText(fAssetText);
    }

    public void setText(String aText) {
      super.setText("<html>" + aText + "</html>");
    }

    public void setEnabled(boolean enabled) {
      if (!enabled) {
        setForeground(Color.GRAY);
        setAssetPrivate(null);
      } else {
        setForeground(Color.BLUE);
        setAssetPrivate(fAsset);
      }
      super.setEnabled(enabled);
    }

    public Dimension getPreferredSize() {
      if (fPreferredSize == null) {
        String longestStr = "...";
        for (int i = 0; i < MAX_TEXT_LEN; i++) {
          longestStr += "a";
        }
        String oldText = super.getText();
        super.setText(longestStr);
        fPreferredSize = new Dimension(super.getPreferredSize());
        super.setText(oldText);

        setMinimumSize(fPreferredSize);
        setMaximumSize(fPreferredSize);
      }
      return fPreferredSize;
    }
  }

  private static class ProgressLabel extends JLabel {

    public ProgressLabel() {
      reset();
    }

    public void reset() {
      setText("-/-");
    }

    public void setProgress(long total, long done) {
      setText(done + "/" + total);
    }

    public void setEnabled(boolean enabled) {
      reset();
      super.setEnabled(enabled);
    }
  }

  private static class ImageLabel extends JLabel {

    private int fImageSize;
    private BufferedImage fImage;

    private volatile boolean fUpdateImage;
    private Timer fUpdateTimer;
    private BufferedImage fSourceImage;

    public ImageLabel(int aImageSize) {
      fImageSize = aImageSize;
      fImage = new BufferedImage(fImageSize, fImageSize, BufferedImage.TYPE_INT_ARGB);
      setIcon(new ImageIcon(fImage));
      setHorizontalAlignment(JLabel.CENTER);

      // Limit the paint frequency
      fUpdateTimer = new Timer(1000, new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          if (fSourceImage != null) {
            fUpdateImage = true;
            repaint();
          }
        }
      });

      clearImage();
    }

    public void setImage(BufferedImage aImage) {
      fSourceImage = aImage;
    }

    protected void paintComponent(Graphics g) {
      if (fUpdateImage && fSourceImage != null) {
        Graphics2D g2d = (Graphics2D) fImage.getGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setComposite(AlphaComposite.Src);
        g2d.drawImage(fSourceImage, 0, 0, fImageSize, fImageSize, null);
        g2d.dispose();
        fUpdateImage = false;
        fSourceImage = null;
      }
      super.paintComponent(g);
    }

    public void clearImage() {
      fSourceImage = null;
      fUpdateImage = false;

      Graphics2D g = (Graphics2D) fImage.getGraphics();
      g.setColor(Color.BLACK);
      g.fillRect(0, 0, fImageSize, fImageSize);
      g.dispose();
      repaint();
    }

    public void startUpdateTimer() {
      fUpdateTimer.start();
    }

    public void stopUpdateTimer() {
      fUpdateTimer.stop();
    }

  }

  private class ImageLabelEditableTileSet extends ALcdEarthTileSetWrapper implements ILcdEarthEditableTileSet {

    public ImageLabelEditableTileSet(ILcdEarthEditableTileSet aDelegate) {
      super(aDelegate);
    }

    @Override
    public ILcdEarthEditableTileSet getDelegateTileSet() {
      return (ILcdEarthEditableTileSet) super.getDelegateTileSet();
    }

    public void commit() throws IOException {
      getDelegateTileSet().commit();
    }

    public void addTileSetCoverage(ILcdEarthTileSetCoverage aCoverage) throws IOException {
      getDelegateTileSet().addTileSetCoverage(aCoverage);
    }

    public void addTileSetCoverage(ILcdEarthTileSetCoverage aCoverage, String aTargetName, ILcdGeoReference aTargetReference) throws IOException {
      getDelegateTileSet().addTileSetCoverage(aCoverage, aTargetName, aTargetReference);
    }

    public void removeTileSetCoverage(ILcdEarthTileSetCoverage aCoverage) throws IOException {
      getDelegateTileSet().removeTileSetCoverage(aCoverage);
    }

    public void addTile(ALcdEarthTile aEarthTile, Object aContext) throws IOException {
      getDelegateTileSet().addTile(aEarthTile, aContext);

      if (aEarthTile.getData() instanceof BufferedImage) {
        BufferedImage image = (BufferedImage) aEarthTile.getData();
        fImageLabel.setImage(image);
      }
    }

    public void removeTile(ALcdEarthTile aTile, Object aContext) throws IOException {
      getDelegateTileSet().removeTile(aTile, aContext);
    }
  }

}

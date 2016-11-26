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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.EventObject;
import java.util.HashSet;
import java.util.List;
import java.util.Stack;
import java.util.Vector;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import com.luciad.earth.metadata.ILcdEarthAsset;
import com.luciad.earth.metadata.ILcdEarthRasterAsset;
import com.luciad.earth.metadata.TLcdEarthAssetModelDescriptor;
import com.luciad.earth.metadata.TLcdEarthClippedRasterAsset;
import com.luciad.earth.metadata.TLcdEarthRasterAsset;
import com.luciad.earth.metadata.format.ILcdEarthAssetCodec;
import com.luciad.earth.metadata.format.TLcdEarthAssetModelCodec;
import com.luciad.earth.metadata.format.TLcdEarthClippedRasterAssetCodec;
import com.luciad.earth.metadata.format.TLcdEarthRasterAssetCodec;
import com.luciad.earth.model.ILcdEarthModelDescriptor;
import com.luciad.earth.tileset.ILcdEarthTileSetCoverage;
import com.luciad.format.raster.ILcdMultilevelRaster;
import com.luciad.format.raster.TLcdDMEDModelDescriptor;
import com.luciad.format.raster.TLcdDTEDModelDescriptor;
import com.luciad.format.shp.TLcdSHPModelDecoder;
import com.luciad.geodesy.TLcdGeodeticDatum;
import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelDecoder;
import com.luciad.model.ILcdModelDescriptor;
import com.luciad.model.ILcdModelReference;
import com.luciad.model.TLcd2DBoundsIndexedModel;
import com.luciad.projection.TLcdEquidistantCylindrical;
import com.luciad.reference.ILcdGeodeticReference;
import com.luciad.reference.TLcdGeodeticReference;
import com.luciad.reference.TLcdGridReference;
import com.luciad.shape.ILcdBounds;
import com.luciad.shape.shape2D.TLcdXYBounds;
import com.luciad.shape.shape3D.ILcd3DEditableBounds;
import com.luciad.util.ILcdFireEventMode;
import com.luciad.util.TLcdNoBoundsException;
import com.luciad.util.TLcdOutOfBoundsException;
import com.luciad.util.logging.ILcdLogger;
import com.luciad.util.logging.TLcdLoggerFactory;
import com.luciad.view.ILcdXYWorldReference;
import com.luciad.view.gxy.ILcdGXYContext;
import com.luciad.view.gxy.ILcdGXYLayer;
import com.luciad.view.gxy.ILcdGXYView;
import com.luciad.view.gxy.TLcdG2DLineStyle;
import com.luciad.view.gxy.TLcdGXYLabelPainter;
import com.luciad.view.gxy.TLcdGXYLayer;
import com.luciad.view.gxy.TLcdGXYPainterColorStyle;
import com.luciad.view.gxy.painter.TLcdGXYBoundsPainter;

import samples.earth.preprocessor.AssetFactory;
import samples.earth.preprocessor.AssetFactoryInputMethod;
import samples.gxy.common.layers.GXYLayerUtil;
import samples.gxy.decoder.MapSupport;

/**
 * A panel for managing (loading/editing/saving) metadata.
 */
public class MetadataPanel extends JPanel implements MetadataProvider {

  private static final ILcdLogger sLogger = TLcdLoggerFactory.getLogger(MetadataPanel.class);

  private static final ILcdModelReference DEFAULT_MODEL_REFERENCE = new TLcdGeodeticReference(new TLcdGeodeticDatum());

  private JComboBox fModelReferenceComboBox;
  private JTable fCompositeTable;
  private AssetTable[] fAssetTables;

  private JButton fAddButton;
  private JButton fRemoveButton;
  private JButton fLoadButton;
  private JButton fSaveButton;

  private JFileChooser fAssetFileChooser;
  private JFileChooser fMetadataFileChooser;

  private boolean fActive;

  private ILcdGXYView fView;
  private ILcdGXYLayer fLayer;
  private TLcd2DBoundsIndexedModel fMetadataModel;

  /**
   * Creates a new metadata panel that uses the given view for visualisation.
   *
   * @param aView The view used to visualise the current metadata.
   */
  public MetadataPanel(ILcdGXYView aView) {
    fView = aView;
    fActive = true;

    // Create the model reference panel
    fModelReferenceComboBox = new JComboBox(
        new ILcdModelReference[]{
            DEFAULT_MODEL_REFERENCE
        }
    );
    fModelReferenceComboBox.setSelectedIndex(0);
    fModelReferenceComboBox.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        if (e.getStateChange() == ItemEvent.SELECTED) {
          setModelReference((ILcdModelReference) e.getItem());
        }
      }
    });

    JPanel modelRefPanel = new JPanel();
    modelRefPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
    modelRefPanel.add(new JLabel("Metadata model reference"));
    modelRefPanel.add(fModelReferenceComboBox);

    // Create the tables to show the current metadata's assets
    fAssetTables = new AssetTable[2];

    fAssetTables[0] = new AssetTable(new ElevationAssetTableModel());

    fAssetTables[1] = new AssetTable(new ImageAssetTableModel());
    fAssetTables[1].setDefaultEditor(ClippingShape.class, new ClippingShapeTableCellEditor());
    fAssetTables[1].setDefaultRenderer(ClippingShape.class, new TableCellRenderer() {
      public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                     boolean hasFocus, int row, int column) {
        if (value == null) {
          return null;
        }

        String source = ((ClippingShape) value).getSource();
        if (source == null) {
          source = "<html><i>none</i></html>";
        }
        return new JLabel(source);
      }
    });

    fCompositeTable = new JTable(new CompositeTableModel());
    fCompositeTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    for (int i = 0; i < fAssetTables.length; i++) {
      final int currTableIndex = i;
      fAssetTables[currTableIndex].getSelectionModel().addListSelectionListener(new ListSelectionListener() {
        public void valueChanged(ListSelectionEvent e) {
          AssetTable t = fAssetTables[currTableIndex];
          ListSelectionModel lsm = t.getSelectionModel();
          int firstIndex = 0;
          for (int i = 0; i < currTableIndex; i++) {
            firstIndex += fAssetTables[i].getRowCount();
          }
          boolean oldIsAdjusting = fCompositeTable.getSelectionModel().getValueIsAdjusting();
          fCompositeTable.getSelectionModel().setValueIsAdjusting(true);
          for (int i = e.getFirstIndex(); i <= e.getLastIndex(); i++) {
            if (lsm.isSelectedIndex(i)) {
              fCompositeTable.getSelectionModel().addSelectionInterval(firstIndex + i, firstIndex + i);
            } else {
              fCompositeTable.getSelectionModel().removeSelectionInterval(firstIndex + i, firstIndex + i);
            }
          }
          fCompositeTable.getSelectionModel().setValueIsAdjusting(oldIsAdjusting);
        }
      });
    }

    // Create a panel for the asset tables
    JPanel assetPanel = new JPanel();
    assetPanel.setLayout(new GridBagLayout());

    for (int i = 0; i < fAssetTables.length; i++) {
      AssetTable currTable = fAssetTables[i];

      JLabel label = new JLabel(currTable.getAssetTableModel().getAssetTypeName() + " assets");
      JScrollPane scrollPane = new JScrollPane(currTable);
      // A transfer handler is necessary to allow dropping content below the table
      scrollPane.setTransferHandler(new TopLevelTransferHandler(fAssetTables[i]));

      GridBagConstraints gbc;

      gbc = new GridBagConstraints();
      gbc.gridx = 0;
      gbc.gridy = 2 * i + 0;
      gbc.anchor = GridBagConstraints.WEST;
      gbc.insets.left = 5;
      gbc.insets.bottom = 2;
      assetPanel.add(label, gbc);

      gbc = new GridBagConstraints();
      gbc.gridx = 0;
      gbc.gridy = 2 * i + 1;
      gbc.weightx = 1;
      gbc.weighty = 1;
      gbc.fill = GridBagConstraints.BOTH;
      gbc.insets.bottom = 5;
      assetPanel.add(scrollPane, gbc);
    }

    assetPanel.setPreferredSize(new Dimension(400, 250));

    // Create the metadata managing buttons
    fAddButton = new JButton("Add asset(s)...");
    fAddButton.setToolTipText("Add asset(s) from files or recursively search a directory for asset(s)");
    fAddButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        addAsset();
      }
    });
    fRemoveButton = new JButton("Remove asset(s)");
    fRemoveButton.setToolTipText("Remove the selected asset(s) from the current metadata");
    fRemoveButton.setEnabled(false);
    fRemoveButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        removeSelectedAssets();
      }
    });
    fSaveButton = new JButton("Save metadata...");
    fSaveButton.setToolTipText("Save the assets to a metadata file");
    fSaveButton.setEnabled(false);
    fSaveButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        saveMetadata();
      }
    });
    fLoadButton = new JButton("Load metadata...");
    fLoadButton.setToolTipText("Load assets from a metadata file");
    fLoadButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        loadMetadata();
      }
    });

    ListSelectionListener tableSelectionListener = new ListSelectionListener() {
      public void valueChanged(ListSelectionEvent e) {
        if (e.getValueIsAdjusting()) {
          return;
        }

        if (e.getSource() instanceof ListSelectionModel) {
          // Only allow selection in 1 asset table at a time
          ListSelectionModel src = (ListSelectionModel) e.getSource();
          if (!src.isSelectionEmpty()) {
            for (int i = 0; i < fAssetTables.length; i++) {
              if (fAssetTables[i].getSelectionModel() != src &&
                  !fAssetTables[i].getSelectionModel().isSelectionEmpty()) {
                fAssetTables[i].clearSelection();
              }
            }
          }
        }

        // Update the asset remove button on selection change
        updateRemoveButton();
      }
    };
    TableModelListener tableModelListener = new TableModelListener() {
      public void tableChanged(TableModelEvent e) {
        // Update the metadata save button on asset add/remove
        updateSaveButton();
      }
    };
    for (int i = 0; i < fAssetTables.length; i++) {
      fAssetTables[i].getSelectionModel().addListSelectionListener(tableSelectionListener);
      fAssetTables[i].getModel().addTableModelListener(tableModelListener);
    }

    // Keep the metadata model up-to-date
    addTableModelListener(new TableModelListener() {
      public void tableChanged(TableModelEvent e) {
        fMetadataModel.removeAllElements(ILcdFireEventMode.FIRE_LATER);
        ILcdEarthAsset[] assets = new ILcdEarthAsset[getAssetCount()];
        for (int i = 0; i < assets.length; i++) {
          assets[i] = getAsset(i);
        }
        Arrays.sort(assets);
        for (int i = 0; i < assets.length; i++) {
          fMetadataModel.addElement(assets[i], ILcdFireEventMode.FIRE_LATER);
        }
        fMetadataModel.fireCollectedModelChanges();
      }
    });
    addTableModelListener(new TableModelListener() {

      private boolean fOldHasMetadata = hasMetadata();

      public void tableChanged(TableModelEvent e) {
        boolean oldHasMetadata = fOldHasMetadata;
        fOldHasMetadata = hasMetadata();
        firePropertyChange(HAS_METADATA_PROPERTY, oldHasMetadata, fOldHasMetadata);
      }
    });
    // Fit to the currently selected assets of the metadata model
    addSelectionListener(new ListSelectionListener() {
      public void valueChanged(ListSelectionEvent e) {
        if (e.getValueIsAdjusting()) {
          return;
        }

        ILcdEarthAsset[] assets = getSelectedAssets();
        if (assets != null && assets.length > 0) {
          ILcdBounds bounds = assets[0].getBounds();

          if (assets.length > 1) {
            ILcd3DEditableBounds editableBounds = bounds.cloneAs3DEditableBounds();
            for (int i = 1; i < assets.length; i++) {
              editableBounds.setTo2DUnion(assets[i].getBounds());
            }
            bounds = editableBounds;
          }

          double incPercent = 0.1;
          TLcdXYBounds incBounds = new TLcdXYBounds(
              bounds.getLocation().getX() - incPercent / 2.0 * bounds.getWidth(),
              bounds.getLocation().getY() - incPercent / 2.0 * bounds.getHeight(),
              bounds.getWidth() * (1.0 + incPercent),
              bounds.getHeight() * (1.0 + incPercent));

          GXYLayerUtil.fitGXYLayer(fView, fLayer, incBounds);
        }

      }
    });

    // Create the panel for the controls
    JPanel controlsPanel = new JPanel();
    controlsPanel.setLayout(new BoxLayout(controlsPanel, BoxLayout.X_AXIS));
    controlsPanel.add(Box.createHorizontalGlue());
    controlsPanel.add(fAddButton);
    controlsPanel.add(fRemoveButton);
    controlsPanel.add(Box.createHorizontalStrut(10));
    controlsPanel.add(fLoadButton);
    controlsPanel.add(fSaveButton);
    controlsPanel.add(Box.createHorizontalGlue());

    // Add all components
    setLayout(new BorderLayout());
    add(modelRefPanel, BorderLayout.NORTH);
    add(assetPanel, BorderLayout.CENTER);
    add(controlsPanel, BorderLayout.SOUTH);

    // Initialize all button states
    updateRemoveButton();
    updateSaveButton();

    // Create the file choosers
    getMetadataFileChooser();
    getAssetFileChooser();
  }

  public void loadData() {
    // Create the model used to visualize the metadata
    fMetadataModel = new TLcd2DBoundsIndexedModel();
    fMetadataModel.setModelReference(getModelReference());
    fMetadataModel.setModelDescriptor(new TLcdEarthAssetModelDescriptor("metadata"));

    // Create a layer for the metadata model
    fLayer = createMetadataLayer(fMetadataModel);
    // Add the layer to the view
    GXYLayerUtil.addGXYLayer(fView, fLayer);
  }

  private TLcdGXYLayer createMetadataLayer(ILcdModel aMetadataModel) {
    TLcdGXYLayer layer = new TLcdGXYLayer(aMetadataModel, "Metadata");
    layer.setGXYPen(MapSupport.createPen(aMetadataModel.getModelReference(), false));
    layer.setGXYPainterProvider(new TLcdGXYBoundsPainter() {

      {
        TLcdG2DLineStyle lineStyle = new TLcdG2DLineStyle();
        lineStyle.setColor(Color.WHITE);
        lineStyle.setSelectionColor(Color.WHITE);
        lineStyle.setLineWidth(2.5);
        lineStyle.setSelectionLineWidth(5.0);
        setLineStyle(lineStyle);

        TLcdGXYPainterColorStyle fillStyle = new TLcdGXYPainterColorStyle();
        fillStyle.setDefaultColor(new Color(255, 255, 255, 25));
        fillStyle.setSelectionColor(new Color(255, 255, 255, 50));
        setFillStyle(fillStyle);

        setMode(TLcdGXYBoundsPainter.OUTLINED_FILLED);
      }

      public void setObject(Object o) {
        super.setObject(((ILcdEarthAsset) o).getBounds());
      }

    });
    layer.setGXYLabelPainterProvider(new TLcdGXYLabelPainter() {
      protected String[] retrieveLabels(int aMode, ILcdGXYContext aILcdGXYContext) {
        return new String[]{((ILcdEarthAsset) getObject()).getSourceName()};
      }
    });
    layer.setSelectable(true);
    return layer;
  }

  /**
   * Adds the a listener to this panel's table model. This table contains a list of all assets in
   * the current metadata.
   *
   * @param aListener The table model listener to add.
   */
  public void addTableModelListener(TableModelListener aListener) {
    fCompositeTable.getModel().addTableModelListener(aListener);
  }

  /**
   * Removes a listener from this panel's table model.
   *
   * @param aListener The table model listener to remove.
   *
   * @see #addTableModelListener(TableModelListener)
   */
  public void removeTableModelListener(TableModelListener aListener) {
    fCompositeTable.getModel().removeTableModelListener(aListener);
  }

  /**
   * Adds a selection listener to this panel's table. This table contains a list of all assets in
   * the current metadata.
   *
   * @param aListener The selection listener to add.
   */
  public void addSelectionListener(ListSelectionListener aListener) {
    fCompositeTable.getSelectionModel().addListSelectionListener(aListener);
  }

  /**
   * Removes a selection listener from this panel's table.
   *
   * @param aListener The selection listener to add.
   *
   * @see #addSelectionListener(ListSelectionListener)
   */
  public void removeSelectionListener(ListSelectionListener aListener) {
    fCompositeTable.getSelectionModel().removeListSelectionListener(aListener);
  }

  /**
   * Returns the asset at the given index.
   *
   * @param aIndex An index in [0,{@link #getAssetCount()}[.
   *
   * @return The asset at the given index.
   */
  public ILcdEarthAsset getAsset(int aIndex) {
    int currStartIndex = 0;
    for (int i = 0; i < fAssetTables.length; i++) {
      if (aIndex - currStartIndex < fAssetTables[i].getRowCount()) {
        return fAssetTables[i].getAssetTableModel().getAsset(aIndex - currStartIndex);
      }
      currStartIndex += fAssetTables[i].getRowCount();
    }
    throw new ArrayIndexOutOfBoundsException(aIndex);
  }

  /**
   * Returns the number of assets in the current metadata.
   *
   * @return The current number of assets.
   */
  public int getAssetCount() {
    int rowCount = 0;
    for (int i = 0; i < fAssetTables.length; i++) {
      rowCount += fAssetTables[i].getRowCount();
    }
    return rowCount;
  }

  /**
   * Returns all currently selected assets.
   *
   * @return The array containing all currently selected assets. It may be of length 0 but will
   *         never be null.
   */
  public ILcdEarthAsset[] getSelectedAssets() {
    int[] rows = fCompositeTable.getSelectedRows();
    ILcdEarthAsset[] assets = new ILcdEarthAsset[rows.length];
    for (int i = 0; i < rows.length; i++) {
      assets[i] = getAsset(rows[i]);
    }
    return assets;
  }

  /**
   * Clears the current asset selection.
   */
  public void clearAssetSelection() {
    for (int i = 0; i < fAssetTables.length; i++) {
      fAssetTables[i].clearSelection();
    }
  }

  /**
   * Sets the selected asset.
   *
   * @param aAsset The asset that should be selected.
   */
  public void setSelectedAsset(ILcdEarthAsset aAsset) {
    fCompositeTable.getSelectionModel().setValueIsAdjusting(true);
    clearAssetSelection();
    if (aAsset != null) {
      for (int i = 0; i < fAssetTables.length; i++) {
        int index = fAssetTables[i].getAssetTableModel().indexOf(aAsset);
        if (index >= 0) {
          fAssetTables[i].getSelectionModel().setSelectionInterval(index, index);
        }
      }
    }
    fCompositeTable.getSelectionModel().setValueIsAdjusting(false);
  }

  /**
   * Activates or deactivates this panel. The current metadata can only be changed if  this panel is
   * activated.
   *
   * @param active True if this panel should be active.
   */
  public void setActive(boolean active) {
    fActive = active;
    fAddButton.setEnabled(active);
    updateRemoveButton();
    fLoadButton.setEnabled(active);
    for (int i = 0; i < fAssetTables.length; i++) {
      AssetTable assetTable = fAssetTables[i];
      assetTable.setActive(active);
    }
  }

  /**
   * Returns whether the current metadata is valid.
   *
   * @return True if the current metadata is valid.
   */
  public boolean hasMetadata() {
    for (int i = 0; i < fAssetTables.length; i++) {
      if (fAssetTables[i].getRowCount() > 0) {
        return true;
      }
    }
    return false;
  }

  /**
   * Returns the current metadata.
   *
   * @return the current metadata.
   */
  public ILcdModel getMetadata() {
    TLcd2DBoundsIndexedModel assetModel = new TLcd2DBoundsIndexedModel();
    assetModel.setModelDescriptor(fMetadataModel.getModelDescriptor());
    assetModel.setModelReference(fMetadataModel.getModelReference());
    for (int i = 0; i < fAssetTables.length; i++) {
      if (fAssetTables[i].isEditing()) {
        if (!fAssetTables[i].getCellEditor().stopCellEditing()) {
          fAssetTables[i].getCellEditor().cancelCellEditing();
        }
      }
      fAssetTables[i].getAssetTableModel().getAssets(assetModel, ILcdFireEventMode.NO_EVENT);
    }
    return assetModel;
  }

  private void updateRemoveButton() {
    boolean hasAssetsSelected = false;
    if (fActive) {
      for (int i = 0; i < fAssetTables.length; i++) {
        if (!fAssetTables[i].getSelectionModel().isSelectionEmpty()) {
          hasAssetsSelected = true;
          break;
        }
      }
    }
    fRemoveButton.setEnabled(hasAssetsSelected);
  }

  private void updateSaveButton() {
    boolean hasAssets = false;
    for (int i = 0; i < fAssetTables.length; i++) {
      if (fAssetTables[i].getRowCount() > 0) {
        hasAssets = true;
        break;
      }
    }
    fSaveButton.setEnabled(hasAssets);
  }

  public void loadMetadata(String aFileName) {
    loadMetadata(new File(aFileName), createMetadataCodec());
  }

  private void loadMetadata(final File f, TLcdEarthAssetModelCodec aAssetModelCodec) {
    // Add the assets
    try {
      final ILcdModel assetModel = aAssetModelCodec.decode(f.toURI().toString());
      setModelReference(assetModel.getModelReference());
      SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          for (Enumeration e = assetModel.elements(); e.hasMoreElements(); ) {
            ILcdEarthAsset currAsset = (ILcdEarthAsset) e.nextElement();
            AssetTable currTable = getAssetTable(currAsset);
            if (currTable != null) {
              currTable.getAssetTableModel().addAsset(currAsset);
            }
          }
        }
      });
    } catch (final Exception e) {
      sLogger.warn("Error while loading metadata from [" + f + "]", e);
      SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          JOptionPane.showMessageDialog(
              MetadataPanel.this,
              "Unable to load metadata: " + e.getMessage(),
              "Error",
              JOptionPane.ERROR_MESSAGE);
        }
      });
    }
  }

  private void setModelReference(ILcdModelReference aModelReference) {
    ILcdModelReference oldModelReference = getModelReference();
    ILcdXYWorldReference worldRef = getWorldReference(aModelReference);
    if (worldRef != null && !fView.getXYWorldReference().equals(worldRef)) {
      fView.setXYWorldReference(worldRef);
    }
    int oldLayerIndex = fView.indexOf(fLayer);
    fView.removeLayer(fLayer);
    ILcdModelDescriptor modelDescr = fMetadataModel.getModelDescriptor();
    fMetadataModel = new TLcd2DBoundsIndexedModel();
    fMetadataModel.setModelReference(aModelReference);
    fMetadataModel.setModelDescriptor(modelDescr);
    for (int i = 0; i < fAssetTables.length; i++) {
      AssetTableModel model = fAssetTables[i].getAssetTableModel();
      ILcdEarthAsset[] oldAssets = new ILcdEarthAsset[model.getAssetCount()];
      for (int j = 0; j < oldAssets.length; j++) {
        oldAssets[j] = model.getAsset(0);
        model.removeAsset(0);
      }
      for (int j = 0; j < oldAssets.length; j++) {
        ILcdEarthAsset oldAsset = oldAssets[j];
        try {
          AssetFactory assetFactory = getAssetFactory();
          ILcdEarthAsset newAsset = assetFactory.createAsset(
              new File(oldAsset.getSourceName()),
              new TransformAssetInputMethod(oldAsset, oldModelReference)
          );

          model.addAsset(newAsset);
        } catch (Exception e) {
          sLogger.warn("Error while transforming asset [" + oldAsset.getSourceName() + "], removing it...", e);
        }
      }
    }
    fLayer = createMetadataLayer(fMetadataModel);
    fView.addGXYLayer(fLayer);
    fView.moveLayerAt(oldLayerIndex, fLayer);

    if (!fModelReferenceComboBox.getSelectedItem().equals(aModelReference)) {
      for (int i = 0; i < fModelReferenceComboBox.getItemCount(); i++) {
        if (fModelReferenceComboBox.getItemAt(i).equals(aModelReference)) {
          fModelReferenceComboBox.setSelectedIndex(i);
          return;
        }
      }
      fModelReferenceComboBox.addItem(aModelReference);
      fModelReferenceComboBox.setSelectedIndex(fModelReferenceComboBox.getItemCount() - 1);
    }
  }

  private ILcdXYWorldReference getWorldReference(ILcdModelReference aModelReference) {
    if (aModelReference instanceof ILcdXYWorldReference) {
      return (ILcdXYWorldReference) aModelReference;
    } else if (aModelReference instanceof ILcdGeodeticReference) {
      return new TLcdGridReference(new TLcdGeodeticDatum(), new TLcdEquidistantCylindrical());
    } else {
      return null;
    }
  }

  private ILcdModelReference getModelReference() {
    return (ILcdModelReference) fModelReferenceComboBox.getSelectedItem();
  }

  private void registerModelReference(ILcdModelReference aModelReference) {
    ComboBoxModel model = fModelReferenceComboBox.getModel();
    for (int i = 0; i < model.getSize(); i++) {
      if (model.getElementAt(i).equals(aModelReference)) {
        return;
      }
    }
    fModelReferenceComboBox.addItem(aModelReference);
  }

  private AssetTable getAssetTable(ILcdEarthAsset aAsset) {
    for (int i = 0; i < fAssetTables.length; i++) {
      if (fAssetTables[i].getAssetTableModel().canHaveAsset(aAsset)) {
        return fAssetTables[i];
      }
    }
    return null;
  }

  private List<AssetTable> getAllAssetTables(ILcdEarthAsset aAsset) {
    ArrayList<AssetTable> list = new ArrayList<AssetTable>();
    for (AssetTable fAssetTable : fAssetTables) {
      if (fAssetTable.getAssetTableModel().canHaveAsset(aAsset)) {
        list.add(fAssetTable);
      }
    }
    return list;
  }

  private TLcdEarthAssetModelCodec createMetadataCodec() {
    TLcdEarthAssetModelCodec ret = new TLcdEarthAssetModelCodec();
    ret.removeAllAssetCodecs();
    for (ILcdEarthAssetCodec codec : getAssetCodecs()) {
      ret.addAssetCodec(codec);
    }
    return ret;
  }

  private void loadMetadata() {
    JFileChooser fileChooser = getMetadataFileChooser();
    if (fileChooser.isShowing()) {
      return;
    }

    int openResult = fileChooser.showOpenDialog(this);
    if (openResult != JFileChooser.APPROVE_OPTION) {
      return;
    }

    final File[] selectedFiles = fileChooser.getSelectedFiles();
    TLcdEarthAssetModelCodec assetModelCodec = createMetadataCodec();
    for (int i = 0; i < selectedFiles.length; i++) {
      loadMetadata(selectedFiles[i], assetModelCodec);
    }
  }

  private void addAsset() {
    JFileChooser fileChooser = getAssetFileChooser();
    if (fileChooser.isShowing()) {
      return;
    }

    int fileChoice = fileChooser.showOpenDialog(this);
    if (fileChoice != JFileChooser.APPROVE_OPTION) {
      return;
    }

    final File[] files = fileChooser.getSelectedFiles();
    final ProgressMonitor progressMonitor = new ProgressMonitor(this, "Adding assets", "", 0, files.length);
    Runnable addAssetsRunnable = new Runnable() {
      public void run() {
        for (int i = 0; i < files.length; i++) {
          if (files[i].isDirectory()) {
            progressMonitor.setNote("Directory: " + files[i].getName());
            addDirectoryAsset(files[i], progressMonitor, i);
          } else {
            progressMonitor.setNote(files[i].getName());
            addFileAsset(files[i]);
          }
          progressMonitor.setProgress(i + 1);
          if (progressMonitor.isCanceled()) {
            break;
          }
        }
        progressMonitor.close();
      }
    };
    Thread addAssetThread = new Thread(addAssetsRunnable);
    addAssetThread.setPriority(Thread.MIN_PRIORITY);
    addAssetThread.start();
  }

  private void addFileAsset(File aFile) {
    try {
      AssetFactory assetFactory = getAssetFactory();
      // Choose a model decoder
      ILcdModelDecoder decoder = chooseModelDecoder(assetFactory, aFile);
      if (decoder == null) {
        return;
      }

      // Create the asset
      ILcdModel[] modelRef = new ILcdModel[1];
      ILcdEarthAsset asset = createAsset(assetFactory, aFile, decoder,
                                         new CreateAssetInputMethod(this, decoder), modelRef);

      // Choose an asset table
      AssetTable table = getAssetTable(aFile, asset);

      // Add the asset
      ILcdModel model = modelRef[0];
      if (table != null) {
        addAsset(asset, model, table, false);
      }
      if (model != null) {
        model.dispose();
      }
    } catch (final Exception e) {
      sLogger.warn("Error while creating asset for file [" + aFile + "], ignoring it...", e);
      try {
        SwingUtilities.invokeAndWait(new Runnable() {
          public void run() {
            JOptionPane.showMessageDialog(
                MetadataPanel.this,
                "Unable to add asset: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
          }
        });
      } catch (InterruptedException e1) {
        // ignore
      } catch (InvocationTargetException e1) {
        // ignore
      }
    }
  }

  private void addDirectoryAsset(File aFile, ProgressMonitor aProgressMonitor, int aProgress) {
    try {
      AssetFactory assetFactory = getAssetFactory();
      // Choose a model decoder
      ILcdModelDecoder[] decoders = assetFactory.getAllModelDecoders();
      String[] names = new String[decoders.length];
      for (int i = 0; i < decoders.length; i++) {
        names[i] = decoders[i].getDisplayName();
      }
      String formatChoice = (String) JOptionPane.showInputDialog(this,
                                                                 "What is the asset format of the assets in \n" +
                                                                 "\"" + aFile.getPath() + "\"?",
                                                                 "Directory asset(s) format?",
                                                                 JOptionPane.QUESTION_MESSAGE,
                                                                 null,
                                                                 names,
                                                                 names[0]);
      if (formatChoice == null) {
        return;
      }

      ILcdModelDecoder decoder = null;
      for (ILcdModelDecoder currDecoder : decoders) {
        if (formatChoice.equals(currDecoder.getDisplayName())) {
          decoder = currDecoder;
          break;
        }
      }

      if (decoder == null) {
        return;
      }

      AssetTable assetTable = null;
      AssetFactoryInputMethod inputMethod = new CreateAssetInputMethod(this, decoder);

      // Recursively search the directory for assets
      Stack<File> filesToScan = new Stack<File>();
      filesToScan.add(aFile);
      int numAssets = 0;
      List<ILcdEarthAsset> assetsToAdd = new ArrayList<ILcdEarthAsset>();
      List<ILcdModel> assetsToAddModels = new ArrayList<ILcdModel>();
      while (!filesToScan.isEmpty()) {
        File currFile = filesToScan.pop();
        if (currFile.isDirectory()) {
          filesToScan.addAll(Arrays.asList(currFile.listFiles()));
        } else if (decoder.canDecodeSource(currFile.getAbsolutePath())) {
          try {
            // Create the asset
            ILcdModel[] modelRef = new ILcdModel[1];
            ILcdEarthAsset asset = createAsset(assetFactory, currFile, decoder,
                                               inputMethod, modelRef);

            // Queue the asset
            assetsToAdd.add(asset);
            assetsToAddModels.add(modelRef[0]);

            if (assetTable == null) {
              // Choose an asset table
              assetTable = getAssetTable(null, assetsToAdd.get(0));
              if (assetTable == null) {
                return;
              }
            }
          } catch (Exception e) {
            sLogger.warn("Error while creating asset for file [" + currFile.getAbsolutePath() + "]", e);
          }
          // Update the GUI in batches
          if (assetsToAdd.size() > 100) {
            // Add the assets
            numAssets += addAssets(assetsToAdd, assetsToAddModels, assetTable, true);

            // Clean up
            for (ILcdModel assetsToAddModel : assetsToAddModels) {
              if (assetsToAddModel != null) {
                assetsToAddModel.dispose();
              }
            }
            assetsToAdd.clear();
            assetsToAddModels.clear();
          }

          aProgressMonitor.setNote("Directory: " + aFile.getName() + " (" + (numAssets + assetsToAdd.size()) + " assets)");
          aProgressMonitor.setProgress(aProgress);
        }
        if (aProgressMonitor.isCanceled()) {
          break;
        }
      }
      // Final GUI update
      if (assetsToAdd.size() > 0) {
        // Add the assets
        numAssets += addAssets(assetsToAdd, assetsToAddModels, assetTable, true);

        // Clean up
        for (ILcdModel assetsToAddModel : assetsToAddModels) {
          if (assetsToAddModel != null) {
            assetsToAddModel.dispose();
          }
        }
        assetsToAdd.clear();
        assetsToAddModels.clear();

        aProgressMonitor.setNote("Directory: " + aFile.getName() + " (" + numAssets + " assets)");
        aProgressMonitor.setProgress(aProgress);
      }
    } catch (final Exception e) {
      sLogger.warn("Error while adding assets for directory [" + aFile + "]", e);
      try {
        SwingUtilities.invokeAndWait(new Runnable() {
          public void run() {
            JOptionPane.showMessageDialog(
                MetadataPanel.this,
                "Unable to add asset: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
          }
        });
      } catch (InterruptedException e1) {
        // ignore
      } catch (InvocationTargetException e1) {
        // ignore
      }
    }
  }

  private int addAssets(final List<ILcdEarthAsset> aAssets, final List<ILcdModel> aModels, final AssetTable aTable, final boolean checkDuplicate) {
    final int[] numAdded = new int[]{0};
    try {
      SwingUtilities.invokeAndWait(new Runnable() {
        public void run() {
          for (int i = 0; i < aAssets.size(); i++) {
            ILcdEarthAsset asset = aAssets.get(i);
            ILcdModel model = aModels.get(i);
            try {
              if (checkDuplicate && aTable.getAssetTableModel().containsAsset(asset)) {
                return;
              }
              if (model != null) {
                registerModelReference(model.getModelReference());
              }
              aTable.getAssetTableModel().addAsset(asset);
              numAdded[0]++;
            } catch (Exception e) {
              sLogger.warn("Error while adding asset [" + asset.getSourceName() + "], ignoring it...", this, e);
            }
          }
        }
      });
    } catch (InterruptedException e) {
      // ignore
    } catch (InvocationTargetException e) {
      // ignore
    }
    return numAdded[0];
  }

  private void addAsset(final ILcdEarthAsset aAsset, final ILcdModel aModel, final AssetTable aTable, final boolean checkDuplicate) {
    try {
      SwingUtilities.invokeAndWait(new Runnable() {
        public void run() {
          if (checkDuplicate && aTable.getAssetTableModel().containsAsset(aAsset)) {
            return;
          }
          if (aModel != null) {
            registerModelReference(aModel.getModelReference());
          }
          aTable.getAssetTableModel().addAsset(aAsset);
        }
      });
    } catch (InterruptedException e) {
      // ignore
    } catch (InvocationTargetException e) {
      // ignore
    }
  }

  private AssetTable getAssetTable(File aFile, ILcdEarthAsset aAsset) {
    List<AssetTable> tables = getAllAssetTables(aAsset);
    AssetTable table = null;
    if (tables.size() == 1) {
      table = (AssetTable) tables.get(0);
    } else if (!tables.isEmpty()) {
      String[] names = new String[tables.size()];
      for (int i = 0; i < tables.size(); i++) {
        names[i] = ((AssetTable) tables.get(i)).getAssetTableModel().getAssetTypeName();
      }
      String message = "What is the asset type";
      if (aFile != null) {
        message += "of\n \"" + aFile.getPath() + "\"";
      }
      message += "?";
      String tableChoice = (String) JOptionPane.showInputDialog(this,
                                                                message,
                                                                "Asset type?",
                                                                JOptionPane.QUESTION_MESSAGE,
                                                                null,
                                                                names,
                                                                names[0]);
      if (tableChoice != null) {
        for (int i = 0; i < names.length; i++) {
          if (names[i].equals(tableChoice)) {
            table = (AssetTable) tables.get(i);
            break;
          }
        }
      }
    } else {
      sLogger.warn("Don't know where to put asset " + aAsset.getSourceName());
    }
    return table;
  }

  private ILcdEarthAsset createAsset(AssetFactory aAssetFactory, File aFile, ILcdModelDecoder aDecoder,
                                     AssetFactoryInputMethod aInputMethod, ILcdModel[] aModelDst) throws IOException, TLcdNoBoundsException, TLcdOutOfBoundsException {
    ILcdEarthAsset asset = aAssetFactory.createAsset(aFile, aDecoder, aInputMethod);
    if (aModelDst != null && asset.getModelDecoder() != null) {
      aModelDst[0] = asset.getModelDecoder().decode(asset.getSourceName());
    }
    return asset;
  }

  private ILcdModelDecoder chooseModelDecoder(AssetFactory aAssetFactory, File aFile) {
    ILcdModelDecoder[] decoders = aAssetFactory.getModelDecodersFor(aFile);
    ILcdModelDecoder decoder;
    if (decoders.length == 1) {
      decoder = decoders[0];
    } else {
      String[] names = new String[decoders.length];
      for (int i = 0; i < decoders.length; i++) {
        names[i] = decoders[i].getDisplayName();
      }
      String formatChoice = (String) JOptionPane.showInputDialog(this,
                                                                 "What is the asset format of \n" +
                                                                 "\"" + aFile.getPath() + "\"?",
                                                                 "Asset format?",
                                                                 JOptionPane.QUESTION_MESSAGE,
                                                                 null,
                                                                 names,
                                                                 names[0]);
      if (formatChoice == null) {
        return null;
      }

      decoder = null;
      for (int i = 0; i < decoders.length; i++) {
        if (formatChoice.equals(decoders[i].getDisplayName())) {
          decoder = decoders[i];
          break;
        }
      }
    }
    return decoder;
  }

  private AssetFactory fAssetFactory;

  private AssetFactory getAssetFactory() {
    if (fAssetFactory == null || !fAssetFactory.getMetadataReference().equals(getModelReference())) {
      fAssetFactory = new AssetFactory(getModelReference());
      for (ILcdEarthAssetCodec codec : getAssetCodecs()) {
        fAssetFactory.addAssetCodec(codec);
      }
    }
    return fAssetFactory;
  }

  private Collection<ILcdEarthAssetCodec> getAssetCodecs() {
    ArrayList<ILcdEarthAssetCodec> codecs = new ArrayList<ILcdEarthAssetCodec>();
    TLcdEarthRasterAssetCodec rasterAssetCodec = new TLcdEarthRasterAssetCodec();
    rasterAssetCodec.setModelDecoderOptional(false);
    codecs.add(rasterAssetCodec);
    TLcdEarthClippedRasterAssetCodec clippedRasterAssetCodec = new TLcdEarthClippedRasterAssetCodec();
    clippedRasterAssetCodec.setModelDecoderOptional(false);
    codecs.add(clippedRasterAssetCodec);
    return codecs;
  }

  private void removeSelectedAssets() {
    for (AssetTable fAssetTable : fAssetTables) {
      int[] indices = fAssetTable.getSelectedRows();
      for (int j = indices.length - 1; j >= 0; j--) {
        fAssetTable.getAssetTableModel().removeAsset(indices[j]);
      }
    }
  }

  private void saveMetadata() {
    JFileChooser fileChooser = getMetadataFileChooser();
    if (fileChooser.isShowing()) {
      return;
    }

    // Stop any edits in progress
    boolean cancelEditsConfirmed = false;
    for (int i = 0; i < fAssetTables.length; i++) {
      if (fAssetTables[i].isEditing()) {
        if (!fAssetTables[i].getCellEditor().stopCellEditing()) {
          if (!cancelEditsConfirmed) {
            int choice2 = JOptionPane.showConfirmDialog(
                this,
                "Some edited values are invalid and will be reverted\n" +
                "to their original value before saving.\n\n" +
                "Do you want to continue?",
                "Invalid edited value",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.ERROR_MESSAGE);
            if (choice2 == JOptionPane.YES_OPTION) {
              cancelEditsConfirmed = true;
            } else {
              return;
            }
          }
          fAssetTables[i].getCellEditor().cancelCellEditing();
        }
      }
    }

    // Select a file
    int choice = fileChooser.showSaveDialog(this);
    if (choice != JFileChooser.APPROVE_OPTION) {
      return;
    }
    File selectedFile = fileChooser.getSelectedFile();

    // Copy the modified assets
    ILcdModel assetModel = getMetadata();
    assetModel.removeAllElements(ILcdFireEventMode.FIRE_LATER);
    for (int i = 0; i < fAssetTables.length; i++) {
      fAssetTables[i].getAssetTableModel().getAssets(assetModel, ILcdFireEventMode.FIRE_LATER);
    }
    assetModel.fireCollectedModelChanges();

    // Save the metadata
    TLcdEarthAssetModelCodec codec = createMetadataCodec();
    try {
      if (!selectedFile.getName().endsWith(".xml")) {
        String filename = selectedFile.getName() + ".xml";
        selectedFile = new File(selectedFile.getParentFile(), filename);
      }
      codec.export(assetModel, selectedFile.getPath());
    } catch (Exception e) {
      sLogger.warn("Error while saving metadata", e);
      JOptionPane.showMessageDialog(this, "Unable to save assets: " + e.getMessage(),
                                    "Error", JOptionPane.ERROR_MESSAGE);
    }
  }

  private JFileChooser getMetadataFileChooser() {
    if (fMetadataFileChooser == null) {
      fMetadataFileChooser = new JFileChooser(".");
      fMetadataFileChooser.setMultiSelectionEnabled(true);
      fMetadataFileChooser.setFileFilter(new MetadataFileFilter());
      fMetadataFileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
      if (fAssetFileChooser != null) {
        fMetadataFileChooser.setCurrentDirectory(fAssetFileChooser.getCurrentDirectory());
      }
    }
    return fMetadataFileChooser;
  }

  private JFileChooser getAssetFileChooser() {
    if (fAssetFileChooser == null) {
      fAssetFileChooser = new JFileChooser(".");
      fAssetFileChooser.setMultiSelectionEnabled(true);
      ILcdModelDecoder[] allDecoders = getAssetFactory().getAllModelDecoders();
      AllAssetFormatFileFilter assetFormatFileFilter = new AllAssetFormatFileFilter();
      fAssetFileChooser.setFileFilter(assetFormatFileFilter);
      for (int i = 0; i < allDecoders.length; i++) {
        fAssetFileChooser.addChoosableFileFilter(
            new SingleAssetFormatFileFilter(allDecoders[i]));
      }
      fAssetFileChooser.setFileFilter(assetFormatFileFilter);
      fAssetFileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
      if (fMetadataFileChooser != null) {
        fAssetFileChooser.setCurrentDirectory(fMetadataFileChooser.getCurrentDirectory());
      }
    }
    return fAssetFileChooser;
  }

  private class AllAssetFormatFileFilter extends FileFilter {

    private ILcdModelDecoder[] fAllDecoders;

    private AllAssetFormatFileFilter() {
      fAllDecoders = getAssetFactory().getAllModelDecoders();
    }

    public boolean accept(File f) {
      if (f.isDirectory()) {
        return true;
      }

      for (int i = 0; i < fAllDecoders.length; i++) {
        if (fAllDecoders[i].canDecodeSource(f.getName())) {
          return true;
        }
      }

      return false;
    }

    public String getDescription() {
      return "All asset files";
    }
  }

  private class SingleAssetFormatFileFilter extends FileFilter {

    private ILcdModelDecoder fModelDecoder;

    private SingleAssetFormatFileFilter(ILcdModelDecoder aModelDecoder) {
      fModelDecoder = aModelDecoder;
    }

    public boolean accept(File f) {
      if (f.isDirectory()) {
        return true;
      }

      return fModelDecoder.canDecodeSource(f.getName());
    }

    public String getDescription() {
      return fModelDecoder.getDisplayName() + " files";
    }
  }

  private static class AssetTable extends JTable {

    private AssetTableTransferHandler fTransferHandler;

    public AssetTable(AssetTableModel aTableModel) {
      super(aTableModel);

      setDragEnabled(true);
      fTransferHandler = new AssetTableTransferHandler();
      setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
      getColumnModel().getColumn(0).setPreferredWidth(30);
      getColumnModel().getColumn(0).setMinWidth(30);
      getColumnModel().getColumn(0).setMaxWidth(30);
      getColumnModel().getColumn(0).setResizable(false);

      setActive(true);
    }

    public AssetTableModel getAssetTableModel() {
      return (AssetTableModel) super.getModel();
    }

    public void setModel(TableModel dataModel) {
      if (!(dataModel instanceof AssetTableModel)) {
        throw new IllegalArgumentException("not a raster table model");
      }
      super.setModel(dataModel);
    }

    public String getToolTipText(MouseEvent e) {
      int rowIndex = rowAtPoint(e.getPoint());
//      int colIndex = columnAtPoint( e.getPoint() );
//      int realColumnIndex = convertColumnIndexToModel( colIndex );

      AssetInfo assetInfo = getAssetTableModel().getAssetInfo(rowIndex);

      if (assetInfo != null) {
        String tileDataType;
        if (assetInfo.getAsset().getCoverageType() == ILcdEarthTileSetCoverage.CoverageType.IMAGE) {
          tileDataType = "pixels";
        } else {
          tileDataType = "vertices";
        }
        return String.format("~%.3f Mtiles of %dx%d %s",
                             assetInfo.getNumTiles() * 1e-6,
                             assetInfo.getTileSize(),
                             assetInfo.getTileSize(),
                             tileDataType);
      } else {
        return super.getToolTipText(e);
      }
    }

    public void setActive(boolean aActive) {
      setTransferHandler(aActive ? fTransferHandler : null);
    }
  }

  private static class AssetInfo {

    private ILcdEarthAsset fAsset;
    private long fNumTiles;
    private int fTileSize;

    public AssetInfo(ILcdEarthAsset aAsset, long aNumTiles, int aTileSize) {
      fAsset = aAsset;
      fNumTiles = aNumTiles;
      fTileSize = aTileSize;
    }

    public ILcdEarthAsset getAsset() {
      return fAsset;
    }

    public long getNumTiles() {
      return fNumTiles;
    }

    public int getTileSize() {
      return fTileSize;
    }
  }

  private static abstract class AssetTableModel extends AbstractTableModel {

    private final Vector<ILcdEarthAsset> fAssets = new Vector<ILcdEarthAsset>();
    private final HashSet<File> fAssetFiles = new HashSet<File>();

    public abstract String getAssetTypeName();

    public int getRowCount() {
      return fAssets.size();
    }

    public int getColumnCount() {
      return 2;
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
      ILcdEarthAsset asset = getAsset(rowIndex);
      switch (columnIndex) {
      case 0:
        return new Integer(rowIndex);
      case 1:
        return asset.getSourceName();
      default:
        return null;
      }
    }

    public String getColumnName(int column) {
      switch (column) {
      case 0:
        return "#";
      case 1:
        return "Source name";
      default:
        return null;
      }
    }

    public Class getColumnClass(int columnIndex) {
      switch (columnIndex) {
      case 0:
        return Integer.class;
      case 1:
        return String.class;
      default:
        return null;
      }
    }

    public ILcdEarthAsset getAsset(int aIndex) {
      return fAssets.get(aIndex);
    }

    public int getAssetCount() {
      return fAssets.size();
    }

    public void addAsset(ILcdEarthAsset asset) {
      if (!canHaveAsset(asset)) {
        throw new IllegalArgumentException("not a valid asset");
      }

      if (containsAsset(asset)) {
        sLogger.warn("Ignoring duplicate asset: " + asset.getSourceName());
        return;
      }

      fAssets.add(asset);
      fAssetFiles.add(new File(asset.getSourceName()));
      super.fireTableRowsInserted(fAssets.size() - 1, fAssets.size() - 1);
    }

    public boolean canHaveAsset(ILcdEarthAsset aAsset) {
      return aAsset != null;
    }

    public void removeAsset(int aIndex) {
      ILcdEarthAsset asset = (ILcdEarthAsset) fAssets.remove(aIndex);
      fAssetFiles.remove(new File(asset.getSourceName()));
      super.fireTableRowsDeleted(aIndex, aIndex);
    }

    public void getAssets(ILcdModel aAssetModel, int aMode) {
      aAssetModel.addElements(fAssets, aMode);
    }

    public int indexOf(ILcdEarthAsset aAsset) {
      return fAssets.indexOf(aAsset);
    }

    public AssetInfo getAssetInfo(int aIndex) {
      ILcdEarthAsset asset = getAsset(aIndex);
      if (asset instanceof ILcdEarthRasterAsset) {
        ILcdEarthRasterAsset rasterAsset = (ILcdEarthRasterAsset) asset;
        double assetPixelDensity = rasterAsset.getPixelDensity();
        int tileSize = getDefaultTileSize();
        ILcdBounds assetBounds = asset.getBounds();
        double assetArea = assetBounds.getWidth() * assetBounds.getHeight();
        double assetTileDensity = assetPixelDensity / (tileSize * tileSize);
        long numTiles = Math.round(assetArea * assetTileDensity);
        return new AssetInfo(asset, numTiles, tileSize);
      } else {
        return null;
      }
    }

    protected abstract int getDefaultTileSize();

    public boolean containsAsset(ILcdEarthAsset aAsset) {
      return fAssetFiles.contains(new File(aAsset.getSourceName()));
    }

    protected void setAsset(int aRowIndex, ILcdEarthAsset aNewAsset) {
      fAssets.set(aRowIndex, aNewAsset);
      fireTableRowsUpdated(aRowIndex, aRowIndex);
    }
  }

  private static class ElevationAssetTableModel extends AssetTableModel {
    public String getAssetTypeName() {
      return "Elevation";
    }

    public boolean canHaveAsset(ILcdEarthAsset aAsset) {
      return super.canHaveAsset(aAsset) && aAsset.getCoverageType() == ILcdEarthTileSetCoverage.CoverageType.ELEVATION;
    }

    protected int getDefaultTileSize() {
      return PreprocessorSettings.ELEVATION_TILE_RESOLUTION;
    }
  }

  private static class ImageAssetTableModel extends AssetTableModel {

    public String getAssetTypeName() {
      return "Image";
    }

    public int getColumnCount() {
      return super.getColumnCount() + 1;
    }

    public String getColumnName(int column) {
      if (isClippingShapeColumn(column)) {
        return "Clipping shape";
      } else {
        return super.getColumnName(column);
      }
    }

    private boolean isClippingShapeColumn(int column) {
      return column == getClippingShapeColumn();
    }

    private int getClippingShapeColumn() {
      return getColumnCount() - 1;
    }

    public Class getColumnClass(int columnIndex) {
      if (isClippingShapeColumn(columnIndex)) {
        return ClippingShape.class;
      } else {
        return super.getColumnClass(columnIndex);
      }
    }

    public void addAsset(ILcdEarthAsset asset) {
      if (!canHaveAsset(asset)) {
        throw new IllegalArgumentException("not a valid asset");
      }

      if (asset.getCoverageType() == ILcdEarthTileSetCoverage.CoverageType.ELEVATION) {
        ILcdEarthRasterAsset rasterAsset = (ILcdEarthRasterAsset) asset;
        asset = new TLcdEarthRasterAsset(
            rasterAsset.getSourceName(),
            rasterAsset.getModelDecoder(),
            rasterAsset.getBounds(),
            ILcdEarthTileSetCoverage.CoverageType.IMAGE,
            rasterAsset.getModificationDate(),
            rasterAsset.getPixelDensity()
        );
      }

      super.addAsset(asset);
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
      if (isClippingShapeColumn(columnIndex)) {
        ILcdEarthAsset asset = getAsset(rowIndex);
        if (asset instanceof TLcdEarthClippedRasterAsset) {
          TLcdEarthClippedRasterAsset a = (TLcdEarthClippedRasterAsset) asset;
          return new ClippingShape(a.getClipSource(), a.getClipModelDecoder());
        } else {
          return new ClippingShape(null, null);
        }
      } else {
        return super.getValueAt(rowIndex, columnIndex);
      }
    }

    public boolean isCellEditable(int rowIndex, int columnIndex) {
      return super.isCellEditable(rowIndex, columnIndex) ||
             (isClippingShapeColumn(columnIndex) &&
              canChangeClippingShape(getRasterAsset(rowIndex)));
    }

    private boolean canChangeClippingShape(ILcdEarthRasterAsset aRasterAsset) {
      return (aRasterAsset.getClass() == TLcdEarthRasterAsset.class ||
              aRasterAsset.getClass() == TLcdEarthClippedRasterAsset.class);
    }

    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
      if (isClippingShapeColumn(columnIndex)) {
        ILcdEarthRasterAsset asset = getRasterAsset(rowIndex);
        ClippingShape cs = (ClippingShape) aValue;

        ILcdEarthAsset newAsset;
        if (cs.hasShape()) {
          newAsset = new TLcdEarthClippedRasterAsset(
              asset.getSourceName(),
              asset.getModelDecoder(),
              asset.getBounds(),
              asset.getCoverageType(),
              asset.getModificationDate(),
              asset.getPixelDensity(),
              cs.getSource(),
              cs.getModelDecoder());
        } else {
          newAsset = new TLcdEarthRasterAsset(
              asset.getSourceName(),
              asset.getModelDecoder(),
              asset.getBounds(),
              asset.getCoverageType(),
              asset.getModificationDate(),
              asset.getPixelDensity()
          );
        }
        setAsset(rowIndex, newAsset);
      }
    }

    private ILcdEarthRasterAsset getRasterAsset(int aIndex) {
      return (ILcdEarthRasterAsset) getAsset(aIndex);
    }

    public boolean canHaveAsset(ILcdEarthAsset aAsset) {
      return super.canHaveAsset(aAsset) && (aAsset instanceof ILcdEarthRasterAsset);
    }

    protected int getDefaultTileSize() {
      return PreprocessorSettings.IMAGE_TILE_RESOLUTION;
    }
  }

  private static class ClippingShape {
    private String fSource;
    private ILcdModelDecoder fDecoder;

    public ClippingShape(ClippingShape aClippingShape) {
      this(aClippingShape.getSource(), aClippingShape.getModelDecoder());
    }

    public ClippingShape(String aSource, ILcdModelDecoder aDecoder) {
      if (aSource == null || aSource.trim().length() == 0) {
        fSource = null;
      } else {
        fSource = aSource;
      }
      fDecoder = aDecoder;
    }

    public String getSource() {
      return fSource;
    }

    public ILcdModelDecoder getModelDecoder() {
      return fDecoder;
    }

    public boolean hasShape() {
      return fSource != null && fDecoder != null;
    }

    public boolean isValid() {
      if (fSource == null && fDecoder == null) {
        return true;
      }
      if (fSource == null || fDecoder == null) {
        return false;
      }

      return fDecoder.canDecodeSource(fSource);
    }
  }

  private class ClippingShapeTableCellEditor extends AbstractCellEditor implements TableCellEditor {

    private ClippingShape origClippingShape;
    private ClippingShape clippingShape;

    private JPanel editPanel;
    private JTextField sourceText;
    private JButton sourceButton;

    private JFileChooser sourceChooser;

    public ClippingShapeTableCellEditor() {
      sourceText = new JTextField();
      sourceText.setEditable(true);
      sourceButton = new JButton("...");
      int defaultH = (int) sourceButton.getPreferredSize().getHeight();
      sourceButton.setPreferredSize(new Dimension(20, defaultH));
      sourceButton.setMinimumSize(new Dimension(20, defaultH));
      sourceButton.setToolTipText("Select a clipping shape");
      sourceButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          selectClippingShape();
        }
      });

      editPanel = new JPanel();
      editPanel.setLayout(new BorderLayout());
      editPanel.add(sourceText, BorderLayout.CENTER);
      editPanel.add(sourceButton, BorderLayout.EAST);
    }

    private JFileChooser getSourceChooser() {
      if (sourceChooser == null) {
        sourceChooser = new JFileChooser(".");
        sourceChooser.setMultiSelectionEnabled(false);
        sourceChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        sourceChooser.addChoosableFileFilter(new ClippingShapeFileFilter());
      }

      if (clippingShape != null && clippingShape.hasShape()) {
        sourceChooser.setCurrentDirectory(new File(clippingShape.getSource()));
      } else if (origClippingShape.hasShape()) {
        sourceChooser.setCurrentDirectory(new File(origClippingShape.getSource()));
      } else {
        sourceChooser.setCurrentDirectory(getAssetFileChooser().getCurrentDirectory());
      }

      return sourceChooser;
    }

    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected,
                                                 int row, int column) {
      origClippingShape = (ClippingShape) value;
      clippingShape = null;
      setTextValid(true);
      sourceText.setText(origClippingShape.getSource());
      if (origClippingShape.hasShape()) {
        getSourceChooser().setCurrentDirectory(new File(origClippingShape.getSource()));
      }
      return editPanel;
    }

    private void setTextValid(boolean valid) {
      if (valid) {
        sourceText.setForeground(Color.BLACK);
      } else {
        sourceText.setForeground(Color.RED);
      }
    }

    public Object getCellEditorValue() {
      if (clippingShape != null) {
        return clippingShape;
      } else {
        return origClippingShape;
      }
    }

    public boolean isCellEditable(EventObject anEvent) {
      return true;
    }

    public boolean shouldSelectCell(EventObject anEvent) {
      return true;
    }

    public boolean stopCellEditing() {
      boolean ret = updateClippingShape();
      if (ret) {
        super.fireEditingStopped();
      }
      return ret;
    }

    private boolean updateClippingShape() {
      clippingShape = new ClippingShape(
          sourceText.getText(),
          getDecoder(sourceText.getText()));

      boolean ret = clippingShape.isValid();
      if (!ret) {
        clippingShape = null;
      }
      setTextValid(ret);
      return ret;
    }

    private void selectClippingShape() {
      JFileChooser chooser = getSourceChooser();

      int choice = chooser.showOpenDialog(MetadataPanel.this);
      if (choice != JFileChooser.APPROVE_OPTION) {
        return;
      }

      File f = chooser.getSelectedFile();

      sourceText.setText(f.getPath());
      updateClippingShape();
    }

    private ILcdModelDecoder getDecoder(String aSource) {
      ILcdModelDecoder[] decoders = {
          new TLcdSHPModelDecoder()
      };
      for (ILcdModelDecoder decoder : decoders) {
        if (decoder.canDecodeSource(aSource)) {
          return decoder;
        }
      }
      return null;
    }

  }

  private static class ClippingShapeFileFilter extends FileFilter {
    public boolean accept(File f) {
      return f.isDirectory() || f.getName().toLowerCase().endsWith(".shp");
    }

    public String getDescription() {
      return "SHP file";
    }
  }

  private class CompositeTableModel extends AbstractTableModel {

    public CompositeTableModel() {
      TableModelListener myTableModelListener = new TableModelListener() {
        public void tableChanged(TableModelEvent e) {
          fireTableDataChanged();
        }
      };

      for (int i = 0; i < fAssetTables.length; i++) {
        fAssetTables[i].getModel().addTableModelListener(myTableModelListener);
      }
    }

    public int getRowCount() {
      return getAssetCount();
    }

    public int getColumnCount() {
      return 1;
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
      return getAsset(rowIndex);
    }
  }

  private static class AssetTableTransferHandler extends TransferHandler {

    public static final DataFlavor ASSET_DATA_FLAVOR = new DataFlavor(ILcdEarthAsset.class, "earth asset");

    public int getSourceActions(JComponent c) {
      return COPY;
    }

    protected Transferable createTransferable(JComponent c) {
      AssetTable table = (AssetTable) c;
      int index = table.getSelectedRow();
      if (index < 0) {
        return null;
      }
      final ILcdEarthAsset asset = table.getAssetTableModel().getAsset(index);
      return new Transferable() {
        public DataFlavor[] getTransferDataFlavors() {
          return new DataFlavor[]{ASSET_DATA_FLAVOR};
        }

        public boolean isDataFlavorSupported(DataFlavor flavor) {
          return flavor.equals(ASSET_DATA_FLAVOR);
        }

        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
          if (!isDataFlavorSupported(flavor)) {
            throw new UnsupportedFlavorException(flavor);
          }
          return asset;
        }
      };
    }

    public boolean importData(JComponent comp, Transferable t) {
      AssetTable table = (AssetTable) comp;
      try {
        ILcdEarthAsset asset = (ILcdEarthAsset) t.getTransferData(ASSET_DATA_FLAVOR);
        if (table.getAssetTableModel().canHaveAsset(asset)) {
          table.getAssetTableModel().addAsset(asset);
          return true;
        }
      } catch (Exception e) {
        sLogger.warn("Error while transfering asset", e);
      }
      return false;
    }

    public boolean canImport(JComponent comp, DataFlavor[] transferFlavors) {
      for (int i = 0; i < transferFlavors.length; i++) {
        if (transferFlavors[i].equals(ASSET_DATA_FLAVOR)) {
          return true;
        }
      }
      return false;
    }
  }

  private static class TopLevelTransferHandler extends TransferHandler {

    private JComponent fComponent;

    public TopLevelTransferHandler(JComponent aComponent) {
      fComponent = aComponent;
    }

    public boolean canImport(JComponent comp, DataFlavor[] transferFlavors) {
      return (fComponent.getTransferHandler() != null &&
              fComponent.getTransferHandler().canImport(fComponent, transferFlavors))
             || super.canImport(comp, transferFlavors);
    }

    public boolean importData(JComponent comp, Transferable t) {
      if (fComponent.getTransferHandler() != null &&
          fComponent.getTransferHandler().canImport(fComponent, t.getTransferDataFlavors())) {
        return fComponent.getTransferHandler().importData(fComponent, t);
      }
      return super.importData(comp, t);
    }
  }

  private static class TransformAssetInputMethod implements AssetFactoryInputMethod {
    private final ILcdEarthAsset fOldAsset;
    private final ILcdModelReference fOldModelReference;

    public TransformAssetInputMethod(ILcdEarthAsset aOldAsset, ILcdModelReference aOldModelReference) {
      fOldAsset = aOldAsset;
      fOldModelReference = aOldModelReference;
    }

    public ILcdModelDecoder chooseModelDecoder(File aFile, ILcdModelDecoder[] aModelDecoders) {
      ILcdModelDecoder oldModelDecoder = fOldAsset.getModelDecoder();
      ILcdModelDecoder decoder = null;
      for (ILcdModelDecoder currDecoder : aModelDecoders) {
        if (currDecoder.getClass() == oldModelDecoder.getClass()) {
          decoder = currDecoder;
          break;
        }
      }
      if (decoder == null && aModelDecoders.length == 1) {
        decoder = aModelDecoders[0];
      }
      return decoder;
    }

    public int chooseRasterLevel(File aFile, ILcdModel aModel, ILcdMultilevelRaster aRaster) {
      if (fOldAsset instanceof ILcdEarthRasterAsset) {
        try {
          ILcdEarthRasterAsset rasterAsset = (ILcdEarthRasterAsset) fOldAsset;
          double oldPixelDensity = rasterAsset.getPixelDensity();
          double oldRelPixelDensity = AssetFactory.calculateRelativePixelDensity(aModel, fOldModelReference);
          double oldPixelDensityModel = oldPixelDensity / oldRelPixelDensity;
          int bestLevel = aRaster.getRasterCount() - 1;
          double bestErr = Double.POSITIVE_INFINITY;
          for (int i = 0; i < aRaster.getRasterCount(); i++) {
            double err = Math.abs(aRaster.getRaster(i).getPixelDensity() - oldPixelDensityModel);
            if (err < bestErr) {
              bestErr = err;
              bestLevel = i;
            }
          }

          return bestLevel;
        } catch (TLcdNoBoundsException e) {
          throw new RuntimeException(e);
        } catch (TLcdOutOfBoundsException e) {
          throw new RuntimeException(e);
        }
      } else {
        return aRaster.getRasterCount() - 1;
      }
    }
  }

  private static class CreateAssetInputMethod implements AssetFactoryInputMethod {
    private final Component fParentComponent;
    private final ILcdModelDecoder fDecoder;

    public CreateAssetInputMethod(Component aParentComponent, ILcdModelDecoder aDecoder) {
      fParentComponent = aParentComponent;
      fDecoder = aDecoder;
    }

    public ILcdModelDecoder chooseModelDecoder(File aFile, ILcdModelDecoder[] aModelDecoders) {
      return fDecoder;
    }

    public int chooseRasterLevel(File aFile, ILcdModel aModel, ILcdMultilevelRaster aRaster) {
      int nLevels = aRaster.getRasterCount();
      if (nLevels <= 1) {
        return nLevels - 1;
      } else if (aModel.getModelDescriptor() instanceof TLcdDTEDModelDescriptor ||
                 aModel.getModelDescriptor() instanceof TLcdDMEDModelDescriptor ||
                 aModel.getModelDescriptor() instanceof ILcdEarthModelDescriptor) {
        // Some levels may or may not be present so let the user select it to avoid redundant preprocessing
        String[] options = new String[nLevels];
        for (int i = 0; i < options.length; i++) {
          options[i] = String.format(
              "Level %d, %.3g pixels/modelarea", i, aRaster.getRaster(i).getPixelDensity()
          );
        }
        String levelChoiceOption = (String) JOptionPane.showInputDialog(
            fParentComponent,
            String.format(
                "What is the maximum level of\n\"%s\"?\n(modelreference=%s, modelarea=%.3fx%.3f)",
                aFile.getPath(),
                aModel.getModelReference().toString(),
                aRaster.getBounds().getWidth(),
                aRaster.getBounds().getHeight()
            ),
            "Asset level?",
            JOptionPane.QUESTION_MESSAGE,
            null,
            options,
            options[nLevels - 1]
        );
        return Arrays.asList(options).indexOf(levelChoiceOption);
      } else {
        return nLevels - 1;
      }
    }
  }
}

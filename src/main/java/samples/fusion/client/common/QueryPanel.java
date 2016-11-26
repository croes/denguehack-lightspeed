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
package samples.fusion.client.common;

import static java.lang.Runtime.getRuntime;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;

import com.luciad.fusion.client.TLfnClientFactory;
import com.luciad.fusion.core.ALfnEnvironment;
import com.luciad.fusion.tilestore.ALfnTileStore;
import com.luciad.fusion.tilestore.ALfnTileStoreProvider;
import com.luciad.fusion.tilestore.ELfnDataType;
import com.luciad.fusion.tilestore.ELfnResourceType;
import com.luciad.fusion.tilestore.TLfnQuery;
import com.luciad.fusion.tilestore.TLfnQuery.Property;
import com.luciad.fusion.tilestore.TLfnServiceException;
import com.luciad.fusion.tilestore.metadata.*;
import com.luciad.gui.TLcdAWTUtil;
import com.luciad.util.TLcdPair;

import samples.fusion.util.QueryHandler;
import samples.gxy.common.ProgressUtil;

/**
 * A GUI panel used for performing a query to a LuciadFusion Tile Store,
 * either located locally on your machine or remote behind a LuciadFusion Data Server.
 */
public class QueryPanel extends JPanel {

  private static final String TILE_STORE_METADATA_FILE_NAME = "tile-store.xml";

  private static final String DEFAULT_TILE_STORE_DIR = "Data/LuciadFusion";
  private static final String DEFAULT_SERVER_URL = "http://localhost:8081/LuciadFusion/lts";
  private final ELfnResourceType fResourceType;
  private TrafficIndicator fTrafficIndicator;
  private JTextField fTileStoreUrlTextField;
  private JButton fTileStoreDirectoryButton;
  private ALfnTileStore fTileStore;
  private DataTypeFilter fDataTypeFilter;
  private JFileChooser fFileChooser;
  private final QueryListener fQueryListener;
  private final JComboBox fTypeComboBox;

  private enum DataTypeFilter {
    ANY, IMAGE, ELEVATION, MULTIVALUED, VECTOR, RASTER
  }

  public QueryPanel(ALfnEnvironment aEnvironment, TLfnClientFactory aFactory, Component aComponent,
                    ResourceHandler aResourceHandler, ELfnResourceType aResourceType) {
    super(new BorderLayout(5, 0));

    fResourceType = aResourceType;
    fQueryListener = new QueryListener(aEnvironment, aFactory, aComponent, aResourceHandler);

    fTileStoreUrlTextField = new JTextField(DEFAULT_SERVER_URL);
    fTileStoreUrlTextField.addActionListener(fQueryListener);

    fTileStoreDirectoryButton = new JButton("...");
    fTileStoreDirectoryButton.setToolTipText("Select a Tile Store on the local file system.");
    fTileStoreDirectoryButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent aEvent) {
        selectTileStore();
      }
    });
    fFileChooser = new JFileChooser(".");
    fFileChooser.setMultiSelectionEnabled(false);
    fFileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
    fFileChooser.setFileFilter(new TileStoreFileFilter());
    File tileStoreDir;
    try {
      tileStoreDir = new File(getClass().getClassLoader().getResource(DEFAULT_TILE_STORE_DIR).toURI());
    } catch (Exception e) {
      tileStoreDir = new File(DEFAULT_TILE_STORE_DIR);
    }
    fFileChooser.setCurrentDirectory(tileStoreDir);
    fFileChooser.setSelectedFile(new File(tileStoreDir, TILE_STORE_METADATA_FILE_NAME));

    // Create a button that queries the specified Tile Store for its list of available coverages.
    JButton queryButton = new JButton("Query");
    queryButton.addActionListener(fQueryListener);

    JButton trafficIndicatorButton = new JButton(
        new ImageIcon(getClass().getClassLoader().getResource("samples/fusion/images/circle_green.png")));
    trafficIndicatorButton.setDisabledIcon(
        new ImageIcon(getClass().getClassLoader().getResource("samples/fusion/images/circle_blue.png")));
    trafficIndicatorButton.setBorderPainted(false);
    trafficIndicatorButton.setContentAreaFilled(false);
    fTrafficIndicator = new TrafficIndicator(trafficIndicatorButton);
    JLabel tileStoreURLLabel = new JLabel("Tile Store URL: ");

    JLabel typeLabel = new JLabel("Data type: ");

    fTypeComboBox = new JComboBox();
    fTypeComboBox.addItem(DataTypeFilter.ANY);
    fTypeComboBox.addItem(DataTypeFilter.IMAGE);
    fTypeComboBox.addItem(DataTypeFilter.ELEVATION);
    fTypeComboBox.addItem(DataTypeFilter.MULTIVALUED);
    fTypeComboBox.addItem(DataTypeFilter.VECTOR);
    fTypeComboBox.addItem(DataTypeFilter.RASTER);
    fTypeComboBox.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent aEvent) {
        JComboBox cb = (JComboBox) aEvent.getSource();
        fDataTypeFilter = (DataTypeFilter) cb.getSelectedItem();
      }
    });
    fTypeComboBox.setSelectedIndex(0);

    setLayout(new GridBagLayout());
    add(trafficIndicatorButton,
        new GridBagConstraints(0, 0, 1, 2, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
                               new Insets(0, 0, 0, 0), 0, 0));
    add(tileStoreURLLabel, new GridBagConstraints(1, 0, 1, 1, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE,
                                                  new Insets(0, 0, 0, 0), 0, 0));
    add(typeLabel, new GridBagConstraints(1, 1, 1, 1, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE,
                                          new Insets(0, 0, 0, 0), 0, 0));
    add(fTileStoreUrlTextField,
        new GridBagConstraints(2, 0, 1, 1, 1.0, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
                               new Insets(0, 0, 0, 0), 0, 0));
    add(fTileStoreDirectoryButton,
        new GridBagConstraints(3, 0, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE,
                               new Insets(0, 0, 0, 0), 0, 0));
    add(fTypeComboBox, new GridBagConstraints(2, 1, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE,
                                              new Insets(0, 0, 0, 0), 0, 0));
    add(queryButton, new GridBagConstraints(4, 0, 1, 1, 0, 0, GridBagConstraints.NORTH, GridBagConstraints.NONE,
                                            new Insets(5, 5, 5, 5), 0, 0));
  }

  public void setSelectableTypes(ELfnDataType... aDataTypes) {
    List<ELfnDataType> dataTypes = Arrays.asList(aDataTypes);
    setSelectable(DataTypeFilter.IMAGE, dataTypes.contains(ELfnDataType.IMAGE));
    setSelectable(DataTypeFilter.ELEVATION, dataTypes.contains(ELfnDataType.ELEVATION));
    setSelectable(DataTypeFilter.MULTIVALUED, dataTypes.contains(ELfnDataType.MULTIVALUED));
    setSelectable(DataTypeFilter.VECTOR, dataTypes.contains(ELfnDataType.VECTOR));
    setSelectable(DataTypeFilter.RASTER, dataTypes.contains(ELfnDataType.RASTER));
  }

  private void setSelectable(DataTypeFilter aDataTypeFilter, boolean aSelectable) {
    boolean wasSelectable = isSelectable(aDataTypeFilter);
    if (!aSelectable && wasSelectable) {
      fTypeComboBox.removeItem(aDataTypeFilter);
    } else if (aSelectable && !wasSelectable) {
      fTypeComboBox.addItem(aDataTypeFilter);
    }
  }

  private boolean isSelectable(DataTypeFilter aDataTypeFilter) {
    for (int i = 0; i < fTypeComboBox.getItemCount(); i++) {
      if (fTypeComboBox.getItemAt(i) == aDataTypeFilter) {
        return true;
      }
    }
    return false;
  }

  public String getURL() {
    return fTileStoreUrlTextField.getText();
  }

  public ALfnTileStore getTileStore() {
    return fTileStore;
  }

  private void closeTileStore() {
    try {
      fTileStore.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void setURL(String aURL) {
    fTileStoreUrlTextField.setText(aURL);
  }

  public void query() {
    fQueryListener.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "Query"));
  }

  /**
   * An action listener which queries a Tile Store for its resources and passes the results to a {@link ResourceHandler}.
   */
  public class QueryListener implements ActionListener {

    private final ALfnEnvironment fEnvironment;
    private final TLfnClientFactory fFactory;
    private final Component fComponent;
    private final ResourceHandler fResourceHandler;
    private QueryWorker fQueryWorker;

    public QueryListener(ALfnEnvironment aEnvironment, TLfnClientFactory aFactory, Component aComponent,
                         ResourceHandler aResourceHandler) {
      fEnvironment = aEnvironment;
      fFactory = aFactory;
      fComponent = aComponent;
      fResourceHandler = aResourceHandler;
    }

    @Override
    public void actionPerformed(ActionEvent aEvent) {
      if (fQueryWorker != null && !fQueryWorker.isDone()) {
        fQueryWorker.cancel(true);
      }
      fQueryWorker = new QueryWorker(fEnvironment, fFactory, fComponent, fResourceHandler);
      fQueryWorker.execute();
    }
  }

  private class QueryWorker extends SwingWorker<List<ResourceHandler.ResourceInfo>, Void> {

    private final Component fComponent;
    private final ResourceHandler fResourceHandler;
    private final JDialog fProgress;
    private final ALfnTileStoreProvider fTileStoreProvider;

    public QueryWorker(ALfnEnvironment aEnvironment, TLfnClientFactory aFactory, Component aComponent,
                       ResourceHandler aResourceHandler) {
      fTileStoreProvider = new TrafficIndicatorTileStoreProvider(aFactory, aEnvironment, fTrafficIndicator);
      fComponent = aComponent;
      fResourceHandler = aResourceHandler;
      fProgress = ProgressUtil.createProgressDialog(fComponent, "Querying LuciadFusion Tile Store...");
      ProgressUtil.showDialog(fProgress);
    }

    @Override
    protected List<ResourceHandler.ResourceInfo> doInBackground() throws Exception {
      URI uri = new URI(getURL());
      fTileStore = fTileStoreProvider.getTileStore(uri);
      fResourceHandler.updateTileStore(fTileStore);
      getRuntime().addShutdownHook(new Thread() {

        @Override
        public void run() {
          closeTileStore();
        }
      });
      TLfnQuery query = createQuery(fResourceType, fDataTypeFilter);
      List<ALfnResourceMetadata> resources = queryResources(query);

      List<ResourceHandler.ResourceInfo> resourceInfos = new ArrayList<>(resources.size());
      for (ALfnResourceMetadata resourceMetadata : resources) {
        GetResourceTypeVisitor visitor = new GetResourceTypeVisitor();
        resourceMetadata.accept(visitor);
        resourceInfos.add(new ResourceHandler.ResourceInfo(resourceMetadata.getId(), resourceMetadata.getName(),
                                                           visitor.getResourceType(), resourceMetadata));
      }
      return resourceInfos;
    }

    @Override
    protected void done() {
      try {
        List<ResourceHandler.ResourceInfo> resourceInfos = get();
        if (resourceInfos.isEmpty()) {
          JOptionPane
              .showMessageDialog(TLcdAWTUtil.findParentFrame(fComponent), "No resources available", "Query result",
                                 JOptionPane.ERROR_MESSAGE);
        }
        fResourceHandler.updateTileStore(fTileStore);
        fResourceHandler.updateResources(resourceInfos);
        ProgressUtil.hideDialog(fProgress);
      } catch (ExecutionException e) {
        JOptionPane.showMessageDialog(TLcdAWTUtil.findParentFrame(fComponent),
                                      "Failed to query Tile Store: " + getURL() + " because: " + e.getCause()
                                                                                                  .getMessage(),
                                      "Query result", JOptionPane.ERROR_MESSAGE);
        ProgressUtil.hideDialog(fProgress);
      } catch (InterruptedException e) {
        ProgressUtil.hideDialog(fProgress);
      }
    }

    private List<ALfnResourceMetadata> queryResources(TLfnQuery aQuery)
        throws IOException, TLfnServiceException, InterruptedException, ExecutionException {
      // Execute the query
      QueryHandler queryHandler = new QueryHandler();
      Future<?> future = fTileStore.query(queryHandler, aQuery);
      future.get();
      return queryHandler.awaitResults();
    }

    private TLfnQuery createQuery(ELfnResourceType aResourceType, DataTypeFilter aDataTypeFilter) throws Exception {
      TLfnQuery.BasicBuilder builder = TLfnQuery.newBasicBuilder();
      builder.resourceTypes(aResourceType);
      if (aDataTypeFilter != DataTypeFilter.ANY) {
        builder.andFilterEquals(Property.ASSET_OR_COVERAGE_type, getSingleDataType(aDataTypeFilter));
      } else { // ANY
        // Allow IMAGE or ELEVATION or MULTIVALUED or VECTOR or RASTER as type
        builder.andFilterAnyEqual(new TLcdPair<Property, Object>(Property.ASSET_OR_COVERAGE_type, ELfnDataType.IMAGE),
                                  new TLcdPair<Property, Object>(Property.ASSET_OR_COVERAGE_type,
                                                                 ELfnDataType.ELEVATION),
                                  new TLcdPair<Property, Object>(Property.ASSET_OR_COVERAGE_type,
                                                                 ELfnDataType.MULTIVALUED),
                                  new TLcdPair<Property, Object>(Property.ASSET_OR_COVERAGE_type, ELfnDataType.VECTOR),
                                  new TLcdPair<Property, Object>(Property.ASSET_OR_COVERAGE_type, ELfnDataType.RASTER));
      }
      builder.sortBy(Property.RESOURCE_name);
      return builder.build();
    }
  }

  /**
   * Returns the single data type for a filter.
   *
   * @param aDataTypeFilter the filter
   * @return the data type
   * @throws IllegalArgumentException if the specified filter is not for a single data type
   */
  private ELfnDataType getSingleDataType(DataTypeFilter aDataTypeFilter) {
    switch (aDataTypeFilter) {
    case IMAGE:
      return ELfnDataType.IMAGE;
    case ELEVATION:
      return ELfnDataType.ELEVATION;
    case MULTIVALUED:
      return ELfnDataType.MULTIVALUED;
    case VECTOR:
      return ELfnDataType.VECTOR;
    case RASTER:
      return ELfnDataType.RASTER;
    default:
      throw new IllegalArgumentException("No a single data type filter: " + aDataTypeFilter);
    }
  }

  /**
   * A resource metadata visitor which keeps track of a string representation of the resource type of the last visited resource metadata.
   */
  private static class GetResourceTypeVisitor extends ALfnResourceMetadataVisitor {

    private String fResourceType = null;

    @Override
    public void visit(TLfnThemeMetadata aTheme) {
      fResourceType = ResourceHandler.ResourceInfo.TYPE_THEME;
    }

    @Override
    public void visit(TLfnRasterCoverageMetadata aCoverage) {
      visit((ALfnCoverageMetadata) aCoverage);
    }

    @Override
    public void visit(TLfnVectorCoverageMetadata aCoverage) {
      visit((ALfnCoverageMetadata) aCoverage);
    }

    @Override
    public void visit(TLfnCoverageMetadata aCoverage) {
      visit((ALfnCoverageMetadata) aCoverage);
    }

    private void visit(ALfnCoverageMetadata aCoverage) {
      fResourceType = ResourceHandler.ResourceInfo.TYPE_COVERAGE;
    }

    @Override
    public void visit(TLfnRasterAssetMetadata aAsset) {
      visit((ALfnAssetMetadata) aAsset);
    }

    @Override
    public void visit(TLfnVectorAssetMetadata aAsset) {
      visit((ALfnAssetMetadata) aAsset);
    }

    private void visit(ALfnAssetMetadata aAsset) {
      fResourceType = ResourceHandler.ResourceInfo.TYPE_ASSET;
    }

    public String getResourceType() {
      return fResourceType;
    }
  }

  private void selectTileStore() {
    File currFile = new File(fTileStoreUrlTextField.getText());
    if (currFile.exists()) {
      fFileChooser.setCurrentDirectory(currFile);
    }

    int choice = fFileChooser.showOpenDialog(this);
    if (choice != JFileChooser.APPROVE_OPTION) {
      return;
    }

    File selectedFile = fFileChooser.getSelectedFile();

    try {
      fTileStoreUrlTextField.setText(selectedFile.toURI().toURL().toString());
    } catch (IOException e) {
      // ignore
    }
  }

  /**
   * A file filter accepting only directories and {@code tile-store.xml} files.
   * It is used by the Tile Store "file open" dialog: only directories are potential Tile Stores.
   */
  private static class TileStoreFileFilter extends FileFilter {
    @Override
    public boolean accept(File aFile) {
      return aFile.isDirectory() || aFile.isFile() && TILE_STORE_METADATA_FILE_NAME.equals(aFile.getName());
    }

    @Override
    public String getDescription() {
      return "LuciadFusion Tile Store";
    }
  }
}

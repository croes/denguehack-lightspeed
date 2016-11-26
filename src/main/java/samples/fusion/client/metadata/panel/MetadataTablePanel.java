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
package samples.fusion.client.metadata.panel;

import static samples.fusion.client.metadata.panel.MetadataTableModel.*;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableCellEditor;

import com.luciad.format.metadata.model.citation.TLcdISO19115Citation;
import com.luciad.format.metadata.model.citation.TLcdISO19115CitationSeries;
import com.luciad.format.metadata.model.constraint.TLcdISO19115ClassificationCode;
import com.luciad.format.metadata.model.constraint.TLcdISO19115Constraints;
import com.luciad.format.metadata.model.constraint.TLcdISO19115SecurityConstraints;
import com.luciad.format.metadata.model.identification.TLcdISO19115Identification;
import com.luciad.format.metadata.model.identification.TLcdISO19115ServiceIdentification;
import com.luciad.format.metadata.model.metadataentityset.TLcdISO19115Metadata;
import com.luciad.fusion.tilestore.ALfnTileStore;
import com.luciad.fusion.tilestore.TLfnServiceException;
import com.luciad.fusion.tilestore.metadata.ALfnAssetMetadata;

import samples.fusion.client.common.QueryPanel;

/**
 * Panel showing metadata as a table.
 */
public class MetadataTablePanel extends JPanel {

  private final MetadataTableModel fModel;

  private final QueryPanel fQueryServerPanel;

  private JTable fTable;

  private JButton fCommit;

  private AssetHolder fSelectedAsset = null;

  private JLabel fCommitStatus;

  public MetadataTablePanel(QueryPanel aQueryServerPanel) {
    super(new BorderLayout());

    fQueryServerPanel = aQueryServerPanel;

    fModel = new MetadataTableModel();

    fTable = new JTable(fModel);
    fTable.setCellSelectionEnabled(true);
    fTable.setPreferredScrollableViewportSize(new Dimension(300, 130));
    fTable.setRowSelectionAllowed(true);
    fTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

    JScrollPane scrollPane = new JScrollPane(fTable);
    add(scrollPane, BorderLayout.CENTER);

    JPanel commitPanel = new JPanel(new BorderLayout());
    fCommit = new JButton("Commit changes");
    fCommit.addActionListener(new CommitListener());
    fCommitStatus = new JLabel();
    commitPanel.add(fCommit, BorderLayout.NORTH);
    commitPanel.add(fCommitStatus, BorderLayout.SOUTH);

    add(commitPanel, BorderLayout.EAST);

    setSelectedAsset(null);
  }

  public void setSelectedAsset(AssetHolder aAsset) {
    fSelectedAsset = aAsset;
    fCommit.setEnabled(fSelectedAsset != null);
    fTable.setEnabled(fSelectedAsset != null);
    fModel.setEnabled(fSelectedAsset != null);
    updateCommitStatus();
  }

  private void updateCommitStatus() {
    if (fSelectedAsset != null) {
      fCommitStatus.setText("Committed version: " + fSelectedAsset.getMetadata().getUpdateSequence());
    } else {
      fCommitStatus.setText(null);
    }
  }

  public JTable getTable() {
    return fTable;
  }

  public void reset() {
    ((MetadataTableModel) fTable.getModel()).reset();
    setSelectedAsset(null);
  }

  /**
   * Action listener committing changes to metadata properties to the Tile Store.
   */
  private class CommitListener implements ActionListener {

    private ALfnAssetMetadata updateAsset(ALfnAssetMetadata aResourceMetadata) {
      if (aResourceMetadata != null) {
        // Apply any pending changes
        TableCellEditor cellEditor = fTable.getCellEditor();
        if (cellEditor != null) {
          if (!cellEditor.stopCellEditing()) {
            cellEditor.cancelCellEditing();
          }
        }

        // Commit
        String resourceName = fModel.getValue(RESOURCE_NAME_ROW);

        TLcdISO19115Metadata isoMetadata = aResourceMetadata.getISO19115Metadata();
        if (isoMetadata == null) {
          isoMetadata = new TLcdISO19115Metadata();
        }

        TLcdISO19115Identification serviceIdentification = new TLcdISO19115ServiceIdentification();

        TLcdISO19115Citation citation = new TLcdISO19115Citation();
        citation.setTitle(fModel.getValue(CITATION_TITLE_ROW));

        TLcdISO19115CitationSeries series = new TLcdISO19115CitationSeries();
        series.setName(fModel.getValue(CITATION_SERIES_NAME_ROW));
        citation.setSeries(series);

        serviceIdentification.setCitation(citation);

        List<TLcdISO19115Identification> identificationInfo = isoMetadata.getIdentificationInfo();
        identificationInfo.clear();
        identificationInfo.add(serviceIdentification);

        String secClass = fModel.getValue(SECURITY_CLASSIFICATION_ROW);

        if (hasValue(secClass)) {
          setSecurityConstraints(isoMetadata, getClassificationCode(secClass));
        } else {
          List<TLcdISO19115Constraints> metadataConstraints = isoMetadata.getMetadataConstraints();
          metadataConstraints.clear();
        }

        ALfnAssetMetadata.Builder assetBuilder = aResourceMetadata.asBuilder();
        assetBuilder.name(resourceName);
        assetBuilder.iso19115Metadata(isoMetadata);
        return assetBuilder.build();
      }
      return null;
    }

    private void setSecurityConstraints(TLcdISO19115Metadata aMetadata,
                                        TLcdISO19115ClassificationCode aClassificationCode) {
      TLcdISO19115SecurityConstraints esc = new TLcdISO19115SecurityConstraints();
      esc.setClassification(aClassificationCode);

      List<TLcdISO19115Constraints> metadataConstraints = aMetadata.getMetadataConstraints();
      metadataConstraints.clear();
      metadataConstraints.add(esc);
    }

    private boolean hasValue(String... aValues) {
      for (String value : aValues) {
        if (value != null && value.length() > 0) {
          return true;
        }
      }
      return false;
    }

    private TLcdISO19115ClassificationCode getClassificationCode(String aString) {
      if ("confidential".equalsIgnoreCase(aString)) {
        return TLcdISO19115ClassificationCode.CONFIDENTIAL;
      }
      if ("restricted".equalsIgnoreCase(aString)) {
        return TLcdISO19115ClassificationCode.RESTRICTED;
      }
      if ("secret".equalsIgnoreCase(aString)) {
        return TLcdISO19115ClassificationCode.SECRET;
      }
      if ("topSecret".equalsIgnoreCase(aString)) {
        return TLcdISO19115ClassificationCode.TOP_SECRET;
      }
      return TLcdISO19115ClassificationCode.UNCLASSIFIED;
    }

    public void actionPerformed(ActionEvent aEvent) {
      ALfnTileStore tileStore = fQueryServerPanel.getTileStore();
      if (tileStore == null) {
        JOptionPane.showMessageDialog(fQueryServerPanel, "Not connected to Tile Store");
        return;
      }

      if (fSelectedAsset == null) {
        JOptionPane.showMessageDialog(fQueryServerPanel, "No asset selected");
        return;
      }

      try {
        ALfnAssetMetadata updatedAsset = fQueryServerPanel.getTileStore().putResourceMetadata(
            updateAsset(fSelectedAsset.getMetadata()));
        fSelectedAsset.setMetadata(updatedAsset);
        updateCommitStatus();
      } catch (IOException | TLfnServiceException e1) {
        JOptionPane.showMessageDialog(fQueryServerPanel, "Failed to commit metadata");
      }
    }
  }

}

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

import java.util.Collection;
import java.util.Enumeration;
import java.util.List;

import com.luciad.format.metadata.model.citation.TLcdISO19115Citation;
import com.luciad.format.metadata.model.constraint.TLcdISO19115ClassificationCode;
import com.luciad.format.metadata.model.constraint.TLcdISO19115Constraints;
import com.luciad.format.metadata.model.constraint.TLcdISO19115SecurityConstraints;
import com.luciad.format.metadata.model.identification.TLcdISO19115Identification;
import com.luciad.format.metadata.model.metadataentityset.TLcdISO19115Metadata;
import com.luciad.fusion.tilestore.ALfnTileStore;
import com.luciad.fusion.tilestore.metadata.ALfnAssetMetadata;
import com.luciad.util.ILcdSelectionListener;
import com.luciad.util.TLcdSelectionChangedEvent;

import samples.fusion.client.common.QueryPanel;

/**
 * A selection listener for the metadata sample.
 */
public class MetadataSelectionListener implements ILcdSelectionListener {

  private final QueryPanel fQueryServerPanel;

  private final MetadataTablePanel fTablePanel;

  public MetadataSelectionListener(QueryPanel aQueryServerPanel, MetadataTablePanel aTablePanel) {
    fQueryServerPanel = aQueryServerPanel;
    fTablePanel = aTablePanel;
  }

  public void selectionChanged(TLcdSelectionChangedEvent aSelectionChangedEvent) {
    ALfnTileStore tileStore = fQueryServerPanel.getTileStore();
    if (tileStore == null) {
      return;
    }

    AssetHolder assetHolder = null;
    Enumeration en = aSelectionChangedEvent.selectedElements();
    while (en.hasMoreElements()) {
      Object selectedElem = en.nextElement();
      if (selectedElem instanceof AssetHolder) {
        assetHolder = (AssetHolder) aSelectionChangedEvent.selectedElements().nextElement();
        break;
      }
    }

    fTablePanel.reset();
    fTablePanel.setSelectedAsset(assetHolder);
    if (assetHolder != null) {
      ALfnAssetMetadata assetMetadata = assetHolder.getMetadata();

      MetadataTableModel metadataTableModel = (MetadataTableModel) fTablePanel.getTable().getModel();
      metadataTableModel.setValueAt(assetMetadata.getName(), MetadataTableModel.RESOURCE_NAME_ROW, 1);

      TLcdISO19115Metadata iso19115Metadata = assetMetadata.getISO19115Metadata();
      if (iso19115Metadata != null) {
        List<TLcdISO19115Identification> identificationInfo = iso19115Metadata.getIdentificationInfo();
        if (hasProperty(identificationInfo)) {
          TLcdISO19115Citation citation = identificationInfo.get(0).getCitation();
          if (citation != null) {
            metadataTableModel.setValueAt(citation.getTitle(), MetadataTableModel.CITATION_TITLE_ROW, 1);
            if (citation.getSeries() != null) {
              metadataTableModel
                  .setValueAt(citation.getSeries().getName(), MetadataTableModel.CITATION_SERIES_NAME_ROW, 1);
            }
          }
        }

        List<TLcdISO19115Constraints> constraints = iso19115Metadata.getMetadataConstraints();
        if (constraints != null && !constraints.isEmpty()) {
          TLcdISO19115ClassificationCode classificationCode = ((TLcdISO19115SecurityConstraints) constraints.get(0))
              .getClassification();
          if (classificationCode != null) {
            String classification = classificationCode.getValueObject();
            metadataTableModel.setValueAt(classification, MetadataTableModel.SECURITY_CLASSIFICATION_ROW, 1);
          }
        }
      }
    }
  }

  private boolean hasProperty(Object aObject) {
    if ((aObject == null) || (aObject instanceof Collection && ((Collection) aObject).isEmpty())) {
      return false;
    }
    return true;
  }
}

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

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.luciad.fusion.tilestore.ALfnTileStore;

/**
 * A GUI panel for handling the selection of coverages retrieved from a LuciadFusion Tile Store.
 */
public class CoverageListPanel extends JPanel implements ResourceHandler {

  public enum SelectionMode {
    ONE_COVERAGE,
    MULTIPLE_COVERAGES
  }

  private final JList<ResourceInfo> fCoverageList;
  private final JButton fCreateLayerButton;
  private List<ResourceInfo> fCoverages;
  private SelectionMode fSelectionMode;
  private int fEventCounter;

  public CoverageListPanel(final ActionListener aActionListener) {
    fCoverages = Collections.emptyList();
    fCoverageList = new JList<ResourceInfo>(fCoverages.toArray(new ResourceInfo[fCoverages.size()])) {
      @Override
      public Dimension getPreferredScrollableViewportSize() {
        return new Dimension((int) Math.min(200, Math.max(150, getPreferredSize().getWidth())),
                             (int) Math.min(250, Math.max(150, getPreferredSize().getHeight())));
      }
    };
    JScrollPane scrollPane = new JScrollPane(fCoverageList);
    scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
    fCoverageList.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() >= 2) {
          aActionListener.actionPerformed(new ActionEvent(fCoverageList, fEventCounter++, null));
        }
      }
    });
    fCreateLayerButton = new JButton("Create layer");
    fCreateLayerButton.addActionListener(aActionListener);
    fCreateLayerButton.setEnabled(false);
    fCreateLayerButton.setAlignmentX(0.5f);

    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    add(scrollPane);
    add(Box.createVerticalStrut(5));
    add(fCreateLayerButton);
    setSelectionMode(SelectionMode.ONE_COVERAGE);
  }

  public void setSelectionMode(SelectionMode aSelectionMode) {
    fSelectionMode = aSelectionMode;
    switch (fSelectionMode) {
    case ONE_COVERAGE:
      fCoverageList.setSelectionModel(new DefaultListSelectionModel());
      fCoverageList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      break;
    case MULTIPLE_COVERAGES:
      fCoverageList.setSelectionModel(new DefaultListSelectionModel());
      fCoverageList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
      break;
    }
    fCoverageList.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
      @Override
      public void valueChanged(ListSelectionEvent e) {
        fCreateLayerButton.setEnabled(fCoverageList.getSelectedIndex() != -1);
      }
    });
  }

  public SelectionMode getSelectionMode() {
    return fSelectionMode;
  }

  public ResourceInfo[] getSelectedCoverages() {
    List<ResourceInfo> selectedValues = fCoverageList.getSelectedValuesList();
    return selectedValues.toArray(new ResourceInfo[selectedValues.size()]);
  }

  public ResourceInfo[] getCoverages() {
    ResourceInfo[] resourceInfos = new ResourceInfo[fCoverageList.getModel().getSize()];
    for (int i = 0; i < resourceInfos.length; i++) {
      resourceInfos[i] = fCoverageList.getModel().getElementAt(i);
    }
    return resourceInfos;
  }

  @Override
  public void updateTileStore(ALfnTileStore aTileStore) {
  }

  @Override
  public void updateResources(List<ResourceInfo> aResources) {
    fCoverages = new ArrayList<>();
    for (ResourceInfo ri : aResources) {
      if ("Coverage".equalsIgnoreCase(ri.getType())) {
        fCoverages.add(ri);
      }
    }

    fCoverageList.setEnabled(true);

    if (!fCoverages.isEmpty()) {
      fCoverageList.setSelectedValue(fCoverages.get(0), true);
    }

    // Redo the layout so the coverages list combo box gets updated.
    fCoverageList.setModel(new DefaultComboBoxModel<>(fCoverages.toArray(new ResourceInfo[fCoverages.size()])));
    revalidate();
  }
}

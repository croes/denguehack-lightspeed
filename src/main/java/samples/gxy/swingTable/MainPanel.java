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
package samples.gxy.swingTable;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.IOException;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;

import com.luciad.gui.TLcdAWTUtil;
import com.luciad.model.ILcdIntegerIndexedModel;
import com.luciad.shape.ILcdBounds;
import com.luciad.shape.shape2D.TLcdLonLatBounds;
import com.luciad.view.gxy.ILcdGXYLayer;
import com.luciad.view.gxy.TLcdGXYLayer;

import samples.common.SampleData;
import samples.gxy.common.GXYSample;
import samples.gxy.common.layers.GXYDataUtil;

/**
 * Displays an ILcdModel's elements in a JTable.
 */
public class MainPanel extends GXYSample {

  private JTabbedPane fJTabbedPane = new JTabbedPane();

  @Override
  protected ILcdBounds getInitialBounds() {
    return new TLcdLonLatBounds(-90.00, 25.00, 20.00, 15.00);
  }

  @Override
  protected JPanel createBottomPanel() {
    JPanel panel = new JPanel(new BorderLayout());
    // set default panel height
    fJTabbedPane.setPreferredSize(new Dimension(0, 200));
    panel.add(fJTabbedPane);
    return panel;
  }

  protected void addData() throws IOException {
    GXYDataUtil.instance().model(SampleData.COUNTRIES).layer().label("Countries").selectable(false).addToView(getView());
    final ILcdGXYLayer layerStates = GXYDataUtil.instance().model(SampleData.US_STATES).layer().label("States").addToView(getView()).getLayer();
    final ILcdGXYLayer layerCities = GXYDataUtil.instance().model(SampleData.US_CITIES).layer().label("Cities").addToView(getView()).getLayer();

    // Adjust the GUI in the Swing thread.
    TLcdAWTUtil.invokeNowOrLater(new Runnable() {
      @Override
      public void run() {
        if (layerStates != null) {
          JTable tableStates = new JTable();
          tableStates.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
          tableStates.setColumnSelectionAllowed(false);
          tableStates.setCellSelectionEnabled(false);
          tableStates.setRowSelectionAllowed(true);
          tableStates.setModel(new DataObjectTableModel((ILcdIntegerIndexedModel) layerStates.getModel()));
          fJTabbedPane.addTab(layerStates.getLabel(), new JScrollPane(tableStates));

          SelectionMediator selectionMediator = new SelectionMediator(layerStates, getView(), tableStates);
          layerStates.setSelectable(true);
          layerStates.addSelectionListener(selectionMediator);
          tableStates.getSelectionModel().addListSelectionListener(selectionMediator);

          // By default, objects smaller than 1 pixel are ignored for painting and selection.
          // The following makes sure that all states are considered in selection rectangles, regardless of their size.
          ((TLcdGXYLayer) layerStates).setMinimumObjectSizeForPainting(0);
        }

        if (layerCities != null) {
          layerCities.setLabeled(true);

          JTable tableCities = new JTable();
          tableCities.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
          tableCities.setColumnSelectionAllowed(false);
          tableCities.setCellSelectionEnabled(false);
          tableCities.setRowSelectionAllowed(true);
          tableCities.setModel(new DataObjectTableModel((ILcdIntegerIndexedModel) layerCities.getModel()));
          fJTabbedPane.addTab(layerCities.getLabel(), new JScrollPane(tableCities));

          SelectionMediator selectionMediator = new SelectionMediator(layerCities, getView(), tableCities);
          layerCities.setSelectable(true);
          layerCities.addSelectionListener(selectionMediator);
          tableCities.getSelectionModel().addListSelectionListener(selectionMediator);
        }
      }
    });

  }

  public static void main(final String[] aArgs) {
    startSample(MainPanel.class, "Model in a table");
  }
}

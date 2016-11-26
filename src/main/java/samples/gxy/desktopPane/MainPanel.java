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
package samples.gxy.desktopPane;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;

import com.luciad.gui.TLcdAWTUtil;
import samples.common.LuciadFrame;
import com.luciad.model.ILcdIntegerIndexedModel;
import com.luciad.shape.shape2D.TLcdLonLatBounds;
import com.luciad.view.gxy.ILcdGXYLayer;
import com.luciad.view.map.TLcdMapJPanel;

import samples.common.SampleData;
import samples.common.SamplePanel;
import samples.gxy.common.SampleMapJPanelFactory;
import samples.gxy.common.layers.GXYDataUtil;
import samples.gxy.common.toolbar.ToolBar;
import samples.gxy.swingTable.DataObjectTableModel;
import samples.gxy.swingTable.SelectionMediator;

/**
 * This is a sample for displaying, in a JTable, Objects contained an ILcdModel.
 * All the Objects implement ILcdDataObject, i.e.<!-- --> there is a set of attributes
 * associated to each of them.
 * <p/>
 * In this sample, we use a JDesktopPane with 2 internal frames: one that
 * contains a map view (TLcdMapJPanel) and one containing a JTabbedPane based
 * view containing several JTable objects.
 * <p/>
 * We load several ILcdModel instances which are displayed/represented in both
 * the TLcdMapJPanel and the JTabbedPane (i.e. each ILcdModel is represented
 * in 2 different views. For each ILcdModel, there is a
 * corresponding ILcdGXYLayer in the TLcdMapJPanel and a JTable in the
 * JTabbedPane. The column names of each JTable correspond to the attributes names,
 * the rows correspond to the attribute values.
 */
public class MainPanel extends SamplePanel {

  private JDesktopPane fDesktopPane = new JDesktopPane();
  private TLcdMapJPanel fMapJPanel = SampleMapJPanelFactory.createMapJPanel(new TLcdLonLatBounds(-125, 25, 60.0, 30.0));
  private JTabbedPane fJTabbedPane = new JTabbedPane();

  private JInternalFrame fMainMapInternalFrame = new JInternalFrame();
  private JInternalFrame fTabbedPaneInternalFrame = new JInternalFrame();

  protected void createGUI() {
    // Create the default toolbar.
    ToolBar toolBar = new ToolBar(fMapJPanel, true, this);

    fJTabbedPane.setTabPlacement(JTabbedPane.BOTTOM);

    fMainMapInternalFrame.getContentPane().setLayout(new BorderLayout());
    fMainMapInternalFrame.getContentPane().add(BorderLayout.NORTH, toolBar);
    fMainMapInternalFrame.getContentPane().add(BorderLayout.CENTER, fMapJPanel);
    fMainMapInternalFrame.setBounds(30, 30, 600, 400);
    fMainMapInternalFrame.setResizable(true);
    fMainMapInternalFrame.setIconifiable(true);
    fMainMapInternalFrame.setTitle("Map");

    fTabbedPaneInternalFrame.getContentPane().setLayout(new BorderLayout());
    fTabbedPaneInternalFrame.getContentPane().add(BorderLayout.CENTER, fJTabbedPane);
    fTabbedPaneInternalFrame.setBounds(150, 370, 600, 180);
    fTabbedPaneInternalFrame.setResizable(true);
    fTabbedPaneInternalFrame.setIconifiable(true);
    fTabbedPaneInternalFrame.setTitle("Table");

    fDesktopPane.add(fMainMapInternalFrame);
    fDesktopPane.add(fTabbedPaneInternalFrame);

    setLayout(new BorderLayout());
    add(BorderLayout.CENTER, fDesktopPane);

    fMainMapInternalFrame.setVisible(true);
    fTabbedPaneInternalFrame.setVisible(true);
    fTabbedPaneInternalFrame.moveToFront();
  }

  protected void addData() {
    GXYDataUtil.instance().model(SampleData.COUNTRIES).layer().label("Countries").selectable(false).addToView(fMapJPanel);

    final ILcdGXYLayer layerStates = GXYDataUtil.instance().model(SampleData.US_STATES).layer().label("States").addToView(fMapJPanel).getLayer();
    if (layerStates != null) {
      final JTable tableStates = new JTable();
      tableStates.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
      tableStates.setColumnSelectionAllowed(false);
      tableStates.setCellSelectionEnabled(false);
      tableStates.setRowSelectionAllowed(true);
      tableStates.setModel(new DataObjectTableModel((ILcdIntegerIndexedModel) layerStates.getModel()));
      TLcdAWTUtil.invokeNowOrLater(new Runnable() {
        public void run() {
          fJTabbedPane.addTab(layerStates.getLabel(), new JScrollPane(tableStates));
        }
      });

      SelectionMediator selectionMediator = new SelectionMediator(layerStates, fMapJPanel, tableStates);
      layerStates.setSelectable(true);
      layerStates.addSelectionListener(selectionMediator);
      tableStates.getSelectionModel().addListSelectionListener(selectionMediator);
    }

    final ILcdGXYLayer layerCities = GXYDataUtil.instance().model(SampleData.US_CITIES).layer().label("Cities").addToView(fMapJPanel).getLayer();
    if (layerCities != null) {
      layerCities.setLabeled(true);

      final JTable tableCities = new JTable();
      tableCities.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
      tableCities.setColumnSelectionAllowed(false);
      tableCities.setCellSelectionEnabled(false);
      tableCities.setRowSelectionAllowed(true);
      tableCities.setModel(new DataObjectTableModel((ILcdIntegerIndexedModel) layerCities.getModel()));
      TLcdAWTUtil.invokeNowOrLater(new Runnable() {
        public void run() {
          fJTabbedPane.addTab(layerCities.getLabel(), new JScrollPane(tableCities));
        }
      });

      SelectionMediator selectionMediator = new SelectionMediator(layerCities, fMapJPanel, tableCities);
      layerCities.setSelectable(true);
      layerCities.addSelectionListener(selectionMediator);
      tableCities.getSelectionModel().addListSelectionListener(selectionMediator);
    }
  }

  public static void main(final String[] aArgs) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        new LuciadFrame(new MainPanel(), "Map in a desktop pane");
      }
    });
  }
}

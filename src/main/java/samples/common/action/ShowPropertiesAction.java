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
package samples.common.action;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.JTree;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeModel;

import com.luciad.datamodel.ILcdDataObject;
import com.luciad.gui.TLcdAWTUtil;
import com.luciad.gui.TLcdIconFactory;
import com.luciad.gui.swing.TLcdSWIcon;
import com.luciad.model.ILcdModel;
import com.luciad.util.ILcdFilter;
import com.luciad.view.ALcdObjectSelectionAction;
import com.luciad.view.ILcdView;
import com.luciad.view.TLcdDomainObjectContext;

import samples.common.SwingUtil;
import samples.common.dataObjectDisplayTree.DataObjectAssociationClassRemovingTreeModel;
import samples.common.dataObjectDisplayTree.DataObjectDisplayTree;
import samples.common.dataObjectDisplayTree.DataObjectEmptyValueFilteringTreeModel;
import samples.common.dataObjectDisplayTree.DataObjectTreeCellRenderer;
import samples.common.dataObjectDisplayTree.DataObjectTreeModel;

/**
 * Pops up a Frame to view the properties of the first selected
 * ILcdDataObject object in an ILcdView.
 */
public class ShowPropertiesAction extends ALcdObjectSelectionAction {

  private Component fParentComponent;
  private JDialog fDialog;
  private JScrollPane fScrollPane;
  private DataObjectDisplayTree fDataObjectCustomizer;
  private JButton fExpandButton;
  private JButton fCollapseButton;
  private JToggleButton fFilterEmptyButton;
  private TreeCellRenderer fTreeCellRenderer;

  private TreeModel fFilteredTreeModel = new DataObjectEmptyValueFilteringTreeModel(new DataObjectAssociationClassRemovingTreeModel(new DataObjectTreeModel()));
  private TreeModel fNonFilteredTreeModel = new DataObjectTreeModel();

  /**
   * Creates an edit selection action with a default tree cell renderer.
   * @param aView            a view
   * @param aParentComponent the parent component containing the action
   */
  public ShowPropertiesAction(ILcdView aView, Component aParentComponent) {
    this(aView, aParentComponent, null);
  }

  /**
   * Creates an edit selection action with a default tree cell renderer.
   * @param aView            a view
   * @param aParentComponent the parent component containing the action
   */
  public ShowPropertiesAction(ILcdView aView, Component aParentComponent, ILcdFilter<TLcdDomainObjectContext> aObjectFilter) {
    super(aView,
          getObjectFilter(aObjectFilter),
          1, // minimum selection count
          1, // maximum selection count
          true); // strict
    setName("View properties");
    fParentComponent = aParentComponent;
  }

  /**
   * Creates an edit selection action with a custom tree cell renderer.
   * @param aView             a view
   * @param aParentComponent  the parent component containing the action
   * @param aTreeCellRenderer a custom tree cell renderer
   */
  public ShowPropertiesAction(ILcdView aView, Component aParentComponent, TreeCellRenderer aTreeCellRenderer, ILcdFilter<TLcdDomainObjectContext> aObjectFilter) {
    this(aView, aParentComponent, aObjectFilter);
    fTreeCellRenderer = aTreeCellRenderer;
  }

  private static ILcdFilter<TLcdDomainObjectContext> getObjectFilter(ILcdFilter<TLcdDomainObjectContext> aObjectFilter) {
    ILcdFilter<TLcdDomainObjectContext> dataObjectFilter = new ILcdFilter<TLcdDomainObjectContext>() {
      @Override
      public boolean accept(TLcdDomainObjectContext aObject) {
        return aObject.getDomainObject() instanceof ILcdDataObject;
      }
    };
    if (aObjectFilter == null) {
      return dataObjectFilter;
    } else {
      CompositeAndFilter<TLcdDomainObjectContext> compositeAndFilter = new CompositeAndFilter<TLcdDomainObjectContext>();
      compositeAndFilter.addFilter(dataObjectFilter);
      compositeAndFilter.addFilter(aObjectFilter);
      return compositeAndFilter;
    }
  }

  @Override
  protected void actionPerformed(ActionEvent aActionEvent, List<TLcdDomainObjectContext> aSelection) {
    if (aSelection.size() > 0) {
      TLcdDomainObjectContext domainObjectContext = aSelection.get(0);
      ILcdDataObject dataObject = (ILcdDataObject) domainObjectContext.getDomainObject();
      ILcdModel model = domainObjectContext.getModel();

      // The parent frame may change (for example when switching to full screen mode)
      if (fDialog != null && fDialog.getParent() != TLcdAWTUtil.findParentFrame(fParentComponent)) {
        fDialog.dispose();
        fDialog = null;
      }
      // lazily created GUI (better compatibility with look and feel changes)
      if (fDialog == null) {
        createFrame();
      }

      if (dataObject.getDataType().getProperties().size() > 0) {
        configureDataObjectCustomizer(dataObject, model);
      }

      if (!fDialog.isVisible()) {
        fDialog.setVisible(true);
      }
      fDialog.toFront();
    }
  }

  private void configureDataObjectCustomizer(ILcdDataObject aDataObject, ILcdModel aModel) {
    if (fTreeCellRenderer == null) {
      fTreeCellRenderer = DataObjectTreeCellRenderer.createRenderer();
    }
    if (fDataObjectCustomizer == null) {
      fDataObjectCustomizer = new DataObjectDisplayTree();
      fDataObjectCustomizer.setModel(fFilteredTreeModel);
      fDataObjectCustomizer.setCellRenderer(fTreeCellRenderer);
      fExpandButton.addActionListener(new TreeExpandAction(true, fDataObjectCustomizer));
      fCollapseButton.addActionListener(new TreeExpandAction(false, fDataObjectCustomizer));
      fFilterEmptyButton.addActionListener(new SwitchTreeModelAction(fDataObjectCustomizer, fFilteredTreeModel, fNonFilteredTreeModel));
    }

    fDataObjectCustomizer.setDataObject(aDataObject);
    fDataObjectCustomizer.setDataModel(aModel);
    fScrollPane.setViewportView(fDataObjectCustomizer);
  }

  private void createFrame() {
    fScrollPane = new JScrollPane();

    fDialog = new JDialog(TLcdAWTUtil.findParentFrame(fParentComponent), "Properties");
    fDialog.setIconImages(SwingUtil.sLuciadFrameImage);
    fDialog.setLayout(new BorderLayout());
    fDialog.setLocationByPlatform(true);

    JPanel controlPanel = new JPanel();
    controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.X_AXIS));

    fScrollPane.setPreferredSize(new Dimension(200, 300));

    fExpandButton = new JButton(new TLcdSWIcon(TLcdIconFactory.create(TLcdIconFactory.EXPAND_NODE_ICON)));
    fCollapseButton = new JButton(new TLcdSWIcon(TLcdIconFactory.create(TLcdIconFactory.COLLAPSE_NODE_ICON)));
    fFilterEmptyButton = new JToggleButton("Filter Empty Values", new TLcdSWIcon(TLcdIconFactory.create(TLcdIconFactory.FILTER_ICON)), true);

    fExpandButton.setToolTipText("Expands all the nodes in the tree.");
    fCollapseButton.setToolTipText("Collapses all the nodes in the tree.");
    fFilterEmptyButton.setToolTipText("Filters empty values in the tree. Can be toggled.");

    controlPanel.add(fFilterEmptyButton);
    controlPanel.add(fExpandButton);
    controlPanel.add(fCollapseButton);
    controlPanel.add(Box.createHorizontalGlue());

    fDialog.add(controlPanel, BorderLayout.NORTH);
    fDialog.add(fScrollPane, BorderLayout.CENTER);
    fDialog.pack();
    fDialog.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        fDialog.setVisible(false);
        if (fDataObjectCustomizer != null) {
          fDataObjectCustomizer.setDataObject(null);
          fDataObjectCustomizer.setDataModel(null);
        }
      }
    });
  }

  /**
   * An action listener that is capable of expanding or collapsing an entire tree.
   */
  private static class TreeExpandAction implements ActionListener {
    private boolean fExpand;
    private JTree fTree;

    private TreeExpandAction(boolean aExpand, JTree aTree) {
      fExpand = aExpand;
      fTree = aTree;
    }

    public void expandAll(JTree aTree, boolean aExpand) {
      if (aExpand) {
        for (int i = 0; i < aTree.getRowCount(); i++) {
          aTree.expandRow(i);
        }
      } else {
        for (int i = aTree.getRowCount() - 1; i >= 0; i--) {
          aTree.collapseRow(i);
        }
      }
    }

    public void actionPerformed(ActionEvent e) {
      expandAll(fTree, fExpand);
    }
  }

  /**
   * An action listener that sets a predefined model on a tree.
   */
  private static class SwitchTreeModelAction implements ActionListener {
    private JTree fTree;
    private TreeModel fEnabledModel;
    private TreeModel fDisabledModel;

    private SwitchTreeModelAction(JTree aTree, TreeModel aEnabledModel, TreeModel aDisabledModel) {
      fTree = aTree;
      fEnabledModel = aEnabledModel;
      fDisabledModel = aDisabledModel;
    }

    public void actionPerformed(ActionEvent e) {
      if (((JToggleButton) e.getSource()).isSelected()) {
        fTree.setModel(fEnabledModel);
      } else {
        fTree.setModel(fDisabledModel);
      }
    }
  }
}


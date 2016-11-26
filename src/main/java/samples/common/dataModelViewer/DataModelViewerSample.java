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
package samples.common.dataModelViewer;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.text.DefaultEditorKit;
import javax.swing.tree.TreePath;

import com.luciad.datamodel.TLcdDataModel;
import com.luciad.datamodel.TLcdDataType;
import com.luciad.model.ILcdDataModelDescriptor;

import samples.common.SamplePanel;
import samples.common.action.ShowReadMeAction;
import samples.common.dataModelDisplayTree.DataModelTreeCellRenderer;
import samples.common.dataModelDisplayTree.DataModelTreeModel;
import samples.common.search.TextFieldComboBox;

public class DataModelViewerSample extends SamplePanel {
  private static final MessageFormat FORMAT = new MessageFormat(
      "new TLcdDataObjectExpressionLanguage().evaluate(\n" +
      "  \"{0}\",\n" +
      "  {1} //specify the actual domain object instance here\n" +
      ");");

  private final TLcdDataType[] fDataModelList;
  /**
   * A panel that contains all the trees for the various data models.
   */
  private JPanel fTreePanel = new JPanel(new CardLayout());
  /**
   * The textfield that contains the output string.
   */
  private JTextArea fTextArea;
  /**
   * A listener that listens to changes in tree selection, so that the output textfield can
   * be updated.
   */
  private MyTreeSelectionListener fTreeSelectionListener = new MyTreeSelectionListener();

  public DataModelViewerSample(TLcdDataType[] aDataModelList) {
    super();
    fDataModelList = aDataModelList;
  }

  @Override
  protected void createGUI() {
    List<String> displayNames = new ArrayList<>(fDataModelList.length);
    for (TLcdDataType dataType : fDataModelList) {
      addDataModel(fTreePanel, dataType);
      displayNames.add(dataType.getDisplayName());
    }

    final TextFieldComboBox dataModelsBox = new TextFieldComboBox(1) {
      @Override
      protected void valueSelected(String aOldValue, String aValue) {
        CardLayout cardLayout = (CardLayout) fTreePanel.getLayout();
        cardLayout.show(fTreePanel, aValue);
      }
    };

    dataModelsBox.setSearchContent(displayNames.toArray(new String[displayNames.size()]));
    dataModelsBox.setText(displayNames.get(0));

    fTextArea = new JTextArea();
    fTextArea.setEditable(false);
    fTextArea.setRows(5);
    JPanel textPanel = new JPanel(new BorderLayout());
    JLabel label = new JLabel("To retrieve this property from the domain object: ");
    label.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 0));
    textPanel.add(label, BorderLayout.NORTH);
    textPanel.add(new JScrollPane(fTextArea), BorderLayout.CENTER);
    add(textPanel, BorderLayout.SOUTH);

    JPopupMenu contextMenu = new JPopupMenu();
    fTextArea.setComponentPopupMenu(contextMenu);

    DefaultEditorKit.CopyAction copyAction = new DefaultEditorKit.CopyAction();
    copyAction.putValue(Action.NAME, "Copy");
    contextMenu.add(copyAction);

    ShowReadMeAction showReadme = ShowReadMeAction.createForSample(this);
    if (showReadme != null) {
      showReadme.actionPerformed(null);
    }
    JPanel panel = new JPanel(new BorderLayout(0, 2));
    JLabel comp = new JLabel("Select the Data Model:");
    comp.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 0));
    panel.add(comp, BorderLayout.NORTH);
    panel.add(dataModelsBox, BorderLayout.CENTER);

    setLayout(new BorderLayout(0, 5));
    add(panel, BorderLayout.NORTH);
    add(fTreePanel, BorderLayout.CENTER);
    add(textPanel, BorderLayout.SOUTH);
  }

  /**
   * Adds a data model to this data model viewer.
   * @param aTreePanel A panel in which to put the tree
   * @param aDataType The data type for which a tree should be created
   */
  private void addDataModel(JPanel aTreePanel, final TLcdDataType aDataType) {
    JTree tree = new JTree(new DataModelTreeModel(new MyDataModelDescriptor(aDataType), true));
    tree.setCellRenderer(new DataModelTreeCellRenderer());
    tree.addTreeSelectionListener(fTreeSelectionListener);
    JScrollPane scrollpane = new JScrollPane(tree);
    aTreePanel.add(scrollpane, aDataType.getDisplayName());
  }

  /**
   * A model descriptor wrapper that takes a single <code>TLcdDataType</code> element
   * and creates a matching data model descriptor for it.
   */
  private static class MyDataModelDescriptor implements ILcdDataModelDescriptor {
    private TLcdDataType fDataType;

    public MyDataModelDescriptor(TLcdDataType aDataType) {
      fDataType = aDataType;
    }

    public TLcdDataModel getDataModel() {
      return fDataType.getDataModel();
    }

    public Set<TLcdDataType> getModelElementTypes() {
      return Collections.singleton(fDataType);
    }

    public Set<TLcdDataType> getModelTypes() {
      return null;
    }

    public String getTypeName() {
      return fDataType.getName();
    }

    public String getDisplayName() {
      return fDataType.getDisplayName();
    }

    public String getSourceName() {
      return fDataType.getDataModel().getName();
    }
  }

  /**
   * An inner tree selection listener that adapts the output textfield whenever the selection
   * in a tree changes.
   */
  private class MyTreeSelectionListener implements TreeSelectionListener {
    private PropertyStringGenerator fPropertyStringGenerator = new PropertyStringGenerator();

    public void valueChanged(TreeSelectionEvent e) {
      String instanceName = getInstanceName(e);
      TreePath path = e.getNewLeadSelectionPath();
      if (path != null) {
        //If the last node in the selected tree path is a property, generate a string.
        //Otherwise, generate a message telling the user to select a property.
        if (path.getLastPathComponent() instanceof DataModelTreeModel.PropertyNode) {
          fTextArea.setText(FORMAT.format(new Object[]{fPropertyStringGenerator.generateString(path), instanceName}));
        } else {
          fTextArea.setText("Please select a property for which a string should be generated.");
        }
      }
    }

    private String getInstanceName(TreeSelectionEvent e) {
      JTree tree = (JTree) e.getSource();
      DataModelTreeModel model = (DataModelTreeModel) tree.getModel();
      TLcdDataType type = model.getDataModelDescriptor().getModelElementTypes().iterator().next();
      String instanceName = type.getDisplayName().replaceAll("Type$", "");
      instanceName = instanceName.toLowerCase().substring(0, 1) + instanceName.substring(1);
      return instanceName;
    }
  }
}

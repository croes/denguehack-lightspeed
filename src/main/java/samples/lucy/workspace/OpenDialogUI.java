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
package samples.lucy.workspace;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;

import com.luciad.gui.TLcdIconFactory;
import com.luciad.gui.swing.TLcdSWIcon;

/**
 * UI class which allows to select a workspace name from a list
 */
final class OpenDialogUI extends JPanel {
  private final CardLayout fCardLayout = new CardLayout();
  private final String fLoadingCard = "loading";
  private final String fInputCard = "input";
  private final JPanel fInputPanel = new JPanel();
  private final JPanel fLoadingPanel = new JPanel();
  private final JList<String> fList = new JList<>();
  private final FileRetrievalSwingWorker fSwingWorker;

  OpenDialogUI(long aDelayInMilliseconds, File aWorkspacesDir) {
    fSwingWorker = new FileRetrievalSwingWorker(aDelayInMilliseconds, aWorkspacesDir);
    initUIComponents();
    updateUIWhenSwingWorkerFinishes();
  }

  private void initUIComponents() {
    fList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    fInputPanel.setLayout(new BorderLayout());
    fInputPanel.add(new JScrollPane(fList));

    fLoadingPanel.setLayout(new BorderLayout(10, 10));
    fLoadingPanel.setOpaque(false);
    fLoadingPanel.add(new JLabel(new TLcdSWIcon(TLcdIconFactory.create(TLcdIconFactory.BUSY_ANIMATED_ICON))), BorderLayout.WEST);
    fLoadingPanel.add(new JLabel("Checking workspace directory ...", SwingConstants.CENTER), BorderLayout.CENTER);

    setLayout(fCardLayout);
    add(fLoadingPanel, fLoadingCard);
    add(fInputPanel, fInputCard);
    fCardLayout.show(this, fLoadingCard);
  }

  private void updateUIWhenSwingWorkerFinishes() {
    fSwingWorker.addPropertyChangeListener(new PropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent evt) {
        FileRetrievalSwingWorker worker = (FileRetrievalSwingWorker) evt.getSource();
        if ("state".equals(evt.getPropertyName()) &&
            worker.getState() == SwingWorker.StateValue.DONE) {
          DefaultListModel<String> listModel = new DefaultListModel<>();
          try {
            List<String> workspaces = worker.get();
            for (String workspace : workspaces) {
              listModel.addElement(workspace);
            }
            setListModel(listModel);
          } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
          }
        }
      }
    });
  }

  void setListModel(ListModel<String> aListModel) {
    fList.setModel(aListModel);
    fCardLayout.show(this, fInputCard);
  }

  String getSelectedWorkspace() {
    return fList.getSelectedValue();
  }

  void start() {
    fSwingWorker.execute();
  }
}

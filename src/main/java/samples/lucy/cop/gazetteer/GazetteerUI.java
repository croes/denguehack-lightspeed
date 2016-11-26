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
package samples.lucy.cop.gazetteer;

import java.awt.CardLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

import com.luciad.gui.TLcdAWTUtil;
import com.luciad.gui.TLcdIconFactory;
import com.luciad.gui.TLcdResizeableIcon;
import com.luciad.gui.swing.TLcdSWIcon;
import com.luciad.util.ILcdStatusListener;
import com.luciad.util.TLcdStatusEvent;
import com.luciad.view.lightspeed.layer.ILspLayer;

/**
 * UI which allows to update the query results of the gazetteer
 */
final class GazetteerUI extends JPanel {

  private final GazetteerModel fGazetteerModel;
  private final ILspLayer fGazetteerLayer;
  private final CardLayout fBusyIconPanelLayout = new CardLayout();
  private final JPanel fBusyIconPanel = new JPanel(fBusyIconPanelLayout);
  private final String fBusyIconCard = "busyIcon";
  private final String fIdleIconCard = "idleIcon";
  private final Icon fBusyIcon = new TLcdSWIcon(TLcdIconFactory.create(TLcdIconFactory.BUSY_ANIMATED_ICON));
  private final Icon fIdleIcon = new TLcdSWIcon(new TLcdResizeableIcon(null, 16, 16));
  final JComboBox fComboBox;

  public GazetteerUI(GazetteerModel aGazetteerModel, ILspLayer aGazetteerLayer) {
    fGazetteerModel = aGazetteerModel;
    fGazetteerModel.addStatusListener(new ShowBusyIndicator());
    fGazetteerLayer = aGazetteerLayer;
    setLayout(new FlowLayout());
    fComboBox = createComboBox();
    initIcons();
    setBusyStatus(false);
    add(fBusyIconPanel);
    add(fComboBox);
    add(new JButton(new UpdateModelAction()));
    add(new JButton(new HideResultsAction()));
  }

  private void initIcons() {
    fBusyIconPanel.add(new JLabel(fBusyIcon), fBusyIconCard);
    fBusyIconPanel.add(new JLabel(fIdleIcon), fIdleIconCard);
  }

  private JComboBox createComboBox() {
    JComboBox<GazetteerModel.Type> comboBox = new JComboBox<>(GazetteerModel.Type.values());
    comboBox.setRenderer(createRenderer());
    comboBox.setEditable(false);
    comboBox.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        updateModelToMatchComboBoxSelection();
      }
    });
    return comboBox;
  }

  private ListCellRenderer createRenderer() {
    return new DefaultListCellRenderer() {
      @Override
      public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        Component listCellRendererComponent = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        if (value instanceof GazetteerModel.Type) {
          setText(((GazetteerModel.Type) value).getDisplayName());
        }
        return listCellRendererComponent;
      }
    };
  }

  private void updateModelToMatchComboBoxSelection() {
    Object selectedItem = fComboBox.getSelectedItem();
    if (selectedItem instanceof GazetteerModel.Type) {
      fGazetteerModel.showElementsOfType((GazetteerModel.Type) selectedItem);
      fGazetteerLayer.setVisible(true);
    }
  }

  private void setBusyStatus(boolean aIsBusy) {
    if (aIsBusy) {
      fBusyIconPanelLayout.show(fBusyIconPanel, fBusyIconCard);
    } else {
      fBusyIconPanelLayout.show(fBusyIconPanel, fIdleIconCard);
    }
  }

  class UpdateModelAction extends AbstractAction {
    private UpdateModelAction() {
      putValue(Action.SMALL_ICON, new TLcdSWIcon(TLcdIconFactory.create(TLcdIconFactory.SEARCH_ICON)));
      putValue(Action.SHORT_DESCRIPTION, "Refresh");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      updateModelToMatchComboBoxSelection();
    }
  }

  class HideResultsAction extends AbstractAction {
    private HideResultsAction() {
      putValue(Action.SMALL_ICON, new TLcdSWIcon(TLcdIconFactory.create(TLcdIconFactory.CLOSE_ICON)));
      putValue(Action.SHORT_DESCRIPTION, "Hide results");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      fGazetteerLayer.setVisible(false);
    }
  }

  class ShowBusyIndicator implements ILcdStatusListener {
    @Override
    public void statusChanged(final TLcdStatusEvent aStatusEvent) {
      TLcdAWTUtil.invokeLater(new Runnable() {
        @Override
        public void run() {
          if (aStatusEvent.getID() == TLcdStatusEvent.START_BUSY) {
            setBusyStatus(true);
          } else if (aStatusEvent.getID() == TLcdStatusEvent.END_BUSY) {
            setBusyStatus(false);
          }
        }
      });
    }
  }
}

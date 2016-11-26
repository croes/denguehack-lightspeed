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
package samples.common.lightspeed.visualinspection;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.luciad.gui.TLcdAWTUtil;
import com.luciad.util.ILcdStringTranslator;
import com.luciad.util.collections.TLcdIdentityHashSet;
import com.luciad.view.ILcdLayer;
import com.luciad.view.ILcdLayerTreeNode;
import com.luciad.view.TLcdLayerTreeNodeUtil;
import com.luciad.view.lightspeed.ILspAWTView;
import com.luciad.view.lightspeed.layer.ILspLayer;

import samples.common.TitledSeparator;

/**
 * Shows two JLists of all layers in the view and allows selecting two sets of layers.
 * This can be used to compare layers using TLspSwipeController or TLspFlickerController.
 *
 * @since 2016.0
 */
public class LayerComparisonChooser {

  /**
   * Creates and shows a layer choose dialog that allows selecting two named sets of layers.
   * The given names are used as titles and also used as keys in the resulting map.
   *
   * @param aLayersToPreselect if not null, the values will be pre-selected in the respective lists
   * @return a map with the layer set names as keys and the selected layer sets as values
   */
  public static Map<String, Set<ILspLayer>> chooseLayerSets(String aTitle,
                                                            String aNameOfFirstLayerSet,
                                                            String aNameOfSecondLayerSet,
                                                            ILspAWTView aView,
                                                            List<Collection<ILspLayer>> aLayersToPreselect,
                                                            ILcdStringTranslator aStringTranslator) {
    MyDialog dialog = new MyDialog(aTitle, aNameOfFirstLayerSet, aNameOfSecondLayerSet, aView, aStringTranslator);
    dialog.pack();
    dialog.setLocationRelativeTo(aView.getHostComponent());
    dialog.selectLayers(aLayersToPreselect);
    dialog.setVisible(true);

    Map<String, Set<ILspLayer>> result = new HashMap<>();
    result.put(aNameOfFirstLayerSet, dialog.getLeftSelectedLayers());
    result.put(aNameOfSecondLayerSet, dialog.getRightSelectedLayers());
    return result;
  }

  private LayerComparisonChooser() {
    // this class is not meant to be instantiated: use the static method instead
  }

  private static class MyDialog extends JDialog implements PropertyChangeListener {

    private final JOptionPane fOptionPane;
    private final JList fList[] = new JList[2];

    private boolean fCancelled = false;
    private final ILcdStringTranslator fStringTranslator;

    private MyDialog(String aTitle, String aLeftListTitle, String aRightListTitle, ILspAWTView aView, ILcdStringTranslator aStringTranslator) {
      super(TLcdAWTUtil.findParentFrame(aView.getHostComponent()), true);
      fStringTranslator = aStringTranslator;
      setTitle(aStringTranslator.translate(aTitle));

      Vector<ILspLayer> layers = new Vector<>();
      for (int i = aView.layerCount() - 1; i >= 0; i--) {
        ILspLayer layer = aView.getLayer(i);
        if (layer.isVisible()) { // We only populate the visible layers, not to overflow the dialog when working with many layers
          layers.add(layer);
        }
      }
      JPanel panel = new JPanel();
      panel.setLayout(new GridLayout(1, 2, 5, 0));
      for (int i = 0; i < 2; i++) {
        fList[i] = new JList<>(layers);
        // We restrict the selection to a single selection interval to avoid
        // confusion when an in-between layer is not swiped, hiding swiped underneath layers.
        // The restriction to use a single selection interval
        // is not a TLspSwipeController or TLspFlickerController restriction though, but
        // is done here to obtain a consistent and understandable UI.
        fList[i].setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        fList[i].setCellRenderer(new DefaultListCellRenderer() {
          @Override
          public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            ILspLayer layer = (ILspLayer) value;
            return super.getListCellRendererComponent(list, layer.getLabel(), index, isSelected, cellHasFocus);
          }
        });
        JScrollPane pane = new JScrollPane(fList[i]) {
          @Override
          public Dimension getPreferredSize() {
            Dimension pref = super.getPreferredSize();
            pref.width = Math.min(150, pref.width);
            pref.height = Math.min(150, pref.height);
            return pref;
          }
        };
        String translatedTitle = i == 0 ? aStringTranslator.translate(aLeftListTitle) : aStringTranslator.translate(aRightListTitle);
        panel.add(TitledPanel.createTitledPanel(translatedTitle, pane));
      }
      JPanel finalPanel = new JPanel();
      finalPanel.setLayout(new BorderLayout(0, 5));
      finalPanel.add(panel, BorderLayout.CENTER);
      finalPanel.add(new JLabel("<html>" +
          aStringTranslator.translate("Choose layers to compare") + ".<br/>" +
          aStringTranslator.translate("Only the visible layers are shown.") + "</html>"
      ), BorderLayout.SOUTH);

      for (int i = 0; i < 2; i++) {
        final int finalI = i;
        fList[i].addListSelectionListener(new ListSelectionListener() {
          @Override
          public void valueChanged(ListSelectionEvent e) {
            // Make sure we don't select the same layer in the left and right list.
            int minIndex = Math.min(e.getFirstIndex(), e.getLastIndex());
            int maxIndex = Math.max(e.getFirstIndex(), e.getLastIndex());
            for (int index = minIndex; index <= maxIndex; index++) {
              if (fList[finalI].isSelectedIndex(index) && fList[1 - finalI].isSelectedIndex(index)) {
                fList[1 - finalI].removeSelectionInterval(index, index);
              }
            }

            // Make sure that all layers in-between the left and right selection are also selected.
            // This avoids having in-between layers that are not swiped/flickered that hide the
            // underneath layers, which could lead to confusion.
            int minIndexLeft = fList[0].getSelectedIndex();
            int minIndexRight = fList[1].getSelectedIndex();
            if (minIndexLeft != -1 && minIndexRight != -1) { // Do we have a selection in both lists?
              if (minIndexLeft < minIndexRight) {
                for (int i = minIndexLeft + 1; i < minIndexRight; i++) {
                  if (!fList[0].isSelectedIndex(i)) {
                    fList[0].addSelectionInterval(i, i);
                  }
                }
              } else if (minIndexRight < minIndexLeft) {
                for (int i = minIndexRight + 1; i < minIndexLeft; i++) {
                  if (!fList[1].isSelectedIndex(i)) {
                    fList[1].addSelectionInterval(i, i);
                  }
                }
              }
            }
          }
        });
      }

      fOptionPane = new JOptionPane(finalPanel, JOptionPane.QUESTION_MESSAGE, JOptionPane.OK_CANCEL_OPTION);

      setContentPane(fOptionPane);
      setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
      addWindowListener(new WindowAdapter() {
        @Override
        public void windowClosing(WindowEvent e) {
          fList[0].clearSelection();
          fList[1].clearSelection();
          fCancelled = true;
          done();
        }
      });
      fOptionPane.addPropertyChangeListener(this);
    }

    private void selectLayers(List<Collection<ILspLayer>> aLayersToPreselect) {
      for (int i = 0; i < 2 && i < aLayersToPreselect.size(); i++) {
        Collection<ILspLayer> layers = aLayersToPreselect.get(i);
        JList jList = fList[i];
        for (int j = 0; j < jList.getModel().getSize(); j++) {
          if (layers.contains(jList.getModel().getElementAt(j))) {
            jList.setSelectedIndex(j);
          }
        }
      }
    }

    public boolean isCancelled() {
      return fCancelled;
    }

    @Override
    public void propertyChange(PropertyChangeEvent e) {
      if (isVisible()) {
        Object value = fOptionPane.getValue();

        if (value == JOptionPane.UNINITIALIZED_VALUE) {
          return;
        }
        fOptionPane.setValue(
            JOptionPane.UNINITIALIZED_VALUE);

        if (JOptionPane.OK_OPTION == ((Integer) value)) {
          Set<ILspLayer> layers1 = getLeftSelectedLayers();
          Set<ILspLayer> layers2 = getRightSelectedLayers();

          int count1 = layers1.size();
          int count2 = layers2.size();
          Set<ILspLayer> intersection = new TLcdIdentityHashSet<>(layers1);
          intersection.retainAll(layers2);
          int intersectionCount = intersection.size();

          int nbSelected = count1 + count2 - 2 * intersectionCount;
          if (nbSelected >= 1) {
            done();
          } else {
            JOptionPane.showMessageDialog(
                MyDialog.this,
                fStringTranslator.translate("Please select at least one layer."),
                fStringTranslator.translate("Invalid selection"),
                JOptionPane.ERROR_MESSAGE);
          }
        } else {
          fList[0].clearSelection();
          fList[1].clearSelection();
          fCancelled = true;
          done();
        }
      }
    }

    private void done() {
      setVisible(false);
    }

    private Set<ILspLayer> getLeftSelectedLayers() {
      return expandLayerTreeNodes(fList[0].getSelectedValuesList());
    }

    private Set<ILspLayer> getRightSelectedLayers() {
      return expandLayerTreeNodes(fList[1].getSelectedValuesList());
    }

    private Set<ILspLayer> expandLayerTreeNodes(List aLayers) {
      TLcdIdentityHashSet<ILspLayer> result = new TLcdIdentityHashSet<>();
      for (Object layer : aLayers) {
        if (layer instanceof ILspLayer) {
          if (layer instanceof ILcdLayerTreeNode) {
            ILcdLayerTreeNode node = (ILcdLayerTreeNode) layer;
            List<ILcdLayer> layers = TLcdLayerTreeNodeUtil.getLayers(node);
            for (ILcdLayer l : layers) {
              if (l instanceof ILspLayer) {
                result.add((ILspLayer) l);
              }
            }
          }
          result.add((ILspLayer) layer);
        }
      }
      return result;
    }
  }

  private static class TitledPanel extends JPanel {

    private static TitledPanel createTitledPanel(String aTitle, Component aComponent) {
      return new TitledPanel(aTitle, aComponent, new Insets(2, 3, 2, 4));
    }

    private TitledPanel(String aTitle, Component aContent, Insets aInsets) {
      super(new BorderLayout());

      JPanel panel = new JPanel(new BorderLayout(0, 3));
      panel.add(aContent, BorderLayout.CENTER);
      panel.add(new TitledSeparator(aTitle), BorderLayout.NORTH);
      if (aInsets != null) {
        panel.setBorder(BorderFactory.createEmptyBorder(
            0, aInsets.left, 0, aInsets.right
        ));
      }
      add(panel, BorderLayout.CENTER);
      if (aInsets != null) {
        setBorder(BorderFactory.createEmptyBorder(
            aInsets.top, 0, aInsets.bottom, 0
        ));
      }
    }
  }

}

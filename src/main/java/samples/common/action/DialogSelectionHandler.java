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

import java.awt.Component;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.luciad.datamodel.ILcdDataObject;
import com.luciad.util.ILcdAssoc;
import com.luciad.util.ILcdFireEventMode;
import com.luciad.view.ILcdLayer;
import com.luciad.view.ILcdView;
import com.luciad.view.TLcdDomainObjectContext;
import com.luciad.view.gxy.ILcdGXYLayer;
import com.luciad.view.gxy.ILcdGXYLayerSubsetList;

import samples.gxy.common.TitledPanel;

/**
 * A custom selection handler that allows a user to choose multiple
 * selection candidates from list(s) inside a Swing dialog.
 */
public class DialogSelectionHandler {

  public static void handleSelectionCandidates(Component aView, ILcdGXYLayerSubsetList aSelectionCandidates) {
    ArrayList<TLcdDomainObjectContext> candidates = new ArrayList<TLcdDomainObjectContext>();
    for (ILcdAssoc assoc : aSelectionCandidates.asAssocs()) {
      ILcdGXYLayer layer = (ILcdGXYLayer) assoc.getKey();
      Vector objects = (Vector) assoc.getValue();
      for (Object o : objects) {
        candidates.add(new TLcdDomainObjectContext(o, layer.getModel(), layer, (ILcdView) aView));
      }
    }
    handleSelectionCandidates(aView, candidates);
  }

  public static void handleSelectionCandidates(Component aView, List<TLcdDomainObjectContext> aSelectionCandidates) {

    JPanel dialogPanel = new JPanel();
    dialogPanel.setLayout(new BoxLayout(dialogPanel, BoxLayout.X_AXIS));

    // Obtain the layers which are affected by this selection.
    Collection<ILcdLayer> layers = new HashSet<ILcdLayer>();
    for (TLcdDomainObjectContext candidate : aSelectionCandidates) {
      layers.add(candidate.getLayer());
    }

    // Create a list of objects per layer.
    for (ILcdLayer layer : layers) {
      JList listForCandidates = createJListOfCandidates(filterBasedOnLayer(aSelectionCandidates, layer));

      // Preferred height is 1/4th of the height of the parent hostComponent.
      final int preferredHeight = aView.getHeight() / 4;

      JScrollPane scrollPane = new JScrollPane(listForCandidates) {
        @Override
        public Dimension getPreferredSize() {
          Dimension size = super.getPreferredSize();
          size.height = preferredHeight;
          return size;
        }
      };

      TitledPanel panel = TitledPanel.createTitledPanel(layer.getLabel(), scrollPane);
      dialogPanel.add(panel);
    }

    // Showing a dialog only makes sense if there is anything to show.
    if (!aSelectionCandidates.isEmpty()) {
      int result = JOptionPane.showConfirmDialog(aView, dialogPanel,
                                                 "Choose the objects you want to select",
                                                 JOptionPane.OK_CANCEL_OPTION);

      if (result == JOptionPane.CANCEL_OPTION) {
        for (TLcdDomainObjectContext selectionCandidate : aSelectionCandidates) {
          ILcdLayer layer = selectionCandidate.getLayer();
          Object object = selectionCandidate.getDomainObject();

          layer.selectObject(object, false, ILcdFireEventMode.FIRE_NOW);
        }
      }
    }
  }

  /**
   * Filters the selection candidates based on the specified layer.
   *
   * @param aSelectionCandidates the list of selection candidates.
   * @param aLayer               the layer.
   *
   * @return the filtered list of selection candidates.
   */
  private static List<TLcdDomainObjectContext> filterBasedOnLayer(List<TLcdDomainObjectContext> aSelectionCandidates, ILcdLayer aLayer) {
    List<TLcdDomainObjectContext> selectionCandidates = new ArrayList<TLcdDomainObjectContext>();

    for (TLcdDomainObjectContext selectionCandidate : aSelectionCandidates) {
      if (selectionCandidate.getLayer().equals(aLayer)) {
        selectionCandidates.add(selectionCandidate);
      }
    }

    return selectionCandidates;
  }

  /**
   * Creates a new JList based on the provided list of selection candidates.
   *
   * @param aSelectionCandidates the list of selection candidates.
   *
   * @return the JList holding the selection candidates.
   */
  private static JList createJListOfCandidates(List<TLcdDomainObjectContext> aSelectionCandidates) {
    List<MyObjectFormatter> myObjectFormatters = wrapInObjectFormatters(aSelectionCandidates);
    sortAlphabetically(myObjectFormatters);

    // Create a JList with all the selection candidates and with each one wrapped in an object formatter that produces a readable toString().
    JList listSW = new JList(myObjectFormatters.toArray());
    listSW.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    listSW.setLayoutOrientation(JList.VERTICAL);
    listSW.setVisibleRowCount(-1);
    listSW.setSelectedIndices(retrieveIndicesOfAlreadySelectedCandidates(myObjectFormatters));

    // Obtain the selection model from the JList and register a listener on it.
    ListSelectionModel listSWSelectionModel = listSW.getSelectionModel();
    listSWSelectionModel.addListSelectionListener(createListSelectionListener(myObjectFormatters));

    return listSW;
  }

  /**
   * Sorts the list of selection candidates alphabetically.
   *
   * @param aSelectionCandidates the list of selection candidates.
   */
  private static void sortAlphabetically(List<MyObjectFormatter> aSelectionCandidates) {
    Collections.sort(aSelectionCandidates, new Comparator<MyObjectFormatter>() {
      @Override
      public int compare(MyObjectFormatter aObjectFormatter, MyObjectFormatter aAnotherObjectFormatter) {
        return aObjectFormatter.toString().compareTo(aAnotherObjectFormatter.toString());
      }
    });
  }

  /**
   * Wraps the list of selection candidates in object formatters.
   * <p/>
   * param aSelectionCandidates the list of selection candidates.
   *
   * @param aSelectionCandidates the list of selection candidates.
   *
   * @return the list of object formatters.
   */
  private static List<MyObjectFormatter> wrapInObjectFormatters(List<TLcdDomainObjectContext> aSelectionCandidates) {
    List<MyObjectFormatter> formattedObjects = new ArrayList<MyObjectFormatter>(aSelectionCandidates.size());

    for (TLcdDomainObjectContext aSelectionCandidate : aSelectionCandidates) {
      formattedObjects.add(new MyObjectFormatter(aSelectionCandidate));
    }

    return formattedObjects;
  }

  /**
   * Retrieves the indices of the selection candidates that are currently selected.
   * </p>
   * If a selection candidate is not selected its index is set to -1.
   *
   * @param aSelectionCandidates the list of selection candidates.
   *
   * @return the indices.
   */
  private static int[] retrieveIndicesOfAlreadySelectedCandidates(List<MyObjectFormatter> aSelectionCandidates) {
    int[] selectedCandidateIndices = new int[aSelectionCandidates.size()];
    // Initialize the indices to -1 so that by default no candidates are highlighted.
    Arrays.fill(selectedCandidateIndices, -1);

    for (int i = 0; i < aSelectionCandidates.size(); i++) {
      TLcdDomainObjectContext selectionCandidate = aSelectionCandidates.get(i).getSelectionCandidate();
      ILcdLayer layer = selectionCandidate.getLayer();

      if (layer.isSelected(selectionCandidate.getDomainObject())) {
        selectedCandidateIndices[i] = i;
      }
    }

    return selectedCandidateIndices;
  }

  /**
   * Creates a list selection listener that selects and deselected objects
   * when the contents of a JList change.
   *
   * @param aSelectionCandidates the list of selection candidates.
   *
   * @return the list selection listener.
   */
  private static ListSelectionListener createListSelectionListener(final List<MyObjectFormatter> aSelectionCandidates) {
    return new ListSelectionListener() {
      @Override
      public void valueChanged(ListSelectionEvent event) {
        // Only do something when the user is done making his selection of objects.
        if (!event.getValueIsAdjusting()) {
          ListSelectionModel selectionModel = (ListSelectionModel) event.getSource();
          for (int i = 0; i < aSelectionCandidates.size(); i++) {
            TLcdDomainObjectContext selectionCandidate = aSelectionCandidates.get(i).getSelectionCandidate();
            ILcdLayer layer = selectionCandidate.getLayer();
            Object object = selectionCandidate.getDomainObject();

            if (selectionModel.isSelectedIndex(i)) {
              layer.selectObject(object, true, ILcdFireEventMode.FIRE_NOW);
            } else {
              layer.selectObject(object, false, ILcdFireEventMode.FIRE_NOW);
            }
          }
        }
      }
    };
  }

  /**
   * A simple object wrapper that encapsulates how a selection candidate
   * is formatted when printed.
   */
  private static class MyObjectFormatter {

    private TLcdDomainObjectContext fSelectionCandidate;

    /**
     * Creates a new MyObjectFormatter based on the provided selection candidate.
     *
     * @param aSelectionCandidate the selection candidate.
     */
    private MyObjectFormatter(TLcdDomainObjectContext aSelectionCandidate) {
      fSelectionCandidate = aSelectionCandidate;
    }

    @Override
    public String toString() {
      Object domainObject = fSelectionCandidate.getDomainObject();
      if (domainObject instanceof ILcdDataObject &&
          fSelectionCandidate.getLayer().getLabel().equalsIgnoreCase("states")) {
        return ((ILcdDataObject) domainObject).getValue("STATE_NAME").toString();
      } else if (domainObject instanceof ILcdDataObject &&
                 fSelectionCandidate.getLayer().getLabel().equalsIgnoreCase("counties")) {
        String county = ((ILcdDataObject) domainObject).getValue("NAME").toString();
        String state = ((ILcdDataObject) domainObject).getValue("STATE_NAME").toString();
        return county + ", " + state;
      } else {
        return domainObject.toString();
      }
    }

    private TLcdDomainObjectContext getSelectionCandidate() {
      return fSelectionCandidate;
    }
  }
}

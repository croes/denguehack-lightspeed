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
package samples.lightspeed.viewselectionhandling;

import java.io.IOException;
import java.util.Enumeration;

import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import com.luciad.format.shp.TLcdSHPModelDecoder;
import com.luciad.util.ILcdSelection;
import com.luciad.util.ILcdSelectionListener;
import com.luciad.util.TLcdSelectionChangedEvent;
import com.luciad.view.ILcdLayer;

import samples.common.SampleData;
import samples.gxy.common.TitledPanel;
import samples.lightspeed.common.LightspeedSample;
import samples.lightspeed.common.LspDataUtil;

/**
 * This sample illustrates how to implement a selection listener. It displays shape data in the USA
 * (states, rivers and cities) and has a component that displays the selection changes that are
 * being made.
 */
public class MainPanel extends LightspeedSample {

  @Override
  protected void addData() throws IOException {
    super.addData();
    TLcdSHPModelDecoder shpModelDecoder = new TLcdSHPModelDecoder();
    shpModelDecoder.setFeatureIndexForDisplayName(1);   // use custom model decoder to make sure display names are shown correctly for states
    LspDataUtil.instance().model(SampleData.US_STATES, shpModelDecoder).layer().label("States").addToView(getView());
    LspDataUtil.instance().model(SampleData.US_RIVERS).layer().label("Rivers").addToView(getView());
    LspDataUtil.instance().model(SampleData.US_CITIES).layer().label("Cities").addToView(getView()).fit();
  }

  @Override
  protected void createGUI() {
    super.createGUI();

    // Add a selection listener to each of the layers within the view
    MyLayerSelectionListener selectionListener = new MyLayerSelectionListener();
    getView().addLayerSelectionListener(selectionListener);

    TitledPanel panel = TitledPanel.createTitledPanel("Selection events", new JScrollPane(selectionListener));

    addComponentBelow(panel);
  }

  /**
   * Prints the objects for which the selection state has changed.
   */
  private static class MyLayerSelectionListener
      extends JTextArea
      implements ILcdSelectionListener {

    public MyLayerSelectionListener() {
      setEditable(false);
      setRows(8);
    }

    public void selectionChanged(TLcdSelectionChangedEvent aSelectionEvent) {
      ILcdSelection selection = aSelectionEvent.getSelection();
      append("Selection has changed in layer[" +
             ((ILcdLayer) selection).getLabel() + "]:\n");
      Enumeration selectedElements = aSelectionEvent.selectedElements();
      while (selectedElements.hasMoreElements()) {
        Object element = selectedElements.nextElement();
        append("\tselected[" + element + "]\n");
      }
      Enumeration deselectedElements = aSelectionEvent.deselectedElements();
      while (deselectedElements.hasMoreElements()) {
        Object element = deselectedElements.nextElement();
        append("\tdeselected[" + element + "]\n");
      }
    }
  }

  public static void main(final String[] aArgs) {
    startSample(MainPanel.class, "View selection handling");
  }

}

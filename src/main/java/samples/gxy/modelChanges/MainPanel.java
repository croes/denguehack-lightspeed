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
package samples.gxy.modelChanges;

import java.awt.BorderLayout;
import java.io.IOException;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;

import com.luciad.gui.swing.TLcdSWAction;
import com.luciad.model.ILcdModel;
import com.luciad.view.TLcdDeleteSelectionAction;
import com.luciad.view.gxy.TLcdGXYLayer;
import com.luciad.view.gxy.TLcdGXYShapePainter;

import samples.gxy.common.GXYSample;
import samples.gxy.common.TitledPanel;
import samples.gxy.common.layers.GXYLayerUtil;
import samples.gxy.fundamentals.step2.FlightPlanLayerFactory;
import samples.gxy.fundamentals.step2.FlightPlanModelDecoder;

/**
 * Demonstrates how to implement an ILcdModelListener that receives notifications every
 * time elements are added or removed from an ILcdModel, or when model elements are changed.
 * All the events are displayed in a JTextArea below the map.
 *
 * Below the view there are a series of buttons for adding, removing, or changing the
 * selected elements or all the elements at once. An ILcdSelectionListener implementation
 * is used to track of the map selection.
 *
 * Note that the model fires events automatically only when elements are added or removed.
 * If an element is changed, the code that makes the change is responsible for notifying the
 * model, which in turn will trigger an "object-changed" event. This behavior is for
 * performance reasons, so that the model does not have to add individual listeners to each of
 * its elements.
 */
public class MainPanel extends GXYSample {

  private static final String FLIGHTPLAN_MODEL = "Data/Custom1/custom.cfp";
  private JToolBar fChangeModelBar;
  private ModelUpdateListener fModelEventsArea;

  @Override
  protected void addData() throws IOException {
    super.addData();

    FlightPlanModelDecoder flightPlanDecoder = new FlightPlanModelDecoder();
    ILcdModel flightPlanModel = flightPlanDecoder.decode("" + FLIGHTPLAN_MODEL);

    TLcdGXYLayer flightPlanLayer = (TLcdGXYLayer) new FlightPlanLayerFactory().createGXYLayer(flightPlanModel);
    // layers need to be editable in order to change them
    flightPlanLayer.setGXYEditorProvider(new TLcdGXYShapePainter());
    flightPlanLayer.setEditable(true);

    fChangeModelBar.add(new TLcdSWAction(new CreateFlightPlanAction(flightPlanModel)));
    fChangeModelBar.addSeparator();
    fChangeModelBar.add(new TLcdSWAction(new ChangeSelectionAction(getView(), flightPlanLayer)));
    fChangeModelBar.add(new TLcdSWAction(new TLcdDeleteSelectionAction(getView())));
    fChangeModelBar.addSeparator();
    fChangeModelBar.add(new TLcdSWAction(new ChangeAllAction(flightPlanModel)));
    fChangeModelBar.add(new TLcdSWAction(new DeleteAllAction(flightPlanModel)));
    flightPlanModel.addModelListener(fModelEventsArea);

    // Adds the background and flight plan layers to the view.
    // Moves the grid layer on top.
    GXYLayerUtil.addGXYLayer(getView(), flightPlanLayer);
    GXYLayerUtil.fitGXYLayer(getView(), flightPlanLayer);
  }

  @Override
  protected JPanel createBottomPanel() {
    // A toolbar with buttons to modify the model
    fChangeModelBar = new JToolBar();

    // A text area that displays the model events.
    fModelEventsArea = new ModelUpdateListener();
    TitledPanel scrollPane = TitledPanel.createTitledPanel("Model events", new JScrollPane(fModelEventsArea));

    JPanel centerPart = new JPanel(new BorderLayout());
    centerPart.add(fChangeModelBar, BorderLayout.NORTH);
    centerPart.add(scrollPane, BorderLayout.CENTER);
    return centerPart;
  }

  public static void main(final String[] aArgs) {
    startSample(MainPanel.class, "Listening to model changes");
  }
}

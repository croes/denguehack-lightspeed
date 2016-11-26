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
package samples.decoder.asdi;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

import com.luciad.datamodel.ILcdDataObject;
import com.luciad.format.asdi.TLcdASDIFlightPlan;
import com.luciad.format.asdi.TLcdASDITrack;
import samples.common.SwingUtil;
import com.luciad.model.ILcdModel;

import samples.common.dataObjectDisplayTree.DataObjectDisplayTree;
import samples.common.dataObjectDisplayTree.DataObjectTreeCellRenderer;

/**
 * This class can display the properties of the data model of an ASDI track and flight plan.
 * This data model can be hierarchical in nature and as such are displayed in a tree-structure.
 */
public class DataObjectDisplay extends JFrame {
  private static final String TRACK_TITLE = "Track properties";
  private static final String FLIGHT_PLAN_TITLE = "Flight Plan properties";

  private DataObjectDisplayTree fTrackDataObjectDisplayTree;
  private DataObjectDisplayTree fFlightPlanDataObjectDisplayTree;

  private DataObjectTreeCellRenderer fTrackCellRenderer;
  private DataObjectTreeCellRenderer fFlightPlanCellRenderer;
  private JTabbedPane fTabbedPane;

  /**
   * Creates the display and shows the properties of the set track and flight plan.
   *
   */
  public DataObjectDisplay() {
    super("Properties");
    setIconImages(SwingUtil.sLuciadFrameImage);

    fTrackDataObjectDisplayTree = new DataObjectDisplayTree();
    fFlightPlanDataObjectDisplayTree = new DataObjectDisplayTree();

    fTrackCellRenderer = new DataObjectTreeCellRenderer();
    fTrackDataObjectDisplayTree.setCellRenderer(fTrackCellRenderer);

    fFlightPlanCellRenderer = new DataObjectTreeCellRenderer();
    fFlightPlanDataObjectDisplayTree.setCellRenderer(fFlightPlanCellRenderer);

    fTabbedPane = new JTabbedPane();
    fTabbedPane.add(TRACK_TITLE, new JScrollPane(fTrackDataObjectDisplayTree));
    fTabbedPane.add(FLIGHT_PLAN_TITLE, new JScrollPane(fFlightPlanDataObjectDisplayTree));

    getContentPane().setLayout(new BorderLayout());
    getContentPane().add(fTabbedPane, BorderLayout.CENTER);
  }

  /**
   * Sets the track of which the properties should be shown.
   *
   * @param aTrack The track of which the properties should be shown.
   */
  public void setTrack(Object aTrack) {
    fTrackDataObjectDisplayTree.setDataObject((ILcdDataObject) aTrack);

    //Adapt the title of the tab to include the track ID
    if (aTrack != null) {
      fTabbedPane.setTitleAt(0, TRACK_TITLE + ": " + trackID((TLcdASDITrack) aTrack));
    } else {
      fTabbedPane.setTitleAt(0, TRACK_TITLE);
    }
  }

  public void setTrackModel(ILcdModel aModel) {
    fTrackDataObjectDisplayTree.setDataModel(aModel);
  }

  /**
   * Sets the flight plan of which the properties should be shown.
   *
   * @param aFlightPlan The flight plan of which the properties should be shown.
   */
  public void setFlightPlan(Object aFlightPlan) {
    fFlightPlanDataObjectDisplayTree.setDataObject((ILcdDataObject) aFlightPlan);

    //Adapt the title of the tab to include the flight plan ID
    if (aFlightPlan != null) {
      fTabbedPane.setTitleAt(1, FLIGHT_PLAN_TITLE + ": " + flightPlanID((TLcdASDIFlightPlan) aFlightPlan));
    } else {
      fTabbedPane.setTitleAt(1, FLIGHT_PLAN_TITLE);
    }
  }

  public void setFlightPlanModel(ILcdModel aModel) {
    fFlightPlanDataObjectDisplayTree.setDataModel(aModel);
  }

  private String trackID(TLcdASDITrack aTrack) {
    //TO and TZ models have different properties
    if (TrackSelectionMediator.isTOModel(fTrackDataObjectDisplayTree.getDataModel())) {
      //Aircraft ID
      return aTrack.getValue("ACID").toString();
    } else {
      ILcdDataObject flightID = (ILcdDataObject) aTrack.getValue("FlightId");
      if (flightID != null) {
        String aircraftIDString = flightID.getValue("AircraftId").toString();
        if (aircraftIDString != null) {
          return aircraftIDString;
        }
      }
    }
    return "-";
  }

  private String flightPlanID(TLcdASDIFlightPlan aFlightPlan) {
    return aFlightPlan.getValue("AircraftId").toString();
  }

}

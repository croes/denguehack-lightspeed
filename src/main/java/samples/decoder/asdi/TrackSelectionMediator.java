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

import java.util.Enumeration;

import com.luciad.format.asdi.ALcdASDIModelDescriptor;
import com.luciad.format.asdi.TLcdASDIFlightPlan;
import com.luciad.format.asdi.TLcdASDIFlightPlanHistory;
import com.luciad.format.asdi.TLcdASDIFlightPlanModelDescriptor;
import com.luciad.format.asdi.TLcdASDITrack;
import com.luciad.format.asdi.TLcdASDITrackModelDescriptor;
import com.luciad.format.asdi.TLcdASDITrajectory;
import com.luciad.format.asdi.TLcdASDITrajectoryModelDescriptor;
import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelDescriptor;
import com.luciad.util.ILcdFireEventMode;
import com.luciad.util.ILcdSelectionListener;
import com.luciad.util.TLcdSelectionChangedEvent;
import com.luciad.view.ILcdLayer;
import com.luciad.view.ILcdLayeredListener;
import com.luciad.view.TLcdLayeredEvent;
import com.luciad.view.gxy.ILcdGXYView;

/**
 * Whenever a track is selected, this mediator selects the matching trajectory and flight plan as well.
 */
public class TrackSelectionMediator implements ILcdSelectionListener, ILcdLayeredListener {
  private static final int TRACK_TO = 0;
  private static final int TRACK_TZ = 1;
  private static final int TRAJECTORY_TO = 2;
  private static final int TRAJECTORY_TZ = 3;
  private static final int FLIGHTPLAN = 4;

  private static final int[] ALL = new int[]{TRACK_TO, TRACK_TZ, TRAJECTORY_TO, TRAJECTORY_TZ, FLIGHTPLAN};
  private ILcdLayer[] fLayers = new ILcdLayer[ALL.length];

  public static void install(ILcdGXYView aGXYView) {
    TrackSelectionMediator mediator = new TrackSelectionMediator();

    //Check all existing layers
    Enumeration layers = aGXYView.layers();
    while (layers.hasMoreElements()) {
      ILcdLayer layer = (ILcdLayer) layers.nextElement();
      mediator.layerAddedOrRemoved(true, layer);
    }

    //Listen for added/removed layers
    aGXYView.addLayeredListener(mediator);
  }

  private ILcdLayer getLayer(int aType) {
    return fLayers[aType];
  }

  private int getType(ILcdLayer aLayer) {
    for (int i = 0; i < fLayers.length; i++) {
      if (fLayers[i] == aLayer) {
        return i;
      }
    }
    return -1;
  }

  private void setLayer(ILcdLayer aLayer, int aType) {
    if (fLayers[aType] != null && (aType == TRACK_TO || aType == TRACK_TZ)) {
      fLayers[aType].removeSelectionListener(this);
    }
    fLayers[aType] = aLayer;
    if (aLayer != null && (aType == TRACK_TO || aType == TRACK_TZ)) {
      aLayer.addSelectionListener(this);
    }
  }

  private void removeLayer(ILcdLayer aLayer) {
    for (int i = 0; i < fLayers.length; i++) {
      if (fLayers[i] == aLayer) {
        fLayers[i] = null;
      }
    }
  }

  public void layeredStateChanged(TLcdLayeredEvent aEvent) {
    if (aEvent.getID() == TLcdLayeredEvent.LAYER_REMOVED ||
        aEvent.getID() == TLcdLayeredEvent.LAYER_ADDED) {
      layerAddedOrRemoved(aEvent.getID() == TLcdLayeredEvent.LAYER_ADDED, aEvent.getLayer());
    }
  }

  public void layerAddedOrRemoved(boolean aAdded, ILcdLayer aLayer) {
    if (!aAdded) {
      removeLayer(aLayer);
    } else {
      ILcdModel model = aLayer.getModel();
      int type = getType(model);
      if (type != -1) {
        setLayer(aLayer, type);
      }
    }
  }

  public static boolean isTrackModel(ILcdModel aModel) {
    return aModel.getModelDescriptor() instanceof TrackSimulationModelDescriptor ||
           aModel.getModelDescriptor() instanceof TLcdASDITrackModelDescriptor;
  }

  public static boolean isFlightPlanModel(ILcdModel aModel) {
    return aModel.getModelDescriptor() instanceof FlightPlanSimulationModelDescriptor ||
           aModel.getModelDescriptor() instanceof TLcdASDIFlightPlanModelDescriptor;
  }

  public static boolean isTrajectoryModel(ILcdModel aModel) {
    return aModel.getModelDescriptor() instanceof TLcdASDITrajectoryModelDescriptor;
  }

  /**
   * Returns <code>true</code> if the given model contains TO tracks.
   * @param aModel The model to check.
   * @return <code>true</code> if the given model contains TO tracks, <code>false</code> otherwise.
   */
  public static boolean isTOModel(ILcdModel aModel) {
    ILcdModelDescriptor descr = aModel.getModelDescriptor();
    if (descr instanceof SimulationModelDescriptor) {
      return ((SimulationModelDescriptor) descr).getStaticDataDescriptor().
          getDataType() == ALcdASDIModelDescriptor.DATA_TYPE_TO_TRACK;
    } else if (descr instanceof ALcdASDIModelDescriptor) {
      return ((ALcdASDIModelDescriptor) descr).getDataType() == ALcdASDIModelDescriptor.DATA_TYPE_TO_TRACK;
    }
    return false;
  }

  private int getType(ILcdModel aModel) {
    boolean track = isTrackModel(aModel);
    boolean trajectory = isTrajectoryModel(aModel);
    boolean flight_plan = isFlightPlanModel(aModel);
    boolean to = isTOModel(aModel);

    if (track && to) {
      return TRACK_TO;
    }
    if (track && !to) {
      return TRACK_TZ;
    }
    if (trajectory && to) {
      return TRAJECTORY_TO;
    }
    if (trajectory && !to) {
      return TRAJECTORY_TZ;
    }
    if (flight_plan) {
      return FLIGHTPLAN;
    }

    return -1;
  }

  private ILcdLayer getTrajectoryLayer(ILcdLayer aTrackLayer) {
    int type = getType(aTrackLayer);
    if (type == TRACK_TO) {
      return getLayer(TRAJECTORY_TO);
    } else if (type == TRACK_TZ) {
      return getLayer(TRAJECTORY_TZ);
    }
    return null;
  }

  private Object findFlightPlan(TLcdASDITrack aTrack) {
    TLcdASDITrajectory traj = aTrack.getTrajectory();
    if (traj != null) {
      TLcdASDIFlightPlanHistory hist = traj.getFlightPlanHistory();
      if (hist != null) {
        ILcdLayer flight_plan_layer = getLayer(FLIGHTPLAN);
        if (flight_plan_layer != null) {
          ILcdModel lcdModel = flight_plan_layer.getModel();
          Enumeration flight_plans = lcdModel.elements();
          while (flight_plans.hasMoreElements()) {
            TLcdASDIFlightPlan fp = (TLcdASDIFlightPlan) flight_plans.nextElement();
            if (fp.getFlightPlanHistory() == hist) {
              return fp;
            }
          }
        }
      }
    }
    return null;
  }

  public void selectionChanged(TLcdSelectionChangedEvent aEvent) {
    ILcdLayer track_layer = (ILcdLayer) aEvent.getSource();
    ILcdLayer trajectory_layer = getTrajectoryLayer(track_layer);
    ILcdLayer flight_plan_layer = getLayer(FLIGHTPLAN);

    for (Enumeration deselected = aEvent.deselectedElements(); deselected.hasMoreElements(); ) {
      updateSelection((TLcdASDITrack) deselected.nextElement(), trajectory_layer, flight_plan_layer, false);
    }
    for (Enumeration selected = aEvent.selectedElements(); selected.hasMoreElements(); ) {
      updateSelection((TLcdASDITrack) selected.nextElement(), trajectory_layer, flight_plan_layer, true);
    }

    if (trajectory_layer != null) {
      trajectory_layer.fireCollectedSelectionChanges();
    }
    if (flight_plan_layer != null) {
      flight_plan_layer.fireCollectedSelectionChanges();
    }
  }

  private void updateSelection(TLcdASDITrack aTrack, ILcdLayer aTrajectoryLayer, ILcdLayer aFlightPlanLayer, boolean aSelect) {
    TLcdASDITrajectory traj = aTrack.getTrajectory();
    if (aTrajectoryLayer != null && traj != null) {
      aTrajectoryLayer.selectObject(traj, aSelect, ILcdFireEventMode.FIRE_LATER);
    }
    if (aFlightPlanLayer != null) {
      Object fp = findFlightPlan(aTrack);
      if (fp != null) {
        aFlightPlanLayer.selectObject(fp, aSelect, ILcdFireEventMode.FIRE_LATER);
      }
    }
  }
}

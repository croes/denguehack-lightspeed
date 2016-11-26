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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;

import com.luciad.format.asdi.TLcdASDIFlightPlan;
import com.luciad.format.asdi.TLcdASDIFlightPlanDataTypes;
import com.luciad.format.asdi.TLcdASDIFlightPlanHistory;
import com.luciad.format.asdi.TLcdASDIFlightPlanHistoryModelDescriptor;
import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelDescriptor;
import com.luciad.model.TLcdVectorModel;
import com.luciad.realtime.ALcdTimeIndexedSimulatorModel;

/**
 * Creates an <CODE>FlightPlanSimulatorModel</CODE> that starts from ASDI flight plan history objects and creates
 * time-dependent flight plan representations for those.
 */
public class FlightPlanSimulatorModel extends ALcdTimeIndexedSimulatorModel {

  private ILcdModel fFlightPlanHistoryModel;

  /**
   * Constructs a new empty <code>FlightPlanSimulatorModel</code>. Use <code>setFlightPlanHistoryModel</code> before
   * using this instance.
   */
  public FlightPlanSimulatorModel() {
  }

  public ILcdModel getFlightPlanHistoryModel() {
    return fFlightPlanHistoryModel;
  }

  public void setFlightPlanHistoryModel(ILcdModel aFlightPlanHistoryModel) {
    if (!(aFlightPlanHistoryModel.getModelDescriptor() instanceof TLcdASDIFlightPlanHistoryModelDescriptor)) {
      throw new IllegalArgumentException("Given model[" + aFlightPlanHistoryModel + "] not a flight plan history model");
    }
    fFlightPlanHistoryModel = aFlightPlanHistoryModel;
    ILcdModel flight_plan_model = createFlightPlanModel(aFlightPlanHistoryModel);
    Collection flight_plans = createFlightPlans(aFlightPlanHistoryModel);
    if (flight_plans.size() == 0) {
      throw new IllegalArgumentException(
          "Flight plan history model [" + aFlightPlanHistoryModel + "] contains no flight plans for simulation.");
    }

    init(flight_plan_model, flight_plans);
  }

  private Collection createFlightPlans(ILcdModel aFlightPlanHistoryModel) {
    //create a flight plan for every flight plan history.
    List flight_plans = new ArrayList();
    for (Enumeration elements = aFlightPlanHistoryModel.elements(); elements.hasMoreElements(); ) {
      TLcdASDIFlightPlanHistory history = (TLcdASDIFlightPlanHistory) elements.nextElement();
      if (history.getPointCount() > 0) {
        flight_plans.add(new TLcdASDIFlightPlan(history));
      }
    }
    return flight_plans;
  }

  private ILcdModel createFlightPlanModel(ILcdModel aModel) {
    TLcdASDIFlightPlanHistoryModelDescriptor descriptor = (TLcdASDIFlightPlanHistoryModelDescriptor) aModel.getModelDescriptor();

    //create a model that will hold the flight plans.
    TLcdVectorModel flight_plan_model = new TLcdVectorModel();
    flight_plan_model.setModelReference(aModel.getModelReference());

    //derive a model descriptor for the flight plans from the model descriptor of the flight plan histories.
    ILcdModelDescriptor model_descriptor = createModelDescriptor(descriptor);
    flight_plan_model.setModelDescriptor(model_descriptor);

    return flight_plan_model;
  }

  protected SimulationModelDescriptor createModelDescriptor(
      TLcdASDIFlightPlanHistoryModelDescriptor aFlightPlanHistoryModelDescriptor) {

    return new FlightPlanSimulationModelDescriptor(
        aFlightPlanHistoryModelDescriptor.getSourceName(),
        "ASDI Flight Plans",
        "Flight Plans",
        aFlightPlanHistoryModelDescriptor,
        this,
        TLcdASDIFlightPlanDataTypes.FlightPlanType);
  }

  protected long getBeginTime(Object aTrack) {
    return ((TLcdASDIFlightPlan) aTrack).getFlightPlanHistory().getBeginTime();
  }

  protected long getEndTime(Object aTrack) {
    return ((TLcdASDIFlightPlan) aTrack).getFlightPlanHistory().getEndTime();
  }

  /**
   * Update the given flight plan to the given time. This will lookup the corresponding point index in the
   * flight plan history associated with the flight plan and update the flight plan to represent the flight plan history at that
   * point.
   *
   * @param aFlightPlanModel The flight plan model.
   * @param aFlightPlan The flight plan to update.
   * @param aDate      The date/time that will be used to lookup the location of the flight plan.
   */
  protected boolean updateTrackForDateSFCT(ILcdModel aFlightPlanModel, Object aFlightPlan, Date aDate) {
    long time = aDate.getTime();
    TLcdASDIFlightPlan flight_plan = (TLcdASDIFlightPlan) aFlightPlan;
    TLcdASDIFlightPlanHistory flight_plan_history = flight_plan.getFlightPlanHistory();

    //Verify if the current index is still valid or not
    int current = flight_plan.getFlightPlanHistoryIndex();
    if (current == -1 ||
        current >= (flight_plan_history.getMessageCount() - 1) ||
        time < flight_plan_history.getTime(current) ||
        time >= flight_plan_history.getTime(current + 1)) {
      flight_plan.setFlightPlanHistoryIndex(flight_plan_history.getIndexForTimeStamp(time));
      return true;
    } else {
      // The current index is still valid.
      return false;
    }
  }
}

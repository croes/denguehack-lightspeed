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
package samples.lightspeed.demo.application.data.support;

import java.beans.PropertyChangeListener;
import java.util.Date;

import com.luciad.model.ILcdModel;
import com.luciad.realtime.ILcdSimulatorModel;
import com.luciad.shape.ILcdShape;

import samples.lightspeed.common.tracks.TrackSimulatorModel;
import samples.lightspeed.demo.simulation.AsynchronousSimulatorModel;

/**
 * Simulator model for airways.
 */
public class EnrouteAirwaySimulatorModel implements ILcdSimulatorModel, AsynchronousSimulatorModel {

  private TrackSimulatorModel fTrackSimulatorModel;
  private final int fHistoryPointCount;

  public EnrouteAirwaySimulatorModel(ILcdModel aModel, int aHistoryPointCount, double aHistoryPointInterval) {
    fHistoryPointCount = aHistoryPointCount;
    fTrackSimulatorModel = new TrackSimulatorModel(aModel, aHistoryPointInterval) {

      @Override
      protected int getHistoryPointCount(ILcdShape aRoute) {
        return fHistoryPointCount;
      }

    };
  }

  public int hashCode() {
    return fTrackSimulatorModel.hashCode();
  }

  public boolean equals(Object aObj) {
    return fTrackSimulatorModel.equals(aObj);
  }

  public ILcdModel[] getTrackModels() {
    return fTrackSimulatorModel.getTrackModels();
  }

  public void setDate(Date aDate) {
    fTrackSimulatorModel.setDate(aDate);
  }

  public Date getDate() {
    return fTrackSimulatorModel.getDate();
  }

  public Date getBeginDate() {
    return fTrackSimulatorModel.getBeginDate();
  }

  public Date getEndDate() {
    return fTrackSimulatorModel.getEndDate();
  }

  public void addPropertyChangeListener(PropertyChangeListener aPropertyChangeListener) {
  }

  public void removePropertyChangeListener(PropertyChangeListener aPropertyChangeListener) {
  }

  public String toString() {
    return fTrackSimulatorModel.toString();
  }

  public static String getModelKey(ILcdModel aModel) {
    return EnrouteAirwaySimulatorModel.class.getName() + " - " + aModel.getModelDescriptor().getSourceName();
  }

  @Override
  public boolean isAsynchronous() {
    return fTrackSimulatorModel.isAsynchronous();
  }

  @Override
  public void setAsynchronous(boolean aAsynchronous) {
    fTrackSimulatorModel.setAsynchronous(aAsynchronous);
  }
}

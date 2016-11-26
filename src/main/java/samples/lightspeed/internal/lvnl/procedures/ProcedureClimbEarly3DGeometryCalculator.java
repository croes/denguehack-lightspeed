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
package samples.lightspeed.internal.lvnl.procedures;

import com.luciad.ais.geodesy.TLcdEllipsoidDistanceUtil;
import com.luciad.ais.model.procedure.ILcdProcedure;
import com.luciad.ais.model.procedure.ILcdProcedureGeometryHandler;
import com.luciad.ais.model.procedure.ILcdProcedureLeg;
import com.luciad.ais.model.procedure.TLcdProcedureGeometryCalculator;
import com.luciad.ais.model.procedure.type.TLcdProcedureGeometryType;
import com.luciad.ais.model.procedure.type.TLcdProcedureLegAltitudeDescription;
import com.luciad.ais.model.procedure.type.TLcdProcedureLegIAPFixRole;
import com.luciad.ais.model.procedure.type.TLcdProcedureLegType;
import com.luciad.ais.model.procedure.type.TLcdProcedureType;
import com.luciad.reference.ILcdGeoReference;
import com.luciad.shape.ILcdPoint;
import com.luciad.shape.shape3D.ILcd3DEditablePoint;
import com.luciad.util.TLcdConstant;
import com.luciad.util.logging.ILcdLogger;
import com.luciad.util.logging.TLcdLoggerFactory;

/**
 * This geometry calculator extends <code>TLcdProcedureGeometryCalculator</code> to include
 * actual altitudes in the geometry.
 *
 * The following strategies are used to compute altitudes in the flight profile
 *
 * SID:
 *   Start at runway altitude. Then climb as soon as possible, not violating the maximum
 *   altitude.
 *
 * STAR:
 *   The initial altitude is determined from the altitudes in the legs. The first minimum
 *   altitude encountered in the procedure leg sequence is used as a starting altitude.
 *   If none, found the first maximum is taken. If also none is found, 11000ft is taken.
 *
 *   Within a leg, try to descend to the maximum allowed altitude within the current leg.
 *
 * IAP:
 *   The initial altitude is determined as for the STAR except that 2000ft is used when no
 *   altitude is found.
 *
 *   The aircraft descends as in a STAR, until it reaches the Missed Approach Point (MAPT)
 *   which is a property of a leg. From that leg on, the aircraft will climb as in a SID.
 *   The leg before the MAPT usually has an undefined altitude with a AT or AT_OR_ABOVE
 *   constraint. (This might be the runway or FAF??) If this is the case, the target
 *   altitude is 0;
 *
 */
class ProcedureClimbEarly3DGeometryCalculator extends TLcdProcedureGeometryCalculator {

  private static final ILcdLogger sLogger = TLcdLoggerFactory.getLogger(ProcedureClimbEarly3DGeometryCalculator.class);

  private ILcdProcedure fProcedure;
  private TLcdEllipsoidDistanceUtil fEllipsoidDistanceUtil = new TLcdEllipsoidDistanceUtil();

  // Threshold for altitude comparisons
  private static final double ALTITUDE_EPSILON = 1e-4;

  private static final int LEVEL_PHASE = 0; // When we don't know what to do: stay level
  private static final int CLIMB_PHASE = 1;
  private static final int DESCEND_PHASE = 2;

  // Current state
  private double fCurrentAltitude;
  private int fCurrentLegIndex;
  private int fVerticalPhase = LEVEL_PHASE;
  private int fTempVerticalPhase;

  // Procedure data
  private double fInitialAltitude;
  private static final int INITIAL_ARRAY_LENGTH = 10;
  private double[] fMinimumLegAltitude = new double[INITIAL_ARRAY_LENGTH];
  private double[] fMaximumLegAltitude = new double[INITIAL_ARRAY_LENGTH];

  // Cached data
  private AltitudeHandler fAltitudeHandler = new AltitudeHandler();

  public ProcedureClimbEarly3DGeometryCalculator() {
    super();
//    System.out.println( "ProcedureClimbEarly3DGeometryCalculator.ProcedureClimbEarly3DGeometryCalculator" );
  }

  public void setCoordinateSystems(ILcdGeoReference aSource, ILcdGeoReference aTarget) {
    super.setCoordinateSystems(aSource, aTarget);
  }

  public synchronized void calculateProcedureGeometry(
      ILcdProcedure aProcedure,
      ILcdProcedureGeometryHandler aProcedureGeometryHandler
  ) {
    fProcedure = aProcedure;
    initialize();
    fAltitudeHandler.setDelegateHandler(aProcedureGeometryHandler);
    super.calculateProcedureGeometry(fProcedure, fAltitudeHandler);
  }

  protected void initializeAircraft(ILcdProcedure aProcedure) {
    super.initializeAircraft(aProcedure);
    getAircraftState().setCurrentAltitude(fInitialAltitude);
    fCurrentAltitude = fInitialAltitude;
    fCurrentLegIndex = -1; // No leg started
  }

  protected void startProcedureLeg(
      ILcdProcedure aProcedure, ILcdProcedureLeg aCurrentLeg
  ) {
    // If we are on holding, we keep the altitude steady.
    if (aCurrentLeg.getType() == TLcdProcedureLegType.HA ||
        aCurrentLeg.getType() == TLcdProcedureLegType.HM ||
        aCurrentLeg.getType() == TLcdProcedureLegType.HF) {
      fTempVerticalPhase = fVerticalPhase;
      fVerticalPhase = LEVEL_PHASE;
    }

    super.startProcedureLeg(aProcedure, aCurrentLeg);
    fCurrentLegIndex++;

    // Switch to climb for missed approach part of IAP
    if (isMissedApproachPoint(aProcedure, aCurrentLeg)) {
      fVerticalPhase = CLIMB_PHASE;
      if (sLogger.isDebugEnabled()) {
        sLogger.debug("Procedure " + aProcedure.getDesignator() + " missed approach fix at leg " +
                      fCurrentLegIndex + " " + aCurrentLeg.getType());
      }
    }
  }

  private boolean isMissedApproachPoint(ILcdProcedure aProcedure, ILcdProcedureLeg aCurrentLeg) {
    return (aProcedure.getType() == TLcdProcedureType.IAP) &&
           (aCurrentLeg.getIAPFixRole() == TLcdProcedureLegIAPFixRole.MAPT);
  }

  protected void endProcedureLeg(
      ILcdProcedure aProcedure, ILcdProcedureLeg aCurrentLeg
  ) {
    // If we leave a holding, set the original vertical phase.
    if (aCurrentLeg.getType() == TLcdProcedureLegType.HA ||
        aCurrentLeg.getType() == TLcdProcedureLegType.HM ||
        aCurrentLeg.getType() == TLcdProcedureLegType.HF) {
      fVerticalPhase = fTempVerticalPhase;
    }

    super.endProcedureLeg(aProcedure, aCurrentLeg);
    getAircraftState().setCurrentAltitude(fCurrentAltitude);

    double min_altitude = fMinimumLegAltitude[fCurrentLegIndex];
    if (!Double.isNaN(min_altitude) && (fCurrentAltitude < (min_altitude - ALTITUDE_EPSILON))) {
      reportAltitudeError("Current altitude " + fCurrentAltitude +
                          " does not comply to minimum altitude " + min_altitude);
      if (getAircraftState().isStateValid()) {
        fAltitudeHandler.handlePoint(
            getAircraftState().getCurrentPosition(), TLcdProcedureGeometryType.ERROR
        );
      }
    }

    double max_altitude = fMaximumLegAltitude[fCurrentLegIndex];
    if (!Double.isNaN(max_altitude) && (fCurrentAltitude > (max_altitude + ALTITUDE_EPSILON))) {
      reportAltitudeError("Current altitude " + fCurrentAltitude +
                          " does not comply to maximum altitude " + max_altitude);
      if (getAircraftState().isStateValid()) {
        fAltitudeHandler.handlePoint(
            getAircraftState().getCurrentPosition(), TLcdProcedureGeometryType.ERROR
        );
      }
    }

    if (sLogger.isDebugEnabled()) {
      sLogger.debug("current " + fCurrentAltitude +
                    " min " + min_altitude + " max " + max_altitude);
    }
  }

  private void reportAltitudeError(String aMessage) {
    sLogger.error("Procedure " + fProcedure.getDesignator() + ": " + aMessage);
  }

  private void initialize() {
    if (fProcedure.getType() == TLcdProcedureType.SID) {
      initializeSID();
    } else if (fProcedure.getType() == TLcdProcedureType.STAR) {
      initializeSTAR();
    } else {
      initializeIAP();
    }
  }

  private double getCurrentAltitude() {
    return fCurrentAltitude;
  }

  /**
   * Updates the altitude when flying the given distance. Returns the distance
   * at which the aircraft changes phase.
   * @param aDistance
   */
  private void updateAltitude(double aDistance) {
    // target altitude
    double target_altitude = retrieveTargetAltitude(fCurrentLegIndex);

    if (altitudesEqual(target_altitude, fCurrentAltitude)) {
      if (fCurrentLegIndex < (fMinimumLegAltitude.length - 1)) {
        target_altitude = retrieveTargetAltitude(fCurrentLegIndex + 1);
      }
      if (target_altitude > fCurrentAltitude) {
        double climb_rate = getAircraftState().getAircraftPerformanceSettings().getClimbRate();
        double full_climb_altitude = fCurrentAltitude + aDistance * climb_rate;
        // Never climb above target altitude
        fCurrentAltitude = Math.min(full_climb_altitude, target_altitude);
      } else {
        // Descending
        double descend_rate = getAircraftState().getAircraftPerformanceSettings().getDescentRate();
        double full_descend_altitude = fCurrentAltitude - aDistance * descend_rate;
        // Never descend below target altitude
        fCurrentAltitude = Math.max(full_descend_altitude, target_altitude);
      }
    } else if (target_altitude > fCurrentAltitude) {
      double climb_rate = getAircraftState().getAircraftPerformanceSettings().getClimbRate();
      double full_climb_altitude = fCurrentAltitude + aDistance * climb_rate;
      // Never climb above target altitude
      fCurrentAltitude = Math.min(full_climb_altitude, target_altitude);
    } else {
      // Descending
      double descend_rate = getAircraftState().getAircraftPerformanceSettings().getDescentRate();
      double full_descend_altitude = fCurrentAltitude - aDistance * descend_rate;
      // Never descend below target altitude
      fCurrentAltitude = Math.max(full_descend_altitude, target_altitude);
    }
  }

  /**
   * Retrieves the target altitude for the current leg. This altitude
   * depends on whether the aircraft is in climbing or descending phase.
   * When climbing, the aircraft will target the maximum allowed altitude (climb early!!!).
   * When descending, the aircraft will target the maximum allowed altitude.
   * @param aLegIndex
   * @return
   */
  private double retrieveTargetAltitude(int aLegIndex) {
    if (fVerticalPhase == CLIMB_PHASE) {
//      if (!Double.isNaN(fMinimumLegAltitude[aLegIndex]))
//        return Math.max(fCurrentAltitude, fMinimumLegAltitude[aLegIndex]);
//      else
//        return fCurrentAltitude;
      if (!Double.isNaN(fMaximumLegAltitude[aLegIndex])) {
        return Math.max(fCurrentAltitude, fMaximumLegAltitude[aLegIndex]); // Normally the max should never be the current altitude
      } else {
        return Math.max(fCurrentAltitude, 10000.0 * TLcdConstant.FT2MTR_STD); // Continue climb to 10000 ft when allowed
      }
    } else if (fVerticalPhase == DESCEND_PHASE) {
      if (!Double.isNaN(fMaximumLegAltitude[aLegIndex])) {
        return Math.min(fCurrentAltitude, fMaximumLegAltitude[aLegIndex]);
      } else {
        return fCurrentAltitude;
      }
    } else {
      return fCurrentAltitude; // Don't change level
    }
  }

  void initializeSID() {
    initAltitudeRanges();
    propagateSIDAltitudes();

    fVerticalPhase = CLIMB_PHASE;

    // initial altitude: take the altitude in the first leg or the runway altitude
    if (!Double.isNaN(fMinimumLegAltitude[0])) {
      fInitialAltitude = fMinimumLegAltitude[0];
    } else {
      fInitialAltitude = 0;

      if (fProcedure.getLegCount() > 0) {
        ILcdPoint fix = fProcedure.getLeg(0).getFix();
        if (fix != null) {
          fInitialAltitude = fix.getZ();
        }
      }
    }
  }

  void initializeIAP() {
    initAltitudeRanges();
    fVerticalPhase = DESCEND_PHASE;

    // initial altitude
    fInitialAltitude = retrieveInitialAltitudeForDescend();

    if (Double.isNaN(fInitialAltitude)) {
      fInitialAltitude = 2000 * TLcdConstant.FT2MTR_STD;
    }
  }

  void initializeSTAR() {
    initAltitudeRanges();
    fVerticalPhase = DESCEND_PHASE;

    // initial altitude
    fInitialAltitude = retrieveInitialAltitudeForDescend();

    if (Double.isNaN(fInitialAltitude)) {
      fInitialAltitude = 11000 * TLcdConstant.FT2MTR_STD;
    }
  }

  /**
   * Finds a suitable initial altitude for descend procedures.
   * Tries to find the first minimum altitude constraint in a procedure and returns it as the
   * initial altitude. If no minimum altitude is found, it returns the first valid
   * maximum altitude. If that is also no found, returns Double.NaN.
   * @return
   */
  private double retrieveInitialAltitudeForDescend() {
    // Find a sensible minimum altitude along the legs
    for (int leg_index = 0; leg_index < fMinimumLegAltitude.length; leg_index++) {
      double min_alt = fMinimumLegAltitude[leg_index];
      if (!Double.isNaN(min_alt) && min_alt > 0) {
        return min_alt;
      }
    }

    // No minimal altitude found, try to find a sensible maximal altitude
    for (int leg_index = 0; leg_index < fMaximumLegAltitude.length; leg_index++) {
      double max_alt = fMaximumLegAltitude[leg_index];
      if (!Double.isNaN(max_alt) && max_alt < Double.MAX_VALUE) {
        return max_alt;
      }
    }

    return Double.NaN;
  }

  /**
   * Ensure arrays that are large enough. Fill the arrays with the
   * altitude range constraints in the legs of the procedures
   */
  void initAltitudeRanges() {
    if (fMinimumLegAltitude.length < fProcedure.getLegCount()) {
      // Extra room to prevent frequent reallocations (*2)
      fMinimumLegAltitude = new double[fProcedure.getLegCount() * 2];
      fMaximumLegAltitude = new double[fProcedure.getLegCount() * 2];
    }

    for (int i = 0; i < fProcedure.getLegCount(); i++) {
      ILcdProcedureLeg leg = fProcedure.getLeg(i);
      fillAltitudeRange(leg, i);
    }
  }

  private void propagateSIDAltitudes() {
    // Climbing: propagate the filled in minimum altitudes from start to end
    // propagate maximum altitudes from end to start

    // Propagate minimum
    double min = Double.NaN;
    for (int i = 0; i < fProcedure.getLegCount(); i++) {
      if (Double.isNaN(fMinimumLegAltitude[i])) {
        fMinimumLegAltitude[i] = min;
      } else {
        min = fMinimumLegAltitude[i];
      }
    }

    // Propagate maximum
    double max = Double.NaN;
    for (int i = fProcedure.getLegCount() - 1; i >= 0; i--) {
      if (Double.isNaN(fMaximumLegAltitude[i])) {
        fMaximumLegAltitude[i] = max;
      } else {
        max = fMaximumLegAltitude[i];
      }
    }
//    if ( isMissedApproachPoint( aProcedure, aCurrentLeg ) ) {
//    dumpAltitudeRanges();
  }

//  private void dumpAltitudeRanges() {
//    System.out.println( "=========================" );
//    System.out.println( "Procedure " + fProcedure.getDesignator() );
//    System.out.println( "Type " + fProcedure.getType());
//    System.out.println( "Altitude ranges" );
//    System.out.print( "Max " );
//    for (int i = 0; i < fProcedure.getLegCount(); i++) {
//      System.out.print( "  " + fMaximumLegAltitude[i] );
//    }
//    System.out.println( "" );
//    System.out.print( "Min " );
//    for (int i = 0; i < fProcedure.getLegCount(); i++) {
//      System.out.print( "  " + fMinimumLegAltitude[i] );
//    }
//    System.out.println( "" );
//  }

  /**
   * Fills minimum and maximum altitude. Unknown altitudes are set to Double.NaN.
   * @param aLeg
   * @param aLegIndex
   */
  private void fillAltitudeRange(ILcdProcedureLeg aLeg, int aLegIndex) {
    if (aLeg.getAltitudeDescription() == TLcdProcedureLegAltitudeDescription.AT) {
      double alt = aLeg.getAltitudeUpper();
      // HACK in DAFIF data, for Missed approaches, the altitude at the leg before MAPT is not
      // filled in, but the AT_OR_ABOVE description is?
      // We set the altitude to 0, assuming it is the runway...
      if (Double.isNaN(alt)) {
        alt = 0;
      }
      fMinimumLegAltitude[aLegIndex] = alt;
      fMaximumLegAltitude[aLegIndex] = alt;
    } else if (aLeg.getAltitudeDescription() == TLcdProcedureLegAltitudeDescription.AT_OR_ABOVE) {
      double min = aLeg.getAltitudeLower();
      // HACK in DAFIF data, for Missed approaches, the altitude at the leg before MAPT is not
      // filled in, but the AT_OR_ABOVE description is?
      // We set the altitude to 0, assuming it is the runway...
      // This is a nice theory, but the height of the runway is not necessarily 0!
      if (Double.isNaN(min)) {
        min = 0;
      }
      fMinimumLegAltitude[aLegIndex] = min;
//      Is 10,000ft a sensible ceiling for altitudes?
//      fMaximumLegAltitude[aLegIndex] = 10000 * TLcdConstant.FT2MTR_STD;
      fMaximumLegAltitude[aLegIndex] = Double.NaN; // 10000 * TLcdConstant.FT2MTR_STD;
    } else if (aLeg.getAltitudeDescription() == TLcdProcedureLegAltitudeDescription.AT_OR_BELOW) {
      // What is a sensible floor altitude for AT_OR_BELOW legs?
//      fMinimumLegAltitude[aLegIndex] = aLeg.getAltitudeUpper() * 0.9;
      fMinimumLegAltitude[aLegIndex] = Double.NaN; // aLeg.getAltitudeUpper() * 0.9;
      fMaximumLegAltitude[aLegIndex] = aLeg.getAltitudeUpper();
    } else if (aLeg.getAltitudeDescription() == TLcdProcedureLegAltitudeDescription.BETWEEN) {
      fMinimumLegAltitude[aLegIndex] = aLeg.getAltitudeLower();
      fMaximumLegAltitude[aLegIndex] = aLeg.getAltitudeUpper();
    } else {
      // Unknown or null altitude description type
      fMinimumLegAltitude[aLegIndex] = Double.NaN;
      fMaximumLegAltitude[aLegIndex] = Double.NaN;
    }

    if (sLogger.isDebugEnabled()) {
      sLogger.debug("Leg " + aLegIndex + " " + aLeg.getType());
      sLogger.debug("" + aLeg.getAltitudeDescription());
      sLogger.debug("Min: " + fMinimumLegAltitude[aLegIndex] +
                    " Max: " + fMaximumLegAltitude[aLegIndex]);
    }
  }

  private static boolean altitudesEqual(double altitude1, double altitude2) {
    return Math.abs(altitude1 - altitude2) < ALTITUDE_EPSILON;
  }

  private class AltitudeHandler implements ILcdProcedureGeometryHandler {
    private ILcdProcedureGeometryHandler fDelegateHandler;

    private ILcd3DEditablePoint fStartPoint = null;
    private ILcd3DEditablePoint fEndPoint = null;

    public AltitudeHandler() {

    }

    public void setDelegateHandler(ILcdProcedureGeometryHandler aDelegateHandler) {
      fDelegateHandler = aDelegateHandler;
    }

    public void handlePoint(ILcdPoint aPoint, TLcdProcedureGeometryType aType) {

      if (fStartPoint == null) {
        fStartPoint = aPoint.cloneAs3DEditablePoint();
      }
      if (fEndPoint == null) {
        fEndPoint = aPoint.cloneAs3DEditablePoint();
      }

      if (isAltitudeType(aType)) {
        fStartPoint.move3D(aPoint.getX(), aPoint.getY(), getCurrentAltitude());
        fDelegateHandler.handlePoint(fStartPoint, aType);
      } else {
        fDelegateHandler.handlePoint(aPoint, aType);
      }
    }

    public void handleLine(
        ILcdPoint aStartPoint, ILcdPoint aEndPoint, TLcdProcedureGeometryType aType
    ) {

      if (fStartPoint == null) {
        fStartPoint = aStartPoint.cloneAs3DEditablePoint();
      }
      if (fEndPoint == null) {
        fEndPoint = aStartPoint.cloneAs3DEditablePoint();
      }

      if (isAltitudeType(aType)) {
        fStartPoint.move3D(aStartPoint.getX(), aStartPoint.getY(), getCurrentAltitude());
        double distance = getEllipsoid().geodesicDistance(aStartPoint, aEndPoint);
        updateAltitude(distance);
        fEndPoint.move3D(aEndPoint.getX(), aEndPoint.getY(), getCurrentAltitude());
        fDelegateHandler.handleLine(fStartPoint, fEndPoint, aType);

      } else {
        fDelegateHandler.handleLine(aStartPoint, aEndPoint, aType);
      }
    }

    private double fH0, fH1;

    public void beginAngleArc(
        ILcdPoint aCenter, double aRadius,
        double aStartAngle, double aArcAngle,
        double aBeginHeight, double aEndHeight,
        TLcdProcedureGeometryType aType
    ) {

      if (fStartPoint == null) {
        fStartPoint = aCenter.cloneAs3DEditablePoint();
      }
      if (fEndPoint == null) {
        fEndPoint = aCenter.cloneAs3DEditablePoint();
      }

      if (isAltitudeType(aType)) {
        double start_height = getCurrentAltitude();
        double distance = fEllipsoidDistanceUtil.distanceAlongArcSegment(
            aCenter, aRadius, aRadius, 0, aStartAngle, aArcAngle, getEllipsoid()
        );
        updateAltitude(distance);
        double end_height = getCurrentAltitude();
        fDelegateHandler.beginAngleArc(
            aCenter, aRadius, aStartAngle, aArcAngle, start_height, end_height, aType
        );

        fH0 = start_height;
        fH1 = end_height;
        fCurrentAltitude = fH0;
      } else {
        fDelegateHandler.beginAngleArc(
            aCenter, aRadius, aStartAngle, aArcAngle, aBeginHeight, aEndHeight, aType
        );

        fH0 = aBeginHeight;
        fH1 = aEndHeight;
      }
    }

    public void handleArcSegment(
        ILcdPoint aP1, ILcdPoint aP2,
        int aIndex, int aNumSegments,
        TLcdProcedureGeometryType aType
    ) {

      if (fStartPoint == null) {
        fStartPoint = aP1.cloneAs3DEditablePoint();
      }
      if (fEndPoint == null) {
        fEndPoint = aP1.cloneAs3DEditablePoint();
      }

      if (isAltitudeType(aType)) {
        double delta_h = (fH1 - fH0) / (double) aNumSegments;

        fStartPoint.move3D(aP1.getX(), aP1.getY(), getCurrentAltitude());
        fCurrentAltitude = getCurrentAltitude() + delta_h;
        fEndPoint.move3D(aP2.getX(), aP2.getY(), getCurrentAltitude());
        fDelegateHandler.handleArcSegment(fStartPoint, fEndPoint, aIndex, aNumSegments, aType);

      } else {
        fDelegateHandler.handleArcSegment(aP1, aP2, aIndex, aNumSegments, aType);
      }
    }

    public void endAngleArc() {
      fDelegateHandler.endAngleArc();
    }

    public void beginProcedureLeg(ILcdProcedureLeg aLeg) {
      fDelegateHandler.beginProcedureLeg(aLeg);
    }

    public void beginProcedure(ILcdProcedure aProcedure) {
      fStartPoint = null;
      fEndPoint = null;

      fDelegateHandler.beginProcedure(aProcedure);
    }

    public void endProcedure() {
      fDelegateHandler.endProcedure();
    }

    private boolean isAltitudeType(TLcdProcedureGeometryType aType) {
      return (aType == TLcdProcedureGeometryType.CONNECTOR) ||
             (aType == TLcdProcedureGeometryType.NORMAL) ||
             (aType == TLcdProcedureGeometryType.ERROR);
    }
  }
}

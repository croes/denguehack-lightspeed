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

import com.luciad.ais.model.procedure.ILcdEditableProcedure;
import com.luciad.ais.model.procedure.ILcdEditableProcedureLeg;
import com.luciad.ais.model.procedure.TLcdAircraftPerformanceSettings;
import com.luciad.ais.model.procedure.TLcdFeaturedProcedure;
import com.luciad.ais.model.procedure.TLcdFeaturedProcedureLeg;
import com.luciad.ais.model.procedure.TLcdProcedureTrajectory;
import com.luciad.ais.model.procedure.type.TLcdProcedureLegAltitudeDescription;
import com.luciad.ais.model.procedure.type.TLcdProcedureLegFixOverflyType;
import com.luciad.ais.model.procedure.type.TLcdProcedureLegType;
import com.luciad.ais.model.procedure.type.TLcdProcedureType;
import com.luciad.shape.shape3D.TLcdLonLatHeightPoint;

@SuppressWarnings("deprecation")
class SIDFactory {

  private static ProcedureClimbEarly3DGeometryCalculator fGeometryCalculator = new ProcedureClimbEarly3DGeometryCalculator();

  private static TLcdLonLatHeightPoint RW06_BEGIN = new TLcdLonLatHeightPoint(4.746944444, 52.29277778, 0);
  private static TLcdLonLatHeightPoint RW06_END = new TLcdLonLatHeightPoint(4.766111111, 52.30027778, 0);
  //  private static TLcdLonLatHeightPoint RW09_BEGIN = new TLcdLonLatHeightPoint(4.750833, 52.316666, 0);
//  private static TLcdLonLatHeightPoint RW09_END = new TLcdLonLatHeightPoint(4.792222, 52.318333, 0);
  private static TLcdLonLatHeightPoint RW18C_BEGIN = new TLcdLonLatHeightPoint(4.740277778, 52.32722222, 0);
  private static TLcdLonLatHeightPoint RW18C_END = new TLcdLonLatHeightPoint(4.739166667, 52.31444444, 0);
//  private static TLcdLonLatHeightPoint RW18L_BEGIN = new TLcdLonLatHeightPoint(4.779722, 52.31638, 0);
//  private static TLcdLonLatHeightPoint RW18L_END = new TLcdLonLatHeightPoint(4.777777, 52.295277, 0);

  private static TLcdLonLatHeightPoint EH004 = new TLcdLonLatHeightPoint(4.835555, 52.36638889, 0);
  private static TLcdLonLatHeightPoint EH007 = new TLcdLonLatHeightPoint(4.743611, 52.36916667, 0);
  private static TLcdLonLatHeightPoint EH009 = new TLcdLonLatHeightPoint(4.554444, 52.223611, 0);
  private static TLcdLonLatHeightPoint EH014 = new TLcdLonLatHeightPoint(4.863888, 52.337777, 0);
  private static TLcdLonLatHeightPoint EH017 = new TLcdLonLatHeightPoint(4.941388, 52.185, 0);
  private static TLcdLonLatHeightPoint EH028 = new TLcdLonLatHeightPoint(4.421111, 52.357777, 0);
  private static TLcdLonLatHeightPoint EH033 = new TLcdLonLatHeightPoint(5.047222, 52.0975, 0);
  private static TLcdLonLatHeightPoint EH036 = new TLcdLonLatHeightPoint(4.84777, 52.256944, 0);
  private static TLcdLonLatHeightPoint EH038 = new TLcdLonLatHeightPoint(4.960833, 52.17, 0);
  private static TLcdLonLatHeightPoint EH045 = new TLcdLonLatHeightPoint(4.7425, 52.3575, 0);
  private static TLcdLonLatHeightPoint EH049 = new TLcdLonLatHeightPoint(4.6125, 52.289166, 0);
  private static TLcdLonLatHeightPoint EH051 = new TLcdLonLatHeightPoint(4.651111, 52.243888, 0);
  private static TLcdLonLatHeightPoint EH057 = new TLcdLonLatHeightPoint(4.690833, 52.371111, 0);
  private static TLcdLonLatHeightPoint EH068 = new TLcdLonLatHeightPoint(4.983611, 52.336111, 0);
  private static TLcdLonLatHeightPoint EH071 = new TLcdLonLatHeightPoint(5.112777, 52.381944, 0);
  private static TLcdLonLatHeightPoint EH072 = new TLcdLonLatHeightPoint(4.843555, 52.020555, 0);
  private static TLcdLonLatHeightPoint EH080 = new TLcdLonLatHeightPoint(4.736388, 52.291388, 0);

  private static TLcdLonLatHeightPoint SPIJKERBOOR = new TLcdLonLatHeightPoint(4.853888, 52.54027778, 0);
  private static TLcdLonLatHeightPoint PAMPUS = new TLcdLonLatHeightPoint(5.092222, 52.33472222, 0);

  private static TLcdLonLatHeightPoint LOPIK = new TLcdLonLatHeightPoint(5.129166, 51.93083, 0);
  private static TLcdLonLatHeightPoint ANDIK = new TLcdLonLatHeightPoint(5.270555, 52.739444, 0);
  private static TLcdLonLatHeightPoint LEKKO = new TLcdLonLatHeightPoint(4.767222, 51.9241666, 0);
  private static TLcdLonLatHeightPoint VALKO = new TLcdLonLatHeightPoint(3.839722, 52.071388, 0);
  private static TLcdLonLatHeightPoint BERGI = new TLcdLonLatHeightPoint(4.358888, 52.746111, 0);
  private static TLcdLonLatHeightPoint AMGOD = new TLcdLonLatHeightPoint(3.685277, 52.975277, 0);

  //RWY 06
  public static TLcdProcedureTrajectory createLEKKO1RSID() {
    // Create the procedure
    ILcdEditableProcedure procedure = new TLcdFeaturedProcedure(1);

    // Initial fix
    ILcdEditableProcedureLeg leg = new TLcdFeaturedProcedureLeg(0);
    leg.setType(TLcdProcedureLegType.IF);
    leg.setFix(RW06_BEGIN);
    leg.setAltitudeDescription(TLcdProcedureLegAltitudeDescription.BETWEEN);
    leg.setAltitudeLower(0);
    leg.setAltitudeUpper(3);
    leg.setCourse(59);
    leg.setSequenceNumber(10);
    procedure.addLeg(leg);

    // Track to fix
    leg = new TLcdFeaturedProcedureLeg(0);
    leg.setType(TLcdProcedureLegType.TF);
    leg.setFix(RW06_END);
    leg.setAltitudeDescription(TLcdProcedureLegAltitudeDescription.BETWEEN);
    leg.setAltitudeLower(0);
    leg.setAltitudeUpper(3);
    leg.setSequenceNumber(20);
    procedure.addLeg(leg);

    // Course to an altitude
    leg = new TLcdFeaturedProcedureLeg(0);
    leg.setType(TLcdProcedureLegType.CA);
    leg.setCourse(59);
    leg.setAltitudeLower(500);
    leg.setAltitudeUpper(500);
    leg.setSequenceNumber(30);
    procedure.addLeg(leg);

    // Course to radial
    leg = new TLcdFeaturedProcedureLeg(0);
    leg.setType(TLcdProcedureLegType.CR);
    leg.setRecommendedNavaid(EH036);
    leg.setTheta(2);
    leg.setCourse(59);
    leg.setAltitudeLower(500);
    leg.setAltitudeUpper(500);
    leg.setSequenceNumber(40);
    procedure.addLeg(leg);

    // Track to fix
    leg = new TLcdFeaturedProcedureLeg(0);
    leg.setType(TLcdProcedureLegType.TF);
    leg.setFix(EH036);
    leg.setAltitudeLower(6000);
    leg.setAltitudeUpper(6000);
    leg.setSequenceNumber(50);
    procedure.addLeg(leg);

    // Track to fix
    leg = new TLcdFeaturedProcedureLeg(0);
    leg.setType(TLcdProcedureLegType.TF);
    leg.setFix(EH072);
    leg.setAltitudeLower(6000);
    leg.setAltitudeUpper(6000);
    leg.setSequenceNumber(60);
    procedure.addLeg(leg);

    // Track to fix
    leg = new TLcdFeaturedProcedureLeg(0);
    leg.setType(TLcdProcedureLegType.TF);
    leg.setFix(LEKKO);
    leg.setAltitudeLower(6000);
    leg.setAltitudeUpper(6000);
    leg.setSequenceNumber(70);
    procedure.addLeg(leg);

    // Add the procedure to a proceduretrajectory, set a feature and add it to the model
    procedure.setDesignator("LEKKO 1R");
    procedure.setType(TLcdProcedureType.SID);
    TLcdProcedureTrajectory trajectory = new TLcdProcedureTrajectory(procedure,
                                                                     new TLcdAircraftPerformanceSettings(), fGeometryCalculator);

    return trajectory;
  }

  //RWY 06
  public static TLcdProcedureTrajectory createLEKKO1TSID() {
    // Create the procedure
    ILcdEditableProcedure procedure = new TLcdFeaturedProcedure(1);

    // Initial fix
    ILcdEditableProcedureLeg leg = new TLcdFeaturedProcedureLeg(0);
    leg.setType(TLcdProcedureLegType.IF);
    leg.setFix(RW06_BEGIN);
    leg.setAltitudeDescription(TLcdProcedureLegAltitudeDescription.BETWEEN);
    leg.setAltitudeLower(0);
    leg.setAltitudeUpper(3);
    leg.setCourse(59);
    leg.setSequenceNumber(10);
    procedure.addLeg(leg);

    // Track to fix
    leg = new TLcdFeaturedProcedureLeg(0);
    leg.setType(TLcdProcedureLegType.TF);
    leg.setFix(RW06_END);
    leg.setAltitudeDescription(TLcdProcedureLegAltitudeDescription.BETWEEN);
    leg.setAltitudeLower(0);
    leg.setAltitudeUpper(3);
    leg.setSequenceNumber(20);
    procedure.addLeg(leg);

    // Course to an altitude
    leg = new TLcdFeaturedProcedureLeg(0);
    leg.setType(TLcdProcedureLegType.CA);
    leg.setCourse(59);
    leg.setAltitudeLower(500);
    leg.setAltitudeUpper(500);
    leg.setSequenceNumber(30);
    procedure.addLeg(leg);

    // Course to radial
    leg = new TLcdFeaturedProcedureLeg(0);
    leg.setType(TLcdProcedureLegType.CR);
    leg.setRecommendedNavaid(EH036);
    leg.setTheta(2);
    leg.setCourse(59);
    leg.setAltitudeLower(500);
    leg.setAltitudeUpper(500);
    leg.setSequenceNumber(40);
    procedure.addLeg(leg);

    // Track to fix
    leg = new TLcdFeaturedProcedureLeg(0);
    leg.setType(TLcdProcedureLegType.TF);
    leg.setFix(EH036);
    leg.setAltitudeLower(6000);
    leg.setAltitudeUpper(6000);
    leg.setSequenceNumber(50);
    procedure.addLeg(leg);

    // Track to fix
    leg = new TLcdFeaturedProcedureLeg(0);
    leg.setType(TLcdProcedureLegType.TF);
    leg.setFix(EH017);
    leg.setAltitudeLower(6000);
    leg.setAltitudeUpper(6000);
    leg.setSequenceNumber(60);
    procedure.addLeg(leg);

    // Track to fix
    leg = new TLcdFeaturedProcedureLeg(0);
    leg.setType(TLcdProcedureLegType.TF);
    leg.setFixOverflyType(TLcdProcedureLegFixOverflyType.FLY_BY);
    leg.setFix(EH038);
    leg.setAltitudeLower(6000);
    leg.setAltitudeUpper(6000);
    leg.setSequenceNumber(70);
    procedure.addLeg(leg);

    // Track to fix
    leg = new TLcdFeaturedProcedureLeg(0);
    leg.setType(TLcdProcedureLegType.TF);
    leg.setFix(LEKKO);
    leg.setAltitudeLower(6000);
    leg.setAltitudeUpper(6000);
    leg.setSequenceNumber(80);
    procedure.addLeg(leg);

    // Add the procedure to a proceduretrajectory, set a feature and add it to the model
    procedure.setDesignator("LEKKO 1T");
    procedure.setType(TLcdProcedureType.SID);
    TLcdProcedureTrajectory trajectory = new TLcdProcedureTrajectory(procedure,
                                                                     new TLcdAircraftPerformanceSettings(), fGeometryCalculator);

    return trajectory;
  }

  //RWY 06
  public static TLcdProcedureTrajectory createANDIK1RSID() {
    // Create the procedure
    ILcdEditableProcedure procedure = new TLcdFeaturedProcedure(1);

    // Initial fix
    ILcdEditableProcedureLeg leg = new TLcdFeaturedProcedureLeg(0);
    leg.setType(TLcdProcedureLegType.IF);
    leg.setFix(RW06_BEGIN);
    leg.setAltitudeDescription(TLcdProcedureLegAltitudeDescription.BETWEEN);
    leg.setAltitudeLower(0);
    leg.setAltitudeUpper(3);
    leg.setCourse(59);
    leg.setSequenceNumber(10);
    procedure.addLeg(leg);

    // Track to fix
    leg = new TLcdFeaturedProcedureLeg(0);
    leg.setType(TLcdProcedureLegType.TF);
    leg.setFix(RW06_END);
    leg.setAltitudeDescription(TLcdProcedureLegAltitudeDescription.BETWEEN);
    leg.setAltitudeLower(0);
    leg.setAltitudeUpper(3);
    leg.setSequenceNumber(20);
    procedure.addLeg(leg);

    // Course to an altitude
    leg = new TLcdFeaturedProcedureLeg(0);
    leg.setType(TLcdProcedureLegType.CA);
    leg.setCourse(59);
    leg.setAltitudeLower(500);
    leg.setAltitudeUpper(500);
    leg.setSequenceNumber(30);
    procedure.addLeg(leg);

    // Track to fix
    leg = new TLcdFeaturedProcedureLeg(0);
    leg.setType(TLcdProcedureLegType.TF);
    leg.setFix(EH014);
    leg.setAltitudeLower(6000);
    leg.setAltitudeUpper(6000);
    leg.setSequenceNumber(50);
    procedure.addLeg(leg);

    // Track to fix
    leg = new TLcdFeaturedProcedureLeg(0);
    leg.setType(TLcdProcedureLegType.TF);
    leg.setFix(EH068);
    leg.setAltitudeLower(6000);
    leg.setAltitudeUpper(6000);
    leg.setSequenceNumber(60);
    procedure.addLeg(leg);

    // Track to fix
    leg = new TLcdFeaturedProcedureLeg(0);
    leg.setType(TLcdProcedureLegType.TF);
    leg.setFix(EH071);
    leg.setAltitudeLower(6000);
    leg.setAltitudeUpper(6000);
    leg.setSequenceNumber(70);
    procedure.addLeg(leg);

    // Track to fix
    leg = new TLcdFeaturedProcedureLeg(0);
    leg.setType(TLcdProcedureLegType.TF);
    leg.setFix(ANDIK);
    leg.setAltitudeLower(6000);
    leg.setAltitudeUpper(6000);
    leg.setSequenceNumber(80);
    procedure.addLeg(leg);

    // Add the procedure to a proceduretrajectory, set a feature and add it to the model
    procedure.setDesignator("ANDIK 1R");
    procedure.setType(TLcdProcedureType.SID);
    TLcdProcedureTrajectory trajectory = new TLcdProcedureTrajectory(procedure,
                                                                     new TLcdAircraftPerformanceSettings(), fGeometryCalculator);

    return trajectory;
  }

  //RWY 06
  public static TLcdProcedureTrajectory createLOPIK1RSID() {
    // Create the procedure
    ILcdEditableProcedure procedure = new TLcdFeaturedProcedure(1);

    // Initial fix
    ILcdEditableProcedureLeg leg = new TLcdFeaturedProcedureLeg(0);
    leg.setType(TLcdProcedureLegType.IF);
    leg.setFix(RW06_BEGIN);
    leg.setAltitudeDescription(TLcdProcedureLegAltitudeDescription.BETWEEN);
    leg.setAltitudeLower(0);
    leg.setAltitudeUpper(3);
    leg.setCourse(59);
    leg.setSequenceNumber(10);
    procedure.addLeg(leg);

    // Track to fix
    leg = new TLcdFeaturedProcedureLeg(0);
    leg.setType(TLcdProcedureLegType.TF);
    leg.setFix(RW06_END);
    leg.setAltitudeDescription(TLcdProcedureLegAltitudeDescription.BETWEEN);
    leg.setAltitudeLower(0);
    leg.setAltitudeUpper(3);
    leg.setSequenceNumber(20);
    procedure.addLeg(leg);

    // Course to an altitude
    leg = new TLcdFeaturedProcedureLeg(0);
    leg.setType(TLcdProcedureLegType.CA);
    leg.setCourse(59);
    leg.setAltitudeLower(500);
    leg.setAltitudeUpper(500);
    leg.setSequenceNumber(30);
    procedure.addLeg(leg);

    // Course to radial
    leg = new TLcdFeaturedProcedureLeg(0);
    leg.setType(TLcdProcedureLegType.CR);
    leg.setRecommendedNavaid(EH036);
    leg.setTheta(2);
    leg.setCourse(59);
    leg.setAltitudeLower(500);
    leg.setAltitudeUpper(500);
    leg.setSequenceNumber(40);
    procedure.addLeg(leg);

    // Track to fix
    leg = new TLcdFeaturedProcedureLeg(0);
    leg.setType(TLcdProcedureLegType.TF);
    leg.setFix(EH036);
    leg.setAltitudeLower(6000);
    leg.setAltitudeUpper(6000);
    leg.setSequenceNumber(50);
    procedure.addLeg(leg);

    // Track to fix
    leg = new TLcdFeaturedProcedureLeg(0);
    leg.setType(TLcdProcedureLegType.TF);
    leg.setFix(EH033);
    leg.setAltitudeLower(6000);
    leg.setAltitudeUpper(6000);
    leg.setSequenceNumber(60);
    procedure.addLeg(leg);

    // Track to fix
    leg = new TLcdFeaturedProcedureLeg(0);
    leg.setType(TLcdProcedureLegType.TF);
    leg.setFix(LOPIK);
    leg.setAltitudeLower(6000);
    leg.setAltitudeUpper(6000);
    leg.setSequenceNumber(70);
    procedure.addLeg(leg);

    // Add the procedure to a proceduretrajectory, set a feature and add it to the model
    procedure.setDesignator("LOPIK 1R");
    procedure.setType(TLcdProcedureType.SID);
    TLcdProcedureTrajectory trajectory = new TLcdProcedureTrajectory(procedure,
                                                                     new TLcdAircraftPerformanceSettings(), fGeometryCalculator);

    return trajectory;
  }

  //RWY 18C
  public static TLcdProcedureTrajectory createSPIJKERBOOR2XSID() {
    // Create the procedure
    ILcdEditableProcedure procedure = new TLcdFeaturedProcedure(1);

    // Initial fix
    ILcdEditableProcedureLeg leg = new TLcdFeaturedProcedureLeg(0);
    leg.setType(TLcdProcedureLegType.IF);
    leg.setFix(RW18C_BEGIN);
    leg.setAltitudeDescription(TLcdProcedureLegAltitudeDescription.BETWEEN);
    leg.setAltitudeLower(0);
    leg.setAltitudeUpper(3);
    leg.setCourse(184);
    leg.setSequenceNumber(10);
    procedure.addLeg(leg);

    // Track to fix
    leg = new TLcdFeaturedProcedureLeg(0);
    leg.setType(TLcdProcedureLegType.TF);
    leg.setFix(RW18C_END);
    leg.setAltitudeDescription(TLcdProcedureLegAltitudeDescription.BETWEEN);
    leg.setAltitudeLower(0);
    leg.setAltitudeUpper(3);
    leg.setSequenceNumber(20);
    procedure.addLeg(leg);

    // Course to an altitude
    leg = new TLcdFeaturedProcedureLeg(0);
    leg.setType(TLcdProcedureLegType.CA);
    leg.setCourse(184);
    leg.setAltitudeLower(0);
    leg.setAltitudeUpper(500);
    leg.setSequenceNumber(30);
    procedure.addLeg(leg);

    // Track to fix
    leg = new TLcdFeaturedProcedureLeg(0);
    leg.setType(TLcdProcedureLegType.TF);
    leg.setFix(EH080);
    leg.setAltitudeLower(0);
    leg.setAltitudeUpper(500);
    leg.setSequenceNumber(40);
    procedure.addLeg(leg);

    // Course to radial
    leg = new TLcdFeaturedProcedureLeg(0);
    leg.setType(TLcdProcedureLegType.CR);
    leg.setRecommendedNavaid(EH049);
    leg.setTheta(285);
    leg.setCourse(234);
    leg.setAltitudeLower(0);
    leg.setAltitudeUpper(6000);
    leg.setSequenceNumber(50);
    procedure.addLeg(leg);

    // Course to radial
    leg = new TLcdFeaturedProcedureLeg(0);
    leg.setType(TLcdProcedureLegType.CR);
    leg.setRecommendedNavaid(EH057);
    leg.setTheta(211);
    leg.setCourse(285);
    leg.setAltitudeLower(0);
    leg.setAltitudeUpper(6000);
    leg.setSequenceNumber(50);
    procedure.addLeg(leg);

    // Track to fix
    leg = new TLcdFeaturedProcedureLeg(0);
    leg.setType(TLcdProcedureLegType.TF);
    leg.setFix(SPIJKERBOOR);
    leg.setAltitudeLower(0);
    leg.setAltitudeUpper(6000);
    leg.setSequenceNumber(70);
    procedure.addLeg(leg);

    // Track to fix
    leg = new TLcdFeaturedProcedureLeg(0);
    leg.setType(TLcdProcedureLegType.TF);
    leg.setFixOverflyType(TLcdProcedureLegFixOverflyType.FLY_BY);
    leg.setFix(ANDIK);
    leg.setAltitudeLower(0);
    leg.setAltitudeUpper(6000);
    leg.setSequenceNumber(80);
    procedure.addLeg(leg);

    // Add the procedure to a proceduretrajectory, set a feature and add it to the model
    procedure.setDesignator("SPIJKERBOOR 2X");
    procedure.setType(TLcdProcedureType.SID);
    TLcdProcedureTrajectory trajectory = new TLcdProcedureTrajectory(procedure,
                                                                     new TLcdAircraftPerformanceSettings(), fGeometryCalculator);

    return trajectory;
  }

  //RWY 18C
  public static TLcdProcedureTrajectory createVALKO2XSID() {
    // Create the procedure
    ILcdEditableProcedure procedure = new TLcdFeaturedProcedure(1);

    // Initial fix
    ILcdEditableProcedureLeg leg = new TLcdFeaturedProcedureLeg(0);
    leg.setType(TLcdProcedureLegType.IF);
    leg.setFix(RW18C_BEGIN);
    leg.setAltitudeDescription(TLcdProcedureLegAltitudeDescription.BETWEEN);
    leg.setAltitudeLower(0);
    leg.setAltitudeUpper(3);
    leg.setCourse(184);
    leg.setSequenceNumber(10);
    procedure.addLeg(leg);

    // Track to fix
    leg = new TLcdFeaturedProcedureLeg(0);
    leg.setType(TLcdProcedureLegType.TF);
    leg.setFix(RW18C_END);
    leg.setAltitudeDescription(TLcdProcedureLegAltitudeDescription.BETWEEN);
    leg.setAltitudeLower(0);
    leg.setAltitudeUpper(3);
    leg.setSequenceNumber(20);
    procedure.addLeg(leg);

    // Course to an altitude
    leg = new TLcdFeaturedProcedureLeg(0);
    leg.setType(TLcdProcedureLegType.CA);
    leg.setCourse(184);
    leg.setAltitudeLower(0);
    leg.setAltitudeUpper(500);
    leg.setSequenceNumber(30);
    procedure.addLeg(leg);

    // Track to fix
    leg = new TLcdFeaturedProcedureLeg(0);
    leg.setType(TLcdProcedureLegType.TF);
    leg.setFix(EH080);
    leg.setAltitudeLower(0);
    leg.setAltitudeUpper(500);
    leg.setSequenceNumber(40);
    procedure.addLeg(leg);

    // Course to fix
    leg = new TLcdFeaturedProcedureLeg(0);
    leg.setType(TLcdProcedureLegType.CF);
    leg.setFix(EH051);
    leg.setCourse(234);
    leg.setAltitudeLower(500);
    leg.setAltitudeUpper(6000);
    leg.setSequenceNumber(50);
    procedure.addLeg(leg);

    // Track to fix
    leg = new TLcdFeaturedProcedureLeg(0);
    leg.setType(TLcdProcedureLegType.TF);
    leg.setFix(EH009);
    leg.setAltitudeLower(0);
    leg.setAltitudeUpper(6000);
    leg.setSequenceNumber(60);
    procedure.addLeg(leg);

    // Track to fix
    leg = new TLcdFeaturedProcedureLeg(0);
    leg.setType(TLcdProcedureLegType.TF);
    leg.setFix(VALKO);
    leg.setAltitudeLower(0);
    leg.setAltitudeUpper(6000);
    leg.setSequenceNumber(70);
    procedure.addLeg(leg);

    // Add the procedure to a proceduretrajectory, set a feature and add it to the model
    procedure.setDesignator("VALKO 2X");
    procedure.setType(TLcdProcedureType.SID);
    TLcdProcedureTrajectory trajectory = new TLcdProcedureTrajectory(procedure,
                                                                     new TLcdAircraftPerformanceSettings(), fGeometryCalculator);

    return trajectory;
  }

  //RWY 18C
  public static TLcdProcedureTrajectory createBERGI2XSID() {
    // Create the procedure
    ILcdEditableProcedure procedure = new TLcdFeaturedProcedure(1);

    // Initial fix
    ILcdEditableProcedureLeg leg = new TLcdFeaturedProcedureLeg(0);
    leg.setType(TLcdProcedureLegType.IF);
    leg.setFix(RW18C_BEGIN);
    leg.setAltitudeDescription(TLcdProcedureLegAltitudeDescription.BETWEEN);
    leg.setAltitudeLower(0);
    leg.setAltitudeUpper(3);
    leg.setCourse(184);
    leg.setSequenceNumber(10);
    procedure.addLeg(leg);

    // Track to fix
    leg = new TLcdFeaturedProcedureLeg(0);
    leg.setType(TLcdProcedureLegType.TF);
    leg.setFix(RW18C_END);
    leg.setAltitudeDescription(TLcdProcedureLegAltitudeDescription.BETWEEN);
    leg.setAltitudeLower(0);
    leg.setAltitudeUpper(3);
    leg.setSequenceNumber(20);
    procedure.addLeg(leg);

    // Course to an altitude
    leg = new TLcdFeaturedProcedureLeg(0);
    leg.setType(TLcdProcedureLegType.CA);
    leg.setCourse(184);
    leg.setAltitudeLower(0);
    leg.setAltitudeUpper(500);
    leg.setSequenceNumber(30);
    procedure.addLeg(leg);

    // Track to fix
    leg = new TLcdFeaturedProcedureLeg(0);
    leg.setType(TLcdProcedureLegType.TF);
    leg.setFix(EH080);
    leg.setAltitudeLower(0);
    leg.setAltitudeUpper(500);
    leg.setSequenceNumber(40);
    procedure.addLeg(leg);

    // Course to fix
    leg = new TLcdFeaturedProcedureLeg(0);
    leg.setType(TLcdProcedureLegType.CF);
    leg.setFix(EH051);
    leg.setCourse(234);
    leg.setAltitudeLower(0);
    leg.setAltitudeUpper(6000);
    leg.setSequenceNumber(50);
    procedure.addLeg(leg);

    // Course to radial
    leg = new TLcdFeaturedProcedureLeg(0);
    leg.setType(TLcdProcedureLegType.CR);
    leg.setRecommendedNavaid(EH028);
    leg.setTheta(330);
    leg.setCourse(252);
    leg.setAltitudeLower(0);
    leg.setAltitudeUpper(6000);
    leg.setSequenceNumber(60);
    procedure.addLeg(leg);

    // Track to fix
    leg = new TLcdFeaturedProcedureLeg(0);
    leg.setType(TLcdProcedureLegType.TF);
    leg.setFix(EH028);
    leg.setAltitudeLower(0);
    leg.setAltitudeUpper(6000);
    leg.setSequenceNumber(70);
    procedure.addLeg(leg);

    // Track to fix
    leg = new TLcdFeaturedProcedureLeg(0);
    leg.setType(TLcdProcedureLegType.TF);
    leg.setFix(BERGI);
    leg.setAltitudeLower(0);
    leg.setAltitudeUpper(6000);
    leg.setSequenceNumber(80);
    procedure.addLeg(leg);

    // Track to fix
    leg = new TLcdFeaturedProcedureLeg(0);
    leg.setType(TLcdProcedureLegType.TF);
    leg.setFix(AMGOD);
    leg.setAltitudeLower(0);
    leg.setAltitudeUpper(6000);
    leg.setSequenceNumber(90);
    procedure.addLeg(leg);

    // Add the procedure to a proceduretrajectory, set a feature and add it to the model
    procedure.setDesignator("BERGI 2X");
    procedure.setType(TLcdProcedureType.SID);
    TLcdProcedureTrajectory trajectory = new TLcdProcedureTrajectory(procedure,
                                                                     new TLcdAircraftPerformanceSettings(), fGeometryCalculator);
    return trajectory;
  }
}

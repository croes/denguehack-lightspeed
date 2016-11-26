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

import java.io.IOException;
import java.util.Enumeration;

import com.luciad.ais.model.TLcdAISDataObjectFactory;
import com.luciad.ais.model.procedure.TLcdProcedureTrajectory;
import com.luciad.ais.model.procedure.TLcdProcedureTrajectoryModelDescriptor;
import com.luciad.ais.model.procedure.type.TLcdProcedureType;
import com.luciad.format.dafif.decoder.TLcdDAFIFIndependentProcedureDecoder;
import com.luciad.geodesy.TLcdGeodeticDatum;
import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelDecoder;
import com.luciad.model.TLcdVectorModel;
import com.luciad.reference.TLcdGeodeticReference;
import com.luciad.util.ILcdFireEventMode;
import com.luciad.util.logging.ILcdLogger;
import com.luciad.util.logging.TLcdLoggerFactory;

/**
 * @author tomn
 * @since 2012.0
 */
public class ProcedureModelDecoder implements ILcdModelDecoder {

  private static final ILcdLogger sLogger = TLcdLoggerFactory.getLogger(ProcedureModelDecoder.class);

  @Override
  public String getDisplayName() {
    return "Procedures";
  }

  @Override
  public boolean canDecodeSource(String aSourceName) {
    return true;
  }

  @Override
  public ILcdModel decode(String aSourceName) throws IOException {
    ProcedureClimbEarly3DGeometryCalculator fGeometryCalculator = new ProcedureClimbEarly3DGeometryCalculator();

    //read dafif procedure data
    TLcdDAFIFIndependentProcedureDecoder procedure_decoder = new TLcdDAFIFIndependentProcedureDecoder(new TLcdAISDataObjectFactory());
    String source = "Data/internal.data/lvnl/dafif/EH";
    ILcdModel model = procedure_decoder.decode(source);

    //filter procedures
//      ProcedureFilter procedure_filter = new ProcedureFilter();
    TLcdGeodeticReference reference = new TLcdGeodeticReference(new TLcdGeodeticDatum());
    TLcdProcedureTrajectoryModelDescriptor model_descriptor = (TLcdProcedureTrajectoryModelDescriptor) model.getModelDescriptor();
    TLcdProcedureTrajectoryModelDescriptor iap_model_descriptor = new TLcdProcedureTrajectoryModelDescriptor(model_descriptor.getSourceName(), model_descriptor.getTypeName(), "IAP", null);
    TLcdProcedureTrajectoryModelDescriptor sid_model_descriptor = new TLcdProcedureTrajectoryModelDescriptor(model_descriptor.getSourceName(), model_descriptor.getTypeName(), "SID", null);
    TLcdProcedureTrajectoryModelDescriptor star_model_descriptor = new TLcdProcedureTrajectoryModelDescriptor(model_descriptor.getSourceName(), model_descriptor.getTypeName(), "STAR", null);

    ILcdModel fSIDModel = new TLcdVectorModel(reference, sid_model_descriptor);

    // From DAFIF
    Enumeration procedure_enumeration = model.elements();
    while (procedure_enumeration.hasMoreElements()) {
      TLcdProcedureTrajectory procedure_trajectory = (TLcdProcedureTrajectory) procedure_enumeration.nextElement();

      // Set aircraft performance settings
//        procedure_trajectory.getAircraftPerformanceSettings().setClimbRate(0.2);
//        procedure_trajectory.getAircraftPerformanceSettings().setMinimumTurnRadius(0.6 * TLcdDistanceUnit.NM_UNIT.getToMetreFactor());
      procedure_trajectory.invalidate();

      // Use new geometry calculator
      procedure_trajectory.setProcedureGeometryCalculator(fGeometryCalculator);

      ProcedureFilter procedure_filter = new ProcedureFilter();
      if (procedure_filter.accept(procedure_trajectory.getProcedure().getDesignator())) {
        if (procedure_trajectory.getProcedure().getType() == TLcdProcedureType.IAP) {
          sLogger.debug("Loaded IAP model for procedure " + procedure_trajectory.getProcedure()
                                                                                .getDesignator());
          fSIDModel.addElement(procedure_trajectory, ILcdFireEventMode.NO_EVENT);
        } else if (procedure_trajectory.getProcedure().getType() == TLcdProcedureType.SID) {
          sLogger.debug("Loaded SID model for procedure " + procedure_trajectory.getProcedure().getDesignator());
          fSIDModel.addElement(procedure_trajectory, ILcdFireEventMode.NO_EVENT);
        } else if (procedure_trajectory.getProcedure().getType() == TLcdProcedureType.STAR) {
          sLogger.debug("Loaded STAR model for procedure " + procedure_trajectory.getProcedure().getDesignator());
          fSIDModel.addElement(procedure_trajectory, ILcdFireEventMode.NO_EVENT);
        }
      }
    }
    sLogger.debug(procedure_decoder.getErrorMessage());

    // Add custom procedure.
    //RW 06
    fSIDModel.addElement(SIDFactory.createLEKKO1RSID(), ILcdFireEventMode.NO_EVENT);
    fSIDModel.addElement(SIDFactory.createLOPIK1RSID(), ILcdFireEventMode.NO_EVENT);
    fSIDModel.addElement(SIDFactory.createANDIK1RSID(), ILcdFireEventMode.NO_EVENT);
    fSIDModel.addElement(SIDFactory.createLEKKO1TSID(), ILcdFireEventMode.NO_EVENT);
    //RW 18C
    fSIDModel.addElement(SIDFactory.createSPIJKERBOOR2XSID(), ILcdFireEventMode.NO_EVENT);
    fSIDModel.addElement(SIDFactory.createVALKO2XSID(), ILcdFireEventMode.NO_EVENT);
    fSIDModel.addElement(SIDFactory.createBERGI2XSID(), ILcdFireEventMode.NO_EVENT);

    return fSIDModel;
  }
}

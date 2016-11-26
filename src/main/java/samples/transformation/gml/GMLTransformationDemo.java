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
package samples.transformation.gml;

import static samples.gxy.fundamentals.step2.FlightPlanDataTypes.NAME;
import static samples.gxy.fundamentals.step2.FlightPlanDataTypes.POLYLINE;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Collections;
import java.util.Enumeration;

import com.luciad.datamodel.ILcdDataObject;
import com.luciad.datamodel.TLcdDataObject;
import com.luciad.format.gml31.model.TLcdGML31DataTypes;
import com.luciad.format.gmlcommon.transformation.TLcdGMLApplicationModelDecoder;
import com.luciad.format.gmlcommon.transformation.TLcdGMLApplicationModelEncoder;
import com.luciad.format.gmlcommon.transformation.TLcdGMLTransformer;
import com.luciad.format.gmlcommon.transformation.TLcdGMLTransformerBuilder;
import com.luciad.io.ILcdOutputStreamFactory;
import com.luciad.io.TLcdInputStreamFactory;
import com.luciad.model.ILcdDataModelDescriptor;
import com.luciad.model.ILcdModel;
import com.luciad.model.TLcdDataModelDescriptor;
import com.luciad.model.TLcdVectorModel;
import com.luciad.reference.TLcdGeodeticReference;
import com.luciad.shape.ILcdPolyline;
import com.luciad.shape.shape2D.TLcdLonLatPolyline;

import samples.gxy.fundamentals.step2.FlightPlanDataTypes;

/**
 * This sample shows how the {@link TLcdGMLTransformer} can be used to
 * transform models from and to GML.
 */
public class GMLTransformationDemo {

  public static void main(String[] aArgs) throws IOException {
    new GMLTransformationDemo(false, false).run();
  }

  private TLcdGMLTransformer fTransformer;
  private PrintStream fOut = System.out;

  /**
   * Constructs a new GMLTransformation
   *
   * @param useSimpleFeaturesProfile if true, the GML application model is generated
   * according to the simple features profile
   * @param useGML31 if true, a GML application model is generated for GML31, otherwise
   * GML32 is used
   */
  public GMLTransformationDemo(boolean useSimpleFeaturesProfile, boolean useGML31) {
    TLcdGMLTransformerBuilder builder = new TLcdGMLTransformerBuilder(FlightPlanDataTypes.getDataModel());
    if (useSimpleFeaturesProfile) {
      builder.useSimpleFeaturesProfile();
    }
    if (useGML31) {
      builder.gmlDataModel(TLcdGML31DataTypes.getDataModel());
    }
    fTransformer = builder.build();
  }

  public void run() throws IOException {
    ILcdModel model = createFlightPlanModel();
    dump(model);
    showApplicationSchema();
    dump(encodeDecode(model));
  }

  public void showApplicationSchema() throws IOException {
    fOut.println("--------- GML Application Schema -----------");
    fTransformer.writeSchema(fOut);
    fOut.println();
  }

  private ILcdModel createFlightPlanModel() {
    TLcdVectorModel model = new TLcdVectorModel();
    model.setModelDescriptor(new TLcdDataModelDescriptor(FlightPlanDataTypes.getDataModel(),
                                                         Collections.singleton(FlightPlanDataTypes.FLIGHT_PLAN_DATA_TYPE),
                                                         FlightPlanDataTypes.getDataModel().getTypes()));
    model.setModelReference(new TLcdGeodeticReference());
    for (int i = 0; i < 2; i++) {
      ILcdDataObject flightPlan = new TLcdDataObject(FlightPlanDataTypes.FLIGHT_PLAN_DATA_TYPE);
      flightPlan.setValue(NAME, "Flight plan " + (i + 1));
      TLcdLonLatPolyline polyline = new TLcdLonLatPolyline();
      for (int j = 0; j < i + 3; j++) {
        polyline.insert2DPoint(j, j + 90, j);
      }
      flightPlan.setValue(POLYLINE, polyline);
      model.addElement(flightPlan, ILcdModel.NO_EVENT);
    }
    return model;
  }

  public void dump(ILcdModel aModel) {
    fOut.println("--------- Dumping model -------");
    fOut.println("ModelReference: " + aModel.getModelReference());
    fOut.println("ModelDescriptor: " + ((ILcdDataModelDescriptor) aModel.getModelDescriptor()).getDataModel().getDisplayName());
    Enumeration<ILcdDataObject> elements = aModel.elements();
    while (elements.hasMoreElements()) {
      ILcdDataObject flightPlan = elements.nextElement();
      fOut.println("  " + flightPlan.getValue(FlightPlanDataTypes.NAME) + " contains " +
                   ((ILcdPolyline) flightPlan.getValue(FlightPlanDataTypes.POLYLINE)).getPointCount() + " points.");
    }
    fOut.println();
  }

  public byte[] encodeAsGML(ILcdModel aModel) throws IOException {
    TLcdGMLApplicationModelEncoder encoder = new TLcdGMLApplicationModelEncoder(fTransformer);
    final ByteArrayOutputStream bos = new ByteArrayOutputStream();
    encoder.getEncoder().setOutputStreamFactory(new ILcdOutputStreamFactory() {
      public OutputStream createOutputStream(String aDestination) throws IOException {
        return bos;
      }
    });
    encoder.save(aModel);
    return bos.toByteArray();
  }

  public ILcdModel encodeDecode(ILcdModel aModel) throws IOException {
    byte[] gml = encodeAsGML(aModel);
    fOut.println("------------- Encoded as GML -------------");
    fOut.println(new String(gml));
    fOut.println();
    return decode(new ByteArrayInputStream(gml));
  }

  public ILcdModel decode(final InputStream aInputStream) throws IOException {
    TLcdGMLApplicationModelDecoder decoder = new TLcdGMLApplicationModelDecoder(fTransformer);
    decoder.getDecoder().setInputStreamFactory(new TLcdInputStreamFactory() {

      @Override
      public InputStream createInputStream(String aSourceName) throws IOException {
        if ("test".equals(aSourceName)) {
          return aInputStream;
        }
        return super.createInputStream(aSourceName);
      }

    });
    return decoder.decode("test");
  }

  public PrintStream getOut() {
    return fOut;
  }

  public void setOut(PrintStream aOut) {
    fOut = aOut;
  }

  public TLcdGMLTransformer getTransformer() {
    return fTransformer;
  }

}

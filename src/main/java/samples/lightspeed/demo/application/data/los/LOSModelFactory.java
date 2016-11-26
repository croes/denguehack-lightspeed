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
package samples.lightspeed.demo.application.data.los;

import java.lang.reflect.Constructor;
import java.util.Properties;

import com.luciad.geodesy.ILcdEllipsoid;
import com.luciad.geodesy.TLcdGeodeticDatum;
import com.luciad.model.ILcdModel;
import com.luciad.model.TLcdModelDescriptor;
import com.luciad.model.TLcdVectorModel;
import com.luciad.reference.TLcdGeodeticReference;
import com.luciad.shape.ILcdPoint;
import com.luciad.shape.shape2D.TLcdLonLatPoint;

import samples.lightspeed.demo.framework.data.AbstractModelFactory;

/**
 * Factory for creating LOS input models. The generated model will contain a LOSCoverageInputShape
 */
public class LOSModelFactory extends AbstractModelFactory {
  private static final String LOS_COVERAGE_INPUT_SHAPE = "samples.tea.lightspeed.los.model.LOSCoverageInputShape";

  private double fPositionLon;
  private double fPositionLat;
  private double fMaxRadius;
  private double fRadiusStep;
  private double fStartAngle;
  private double fArcAngle;
  private double fAngleStep;

  public LOSModelFactory(String aType) {
    super(aType);
  }

  @Override
  public ILcdModel createModel(String aSource) {
    Object inputObject;
    TLcdGeodeticReference geodeticReference = new TLcdGeodeticReference(new TLcdGeodeticDatum());
    try {
      Class<?> losCoverageInputShapeClass = Class.forName(LOS_COVERAGE_INPUT_SHAPE);
      Constructor<?> constructor = losCoverageInputShapeClass.getConstructor(ILcdPoint.class, double.class, double.class, double.class, double.class, double.class, double.class, double.class, ILcdEllipsoid.class, boolean.class);
      inputObject = constructor.newInstance(new TLcdLonLatPoint(fPositionLon, fPositionLat),
                                            fMaxRadius,
                                            fRadiusStep,
                                            fStartAngle,
                                            fArcAngle,
                                            fAngleStep,
                                            0,
                                            180,
                                            geodeticReference.getGeodeticDatum().getEllipsoid(),
                                            false);
    } catch (Exception e) {
      throw new UnsupportedOperationException(e);
    }

    final TLcdVectorModel losInputModel = new TLcdVectorModel(
        geodeticReference,
        new TLcdModelDescriptor("LOSInputModel", "LOSInputModel", "LOSInputModel")
    );
    losInputModel.addElement(inputObject, ILcdModel.FIRE_NOW);
    return losInputModel;
  }

  @Override
  public void configure(Properties aProperties) {
    super.configure(aProperties);
    fPositionLon = Double.parseDouble(aProperties.getProperty("positionLon", "-122.451"));
    fPositionLat = Double.parseDouble(aProperties.getProperty("positionLat", "37.753"));
    fMaxRadius = Double.parseDouble(aProperties.getProperty("maxRadius", "5000.0"));
    fRadiusStep = Double.parseDouble(aProperties.getProperty("radiusStep", "125.0"));
    fStartAngle = Double.parseDouble(aProperties.getProperty("startAngle", "0.0"));
    fArcAngle = Double.parseDouble(aProperties.getProperty("arcAngle", "360.0"));
    fAngleStep = Double.parseDouble(aProperties.getProperty("angleStep", "1"));
  }

}

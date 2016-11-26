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
package samples.decoder.aixm51.transformation;

import java.util.Map;

import com.luciad.ais.model.airspace.ILcdAirspace;
import com.luciad.ais.model.util.TLcdAltitudeReference;
import com.luciad.datamodel.transformation.ALcdObjectTransformation;
import com.luciad.format.aixm.model.airspace.TLcdAIXMAirspaceDataProperties;
import com.luciad.format.aixm51.model.datatypes.TLcdAIXM51CodeAirspaceAggregation;
import com.luciad.format.aixm51.model.datatypes.TLcdAIXM51CodeVerticalReference;
import com.luciad.format.aixm51.model.datatypes.TLcdAIXM51ValDistanceVertical;
import com.luciad.format.aixm51.model.features.airspace.TLcdAIXM51AirspaceGeometryComponent;
import com.luciad.format.aixm51.model.features.airspace.TLcdAIXM51AirspaceVolume;
import com.luciad.format.aixm51.model.features.geometry.TLcdAIXM51Surface;
import com.luciad.format.gml32.model.TLcdGML32PolygonPatch;
import com.luciad.format.gml32.model.TLcdGML32Ring;
import com.luciad.util.TLcdAltitudeUnit;
import com.luciad.util.iso19103.ILcdISO19103Measure;

/**
 * A transformation from an AIS (AIXM 3/4, but the code can be easily adopted to DAFIF or ARINC) airspace geometry,
 * represented by an <code>ILcdGeoPath</code>, to an AIXM 5.1 airspace geometry, represented by
 * <code>TLcdAIXM51AirspaceGeometryComponent</code>.
 * <p/>
 * The actual mapping from an <code>ILcdGeoPath</code> to a suitable GML
 * geometry is done by {@link LonLatGeoPathAsCurve}.
 */
class AirspaceGeometryTransformation extends ALcdObjectTransformation {

  @Override
  public Object transform(Object aObject, Map<Object, Object> aContext) {
    ILcdAirspace airspace = (ILcdAirspace) aObject;

    TLcdAIXM51AirspaceGeometryComponent geometryComponent = new TLcdAIXM51AirspaceGeometryComponent();
    geometryComponent.setOperation(TLcdAIXM51CodeAirspaceAggregation.BASE);
    geometryComponent.setOperationSequence(0L);
    TLcdAIXM51AirspaceVolume volume = new TLcdAIXM51AirspaceVolume();

    TLcdAIXM51ValDistanceVertical lowerLimit = new TLcdAIXM51ValDistanceVertical();
    setupLimit(lowerLimit, (Float) airspace.getValue(TLcdAIXMAirspaceDataProperties.LOWER_LIMIT), (TLcdAltitudeUnit) airspace.getValue(TLcdAIXMAirspaceDataProperties.LOWER_LIMIT_UNIT));
    if (lowerLimit.getValueObject() != null) {
      volume.setLowerLimit(lowerLimit);
      volume.setLowerLimitReference(convertAltitudeReference((TLcdAltitudeReference) airspace.getValue(TLcdAIXMAirspaceDataProperties.LOWER_LIMIT_REFERENCE)));
    }

    TLcdAIXM51ValDistanceVertical upperLimit = new TLcdAIXM51ValDistanceVertical();
    setupLimit(upperLimit, (Float) airspace.getValue(TLcdAIXMAirspaceDataProperties.UPPER_LIMIT), (TLcdAltitudeUnit) airspace.getValue(TLcdAIXMAirspaceDataProperties.UPPER_LIMIT_UNIT));
    if (upperLimit.getValueObject() != null) {
      volume.setUpperLimit(upperLimit);
      volume.setUpperLimitReference(convertAltitudeReference((TLcdAltitudeReference) airspace.getValue(TLcdAIXMAirspaceDataProperties.UPPER_LIMIT_REFERENCE)));
    }

    TLcdAIXM51ValDistanceVertical minimumLimit = new TLcdAIXM51ValDistanceVertical();
    setupLimit(minimumLimit, (Float) airspace.getValue(TLcdAIXMAirspaceDataProperties.MINIMUM_LIMIT), (TLcdAltitudeUnit) airspace.getValue(TLcdAIXMAirspaceDataProperties.MINIMUM_LIMIT_UNIT));
    if (minimumLimit.getValueObject() != null) {
      volume.setMinimumLimit(minimumLimit);
      volume.setMinimumLimitReference(convertAltitudeReference((TLcdAltitudeReference) airspace.getValue(TLcdAIXMAirspaceDataProperties.MINIMUM_LIMIT_REFERENCE)));
    }

    TLcdAIXM51ValDistanceVertical maximumLimit = new TLcdAIXM51ValDistanceVertical();
    setupLimit(maximumLimit, (Float) airspace.getValue(TLcdAIXMAirspaceDataProperties.MAXIMUM_LIMIT), (TLcdAltitudeUnit) airspace.getValue(TLcdAIXMAirspaceDataProperties.MAXIMUM_LIMIT_UNIT));
    if (maximumLimit.getValueObject() != null) {
      volume.setMaximumLimit(maximumLimit);
      volume.setMaximumLimitReference(convertAltitudeReference((TLcdAltitudeReference) airspace.getValue(TLcdAIXMAirspaceDataProperties.MAXIMUM_LIMIT_REFERENCE)));
    }

    TLcdAIXM51Surface surface = new TLcdAIXM51Surface(AIXM51FeatureDataTypeMapping.getSRS(aContext));
    TLcdGML32PolygonPatch polygonPatch = new TLcdGML32PolygonPatch();

    LonLatGeoPathAsCurve curve = new LonLatGeoPathAsCurve(airspace, AIXM51FeatureDataTypeMapping.getSRS(aContext));
    TLcdGML32Ring ring = new TLcdGML32Ring();
    ring.getCurveMember().add(curve);
    polygonPatch.setExterior(ring);
    surface.getPatches().add(polygonPatch);
    volume.setHorizontalProjection(surface);
    geometryComponent.setTheAirspaceVolume(volume);

    return geometryComponent;
  }

  @Override
  protected Object invert(Object aObject, Map<Object, Object> aContext) {
    throw new UnsupportedOperationException();
  }

  private TLcdAIXM51CodeVerticalReference convertAltitudeReference(TLcdAltitudeReference aValue) {
    if (aValue == null) {
      return null;
    }
    if (TLcdAltitudeReference.ALT == aValue) {
      return TLcdAIXM51CodeVerticalReference.MSL;
    } else if (TLcdAltitudeReference.STD == aValue) {
      return TLcdAIXM51CodeVerticalReference.STD;
    } else if (TLcdAltitudeReference.HEI == aValue) {
      return TLcdAIXM51CodeVerticalReference.SFC;
    }
    if (TLcdAltitudeReference.W84 == aValue) {
      return TLcdAIXM51CodeVerticalReference.W84;
    }
    return new TLcdAIXM51CodeVerticalReference("Other:" + aValue.toString());
  }

  private void setupLimit(ILcdISO19103Measure aLimit, Float aLimitValue, TLcdAltitudeUnit aLimitUom) {
    if (aLimitValue != null && aLimitUom != null) {
      aLimit.setValue(aLimitValue);
      aLimit.setUnitOfMeasure(aLimitUom);
    }
  }
}

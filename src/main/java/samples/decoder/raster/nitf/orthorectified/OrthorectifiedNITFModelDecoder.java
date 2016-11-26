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
package samples.decoder.raster.nitf.orthorectified;

import java.io.IOException;
import java.util.Enumeration;

import com.luciad.format.raster.ILcdMultilevelRaster;
import com.luciad.format.raster.ILcdRaster;
import com.luciad.format.raster.TLcdDEMModelDecoder;
import com.luciad.format.raster.TLcdDMEDModelDecoder;
import com.luciad.format.raster.TLcdDTEDModelDecoder;
import com.luciad.format.raster.TLcdETOPOModelDecoder;
import com.luciad.format.raster.TLcdNITFModelDecoder;
import com.luciad.imaging.ALcdBasicImage;
import com.luciad.imaging.ALcdImage;
import com.luciad.imaging.ALcdMultilevelImage;
import com.luciad.imaging.TLcdImageBuilder;
import com.luciad.io.ILcdInputStreamFactory;
import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelDecoder;
import com.luciad.model.ILcdModelReference;
import com.luciad.model.TLcd2DBoundsIndexedModel;
import com.luciad.projection.ILcdProjection;
import com.luciad.projection.TLcdOrthorectifiedProjection;
import com.luciad.projection.TLcdPerspectiveProjection;
import com.luciad.reference.ILcdGeoReference;
import com.luciad.reference.ILcdGridReference;
import com.luciad.reference.TLcdGeodeticReference;
import com.luciad.reference.TLcdGridReference;
import com.luciad.reference.TLcdModelReferenceDecoder;
import com.luciad.reference.TLcdUTMGrid;
import com.luciad.shape.shape2D.TLcdXYBounds;
import com.luciad.transformation.TLcdGeoReference2GeoReference;
import com.luciad.util.TLcdNoBoundsException;
import com.luciad.util.TLcdSharedBuffer;
import com.luciad.util.height.ILcdHeightProvider;
import com.luciad.util.height.TLcdRasterHeightProvider;
import com.luciad.util.height.TLcdTransformedHeightProvider;
import com.luciad.util.service.LcdService;

/**
 * This model decoder decodes NITF rasters and DEM/DMED/DTED/ETOPO elevation data.
 * If the NITF raster contains a SENSRB extension, it is orthorectified, based
 * on previously decoded elevation data.
 * <p>
 * In other applications, decoding the elevation data could be done by
 * an external decoder, as long as the data is available for the
 * orthorectification step.
 * </p>
 * <p>
 * This decoder is only available for NITF if some elevation data was decoded first.
 * Elevation data decoded by another decoder cannot be used; you must decode
 * DEM/DMED/DTED/ETOPO data using <em>this</em> decoder.
 * </p>
 */
@LcdService(service = ILcdModelDecoder.class, priority = LcdService.LOW_PRIORITY)
public class OrthorectifiedNITFModelDecoder implements ILcdModelDecoder {

  private final TLcdNITFModelDecoder fNITFModelDecoder;
  private final TLcdDEMModelDecoder fDEMModelDecoder = new TLcdDEMModelDecoder();
  private final TLcdDMEDModelDecoder fDMEDModelDecoder = new TLcdDMEDModelDecoder();
  private final TLcdDTEDModelDecoder fDTEDModelDecoder = new TLcdDTEDModelDecoder();
  private final TLcdETOPOModelDecoder fETOPOModelDecoder = new TLcdETOPOModelDecoder();

  private static ILcdModel fElevationModel;

  /**
   * Creates a new OrthorectifiedNITFModelDecoder.
   */
  public OrthorectifiedNITFModelDecoder() {
    // Create a model decoder. In this example, we also provide a model
    // reference decoder, a default model reference, and default raster bounds,
    // in case a decoded raster doesn't specify them.
    fNITFModelDecoder = new TLcdNITFModelDecoder(TLcdSharedBuffer.getBufferInstance(),
                                                 new TLcdModelReferenceDecoder(),
                                                 new TLcdUTMGrid(30),
                                                 new TLcdXYBounds(5e5, 0.0, 1e3, 1e3));

    // Allow a raster to be positioned in an approximate orthographic reference
    // that better suits the specified corner points. This is typically useful
    // when decoding aerial imagery that is only positioned approximately.
    fNITFModelDecoder.setAllowOrthographicReferencing(true);

    // Allow a large residual error when positioning the raster based on its
    // corner points. If the residual error is larger than this value, the
    // raster is rejected with an IOException.
    fNITFModelDecoder.setMaximumResidualTiePointError(1000.0);

    // We can set some options on the elevation model decoders.
    //fDEMModelDecoder.setSupportGeoidDatums( true );
    //fDMEDModelDecoder.setSupportGeoidDatums( true );
    //fDTEDModelDecoder.setSupportGeoidDatums( true );
    //fETOPOModelDecoder.setSupportGeoidDatums( true );

    fDMEDModelDecoder.setSampleStrategy(TLcdDMEDModelDecoder.BILINEAR);
    fDTEDModelDecoder.setSampleStrategy(TLcdDTEDModelDecoder.BILINEAR);
  }

  public void setInputStreamFactory(ILcdInputStreamFactory aInputStreamFactory) {
    fNITFModelDecoder.setInputStreamFactory(aInputStreamFactory);
    fDEMModelDecoder.setInputStreamFactory(aInputStreamFactory);
    fDMEDModelDecoder.setInputStreamFactory(aInputStreamFactory);
    fDTEDModelDecoder.setInputStreamFactory(aInputStreamFactory);
    fETOPOModelDecoder.setInputStreamFactory(aInputStreamFactory);
  }

  public ILcdInputStreamFactory getInputStreamFactory() {
    return fNITFModelDecoder.getInputStreamFactory();
  }

  public String getDisplayName() {
    return "NITF orthorectifying decoder";
  }

  public boolean canDecodeSource(String aSourceName) {
    return fDEMModelDecoder.canDecodeSource(aSourceName) ||
           fDMEDModelDecoder.canDecodeSource(aSourceName) ||
           fDTEDModelDecoder.canDecodeSource(aSourceName) ||
           fETOPOModelDecoder.canDecodeSource(aSourceName) ||
           (fElevationModel != null &&
            fNITFModelDecoder.canDecodeSource(aSourceName));
  }

  public ILcdModel decode(String aSourceName) throws IOException {
    // Decode, remember, and return the elevation model, if possible.
    if (fDEMModelDecoder.canDecodeSource(aSourceName)) {
      return fElevationModel = fDEMModelDecoder.decode(aSourceName);
    }
    if (fDMEDModelDecoder.canDecodeSource(aSourceName)) {
      return fElevationModel = fDMEDModelDecoder.decode(aSourceName);
    }
    if (fDTEDModelDecoder.canDecodeSource(aSourceName)) {
      return fElevationModel = fDTEDModelDecoder.decode(aSourceName);
    }
    if (fETOPOModelDecoder.canDecodeSource(aSourceName)) {
      return fElevationModel = fETOPOModelDecoder.decode(aSourceName);
    }

    // Otherwise decode the NITF model.
    ILcdModel model = fNITFModelDecoder.decode(aSourceName);

    // Can we orthorectify the NITF model?
    if (fElevationModel != null) {

      ILcdModelReference originalModelReference = model.getModelReference();
      if (originalModelReference instanceof ILcdGridReference) {
        ILcdGridReference originalGridReference = (ILcdGridReference) originalModelReference;

        ILcdProjection originalProjection = originalGridReference.getProjection();
        if (originalProjection instanceof TLcdPerspectiveProjection) {

          try {
            // Get the elevation raster. For simplicity, we are only considering
            // a single raster from a single model.
            ILcdRaster elevationRaster = retrieveElevationRaster(fElevationModel);

            // Create an orthorectified version of the original NITF reference.
            TLcdGridReference orthorectifiedReference = createOrthorectifiedReference(
                originalGridReference,
                elevationRaster,
                (ILcdGeoReference) fElevationModel.getModelReference()
            );

            // Copy the model, with the orthorectified reference.
            return copyModel(model, orthorectifiedReference);
          } catch (TLcdNoBoundsException aException) {
            aException.printStackTrace();
          }
        }
      }
    }

    return model;
  }

  /**
   * Extracts the most detailed elevation raster from the given elevation model.
   */
  private static ILcdRaster retrieveElevationRaster(ILcdModel aElevationModel) {
    // We are only considering the first element here.
    Object element = aElevationModel.elements().nextElement();
    if (element instanceof ILcdRaster) {
      return (ILcdRaster) element;
    } else {
      ILcdMultilevelRaster multilevelRaster = (ILcdMultilevelRaster) element;
      return multilevelRaster.getRaster(multilevelRaster.getRasterCount() - 1);
    }
  }

  /**
   * Creates an orthorectified version of the given grid reference.
   */
  private static TLcdGridReference createOrthorectifiedReference(ILcdGridReference aOriginalGridReference,
                                                                 ILcdRaster aElevationRaster,
                                                                 ILcdGeoReference aElevationModelReference) throws TLcdNoBoundsException {

    // The original grid reference must have a perspective projection. It
    // contains the camera information based on which the orthorectification
    // can be computed.
    TLcdPerspectiveProjection cameraProjection = (TLcdPerspectiveProjection) aOriginalGridReference.getProjection();

    // Create a height provider from the elevation raster. The height provider
    // has to return heights in a geodetic coordinate system with the geodetic
    // datum of the NITF raster.
    TLcdGeodeticReference wgs84 = new TLcdGeodeticReference(aOriginalGridReference.getGeodeticDatum());
    TLcdGeoReference2GeoReference transformation = new TLcdGeoReference2GeoReference(aElevationModelReference, wgs84);
    TLcdRasterHeightProvider rasterHeightProvider = new TLcdRasterHeightProvider(aElevationRaster);
    ILcdHeightProvider heightProvider = new TLcdTransformedHeightProvider(rasterHeightProvider, transformation);

    // Derive an orthorectified projection. It magically adjusts the
    // transformations of the the original camera projection to correct
    // for the terrain distortion.
    TLcdOrthorectifiedProjection orthorectifiedProjection = new TLcdOrthorectifiedProjection(cameraProjection, heightProvider);

    // Derive an orthorectified reference. This reference contains the adjusted
    // transformations, making sure the NITF raster will be painted in the
    // correct location.
    TLcdGridReference orthorectifiedReference = new TLcdGridReference(aOriginalGridReference);
    orthorectifiedReference.setProjection(orthorectifiedProjection);

    return orthorectifiedReference;
  }

  /**
   * Copies a given model, with a different given model reference.
   */
  private static ILcdModel copyModel(ILcdModel aModel, ILcdModelReference aModelReference) {
    // Create a new model.
    TLcd2DBoundsIndexedModel newModel = new TLcd2DBoundsIndexedModel();
    newModel.setModelDescriptor(aModel.getModelDescriptor());
    newModel.setModelReference(aModelReference);

    // Copy the elements.
    Enumeration elements = aModel.elements();
    while (elements.hasMoreElements()) {
      Object elem = copyElement(elements.nextElement(), aModelReference);
      newModel.addElement(elem, ILcdModel.NO_EVENT);
    }

    return newModel;
  }

  /**
   * Constructs a model element based on the given element in the given reference.
   * This is required for image elements as they have a reference of their own, which
   * may be different from the model reference. Raster and vector elements use the
   * model reference and are returned as-is.
   */
  private static Object copyElement(Object aElement, ILcdModelReference aModelReference) {
    if (aElement instanceof ALcdImage) {
      if (aElement instanceof ALcdBasicImage) {
        ALcdBasicImage bi = (ALcdBasicImage) aElement;
        return TLcdImageBuilder
            .newBuilder()
            .image(bi)
            .imageReference(aModelReference)
            .buildBasicImage();
      } else if (aElement instanceof ALcdMultilevelImage) {
        TLcdImageBuilder imageBuilder = TLcdImageBuilder.newBuilder();
        ALcdMultilevelImage mli = (ALcdMultilevelImage) aElement;
        for (int i = 0; i < mli.getConfiguration().getNumberOfLevels(); i++) {
          imageBuilder.image((ALcdBasicImage) copyElement(mli.getLevel(i), aModelReference));
        }
        return imageBuilder.buildMultilevelImage();
      } else {
        throw new IllegalArgumentException("Unsupported element: " + aElement);
      }
    }
    return aElement;
  }
}

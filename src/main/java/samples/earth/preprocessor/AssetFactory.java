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
package samples.earth.preprocessor;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import com.luciad.earth.metadata.ILcdEarthAsset;
import com.luciad.earth.metadata.TLcdEarthRasterAsset;
import com.luciad.earth.metadata.format.ILcdEarthAssetCodec;
import com.luciad.earth.tileset.ILcdEarthTileSetCoverage;
import com.luciad.format.raster.ILcdMultilevelRaster;
import com.luciad.format.raster.ILcdRaster;
import com.luciad.format.raster.TLcdMultilevelRasterModelDescriptor;
import com.luciad.format.raster.TLcdRasterModelDescriptor;
import com.luciad.io.TLcdIOUtil;
import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelDecoder;
import com.luciad.model.ILcdModelDescriptor;
import com.luciad.model.ILcdModelReference;
import com.luciad.model.ILcdModelReferenceDecoder;
import com.luciad.reference.ILcdGeoReference;
import com.luciad.reference.ILcdGeodeticReference;
import com.luciad.reference.TLcdModelReferenceDecoder;
import com.luciad.shape.ILcdBounded;
import com.luciad.shape.ILcdBounds;
import com.luciad.shape.shape3D.ILcd3DEditableBounds;
import com.luciad.shape.shape3D.TLcdLonLatHeightBounds;
import com.luciad.shape.shape3D.TLcdXYZBounds;
import com.luciad.transformation.TLcdGeoReference2GeoReference;
import com.luciad.util.ILcdBuffer;
import com.luciad.util.TLcdNoBoundsException;
import com.luciad.util.TLcdOutOfBoundsException;
import com.luciad.util.TLcdSharedBuffer;
import com.luciad.util.logging.ILcdLogger;
import com.luciad.util.logging.TLcdLoggerFactory;

/**
 * A factory for creating an asset from a file and model decoder.
 */
public class AssetFactory {

  private static final ILcdLogger sLogger = TLcdLoggerFactory.getLogger(AssetFactory.class);

  private static final String MODELDECODERS_SOURCE_NAME = "samples/earth.preprocessor.modeldecoders.properties";

  private HashMap<ILcdModelDecoder, ModelDecoderInfo> fModelDecoderInfos;
  private final ILcdModelReference fMetadataReference;
  private List<ILcdEarthAssetCodec> fAssetCodecs;

  /**
   * Creates a new asset factory with the default model decoders.
   *
   * @param aMetadataReference the model reference of the metadata for which to create assets
   */
  public AssetFactory(ILcdModelReference aMetadataReference) {
    fMetadataReference = aMetadataReference;
    fModelDecoderInfos = new HashMap<ILcdModelDecoder, ModelDecoderInfo>();
    fAssetCodecs = new ArrayList<ILcdEarthAssetCodec>();

    try {
      Properties modelDecoderProps = new Properties();
      TLcdIOUtil ioUtil = new TLcdIOUtil();
      ioUtil.setSourceName(MODELDECODERS_SOURCE_NAME);
      String sourceName = ioUtil.getFileName() != null ? ioUtil.getFileName() : ioUtil.getURL().toString();
      modelDecoderProps.load(ioUtil.retrieveInputStream());
      if (modelDecoderProps.size() == 0) {
        sLogger.warn("No model decoders found - please check if " + MODELDECODERS_SOURCE_NAME + " is available in your classpath and is not empty.");
      }
      for (Object key : modelDecoderProps.keySet()) {
        String keyStr = String.valueOf(key);
        if (keyStr.endsWith(".class")) {
          String err = null;
          String modelDecoderName = keyStr.substring(0, keyStr.lastIndexOf('.'));
          String classStr = modelDecoderProps.getProperty(modelDecoderName + ".class");
          try {
            ILcdModelDecoder md;
            Class<ILcdModelDecoder> mdclazz = (Class<ILcdModelDecoder>) Class.forName(classStr);
            try {
              Constructor<ILcdModelDecoder> xtor = mdclazz.getConstructor(ILcdBuffer.class);
              md = xtor.newInstance(TLcdSharedBuffer.getBufferInstance());
            } catch (NoSuchMethodException e) {
              md = mdclazz.newInstance();
            }
            try {
              Method setMRD = mdclazz.getMethod("setModelReferenceDecoder",
                                                new Class[]{ILcdModelReferenceDecoder.class});
              setMRD.invoke(md, new TLcdModelReferenceDecoder());
            } catch (NoSuchMethodException e) {
              // the method does not exists
            }

            String isElevationStr = modelDecoderProps.getProperty(modelDecoderName + ".iselevation");
            if (isElevationStr == null) {
              err = "[" + modelDecoderName + ".iselevation] is undefined";
            } else {
              ILcdEarthTileSetCoverage.CoverageType coverageType =
                  Boolean.parseBoolean(isElevationStr) ? ILcdEarthTileSetCoverage.CoverageType.ELEVATION : ILcdEarthTileSetCoverage.CoverageType.IMAGE;

              String fileExtentions = modelDecoderProps.getProperty(modelDecoderName + ".extentions");
              registerModelDecoder(md, coverageType, fileExtentions);
            }
          } catch (Exception e) {
            err = e.getClass().getName() + " [" + e.getMessage() + "]";
          }
          if (err != null) {
            sLogger.info("Skipping model decoder [" + modelDecoderName + "] in [" + sourceName + "]: " + err);
          }
        }
      }
    } catch (IOException e) {
      sLogger.warn("Unable to load model decoders", this, e);
    }
  }

  public ILcdModelReference getMetadataReference() {
    return fMetadataReference;
  }

  public void addAssetCodec(ILcdEarthAssetCodec aCodec) {
    fAssetCodecs.add(aCodec);
  }

  /**
   * Registers the given model decoder.
   *
   * @param aModelDecoder The model decoder.
   * @param aCoverageType The coverage type for the models created by the given model decoder.
   */
  public void registerModelDecoder(ILcdModelDecoder aModelDecoder, ILcdEarthTileSetCoverage.CoverageType aCoverageType) {
    fModelDecoderInfos.put(aModelDecoder, new ModelDecoderInfo(aModelDecoder, aCoverageType));
  }

  /**
   * Registers the given model decoder.
   *
   * @param aModelDecoder The model decoder.
   * @param aCoverageType The coverage type for the models created by the given model decoder.
   * @param aExtentions   The extentions of the files supported by the given model decoder. Each
   *                      extention should start with a '.' and multiple extentions should be
   *                      separated by a '|'.
   */
  public void registerModelDecoder(ILcdModelDecoder aModelDecoder, ILcdEarthTileSetCoverage.CoverageType aCoverageType,
                                   String aExtentions) {
    fModelDecoderInfos.put(aModelDecoder, new ModelDecoderInfo(aModelDecoder, aCoverageType, aExtentions));
  }

  /**
   * Returns the possible model decoders for the given file or null for an unknown format.
   *
   * @param aFile A file.
   *
   * @return The possible model decoders.
   */
  public ILcdModelDecoder[] getModelDecodersFor(File aFile) {
    ArrayList<ILcdModelDecoder> possibleDecoders = new ArrayList<ILcdModelDecoder>();
    if (aFile.isFile()) {
      String sourceName = aFile.getName();

      for (ILcdModelDecoder md : fModelDecoderInfos.keySet()) {
        if (md.canDecodeSource(sourceName)) {
          possibleDecoders.add(md);
        }
      }
    }

    if (possibleDecoders.isEmpty()) {
      possibleDecoders.addAll(fModelDecoderInfos.keySet());
    }

    // Sort the decoders
    Collections.sort(possibleDecoders, new Comparator<ILcdModelDecoder>() {
      public int compare(ILcdModelDecoder md1, ILcdModelDecoder md2) {
        return md1.getDisplayName().toLowerCase().compareTo(md2.getDisplayName().toLowerCase());
      }
    });

    return possibleDecoders.toArray(new ILcdModelDecoder[possibleDecoders.size()]);
  }

  /**
   * Creates an asset from the given file.
   *
   * @param aFile        The file of the asset.
   *
   * @return The asset.
   *
   * @throws IOException              If an IO error occurs.
   * @throws TLcdNoBoundsException    If a no bounds error occurs.
   * @throws TLcdOutOfBoundsException If an out of bounds exception occurs.
   */
  public ILcdEarthAsset createAsset(File aFile) throws IOException, TLcdNoBoundsException, TLcdOutOfBoundsException {
    return createAsset(aFile, new DefaultAssetFactoryInputMethod());
  }

  /**
   * Creates an asset from the given file with a given input method.
   *
   * @param aFile        The file of the asset.
   * @param aInputMethod The input method that should be used.
   *
   * @return The asset.
   *
   * @throws IOException              If an IO error occurs.
   * @throws TLcdNoBoundsException    If a no bounds error occurs.
   * @throws TLcdOutOfBoundsException If an out of bounds exception occurs.
   */
  public ILcdEarthAsset createAsset(File aFile, AssetFactoryInputMethod aInputMethod) throws IOException, TLcdNoBoundsException, TLcdOutOfBoundsException {
    ILcdModelDecoder modelDecoder = aInputMethod.chooseModelDecoder(aFile, getModelDecodersFor(aFile));
    return createAsset(aFile, modelDecoder, aInputMethod);
  }

  /**
   * Creates an asset from the given file with a given model decoder.
   *
   * @param aFile         The file of the asset.
   * @param aModelDecoder The model decoder to use.
   *
   * @return The asset.
   *
   * @throws IOException              If an IO error occurs.
   * @throws TLcdNoBoundsException    If a no bounds error occurs.
   * @throws TLcdOutOfBoundsException If an out of bounds exception occurs.
   */
  public ILcdEarthAsset createAsset(File aFile, ILcdModelDecoder aModelDecoder) throws IOException, TLcdNoBoundsException, TLcdOutOfBoundsException {
    return createAsset(aFile, aModelDecoder, new DefaultAssetFactoryInputMethod());
  }

  /**
   * Creates an asset from the given file with a given model decoder and input method.
   *
   * @param aFile         The file of the asset.
   * @param aModelDecoder The model decoder to use.
   * @param aInputMethod  The input method to use.
   *
   * @return The asset.
   *
   * @throws IOException              If an IO error occurs.
   * @throws TLcdNoBoundsException    If a no bounds error occurs.
   * @throws TLcdOutOfBoundsException If an out of bounds exception occurs.
   */
  public ILcdEarthAsset createAsset(File aFile, ILcdModelDecoder aModelDecoder, AssetFactoryInputMethod aInputMethod) throws IOException, TLcdNoBoundsException, TLcdOutOfBoundsException {
    ModelDecoderInfo modelDecoderInfo = null;
    for (ModelDecoderInfo info : fModelDecoderInfos.values()) {
      if (info.getModelDecoder().equals(aModelDecoder)) {
        modelDecoderInfo = info;
        break;
      }
    }
    if (modelDecoderInfo == null) {
      modelDecoderInfo = new ModelDecoderInfo(aModelDecoder, ILcdEarthTileSetCoverage.CoverageType.IMAGE);
    }

    ILcdEarthAsset asset;
    if (aFile.isFile()) {
      asset = importFile(modelDecoderInfo, aFile, aInputMethod);
    } else {
      throw new IllegalArgumentException("Not a file: " + aFile);
    }

    // encode/decode to make sure the asset is the same as when loading from metadata
    for (int i = fAssetCodecs.size() - 1; i >= 0; i--) {
      ILcdEarthAssetCodec codec = fAssetCodecs.get(i);
      if (codec.canEncodeAsset(asset)) {
        return codec.decodeAsset(codec.encodeAsset(asset));
      }
    }
    throw new IllegalArgumentException("No asset codec avaialble for: " + aFile);
  }

  private ILcdEarthAsset importFile(ModelDecoderInfo aModelDecoderInfo, File aFile, AssetFactoryInputMethod aInputMethod) throws IOException, TLcdNoBoundsException, TLcdOutOfBoundsException {
    String sourceName = aFile.getPath();
    ILcdModel model = aModelDecoderInfo.getModelDecoder().decode(sourceName);
    ILcdBounds boundsModelCoords = calculateModelBounds(model, model.getModelReference());
    ILcdBounds boundsMetadataCoords = calculateModelBounds(model, getMetadataReference());
    double relativePixelDensity = calculateRelativePixelDensity(boundsModelCoords, boundsMetadataCoords);
    double pixelDensityMetadataCoords = 0;
    for (Enumeration e = model.elements(); e.hasMoreElements(); ) {
      final Object o = e.nextElement();

      ILcdRaster raster = null;
      if (o instanceof ILcdMultilevelRaster) {
        ILcdMultilevelRaster multilevelRaster = (ILcdMultilevelRaster) o;
        raster = multilevelRaster.getRaster(aInputMethod.chooseRasterLevel(aFile, model, multilevelRaster));
      } else if (o instanceof ILcdRaster) {
        raster = (ILcdRaster) o;
      }
      if (raster != null) {
        pixelDensityMetadataCoords = Math.max(pixelDensityMetadataCoords, raster.getPixelDensity() * relativePixelDensity);
      }
    }
    ILcdModelDescriptor modelDescriptor = model.getModelDescriptor();
    ILcdEarthTileSetCoverage.CoverageType coverageType;
    if (modelDescriptor instanceof TLcdRasterModelDescriptor) {
      coverageType = ((TLcdRasterModelDescriptor) modelDescriptor).isElevation() ?
                     ILcdEarthTileSetCoverage.CoverageType.ELEVATION :
                     ILcdEarthTileSetCoverage.CoverageType.IMAGE;
    } else if (modelDescriptor instanceof TLcdMultilevelRasterModelDescriptor) {
      coverageType = ((TLcdMultilevelRasterModelDescriptor) modelDescriptor).isElevation() ?
                     ILcdEarthTileSetCoverage.CoverageType.ELEVATION :
                     ILcdEarthTileSetCoverage.CoverageType.IMAGE;
    } else {
      coverageType = aModelDecoderInfo.getCoverageType();
    }
    model.dispose();

    return new TLcdEarthRasterAsset(model.getModelDescriptor().getSourceName(),
                                    aModelDecoderInfo.getModelDecoder(),
                                    boundsMetadataCoords,
                                    coverageType,
                                    new Date(),
                                    pixelDensityMetadataCoords
    );
  }

  private static ILcdBounds calculateModelBounds(ILcdModel aModel, ILcdModelReference aTargetReference) throws TLcdNoBoundsException {
    ILcdBounds modelBounds = ((ILcdBounded) aModel).getBounds();
    TLcdGeoReference2GeoReference g2g = new TLcdGeoReference2GeoReference(
        (ILcdGeoReference) aModel.getModelReference(), (ILcdGeoReference) aTargetReference
    );
    ILcd3DEditableBounds boundsSFCT = aTargetReference instanceof ILcdGeodeticReference ?
                                      new TLcdLonLatHeightBounds() : new TLcdXYZBounds();
    g2g.sourceBounds2destinationSFCT(modelBounds, boundsSFCT);
    return boundsSFCT;
  }

  // calculates the model's relative pixel density wrt the metadata model reference
  private static double calculateRelativePixelDensity(ILcdBounds aBoundsModelCoords, ILcdBounds aBoundsMetadataCoords) throws TLcdNoBoundsException, TLcdOutOfBoundsException {
    // calculate relative pixel density for metadata coordinate system
    // = modelArea / metaArea
    final double modelArea = aBoundsModelCoords.getWidth() * aBoundsModelCoords.getHeight();
    final double metaArea = aBoundsMetadataCoords.getWidth() * aBoundsMetadataCoords.getHeight();
    return modelArea / metaArea;
  }

  /**
   * Calculates the relative pixel density for a model in a reference.
   *
   * @param aModel             the model for which the relative pixel density should be computed.
   * @param aMetadataReference the reference to which the relative pixel density should transform.
   *
   * @return the relative pixel density.
   *
   * @throws TLcdNoBoundsException    If a no bounds error occurs.
   * @throws TLcdOutOfBoundsException If an out of bounds exception occurs.
   */
  public static double calculateRelativePixelDensity(ILcdModel aModel, ILcdModelReference aMetadataReference) throws TLcdNoBoundsException, TLcdOutOfBoundsException {
    ILcdBounds modelBounds = calculateModelBounds(aModel, aModel.getModelReference());
    ILcdBounds modelBoundsTarget = calculateModelBounds(aModel, aMetadataReference);
    return calculateRelativePixelDensity(modelBounds, modelBoundsTarget);
  }

  public ILcdModelDecoder[] getAllModelDecoders() {
    return fModelDecoderInfos.keySet().toArray(new ILcdModelDecoder[fModelDecoderInfos.size()]);
  }

  private static class ModelDecoderInfo {

    private ILcdModelDecoder fModelDecoder;
    private ILcdEarthTileSetCoverage.CoverageType fCoverageType;
    private String fExtentions;

    public ModelDecoderInfo(ILcdModelDecoder aModelDecoder, ILcdEarthTileSetCoverage.CoverageType aCoverageType) {
      this(aModelDecoder, aCoverageType, null);
    }

    public ModelDecoderInfo(ILcdModelDecoder aModelDecoder,
                            ILcdEarthTileSetCoverage.CoverageType aCoverageType,
                            String aExtentions) {
      fModelDecoder = aModelDecoder;
      fCoverageType = aCoverageType;
      fExtentions = aExtentions;
    }

    public ILcdModelDecoder getModelDecoder() {
      return fModelDecoder;
    }

    public ILcdEarthTileSetCoverage.CoverageType getCoverageType() {
      return fCoverageType;
    }

    public String getExtentions() {
      return fExtentions;
    }
  }

  private static class DefaultAssetFactoryInputMethod implements AssetFactoryInputMethod {
    public ILcdModelDecoder chooseModelDecoder(File aFile, ILcdModelDecoder[] aModelDecoders) {
      if (aModelDecoders.length == 0) {
        throw new IllegalArgumentException("No modeldecoders available for [" + aFile.getPath() + "]");
      }
      return aModelDecoders[0];
    }

    public int chooseRasterLevel(File aFile, ILcdModel aModel, ILcdMultilevelRaster aRaster) {
      return aRaster.getRasterCount() - 1;
    }
  }
}

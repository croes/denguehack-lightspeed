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
package samples.ogc.wcs.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.luciad.format.raster.TLcdDMEDModelDecoder;
import com.luciad.format.raster.TLcdETOPOModelDecoder;
import com.luciad.format.raster.TLcdGeoTIFFModelDecoder;
import com.luciad.format.raster.TLcdRasterModelDecoder;
import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelReference;
import com.luciad.model.TLcdSoft2DBoundsIndexedModel;
import com.luciad.ogc.wcs.TLcdWCSRequestContext;
import com.luciad.ogc.wcs.model.ILcdCoverageOffering;
import com.luciad.ogc.wcs.model.ILcdCoverageOfferingList;
import com.luciad.ogc.wcs.model.TLcdCoverageOffering;
import com.luciad.util.TLcdConcurrentBuffer;

/**
 * An example implementation of ILcdCoverageOfferingList that can serve GeoTIFF, RST, DMED
 * and ETOPO files.
 */
public class WCSCoverageProvider implements ILcdCoverageOfferingList {

  private TLcdGeoTIFFModelDecoder fGeoTIFFDecoder = new TLcdGeoTIFFModelDecoder();
  private TLcdRasterModelDecoder fRstDecoder = new TLcdRasterModelDecoder(new TLcdConcurrentBuffer());
  private TLcdDMEDModelDecoder fDMEDDecoder = new TLcdDMEDModelDecoder(new TLcdConcurrentBuffer());
  private TLcdETOPOModelDecoder fETOPOModelDecoder = new TLcdETOPOModelDecoder(new TLcdConcurrentBuffer());

  private List<TLcdCoverageOffering> fCoverages = new ArrayList<>();
  private HashMap<String, TLcdCoverageOffering> fCoverageHash = new HashMap<>();

  /**
   * Creates a new coverage for the given source data file, and adds it to the list of coverages in
   * this <code>ILcdCoverageOfferingList</code> implementation. The source data file should be in a
   * format supported by this sample implementation: GeoTIFF, RST or DMED.
   *
   * @param aSource the source data file for which to create and add a coverage
   * @param aUID    the UID to be used for the new coverage
   *
   * @throws IOException if decoding of the source data file fails
   */
  public void addCoverage(String aSource, String aUID) throws IOException {
    TLcdCoverageOffering coverageOffering = createCoverageInstance(aSource, aUID);
    addCoverage(coverageOffering);
  }

  /**
   * Adds the given coverage offering to the list of coverage offerings.
   *
   * @param aCoverageOffering a coverage offering to be added to this coverage offering list
   */
  public void addCoverage(TLcdCoverageOffering aCoverageOffering) {
    fCoverages.add(aCoverageOffering);
    fCoverageHash.put(aCoverageOffering.getName(), aCoverageOffering);
  }

  private TLcdCoverageOffering createCoverageInstance(String aSourceName, String aUID) throws IOException {
    ILcdModel model;
    if (fGeoTIFFDecoder.canDecodeSource(aSourceName)) {
      model = new TLcdSoft2DBoundsIndexedModel(aSourceName, fGeoTIFFDecoder);
    } else if (fRstDecoder.canDecodeSource(aSourceName)) {
      model = new TLcdSoft2DBoundsIndexedModel(aSourceName, fRstDecoder);
    } else if (fDMEDDecoder.canDecodeSource(aSourceName)) {
      model = new TLcdSoft2DBoundsIndexedModel(aSourceName, fDMEDDecoder);
    } else if (fETOPOModelDecoder.canDecodeSource(aSourceName)) {
      model = new TLcdSoft2DBoundsIndexedModel(aSourceName, fETOPOModelDecoder);
    } else {
      throw new IOException("No decoder for '" + aSourceName + "'");
    }

    return createCoverageInstance(model, aUID);
  }

  /**
   * Creates a new instance of TLcdCoverageOffering with the given model and name. This method is
   * protected to allow you to create a non default TLcdCoverageOffering, with a custom
   * {@link com.luciad.ogc.wcs.model.ILcdCoverageOfferingDetail} for example.
   *
   * @param aModel the model
   * @param aName the name
   * @return a new instance of TLcdCoverageOffering
   */
  protected TLcdCoverageOffering createCoverageInstance(ILcdModel aModel, String aName) {
    return new TLcdCoverageOffering(aModel, aName);
  }

  /**
   * Returns the coverage with the given name. If you have implemented ILcdWCSCoverageFilterFactory,
   * the filters you create will be passed to this method as well, allowing you to fetch a subset of
   * the coverage data.
   */
  public ILcdModel getModel(String aCoverageName, Object[] aCoverageFilter, TLcdWCSRequestContext aRequestContext) throws IOException {
    TLcdCoverageOffering coverageOffering = fCoverageHash.get(aCoverageName);
    if (coverageOffering == null) {
      throw new IOException("Coverage offering '" + aCoverageName + "' does not exist");
    }

    return coverageOffering.getModel();
  }

  /**
   * Returns the model reference of the given coverage. This naive implementation requires the whole
   * model to be loaded in order for the reference to be available. Servers dealing with large
   * datasets should implement this method so that it can return the model reference without having
   * to read the actual coverage data.
   */
  public ILcdModelReference getModelReference(String aCoverageName, TLcdWCSRequestContext aRequestContext) throws IOException {
    TLcdCoverageOffering coverageOffering = fCoverageHash.get(aCoverageName);
    if (coverageOffering == null) {
      throw new IOException("Coverage offering '" + aCoverageName + "' does not exist");
    }
    return coverageOffering.getModel().getModelReference();
  }

  public ILcdCoverageOffering getCoverageOffering(String aName, TLcdWCSRequestContext aRequestContext) {
    return fCoverageHash.get(aName);
  }

  public int getCoverageOfferingCount(TLcdWCSRequestContext aRequestContext) {
    return fCoverages.size();
  }

  public ILcdCoverageOffering getCoverageOffering(int aIndex, TLcdWCSRequestContext aRequestContext) throws IndexOutOfBoundsException {
    return fCoverages.get(aIndex);
  }
}

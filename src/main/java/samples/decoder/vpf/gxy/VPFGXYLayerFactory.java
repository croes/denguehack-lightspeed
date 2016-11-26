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
package samples.decoder.vpf.gxy;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.luciad.datamodel.TLcdDataProperty;
import com.luciad.datamodel.TLcdDataType;
import com.luciad.format.vpf.TLcdVPFFeatureClass;
import com.luciad.format.vpf.TLcdVPFGeoSymProvider;
import com.luciad.format.vpf.TLcdVPFLayerFactoryModel;
import com.luciad.format.vpf.TLcdVPFLibrary;
import com.luciad.format.vpf.TLcdVPFModelDescriptor;
import com.luciad.format.vpf.gxy.TLcdVPFGXYPainterProvider;
import samples.gxy.decoder.MapSupport;
import com.luciad.util.service.LcdService;
import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelReference;
import com.luciad.model.ILcdModelTreeNode;
import com.luciad.model.TLcdModelTreeNode;
import com.luciad.util.TLcdInterval;
import com.luciad.util.service.LcdService;
import com.luciad.view.ILcdLayer;
import com.luciad.view.gxy.ILcdGXYLayer;
import com.luciad.view.gxy.ILcdGXYLayerFactory;
import com.luciad.view.gxy.TLcdGXYLayer;
import com.luciad.view.gxy.TLcdGXYLayerTreeNode;

/**
 * An <code>ILcdGXYLayerFactory</code> for VPF models.
 */
@LcdService(service = ILcdGXYLayerFactory.class, priority = LcdService.LOW_PRIORITY)
public class VPFGXYLayerFactory implements ILcdGXYLayerFactory {

  // the resolution of the screen, in pix/m
  private static double SCREEN_RESOLUTION = 3000;

  private final TLcdVPFLayerFactoryModel fVPFLayerFactoryModel = new TLcdVPFLayerFactoryModel();

  private boolean fUseScaleRange = true;
  private boolean fHideLessRelevantLayers = true;

  private static final List<String> LESS_RELEVANT_COVERAGES;

  private static final String TILE_REF_COVERAGE = "tileref";

  static {
    LESS_RELEVANT_COVERAGES = new ArrayList<>();
    LESS_RELEVANT_COVERAGES.add("dq");
    LESS_RELEVANT_COVERAGES.add("dbref");
    LESS_RELEVANT_COVERAGES.add("libref");
  }

  private static final String REFERENCE_LIBRARY_NAME = "rference";

  /**
   * Returns <code>true</code> if scale range is used, <code>false</code> otherwise.
   */
  public boolean isUseScaleRange() {
    return fUseScaleRange;
  }

  /**
   * Sets whether scale ranges should be used.
   */
  public void setUseScaleRange(boolean aUseScaleRange) {
    fUseScaleRange = aUseScaleRange;
  }

  /**
   * Returns <code>true</code> if reference and data quality layers are hidden by default, <code>false</code> otherwise.
   */
  public boolean isHideLessRelevantLayers() {
    return fHideLessRelevantLayers;
  }

  /**
   * Sets whether reference and data quality layers should be hidden by default.
   */
  public void setHideLessRelevantLayers(boolean aHideLessRelevantLayers) {
    fHideLessRelevantLayers = aHideLessRelevantLayers;
  }

  // Implementations for ILcdGXYLayerFactory
  public ILcdGXYLayer createGXYLayer(ILcdModel aModel) {

    if (!(aModel.getModelDescriptor() instanceof TLcdVPFModelDescriptor)) {
      return null;
    }

    if (aModel instanceof ILcdModelTreeNode) {
      ILcdModelTreeNode rootModel = (TLcdModelTreeNode) aModel;
      TLcdGXYLayerTreeNode rootLayerNode = new TLcdGXYLayerTreeNode(rootModel);

      for (int i = 0; i < rootModel.modelCount(); i++) {
        ILcdModelTreeNode libraryModel = (ILcdModelTreeNode) rootModel.getModel(i);
        TLcdGXYLayerTreeNode libraryLayerNode = new TLcdGXYLayerTreeNode(libraryModel);
        rootLayerNode.addLayer(libraryLayerNode);

        for (int j = 0; j < libraryModel.modelCount(); j++) {
          ILcdModelTreeNode coverageModel = (ILcdModelTreeNode) libraryModel.getModel(j);
          TLcdGXYLayerTreeNode coverageLayerNode = new TLcdGXYLayerTreeNode(coverageModel);

          libraryLayerNode.addLayer(coverageLayerNode);

          for (int k = 0; k < coverageModel.modelCount(); k++) {
            ILcdModel featureClassModel = coverageModel.getModel(k);
            ILcdLayer featureClassLayer = createLayer(featureClassModel);
            coverageLayerNode.addLayer(featureClassLayer);
          }
        }
      }
      return rootLayerNode;
    } else {
      return createLayer(aModel);
    }
  }

  private ILcdGXYLayer createLayer(ILcdModel aModel) {
    TLcdVPFModelDescriptor descriptor = (TLcdVPFModelDescriptor) aModel.getModelDescriptor();
    TLcdVPFFeatureClass featureClass = descriptor.getVPFFeatureClass();
    TLcdVPFLibrary library = featureClass.getCoverage().getLibrary();

    String product_type = library.getProductType();
    TLcdGXYLayer vpfLayer = new TLcdGXYLayer();
    vpfLayer.setModel(aModel);

    vpfLayer.setSelectable(true);
    vpfLayer.setEditable(false);
    vpfLayer.setSelectionLabeled(true);
    vpfLayer.setLabeled(false);
    vpfLayer.setIcon(fVPFLayerFactoryModel.getGXYLayerIcon(featureClass));

    // Set a pen
    ILcdModelReference model_reference = aModel.getModelReference();
    vpfLayer.setGXYPen(MapSupport.createPen(model_reference));

    // Set label
    String label = featureClass.getDescription();
    vpfLayer.setLabel(label);

    // Set painter provider
    TLcdVPFGXYPainterProvider painter_provider = new TLcdVPFGXYPainterProvider();
    painter_provider.setVPFLayerFactoryModel(fVPFLayerFactoryModel);
    vpfLayer.setGXYPainterProvider(painter_provider);
    vpfLayer.setGXYLabelPainterProvider(painter_provider);

    // Configure default labels
    fVPFLayerFactoryModel.setLabelIndices(featureClass, getDefaultLabelIndices(descriptor));

    if (fUseScaleRange && !TILE_REF_COVERAGE.equalsIgnoreCase(featureClass.getCoverage().getName())) {
      // Set a reasonable scale range, based on scale of the library
      vpfLayer.setScaleRange(new TLcdInterval(1d / library.getScale() * 0.1 * SCREEN_RESOLUTION, Double.POSITIVE_INFINITY));
    }

    // Using a GeoSym style provider
    TLcdVPFGeoSymProvider geosymProvider = TLcdVPFGeoSymProvider.getInstance(product_type);
    painter_provider.setGeoSymProvider(geosymProvider);

    if (isHideLessRelevantLayers()) {
      //Some coverages contain less relevant data, set these invisible by default
      String coverageName = featureClass.getCoverage().getName();
      String libraryName = featureClass.getCoverage().getLibrary().getName();
      vpfLayer.setVisible(!LESS_RELEVANT_COVERAGES.contains(coverageName) && !REFERENCE_LIBRARY_NAME.equals(libraryName));
    }

    return vpfLayer;
  }

  /**
   * Returns a label index which is suitable for a wide range of VPF feature classes.
   *
   * The implementation searches for a name property; if no such label can be found, the first
   * property is used.
   *
   * @param aModelDescriptor the VPF model descriptor
   * @return a suitable label index array
   */
  private int[] getDefaultLabelIndices(TLcdVPFModelDescriptor aModelDescriptor) {
    String[] defaultLabelProperties = new String[]{"nam", "NAM", "name", "Name", "NAME"};
    Iterator<TLcdDataType> typeIterator = aModelDescriptor.getModelElementTypes().iterator();
    if (typeIterator.hasNext()) {
      TLcdDataType dataType = typeIterator.next();
      TLcdDataProperty property = null;
      for (int i = 0; i < defaultLabelProperties.length && property == null; i++) {
        property = dataType.getProperty(defaultLabelProperties[i]);
      }
      if (property != null) {
        return new int[]{property.getIndex()};
      }
    }
    return new int[]{0};
  }
}

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
package samples.lucy.search.service.data;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jdesktop.swingx.renderer.StringValue;

import samples.lucy.tableview.ValueUtil;
import com.luciad.lucy.ILcyLucyEnv;
import com.luciad.model.ILcdDataModelDescriptor;
import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelReference;
import com.luciad.model.transformation.ALcdTransformingModel;
import com.luciad.reference.ILcdGeoReference;
import com.luciad.shape.ILcdBounded;
import com.luciad.shape.ILcdBounds;
import com.luciad.util.ILcdFeatured;
import com.luciad.util.ILcdFeaturedDescriptor;
import com.luciad.view.ILcdLayer;

/**
 * {@link ModelDataSearchService.ModelDataSearchTaskFactory} implementation which searches in the
 * properties exposed by the {@link ILcdFeatured} interface.
 */
public final class FeaturedSearchTaskFactory implements ModelDataSearchService.ModelDataSearchTaskFactory {

  private final ILcyLucyEnv fLucyEnv;

  public FeaturedSearchTaskFactory(ILcyLucyEnv aLucyEnv) {
    fLucyEnv = aLucyEnv;
  }

  @Override
  public ModelDataSearchTask createSearchTask(ModelDataSearchService aModelDataSearchService, ILcdModel aModel, ILcdLayer aLayer) {
    aModel = unpackModel(aModel);
    if (aModel.getModelDescriptor() instanceof ILcdFeaturedDescriptor &&
        !(aModel.getModelDescriptor() instanceof ILcdDataModelDescriptor)) {
      return new FeaturedSearchTask(aModel, aLayer, fLucyEnv, aModelDataSearchService);
    }
    return null;
  }

  private static ILcdModel unpackModel(ILcdModel aModel) {
    if (aModel instanceof ALcdTransformingModel) {
      return unpackModel(((ALcdTransformingModel) aModel).getOriginalModel());
    }
    return aModel;
  }

  private static final class FeaturedSearchTask extends ModelDataSearchTask {

    private FeaturedSearchTask(ILcdModel aModel, ILcdLayer aLayer, ILcyLucyEnv aLucyEnv, ModelDataSearchService aSearchService) {
      super(aSearchService, aModel, aLayer, aLucyEnv);
    }

    @Override
    protected void searchDomainObject(Pattern aSearchPattern, Object aDomainObject) {
      if (!(aDomainObject instanceof ILcdFeatured)) {
        return;
      }
      ILcdFeaturedDescriptor modelDescriptor = (ILcdFeaturedDescriptor) getModel().getModelDescriptor();
      StringValue compositeStringValue = ValueUtil.createCompositeStringValue(getLucyEnv(), getModel().getModelReference());

      ILcdFeatured element = (ILcdFeatured) aDomainObject;
      for (int i = 0; i < element.getFeatureCount(); i++) {
        Object value = element.getFeature(i);
        String featureName = modelDescriptor.getFeatureName(i);
        if (matchesSearchPattern(featureName, aSearchPattern)) {
          publishResult(aDomainObject, new String[]{featureName}, value, aSearchPattern);
          continue;
        }

        String featureValueAsRenderedInTableViewAndObjectPropertiesPanel = compositeStringValue.getString(value);
        if (matchesSearchPattern(featureValueAsRenderedInTableViewAndObjectPropertiesPanel, aSearchPattern)) {
          publishResult(aDomainObject, new String[]{featureName}, value, aSearchPattern);
        }
      }
    }

    private void publishResult(Object aDomainObject, String[] aPropertyPath, Object aValue, Pattern aSearchPattern) {
      if (!(aDomainObject instanceof ILcdBounded)) {
        return;
      }

      ILcdBounds bounds = ((ILcdBounded) aDomainObject).getBounds();
      ILcdModelReference modelReference = getModel().getModelReference();

      publishSearchResult(aDomainObject, aPropertyPath, aValue, bounds, (ILcdGeoReference) modelReference, aSearchPattern);
    }

    private static boolean matchesSearchPattern(String aToCheck, Pattern aSearchPattern) {
      if (aToCheck == null) {
        return false;
      }
      Matcher matcher = aSearchPattern.matcher(aToCheck);
      return matcher.find();
    }
  }
}

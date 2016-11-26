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
package samples.lucy.fundamentals.waypoints.model;

import com.luciad.lucy.ILcyLucyEnv;
import com.luciad.lucy.format.ALcyFileFormat;
import com.luciad.lucy.gui.customizer.ILcyCustomizerPanelFactory;
import com.luciad.lucy.map.ILcyGXYLayerTypeProvider;
import com.luciad.lucy.model.ALcyDefaultModelDescriptorFactory;
import com.luciad.lucy.model.ILcyModelContentType;
import com.luciad.lucy.model.ILcyModelContentTypeProvider;
import com.luciad.lucy.util.properties.ALcyProperties;
import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelDecoder;
import com.luciad.model.ILcdModelDescriptor;
import com.luciad.model.ILcdModelEncoder;
import com.luciad.model.ILcdModelFactory;
import com.luciad.model.ILcdModelReference;
import com.luciad.model.TLcd2DBoundsIndexedModel;
import com.luciad.util.ILcdFilter;
import com.luciad.view.gxy.ILcdGXYLayerFactory;

import samples.gxy.fundamentals.step3.WayPointModelDecoder;
import samples.lucy.fundamentals.waypoints.WayPointsModelFilter;

/**
 * <p>
 *   {@code ALcyFileFormat} extension which adds support for waypoint models.
 *   As the waypoints can be edited, this format registers a model encoder as well.
 * </p>
 *
 * <p>
 *   This format only provides the model-related functionality.
 *   We do not provide any GXY layer functionality, so all layer-related functionality remains
 *   unimplemented.
 *   We only support Lightspeed visualization for these models, which is provided by the
 *   {@link samples.lucy.fundamentals.waypoints.WayPointsAddOn}.
 * </p>
 */
public class WayPointsModelFormat extends ALcyFileFormat {
  private final ILcdFilter<ILcdModel> fModelFilter = new WayPointsModelFilter();

  public WayPointsModelFormat(ILcyLucyEnv aLucyEnv,
                              String aLongPrefix,
                              String aShortPrefix,
                              ALcyProperties aProperties) {
    super(aLucyEnv, aLongPrefix, aShortPrefix, aProperties);
  }

  @Override
  public boolean isModelOfFormat(ILcdModel aModel) {
    return fModelFilter.accept(aModel);
  }

  @Override
  protected ILcyModelContentTypeProvider createModelContentTypeProvider() {
    return new ILcyModelContentTypeProvider() {
      @Override
      public int getModelContentType(ILcdModel aModel) {
        return ILcyModelContentType.POINT;
      }
    };
  }

  @Override
  protected ILcdModelDecoder[] createModelDecoders() {
    return new ILcdModelDecoder[]{
        new WayPointModelDecoder()
    };
  }

  @Override
  protected ILcdModelEncoder[] createModelEncoders() {
    return new ILcdModelEncoder[]{
        new WayPointModelEncoder()
    };
  }

  @Override
  protected ILcdModelFactory createModelFactory() {
    return new ILcdModelFactory() {

      @Override
      public ILcdModel createModel(ILcdModelDescriptor aModelDescriptor, ILcdModelReference aModelReference) throws IllegalArgumentException {
        if (!WayPointsModelFilter.isWayPointsModelDescriptor(aModelDescriptor)) {
          throw new IllegalArgumentException("Cannot create model for model descriptor [" + aModelDescriptor + "]");
        }
        TLcd2DBoundsIndexedModel model = WayPointModelDecoder.createEmptyModel();
        model.setModelDescriptor(aModelDescriptor);
        return model;
      }
    };
  }

  @Override
  protected ALcyDefaultModelDescriptorFactory[] createDefaultModelDescriptorFactories() {
    return new ALcyDefaultModelDescriptorFactory[]{
        new ALcyDefaultModelDescriptorFactory() {
          @Override
          public String getDisplayName() {
            return "Way Point";
          }

          @Override
          public ILcdModelDescriptor createDefaultModelDescriptor() {
            return WayPointModelDecoder.createModelDescriptor(null);
          }
        }
    };
  }

  @Override
  protected ILcyGXYLayerTypeProvider createGXYLayerTypeProvider() {
    return null;
  }

  @Override
  protected ILcdGXYLayerFactory createGXYLayerFactory() {
    return null;
  }

  @Override
  protected ILcyCustomizerPanelFactory[] createDomainObjectCustomizerPanelFactories() {
    return new ILcyCustomizerPanelFactory[]{
        new WayPointCustomizerPanelFactory(getLucyEnv())
    };
  }
}

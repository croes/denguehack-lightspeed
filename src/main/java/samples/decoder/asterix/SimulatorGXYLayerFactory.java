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
package samples.decoder.asterix;

import java.awt.Color;
import java.util.List;
import java.util.Set;

import com.luciad.datamodel.TLcdDataProperty;
import com.luciad.datamodel.TLcdDataType;
import com.luciad.format.asterix.TLcdASTERIXTrackModelDescriptor;
import samples.common.gxy.GXYScaleSupport;
import samples.decoder.asterix.TrackGXYPainter;
import samples.decoder.asterix.TrackSimulationModelDescriptor;
import com.luciad.model.ILcdDataModelDescriptor;
import com.luciad.model.ILcdModel;
import com.luciad.util.ILcdFilter;
import com.luciad.util.TLcdInterval;
import com.luciad.util.service.LcdService;
import com.luciad.view.gxy.ILcdGXYLayer;
import com.luciad.view.gxy.ILcdGXYLayerFactory;
import com.luciad.view.gxy.TLcdGXYDataObjectLabelPainter;
import com.luciad.view.gxy.TLcdGXYLayer;

import samples.gxy.decoder.MapSupport;
import samples.realtime.common.LockedGXYLayer;

/**
 * ILcdGXYLayerFactory that can create ILcdGXYLayers for the real-time track models.
 */
@LcdService(service = ILcdGXYLayerFactory.class)
public class SimulatorGXYLayerFactory implements ILcdGXYLayerFactory, ILcdFilter {

  @Override
  public boolean accept(Object aObject) {
    if (aObject instanceof ILcdModel) {
      ILcdModel model = (ILcdModel) aObject;
      return model.getModelDescriptor() instanceof TrackSimulationModelDescriptor ||
             model.getModelDescriptor() instanceof TLcdASTERIXTrackModelDescriptor;
    }
    return false;
  }

  @Override
  public ILcdGXYLayer createGXYLayer(ILcdModel aModel) {
    if (accept(aModel)) {
      return createTrackLayer(aModel);
    }
    return null;
  }

  private ILcdGXYLayer createTrackLayer(ILcdModel aModel) {
    TLcdGXYLayer layer = new LockedGXYLayer();
    layer.setModel(aModel);
    layer.setLabel(aModel.getModelDescriptor().getDisplayName());
    layer.setLabeled(true);
    layer.setLabelsEditable(true);
    layer.setGXYPen(MapSupport.createPen(aModel.getModelReference()));

    layer.setGXYPainterProvider(new TrackGXYPainter());

    //Don't paint the labels when zoomed out (small scale)
    layer.setLabelScaleRange(new TLcdInterval(
        GXYScaleSupport.mapScale2InternalScale(1.0 / 5000000.0, -1, null), Double.MAX_VALUE));

    TLcdGXYDataObjectLabelPainter labelPainter = new TLcdGXYDataObjectLabelPainter();
    labelPainter.setWithPin(true);
    labelPainter.setFrame(true);
    labelPainter.setFilled(true);
    labelPainter.setBackground(new Color(255, 255, 255, 128));
    labelPainter.setExpressions(findLabelProperty(aModel));
    layer.setGXYLabelPainterProvider(labelPainter);
    layer.setGXYLabelEditorProvider(labelPainter);

    return layer;
  }

  public static String[] findLabelProperty(ILcdModel aModel) {
    ILcdDataModelDescriptor modelDescriptor = (ILcdDataModelDescriptor) aModel.getModelDescriptor();
    Set<TLcdDataType> dataTypeSet = modelDescriptor.getModelElementTypes();
    // Find a property that has 'track' and 'number' in its name.
    for (TLcdDataType dataType : dataTypeSet) {
      List<TLcdDataProperty> dataProperties = dataType.getProperties();
      for (TLcdDataProperty dataProperty : dataProperties) {
        if (dataProperty.getName().contains("Track") &&
            dataProperty.getName().contains("Number")) {
          return new String[]{dataProperty.getName()};
        }
      }

    }
    //If no property could be found, return the first property of the first type
    if (modelDescriptor.getModelElementTypes().size() > 0) {
      return new String[]{modelDescriptor.getModelElementTypes().iterator().next().getProperties().get(0).getName()};
    } else {
      return null;
    }
  }
}

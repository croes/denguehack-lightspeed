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
package samples.gxy.decoder.mif;

import java.awt.Color;
import java.awt.Font;

import com.luciad.datamodel.TLcdDataProperty;
import com.luciad.datamodel.TLcdDataType;
import com.luciad.format.mif.TLcdMIFModelDescriptor;
import com.luciad.format.mif.gxy.TLcdMIFGXYPainterProvider;
import com.luciad.model.ILcdDataModelDescriptor;
import com.luciad.model.ILcdModel;
import com.luciad.util.service.LcdService;
import com.luciad.view.gxy.ILcdGXYLayer;
import com.luciad.view.gxy.ILcdGXYLayerFactory;
import com.luciad.view.gxy.TLcdGXYDataObjectLabelPainter;
import com.luciad.view.gxy.TLcdGXYDataObjectPolylineLabelPainter;
import com.luciad.view.gxy.TLcdGXYLayer;

import samples.gxy.decoder.MapSupport;

/**
 * This is an example of ILcdGXYLayerFactory for a MIF ILcdGXYLayer. It creates and
 * sets up a TLcdGXYLayer for displaying Objects contained in an ILcdModel created
 * by a TLcdMIFModelDecoder. It considers only MIF REGIONs, LINEs, PLINEs and POINTs.
 */
@LcdService
public class MIFLayerFactory
    implements ILcdGXYLayerFactory {

  @Override
  public ILcdGXYLayer createGXYLayer(ILcdModel aModel) {

    if (!(aModel.getModelDescriptor() instanceof TLcdMIFModelDescriptor)) {
      return null;
    }

    TLcdMIFModelDescriptor mif_data_descriptor = (TLcdMIFModelDescriptor) aModel.getModelDescriptor();

    TLcdGXYLayer layer = new TLcdGXYLayer(aModel);
    layer.setSelectable(true);
    layer.setEditable(false);
    layer.setLabeled(false);
    layer.setVisible(true);

    // Set a suitable pen on the layer.
    layer.setGXYPen(MapSupport.createPen(aModel.getModelReference()));

    layer.setGXYPainterProvider(new TLcdMIFGXYPainterProvider(mif_data_descriptor, true));

    Font font = new Font("Lucida Sans", Font.PLAIN, 11);
    if ((mif_data_descriptor.getMIFGeometryTypes().length == 1) &&
        (mif_data_descriptor.getMIFGeometryTypes()[0] == TLcdMIFModelDescriptor.PLINE)) {
      TLcdGXYDataObjectPolylineLabelPainter labelPainter = new TLcdGXYDataObjectPolylineLabelPainter();
      labelPainter.setExpressions(getExpressions(mif_data_descriptor));
      labelPainter.setForeground(Color.WHITE);
      labelPainter.setHaloColor(Color.BLACK);
      labelPainter.setHaloEnabled(true);
      labelPainter.setFont(font);
      layer.setGXYLabelPainterProvider(labelPainter);
    } else {
      TLcdGXYDataObjectLabelPainter labelPainter = new TLcdGXYDataObjectLabelPainter();
      labelPainter.setExpressions(getExpressions(mif_data_descriptor));
      labelPainter.setForeground(Color.WHITE);
      labelPainter.setHaloColor(Color.BLACK);
      labelPainter.setHaloEnabled(true);
      labelPainter.setFont(font);
      layer.setGXYLabelPainterProvider(labelPainter);
    }
    return layer;
  }

  // Display the first string property as label.
  private String[] getExpressions(ILcdDataModelDescriptor aDescriptor) {
    TLcdDataType featureType = aDescriptor.getModelElementTypes().iterator().next();
    for (TLcdDataProperty p : featureType.getProperties()) {
      if (p.getType().getInstanceClass() == String.class) {
        return new String[]{p.getName()};
      }
    }
    return null;
  }

}

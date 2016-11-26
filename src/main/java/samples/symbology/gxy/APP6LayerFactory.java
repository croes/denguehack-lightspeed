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
package samples.symbology.gxy;

import java.awt.Color;
import java.beans.PropertyChangeListener;

import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelDescriptor;
import com.luciad.symbology.app6a.model.ILcdAPP6ACoded;
import com.luciad.symbology.app6a.model.TLcdAPP6AModelDescriptor;
import com.luciad.symbology.app6a.view.gxy.painter.TLcdAPP6AGXYEditorProvider;
import com.luciad.symbology.app6a.view.gxy.painter.TLcdAPP6AGXYLabelPainterProvider;
import com.luciad.symbology.app6a.view.gxy.painter.TLcdAPP6AGXYPainterProvider;
import com.luciad.util.ILcdInterval;
import com.luciad.view.gxy.ILcdGXYLayer;
import com.luciad.view.gxy.ILcdGXYLayerFactory;
import com.luciad.view.gxy.TLcdGXYLayer;

import samples.gxy.decoder.MapSupport;

/**
 * A layer factory for models containing APP-6 objects.
 */
public class APP6LayerFactory implements ILcdGXYLayerFactory {

  private final ILcdInterval fLabelScaleRange;

  private final PropertyChangeListener fPropertyChangeListener;

  public APP6LayerFactory() {
    this(null, null);
  }

  public APP6LayerFactory(PropertyChangeListener aPropertyChangeListener) {
    this(null, aPropertyChangeListener);
  }

  public APP6LayerFactory(ILcdInterval aLabelScaleRange, PropertyChangeListener aPropertyChangeListener) {
    fLabelScaleRange = aLabelScaleRange;
    fPropertyChangeListener = aPropertyChangeListener;
  }

  /**
   * Creates a gxy layer for models that contain APP-6 objects. The supplied model must have a model
   * descriptor of type <code>TLcdAPP6AModelDescriptor</code> and a model reference of type
   * <code>ILcdGeodeticReference</code>.
   */
  public ILcdGXYLayer createGXYLayer(ILcdModel model) {

    ILcdModelDescriptor modelDescriptor = model.getModelDescriptor();
    if (!(modelDescriptor instanceof TLcdAPP6AModelDescriptor)) {
      return null;
    }

    // Create a standard layer
    TLcdGXYLayer layer = new TLcdGXYLayer(model);

    // Create a painter provider
    TLcdAPP6AGXYPainterProvider painterProvider = new TLcdAPP6AGXYPainterProvider();

    // We set some default style settings: this style is used when
    // the symbol object doesn't have a style already (by implementing ILcdAPP6AStyled)
    // and if it is applicable for the particular symbol

    //the frame, fill and icon setting determine the visualisation of a symbol icon;
    //see the "Display Option Hierarchy" in the APP-6A standard.
    painterProvider.getDefaultStyle().setSymbolFrameEnabled(true); //this is the default setting
    painterProvider.getDefaultStyle().setSymbolFillEnabled(true); //this is the default setting
    painterProvider.getDefaultStyle().setSymbolIconEnabled(true); //this is the default setting
    painterProvider.getDefaultStyle().setAffiliationColorEnabled(true);

    // default color for line symbols and unframed icon symbols
    painterProvider.getDefaultStyle().setColor(Color.black);

    //<< Performance enhancement: image caching.
    // One important note: the model listener should be removed when the layer/model is removed.
//    APP6ImageObjectIconProvider iconProvider = new APP6ImageObjectIconProvider();
//    iconProvider.setDefaultStyle(painterProvider.getDefaultStyle());
//    painterProvider.setIconProvider(iconProvider);
//    model.addModelListener(iconProvider);
    //>>

    // Register the painter/editor provider on the layer
    layer.setGXYPainterProvider(painterProvider);
    layer.setGXYEditorProvider(new TLcdAPP6AGXYEditorProvider());

    TLcdAPP6AGXYLabelPainterProvider labelPainterProvider = new TLcdAPP6AGXYLabelPainterProvider();

    // We set some default style settings: this style is used when
    // the symbol object doesn't have a style already (by implementing ILcdAPP6AStyled)
    // and if it is applicable for the particular symbol

    //labels can be enabled/disabled on the view; all labels are enabled by default
    labelPainterProvider.getDefaultStyle().setLabelEnabled(ILcdAPP6ACoded.sLocationLabel, false); //true is the default setting
    labelPainterProvider.getDefaultStyle().setLabelEnabled(ILcdAPP6ACoded.sEffectiveTime, false); //true is the default setting

    // Register the label painter provider on the layer
    layer.setGXYLabelPainterProvider(labelPainterProvider);

    // Layer properties
    layer.setGXYPen(MapSupport.createPen(model.getModelReference()));
    layer.setSelectable(true);
    layer.setEditable(true);
    layer.setLabeled(true);
    if (fLabelScaleRange != null) {
      layer.setLabelScaleRange(fLabelScaleRange);
    }
    if (fPropertyChangeListener != null) {
      layer.addPropertyChangeListener(fPropertyChangeListener);
    }

    return layer;
  }
}

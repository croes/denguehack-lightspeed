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
import com.luciad.symbology.milstd2525b.model.ILcdMS2525bCoded;
import com.luciad.symbology.milstd2525b.model.TLcdMS2525bModelDescriptor;
import com.luciad.symbology.milstd2525b.view.gxy.painter.TLcdMS2525bGXYEditorProvider;
import com.luciad.symbology.milstd2525b.view.gxy.painter.TLcdMS2525bGXYLabelPainterProvider;
import com.luciad.symbology.milstd2525b.view.gxy.painter.TLcdMS2525bGXYPainterProvider;
import com.luciad.util.ILcdInterval;
import com.luciad.view.gxy.ILcdGXYLayer;
import com.luciad.view.gxy.ILcdGXYLayerFactory;
import com.luciad.view.gxy.TLcdGXYLayer;

import samples.gxy.decoder.MapSupport;

/**
 * A layer factory for models containing MS2525 objects.
 */
public class MS2525LayerFactory implements ILcdGXYLayerFactory {

  private final ILcdInterval fLabelScaleRange;
  private final PropertyChangeListener fPropertyChangeListener;

  public MS2525LayerFactory() {
    this(null, null);
  }

  public MS2525LayerFactory(PropertyChangeListener aPropertyChangeListener) {
    this(null, aPropertyChangeListener);
  }

  public MS2525LayerFactory(ILcdInterval aLabelScaleRange, PropertyChangeListener aPropertyChangeListener) {
    fLabelScaleRange = aLabelScaleRange;
    fPropertyChangeListener = aPropertyChangeListener;
  }

  /**
   * Creates a gxy layer for models that contain MS2525 objects. The supplied model must have a model
   * descriptor of type <code>TLcdMS2525bModelDescriptor</code> and a model reference of type
   * <code>ILcdGeodeticReference</code>.
   */
  public ILcdGXYLayer createGXYLayer(ILcdModel model) {

    ILcdModelDescriptor modelDescriptor = model.getModelDescriptor();
    if (!(modelDescriptor instanceof TLcdMS2525bModelDescriptor)) {
      return null;
    }

    // Create a standard layer
    TLcdGXYLayer layer = new TLcdGXYLayer(model);

    // Create a painter provider
    TLcdMS2525bGXYPainterProvider painterProvider = new TLcdMS2525bGXYPainterProvider();

    // We set some default style settings: this style is used when
    // the symbol object doesn't have a style already (by implementing ILcdMS2525bStyled)
    // and if it is applicable for the particular symbol

    //the frame, fill and icon setting determine the visualisation of a symbol icon;
    //see the "Display Option Hierarchy" in the MS2525b standard.
    painterProvider.getDefaultStyle().setSymbolFrameEnabled(true); //this is the default setting
    painterProvider.getDefaultStyle().setSymbolFillEnabled(true); //this is the default setting
    painterProvider.getDefaultStyle().setSymbolIconEnabled(true); //this is the default setting
    painterProvider.getDefaultStyle().setAffiliationColorEnabled(true);

    // default color for line symbols and unframed icon symbols
    painterProvider.getDefaultStyle().setColor(Color.black);

    //<< Performance enhancement: image caching.
    // One important note: the model listener should be removed when the layer/model is removed.
//    MS2525ImageObjectIconProvider iconProvider = new MS2525ImageObjectIconProvider();
//    iconProvider.setDefaultStyle(painterProvider.getDefaultStyle());
//    painterProvider.setIconProvider(iconProvider);
//    model.addModelListener(iconProvider);
    //>>

    // Register the painter/editor provider on the layer
    layer.setGXYPainterProvider(painterProvider);
    layer.setGXYEditorProvider(new TLcdMS2525bGXYEditorProvider());

    TLcdMS2525bGXYLabelPainterProvider labelPainterProvider = new TLcdMS2525bGXYLabelPainterProvider();

    // We set some default style settings: this style is used when
    // the symbol object doesn't have a style already (by implementing ILcdMS2525bStyled)
    // and if it is applicable for the particular symbol

    //labels can be enabled/disabled on the view; all labels are enabled by default
    labelPainterProvider.getDefaultStyle().setLabelEnabled(ILcdMS2525bCoded.sLocationLabel, false); //true is the default setting
    labelPainterProvider.getDefaultStyle().setLabelEnabled(ILcdMS2525bCoded.sEffectiveTime, false); //true is the default setting

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

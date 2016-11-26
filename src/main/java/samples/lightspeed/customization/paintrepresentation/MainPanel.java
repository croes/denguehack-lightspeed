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
package samples.lightspeed.customization.paintrepresentation;

import com.luciad.model.ILcdModel;
import com.luciad.view.lightspeed.ILspAWTView;
import com.luciad.view.lightspeed.layer.ILspInteractivePaintableLayer;

import samples.lightspeed.common.LspDataUtil;
import samples.lightspeed.editing.ModelFactory;

/**
 * This sample defines a new "BOUNDS_BODY" paint representation alongside the
 * default "BODY" and "LABEL" paint representation, and uses a simple shape
 * painter for the visualization. The main advantage of custom paint
 * representations is that they allow you to paint the same objects with two
 * entirely different painters.
 * <p/>
 * This sample is an adaptation of the Editing sample (which illustrates the
 * editing and creation capabilities of the editors that are available in the
 * Lightspeed API).
 * <p/>
 * The shapes layer has a default painter to paint the shapes' body, and a
 * regular shape painter (<code>TLspShapePainter</code>) to paint the shapes'
 * bounds. Hence, each shape is painted twice: once using the default painter
 * and once using the shape painter.
 */
public class MainPanel extends samples.lightspeed.editing.MainPanel {

  @Override
  // Overridden to use a layer factory that adds our custom paint representation.
  protected ILspInteractivePaintableLayer createAndAddEditableShapesLayer(ILspAWTView aView) {
    ILcdModel shapesModel = new ModelFactory().createShapesModel();
    return (ILspInteractivePaintableLayer) LspDataUtil.instance()
                                                      .model(shapesModel)
                                                      .layer(new LayerFactoryWithBoundsRepresentation())
                                                      .label("Editable shapes")
                                                      .addToView(aView)
                                                      .getLayer();
  }

  public static void main(final String[] aArgs) {
    startSample(MainPanel.class, "Custom paint representation");
  }

}

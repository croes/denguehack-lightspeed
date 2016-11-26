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
package samples.gxy.editing.actions;

import java.awt.event.ActionEvent;
import java.util.List;

import com.luciad.gui.TLcdActionAtLocationEvent;
import com.luciad.shape.ILcdCurve;
import com.luciad.shape.ILcdEditableCompositeCurve;
import com.luciad.util.ILcdFilter;
import com.luciad.view.ALcdObjectSelectionAction;
import com.luciad.view.TLcdDomainObjectContext;
import com.luciad.view.gxy.ILcdGXYContext;
import com.luciad.view.gxy.ILcdGXYController;
import com.luciad.view.gxy.ILcdGXYLayer;
import com.luciad.view.gxy.ILcdGXYLayerSubsetList;
import com.luciad.view.gxy.ILcdGXYPainter;
import com.luciad.view.gxy.ILcdGXYView;
import com.luciad.view.gxy.TLcdGXYContext;
import com.luciad.view.gxy.TLcdGXYSetControllerAction;
import com.luciad.view.gxy.controller.TLcdGXYNewController2;

import samples.gxy.common.controller.ControllerUtil;
import samples.gxy.editing.ShapeGXYLayerFactory;
import samples.gxy.editing.controllers.ControllerSettingsNotifier;
import samples.gxy.editing.controllers.InsertCompositeCurveControllerModel;
import samples.gxy.editing.controllers.NewShapeControllerModel;

/**
 * Inserts a curve into a composite curve, right after the segment located at the clicked point.
 */
public class InsertCurveAction extends ALcdObjectSelectionAction {

  private ILcdGXYController fControllerAfterAction;
  private ControllerSettingsNotifier fNotifier;
  private ILcdGXYLayerSubsetList fSnappables;

  public InsertCurveAction(ILcdGXYController aControllerAfterAction,
                           ControllerSettingsNotifier aNotifier,
                           ILcdGXYLayerSubsetList aSnappables,
                           ILcdGXYView aView) {
    super(aView,
          new ILcdFilter<TLcdDomainObjectContext>() {
            @Override
            public boolean accept(TLcdDomainObjectContext aObject) {
              return aObject.getDomainObject() instanceof ILcdEditableCompositeCurve;
            }
          });
    setName("Insert sub-curve");
    fControllerAfterAction = aControllerAfterAction;
    fNotifier = aNotifier;
    fSnappables = aSnappables;
  }

  @Override
  protected void actionPerformed(ActionEvent aActionEvent, List<TLcdDomainObjectContext> aSelection) {
    TLcdDomainObjectContext domainObjectContext = aSelection.get(0);
    ILcdEditableCompositeCurve compositeCurve = (ILcdEditableCompositeCurve) domainObjectContext.getDomainObject();
    ILcdGXYLayer layer = (ILcdGXYLayer) domainObjectContext.getLayer();

    // Retrieves the touched segment.

    if (!(aActionEvent instanceof TLcdActionAtLocationEvent)) {
      throw new IllegalArgumentException("This action needs a location to operate");
    }
    TLcdActionAtLocationEvent event = (TLcdActionAtLocationEvent) aActionEvent;
    ILcdGXYView view = (ILcdGXYView) getView();
    TLcdGXYContext context = new TLcdGXYContext(view, layer);
    context.setX(event.getLocation().x);
    context.setY(event.getLocation().y);
    ILcdCurve touchedSegment = retrieveTouchedSegment(compositeCurve, context);
    if (touchedSegment == null) {
      return;
    }

    // Sets up a controller that lets the user draw a new sub-curve.

    InsertCompositeCurveControllerModel controllerModel = new InsertCompositeCurveControllerModel(
        NewShapeControllerModel.ShapeType.COMPOSITE_CURVE, layer, fNotifier
    );
    controllerModel.init(compositeCurve.getCurves().indexOf(touchedSegment) + 1, compositeCurve, compositeCurve);

    TLcdGXYNewController2 controller = new TLcdGXYNewController2(controllerModel);
    controller.setActionToTriggerAfterCommit(new TLcdGXYSetControllerAction(view, fControllerAfterAction));
    controllerModel.setSubCurveType(fNotifier.getCurveType(), null, layer);
    controller.setSnappables(fSnappables);
    fNotifier.addListener(controllerModel);
    view.setGXYController(ControllerUtil.wrapWithZoomAndPan(controller));
  }

  public static ILcdCurve retrieveTouchedSegment(ILcdEditableCompositeCurve aCompositeCurve, ILcdGXYContext aContext) {
    for (ILcdCurve curve : aCompositeCurve.getCurves()) {
      ILcdGXYPainter painter = ShapeGXYLayerFactory.retrieveGXYPainterEditor(aContext.getGXYLayer()).getGXYPainter(curve);
      int mode = ILcdGXYPainter.BODY | ILcdGXYPainter.SELECTED;
      if (painter.isTouched(aContext.getGXYView().getGraphics(), mode, aContext)) {
        return curve;
      }
    }
    return null;
  }
}

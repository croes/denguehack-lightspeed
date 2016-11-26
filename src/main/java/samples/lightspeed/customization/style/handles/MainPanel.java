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
package samples.lightspeed.customization.style.handles;

import static com.luciad.view.lightspeed.style.complexstroke.ALspComplexStroke.*;

import java.awt.Color;
import java.io.IOException;

import com.luciad.view.lightspeed.ILspAWTView;
import com.luciad.view.lightspeed.controller.ILspController;
import com.luciad.view.lightspeed.controller.manipulation.TLspEditController;
import com.luciad.view.lightspeed.controller.manipulation.TLspEditHandleStyler;
import com.luciad.view.lightspeed.editor.TLspHandleGeometryType;
import com.luciad.view.lightspeed.style.TLspComplexStrokedLineStyle;
import com.luciad.view.lightspeed.style.complexstroke.ALspComplexStroke;

import samples.lightspeed.common.LightspeedSample;
import samples.lightspeed.common.CreateAndEditToolBar;
import samples.lightspeed.common.LspDataUtil;
import samples.lightspeed.common.ToolBar;

/**
 * This sample illustrates how to customize the visualization of editing handles.
 */
public class MainPanel extends LightspeedSample {

  private CreateAndEditToolBar fCreateAndEditToolBar;

  @Override
  protected ToolBar[] createToolBars(ILspAWTView aView) {
    ToolBar toolBar = new ToolBar(aView, this, true, true) {
      @Override
      protected ILspController createDefaultController() {
        ILspController controller = super.createDefaultController();
        TLspEditController editController = findEditController(controller);
        if (editController != null) {
          customizeHandleVisualization(editController);
        }
        return controller;
      }

      private TLspEditController findEditController(ILspController aController) {
        ILspController candidateEditController = aController;
        while (candidateEditController != null &&
               !(candidateEditController instanceof TLspEditController)) {
          candidateEditController = candidateEditController.getNextController();
        }
        return (TLspEditController) candidateEditController;
      }

      private void customizeHandleVisualization(TLspEditController aEditController) {
        TLspEditHandleStyler handleStyler = new TLspEditHandleStyler();

        TLspComplexStrokedLineStyle strokedLineStyle = createStrokedLineStyle(2, Color.yellow);
        handleStyler.setStyles(TLspHandleGeometryType.REGULAR_LINE, strokedLineStyle);

        strokedLineStyle = createStrokedLineStyle(2, Color.green);
        handleStyler.setStyles(TLspHandleGeometryType.VISUAL_AID_LINE, strokedLineStyle);

        aEditController.setHandleStyler(handleStyler);
        aEditController.setFocusHandleStyler(handleStyler);
        aEditController.setHandleLabelStyler(new LineHandleLabelStyler(Color.white, Color.black));
        aEditController.setFocusHandleLabelStyler(new LineHandleLabelStyler(Color.yellow, Color.black));
      }

      private TLspComplexStrokedLineStyle createStrokedLineStyle(int aLineWidth, Color aColor) {
        ALspComplexStroke parallelLine = parallelLine().length(5).lineWidth(aLineWidth).lineColor(aColor).build();
        return TLspComplexStrokedLineStyle.newBuilder()
                                          .fallback(append(parallelLine, gap(5)))
                                          .build();
      }
    };
    if (fCreateAndEditToolBar == null) {
      fCreateAndEditToolBar = new CreateAndEditToolBar(aView, this,
                                                       toolBar.getButtonGroup(),
                                                       false, true, false);
    }
    return new ToolBar[]{toolBar, fCreateAndEditToolBar};
  }

  protected void addData() throws IOException {
    super.addData();
    LspDataUtil.instance().model(ModelFactory.createEllipseModel()).layer().label("Ellipses").editable(true).addToView(getView()).fit();
  }

  public static void main(final String[] aArgs) {
    startSample(MainPanel.class, "Editing - handle styling");
  }

}

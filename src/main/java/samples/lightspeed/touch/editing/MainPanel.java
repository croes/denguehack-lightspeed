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
package samples.lightspeed.touch.editing;

import javax.swing.JToolBar;

import com.luciad.gui.TLcdIconFactory;
import com.luciad.model.ILcdModel;
import com.luciad.view.lightspeed.ILspAWTView;
import com.luciad.view.lightspeed.controller.ILspController;
import com.luciad.view.lightspeed.layer.ILspInteractivePaintableLayer;

import samples.gxy.common.touch.TouchUtil;
import samples.lightspeed.common.CreateAndEditToolBar;
import samples.lightspeed.common.LightspeedSample;
import samples.lightspeed.common.LspDataUtil;
import samples.lightspeed.common.touch.TouchToolBar;
import samples.lightspeed.editing.EditableLayerFactory;
import samples.lightspeed.editing.ModelFactory;

/**
 * This sample demonstrates the use of the touch-based ILspControllers for editing purposes.
 */
public class MainPanel extends LightspeedSample {

  // Custom toolbar which adds editing and creation controllers
  // to the default Lightspeed sample toolbar
  private CreateAndEditToolBar fCreateAndEditToolBar;

  public MainPanel() {
    super(true); // true because we want a touch toolbar.
  }

  @Override
  protected JToolBar[] createToolBars(ILspAWTView aView) {
    final TouchToolBar touchToolBar = (TouchToolBar) super.createToolBars(aView)[0];
    if (fCreateAndEditToolBar == null) {
      ILspInteractivePaintableLayer shapesLayer = createAndAddShapesLayer(aView);
      fCreateAndEditToolBar = new CreateAndEditToolBar(aView, this,
                                                       touchToolBar.getButtonGroup(),
                                                       true, false, true, shapesLayer) {
        @Override
        protected ILspController createDefaultController() {
          return touchToolBar.getDefaultController();
        }

      };
    }
    return new JToolBar[]{touchToolBar, fCreateAndEditToolBar};
  }

  @Override
  protected void createGUI() {
    super.createGUI();

    // Once everything is connected to the sample, adapt the look and feel.
    TouchUtil.setTouchLookAndFeel(this);
  }

  private ILspInteractivePaintableLayer createAndAddShapesLayer(ILspAWTView aView) {
    ILcdModel shapesModel = new ModelFactory().createShapesModel();
    return (ILspInteractivePaintableLayer) LspDataUtil.instance()
                                                      .model(shapesModel)
                                                      .layer(new EditableLayerFactory())
                                                      .label("Editable shapes")
                                                      .addToView(aView)
                                                      .getLayer();
  }

  public static void main(final String[] aArgs) {
    TLcdIconFactory.setDefaultSize(TLcdIconFactory.Size.SIZE_32);
    startSample(MainPanel.class, "Touch editing");
  }

}

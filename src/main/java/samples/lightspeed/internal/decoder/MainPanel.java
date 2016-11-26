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
package samples.lightspeed.internal.decoder;

import java.awt.Color;
import java.io.IOException;
import java.util.Collection;

import com.luciad.format.bingmaps.ELcdBingMapsMapStyle;
import com.luciad.gui.TLcdPrintComponentAction;
import com.luciad.model.ILcdModel;
import com.luciad.view.lightspeed.ILspAWTView;
import com.luciad.view.lightspeed.services.effects.ALspGraphicsEffect;
import com.luciad.view.lightspeed.services.effects.TLspAmbientLight;
import com.luciad.view.lightspeed.services.effects.TLspFog;
import com.luciad.view.lightspeed.services.effects.TLspHeadLight;

import samples.common.formatsupport.OpenAction;
import samples.common.serviceregistry.ServiceRegistry;
import samples.decoder.bingmaps.DataSourceFactory;
import samples.gxy.common.ProgressUtil;
import samples.lightspeed.common.LightspeedSample;
import samples.lightspeed.common.LspDataUtil;
import samples.lightspeed.common.LspOpenSupport;
import samples.lightspeed.common.ToolBar;
import samples.lightspeed.printing.PrintAction;
import samples.lightspeed.printing.PrintPreviewAction;

/**
 * The decoder sample illustrates the use of <code>ILcdModelDecoder</code> and
 * <code>ILspLayerFactory</code> to add models to an <code>ILspView</code>.
 * <p/>
 * The subpackages of this sample contain layer factory implementations specific to various
 * file formats. The sample itself uses <code>TLspCompositeLayerFactory</code> to aggregate these
 * implementations, and <code>TLcdOpenAction</code> to aggregate the corresponding model decoders.
 * The open action can be invoked via a toolbar button or by dragging and dropping files onto the
 * {@code ILspView}.
 */
public class MainPanel extends LightspeedSample {

  @Override
  protected void createGUI() {
    super.createGUI();

    LspOpenSupport openSupport = new LspOpenSupport(getView());
    openSupport.addStatusListener(ProgressUtil.createProgressDialog(this, "Loading data"));

    getToolBars()[0].addAction(new OpenAction(openSupport), ToolBar.FILE_GROUP);

    TLcdPrintComponentAction printAction = new TLcdPrintComponentAction(getView().getHostComponent());
    printAction.setRasterizedLayerQualityFactor(16);
    printAction.setScale(0);
    printAction.setRasterizedLayerMaximumStripSize(0);
    printAction.setShortDescription(
        "High res print (" + printAction.getRasterizedLayerQualityFactor() + "x / " + printAction.getScale() + "x)"
    );
    getToolBars()[0].addAction(printAction, ToolBar.FILE_GROUP);

    getToolBars()[0].addAction(new PrintAction(this, getView()), ToolBar.FILE_GROUP);
    getToolBars()[0].addAction(new PrintPreviewAction(this, getView()), ToolBar.FILE_GROUP);
  }

  @Override
  protected void addData() throws IOException {
    super.addData();

    ServiceRegistry.getInstance().register(new DGNLayerFactory());
    ServiceRegistry.getInstance().register(new DWGLayerFactory());

    ILcdModel bing = DataSourceFactory.createDefaultBingModel(ELcdBingMapsMapStyle.AERIAL_WITH_LABELS, this);
    if (bing != null) {
      LspDataUtil.instance().model(bing).layer().addToView(getView());
    }
  }

  @Override
  protected ILspAWTView createView() {
    ILspAWTView view = super.createView();
    view.setBackground(Color.white);
    Collection<ALspGraphicsEffect> fx = view.getServices().getGraphicsEffects();
    fx.add(new TLspHeadLight(getView()));
    fx.add(new TLspAmbientLight(new Color(64, 64, 64)));
    fx.add(new TLspFog(getView()));
    return view;
  }

}

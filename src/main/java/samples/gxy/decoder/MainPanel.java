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
package samples.gxy.decoder;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.luciad.format.raster.TLcdBufferedTile;
import com.luciad.imaging.ALcdImage;
import com.luciad.imaging.operator.ALcdImageOperatorChain;
import com.luciad.io.TLcdIOUtil;
import com.luciad.io.TLcdInputStreamFactory;
import com.luciad.view.gxy.ILcdGXYLayer;
import com.luciad.view.gxy.ILcdGXYView;

import samples.common.action.SaveAction;
import samples.common.dimensionalfilter.LayerDimensionalFilterCustomizer;
import samples.common.dimensionalfilter.model.DimensionalFilterProvider;
import samples.common.formatsupport.GXYOpenSupport;
import samples.common.formatsupport.OpenAction;
import samples.common.formatsupport.OpenURLAction;
import samples.common.serviceregistry.ServiceRegistry;
import samples.gxy.common.GXYSample;
import samples.gxy.common.toolbar.ToolBar;
import samples.gxy.decoder.raster.multispectral.BandSelectLayeredListener;
import samples.gxy.decoder.raster.multispectral.ImageUtil;
import samples.gxy.fundamentals.step1.Main;
import samples.gxy.projections.ProjectionComboBox;

/**
 * This sample demonstrates the ability to load data from sources in
 * different formats using {@link GXYOpenSupport}.
 * The support class allows you to load data in almost all LuciadLightspeed supported formats.
 * <br/>
 * To accomplish this, a composite layer factory and composite model decoder are populated
 * by making use of a service registry.
 * <p/>
 * For a step-by-step explanation of how to load and visualize models in a view, refer to the {@link Main fundamentals samples}
 * and the developer's guide.
 */
public class MainPanel extends GXYSample {

  private GXYOpenSupport fOpenSupport;
  private LayerDimensionalFilterCustomizer fLayerDimensionalFilterCustomizer;
  private BandSelectLayeredListener<ILcdGXYView, ILcdGXYLayer> fBandSelectLayeredListener;
  private String[] fArgs = new String[0];

  public MainPanel() {
    super();
  }

  public MainPanel(boolean aAnimateSideBar) {
    super(aAnimateSideBar);
  }

  public MainPanel(final String[] aArgs) {
    super();
    fArgs = aArgs;
  }

  protected void createGUI() {
    super.createGUI();
    configureActions();

    // Add a projection combo box.
    getToolBars()[0].addSpace();
    getToolBars()[0].addComponent(new ProjectionComboBox(getView(), 0));

    // Displays an information message for multi-spectral images
    fBandSelectLayeredListener = new BandSelectLayeredListener<ILcdGXYView, ILcdGXYLayer>(getView(), getView(), getSelectedLayers()) {
      @Override
      protected void setBandSelectFilter(ILcdGXYLayer aLayer, ALcdImage aImage, ALcdImageOperatorChain aBandSelect) {
        ImageUtil.setImageOperatorChain(aLayer, aImage, aBandSelect);
      }
    };

    // Filters layers with multi-dimensional data
    Iterable<DimensionalFilterProvider> query = ServiceRegistry.getInstance().query(DimensionalFilterProvider.class);
    fLayerDimensionalFilterCustomizer = new LayerDimensionalFilterCustomizer(getView(), getSelectedLayers(), getOverlayPanel(), query);
  }

  private void configureActions() {
    ToolBar toolBar = getToolBars()[0];
      fOpenSupport = new GXYOpenSupport(getView());
      fOpenSupport.addStatusListener(getStatusBar());
      toolBar.addAction(new OpenAction(fOpenSupport));
      toolBar.addAction(new OpenURLAction(fOpenSupport));
      toolBar.addAction(new SaveAction(getView()));
  }

  @Override
  protected void addData() throws IOException {
    super.addData();
    for (String source : fArgs) {
      fOpenSupport.openSource(source, null);
    }
  }

  @Override
  protected void tearDown() {
    fBandSelectLayeredListener.dispose();
    fLayerDimensionalFilterCustomizer.dispose();
    super.tearDown();
  }

  public static void main(final String[] aArgs) {
    startSample(MainPanel.class, aArgs, "GXY Data Viewer");
  }

}


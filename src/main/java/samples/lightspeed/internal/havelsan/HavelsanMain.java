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
package samples.lightspeed.internal.havelsan;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.io.IOException;
import java.util.Collection;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.luciad.geodesy.TLcdGeodeticDatum;
import com.luciad.projection.TLcdEquidistantCylindrical;
import com.luciad.reference.TLcdGridReference;
import com.luciad.util.TLcdNoBoundsException;
import com.luciad.util.TLcdOutOfBoundsException;
import com.luciad.view.lightspeed.ALspViewAdapter;
import com.luciad.view.lightspeed.ILspAWTView;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.TLspViewBuilder;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.ILspLayerFactory;
import com.luciad.view.lightspeed.layer.TLspCompositeLayerFactory;
import com.luciad.view.lightspeed.layer.raster.TLspRasterLayer;
import com.luciad.view.lightspeed.util.TLspViewNavigationUtil;
import com.luciad.view.lightspeed.util.TLspViewTransformationUtil;
import com.luciad.view.opengl.binding.ILcdGLDrawable;

import samples.common.DefaultExceptionHandler;
import samples.common.SampleData;
import samples.lightspeed.common.LspDataUtil;
import samples.lightspeed.internal.havelsan.radar.RadarLayerFactory;
import samples.lightspeed.internal.havelsan.radar.RadarModelFactory;
import samples.lightspeed.internal.havelsan.tactical.TacticalLayerFactory;
import samples.lightspeed.internal.havelsan.tactical.TacticalModelFactory;

/**
 * @author tomn
 * @since 2012.0
 */
public class HavelsanMain extends JPanel {

  private final ILspAWTView fView;

  public HavelsanMain() {
    setLayout(new BorderLayout());
    fView = createView();
    fView.setLayerFactory(createLayerFactory());
    TLspViewTransformationUtil.setup2DView(
        fView, new TLcdGridReference(new TLcdGeodeticDatum(), new TLcdEquidistantCylindrical())
    );
    add(fView.getHostComponent(), BorderLayout.CENTER);

//    add( LayerControlPanelFactoryLsp.createDefaultLayerControlPanel( fView ), BorderLayout.EAST );

    Runnable runnable = new Runnable() {
      public void run() {
        try {
          addData();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    };
    EventQueue.invokeLater(runnable);
  }

  public ILspAWTView getView() {
    return fView;
  }

  protected ILspAWTView createView() {
//    ALcdAnimationManager.getInstance().setTargetUpdateRate( 5 );

    return TLspViewBuilder.newBuilder()
                          .size(512, 512)
                          .buildAWTView();
  }

  protected void addData() throws IOException {
    TLspRasterLayer background = new TLspRasterLayer(
        LspDataUtil.instance().model(SampleData.EARTH).getModel(),
        ILspLayer.LayerType.BACKGROUND
    );
    background.setCacheSize(1 * 1024 * 1024);
    getView().addLayer(background);

    try {
      getView().addLayersFor(RadarModelFactory.createRadarModel());
    } catch (Exception e) {
      e.printStackTrace();
    }

    Collection<ILspLayer> tactical = getView().addLayersFor(
        TacticalModelFactory.createTacticalModel()
    );

    fitOnLayers(tactical);
  }

  protected void fitOnLayers(final Collection<ILspLayer> aLayers) {
    if (aLayers != null && aLayers.size() > 0) {

      SwingUtilities.invokeLater(
          new Runnable() {
            public void run() {
              //At startup the view's width and height might not
              //have been initialized. To make sure that these
              //have been set properly, fitting is performed
              //at the end of the view's repaint by using an
              //ILspViewListener.
              getView().addViewListener(new ALspViewAdapter() {
                @Override
                public void postRender(ILspView aView, ILcdGLDrawable aGLDrawable) {
                  aView.removeViewListener(this);
                  try {
                    new TLspViewNavigationUtil(aView).fit(aLayers);
                  } catch (TLcdOutOfBoundsException e) {
                    System.out.println("Layer not visible in current projection.");
                  } catch (TLcdNoBoundsException e) {
                    System.out.println("Could not fit on the layer.\n" + e.getMessage());
                  }
                }
              });
            }
          }
      );
    }
  }

  protected ILspLayerFactory createLayerFactory() {
    return new TLspCompositeLayerFactory(
        new TacticalLayerFactory(),
        new RadarLayerFactory()
    );
  }

}

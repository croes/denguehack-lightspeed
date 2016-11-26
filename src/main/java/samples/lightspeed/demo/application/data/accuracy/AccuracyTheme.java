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
package samples.lightspeed.demo.application.data.accuracy;

import java.awt.Color;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JPanel;

import com.luciad.datamodel.ILcdDataObject;
import com.luciad.format.mif.TLcdMIFModelDecoder;
import com.luciad.model.ILcdModel;
import com.luciad.text.TLcdDistanceFormat;
import com.luciad.util.TLcdDistanceUnit;
import com.luciad.util.collections.ILcdMultiKeyCache;
import com.luciad.util.collections.TLcdWeakIdentityHashMap;
import com.luciad.view.animation.ALcdAnimation;
import com.luciad.view.lightspeed.ILspAWTView;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.TLspContext;
import com.luciad.view.lightspeed.controller.ILspController;
import com.luciad.view.TLcdAWTEventFilterBuilder;
import com.luciad.view.lightspeed.controller.ruler.TLspRulerController;
import com.luciad.view.lightspeed.controller.ruler.TLspRulerDistanceFormatStyle;
import com.luciad.view.lightspeed.controller.ruler.TLspRulerLabelStyler;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.TLspPaintState;
import com.luciad.view.lightspeed.layer.shape.TLspShapeLayerBuilder;
import com.luciad.view.lightspeed.style.ILspTexturedStyle;
import com.luciad.view.lightspeed.style.ILspWorldElevationStyle.ElevationMode;
import com.luciad.view.lightspeed.style.TLspFillStyle;
import com.luciad.view.lightspeed.style.styler.ALspStyleCollector;
import com.luciad.view.lightspeed.style.styler.ALspStyler;
import com.luciad.view.lightspeed.style.styler.TLspCustomizableStyle;

import samples.common.HaloLabel;
import samples.lightspeed.demo.framework.data.IOUtil;
import samples.lightspeed.demo.framework.data.themes.AbstractTheme;
import samples.lightspeed.demo.framework.data.themes.ThemeAnimation;

/**
 * @author tomn
 * @since 2012.0
 */
public class AccuracyTheme extends AbstractTheme {

  private String fActiveSubTheme = null;

  private String fSource = "Data/internal/accuracy/accuracy.mif";
  private double fSpeedUp = 1;
  private Map<ILspView, ILspLayer> fView2Layer = new TLcdWeakIdentityHashMap<ILspView, ILspLayer>();

  private Map<ILspView, ILspController> fView2Controller = new TLcdWeakIdentityHashMap<ILspView, ILspController>();

  public AccuracyTheme() {
    setName("Accuracy");
    setCategory("Terrain");
  }

  public String getActiveSubTheme() {
    return fActiveSubTheme;
  }

  @Override
  public void initialize(List<ILspView> aViews, Properties aProps) {
    super.initialize(aViews, aProps);

    for (ILspView view : aViews) {
      ((ILspAWTView) view).getHostComponent().addKeyListener(new AccuracyThemeWriteCameraStateKeyListener(this, view));
    }

    String src = aProps.getProperty("data.src");
    if (src != null) {
      fSource = src;
    }

    fSpeedUp = Double.parseDouble(aProps.getProperty("flyto.speedup", "0.2"));
  }

  private void setYardsRulerController(ILspView aView) {
    TLcdDistanceFormat format = new TLcdDistanceFormat(new TLcdDistanceUnit("yards", "yards", 0.9144));
    format.setFractionDigits(0);
    ILspController controller = aView.getController();
    aView.setController(null);
    TLspRulerController ruler = new TLspRulerController();
    adaptDistanceFormat((TLspRulerLabelStyler) ruler.getLabelStyler(), format);
    ruler.setAWTFilter(TLcdAWTEventFilterBuilder.newBuilder().openBrackets().leftMouseButton().or().rightMouseButton().closeBrackets().and().ctrlFilter(true).build());
    ruler.appendController(controller);
    aView.setController(ruler);
    fView2Controller.put(aView, controller);
  }

  private void setCmRulerController(ILspView aView) {
    TLcdDistanceFormat format = new TLcdDistanceFormat(new TLcdDistanceUnit("cm", "cm", 0.01));
    format.setFractionDigits(0);
    ILspController controller = aView.getController();
    aView.setController(null);
    TLspRulerController ruler = new TLspRulerController();
    adaptDistanceFormat((TLspRulerLabelStyler) ruler.getLabelStyler(), format);
    ruler.setAWTFilter(TLcdAWTEventFilterBuilder.newBuilder().openBrackets().leftMouseButton().or().rightMouseButton().closeBrackets().and().ctrlFilter(true).build());
    ruler.appendController(controller);
    aView.setController(ruler);
    fView2Controller.put(aView, controller);
  }

  private void adaptDistanceFormat(TLspRulerLabelStyler aLabelStyler, TLcdDistanceFormat aFormat) {
    for (TLspCustomizableStyle style : aLabelStyler.getStyles()) {
      if (style.getStyle() instanceof TLspRulerDistanceFormatStyle) {
        style.setStyle(((TLspRulerDistanceFormatStyle) style.getStyle()).
                                                                            asBuilder().distanceFormat(aFormat).build());
      }
    }
  }

  private void restoreController(ILspView aView) {
    ILspController controller = fView2Controller.get(aView);
    if (controller != null) {
      aView.setController(controller);
    }
    fView2Controller.remove(aView);
  }

  @Override
  public void deactivate() {
    for (ILspView view : fView2Layer.keySet()) {
      restoreController(view);
    }
    super.deactivate();    //To change body of overridden methods use File | Settings | File Templates.
  }

  @Override
  public List<JPanel> getThemePanels() {
    JButton zoomOut = new JButton("Zoom out");
    zoomOut.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        getAnimation().doAnimation();
      }
    });

    JPanel panel = new JPanel(new GridLayout(7, 1, 10, 10)) {
      @Override
      public Insets getInsets() {
        return new Insets(10, 10, 10, 10);
      }
    };
    panel.add(new HaloLabel("LA Sight Seeing", 15, true));
    panel.add(createFitButton("Airport", "International Airport", false, false));
    panel.add(createFitButton("Getty", "Getty Center", false, false));
    panel.add(createFitButton("Universal", "Universal Studios", false, false));
    panel.add(createFitButton("Football", "Football Game", true, false));
    panel.add(createFitButton("Pool", "Pool Time", false, false));
    panel.add(zoomOut);
    panel.setSize(panel.getLayout().preferredLayoutSize(panel));
    panel.updateUI();

    return Collections.singletonList(panel);
  }

  private JButton createFitButton(final String aSubThemeName, String aText, final boolean aWithYardsController, final boolean aWithCmController) {
    JButton zoomIn = new JButton(aText);
    final ThemeAnimation[] animation = {null};
    zoomIn.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        fActiveSubTheme = aSubThemeName;
        for (ILspView view : fView2Layer.keySet()) {
          if (aWithYardsController) {
            restoreController(view);
            setYardsRulerController(view);
          } else if (aWithCmController) {
            restoreController(view);
            setCmRulerController(view);
          } else {
            restoreController(view);
          }
        }

        animation[0] = new ThemeAnimation(aSubThemeName, new ArrayList<ILspView>(fView2Layer.keySet()), new EaseOutInterpolator(), 7);
        animation[0].doAnimation();
      }
    });
    return zoomIn;
  }

  @Override
  protected List<ILspLayer> createLayers(List<ILspView> aViews) {
    ArrayList<ILspLayer> layers = new ArrayList<ILspLayer>();

    ILcdModel model = null;
    try {
      model = new TLcdMIFModelDecoder().decode(fSource);
      for (ILspView view : aViews) {
        ILspLayer layer = TLspShapeLayerBuilder.newBuilder()
                                               .model(model)
                                               .selectable(false)
                                               .bodyStyler(
                                                   TLspPaintState.REGULAR,
                                                   new AccuracyThemeStyler()
                                               )
                                               .build();
        view.addLayer(layer);
        fView2Layer.put(view, layer);
        layers.add(layer);
      }

      return layers;
    } catch (IOException e) {
      e.printStackTrace();
      return layers;
    }
  }

  public static class AccuracyThemeStyler extends ALspStyler {
    @Override
    public void style(Collection<?> aObjects, ALspStyleCollector aStyleCollector, TLspContext aContext) {
      for (Object o : aObjects) {
        try {
          ILcdDataObject dataObject = (ILcdDataObject) o;

          ILcdMultiKeyCache mkc = aContext.getView().getServices().getMultiKeyCache();
          Object[] keys = new Object[]{this, dataObject};
          BufferedImage image = (BufferedImage) mkc.get(keys);
          if (image == null) {
            String name = (String) dataObject.getValue("Name");
            image = IOUtil.readImage(name);
            mkc.put(keys, image);
          }

          TLspFillStyle style = TLspFillStyle.newBuilder()
                                             .elevationMode(ElevationMode.ON_TERRAIN)
                                             .color(Color.white)
                                             .opacity(1f)
                                             .texture(image)
                                             .textureCoordinatesMode(ILspTexturedStyle.TextureCoordinatesMode.OBJECT_RELATIVE)
                                             .build();

          aStyleCollector
              .object(dataObject)
              .style(style)
              .submit();
        } catch (Exception e) {
          // Ignore object
        }
      }
    }
  }

  private static class EaseOutInterpolator implements ALcdAnimation.Interpolator {
    @Override
    public double transform(double aTime) {
      return 1 - Math.pow(1 - aTime, 6);
    }
  }
}

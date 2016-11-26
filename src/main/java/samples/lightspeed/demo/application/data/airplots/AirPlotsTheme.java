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
package samples.lightspeed.demo.application.data.airplots;

import static com.luciad.view.lightspeed.label.algorithm.TLspLabelLocationProvider.Location.NORTH;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.text.DateFormatter;

import com.luciad.datamodel.ILcdDataObject;
import com.luciad.datamodel.TLcdDataModel;
import com.luciad.datamodel.TLcdDataType;
import com.luciad.model.ILcdDataModelDescriptor;
import com.luciad.model.ILcdModel;
import com.luciad.shape.ILcdPoint;
import com.luciad.shape.shape2D.TLcdXYPoint;
import com.luciad.util.ILcdSelectionListener;
import com.luciad.util.TLcdInterval;
import com.luciad.util.TLcdSelectionChangedEvent;
import com.luciad.view.lightspeed.ILspAWTView;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.TLspContext;
import com.luciad.view.lightspeed.layer.ALspTouchInfo;
import com.luciad.view.lightspeed.layer.ILspInteractivePaintableLayer;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.TLspPaintRepresentationState;
import com.luciad.view.lightspeed.layer.TLspPaintState;
import com.luciad.view.lightspeed.layer.plots.TLspPlotLayerBuilder;
import com.luciad.view.lightspeed.painter.label.style.ALspLabelTextProviderStyle;
import com.luciad.view.lightspeed.query.TLspPaintedObjectsTouchQuery;
import com.luciad.view.lightspeed.style.TLspTextStyle;
import com.luciad.view.lightspeed.style.styler.ILspStyler;
import com.luciad.view.lightspeed.style.styler.TLspLabelStyler;

import samples.lightspeed.demo.framework.application.Framework;
import samples.lightspeed.demo.framework.data.themes.AbstractTheme;
import samples.lightspeed.plots.HeadingStyler;
import samples.lightspeed.common.ViewScaleThresholdListener;
import samples.lightspeed.plots.datamodelstyling.DataTypeStyler;

/**
 * Realtime plot painting theme for generated Air Tracks data set.
 */
public class AirPlotsTheme extends AbstractTheme {

  private final AirPlotsPanelFactory fPanelFactory = new AirPlotsPanelFactory();
  private Map<ILspView, ToolTipMouseMotionListener> fMouseMotionListeners = new HashMap<ILspView, ToolTipMouseMotionListener>();
  private List<ILspView> fViews;
  TLcdDataType fDataType;
  DataTypeStyler fDataTypeStyler;
  HighlightingStyler fHighlightStyler;

  public AirPlotsTheme() {
    setName("Air Plots");
    setCategory("Tracks");
  }

  @Override
  public void initialize(List<ILspView> aViews, Properties aProps) {
    super.initialize(aViews, aProps);
    fViews = new ArrayList<ILspView>(aViews);
  }

  @Override
  public void activate() {
    super.activate();
    addMouseMotionListener();
  }

  @Override
  public void deactivate() {
    removeMouseMotionListener();
    super.deactivate();
  }

  private void addMouseMotionListener() {
    for (final ILspView view : fViews) {
      if (view instanceof ILspAWTView) {
        final ILspAWTView awtView = (ILspAWTView) view;

        ToolTipMouseMotionListener listener = fMouseMotionListeners.get(view);
        if (listener == null) {
          listener = new ToolTipMouseMotionListener(awtView);
          fMouseMotionListeners.put(awtView, listener);
        }

        awtView.getHostComponent().addMouseMotionListener(listener);
      }
    }
  }

  private void removeMouseMotionListener() {
    for (final ILspView view : fViews) {
      if (view instanceof ILspAWTView) {
        final ILspAWTView awtView = (ILspAWTView) view;
        ToolTipMouseMotionListener listener = fMouseMotionListeners.get(view);
        if (listener != null) {
          listener.setTooltip(null, -1, -1);
          awtView.getHostComponent().removeMouseMotionListener(listener);
        }
      }
    }
  }

  @Override
  protected List<ILspLayer> createLayers(List<ILspView> aViews) {
    Framework framework = Framework.getInstance();

    ILcdModel model = framework.getModelWithID("model.id.airplots.plots");

    TLcdDataModel dataModel = ((ILcdDataModelDescriptor) model.getModelDescriptor()).getDataModel();
    fDataType = dataModel.getDeclaredTypes().iterator().next();
    fDataTypeStyler = new DataTypeStyler(fDataType);

    final HeadingStyler headingStyler = new HeadingStyler(fDataTypeStyler, fDataType.getProperty("Heading"));

    fHighlightStyler = new HighlightingStyler(headingStyler);

    ILspStyler styler = fHighlightStyler;

    List<ILspLayer> allLayers = new ArrayList<ILspLayer>();
    for (ILspView view : aViews) {
      ILspLayer layer = createLayer(model, styler);
      view.addLayer(layer);
      framework.registerLayers("layer.id.airplots.plots", view, Collections.singleton(layer));
      allLayers.add(layer);

      ViewScaleThresholdListener.attach(view, 0.04, new ViewScaleThresholdListener.ThresholdListener() {
        @Override
        public void thresholdChanged(boolean aBelowThreshold) {
          headingStyler.setEnabled(aBelowThreshold);
        }
      });

      layer.addSelectionListener(new MySelectionListener());
    }

    return allLayers;
  }

  @Override
  public List<JPanel> getThemePanels() {
    return fPanelFactory.createThemePanels(this);
  }

  public ILspLayer createLayer(ILcdModel aModel, ILspStyler aStyler) {
    return TLspPlotLayerBuilder.newBuilder()
                               .model(aModel)
                               .mandatoryOrientation(true)
                               .mandatoryAttributes(fDataTypeStyler.getAttributes())
                               .bodyStyler(TLspPaintState.REGULAR, aStyler)
                               .labelStyler(TLspPaintState.REGULAR, createLabelStyler())
                               .labelScaleRange(new TLcdInterval(0.2, Double.MAX_VALUE))
                               .build();
  }

  private TLspLabelStyler createLabelStyler() {
    return TLspLabelStyler.newBuilder()
                          .locations(8, NORTH)
                          .styles(
                              new TimeProvider(),
                              TLspTextStyle.newBuilder()
                                           .font("Dialog-BOLD-11")
                                           .textColor(Color.white)
                                           .haloColor(Color.decode("0x024E68"))
                                           .build()
                          )
                          .build();
  }

  private static class TimeProvider extends ALspLabelTextProviderStyle {
    @Override
    public String[] getText(Object aDomainObject, Object aSubLabelID, TLspContext aContext) {
      ILcdDataObject plot = (ILcdDataObject) aDomainObject;
      try {
        return new String[]{new DateFormatter(new SimpleDateFormat("HH:mm:ss")).valueToString(new Date((Integer) plot.getValue("Time")))};
      } catch (ParseException e) {
        return new String[]{};
      }
    }
  }

  private class ToolTipMouseMotionListener implements MouseMotionListener {

    private final ILspAWTView fView;
    private final Container fContainer;

    private Component fLabel = null;
    private ILcdDataObject fCurrentPlot;

    public ToolTipMouseMotionListener(ILspAWTView aView) {
      fView = aView;
      fContainer = fView.getOverlayComponent();
    }

    public void setTooltip(ILcdDataObject aPlot, int aX, int aY) {
      if (aPlot == null) {
        if (fLabel != null) {
          fContainer.remove(fLabel);
          revalidate(fContainer);
          fContainer.repaint();
          fLabel = null;
          fCurrentPlot = null;
        }
        return;
      }

      if (aPlot != fCurrentPlot) {
        fCurrentPlot = aPlot;

        if (fLabel != null) {
          fContainer.remove(fLabel);
        }
        fLabel = new MyLabel(aPlot);
        fContainer.add(fLabel);
        revalidate(fContainer);
        fContainer.repaint();
      }

      if (fLabel != null) {
        Dimension preferredSize = fLabel.getPreferredSize();
        int w = preferredSize.width + 10;
        int h = preferredSize.height + 6;
        fLabel.setBounds(aX - (w / 2), aY + h, w, h);
      }
    }

    private void revalidate(Component aComponent) {
      synchronized (aComponent.getTreeLock()) {
        aComponent.invalidate();

        Container root = aComponent.getParent();
        if (root == null) {
          // There's no parents. Just validate itself.
          aComponent.validate();
        } else {
          while (!(root instanceof JComponent) || !((JComponent) root).isValidateRoot()) {
            if (root.getParent() == null) {
              // If there's no validate roots, we'll validate the
              // topmost container
              break;
            }

            root = root.getParent();
          }

          root.validate();
        }
      }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
    }

    @Override
    public void mouseMoved(MouseEvent e) {
      if (!fDataTypeStyler.isUseDensity()) {
        ILcdDataObject plot = null;
        for (ILspLayer layer : getLayers()) {
          TLspContext context = new TLspContext(layer, fView);
          Collection<ALspTouchInfo> result = ((ILspInteractivePaintableLayer) layer).query(
              new TLspPaintedObjectsTouchQuery(
                  TLspPaintRepresentationState.REGULAR_BODY,
                  (ILcdPoint) new TLcdXYPoint(e.getX(), e.getY()),
                  16
              ),
              context
          );
          if (result.size() > 0) {
            ALspTouchInfo hit = result.iterator().next();
            plot = (ILcdDataObject) hit.getDomainObject();
            break;
          }
        }
        fHighlightStyler.setHighlightedObject(plot);
        setTooltip(plot, e.getX(), e.getY());

      } else {
        fHighlightStyler.setHighlightedObject(null);
        setTooltip(null, -1, -1);
      }

    }
  }

  private static class MyLabel extends JLabel {

    public MyLabel(ILcdDataObject aDataObject) {
      try {
        setHorizontalAlignment(JLabel.CENTER);
        setText("<html><b>" + aDataObject.getValue("Flight_nr") + "</b> - " + aDataObject.getValue("Type") + " - " + aDataObject.getValue("Class") + "<br>" + new DateFormatter(new SimpleDateFormat("yyyy.MM.dd HH:mm:ss")).valueToString(new Date((Integer) aDataObject.getValue("Time"))) + "</html>");
      } catch (ParseException e) {
        e.printStackTrace();
      }
    }

    @Override
    protected void paintComponent(Graphics g) {
      // Add a fill and frame color
      g.setColor(new Color(0.3f, 0.3f, 0.3f, 0.9f));
      g.fillRect(0, 0, getSize().width, getSize().height);
      g.setColor(Color.black);
      g.drawRect(0, 0, getSize().width - 1, getSize().height - 1);
      super.paintComponent(g);
    }
  }

  private class MySelectionListener implements ILcdSelectionListener {
    @Override
    public void selectionChanged(TLcdSelectionChangedEvent aSelectionEvent) {
      Enumeration selectedElements = aSelectionEvent.selectedElements();
      if (selectedElements.hasMoreElements()) {
        fHighlightStyler.setSelectedObject(selectedElements.nextElement());
      } else {
        fHighlightStyler.setSelectedObject(null);
      }
    }
  }
}

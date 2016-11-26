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
package samples.lightspeed.grid;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;

import com.luciad.gui.TLcdAWTUtil;
import com.luciad.gui.swing.TLcdOverlayLayout;
import samples.common.HaloLabel;
import com.luciad.reference.ILcdGeocentricReference;
import com.luciad.util.ALcdWeakPropertyChangeListener;
import com.luciad.util.collections.TLcdWeakIdentityHashMap;
import com.luciad.view.ILcdLayer;
import com.luciad.view.lightspeed.ILspAWTView;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.layer.ILspLayer;

import samples.common.gui.RoundedBorder;

/**
 * This mediator is used to make sure that a warning is displayed when the world reference is switched to 3D.
 * This warning is shows in the view, but also as part of the layer name. TLspXYGridLayerBuilder doesn't support
 * geocentric references. When using a grid layer that uses the world reference as reference, no grid is painted, see
 * {@link com.luciad.view.lightspeed.painter.grid.TLspXYGridLayerBuilder#createModel(String)}. In order to
 * give feedback to the user about this, this mediator can be used to display a warning message.
 *
 * When using TLspXYGridLayerBuilder with a fixed reference, this problem can easily be avoid by using a
 * '2D reference', e.g. ILcdGridReference. In that case, this mediator will not be needed.
 */
public class XYGridLayerWarningMediator {

  private static final RoundedBorder ROUNDED_BORDER = new RoundedBorder(new Color(0.3f, 0.3f, 0.3f, 0.5f), 10);

  private static final String WARNING_STRING = "(Only 2D)";
  private static final String TOOLTIP_STRING = "XY grids are not supported in 3D views";

  public XYGridLayerWarningMediator(final ILspLayer aLayer) {
    this(aLayer, WARNING_STRING, TOOLTIP_STRING);
  }

  public XYGridLayerWarningMediator(final ILspLayer aLayer, final String aWarningString, final String aTooltipString) {
    TLcdAWTUtil.invokeNowOrLater(new Runnable() {
      @Override
      public void run() {
        JPanel tooltipPanel = new JPanel(new BorderLayout());
        tooltipPanel.setBorder(ROUNDED_BORDER);
        tooltipPanel.add(createLabel(aTooltipString));
        makeTransparent(tooltipPanel);
        aLayer.addPropertyChangeListener(new CurrentViewsPropertyChangeListener(aLayer, aWarningString, tooltipPanel));
      }
    });
  }

  /**
   * Makes the component's background transparent.
   *
   * @param aComponent the component
   */
  private static void makeTransparent(JComponent aComponent) {
    aComponent.setBackground(new Color(0f, 0f, 0f, 0f));
    aComponent.setOpaque(false);
  }

  private static JLabel createLabel(String aText) {
    HaloLabel label = new HaloLabel(aText);
    label.setTextColor(new Color(1f, 1f, 1f, 0.9f));
    label.setHaloColor(new Color(0.3f, 0.3f, 0.3f, 0.7f));
    makeTransparent(label);
    return label;
  }

  private static void adjustLabel(ILcdLayer aLayer, ILspView aView, String aWarningString, final JPanel aTooltipPanel) {
    boolean is3D = aView.getXYZWorldReference() instanceof ILcdGeocentricReference;
    String label = aLayer.getLabel();
    boolean containsWarning = label.contains(aWarningString);
    if (is3D && !containsWarning) {
      aLayer.setLabel(label + " " + aWarningString);
      if (aView instanceof ILspAWTView) {
        final ILspAWTView awtView = (ILspAWTView) aView;
        showTooltip(aTooltipPanel, awtView);
        final Timer timer = new Timer(5000, new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent e) {
            hideTooltip(awtView, aTooltipPanel);
          }
        });
        timer.setRepeats(false);
        timer.start();
      }
    } else if (!is3D && containsWarning) {
      removeLabel(aLayer, aView, aWarningString, aTooltipPanel);
    }
  }

  private static void removeLabel(ILcdLayer aLayer, ILspView aView, String aWarningString, JPanel aTooltipPanel) {
    String label = aLayer.getLabel();
    String newLabel = label.replace(aWarningString, "").trim();
    aLayer.setLabel(newLabel);
    if (aView instanceof ILspAWTView) {
      ILspAWTView awtView = (ILspAWTView) aView;
      hideTooltip(awtView, aTooltipPanel);
    }
  }

  private static void showTooltip(JPanel aTooltipPanel, ILspAWTView aAwtView) {
    aAwtView.getOverlayComponent().add(aTooltipPanel, TLcdOverlayLayout.Location.CENTER);
    aAwtView.getOverlayComponent().revalidate();
    aAwtView.getOverlayComponent().repaint();
  }

  private static void hideTooltip(ILspAWTView aAwtView, JPanel aTooltipPanel) {
    aAwtView.getOverlayComponent().remove(aTooltipPanel);
    aAwtView.getOverlayComponent().revalidate();
    aAwtView.getOverlayComponent().repaint();
  }

  private static class CurrentViewsPropertyChangeListener extends ALcdWeakPropertyChangeListener<ILspLayer> {

    private final Map<ILspView, PropertyChangeListener> fViewListeners = new TLcdWeakIdentityHashMap<>();
    private final String fWarningString;
    private final JPanel fTooltipPanel;

    public CurrentViewsPropertyChangeListener(ILspLayer aLayer, String aWarningString, JPanel aTooltipPanel) {
      super(aLayer);
      fWarningString = aWarningString;
      fTooltipPanel = aTooltipPanel;
      ArrayList<ILspView> currentViews = new ArrayList<>(aLayer.getCurrentViews());
      for (ILspView currentView : currentViews) {
        viewAdded(currentView, aLayer);
      }
    }

    @Override
    protected void propertyChangeImpl(ILspLayer aLayer, PropertyChangeEvent evt) {
      if ("currentViews".equals(evt.getPropertyName())) {
        Collection<ILspView> viewsToRemove = new ArrayList<>(fViewListeners.keySet());
        Collection<ILspView> currentViews = aLayer.getCurrentViews();

        for (ILspView currentView : currentViews) {
          if (!fViewListeners.containsKey(currentView)) {
            viewAdded(currentView, aLayer);
          }
          viewsToRemove.remove(currentView);
        }

        for (ILspView viewToRemove : viewsToRemove) {
          viewRemoved(viewToRemove);
          removeLabel(aLayer, viewToRemove, fWarningString, fTooltipPanel);
        }
      }
    }

    private void viewAdded(ILspView aView, ILcdLayer aLayer) {
      adjustLabel(aLayer, aView, fWarningString, fTooltipPanel);
      WorldReferencePropertyChangeListener listener = new WorldReferencePropertyChangeListener(aLayer, fWarningString, fTooltipPanel);
      aView.addPropertyChangeListener(listener);
      fViewListeners.put(aView, listener);
    }

    private void viewRemoved(ILspView aView) {
      PropertyChangeListener listener = fViewListeners.remove(aView);
      if (listener != null) {
        aView.removePropertyChangeListener(listener);
      }
    }
  }

  private static class WorldReferencePropertyChangeListener extends ALcdWeakPropertyChangeListener<ILcdLayer> {

    private final String fWarningString;
    private final JPanel fTooltipPanel;

    public WorldReferencePropertyChangeListener(ILcdLayer aLayer, String aWarningString, JPanel aTooltipPanel) {
      super(aLayer);
      fWarningString = aWarningString;
      fTooltipPanel = aTooltipPanel;
    }

    @Override
    protected void propertyChangeImpl(ILcdLayer aLayer, PropertyChangeEvent aPropertyChangeEvent) {
      if ("XYZWorldReference".equals(aPropertyChangeEvent.getPropertyName())) {
        ILspView view = (ILspView) aPropertyChangeEvent.getSource();
        adjustLabel(aLayer, view, fWarningString, fTooltipPanel);
      }
    }
  }
}

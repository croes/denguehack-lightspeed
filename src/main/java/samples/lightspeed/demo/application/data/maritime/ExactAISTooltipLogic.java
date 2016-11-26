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
package samples.lightspeed.demo.application.data.maritime;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

import javax.swing.JLabel;

import com.luciad.gui.swing.TLcdSWIcon;
import com.luciad.view.ILcdLayer;
import com.luciad.view.lightspeed.layer.ILspLayer;

import samples.lightspeed.demo.application.data.maritime.countrycodeutil.CountryCodeUtil;
import samples.lightspeed.demo.framework.gui.TooltipMouseListener;

public final class ExactAISTooltipLogic implements TooltipMouseListener.TooltipLogic {
  private final List<ILspLayer> fExactAISLayers;
  private final ExactAISLayerFactory fExactAISLayerFactory;
  private final ExactAISModelDescriptor fModelDescriptor;

  public ExactAISTooltipLogic(List<ILspLayer> aExactAISLayers, ExactAISLayerFactory aExactAISLayerFactory, ExactAISModelDescriptor aModelDescriptor) {
    fExactAISLayers = aExactAISLayers;
    fExactAISLayerFactory = aExactAISLayerFactory;
    fModelDescriptor = aModelDescriptor;
  }

  @Override
  public boolean canHandleLayer(ILcdLayer aLayer) {
    return fExactAISLayers.contains(aLayer);
  }

  @Override
  public boolean shouldConsiderLayer(ILcdLayer aLayer) {
    if (canHandleLayer(aLayer)) {
      return !fExactAISLayerFactory.getStyler().isPaintDensity();
    }
    return false;
  }

  @Override
  public void willRecalculateTooltip() {
    if (fExactAISLayerFactory.getStyler().isPaintDensity()) {
      fExactAISLayerFactory.getStyler().setHighlightedMMSI(ExactAISStyler.NO_MMSI_SELECTED);
    }
  }

  @Override
  public Component createLabelComponent(Object aModelElement) {
    AISPlot aisPlot = (AISPlot) aModelElement;
    ExactAISModelDescriptor.ShipDescriptor shipDescriptor = fModelDescriptor.getShipDescriptor(aisPlot.getID());
    return new PlotLabel(aisPlot, shipDescriptor);
  }

  @Override
  public void updateForFoundModelElement(Object aModelElement, ILcdLayer aLayer, MouseEvent aMouseEvent, TooltipMouseListener aTooltipMouseListener) {
    fExactAISLayerFactory.getStyler().setHighlightedMMSI(aModelElement instanceof AISPlot ? ((AISPlot) aModelElement).getID() : ExactAISStyler.NO_MMSI_SELECTED);
    aTooltipMouseListener.showTooltip(aModelElement, aLayer, aMouseEvent.getX(), aMouseEvent.getY());
  }

  @Override
  public int getQuerySensitivity() {
    return 16;
  }

  private static class PlotLabel extends JLabel {

    public PlotLabel(AISPlot aPlot, ExactAISModelDescriptor.ShipDescriptor aShip) {
      try {
        setHorizontalAlignment(JLabel.CENTER);
        if (aShip != null && aPlot != null) {
          setText("<html>" + aShip.getVesselName().trim() + " -> " +
                  aShip.getDestination().trim() + "<br>" + "" +
                  aShip.getDraught() + " m - " + "<i>" +
                  MaritimeTheme.sDateFormatter.valueToString(new Date(aPlot.getTimeStamp())) + "</i>" + "</html>");
          setIcon(new TLcdSWIcon(CountryCodeUtil.getCountryFlagIcon(getIsoCode(aShip))));
        } else {
          setText("<html>Unknown</html>");
        }
      } catch (ParseException e) {
        throw new IllegalStateException(e);
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

  public static String getIsoCode(ExactAISModelDescriptor.ShipDescriptor aShip) {
    String mid = ("" + aShip.getMMSI()).substring(0, 3);
    return CountryCodeUtil.getIso2AlphaCode(mid);
  }
}

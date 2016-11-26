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
package samples.hana.lightspeed.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.event.MouseEvent;

import javax.swing.JLabel;

import com.luciad.view.ILcdLayer;

import samples.hana.lightspeed.domain.CustomerPoint;
import samples.hana.lightspeed.styling.CustomerPlotStyler;

/**
 * Tooltip displayed when hovering over a policy holder icon.
 */
public class CustomerStatisticsTooltip implements TooltipMouseListener.TooltipLogic {

  @Override
  public boolean canHandleLayer(ILcdLayer aLayer) {
    return true;
  }

  @Override
  public boolean shouldConsiderLayer(ILcdLayer aLayer) {
    return true;
  }

  @Override
  public void willRecalculateTooltip() {
  }

  @Override
  public Component createLabelComponent(Object aModelElement) {
    return new PlotLabel((CustomerPoint) aModelElement);
  }

  @Override
  public void updateForFoundModelElement(Object aModelElement, ILcdLayer aLayer, MouseEvent aMouseEvent, TooltipMouseListener aTooltipMouseListener) {
    aTooltipMouseListener.showTooltip(aModelElement, aLayer, aMouseEvent.getX(), aMouseEvent.getY());
  }

  @Override
  public int getQuerySensitivity() {
    return 1;
  }

  private static class PlotLabel extends JLabel {

    public PlotLabel(CustomerPoint aPoint) {
      setHorizontalAlignment(JLabel.CENTER);
      if (aPoint != null) {
        String policyValue = "$" + CustomerPlotsTheme.sDollarFormat.format(aPoint.getPolicyValue());
        setText("<html>" + "<b>" + aPoint.getCategory() + "</b>" + "<br>" +
                aPoint.getInsurance() + "<br>" +
                "<i>" + policyValue + "</i>" + "</html>");
        setIcon(CustomerPlotStyler.getSwingIcon(aPoint.getInsurance()));
      } else {
        setText("<html>Unknown</html>");
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

}

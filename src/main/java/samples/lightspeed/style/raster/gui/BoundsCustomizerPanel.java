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
package samples.lightspeed.style.raster.gui;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.luciad.gui.TLcdAWTUtil;
import com.luciad.gui.TLcdSymbol;
import com.luciad.gui.swing.TLcdSWIcon;

import samples.gxy.common.TitledPanel;
import samples.lightspeed.style.raster.RasterStyler;

/**
 * Panel to change the styling of the raster bounds.
 */
public class BoundsCustomizerPanel extends JPanel {

  private RasterStyler fStyler;
  private JButton fColorButton;

  public BoundsCustomizerPanel() {
    fColorButton = new JButton();
    setButtonColor(Color.RED);
    fColorButton.setToolTipText("Change the bounds color");
    fColorButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        Color newColor = JColorChooser.showDialog(
            TLcdAWTUtil.findParentFrame(e.getSource()),
            "Choose bounds color",
            fStyler.getBoundsColor()
        );
        if (newColor != null) {
          setButtonColor(newColor);
          fStyler.setBoundsColor(newColor);
        }
      }
    });

    JPanel panel = new JPanel(new GridBagLayout());
    panel.add(new JLabel("Color: "), new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    panel.add(fColorButton, new GridBagConstraints(1, 0, 1, 1, 0, 0, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    panel.add(Box.createHorizontalGlue(), new GridBagConstraints(2, 0, 1, 1, 1, 0, GridBagConstraints.LINE_START, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));

    setLayout(new GridLayout(1, 1));
    add(TitledPanel.createTitledPanel("Bounds Style", panel));
  }

  public void setStyler(RasterStyler aStyler) {
    fStyler = null;
    Util.setEnabledRecursive(this, aStyler != null);

    if (aStyler == null) {
      return;
    }

    setButtonColor(aStyler.getBoundsColor());

    fStyler = aStyler;
  }

  private void setButtonColor(Color aColor) {
    fColorButton.setIcon(new TLcdSWIcon(new TLcdSymbol(TLcdSymbol.FILLED_RECT, 16, aColor)));
  }
}

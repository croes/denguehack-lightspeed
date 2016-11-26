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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.IndexColorModel;

import javax.swing.JPanel;

import com.luciad.format.raster.TLcdJetIndexColorModelFactory;
import com.luciad.view.lightspeed.ILspView;

import samples.common.HaloLabel;
import samples.hana.lightspeed.MainPanel;
import samples.hana.lightspeed.common.ThemeComponent;
import samples.hana.lightspeed.common.UIUtil;
import samples.hana.lightspeed.model.HanaConnectionExecutorService;

public class CustomerDensityTheme extends ThemeComponent.Theme {
  private static final ThemeComponent.POI ATLANTIC_CITY = new ThemeComponent.POI(
      -077.64709691, 38.79177927, 1.0950741, 0.3512613, "Go to Washington DC");

  private final MainPanel fMainPanel;
  private IndexColorModel sColorModel = createDefaultColorModel(255);

  public CustomerDensityTheme(MainPanel aMainPanel, HanaConnectionExecutorService aExecutorService) {
    super("Policy holder heat map", "samples/hana/lightspeed/insurance/customerdensity.html", ATLANTIC_CITY);
    fMainPanel = aMainPanel;
    aExecutorService.addBusyListener(getListener());
  }

  @Override
  public ILspView getView() {
    return fMainPanel.getView();
  }

  @Override
  public Component getGui() {
    JPanel panel = new JPanel(new BorderLayout());

    JPanel hue = new JPanel() {
      @Override
      public void paint(Graphics g) {
        super.paint(g);
        g.setColor(new Color(0, 0, 0, 0));
        g.fillRect(0, 0, getWidth(), getHeight());
        double width = getWidth() / 256.;
        for (int i = 0; i < 255; i++) {
          Color col = new Color(sColorModel.getRed(i), sColorModel.getGreen(i), sColorModel.getBlue(i));
          g.setColor(col);
          g.fillRect((int) Math.floor(i * width), 0, (int) Math.ceil(width), getHeight());
        }
      }
    };
    hue.setBackground(new Color(0, 0, 0, 0));
    hue.setPreferredSize(new Dimension(200, 30));
    panel.add(hue, BorderLayout.NORTH);
    panel.add(new HaloLabel("Low density", 11, false), BorderLayout.WEST);
    panel.add(new HaloLabel("High density", 11, false), BorderLayout.EAST);
    panel.setOpaque(false);
    return panel;
  }

  @Override
  public void activate() {
    fMainPanel.getThemeManager().activateDensityTheme();
    UIUtil.fit(getView(), ATLANTIC_CITY);
  }

  public static IndexColorModel createDefaultColorModel(int alpha) {
    TLcdJetIndexColorModelFactory factory = new TLcdJetIndexColorModelFactory();
    factory.setBits(8);
    factory.setSize(256);
    factory.setBasicColor(0, new Color(0, 0, 255, 0));
    factory.setBasicColor(1, new Color(0, 0, 255, alpha));
    factory.setBasicColor(30, new Color(0, 255, 255, alpha));
    factory.setBasicColor(100, new Color(255, 255, 0, alpha));
    factory.setBasicColor(200, new Color(255, 200, 0, alpha));
    factory.setBasicColor(255, new Color(255, 50, 0, alpha));

    // Create the color model.
    return (IndexColorModel) factory.createColorModel();
  }
}

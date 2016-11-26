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
package samples.lightspeed.customization.style.thematic;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.io.IOException;

import javax.swing.JColorChooser;

import com.luciad.gui.ALcdAction;
import com.luciad.gui.TLcdColor;
import com.luciad.gui.TLcdIconFactory;
import com.luciad.view.lightspeed.layer.ILspLayerFactory;

import samples.common.SampleData;
import samples.lightspeed.common.LightspeedSample;
import samples.lightspeed.common.LspDataUtil;

/**
 * This sample demonstrates how to implement "thematic mapping", i.e. to adjust the style of an
 * object to that object's attributes.
 */
public class MainPanel extends LightspeedSample {
  private CountryStyler fCountryStyler;
  private CountryStyler fSelectedCountryStyler;

  private ILspLayerFactory createCountryLayerFactory() {
    fCountryStyler = new CountryStyler();
    fSelectedCountryStyler = new CountryStyler();
    fSelectedCountryStyler.setColorMin(TLcdColor.interpolate(fCountryStyler.getColorMin(), Color.RED, 0.5));
    fSelectedCountryStyler.setColorMax(TLcdColor.interpolate(fCountryStyler.getColorMax(), Color.RED, 0.5));
    return new LayerFactory(fCountryStyler, fSelectedCountryStyler);
  }

  @Override
  protected void createGUI() {
    super.createGUI();

    getToolBars()[0].addAction(new AChooseColorAction("Set lowest population color") {
      @Override
      public Color getCurrentColor() {
        return fCountryStyler.getColorMin();
      }

      @Override
      public void applyNewColor(Color aColor) {
        fCountryStyler.setColorMin(aColor);
        fSelectedCountryStyler.setColorMin(TLcdColor.interpolate(aColor, Color.RED, 0.5));
      }
    }, 7);
    getToolBars()[0].addSpace(8);

    getToolBars()[0].addAction(new AChooseColorAction("Set highest population color") {
      @Override
      public Color getCurrentColor() {
        return fCountryStyler.getColorMax();
      }

      @Override
      public void applyNewColor(Color aColor) {
        fCountryStyler.setColorMax(aColor);
        fSelectedCountryStyler.setColorMax(TLcdColor.interpolate(aColor, Color.RED, 0.5));

      }
    }, 9);
    getToolBars()[0].addSpace(10);
  }

  protected void addData() throws IOException {
    super.addData();
    ILspLayerFactory layerFactory = createCountryLayerFactory();
    LspDataUtil.instance().model(SampleData.COUNTRIES).layer(layerFactory).label("Countries by Population").addToView(getView()).fit();
  }

  private abstract class AChooseColorAction extends ALcdAction {
    private AChooseColorAction(String aName) {
      super(aName, TLcdIconFactory.create(TLcdIconFactory.GRADIENT_ICON));
      setShortDescription(aName);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      Color c = JColorChooser.showDialog(MainPanel.this, "Choose color", getCurrentColor());
      if (c != null) {
        applyNewColor(c);
      }
    }

    public abstract Color getCurrentColor();

    public abstract void applyNewColor(Color aColor);
  }

  public static void main(final String[] aArgs) {
    startSample(MainPanel.class, "Thematic mapping");
  }

}

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
package samples.lightspeed.demo.application.gui;

import static samples.lightspeed.demo.application.gui.ColorMapLegend.Orientation.BOTTOM_TO_TOP;
import static samples.lightspeed.demo.application.gui.ColorMapLegend.Orientation.RIGHT_TO_LEFT;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import com.luciad.util.ILcdInterval;
import com.luciad.util.TLcdColorMap;

/**
 * A component that displays the colors of a TLcdColorMap.
 */
public class ColorMapLegend extends JPanel {

  private final Orientation fOrientation;

  private TLcdColorMap fColorMap;

  public ColorMapLegend(TLcdColorMap aColorMap, Orientation aOrientation) {
    fColorMap = aColorMap;
    fOrientation = aOrientation;
    setBorder(BorderFactory.createLineBorder(Color.black, 1));

    setSize(new Dimension(240, 20));
    setPreferredSize(getSize());
  }

  protected void paintComponent(Graphics aGraphics) {
    super.paintComponent(aGraphics);

    if (fColorMap != null) {
      Insets insets = getInsets();
      int height = getHeight() - insets.bottom - insets.top;
      int width = getWidth() - insets.left - insets.right;

      aGraphics.fillRect(insets.left, insets.top, width, height);

      int max_pixel = fOrientation.isHorizontal() ? width : height;
      for (int pixel = 0; pixel < max_pixel; pixel++) {
        double level = pixel2Level(pixel, max_pixel);
        aGraphics.setColor(fColorMap.retrieveColor(level));
        if (fOrientation.isHorizontal()) {
          aGraphics.fillRect(insets.left + pixel, insets.top, 1, height);
        } else {
          aGraphics.fillRect(insets.left, insets.top + pixel, width, 1);
        }
      }
    }
  }

  public void setColorMap(TLcdColorMap aColorMap) {
    fColorMap = aColorMap;
    invalidate();
    validate();
    repaint();
  }

  private double pixel2Level(int aPixel, int aTotalPixelDelta) {
    //normalize pixel value
    if (aPixel < 0) {
      aPixel = 0;
    }
    if (aPixel >= aTotalPixelDelta) {
      aPixel = aTotalPixelDelta - 1;
    }

    //invert pixel when bottom to top or right to left orientation
    int pixel_height;
    if (fOrientation == BOTTOM_TO_TOP || fOrientation == RIGHT_TO_LEFT) {
      pixel_height = (aTotalPixelDelta - 1) - aPixel;
    } else {
      pixel_height = aPixel;
    }

    ILcdInterval levelInterval = fColorMap.getLevelInterval();
    double levelDelta = levelInterval.getMax() - levelInterval.getMin();
    double levelMin = levelInterval.getMin();
    double fraction = (double) pixel_height / (double) (aTotalPixelDelta - 1);
    return (fraction * levelDelta) + levelMin;
  }

  public enum Orientation {

    LEFT_TO_RIGHT(true),
    RIGHT_TO_LEFT(true),
    BOTTOM_TO_TOP(false),
    TOP_TO_BOTTOM(false);

    private final boolean fHorizontal;

    Orientation(boolean aHorizontal) {
      fHorizontal = aHorizontal;
    }

    public boolean isHorizontal() {
      return fHorizontal;
    }

  }

}

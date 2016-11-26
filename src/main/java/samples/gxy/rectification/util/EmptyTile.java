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
package samples.gxy.rectification.util;

import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.ColorModel;
import java.awt.image.MemoryImageSource;

import com.luciad.format.raster.ILcdTile;

/**
 * A dummy implementation of a raster tile.
 */
public class EmptyTile implements ILcdTile {

  private static final int WIDTH = 1;
  private static final int HEIGHT = 1;

  private int fPixelSize;
  private int fValue;

  private ColorModel fColorModel;

  public EmptyTile() {
    this(8, 0, null);
  }

  public EmptyTile(int aPixelSize, int aValue) {
    this(aPixelSize, aValue, null);
  }

  public EmptyTile(int aPixelSize, int aValue, ColorModel aColorModel) {
    fPixelSize = aPixelSize;
    fValue = aValue;
    fColorModel = aColorModel;
  }

  public int getWidth() {
    return WIDTH;
  }

  public int getHeight() {
    return HEIGHT;
  }

  public int getType() {
    return fPixelSize > 16 ? ILcdTile.INT :
           fPixelSize > 8 ? ILcdTile.SHORT :
           ILcdTile.BYTE;
  }

  public int getPixelSize() {
    return fPixelSize;
  }

  public ColorModel getColorModel() {
    return fColorModel;
  }

  public int retrieveValue(int aX, int aY) {
    return fValue;
  }

  public void setDefaultValue(int aValue) {
    fValue = aValue;
  }

  public long getDefaultValue() {
    return fValue;
  }

  public boolean isAllDefault() {
    return true;
  }

  public Image createImage() {
    return createImage(fColorModel);
  }

  public Image createImage(ColorModel aColorModel) {
    return createImage(0, 0, WIDTH, HEIGHT, aColorModel);
  }

  public Image createImage(int aX, int aY,
                           int aWidth, int aHeight) {
    return createImage(aX, aY, aWidth, aHeight, fColorModel);
  }

  public Image createImage(int aX, int aY,
                           int aWidth, int aHeight,
                           ColorModel aColorModel) {

    return Toolkit.getDefaultToolkit().createImage(
        fPixelSize <= 8 ?
        new MemoryImageSource(WIDTH, HEIGHT, aColorModel, new byte[]{(byte) fValue}, 0, WIDTH) :
        new MemoryImageSource(WIDTH, HEIGHT, aColorModel, new int[]{fValue}, 0, WIDTH));
  }
}


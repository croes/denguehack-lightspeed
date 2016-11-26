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
package samples.lightspeed.demo.application.data.osm;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;

import com.luciad.gui.ILcdResizeableIcon;

/**
 *
 */
public class IconHalo implements ILcdResizeableIcon {

  private ILcdResizeableIcon fResizeableIconDelegate;
  private BufferedImage fGlow;
  private int fPadding = 1;

  public IconHalo(ILcdResizeableIcon aResizeableIconDelegate) {
    fResizeableIconDelegate = aResizeableIconDelegate;
  }

  @Override
  public void setIconWidth(int aWidth) {
    fGlow = null;
    fResizeableIconDelegate.setIconWidth(aWidth);
    fPadding = calcPadding();
  }

  @Override
  public void setIconHeight(int aHeight) {
    fGlow = null;
    fResizeableIconDelegate.setIconHeight(aHeight);
    fPadding = calcPadding();
  }

  private int calcPadding() {
    int maxAxis = Math.max(fResizeableIconDelegate.getIconWidth(), fResizeableIconDelegate.getIconHeight());
    return (int) Math.max(1, maxAxis * 0.1);
  }

  @Override
  public void paintIcon(Component c, Graphics g, int x, int y) {
    if (fGlow == null) {
      BufferedImage iconPreview = new BufferedImage(this.getIconWidth(), this.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
      Graphics glowGraphics = iconPreview.getGraphics();
      fResizeableIconDelegate.paintIcon(c, glowGraphics, fPadding, fPadding);
      fGlow = createGlowMask(iconPreview);
    }

    Graphics2D g2d = (Graphics2D) g;
    Object lerp = g2d.getRenderingHint(RenderingHints.KEY_INTERPOLATION);
    if (lerp == null) {
      lerp = RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR;
    }
    g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
    g2d.drawImage(fGlow, x, y, null);
    g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, lerp);
    //delegate
    fResizeableIconDelegate.paintIcon(c, g, x + fPadding, y + fPadding);
  }

  private BufferedImage createGlowMask(BufferedImage image) {
    BufferedImage mask = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB_PRE);

    Graphics2D g2d = mask.createGraphics();
    g2d.drawImage(image, 0, 0, null);
    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_IN, 1.0f));
    g2d.setColor(Color.white);
    g2d.fillRect(0, 0, image.getWidth(), image.getHeight());
    g2d.dispose();
    BufferedImage tempImage = new BufferedImage(mask.getWidth(), mask.getHeight(), BufferedImage.TYPE_INT_ARGB);
    int samplinggrid = Math.max(2, Math.min((int) Math.ceil(image.getWidth() / 10), 32));
    float[] blurKernel = createBlurKernel(samplinggrid);
    BufferedImageOp blur = new ConvolveOp(new Kernel(samplinggrid, samplinggrid, blurKernel));
    blur.filter(mask, tempImage);
    blur.filter(tempImage, mask);
    return mask;
  }

  private float[] createBlurKernel(int i) {
    float[] floats = new float[i * i];
    for (int j = 0; j < floats.length; j++) {
      floats[j] = 1f / (i * i);
    }
    return floats;
  }

  @Override
  public int getIconWidth() {
    return (int) (fResizeableIconDelegate.getIconWidth()) + fPadding * 2;
  }

  @Override
  public int getIconHeight() {
    return (int) (fResizeableIconDelegate.getIconHeight()) + fPadding * 2;
  }

  @Override
  public Object clone() {
    try {
      IconHalo clone = (IconHalo) super.clone();
      clone.fResizeableIconDelegate = (ILcdResizeableIcon) fResizeableIconDelegate.clone();
      clone.fGlow = null;
      return clone;
    } catch (CloneNotSupportedException e) {
      throw new RuntimeException(e);
    }
  }
}

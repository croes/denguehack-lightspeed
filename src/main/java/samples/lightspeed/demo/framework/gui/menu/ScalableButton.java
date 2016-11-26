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
package samples.lightspeed.demo.framework.gui.menu;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JButton;

import com.luciad.gui.ILcdAction;
import com.luciad.gui.ILcdIcon;
import com.luciad.gui.swing.TLcdSWAction;
import com.luciad.view.animation.ALcdAnimationManager;
import com.luciad.view.animation.ILcdAnimation;

import samples.lightspeed.demo.framework.application.Framework;
import samples.lightspeed.demo.framework.data.IOUtil;

/**
 * An image button whose image is enlarged on mouse over.
 */
public class ScalableButton extends JButton implements MouseListener, ILcdAnimation {

  private static final int DEFAULT_START_SIZE = 96;

  private BufferedImage fImage;
  private String fText;
  private int fCurrentWidth;
  private int fCurrentHeight;
  private double fDuration = 0.1;
  private double fCurrentTime = 0;
  private boolean fExpanding = false;
  private Color fBgColor;
  private int fStartSize;
  private Map<Integer, Image> fScaledImages = new HashMap<Integer, Image>();

  private final Color fHaloColor = new Color(0f, 0f, 0f, 0.9f);
  private final Color fTextColor = new Color(1f, 1f, 1f, 0.9f);

  public static ScalableButton createButton(String aImgName, String aDisplayName) throws IOException {
    String imagePath = Framework.getInstance().getProperty("button.icon.path");
    BufferedImage img = IOUtil.readImage(imagePath, aImgName);
    return new ScalableButton(img, aDisplayName);
  }

  /**
   *
   * @param aAction The action. Make sure the {@link ILcdAction#SMALL_ICON} is set, as this will be used as icon
   * @return
   */
  public static ScalableButton createButton(final ILcdAction aAction, String aDisplayName ) {
    ILcdIcon icon = (ILcdIcon) aAction.getValue(ILcdAction.SMALL_ICON);
    BufferedImage bufferedImage = new BufferedImage(icon.getIconWidth(), icon.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
    icon.paintIcon(null, bufferedImage.getGraphics(), 0, 0);

    final ScalableButton scalableButton = new ScalableButton(bufferedImage, aDisplayName);
    scalableButton.setAction(new TLcdSWAction(aAction));

    aAction.addPropertyChangeListener(new PropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(ILcdAction.VISIBLE)) {
          scalableButton.setVisible((Boolean) evt.getNewValue());
        }
      }
    });
    Boolean visible = (Boolean) aAction.getValue(ILcdAction.VISIBLE);
    scalableButton.setVisible(visible != null ? visible : true);
    return scalableButton;
  }

  private ScalableButton(BufferedImage aImage, String aText) {
    fImage = aImage;
    fText = aText;
    fStartSize = (int) (DEFAULT_START_SIZE / 2.75);
    fCurrentWidth = fStartSize;
    fCurrentHeight = fStartSize;
    setFont(new Font(getFont().getName(), getFont()
        .getStyle(), (int) (DEFAULT_START_SIZE * 0.15)));
    setBgColor(new Color(1, 1, 1, 0));
    setForeground(Color.white);
    addMouseListener(this);
    setScaledImages();
  }

  private void updateImage( BufferedImage aImage ){
    fImage = aImage;
    fScaledImages.clear();
    revalidate();
    repaint();
  }

  @Override
  public Dimension getPreferredSize() {
    return new Dimension(DEFAULT_START_SIZE, DEFAULT_START_SIZE);
  }

  public void setBgColor(Color aBgColor) {
    fBgColor = aBgColor;
  }

  @Override
  public String getText() {
    return fText;
  }

  private void setScaledImages() {
    for (int i = fStartSize; i <= 2 * fStartSize; i++) {
      fScaledImages.put(i, fImage.getScaledInstance(i, i, BufferedImage.SCALE_SMOOTH));
    }
  }

  public void setBackgroundColor(Color aColor) {
    fBgColor = aColor;
  }

  @Override
  protected void paintComponent(Graphics g) {
    Graphics2D g2d = (Graphics2D) g;

    // Draw background color of menu
    g2d.setColor(fBgColor);
    g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);

    // paint image
    int locX = getWidth() / 2 - fCurrentWidth / 2;
    int locY = getHeight() / 2 - fCurrentHeight / 2;

    // Paint scaled image
    Image scaledImage = fScaledImages.get(fCurrentWidth);
    if (scaledImage == null) {
      scaledImage = fImage
          .getScaledInstance(fCurrentWidth, fCurrentHeight, BufferedImage.SCALE_SMOOTH);
      fScaledImages.put(fCurrentWidth, scaledImage);
    }
    g2d.drawImage(scaledImage, locX, locY, null);

    // Calculate position of text
    locX = getWidth() / 2 - getFontMetrics(getFont()).stringWidth(fText) / 2;
    locY = locY + fCurrentHeight + 10;

    // paint halo 8 times (N, NW, W, SW, S, ...)
    g2d.setColor(fHaloColor);
    g2d.drawString(fText, locX + 1, locY);     // east
    g2d.drawString(fText, locX + 1, locY + 1);   // south-east
    g2d.drawString(fText, locX, locY + 1);     // south
    g2d.drawString(fText, locX - 1, locY + 1);   // south-west
    g2d.drawString(fText, locX - 1, locY);     // west
    g2d.drawString(fText, locX - 1, locY - 1);   // north-west
    g2d.drawString(fText, locX, locY - 1);     // north
    g2d.drawString(fText, locX + 1, locY - 1);   // north-east

    // paint text
    g2d.setColor(fTextColor);
    g2d.drawString(fText, locX, locY);
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////
  // Mouse Listener Interface

  public void mouseClicked(MouseEvent e) {
    // Do nothing
  }

  public void mousePressed(MouseEvent e) {
    // do nothing
  }

  public void mouseReleased(MouseEvent e) {
    // do nothing
  }

  public void mouseEntered(MouseEvent e) {
    expand();
  }

  public void mouseExited(MouseEvent e) {
    retract();
  }

  public void expand() {
    if (fExpanding) {
      return;
    }
    fExpanding = true;
    ALcdAnimationManager.getInstance().putAnimation(this, this);
  }

  public void retract() {
    if (!fExpanding) {
      return;
    }
    fExpanding = false;
    ALcdAnimationManager.getInstance().putAnimation(this, this);
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////
  // ILcdAnimation Interface

  public double getDuration() {
    return fDuration;
  }

  public void start() {
    fCurrentWidth = fStartSize;
    fCurrentTime = 0;
  }

  public void stop() {
  }

  public boolean isLoop() {
    return false;
  }

  public void restart() {
  }

  public synchronized void setTime(double aTime) {
    fCurrentTime = aTime;
    if (fExpanding) {
      fCurrentWidth = (int) (fStartSize + (fCurrentTime / fDuration) * fStartSize);
      fCurrentHeight = (int) (fStartSize + (fCurrentTime / fDuration) * fStartSize);
      fCurrentWidth = Math.min(getWidth(), fCurrentWidth);
      fCurrentHeight = Math.min(getHeight(), fCurrentHeight);
    } else {
      fCurrentWidth = (int) (2 * fStartSize - (fCurrentTime / fDuration) * fStartSize);
      fCurrentHeight = (int) (2 * fStartSize - (fCurrentTime / fDuration) * fStartSize);
      fCurrentWidth = Math.max(fStartSize, fCurrentWidth);
      fCurrentHeight = Math.max(fStartSize, fCurrentHeight);
    }
    repaint();
  }

}

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
package samples.decoder.kml22.common.modelcontenttree;

import com.luciad.gui.ILcdIcon;

import javax.swing.Icon;
import java.awt.Component;
import java.awt.Graphics;

/**
 * <p>Implementation of <code>ILcdIcon</code> that uses the decorator design pattern to
 * animate another list of given <code>ILcdIcon</code>s.</p>
 *
 * <p>For convenience, is also implements the Swing <code>Icon</code> interface.</p>
 *
 * <p>This icon is not designed to be used in combination with a
 * {@link com.luciad.view.gxy.painter.TLcdGXYIconPainter TLcdGXYIconPainter}.</p>
 *
 * @since 9.0
 */
public class AnimatedIcon implements ILcdIcon, Cloneable, Icon {
  private ILcdIcon[] fIconsToAnimate;
  private int fWidth = -1;
  private int fHeight = -1;
  private int fIndex = 0;



  /**
   * Creates an animated icon
   * @param aIconsToAnimate The icons to animate.
   */
  public AnimatedIcon( ILcdIcon[] aIconsToAnimate ) {
    fIconsToAnimate = aIconsToAnimate;
  }

  public int getIconWidth() {
    if ( fWidth==-1 ) {
      for ( ILcdIcon icon : fIconsToAnimate ) {
        fWidth = Math.max(icon.getIconWidth(),fWidth);
      }
    }
    return fWidth;
  }

  public int getIconHeight() {
    if ( fHeight==-1 ) {
      for ( ILcdIcon icon : fIconsToAnimate ) {
        fHeight = Math.max(icon.getIconWidth(),fHeight);
      }
    }
    return fHeight;
  }

  public Object clone() {
    try {
      return super.clone();
    } catch ( CloneNotSupportedException e ) {
      //Can't happen
      throw new RuntimeException( e );
    }
  }

  public void paintIcon( final Component aComponent, Graphics aGraphics, int aX, int aY ) {
    fIconsToAnimate[fIndex].paintIcon( aComponent, aGraphics, aX, aY );
  }

  /**
   * Resets the animation.
   */
  private void reset() {
    fIndex = 0;
  }

  /**
   * Sets the next icon for the animation
   */
  public void nextIcon( ) {
    fIndex++;
    if(fIndex>fIconsToAnimate.length-1){
      reset();
    }
  }
}


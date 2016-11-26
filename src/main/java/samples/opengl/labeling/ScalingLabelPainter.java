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
package samples.opengl.labeling;

import com.luciad.shape.ILcdPoint;
import com.luciad.view.opengl.*;
import com.luciad.view.opengl.binding.ILcdGLDrawable;
import com.luciad.view.opengl.painter.*;

import java.awt.*;

/**
 * An extension of TLcdGLLabelPainter that decreases the label's font size with
 * distance.
 */
class ScalingLabelPainter extends TLcdGLLabelPainter {

  private TLcdGLFont fFonts[];

  public ScalingLabelPainter( Font aFont ) {
    super();

    String name = aFont.getName();
    int style = aFont.getStyle();

    // Given the AWT font aFont, create TLcdGLFonts that represent different font sizes.
    fFonts = new TLcdGLFont[36];
    for ( int i = 0; i < fFonts.length ; i++ ) {
      fFonts[ i ] = new TLcdGLFont( new Font( name, style, i + 1 ) );
    }
  }

  protected String[] retrieveLabel( ILcdGLDrawable aGLDrawable, Object aObject, ILcdGLPaintMode aMode, ILcdGLContext aContext ) {
    return new String[] { aContext.getLayer().getLabel() };
  }

  protected ILcdPoint retrieveLabelPosition( ILcdGLDrawable aGLDrawable, Object aObject, ILcdGLPaintMode aMode, ILcdGLContext aContext ) {
    ILcdPoint p = super.retrieveLabelPosition( aGLDrawable, aObject, aMode, aContext );
    if ( p == null ) return null;

    ILcdGLCamera camera = aContext.getView().getCamera();
    ILcdPoint eyePoint = camera.getEyePoint();
    double x = eyePoint.getX() - p.getX();
    double y = eyePoint.getY() - p.getY();
    double z = eyePoint.getZ() - p.getZ();
    double distance = Math.sqrt( x * x + y * y + z * z );

    /* We choose the font size as a linear function of the distance between the
      label and the camera. Other criteria can of course be used as well, e.g.
      the user's screen resolution. */
    int font = (int) Math.round( distance / 5 );
    if ( font < 0 ) font = 0;
    else if ( font >= fFonts.length ) font = fFonts.length - 1;

    setFont( fFonts[ fFonts.length - font - 1 ] );

    return p;
  }
}

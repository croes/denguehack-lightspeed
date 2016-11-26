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

import com.luciad.reference.TLcdGridReference;
import com.luciad.shape.shape2D.TLcdXYBounds;
import com.luciad.view.ILcdXYZWorldReference;
import com.luciad.view.opengl.ILcdGLLayerFactory;
import com.luciad.view.opengl.ILcdGLView;
import com.luciad.view.opengl.TLcdGLTextOverlay;
import com.luciad.view.opengl.TLcdGLViewCanvas;
import com.luciad.view.opengl.action.TLcdGLViewFitAction;
import samples.opengl.common.Abstract3DPanel;

import java.awt.Color;
import java.awt.Font;

/**
 * The main panel of the first sample application.
 */
class MainPanel extends Abstract3DPanel {

  private static final float UNIT_OF_MEASURE = 10000f;

  public ILcdGLLayerFactory getGLLayerFactory() {
    return new LayerFactory(UNIT_OF_MEASURE);
  }

  protected TLcdGLViewCanvas createCanvas() {
    TLcdGLViewCanvas canvas = super.createCanvas();
    ILcdXYZWorldReference world_reference = canvas.getXYZWorldReference();
    if ( world_reference instanceof TLcdGridReference ) {
      ((TLcdGridReference)world_reference).setUnitOfMeasure( UNIT_OF_MEASURE );
    }
    Abstract3DPanel.setupCamera( canvas );
    Abstract3DPanel.setupSkybox( canvas );
    setupOverlayText( canvas );
    return canvas;
  }

  protected void addData() {
    ILcdGLView canvas = getCanvas();
    canvas.addModel( ModelFactory.createGridModel    () );
    canvas.addModel( ModelFactory.createWorldModel   () );
    canvas.addModel( ModelFactory.createEllipseModel () );
    canvas.addModel( ModelFactory.createPointModel   () );
    canvas.addModel( ModelFactory.createPolylineModel() );

    TLcdGLViewFitAction fit = new TLcdGLViewFitAction( canvas );
    fit.setBounds( new TLcdXYBounds( -40, -40, 80, 80 ) );
    fit.fit();
  }

  /**
   * Sets up the overlay text.
   * @param aCanvas
   */
  public static void setupOverlayText( TLcdGLViewCanvas aCanvas ) {
    TLcdGLTextOverlay textOverlay = new TLcdGLTextOverlay(
        new String[]{"Overlay", "Text label"},
        new Font("Helvetica", Font.BOLD, 16),
        Color.white,
        new Color(0,0,0,127),
        new Color(204,127,127,255)
    );
    textOverlay.setPosition( 15, 95 );
    textOverlay.setBorderWidth( 5 );
    aCanvas.addViewListener( textOverlay );
  }
}

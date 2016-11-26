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
package samples.opengl.printing;

import com.luciad.gui.TLcdPrintComponentAction;
import com.luciad.view.opengl.*;
import samples.opengl.common.Abstract3DPanel;

/**
 * The main panel of the first sample application.
 */
class MainPanel extends Abstract3DPanel {

  public ILcdGLLayerFactory getGLLayerFactory() {
    return new LayerFactory();
  }

  protected TLcdGLViewCanvas createCanvas() {
    TLcdGLViewCanvas canvas = super.createCanvas();
    Abstract3DPanel.setupCamera( canvas );
    Abstract3DPanel.setupLights( canvas );
    Abstract3DPanel.setupFog   ( canvas );
    ILcdGLCamera camera = canvas.getCamera();
    camera.setAltitudeExaggerationFactor( 10 );
    return canvas;
  }

  protected void createGUI() {
    super.createGUI();

    // Install a printing function in the 3D view.
    TLcdGLTilingPrintingFunction function = new TLcdGLTilingPrintingFunction();

    // Increase the resolution with a factor 4. This means that a 3D view of
    // 200x100 pixels on screen, will use 800x400 pixels when printed.
    // In general, the scale should take the target DPI and size of the
    // print into account to get an optimal quality.
    function.setResolutionScale( 4 );
    getCanvas().setPrintingFunction( function );

    // Once the printing function is set for the 3D view, it can be printed
    // as any other component. We reuse a standard luciad printing action,
    // which displays printer and page setup dialogs when necessary. The complete
    // panel including the controls will be printed, since 'this' is supplied to
    // the print action. To print just the 3D view, we would have to supply
    // 'canvas' instead.
    TLcdPrintComponentAction core_print_action = new TLcdPrintComponentAction( this );

    // Layered rendering is not necessary for the 3D view.
    core_print_action.setLayeredRendering( false );
    core_print_action.setForceLayeredRendering( false );
    getToolbar().addAction( core_print_action );
  }

  protected void addData() {
    ILcdGLView canvas = getCanvas();
    canvas.addModel( ModelFactory.createGridModel   () );
    canvas.addModel( ModelFactory.createEllipseModel() );
  }
}

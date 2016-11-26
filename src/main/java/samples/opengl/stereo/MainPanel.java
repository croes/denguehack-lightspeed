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
package samples.opengl.stereo;

import com.luciad.geodesy.TLcdGeodeticDatum;
import com.luciad.projection.TLcdEquidistantCylindrical;
import com.luciad.reference.TLcdGridReference;
import com.luciad.view.opengl.*;
import com.luciad.view.opengl.action.TLcdGLViewFitAction;
import com.luciad.view.opengl.binding.*;
import samples.opengl.common.Abstract3DPanel;

import java.awt.*;

/**
 * The main panel of the sample application.
 */
class MainPanel extends Abstract3DPanel {

  private static final Color BACKGROUND_COLOR = new Color( 123, 174, 231 );

  private static final ObjectIconProvider fIconProvider = new ObjectIconProvider();

  public ILcdGLLayerFactory getGLLayerFactory() {
    LayerFactory layer_factory = new LayerFactory();
    layer_factory.setIconProvider( fIconProvider );
    return layer_factory;
  }

  protected TLcdGLViewCanvas createCanvas() {
    // Create an ILcdGLCapabilities and set its stereo flag.
    ILcdGLCapabilities capabilities =
            ALcdGLBinding.getInstance().getGLDrawableFactory().createGLCapabilities();
    capabilities.setStereo( true );

    // Now create an ILcdGLView with the given capabilities.
    TLcdGLViewCanvasStereo canvas = new TLcdGLViewCanvasStereo( capabilities );
    // Set default values for eye separation and focal length.
    canvas.setEyeSeparation( 500 );
    canvas.setFocalLength( 30000 );
    TLcdGridReference world_reference = new TLcdGridReference();
    world_reference.setGeodeticDatum( new TLcdGeodeticDatum() );
    world_reference.setProjection( new TLcdEquidistantCylindrical() );
    canvas.setXYZWorldReference( world_reference );
    canvas.setLayerFactory( getGLLayerFactory() );
    canvas.setBackground( BACKGROUND_COLOR );
    Abstract3DPanel.setupCamera( canvas );
    Abstract3DPanel.setupLights( canvas );
    return canvas;
  }

  protected void createGUI() {
    super.createGUI();
    setComponentNorthEast( new StereoControlPanel( (TLcdGLViewCanvasStereo)getCanvas() ) );
  }

  protected void addData() {
    ILcdGLView canvas = getCanvas();
    canvas.addModel( ModelFactory.createGridModel () );
    canvas.addModel( ModelFactory.createPointModel() );

    TLcdGLViewFitAction fit = new TLcdGLViewFitAction( canvas );
    fit.setLayer( (ILcdGLLayer) canvas.getLayer( 1 ) );
    fit.fit();
  }
}

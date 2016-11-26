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
package samples.lightspeed.integration.gl;

import java.io.IOException;

import com.luciad.model.ILcdModel;
import com.luciad.view.lightspeed.ILspAWTView;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.layer.integration.gl.TLspGLLayerAdapter;
import com.luciad.view.lightspeed.layer.integration.gl.TLspGLLayerTreeNodeAdapter;
import com.luciad.view.opengl.TLcdGLLayer;
import com.luciad.view.opengl.TLcdGLLayerTreeNode;

import samples.lightspeed.common.LightspeedSample;

/**
 * This sample demonstrates how to visualize ILcdGLLayer instances in a Lightspeed 3D
 * view. This is achieved using TLspGLLayerAdapter, which paints the model of the ILcdGLLayer in
 * the Lightspeed 3D view using the layer's ILcdGLPainter. This assures pixel-by-pixel
 * correspondence between visualizations with ILcdGLView and ILspView.
 */
public class MainPanel extends LightspeedSample {

  @Override
  protected ILspAWTView createView() {
    return super.createView( ILspView.ViewType.VIEW_3D );
  }

  /**
   * In this sample we create Lightspeed layers by wrapping ILcdGLLayer
   * instances in TLspGLLayerAdapter layers.
   * @see GLLayerAdapterFactory
   */
  protected void addData() throws IOException {
    super.addData();

    samples.lightspeed.integration.gl.ModelFactory modelfactory = new ModelFactory();

    ILcdModel ellipseModel = modelfactory.createModel( ModelFactory.ELLIPSE_MODEL_TYPE_NAME,"Ellipses" );
    ILcdModel point3dModel = modelfactory.createModel( ModelFactory.POINT3D_MODEL_TYPE_NAME, "3D Points" );
    ILcdModel point2dModel = modelfactory.createModel( ModelFactory.POINT2D_MODEL_TYPE_NAME, "2D Points" );
    ILcdModel worldModel = modelfactory.createModel( ModelFactory.SHP_MODEL_TYPE_NAME, "Data/Shp/World/world.shp" );
    ILcdModel arcBandModel = modelfactory.createModel( ModelFactory.ARCBAND_MODEL_TYPE_NAME, "Arcband" );
    ILcdModel complexPolyModel = modelfactory.createModel(ModelFactory.COMPLEXPOLY_MODEL_TYPE_NAME,"Complex polygon");
    ILcdModel llhModel = modelfactory.createModel(ModelFactory.LLH_BUFFER_MODEL_TYPE_NAME,"Lonlatheightbuffer");
    ILcdModel llModel = modelfactory.createModel(ModelFactory.LL_BUFFER_MODEL_TYPE_NAME,"Lonlatbuffer");
    ILcdModel sphereModel = modelfactory.createModel(ModelFactory.SPHERE_MODEL_TYPE_NAME,"Sphere");
    ILcdModel domeModel = modelfactory.createModel(ModelFactory.DOME_MODEL_TYPE_NAME,"Dome");
    ILcdModel circleModel = modelfactory.createModel(ModelFactory.CIRCLE_MODEL_TYPE_NAME,"Circle");
    ILcdModel polygonModel = modelfactory.createModel(ModelFactory.POLYGON_MODEL_TYPE_NAME,"Polygon");

    GLLayerFactory glLayerFactory = new GLLayerFactory();

    TLcdGLLayerTreeNode ellipseLayer = new TLcdGLLayerTreeNode();
    glLayerFactory.initLayerSFCT( ellipseModel, ellipseLayer );
    TLcdGLLayerTreeNode arcBandLayer = new TLcdGLLayerTreeNode();
    glLayerFactory.initLayerSFCT( arcBandModel, arcBandLayer );
    TLcdGLLayerTreeNode complexPolyLayer = new TLcdGLLayerTreeNode();
    glLayerFactory.initLayerSFCT( complexPolyModel, complexPolyLayer );
    TLcdGLLayerTreeNode sphereLayer = new TLcdGLLayerTreeNode();
    glLayerFactory.initLayerSFCT( sphereModel, sphereLayer );

    TLcdGLLayer LLHBufferLayer = new TLcdGLLayer();
    glLayerFactory.initLayerSFCT( llhModel, LLHBufferLayer );
    TLcdGLLayer LLBufferLayer = new TLcdGLLayer();
    glLayerFactory.initLayerSFCT( llModel, LLBufferLayer );
    TLcdGLLayer polygonLayer = new TLcdGLLayer();
    glLayerFactory.initLayerSFCT( polygonModel, polygonLayer );
    TLcdGLLayer circleLayer = new TLcdGLLayer();
    glLayerFactory.initLayerSFCT( circleModel, circleLayer );
    TLcdGLLayer domeLayer = new TLcdGLLayer();
    glLayerFactory.initLayerSFCT( domeModel, domeLayer );

    ellipseLayer.addLayer( arcBandLayer );
    ellipseLayer.addLayer( complexPolyLayer );
    arcBandLayer.addLayer( LLBufferLayer );
    arcBandLayer.addLayer( LLHBufferLayer );
    complexPolyLayer.addLayer( polygonLayer );
    sphereLayer.addLayer( circleLayer );

    TLcdGLLayer worldLayer = new TLcdGLLayer();
    glLayerFactory.initLayerSFCT( worldModel, worldLayer );
    TLcdGLLayer point3DLayer = new TLcdGLLayer();
    glLayerFactory.initLayerSFCT( point3dModel, point3DLayer );
    TLcdGLLayer point2DLayer = new TLcdGLLayer();
    glLayerFactory.initLayerSFCT( point2dModel, point2DLayer );

    GLLayerAdapterFactory layerAdapterFactory = new GLLayerAdapterFactory();

    TLspGLLayerAdapter lspSphereLayer = layerAdapterFactory.createGLAdapter( sphereLayer );
    TLspGLLayerAdapter lspDomeLayer = layerAdapterFactory.createGLAdapter( domeLayer );
    ((TLspGLLayerTreeNodeAdapter )lspSphereLayer).addLayer( lspDomeLayer );

    TLspGLLayerAdapter lspEllipseLayer = layerAdapterFactory.createGLAdapter( ellipseLayer );
    TLspGLLayerAdapter lspWorldLayer = layerAdapterFactory.createGLAdapter( worldLayer );

    TLspGLLayerTreeNodeAdapter lspPointsLayer = new TLspGLLayerTreeNodeAdapter(new TLcdGLLayerTreeNode());
    lspPointsLayer.setLabel( "Points" );
    lspPointsLayer.addLayer( new GLLayerAdapterFactory().createGLAdapter( point2DLayer ) );
    lspPointsLayer.addLayer( new GLLayerAdapterFactory().createGLAdapter( point3DLayer ) );

    getView().addLayer( lspEllipseLayer );
    getView().addLayer( lspWorldLayer );
    getView().addLayer( lspSphereLayer );
    getView().addLayer( lspPointsLayer );
  }

  public static void main( final String[] aArgs ) {
    startSample(MainPanel.class, "GL Layer Adapter");
  }
}

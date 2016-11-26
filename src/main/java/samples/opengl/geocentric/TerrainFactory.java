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
package samples.opengl.geocentric;

import com.luciad.format.raster.terrain.TLcdTerrainModelDecoder;
import com.luciad.format.raster.terrain.opengl.TLcdGLTerrainPainter;
import com.luciad.format.raster.terrain.opengl.paintable.TLcdGLTerrainPaintableFactory;
import com.luciad.gui.TLcdAWTUtil;
import com.luciad.model.ILcdModel;
import com.luciad.reference.ILcdGeocentricReference;
import com.luciad.view.opengl.ILcdGLView;
import com.luciad.view.opengl.TLcdGLLayer;
import samples.opengl.common.GLViewSupport;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

/**
 * 3D terrain layer factory for the geocentric sample application. If a
 * TLcdGLAboveGroundPointlistPainter is supplied to the constructor, this class
 * will link that painter to the 3D terrain it creates.
 */
class TerrainFactory {

  private TLcdGLTerrainPaintableFactory fPaintableFactory = new TLcdGLTerrainPaintableFactory();
  private TLcdGLTerrainPainter fPainter = new TLcdGLTerrainPainter();

  public TerrainFactory() {
    fPainter.setPaintableFactory( fPaintableFactory );
  }

  private ILcdModel createTerrainModel( String aSourceName ) throws IOException {
    // Load the preprocessed terrain model from the specified source.
    TLcdTerrainModelDecoder dec = new TLcdTerrainModelDecoder();
    return dec.decode( aSourceName );
  }

  /**
   * Loads 3D terrain from the specified source and adds it to the given view.
   */
  public void addTerrainToView( final ILcdGLView aView, String aSourceName ) throws IOException {
    // If the world reference is geocentric, check for vertex shader support.
    if (aView.getXYZWorldReference() instanceof ILcdGeocentricReference) {
      String extensions = aView.getGLInformation().getProperty("gl.extensions", "");
      if (extensions.indexOf("GL_ARB_vertex_shader") < 0) {
        SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            Frame parentFrame = TLcdAWTUtil.findParentFrame( aView );
            JOptionPane.showMessageDialog(
                    parentFrame,
                    "Cannot create terrain. To display 3D terrain in a geocentric world reference, a vertex shader capable graphics card is required."
                  );
          }
        });
        return;
      }
    }

    // Load the terrain
    ILcdModel terrain_model = createTerrainModel( aSourceName );
    // Put it on a layer
    TLcdGLLayer layer = new TLcdGLLayer( terrain_model );
    layer.setPathFactory(GLViewSupport.createPathFactory(terrain_model.getModelReference()));
    layer.setPainter( fPainter );
    layer.setSelectable(false);
    // Add the layer to the view
    aView.addLayer( layer );
  }
}

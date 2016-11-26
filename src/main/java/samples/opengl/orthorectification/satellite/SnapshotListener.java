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
package samples.opengl.orthorectification.satellite;

import com.luciad.geodesy.*;
import com.luciad.projection.*;
import com.luciad.reference.*;
import com.luciad.shape.ILcdPoint;
import com.luciad.shape.shape3D.*;
import com.luciad.view.opengl.*;
import org.hiranabe.vecmath.Vector3d;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.RenderedImage;
import java.io.*;

/**
 * This ActionListener saves the image of its 3D view as a TIFF image with a perspective reference.
 */
public class SnapshotListener implements ActionListener {

  private static final int IMAGE_SCALE = 4;

  private static final String TIF_EXTENSION = "tif";
  private static final String TFW_EXTENSION = "tfw";
  private static final String REF_EXTENSION = "ref";


  private TLcdGLViewCanvas fViewCanvas;


  public SnapshotListener( TLcdGLViewCanvas aViewCanvas ) {
    fViewCanvas = aViewCanvas;
  }


  public void actionPerformed( ActionEvent e ) {

    JFileChooser fileChooser = new JFileChooser();
    fileChooser.setFileFilter( new TifFileFilter() );
    if ( fileChooser.showSaveDialog( null ) == JFileChooser.APPROVE_OPTION ) {
      File tifFile = fileChooser.getSelectedFile();
      String baseFilename;
      try {
        String tifFilePath = tifFile.getCanonicalPath();
        int index = tifFilePath.lastIndexOf( "." );
        if ( index == -1 ) {
          baseFilename = tifFilePath + ".";
        }
        else {
          baseFilename = tifFilePath.substring( 0, index + 1 );
        }
      } catch ( IOException e1 ) {
        return;
      }

      // Create an offscreen image at a higher resolution.

      TLcdGLViewOffScreen view_offscreen = null;
      for (int image_scale = IMAGE_SCALE; image_scale >= 1; image_scale /= 2) {
        try {
          view_offscreen = new TLcdGLViewOffScreen( image_scale * fViewCanvas.getWidth(),
                                   image_scale * fViewCanvas.getHeight() );
          break;
        } catch(Exception ex) {
          // continue
        }
      }

      if (view_offscreen == null) {
        JOptionPane.showMessageDialog(fViewCanvas, "Could not create snapshot, due to limited graphics card resources" ); 
        return;
      }

      view_offscreen.setAutoUpdate( false );
      view_offscreen.setXYZWorldReference( fViewCanvas.getXYZWorldReference() );
      view_offscreen.setBackground( fViewCanvas.getBackground() );
      view_offscreen.setCamera( fViewCanvas.getCamera() );
      view_offscreen.setLayerFactory( new LayerFactory() );

      for ( int index = 0; index < fViewCanvas.layerCount(); index++ )
        view_offscreen.addModel( fViewCanvas.getLayer( index ).getModel() );

      if (view_offscreen.getGLDrawable().canMakeCurrent()) {
        view_offscreen.getGLDrawable().makeCurrent();
      }
      view_offscreen.repaint();
      Image image = view_offscreen.getImage();
      view_offscreen.removeAllLayers();

      // Save the image as a TIFF file.
      File f = new File( baseFilename + TIF_EXTENSION );
      try {
        ImageIO.write( ( RenderedImage ) image, "tiff", f );
      } catch ( IOException e1 ) {
        e1.printStackTrace();
      }

      // Create a perspective projection and reference that corresponds to the
      // projection of the 3D view.
      ILcdGeodeticDatum datum = ( ( ILcdGeoReference ) view_offscreen.getXYZWorldReference() ).getGeodeticDatum();

      ILcdGLCamera camera = view_offscreen.getCamera();
      ILcdPoint camera_geocentric = camera.getEyePoint();
      ILcdPoint reference_geocentric = camera.getReferencePoint();
      Vector3d camera_dir = new Vector3d( reference_geocentric.getX() - camera_geocentric.getX(),
                                          reference_geocentric.getY() - camera_geocentric.getY(),
                                          reference_geocentric.getZ() - camera_geocentric.getZ() );
      camera_dir.normalize();
      Vector3d principal_pct = new Vector3d( camera_dir );
      double focal_length = view_offscreen.getHeight() / 2.0 /
                            Math.tan( Math.toRadians( camera.getFov() / 2.0 ) );
      principal_pct.scale( focal_length );

      TLcdXYZPoint principal_point = new TLcdXYZPoint( camera_geocentric );
      principal_point.translate3D( principal_pct.x,
                                   principal_pct.y,
                                   principal_pct.z );

      TLcdPerspectiveProjection persp = new TLcdPerspectiveProjection( camera_geocentric,
                                                                       principal_point,
                                                                       camera.getUpVector() );

      TLcdGridReference ref = new TLcdGridReference( datum, persp, 0.0, 0.0, 1.0, 1.0, 0.0 );

      // Encode the raster reference in a .ref file.
      TLcdModelReferenceEncoder encoder = new TLcdModelReferenceEncoder();
      try {
        encoder.save( ref, baseFilename + REF_EXTENSION );
      } catch ( IOException e1 ) {
        e1.printStackTrace();
      }

      // Set the raster position: top-left pixel and pixel scales.
      double scale_x = 1.0;
      double scale_y = -1.0;

      double translate_x = -view_offscreen.getWidth() / 2.0;
      double translate_y = view_offscreen.getHeight() / 2.0;

      // Encode the raster position in a .tfw file.
      try {
        PrintWriter out = new PrintWriter( new BufferedWriter( new FileWriter( baseFilename + TFW_EXTENSION ) ) );
        out.println( scale_x );
        out.println( 0.0 );
        out.println( 0.0 );
        out.println( scale_y );
        out.println( translate_x );
        out.println( translate_y );
        out.close();
      } catch ( IOException e1 ) {
        e1.printStackTrace();
      }

    }
  }

  private class TifFileFilter extends javax.swing.filechooser.FileFilter {

    public boolean accept( File aFile ) {

      if ( aFile.getName().endsWith( ".tif" ) || aFile.getName().endsWith( ".tiff" ) ||
           aFile.isDirectory() ) return true;
      return false;
    }

    public String getDescription() {
      return "tif files";
    }
  }
}

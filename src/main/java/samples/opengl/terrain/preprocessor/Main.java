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
package samples.opengl.terrain.preprocessor;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

import com.luciad.format.raster.terrain.preprocessor.ILcdTextureGenerator;
import com.luciad.format.raster.terrain.preprocessor.TLcdInterpolatingRasterElevationProvider;
import com.luciad.format.raster.terrain.preprocessor.TLcdTerrainPreprocessor;
import com.luciad.geodesy.TLcdGeodeticDatum;
import com.luciad.gui.TLcdAWTUtil;
import com.luciad.model.ILcdModel;
import com.luciad.projection.TLcdEquidistantCylindrical;
import com.luciad.reference.ILcdGeoReference;
import com.luciad.reference.TLcdGeodeticReference;
import com.luciad.reference.TLcdGridReference;
import com.luciad.shape.ILcdBounds;
import com.luciad.shape.shape2D.TLcdXYBounds;
import com.luciad.shape.shape3D.TLcdXYZBounds;
import com.luciad.transformation.ILcdModelModelTransformation;
import com.luciad.transformation.TLcdGeoReference2GeoReference;
import com.luciad.util.ILcdStatusListener;
import com.luciad.util.TLcdBuffer;
import com.luciad.util.TLcdNoBoundsException;
import com.luciad.util.TLcdStatusEvent;

/**
 * This sample illustrates the use of TLcdTerrainPreprocessor. The sample sets
 * up the preprocessor using configuration settings read from a small XML file.
 * An example configuration, terrain_sample.xml, is included.
 */
public class Main {

  private CompositeRasterModelDecoder fRasterDecoder;
  private TextureGeneratorFactory fTexGenFactory;
  private TLcdGridReference fWorldRef;
  private String fDestination;

  public Main() {
    /* Create the world reference to be used for the terrain data. It is
       important that this matches the world reference of the ILcdGLView that
       will later be used to display the data. */
    fWorldRef = new TLcdGridReference();
    fWorldRef.setGeodeticDatum(new TLcdGeodeticDatum());
    fWorldRef.setProjection(new TLcdEquidistantCylindrical());
    fWorldRef.setUnitOfMeasure(1.0f);

    fRasterDecoder = new CompositeRasterModelDecoder();
    fTexGenFactory = new TextureGeneratorFactory();
  }

  /**
   * Transforms a bounds from model coordinates to world coordinates.
   */
  private ILcdBounds transformedBounds(ILcdGeoReference aSourceReference,
                                       ILcdGeoReference aDestinationReference,
                                       ILcdBounds aSourceBounds) {
    try {
      ILcdModelModelTransformation m2m =
          new TLcdGeoReference2GeoReference(aSourceReference,
                                            aDestinationReference);

      TLcdXYZBounds destinationBounds = new TLcdXYZBounds();
      m2m.sourceBounds2destinationSFCT(aSourceBounds, destinationBounds);

      return destinationBounds;
    } catch (TLcdNoBoundsException e) {
      e.printStackTrace();
    }

    return null;
  }

  /**
   * Reads the XML configuration file and invokes the terrain preprocessor.
   */
  public void preprocessTerrain(String aSourceName) throws IOException {

    // Create the preprocessor.
    TLcdTerrainPreprocessor preprocessor = new TLcdTerrainPreprocessor();
    preprocessor.addStatusListener(new MyStatusListener());
    preprocessor.setXYZWorldReference(fWorldRef);

    // Parse the XML file.
    SAXBuilder builder = new SAXBuilder();
    Document doc = null;
    try {
      doc = builder.build(new File(aSourceName));

      Element terrain = doc.getRootElement();
      if (terrain != null) {
        // Shared buffer for raster decoders
        String bs = terrain.getAttributeValue("bufferSize" );
        if ((bs != null) && (bs.length() > 0)) {
          int size = Integer.valueOf(bs).intValue();
          TLcdBuffer buffer = new TLcdBuffer(size * 1024 * 1024);
          fRasterDecoder.setBuffer(buffer);
          fTexGenFactory.setBuffer(buffer);
        }


        List elements = terrain.getChildren();
        for (int i = 0; i < elements.size(); i++) {
          Element element = (Element) elements.get(i);
          String source = element.getText().trim();

          // Elevation map
          if (element.getName().equals("Elevation" )) {
            ILcdModel elvm = fRasterDecoder.decode(source);
            preprocessor.setElevationProvider(
                new TLcdInterpolatingRasterElevationProvider(elvm)
            );
            String width = element.getAttributeValue("width", "16" );
            String height = element.getAttributeValue("height", "16" );
            String exponent = element.getAttributeValue("tileExponent", "6" );
            preprocessor.setGeometryResolution(
                Integer.valueOf(width).intValue(),
                Integer.valueOf(height).intValue(),
                Integer.valueOf(exponent).intValue()
            );
          }
          // Texture
          else if (element.getName().equals("Texture" )) {
            String width = element.getAttributeValue("width", "1" );
            String height = element.getAttributeValue("height", "1" );
            String tileres = element.getAttributeValue("tileExponent", "6" );
            String gen = element.getAttributeValue("generator", "" );
            String name = element.getAttributeValue("name", "texture" );
            String compress = element.getAttributeValue("compress", "false" );

            ILcdTextureGenerator tg = fTexGenFactory.createTextureGenerator(
                source, gen, Boolean.valueOf(compress).booleanValue()
            );
            preprocessor.addTexture(
                Integer.valueOf(width).intValue(),
                Integer.valueOf(height).intValue(),
                Integer.valueOf(tileres).intValue(),
                name,
                tg
            );
          }
          // Area of interest
          else if (element.getName().equals("Bounds" )) {
            String x = element.getChildText("X" );
            String y = element.getChildText("Y" );
            String w = element.getChildText("W" );
            String h = element.getChildText("H" );

            try {
              ILcdBounds aoi = new TLcdXYBounds(
                  Double.valueOf(x).doubleValue(),
                  Double.valueOf(y).doubleValue(),
                  Double.valueOf(w).doubleValue(),
                  Double.valueOf(h).doubleValue()
              );
              // Transform the AOI from WGS84 lon/lat coords to the world reference
              TLcdGeodeticReference wgs84 = new TLcdGeodeticReference(
                  new TLcdGeodeticDatum()
              );
              aoi = transformedBounds(wgs84, fWorldRef, aoi);
              preprocessor.setAreaOfInterest(aoi);
            } catch (NumberFormatException e) {
              System.err.println("ERROR: " + e.getMessage());
              preprocessor.setAreaOfInterest(null);
            }
          }
          // Output file
          else if (element.getName().equals("Output" )) {
            fDestination = element.getText().trim();
            File f = new File(fDestination);
            f = f.getParentFile();
            f.delete();
            f.mkdirs();
            preprocessor.setDestination(fDestination);
          }
        }
      } else {
        throw new IOException("File does not contain a <Terrain> element!" );
      }
    } catch (Exception e) {
      System.err.println("ERROR: " + e.getMessage());
      throw new IOException("Can't decode " + aSourceName +
                            " (" + e.getMessage() + ")" );
    }

    // Configuration is complete, we can now run the preprocessor.
    preprocessor.preprocessTerrain();
  }

  private static File getFileInResourcePath(String aPath) {
    if (aPath == null) {
      return null;
    }
    File f = new File(aPath);
    if (f.isAbsolute()) {
      return f;
    }
    URL url = Main.class.getClassLoader().getResource(aPath);
    if (url != null) {
      try {
        f = new File(url.toURI());
      } catch (URISyntaxException e) {
      }
    }
    return f;
  }

  private class MyStatusListener implements ILcdStatusListener {

    public void statusChanged(TLcdStatusEvent event) {
      System.out.println(event.getMessage() +
                         " (" + (int) (event.getValue() * 100) + "%)" );
    }
  }

  public static void main(String[] aArgs) {
    TLcdAWTUtil.invokeAndWait(new Runnable() {
      @Override
      public void run() {
        JFileChooser c = new JFileChooser();
        c.setFileFilter(new FileFilter() {
          public boolean accept(File f) {
            return f.isDirectory() || f.getName().toLowerCase().endsWith(".xml" );
          }

          public String getDescription() {
            return "XML files (*.xml)";
          }
        });

        File dataDir = getFileInResourcePath("Data" );

        c.setCurrentDirectory(dataDir);
        if (c.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
          Main prep = new Main();
          try {
            long t0 = System.currentTimeMillis();
            prep.preprocessTerrain(c.getSelectedFile().getAbsolutePath());
            long t1 = System.currentTimeMillis();
            double dt = (t1 - t0) / 1000.0;
            System.out.println("--- Total time elapsed = " + dt + " sec ---" );
          } catch (IOException e) {
            e.printStackTrace();
          }
        }
      }
    });
  }
}

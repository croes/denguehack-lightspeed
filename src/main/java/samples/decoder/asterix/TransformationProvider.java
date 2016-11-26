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
package samples.decoder.asterix;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Locale;
import java.util.StringTokenizer;

import com.luciad.format.asterix.ALcdASTERIXTransformationProvider;
import com.luciad.geodesy.TLcdGeodeticDatum;
import com.luciad.io.TLcdInputStreamFactory;
import com.luciad.model.ILcdModel;
import com.luciad.projection.TLcdStereographic;
import com.luciad.reference.ILcdGeoReference;
import com.luciad.reference.TLcdGeodeticReference;
import com.luciad.reference.TLcdGridReference;
import com.luciad.shape.ILcdPoint;
import com.luciad.shape.shape3D.TLcdLonLatHeightPoint;
import com.luciad.transformation.ILcdModelModelTransformation;
import com.luciad.transformation.TLcdGeoReference2GeoReference;
import com.luciad.util.TLcdLonLatParser;

/**
 * Provides transformations for given SAC/SIC codes. It reads the actual
 * values from a configuration file.
 */
public class TransformationProvider extends ALcdASTERIXTransformationProvider {
  private static final ILcdGeoReference WGS84 = new TLcdGeodeticReference(new TLcdGeodeticDatum());

  private ILcdPoint fDefaultPoint = new TLcdLonLatHeightPoint(0, 0, 0);
  private HashMap<Integer, ILcdPoint> fCode2Location = new HashMap<Integer, ILcdPoint>();
  private HashMap<Integer, ILcdModelModelTransformation> fCode2Transformation = new HashMap<Integer, ILcdModelModelTransformation>();

  public TransformationProvider(String aConfigFileLocation) throws IOException {
    readConfig(aConfigFileLocation);
  }

  public ILcdModelModelTransformation provideModelModelTransformation(int aSacSic,
                                                                      ILcdModel aModel,
                                                                      Object aObject) {
    ILcdModelModelTransformation transformation = fCode2Transformation.get(aSacSic);
    if (transformation == null) {
      ILcdPoint point = fCode2Location.get(aSacSic);
      transformation = createTransformation(point != null ? point : fDefaultPoint);
      fCode2Transformation.put(aSacSic, transformation);
    }

    return transformation;
  }

  private ILcdModelModelTransformation createTransformation(ILcdPoint aLocation) {
    //Assuming all surveillance systems use a Stereographic projection...
    TLcdGridReference ref = new TLcdGridReference(new TLcdGeodeticDatum(),
                                                  new TLcdStereographic(aLocation),
                                                  0, 0, 1, 1, 0);

    return new TLcdGeoReference2GeoReference(ref, WGS84);
  }

  private void readConfig(String aSourceName) throws IOException {
    NumberFormat numberParser = NumberFormat.getInstance(Locale.ENGLISH);
    TLcdLonLatParser lonLatParser = new TLcdLonLatParser();

    BufferedReader reader = new BufferedReader(new InputStreamReader(
        new TLcdInputStreamFactory().createInputStream(aSourceName)));

    try {
      String line = reader.readLine();
      while (line != null) {
        line = line.trim();

        if (!line.startsWith("#") && line.length() > 0) {
          StringTokenizer tokenizer = new StringTokenizer(line);

          String sacSicAsString = tokenizer.nextToken().trim();
          String latAsString = tokenizer.nextToken().trim();
          String lonAsString = tokenizer.nextToken().trim();
          String altitudeAsString = tokenizer.nextToken().trim();

          //parse sac/sic
          if (sacSicAsString.startsWith("0x")) {
            sacSicAsString = sacSicAsString.substring(2);
          }
          int sacSic = Integer.parseInt(sacSicAsString, 16);

          try {
            double lon = lonLatParser.ordinateAsDouble(lonAsString);
            double lat = lonLatParser.ordinateAsDouble(latAsString);
            double altitude = numberParser.parse(altitudeAsString).doubleValue();
            fCode2Location.put(sacSic, new TLcdLonLatHeightPoint(lon, lat, altitude));
          } catch (ParseException ex) {
            IOException io = new IOException("Could not parse value[" + altitudeAsString + "]");

            //Only in java 1.4 or above
            io.initCause(ex);

            throw io;
          }
        }

        line = reader.readLine();
      }
    } finally {
      try {
        reader.close();
      } catch (IOException ignore) {
        //ignore, not that bad if only closing failed
      }
    }
  }
}

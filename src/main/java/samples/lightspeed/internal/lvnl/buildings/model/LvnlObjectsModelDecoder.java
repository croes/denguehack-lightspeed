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
package samples.lightspeed.internal.lvnl.buildings.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Properties;

import com.luciad.geodesy.TLcdGeodeticDatum;
import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelDecoder;
import com.luciad.model.TLcdModelDescriptor;
import com.luciad.model.TLcdVectorModel;
import com.luciad.reference.TLcdGeodeticReference;
import com.luciad.util.ILcdFireEventMode;

/**
 * Date: Jan 25, 2007
 * Time: 8:59:41 AM
 *
 * @author Tom Nuydens
 */
public class LvnlObjectsModelDecoder implements ILcdModelDecoder {
  public static final String OBJECTS_TYPE_NAME = "Buildings";

  private static final String ID = "id";
  private static final String OBJECT = "object.";
  private static final String LON = "lon";
  private static final String LAT = "lat";
  private static final String HEIGHT = "height";
  private static final String ROTATION = "rotation";

  public String getDisplayName() {
    return "Objects codec";
  }

  public boolean canDecodeSource(String string) {
    return new File(string).getName().toLowerCase().equals("object_placement.properties");
  }

  public ILcdModel decode(String string) throws IOException {
    TLcdVectorModel model = new TLcdVectorModel();
    model.setModelDescriptor(new TLcdModelDescriptor(
        string,
        OBJECTS_TYPE_NAME,
        "Schiphol"
    ));
    model.setModelReference(new TLcdGeodeticReference(new TLcdGeodeticDatum()));

    Properties p = new Properties();
    FileInputStream fileInputStream = new FileInputStream(string);
    try {
      p.load(fileInputStream);
    } catch (IOException e) {
      // Ignore; continue with empty properties
      p = new Properties();
    } finally {
      try {
        fileInputStream.close();
      } catch (IOException e) {
        // Ignore I/O exception on close.
      }
    }

    Enumeration<Object> keys = p.keys();
    while (keys.hasMoreElements()) {
      String key = (String) keys.nextElement();
      if (key.startsWith(OBJECT) && key.endsWith(ID)) {
        String base = key.substring(0, key.length() - ID.length());

        String object = p.getProperty(key);
        double lat = Double.parseDouble(p.getProperty(base + LAT, "0.0"));
        double lon = Double.parseDouble(p.getProperty(base + LON, "0.0"));
        double height = Double.parseDouble(p.getProperty(base + HEIGHT, "0.0"));
        double rot = Double.parseDouble(p.getProperty(base + ROTATION, "0.0"));
        LvnlPositionedObject obstacle = new LvnlPositionedObject(lon, lat, height);
        obstacle.setOrientation(rot);
        obstacle.setObjectName(object);

        model.addElement(obstacle, ILcdFireEventMode.NO_EVENT);
      }
    }

    return model;
  }
}

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
package samples.network.numeric;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import com.luciad.shape.ILcdBounds;
import com.luciad.shape.shape2D.ILcd2DEditableBounds;

/**
 * Utility class for reading and writing bounds to a properties file.
 */
public class BoundaryUtil {

  private static final String CLASS_NAME = "class.name";
  private static final String LOCATION_X = "location.x";
  private static final String LOCATION_Y = "location.y";
  private static final String WIDTH = "width";
  private static final String HEIGHT = "height";

  public static void writeBounds(ILcdBounds aBounds, OutputStream aOutputStream) throws IOException {
    Properties properties = new Properties();
    properties.put(CLASS_NAME, aBounds.getClass().getName());
    properties.put(LOCATION_X, Double.toString(aBounds.getLocation().getX()));
    properties.put(LOCATION_Y, Double.toString(aBounds.getLocation().getY()));
    properties.put(WIDTH, Double.toString(aBounds.getWidth()));
    properties.put(HEIGHT, Double.toString(aBounds.getHeight()));
    properties.store(aOutputStream, "Bounds properties");
  }

  public static ILcdBounds readBounds(InputStream aInputStream) throws IOException {
    Properties properties = new Properties();
    properties.load(aInputStream);
    String clazzName = properties.getProperty(CLASS_NAME);

    Class<ILcd2DEditableBounds> clazz;
    try {
      clazz = (Class<ILcd2DEditableBounds>) Class.forName(clazzName);
    } catch (ClassNotFoundException e) {
      throw new IOException("Could not load class with name: " + clazzName);
    }

    ILcd2DEditableBounds bounds;
    try {
      bounds = clazz.newInstance();
    } catch (InstantiationException e) {
      throw new IOException("Could not create instance of class: " + clazzName);
    } catch (IllegalAccessException e) {
      throw new IOException("Could not create instance of class: " + clazzName);
    }

    bounds.move2D(Double.parseDouble(properties.getProperty(LOCATION_X)),
                  Double.parseDouble(properties.getProperty(LOCATION_Y)));
    bounds.setWidth(Double.parseDouble(properties.getProperty(WIDTH)));
    bounds.setHeight(Double.parseDouble(properties.getProperty(HEIGHT)));

    return bounds;
  }

}

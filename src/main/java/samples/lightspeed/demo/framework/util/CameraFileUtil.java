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
package samples.lightspeed.demo.framework.util;

import java.awt.Point;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;

import com.luciad.shape.ILcdPoint;
import com.luciad.shape.shape2D.TLcdXYPoint;
import com.luciad.shape.shape3D.TLcdXYZPoint;
import com.luciad.util.logging.ILcdLogger;
import com.luciad.util.logging.TLcdLoggerFactory;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.camera.TLspViewXYZWorldTransformation2D;
import com.luciad.view.lightspeed.camera.TLspViewXYZWorldTransformation3D;

import samples.lightspeed.demo.framework.data.IOUtil;

/**
 * Utility class for reading/writing camera state from/to file.
 */
public class CameraFileUtil {

  private static final ILcdLogger sLogger = TLcdLoggerFactory.getLogger(CameraFileUtil.class);

  private static String CAMERA_INFO_FILE_NAME = "camera_info_";

  /**
   * Reads camera state for a 3D camera from a file.
   * <p/>
   * This method returns an array of camera parameters. The array contains 6 elements and is
   * specified
   * as follows:
   * <ol start=0>
   * <li> X-coordinate of eye point (i.e. location of the camera in world coordinates)
   * <li> Y-coordinate of eye point (i.e. location of the camera in world coordinates)
   * <li> Z-coordinate of eye point (i.e. location of the camera in world coordinates)
   * <li> Distance of camera to reference point
   * <li> Pitch of the camera
   * <li> Yaw of the camera
   * </ol>
   *
   * @param aSourceName the source containing the camera state
   *
   * @return an array containing the camera parameters ({@code null} if an error occurred)
   */
  public static double[] read3DCamera(String aSourceName) {
    try {
      BufferedReader reader = IOUtil.createReader(aSourceName);
      TLcdXYZPoint location = new TLcdXYZPoint();
      double distance, pitch, yaw;

      // Read parameters from file
      readPoint(reader, location);
      distance = readDouble(reader);
      pitch = readDouble(reader);
      yaw = readDouble(reader);

      // Create array
      double[] result = new double[6];
      result[0] = location.getX();
      result[1] = location.getY();
      result[2] = location.getZ();
      result[3] = distance;
      result[4] = pitch;
      result[5] = yaw;

      reader.close();
      return result;
    } catch (Exception e) {
      sLogger.warn("Could not read 3D camera file from " + aSourceName + " (" + e.getMessage() + ")");
      return null;
    }
  }

  /**
   * Reads camera state for a 2D camera from a file.
   * <p/>
   * This method returns an array of camera parameters. The array contains 7 elements and is
   * specified
   * as follows:
   * <ol start=0>
   * <li> x-coordinate of world origin
   * <li> y-coordinate of world origin
   * <li> x-coordinate of view origin
   * <li> y-coordinate of view origin
   * <li> scale in x-direction
   * <li> scale in y-direction
   * <li> view rotation angle (in degrees)
   * </ol>
   *
   * @param aSourceName the source containing the camera state
   *
   * @return an array containing the camera parameters ({@code null} if an error occurred)
   */
  public static double[] read2DCamera(String aSourceName) {
    try {
      BufferedReader reader = IOUtil.createReader(aSourceName);

      // Read parameters from file
      double wx = readDouble(reader);
      double wy = readDouble(reader);
      double vx = readDouble(reader);
      double vy = readDouble(reader);
      double sx = readDouble(reader);
      double sy = readDouble(reader);
      double r = readDouble(reader);

      // Create array
      double[] result = new double[7];
      result[0] = wx;
      result[1] = wy;
      result[2] = vx;
      result[3] = vy;
      result[4] = sx;
      result[5] = sy;
      result[6] = r;

      reader.close();
      return result;
    } catch (Exception e) {
      sLogger.warn("Could not read 2D camera file from " + aSourceName + " (" + e.getMessage() + ")");
      return null;
    }
  }

  /**
   * Reads a line and parses it as a <code>TLcdXYZPoint</code>. Format for parsing is 3 doubles,
   * separated by a space. For example: "0 0 0" would be parsed as (0,0,0)
   *
   * @param aReader    the reader used to retrieve the line
   * @param aPointSFCT the point that will be filled in with the parsed result
   *
   * @throws IOException
   */
  private static void readPoint(BufferedReader aReader, TLcdXYZPoint aPointSFCT) throws IOException {
    String line = aReader.readLine();
    if (line == null) {
      throw new IOException(CAMERA_INFO_FILE_NAME + " does not contain enough data.");
    }
    String[] coordinates = line.split(" ");
    aPointSFCT.move3D(
        Double.parseDouble(coordinates[0]),
        Double.parseDouble(coordinates[1]),
        Double.parseDouble(coordinates[2])
    );
  }

  /**
   * Reads a line and parses it as a double.
   *
   * @param aReader the reader used to retrieve the line
   *
   * @return the parsed double value
   *
   * @throws IOException
   */
  private static double readDouble(BufferedReader aReader) throws IOException {
    String line = aReader.readLine();
    if (line == null) {
      throw new IOException(CAMERA_INFO_FILE_NAME + " does not contain enough data.");
    }
    return Double.parseDouble(line);
  }

  /**
   * Writes camera state of given 2D view to file.
   *
   * @param aView the 2D view for which the camera state is to be written to file
   * @param aFile the file to which the camera state will be written
   *
   * @throws IllegalArgumentException when given view is not a 2D view
   */
  public static void write2DCamera(ILspView aView, File aFile) {
    if (aView.getViewXYZWorldTransformation()
        instanceof TLspViewXYZWorldTransformation3D) {
      throw new IllegalArgumentException("Could not write 2D camera state to file, reason: view is not a 2D view!");
    }
    TLspViewXYZWorldTransformation2D t = (TLspViewXYZWorldTransformation2D) aView
        .getViewXYZWorldTransformation();

    TLcdXYPoint worldOrigin = t.getWorldOrigin();
    Point viewOrigin = t.getViewOrigin();
    double width = t.getWidth();
    double height = t.getHeight();
    double scaleX = t.getScaleX();
    double scaleY = t.getScaleY();
    double wx = worldOrigin.getX();
    double wy = worldOrigin.getY();
    double vx = viewOrigin.getX() / width;
    double vy = viewOrigin.getY() / height;
    double rotation = t.getRotation();

    try {
      boolean success = aFile.getParentFile().mkdirs();
      if (!aFile.getParentFile().exists()) {
        throw new RuntimeException("Could not create directories.");
      }
      BufferedWriter out = IOUtil.createWriter(aFile);
      out.write(wx + "\n" + wy + "\n");
      out.write(vx + "\n" + vy + "\n");
      out.write(scaleX + "\n" + scaleY + "\n");
      out.write(rotation + "\n");
      out.close();
    } catch (Exception e) {
      sLogger.error("Could not write 2D Camera file", e);
    }

  }

  /**
   * Writes camera state of given 3D view to file.
   *
   * @param aView the 3D view for which the camera state is to be written to file
   * @param aFile the file to which the camera state will be written
   *
   * @throws IllegalArgumentException when given view is not a 3D view
   */
  public static void write3DCamera(ILspView aView, File aFile) {
    if (aView.getViewXYZWorldTransformation()
        instanceof TLspViewXYZWorldTransformation2D) {
      throw new IllegalArgumentException("Could not write 3D camera state to file, reason: view is not a 3D view!");
    }
    TLspViewXYZWorldTransformation3D t = (TLspViewXYZWorldTransformation3D) aView
        .getViewXYZWorldTransformation();

    ILcdPoint location = t.getReferencePoint();
    double distance = t.getDistance();
    double pitch = t.getPitch();
    double yaw = t.getYaw();

    try {
      aFile.getParentFile().mkdirs();
      if (!aFile.getParentFile().exists()) {
        throw new RuntimeException("Could not create directories.");
      }
      BufferedWriter out = IOUtil.createWriter(aFile);
      out.write(location.getX() + " " + location.getY() + " " + location.getZ() + "\n");
      out.write(distance + "\n");
      out.write(pitch + "\n");
      out.write(yaw + "\n");
      out.close();
    } catch (Exception e) {
      sLogger.error("Could not write 3D camera file.", e);
    }
  }

}

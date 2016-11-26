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
package samples.gxy.rectification.util;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import org.hiranabe.vecmath.Matrix3d;
import org.hiranabe.vecmath.Vector3d;

import com.luciad.geodesy.ILcdEllipsoid;
import com.luciad.geodesy.TLcdEllipsoid;
import com.luciad.projection.TLcdPerspectiveProjection;
import com.luciad.shape.shape3D.TLcdLonLatHeightPoint;
import com.luciad.shape.shape3D.TLcdXYZPoint;

/**
 * A dialog that asks the parameters of the camera from the user. It requires the 3D position
 * (longitude, latitude and altitude) of the camera, the orientation (yaw, pitch, roll) and the
 * focal length (the distance from the camera's optical center to the projection plane).
 */
public class PerspectiveParamsDialog extends JDialog {

  private JTextField fLon = new JTextField("0");
  private JTextField fLat = new JTextField("0");
  private JTextField fAltitude = new JTextField("10000"); // Meters above the ellipsoid.
  private JTextField fYaw = new JTextField("0");          // Orientation clockwise from North.
  private JTextField fPitch = new JTextField("-90");      // 0 is horizontal, -90 is straight down.
  private JTextField fRoll = new JTextField("0");         // Positive when the right wing goes down.
  private JTextField fFocalLength = new JTextField("1000"); // Distance to projection plane (meters)

  public PerspectiveParamsDialog(Component aParent) {
    super((Frame) null, "Sensor parameters", true);
    getContentPane().setLayout(new GridBagLayout());
    setLocationRelativeTo(aParent);
    setMinimumSize(new Dimension(408, 249));

    MyFieldsListener myPropertyListener = new MyFieldsListener();
    MyOkButtonListener myOkButtonListener = new MyOkButtonListener();

    fLon.setHorizontalAlignment(JTextField.RIGHT);
    fLat.setHorizontalAlignment(JTextField.RIGHT);
    fAltitude.setHorizontalAlignment(JTextField.RIGHT);
    fYaw.setHorizontalAlignment(JTextField.RIGHT);
    fPitch.setHorizontalAlignment(JTextField.RIGHT);
    fRoll.setHorizontalAlignment(JTextField.RIGHT);
    fFocalLength.setHorizontalAlignment(JTextField.RIGHT);

    fLon.addActionListener(myPropertyListener);
    fLat.addActionListener(myPropertyListener);
    fAltitude.addActionListener(myPropertyListener);
    fYaw.addActionListener(myPropertyListener);
    fPitch.addActionListener(myPropertyListener);
    fRoll.addActionListener(myPropertyListener);
    fFocalLength.addActionListener(myPropertyListener);

    JButton oKButton = new JButton("OK");
    oKButton.addActionListener(myOkButtonListener);

    GridBagConstraints c;

    Insets insetsTLR = new Insets(5, 5, 2, 5);
    Insets insetsLR = new Insets(2, 5, 2, 5);
    Insets insetsTLBR = new Insets(10, 10, 10, 10);

    c = new GridBagConstraints();
    c.gridx = 0;
    c.gridy = 0;
    c.gridwidth = 2;
    c.anchor = GridBagConstraints.LINE_START;
    c.insets = insetsTLBR;
    add(new JLabel("Please provide the parameters of the sensor: "), c);

    c = new GridBagConstraints();
    c.gridx = 0;
    c.gridy = 1;
    c.weightx = 0.1;
    c.fill = GridBagConstraints.NONE;
    c.anchor = GridBagConstraints.LINE_END;
    c.insets = insetsTLR;
    add(new JLabel("Lon:"), c);

    c = new GridBagConstraints();
    c.gridx = 1;
    c.gridy = 1;
    c.weightx = 0.9;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.insets = insetsTLR;
    add(fLon, c);

    c = new GridBagConstraints();
    c.gridx = 0;
    c.gridy = 2;
    c.fill = GridBagConstraints.NONE;
    c.anchor = GridBagConstraints.LINE_END;
    c.insets = insetsLR;
    add(new JLabel("Lat:"), c);

    c = new GridBagConstraints();
    c.gridx = 1;
    c.gridy = 2;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.insets = insetsLR;
    add(fLat, c);

    c = new GridBagConstraints();
    c.gridx = 0;
    c.gridy = 3;
    c.fill = GridBagConstraints.NONE;
    c.anchor = GridBagConstraints.LINE_END;
    c.insets = insetsLR;
    add(new JLabel("Altitude:"), c);

    c = new GridBagConstraints();
    c.gridx = 1;
    c.gridy = 3;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.insets = insetsLR;
    add(fAltitude, c);

    c = new GridBagConstraints();
    c.gridx = 0;
    c.gridy = 4;
    c.fill = GridBagConstraints.NONE;
    c.anchor = GridBagConstraints.LINE_END;
    c.insets = insetsLR;
    add(new JLabel("Yaw:"), c);

    c = new GridBagConstraints();
    c.gridx = 1;
    c.gridy = 4;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.insets = insetsLR;
    add(fYaw, c);

    c = new GridBagConstraints();
    c.gridx = 0;
    c.gridy = 5;
    c.fill = GridBagConstraints.NONE;
    c.anchor = GridBagConstraints.LINE_END;
    c.insets = insetsLR;
    add(new JLabel("Pitch:"), c);

    c = new GridBagConstraints();
    c.gridx = 1;
    c.gridy = 5;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.insets = insetsLR;
    add(fPitch, c);

    c = new GridBagConstraints();
    c.gridx = 0;
    c.gridy = 6;
    c.fill = GridBagConstraints.NONE;
    c.anchor = GridBagConstraints.LINE_END;
    c.insets = insetsLR;
    add(new JLabel("Roll:"), c);

    c = new GridBagConstraints();
    c.gridx = 1;
    c.gridy = 6;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.insets = insetsLR;
    add(fRoll, c);

    c = new GridBagConstraints();
    c.gridx = 0;
    c.gridy = 7;
    c.fill = GridBagConstraints.NONE;
    c.anchor = GridBagConstraints.LINE_END;
    c.insets = insetsLR;
    add(new JLabel("Focal length:"), c);

    c = new GridBagConstraints();
    c.gridx = 1;
    c.gridy = 7;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.insets = insetsLR;
    add(fFocalLength, c);

    c = new GridBagConstraints();
    c.gridx = 1;
    c.gridy = 8;
    c.fill = GridBagConstraints.NONE;
    c.anchor = GridBagConstraints.CENTER;
    c.insets = insetsTLBR;
    add(oKButton, c);

    pack();

  }

  // could be null in case no projection was set
  public TLcdPerspectiveProjection getProjection() {

    return createPerspectiveProjection(TLcdEllipsoid.DEFAULT,
                                       getLon(),
                                       getLat(),
                                       getAltitude(),
                                       getYaw(),
                                       getPitch(),
                                       getRoll(),
                                       getFocalLength());
  }

  private static TLcdPerspectiveProjection createPerspectiveProjection(ILcdEllipsoid aEllipsoid,
                                                                       double aSensorLon,
                                                                       double aSensorLat,
                                                                       double aSensorAltitude,
                                                                       double aSensorYaw,
                                                                       double aSensorPitch,
                                                                       double aSensorRoll,
                                                                       double aSensorFocalLength) {
    Matrix3d sensor_YPR_RotationMatrix;
    sensor_YPR_RotationMatrix = getYPRRotationMatrix(aSensorYaw,
                                                     aSensorPitch,
                                                     aSensorRoll);
    Matrix3d NED2Geocentric = getNED2Geocentric(aSensorLon, aSensorLat);

    // Compute the platform + sensor rotation matrix
    Matrix3d fullNEDXYZRotationMatrix = new Matrix3d();
    fullNEDXYZRotationMatrix.mul(NED2Geocentric, sensor_YPR_RotationMatrix);

    TLcdLonLatHeightPoint sensor_llh_location = new TLcdLonLatHeightPoint(aSensorLon,
                                                                          aSensorLat,
                                                                          aSensorAltitude);

    TLcdXYZPoint sensor_xyz_location = new TLcdXYZPoint();
    aEllipsoid.llh2geocSFCT(sensor_llh_location, sensor_xyz_location);

    // Find the 3D location of the grid origin (the principal point)
    Vector3d grid_xyz_origin = new Vector3d(aSensorFocalLength, 0, 0);
    fullNEDXYZRotationMatrix.transform(grid_xyz_origin);
    grid_xyz_origin.add(new Vector3d(sensor_xyz_location.getX(),
                                     sensor_xyz_location.getY(),
                                     sensor_xyz_location.getZ()));
    TLcdXYZPoint grid_location = new TLcdXYZPoint(grid_xyz_origin.x,
                                                  grid_xyz_origin.y,
                                                  grid_xyz_origin.z);
    Vector3d y_axis = new Vector3d(0, 0, -1);
    fullNEDXYZRotationMatrix.transform(y_axis);
    return new TLcdPerspectiveProjection(sensor_xyz_location,
                                         grid_location,
                                         new TLcdXYZPoint(y_axis.x,
                                                          y_axis.y,
                                                          y_axis.z));
  }

  /**
   * Creates the matrix that transforms a direction from NED (North-East-Down) coordinates to
   * geocentric.
   *
   * @param aLon longitude of the camera
   * @param aLat latitude of the camera
   *
   * @return the NED2ECEF transformation matrix {@see <a href="http://www.mathworks.com/access/helpdesk/help/toolbox/aeroblks/directioncosinematrixeceftonedtolatitudeandlongitude.html">Mathworks</a>}
   */
  public static Matrix3d getNED2Geocentric(double aLon, double aLat) {
    double lon_radians = Math.toRadians(aLon);
    double lat_radians = Math.toRadians(aLat);
    double cos_lon = Math.cos(lon_radians);
    double sin_lon = Math.sin(lon_radians);
    double cos_lat = Math.cos(lat_radians);
    double sin_lat = Math.sin(lat_radians);

    return new Matrix3d(
        -sin_lat * cos_lon, -sin_lon, -cos_lat * cos_lon,
        -sin_lat * sin_lon, cos_lon, -cos_lat * sin_lon,
        cos_lat, 0, -sin_lat);
  }

  /**
   * Creates a rotation matrix in NED (North-East-Down) coordinates.
   *
   * @param aYaw   the yaw
   * @param aPitch the pitch
   * @param aRoll  the roll
   *
   * @return a rotation matrix in NED (North-East-Down) coordinates.
   */
  private static Matrix3d getYPRRotationMatrix(double aYaw, double aPitch, double aRoll) {
    double yaw = Math.toRadians(aYaw);
    double pitch = Math.toRadians(aPitch);
    double roll = Math.toRadians(aRoll);
    Matrix3d yaw_matrix = new Matrix3d();
    Matrix3d pitch_matrix = new Matrix3d();
    Matrix3d roll_matrix = new Matrix3d();

    yaw_matrix.rotZ(yaw);
    pitch_matrix.rotY(pitch);
    roll_matrix.rotX(roll);

    // Compute the platform rotation matrix
    Matrix3d rotation = new Matrix3d();
    rotation.mul(yaw_matrix, pitch_matrix);
    rotation.mul(roll_matrix);
    return rotation;
  }

  public double getLon() {
    return Double.parseDouble(fLon.getText());
  }

  public double getLat() {
    return Double.parseDouble(fLat.getText());
  }

  public double getAltitude() {
    return Double.parseDouble(fAltitude.getText());
  }

  public double getYaw() {
    return Double.parseDouble(fYaw.getText());
  }

  public double getPitch() {
    return Double.parseDouble(fPitch.getText());
  }

  public double getRoll() {
    return Double.parseDouble(fRoll.getText());
  }

  public double getFocalLength() {
    return Double.parseDouble(fFocalLength.getText());
  }

  private boolean validateFields() {
    try {
      double lon = getLon();
      if (lon < -180 || lon > 180) {
        JOptionPane.showMessageDialog(null, "Invalid longitude", "Error", JOptionPane.ERROR_MESSAGE);
        return false;
      }
      double lat = getLat();
      if (lat < -90 || lat > 90) {
        JOptionPane.showMessageDialog(null, "Invalid latitude", "Error", JOptionPane.ERROR_MESSAGE);
        return false;
      }
      double altitude = getAltitude();
      if (altitude < 0.8) {
        JOptionPane.showMessageDialog(null, "Invalid altitude", "Error", JOptionPane.ERROR_MESSAGE);
        return false;
      }
      double yaw = getYaw();
      if (yaw < 0 || yaw > 360) {
        JOptionPane.showMessageDialog(null, "Invalid yaw", "Error", JOptionPane.ERROR_MESSAGE);
        return false;
      }
      double pitch = getPitch();
      if (pitch < -90 || pitch > 90) {
        JOptionPane.showMessageDialog(null, "Invalid pitch", "Error", JOptionPane.ERROR_MESSAGE);
        return false;
      }
      double roll = getRoll();
      if (roll < 0 || roll > 360) {
        JOptionPane.showMessageDialog(null, "Invalid roll", "Error", JOptionPane.ERROR_MESSAGE);
        return false;
      }
      double focalLength = getFocalLength();
      if (focalLength < 0) {
        JOptionPane.showMessageDialog(null, "Invalid focal length", "Error", JOptionPane.ERROR_MESSAGE);
        return false;
      }
    } catch (NumberFormatException ex) {
      JOptionPane.showMessageDialog(null, "Invalid number", "Error", JOptionPane.ERROR_MESSAGE);
      return false;
    }
    return true;
  }

  private class MyFieldsListener implements ActionListener {

    public void actionPerformed(ActionEvent aActionEvent) {
      validateFields();
    }
  }

  private class MyOkButtonListener implements ActionListener {

    public void actionPerformed(ActionEvent e) {
      if (validateFields()) {
        setVisible(false);
      }
    }
  }

}

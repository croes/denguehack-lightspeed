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
package samples.gxy.touch.gestures;

import java.awt.Point;
import java.util.List;

import Jama.EigenvalueDecomposition;
import Jama.Matrix;

import com.luciad.geodesy.TLcdEllipsoid;
import com.luciad.shape.shape3D.TLcdLonLatHeightPoint;
import com.luciad.shape.shape3D.TLcdXYZPoint;

/**
 * Approximates the given points by an ellipse. The fitting algorithm is described in:
 *    A. Fitzgibbon, M Pilu, R. Fisher: Direct Least Squares Fitting of Ellipses
 *    IEEE Transactions on Pattern Analysis and Machine Intelligence, 21(5), 476--480, May 1999
 *
 * Copyright (c) 1999, 2005, Andrew Fitzgibbon, Maurizio Pilu, Bob Fisher
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 */
public class EllipseFitter {
  private double fLocationX;
  private double fLocationY;
  private double fA;
  private double fB;
  private double fRotation;
  private double fMaxDeviation;

  public boolean optimize(List<Point> aPoints) {
    if (aPoints.size() < 6) {
      return false;
    }

    int sum_x = 0;
    int sum_y = 0;
    double max_x = Double.NEGATIVE_INFINITY;
    double max_y = Double.NEGATIVE_INFINITY;
    double min_x = Double.POSITIVE_INFINITY;
    double min_y = Double.POSITIVE_INFINITY;
    for (Point point : aPoints) {
      sum_x += point.x;
      sum_y += point.y;
      if (point.x < min_x) {
        min_x = point.x;
      }
      if (point.x > max_x) {
        max_x = point.x;
      }
      if (point.y < min_y) {
        min_y = point.y;
      }
      if (point.y > max_y) {
        max_y = point.y;
      }
    }

    if (max_x == min_x || max_y == min_y) {
      return false;
    }

    double mx = sum_x / (double) aPoints.size();
    double my = sum_y / (double) aPoints.size();
    double sx = (max_x - min_x) / 2.0;
    double sy = (max_y - min_y) / 2.0;

    double[] x = new double[aPoints.size()];
    double[] y = new double[aPoints.size()];

    // Design matrix
    Matrix D = new Matrix(aPoints.size(), 6);
    for (int i = 0; i < aPoints.size(); i++) {
      x[i] = (aPoints.get(i).x - mx) / sx;
      y[i] = (aPoints.get(i).y - my) / sy;
      D.set(i, 0, x[i] * x[i]);
      D.set(i, 1, x[i] * y[i]);
      D.set(i, 2, y[i] * y[i]);
      D.set(i, 3, x[i]);
      D.set(i, 4, y[i]);
      D.set(i, 5, 1);
    }

    // Scatter matrix;
    Matrix S = D.transpose().times(D);

    // Constraint matrix:
    Matrix C = new Matrix(6, 6);
    C.set(0, 2, -2);
    C.set(1, 1, 1);
    C.set(2, 0, -2);

    // Generalized eigenvalue decomposition of S and C
    EigenvalueDecomposition eig;
    try {
      eig = S.inverse().times(C).eig();
    } catch (Throwable t) {
      // Matrix is singular.
      return false;
    }
    double[] real_eigenvalues = eig.getRealEigenvalues();
    int column;
    for (column = 0; column < real_eigenvalues.length; column++) {
      // Find the negative eigenvalue 
      double val = real_eigenvalues[column];
      if (Math.abs(val) > 1e-8 && 1 / val < 1e-8) {
        break;
      }
    }

    Matrix V = eig.getV();
    double[] A = new double[V.getRowDimension()];
    for (int i = 0; i < A.length; i++) {
      A[i] = V.get(i, column);
    }
    double[] par = new double[6];
    par[0] = A[0] * sy * sy;
    par[1] = A[1] * sx * sy;
    par[2] = A[2] * sx * sx;
    par[3] = -2 * A[0] * sy * sy * mx - A[1] * sx * sy * my + A[3] * sx * sy * sy;
    par[4] = -A[1] * sx * sy * mx - 2 * A[2] * sx * sx * my + A[4] * sx * sx * sy;
    par[5] = A[0] * sy * sy * mx * mx + A[1] * sx * sy * mx * my + A[2] * sx * sx * my * my
             - A[3] * sx * sy * sy * mx - A[4] * sx * sx * sy * my + A[5] * sx * sx * sy * sy;

    fRotation = 0.5 * Math.atan2(par[1], par[0] - par[2]);
    final double cos = Math.cos(fRotation);
    final double sin = Math.sin(fRotation);
    final double sin2 = sin * sin;
    final double cos2 = cos * cos;
    final double sincos = sin * cos;

    final double Ao = par[5];
    final double Au = par[3] * cos + par[4] * sin;
    final double Av = -par[3] * sin + par[4] * cos;
    final double Auu = par[0] * cos2 + par[2] * sin2 + par[1] * sincos;
    final double Avv = par[0] * sin2 + par[2] * cos2 - par[1] * sincos;

    final double tuCentre = -Au / (2 * Auu);
    final double tvCentre = -Av / (2 * Avv);
    final double wCentre = Ao - Auu * tuCentre * tuCentre - Avv * tvCentre * tvCentre;

    fLocationX = tuCentre * cos - tvCentre * sin;
    fLocationY = tuCentre * sin + tvCentre * cos;

    final double a = -wCentre / Auu;
    final double b = -wCentre / Avv;

    fA = Math.sqrt(Math.abs(a)) * Math.signum(a);
    fB = Math.sqrt(Math.abs(b)) * Math.signum(b);

    fRotation = Math.toDegrees(fRotation);

    // To find out the distance of the input points to the ellipse we just computed we would have
    // to solve a quartic equation. However, similar computations are already available in the
    // TLcdEllipsoid class. It allows us to transform a (x,y,z) geocentric point to a
    // (lon,lat,height) geodetic point. If we make our 2D input point a 3D "geocentric" point, we
    // can immediately obtain the height above the ellipsoid, which is exactly the distance
    // that we need.
    TLcdEllipsoid ellipsoid = new TLcdEllipsoid();
    ellipsoid.initializeAB(fA, fB);
    TLcdXYZPoint xyz_point = new TLcdXYZPoint();
    TLcdLonLatHeightPoint llh_point = new TLcdLonLatHeightPoint();
    fMaxDeviation = Double.NEGATIVE_INFINITY;
    for (Point point : aPoints) {
      // Rotate the point to align it with the ellipse's axes
      double dx = point.x - fLocationX;
      double dy = point.y - fLocationY;
      double X = dx * cos - dy * sin;
      double Y = dx * sin + dy * cos;

      // Create a suitable 3D geocentric point
      xyz_point.move3D(0, X, Y);
      ellipsoid.geoc2llhSFCT(xyz_point, llh_point);
      double distance = llh_point.getZ();
      if (distance > fMaxDeviation) {
        fMaxDeviation = distance;
      }
    }
    return true;
  }

  public double getLocationX() {
    return fLocationX;
  }

  public double getLocationY() {
    return fLocationY;
  }

  public double getA() {
    return fA;
  }

  public double getB() {
    return fB;
  }

  public double getRotation() {
    return fRotation;
  }

  public double getMaxDeviation() {
    return fMaxDeviation;
  }
}

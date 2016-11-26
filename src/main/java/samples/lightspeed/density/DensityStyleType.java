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
package samples.lightspeed.density;

/**
 * Enumeration describing different categories of density styles. Styles can be either point or
 * line styles, and have their width defined in either pixel or world size. For each of these
 * categories sensible default, minimum and maximum widths are defined.
 */
public enum DensityStyleType {
  /**
   * Density style used where the width of lines is expressed in number of pixels
   */
  LINES_PIXEL_SIZED(false, false, 1.0, 5.0, 10.0) {
    public String toString() {
      return "Pixel-sized line density";
    }
  },
  /**
   * Density style used where the width of lines is expressed in world size.
   */
  LINES_WORLD_SIZED(false, true, 20000, 100000.0, 200000.0) {
    public String toString() {
      return "World-sized line density";
    }
  },
  /**
   * Density style used where the width of points is expressed in number of pixels
   */
  POINTS_PIXEL_SIZED(true, false, 2.0, 26.0, 50.0) {
    public String toString() {
      return "Pixel-sized point density";
    }
  },
  /**
   * Density style used where the width of points is expressed in world size.
   */
  POINTS_WORLD_SIZED(true, true, 100000.0, 240000, 400000.0) {
    public String toString() {
      return "World-sized point density";
    }
  };
  //Minimum width of density style
  private final double fMinimumSize;
  //Maximum width of density style
  private final double fMaximumSize;
  //Default width of density style
  private final double fDefaultSize;
  //Whether the width is defined in world size or not
  private final boolean fWorldSize;
  //Whether this style should be used for points or not.
  private final boolean fIsPointStyle;

  /**
   * Returns the maximum width of the density style
   * @return the maximum width of the density style
   */
  public double getMaximumSize() {
    return fMaximumSize;
  }

  /**
   * Returns the minimum width of the density style
   * @return the minimum width of the density style
   */
  public double getMinimumSize() {
    return fMinimumSize;
  }

  /**
   * Returns the default width of the density style
   * @return the default width of the density style
   */
  public double getDefaultSize() {
    return fDefaultSize;
  }

  /**
   * Returns whether the width is expressed in world size.
   * @return whether the width is expressed in world size.
   */
  public boolean isWorldSize() {
    return fWorldSize;
  }

  /**
   * Returns whether this style should be used for points.
   * @return Whether this style should be used for points.
   */
  public boolean isPointStyle() {
    return fIsPointStyle;
  }

  /**
   * Constructs a new density style type
   * @param aIsPointStyle   whether this style should be used for points
   * @param aWorldSize      whether the width is expressed in world size
   * @param aMinimumSize    minimum width of the density style
   * @param aDefaultSize    default width of the density style
   * @param aMaximumSize    maximum width of the density style
   */
  private DensityStyleType(boolean aIsPointStyle, boolean aWorldSize, double aMinimumSize,
                           double aDefaultSize, double aMaximumSize) {
    fIsPointStyle = aIsPointStyle;
    fWorldSize = aWorldSize;
    fMinimumSize = aMinimumSize;
    fDefaultSize = aDefaultSize;
    fMaximumSize = aMaximumSize;
  }
}

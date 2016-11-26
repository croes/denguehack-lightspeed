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
package samples.gxy.grid.multilevel.gars;

import com.luciad.view.map.multilevelgrid.ILcdEditableMultilevelGridCoordinate;

/**
 * A coordinate designating an element of a GARS grid. Depending on the level at which the
 * coordinates are specified, the element is called a cell, a quadrant or a keypad.
 */
public class GARSGridCoordinate implements Cloneable, ILcdEditableMultilevelGridCoordinate {

  private static final int X_AXIS = 0;
  private static final int Y_AXIS = 1;

  private int fCellX, fCellY;
  private int fQuadrantX, fQuadrantY;
  private int fKeypadX, fKeypadY;

  private int fSpecifiedCoordinateLevelCount = 0;
  /**
   * Value indicating that the cell level is specified.
   * @see #getCoordinateLevelCount()
   */
  public static final int CELL = 0;
  /**
   * Value indicating that the quadrant level is specified.
   * @see #getCoordinateLevelCount()
   */
  public static final int QUADRANT = 1;
  /**
   * Value indicating that the keypad level is specified.
   * @see #getCoordinateLevelCount()
   */
  public static final int KEYPAD = 2;
  /**
   * Value indicating northwest quadrant.
   */
  public static final int QUADRANT_NW = 1;
  /**
   * Value indicating northeast quadrant.
   */
  public static final int QUADRANT_NE = 2;
  /**
   * Value indicating southwest quadrant.
   */
  public static final int QUADRANT_SW = 3;
  /**
   * Value indicating southeast quadrant.
   */
  public static final int QUADRANT_SE = 4;
  /**
   * Array listing all quadrants to be used in iteration over quadrants.
   */
  public int[] QUADRANT_ARRAY = new int[]{QUADRANT_NW, QUADRANT_NE, QUADRANT_SW, QUADRANT_SE};

  public int getCoordinate(int aLevel, int aAxis) {
    if (aAxis != X_AXIS && aAxis != Y_AXIS) {
      throw new IndexOutOfBoundsException("There is no axis specified for the value " + aAxis);
    }

    switch (aLevel) {
    case CELL:
      if (aAxis == X_AXIS) {
        return fCellX;
      } else {
        return fCellY;
      }
    case KEYPAD:
      if (aAxis == X_AXIS) {
        return fKeypadX;
      } else {
        return fKeypadY;
      }
    case QUADRANT:
      if (aAxis == X_AXIS) {
        return fQuadrantX;
      } else {
        return fQuadrantY;
      }
    default:
      throw new IndexOutOfBoundsException("No level " + aLevel + " available in GARS coordinates .GARS coordinates can only contain 3 levels.");
    }
  }

  public int getCoordinateLevelCount() {
    return fSpecifiedCoordinateLevelCount;
  }

  /**
   * Cell row starting from 1.
   * @return Cell row.
   */
  public int getCellRow() {
    return fCellY + 1;
  }

  private void setCellRow(int aCellRow) {
    if (aCellRow < 1) {
      throw new IllegalArgumentException("The cell row in GARS must be higher then 0, now it was " + aCellRow);
    }
    fCellY = aCellRow - 1;
  }

  /**
   * Cell column starting from 1.
   * @return Cell column.
   */
  public int getCellColumn() {
    return fCellX + 1;
  }

  private void setCellColumn(int aCellColumn) {
    if (aCellColumn < 1) {
      throw new IllegalArgumentException("The cell column in GARS must be higher then 0, now it was " + aCellColumn);
    }
    fCellX = aCellColumn - 1;
  }

  /**
   * Gets the keypad of this GARS Grid Coordinate
   * (this method should be called only if {@link #getCoordinateLevelCount()}>keypad)
   * @return A number in the range of [1,9] if the keypad level is specified,
   *          otherwise it returns -1
   */
  public int getKeypad() {
    if (getCoordinateLevelCount() > KEYPAD) {
      return (2 - fKeypadY) * 3 + fKeypadX + 1;
    } else {
      return -1;
    }
  }

  private void setKeyPad(int aKeypad) {
    if (!(aKeypad >= 1 && aKeypad <= 9)) {
      throw new IllegalArgumentException("Invalid keypad for a GARS grid coordinate. It should be in the range [1,9]. Instead it was: " + aKeypad);
    }
    // correction: treat the numbers as if it was in the range of [0,8]
    aKeypad--;
    int keypad_column = (aKeypad) % 3;
    int keypad_row = 2 - (aKeypad / 3);
    fKeypadX = keypad_column;
    fKeypadY = keypad_row;
  }

  /**
   * Returns the quadrant as one of
   * <ul>
   * <li>
   * {@link GARSGridCoordinate#QUADRANT_NW},
   * </li>
   * <li>
   * {@link GARSGridCoordinate#QUADRANT_NE},
   * </li>
   * <li>
   * {@link GARSGridCoordinate#QUADRANT_SW}, or
   * </li>
   * <li>
   * {@link GARSGridCoordinate#QUADRANT_SE}.
   * </li>
   * </ul>
   *
   * @return one of {@link GARSGridCoordinate#QUADRANT_NW}, {@link GARSGridCoordinate#QUADRANT_NE}, {@link GARSGridCoordinate#QUADRANT_SW}, or {@link GARSGridCoordinate#QUADRANT_SE}.
   */
  public int getQuadrant() {
    if (getCoordinateLevelCount() > QUADRANT) {
      return (1 - fQuadrantY) * 2 + fQuadrantX + 1;
    } else {
      return -1;
    }
  }

  private void setQuadrant(int aQuadrant) {
    if (aQuadrant < 1 || aQuadrant > 4) {
      throw new IllegalArgumentException("Invalid quadrant for a GARS grid coordinate. It should be in the range [1,4]. Instead it was: " + aQuadrant);
    }
    switch (aQuadrant) {
    case (QUADRANT_NE):
      fQuadrantX = 1;
      fQuadrantY = 1;
      break;
    case (QUADRANT_NW):
      fQuadrantX = 0;
      fQuadrantY = 1;
      break;
    case (QUADRANT_SE):
      fQuadrantX = 1;
      fQuadrantY = 0;
      break;
    case (QUADRANT_SW):
      fQuadrantX = 0;
      fQuadrantY = 0;
      break;
    default:
      throw new IndexOutOfBoundsException("GARS quadrant coordinates should be one of NE, NW, SE of SW.");
    }
  }

  public void setCoordinate(int aLevel, int aAxis, int aCoordinate) {
    switch (aLevel) {
    case (CELL):
      if (aAxis == 0) {
        fCellX = aCoordinate;
      } else {
        fCellY = aCoordinate;
      }
      break;
    case (KEYPAD):
      if (aAxis == 0) {
        fKeypadX = aCoordinate;
      } else {
        fKeypadY = aCoordinate;
      }
      break;
    case (QUADRANT):
      if (aAxis == 0) {
        fQuadrantX = aCoordinate;
      } else {
        fQuadrantY = aCoordinate;
      }
      break;
    default:
      throw new IllegalArgumentException("Not a valid level, should be in the range [0,3] for GARS, instead it was " + aLevel);
    }
  }

  public void setCoordinateLevelCount(int aLevelCount) {
    if (!(aLevelCount >= 0 && aLevelCount <= 3)) {
      throw new IllegalArgumentException("not a valid level, should be in the range [0,3] for GARS, instead it was " + (aLevelCount - 1));
    }
    fSpecifiedCoordinateLevelCount = aLevelCount;
  }

  public ILcdEditableMultilevelGridCoordinate cloneAsEditableMultilevelCoordinate() {
    try {
      return (GARSGridCoordinate) clone();
    } catch (CloneNotSupportedException e) {
      throw new InternalError("Could not clone this object as an editable GARS coordinate. Regular clone failed.");
    }
  }

  /**
   * Makes the clone method public.
   * @return A clone of this object. The clone should be at least so deep that when coordinates are changed on the clone
   * they are not changed on the original GARS coordinate.
   * @throws CloneNotSupportedException when cloning is not supported by the implementation.
   */
  public Object clone() throws CloneNotSupportedException {
    try {
      return super.clone();
    } catch (CloneNotSupportedException e) {
      throw new InternalError("Clone not supported by " + this.getClass().getName());
    }
  }

  public void move(int aCellColumn,
                   int aCellRow) {
    fSpecifiedCoordinateLevelCount = CELL + 1;
    setCellRow(aCellRow);
    setCellColumn(aCellColumn);

  }

  public void move(int aCellColumn,
                   int aCellRow,
                   int aQuadrant) {
    fSpecifiedCoordinateLevelCount = QUADRANT + 1;
    setCellRow(aCellRow);
    setCellColumn(aCellColumn);
    setQuadrant(aQuadrant);

  }

  public void move(int aCellColumn,
                   int aCellRow,
                   int aQuadrant,
                   int aKeypad) {
    fSpecifiedCoordinateLevelCount = KEYPAD + 1;
    setCellRow(aCellRow);
    setCellColumn(aCellColumn);
    setQuadrant(aQuadrant);
    setKeyPad(aKeypad);

  }

  public String toString() {
    int levelCount = getCoordinateLevelCount();
    String result = "GARS coordinate: [" + levelCount + "]";
    if (levelCount > 0) {
      result += ":[" + getCellColumn() + "," + getCellRow() + "]";
    }
    if (levelCount > 1) {
      result += ":[" + getQuadrant() + "]";
    }
    if (levelCount > 2) {
      result += ":[" + getKeypad() + "]";
    }
    return result;
  }

}

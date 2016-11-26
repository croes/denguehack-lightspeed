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
package samples.gxy.grid.multilevel.cgrs;

import com.luciad.view.map.multilevelgrid.ILcdEditableMultilevelGridCoordinate;
import com.luciad.view.map.multilevelgrid.ILcdMultilevelGrid;

/**
 * A coordinate designating an element of a CGRS grid.
 * Depending on the level of coordinates specified an element is called a cell, a keypad or a quadrant.
 */
public class CGRSGridCoordinate
    implements ILcdEditableMultilevelGridCoordinate, Cloneable {

  private static final int X_AXIS = 0;
  private static final int Y_AXIS = 1;

  private int fCellX, fCellY;
  private int fKeypadX, fKeypadY;
  private int fQuadrantX, fQuadrantY;

  private int fSpecifiedCoordinateLevelCount = 0;
  /**
   * Value indicating that the cell level is specified.
   * @see #getCoordinateLevelCount()
   */
  public static final int CELL = 0;
  /**
   * Value indicating that the keypad level is specified.
   * @see #getCoordinateLevelCount()
   */
  public static final int KEYPAD = 1;
  /**
   * Value indicating that the quadrant level is specified.
   * @see #getCoordinateLevelCount()
   */
  public static final int QUADRANT = 2;
  /**
   * Value indicating northwest quadrant.
   */
  public static final int QUADRANT_NW = 0;
  /**
   * Value indicating northeast quadrant.
   */
  public static final int QUADRANT_NE = 1;
  /**
   * Value indicating southwest quadrant.
   */
  public static final int QUADRANT_SW = 2;
  /**
   * Value indicating southeast quadrant.
   */
  public static final int QUADRANT_SE = 3;
  /**
   * Array listing all quadrants to be used in iteration over quadrants.
   */
  public int[] QUADRANT_ARRAY = new int[]{QUADRANT_NW, QUADRANT_NE, QUADRANT_SW, QUADRANT_SE};

  /**
   * Creates a new <code>TLcdCGRSGridCoordinate</code>, with <b>0</b> specified levels.
   */
  public CGRSGridCoordinate() {
  }

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
      throw new IndexOutOfBoundsException("No level " + aLevel + " available in CGRS coordinates .CGRS coordinates can only contain 3 levels.");
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
      throw new IllegalArgumentException("In CGRS the cell rows are in the range [1,..], the given cell row was " + aCellRow);
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
      throw new IllegalArgumentException("In CGRS the column rows are in the range [1,..], the given cell row was " + aCellColumn);
    }
    fCellX = aCellColumn - 1;
  }

  /**
   * Gets the keypad of this CGRS Grid Coordinate
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
      throw new IllegalArgumentException("Invalid keypad for a CGRS grid coordinate. It should be in the range [1,9]. Instead it was: " + aKeypad);
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
   * {@link CGRSGridCoordinate#QUADRANT_NW},
   * </li>
   * <li>
   * {@link CGRSGridCoordinate#QUADRANT_NE},
   * </li>
   * <li>
   * {@link CGRSGridCoordinate#QUADRANT_SW}, or
   * </li>
   * <li>
   * {@link CGRSGridCoordinate#QUADRANT_SE}.
   * </li>
   * </ul>
   *
   * @return one of {@link CGRSGridCoordinate#QUADRANT_NW}, {@link CGRSGridCoordinate#QUADRANT_NE}, {@link CGRSGridCoordinate#QUADRANT_SW}, or {@link CGRSGridCoordinate#QUADRANT_SE}.
   */
  public int getQuadrant() {
    if (getCoordinateLevelCount() > QUADRANT) {
      return (1 - fQuadrantY) * 2 + fQuadrantX;
    } else {
      return -1;
    }
  }

  private void setQuadrant(int aQuadrant) {
    if (aQuadrant < 0 || aQuadrant > 3) {
      throw new IllegalArgumentException("Invalid quadrant for a CGRS grid coordinate. It should be in the range [0,3]. Instead it was: " + aQuadrant);
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
      throw new IndexOutOfBoundsException("CGRS quadrant coordinates should be one of NE, NW, SE of SW.");
    }
  }

  public void setCoordinate(int aLevel, int aAxis, int aCoordinate) {
    if (aCoordinate < 0) {
      throw new IllegalArgumentException("Negative coordinates are not allowed");
    }
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
      throw new IllegalArgumentException("Not a valid level, should be in the range [0,3] for CGRS, instead it was " + aLevel);
    }
  }

  public void setCoordinateLevelCount(int aLevelCount) {
    if (!(aLevelCount >= 0 && aLevelCount <= 3)) {
      throw new IllegalArgumentException("not a valid level, should be in the range [0,3] for CGRS, instead it was " + (aLevelCount - 1));
    }
    fSpecifiedCoordinateLevelCount = aLevelCount;
  }

  public ILcdEditableMultilevelGridCoordinate cloneAsEditableMultilevelCoordinate() {
    try {
      return (ILcdEditableMultilevelGridCoordinate) clone();
    } catch (CloneNotSupportedException e) {
      throw new InternalError("Could not clone this object as an editable multilevel coordinate. Regular clone failed.");
    }
  }

  /**
   * Makes the clone method public.
   * @return A clone of this object. The clone should be at least so deep that when coordinates are changed on the clone
   * they are not changed on the original CGRS coordinate.
   * @throws CloneNotSupportedException when cloning is not supported by the implementation.
   */
  public Object clone() throws CloneNotSupportedException {
    try {
      return super.clone();
    } catch (CloneNotSupportedException e) {
      throw new InternalError("Clone not supported by " + this.getClass().getName());
    }
  }

  /**
   * Moves the coordinate to a specified cell. The coordinate will be specified at cell level only.
   * @param aCellRow the cell row to move to.
   * @param aCellColumn the cell column to move to.
   */
  public void move(int aCellColumn,
                   int aCellRow) {
    setCoordinateLevelCount(CELL + 1);
    setCellRow(aCellRow);
    setCellColumn(aCellColumn);

  }

  /**
   * Moves the coordinate to a specified cell and keypad. The coordinate will be specified to keypad level.
   * @param aCellRow the cell row to move to.
   * @param aCellColumn the cell column to move to.
   * @param aKeypad the keypad to move to, ranging from 1 to 9.
   */
  public void move(int aCellColumn,
                   int aCellRow,
                   int aKeypad) {
    setCoordinateLevelCount(KEYPAD + 1);
    setCellRow(aCellRow);
    setCellColumn(aCellColumn);
    setKeyPad(aKeypad);

  }

  /**
   * Moves the coordinate to a specified cell, keypad and quadrant. The coordinate will be specified to quadrant level.
   * @param aCellRow the cell row to move to.
   * @param aCellColumn the cell column to move to.
   * @param aKeypad the keypad to move to, ranging from 1 to 9.
   * @param aQuadrant the quadrant to move to. The value should be one of
   * <ul>
   * <li>
   * {@link CGRSGridCoordinate#QUADRANT_NW},
   * </li>
   * <li>
   * {@link CGRSGridCoordinate#QUADRANT_NE},
   * </li>
   * <li>
   * {@link CGRSGridCoordinate#QUADRANT_SW}, or
   * </li>
   * <li>
   * {@link CGRSGridCoordinate#QUADRANT_SE}.
   * </li>
   * </ul>
   */
  public void move(int aCellColumn,
                   int aCellRow,
                   int aKeypad,
                   int aQuadrant) {
    setCoordinateLevelCount(QUADRANT + 1);
    setCellRow(aCellRow);
    setCellColumn(aCellColumn);
    setKeyPad(aKeypad);
    setQuadrant(aQuadrant);

  }

  /**
   * An object is considered equal to this CGRS grid coordinate when it is a <code>TLcdCGRSGridCoordinate</code>,
   * with the same number of specified levels and the same coordinates at every level.
   * @param aObject the object to check for equality
   * @return true if the object passed is a <code>TLcdCGRSGridCoordinate</code> with the same number of specified
   * levels and the same coordinates at every level.
   */
  public boolean equals(Object aObject) {
    if (aObject instanceof CGRSGridCoordinate) {
      CGRSGridCoordinate cgrs_grid_coordinate = (CGRSGridCoordinate) aObject;
      if (getCoordinateLevelCount() == cgrs_grid_coordinate.getCoordinateLevelCount()) {
        int level_index = 0;
        boolean equal = true;
        while (level_index < getCoordinateLevelCount() && equal) {
          equal = (getCoordinate(level_index, ILcdMultilevelGrid.X_AXIS) == cgrs_grid_coordinate.getCoordinate(level_index, ILcdMultilevelGrid.X_AXIS));
          equal &= (getCoordinate(level_index, ILcdMultilevelGrid.Y_AXIS) == cgrs_grid_coordinate.getCoordinate(level_index, ILcdMultilevelGrid.Y_AXIS));
          level_index++;
        }
        return equal;
      } else {
        return false;
      }
    } else {
      return false;
    }
  }

  /**
   * Takes into account all the coordinates for all specified levels.
   * @return a value taking into account all the coordinates for all specified levels.
   */
  public int hashCode() {
    int hash = 37;
    for (int level_index = 0; level_index < getCoordinateLevelCount(); level_index++) {
      hash += 11 * getCoordinate(level_index, ILcdMultilevelGrid.X_AXIS) + 13 * getCoordinate(level_index, ILcdMultilevelGrid.Y_AXIS);
    }
    return hash;
  }

  public String toString() {
    int levelCount = getCoordinateLevelCount();
    String result = "CGRS coordinate: [" + levelCount + "]";
    if (levelCount > 0) {
      result += ":[" + getCellColumn() + "," + getCellRow() + "]";
    }
    if (levelCount > 1) {
      result += ":[" + getKeypad() + "]";
    }
    if (levelCount > 2) {
      result += ":[" + getQuadrant() + "]";
    }
    return result;
  }
}

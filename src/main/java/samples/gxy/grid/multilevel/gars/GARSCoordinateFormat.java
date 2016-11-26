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

import java.text.FieldPosition;
import java.text.ParseException;
import java.text.ParsePosition;

import com.luciad.view.map.multilevelgrid.ALcdMultilevelGridCoordinateFormat;
import com.luciad.view.map.multilevelgrid.ILcdMultilevelGridCoordinate;

/**
 * Formats a <code>ILcdGARSCoordinate</code> to a human readable form and constructs a
 * <code>ILcdGARSEditableCoordinate</code> from a String.
 */
public class GARSCoordinateFormat
    extends ALcdMultilevelGridCoordinateFormat {

  /**
   * Formats a <code>ILcdGARSCoordinate</code> into a human readable form by calling
   * formatCell, formatQuadrant and formatKeypad.
   * @param aObject the <code>ILcdGARSCoordinate</code> to return a human readable form for.
   * @param aStringBuffer the StringBuffer to append to/to modify.
   * @param aPosition identifies a field in the formatted text.
   * @return a StringBuffer containing a human readable representation of the object passed.
   * @throws IllegalArgumentException when the object pased is not a <code>ILcdMultilevelCoordinate</code>
   */
  // keep this method for the adapted documentation.
  public StringBuffer format(Object aObject, StringBuffer aStringBuffer, FieldPosition aPosition) {
    return super.format(aObject, aStringBuffer, aPosition);
  }

  /**
   * Depending on the level passed calls formatCell, formatKeypad or formatQuadrant.
   * @param aStringBuffer the StringBuffer to append to/to modify.
   * @param aLevel the level to format the coordinates for
   * @param aMultilevelGridCoordinate the coordinate to format
   * @param aPosition a <code>FieldPosition</code> identifying a field in the formatted text.
   * @return a StringBuffer containing the information of the coordinates at the given level in human readable form.
   */
  public StringBuffer formatCoordinate(ILcdMultilevelGridCoordinate aMultilevelGridCoordinate,
                                       int aLevel,
                                       StringBuffer aStringBuffer,
                                       FieldPosition aPosition) {
    switch (aLevel) {
    case GARSGridCoordinate.CELL:
      return formatCell(aStringBuffer,
                        GARSGridUtil.getCellColumn(aMultilevelGridCoordinate),
                        GARSGridUtil.getCellRow(aMultilevelGridCoordinate));
    case GARSGridCoordinate.KEYPAD:
      return formatKeypad(aStringBuffer, GARSGridUtil.getKeypad(aMultilevelGridCoordinate));
    case GARSGridCoordinate.QUADRANT:
      return formatQuadrant(aStringBuffer, GARSGridUtil.getQuadrant(aMultilevelGridCoordinate));
    default:
      throw new IllegalArgumentException("GARS can only have 3 levels.");
    }
  }

  /**
   * Appends the GARS notation for the quadrant to the given StringBuffer.
   * @param aStringBuffer the StringBuffer to append to/to modify.
   * @param aQuadrant the index of the quadrant. One of
   * one of {@link GARSGridCoordinate#QUADRANT_NW},
   * {@link GARSGridCoordinate#QUADRANT_NE},
   * {@link GARSGridCoordinate#QUADRANT_SW},
   * {@link GARSGridCoordinate#QUADRANT_SE},
   * @return a StringBuffer containing the added information on the quadrant.
   */
  protected StringBuffer formatQuadrant(StringBuffer aStringBuffer, int aQuadrant) {
    switch (aQuadrant) {
    case GARSGridCoordinate.QUADRANT_NW:
      aStringBuffer.append("1");
      break;
    case GARSGridCoordinate.QUADRANT_NE:
      aStringBuffer.append("2");
      break;
    case GARSGridCoordinate.QUADRANT_SW:
      aStringBuffer.append("3");
      break;
    case GARSGridCoordinate.QUADRANT_SE:
      aStringBuffer.append("4");
      break;
    default:
    }
    return aStringBuffer;
  }

  /**
   * Appends the cell row as a number and the cell column as a letter, starting from A.
   * @param aStringBuffer the StringBuffer to append to/to modify.
   * @param aCellColumn the cells column, equal to or greater than 0
   * @param aCellRow the cells row, equal to or greater than 0
   * @return a StringBuffer containing the added information on the cell location.
   */
  protected StringBuffer formatCell(StringBuffer aStringBuffer, int aCellColumn, int aCellRow) {
    aStringBuffer = formatCellColumnName(aStringBuffer, aCellColumn);
    return formatCellRowName(aStringBuffer, aCellRow);
  }

  private StringBuffer formatCellRowName(StringBuffer aStringBuffer, int aCellRow) {
    int first_char = (aCellRow - 1) / 24;
    int second_char = (aCellRow - 1) % 24;
    aStringBuffer.append(formatCellRow(first_char));
    aStringBuffer.append(formatCellRow(second_char));
    return aStringBuffer;
  }

  // transforms a cell row integer range 0..23 into a char range A-Z, omitting I and O
  private char formatCellRow(int aCellRow) {
    int remainder = aCellRow;
    if (remainder > 12) { // 0= A, 14-1(I) = O
      remainder += 2;
    } else if (remainder > 7) { // 0 = A, 8 = I.
      remainder++;
    }
    return (char) ('A' + (char) remainder);
  }

  // transforms an A-Z char into an integer range 0..23, omitting I and O
  private int parseCellRow(char aCellRowName) {
    int number = aCellRowName - 'A'; // 0..25
    if (number == 14 || number == 8) {
      throw new IllegalArgumentException(aCellRowName + " is not a valid GARS cell row character.");
    }
    if (number > 14) { // Compensate for I and O
      number -= 2;
    } else if (number > 8) { // Compensate for I
      number--;
    }
    return number;
  }

  private StringBuffer formatCellColumnName(StringBuffer aStringBuffer, int aCellColumn) {
    if (aCellColumn < 1 || aCellColumn > 999) {
      throw new IllegalArgumentException("an illegal GARS cell column index, the number should be in the range of [1,999], instead it was " + aCellColumn);
    }
    if (aCellColumn < 10) {
      aStringBuffer.append("00");
    } else if (aCellColumn < 100) {
      aStringBuffer.append("0");
    }
    aStringBuffer.append(aCellColumn);
    return aStringBuffer;
  }

  /**
   * Parses a string to a GARS grid coordinate
   *
   * @param aSource        the source that is parsed
   * @param aParsePosition (is not used)
   * @return A GARS grid coordinate
   */
  public Object parseObject(String aSource, ParsePosition aParsePosition) {
    int originalParsePos = aParsePosition.getIndex();
    try {
      if (aSource == null || aSource.length() == 0) {
        throw new ParseException("To parse a GARS Coordinate, the string should not be null pointer or should have length > 0", originalParsePos);
      }

      GARSGridCoordinate coordinate = createEditableGridCoordinate();

      int cell_x_coordinate = parseCellColumn(aSource, aParsePosition);
      int cell_y_coordinate = parseCellRow(aSource, aParsePosition);

      if (aParsePosition.getIndex() < aSource.length()) {
        int quadrant = parseQuadrant(aSource, aParsePosition);

        if (aParsePosition.getIndex() < aSource.length()) {
          int keypad = parseKeypad(aSource, aParsePosition);
          coordinate.move(cell_x_coordinate, cell_y_coordinate, quadrant, keypad);
          return coordinate;
        }

        coordinate.move(cell_x_coordinate, cell_y_coordinate, quadrant);
        return coordinate;
      }
      coordinate.move(cell_x_coordinate, cell_y_coordinate);
      return coordinate;
    } catch (ParseException ex) {
      //Contract of super class requires to leave the parse position untouched, set the error
      //index and return null
      aParsePosition.setIndex(originalParsePos);
      aParsePosition.setErrorIndex(ex.getErrorOffset());
      return null;
    }
  }

  /**
   * @param aSource the String to parse the cell column from.
   * @param aParsePositionSFCT the position at which the cell column information is stored.
   * @return an array: the first element is the column coordinate
   *         the second parameter is the parseposition in aSource that is the first to be read
   * @throws ParseException In case the given string can't be parsed
   */
  private int parseCellColumn(String aSource, ParsePosition aParsePositionSFCT) throws ParseException {
    int parsePos = aParsePositionSFCT.getIndex();
    if (parsePos + 3 > aSource.length()) {
      throw new ParseException("The cell column symbol is too short, should be 3 digits", aParsePositionSFCT.getIndex());
    }
    String column_string_coordinate = aSource.substring(parsePos, parsePos + 3);
    try {
      int column_coordinate = Integer.parseInt(column_string_coordinate);
      aParsePositionSFCT.setIndex(aParsePositionSFCT.getIndex() + 3);
      return column_coordinate;
    } catch (NumberFormatException e) {
      throw new ParseException("The cell column symbol in GARS should be 3 digits, now it was " + column_string_coordinate, parsePos);
    }
  }

  /**
   * @param aSource the String to parse the cell row from.
   * @param aParsePositionSFCT the position at which the cell row information is stored.
   * @return the first element in the array us the row coordinate
   *         the second parameter is the parseposition
   * @throws ParseException In case the given string can't be parsed
   */
  private int parseCellRow(String aSource, ParsePosition aParsePositionSFCT) throws ParseException {
    if (aParsePositionSFCT.getIndex() + 2 > aSource.length()) {
      throw new ParseException("The cell row symbol is too short, should be 2 letters", aParsePositionSFCT.getIndex());
    }
    char first_letter = aSource.charAt(aParsePositionSFCT.getIndex());
    aParsePositionSFCT.setIndex(aParsePositionSFCT.getIndex() + 1);
    char second_letter = aSource.charAt(aParsePositionSFCT.getIndex());
    if (!Character.isLetter(first_letter) || !Character.isLetter(second_letter)) {
      throw new ParseException("The cell row symbol in GARS should be 2 letters, now it was " + first_letter + " and " + second_letter, aParsePositionSFCT.getIndex());
    }
    int cell_coordinate = 24 * parseCellRow(first_letter) + parseCellRow(second_letter) + 1;
    aParsePositionSFCT.setIndex(aParsePositionSFCT.getIndex() + 1);

    return cell_coordinate;
  }

  /**
   * @param aSource the String to parse the quadrant from.
   * @param aParsePositionSFCT the position at which the quadrant information is stored.
   * @return the first parameter is the quadrant, the second parameter is the parseposition
   * @throws ParseException In case the given string can't be parsed
   */
  private int parseQuadrant(String aSource, ParsePosition aParsePositionSFCT) throws ParseException {
    char value = aSource.charAt(aParsePositionSFCT.getIndex());
    if (!Character.isDigit(value)) {
      throw new IllegalArgumentException("The quadrant symbol in GARS should be a digit, now it was " + value);
    }
    int digit = Integer.parseInt(aSource.substring(aParsePositionSFCT.getIndex(), aParsePositionSFCT.getIndex() + 1));
    aParsePositionSFCT.setIndex(aParsePositionSFCT.getIndex() + 1);

    return digit;
  }

  /**
   * @param aSource the String to parse the keypad from.
   * @param aParsePositionSFCT the position at which the keypad information is stored.
   * @return the first parameter is the keypad value, the second parameter is the parseposition
   * @throws ParseException In case the given string can't be parsed
   */
  private int parseKeypad(String aSource, ParsePosition aParsePositionSFCT) throws ParseException {
    char value = aSource.charAt(aParsePositionSFCT.getIndex());
    if (!Character.isDigit(value)) {
      throw new ParseException("The keypad symbol in GARS should be a digit, now it was " + value, aParsePositionSFCT.getIndex());
    }
    int digit = Integer.parseInt(aSource.substring(aParsePositionSFCT.getIndex(), aParsePositionSFCT.getIndex() + 1));
    aParsePositionSFCT.setIndex(aParsePositionSFCT.getIndex() + 1);
    return digit;
  }

  /**
   * This method is called by {@link #parseObject(String, ParsePosition)}.
   * @return a TLcdGARSGridCoordinate instance.
   */
  protected GARSGridCoordinate createEditableGridCoordinate() {
    return new GARSGridCoordinate();
  }

  /**
   * Appends the keypad to the given StringBuffer.
   * @param aStringBuffer the StringBuffer to append to/to modify.
   * @param aKeyPad the index of the keypad
   * @return a StringBuffer containing the added information on the keypad.
   */
  protected StringBuffer formatKeypad(StringBuffer aStringBuffer, int aKeyPad) {
    aStringBuffer.append(aKeyPad);
    return aStringBuffer;
  }
}

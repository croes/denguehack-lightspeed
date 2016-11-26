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

import java.text.FieldPosition;
import java.text.ParsePosition;

import com.luciad.view.map.multilevelgrid.ALcdMultilevelGridCoordinateFormat;
import com.luciad.view.map.multilevelgrid.ILcdMultilevelGridCoordinate;

/**
 * Formats a <code>ILcdCGRSCoordinate</code> to a human readable form and constructs a
 * <code>ILcdCGRSEditableCoordinate</code> from a String.
 */
public class CGRSCoordinateFormat
    extends ALcdMultilevelGridCoordinateFormat {

  /**
   * Formats a <code>ILcdCGRSCoordinate</code> into a human readable form by calling
   * formatCell, formatKeypad, formatQuadrant.
   *
   * @param aObject the <code>ILcdCGRSCoordinate</code> to return a human readable form for.
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
    case CGRSGridCoordinate.CELL:
      return formatCell(aStringBuffer,
                        CGRSGridUtil.getCellColumn(aMultilevelGridCoordinate),
                        CGRSGridUtil.getCellRow(aMultilevelGridCoordinate));
    case CGRSGridCoordinate.KEYPAD:
      return formatKeypad(aStringBuffer, CGRSGridUtil.getKeypad(aMultilevelGridCoordinate));
    case CGRSGridCoordinate.QUADRANT:
      return formatQuadrant(aStringBuffer, CGRSGridUtil.getQuadrant(aMultilevelGridCoordinate));
    default:
      throw new IllegalArgumentException("CGRS can only have 3 levels.");
    }
  }

  /**
   * Appends the CGRS notation for the quadrant to the given StringBuffer.
   * @param aStringBuffer the StringBuffer to append to/to modify.
   * @param aQuadrant the index of the quadrant. One of
   * one of {@link CGRSGridCoordinate#QUADRANT_NW},
   * {@link CGRSGridCoordinate#QUADRANT_NE},
   * {@link CGRSGridCoordinate#QUADRANT_SW},
   * {@link CGRSGridCoordinate#QUADRANT_SE},
   * @return a StringBuffer containing the added information on the quadrant.
   */
  protected StringBuffer formatQuadrant(StringBuffer aStringBuffer, int aQuadrant) {
    switch (aQuadrant) {
    case CGRSGridCoordinate.QUADRANT_NW:
      aStringBuffer.append("NW");
      break;
    case CGRSGridCoordinate.QUADRANT_NE:
      aStringBuffer.append("NE");
      break;
    case CGRSGridCoordinate.QUADRANT_SW:
      aStringBuffer.append("SW");
      break;
    case CGRSGridCoordinate.QUADRANT_SE:
      aStringBuffer.append("SE");
      break;
    default:
    }
    return aStringBuffer;
  }

  /**
   * Appends the cell row as a number and the cell column as a letter, starting from A.
   * @param aStringBuffer the StringBuffer to append to/to modify.
   * @param aCellColumn the cells column, equal to or greater than 1
   * @param aCellRow the cells row, equal to or greater than 1
   * @return a StringBuffer containing the added information on the cell location.
   */
  protected StringBuffer formatCell(StringBuffer aStringBuffer, int aCellColumn, int aCellRow) {
    aStringBuffer.append(aCellRow);
    formatCellColumnName(aStringBuffer, aCellColumn);
    return aStringBuffer;
  }

  private StringBuffer formatCellColumnName(StringBuffer aStringBuffer, int aCellColumn) {
    int location = aStringBuffer.length();
    int quotient = aCellColumn;
    while (quotient > 0) {
      // take into account that I and O should be left out.
      int remainder = (quotient - 1) % 24;
      if (remainder > 12) { // 0= A, 14-1(I) = O
        remainder += 2;
      } else if (remainder > 7) { // 0 = A, 8 = I.
        remainder++;
      }
      char character = (char) ('A' + (char) remainder);
      aStringBuffer.insert(location, character);
      quotient = (quotient - 1) / 24;
    }
    return aStringBuffer;
  }

  /**
   * Parses a String to a CGRS grid coordinate
   * @param aStringToParse the source that is parsed
   * @param aParsePosition not taken into account
   * @return a CGRS grid coordinate
   */
  public Object parseObject(String aStringToParse, ParsePosition aParsePosition) {
    if (aStringToParse == null || aStringToParse.length() == 0) {
      throw new IllegalArgumentException("To parse a CGRS Coordinate, the string should not be null pointer or should have length > 0");
    }
    //done to avoid an exception thrown by parseObject
    if (aParsePosition != null) {
      aParsePosition.setIndex(1);
    }

    CGRSGridCoordinate coordinate = createEditableGridCoordinate();

    int[] row_and_parse_position = parseCellRow(aStringToParse, 0);
    int parse_position = row_and_parse_position[1];
    int cell_y_coordinate = row_and_parse_position[0];

    int[] column_and_parse_position = parseCellColumn(aStringToParse, parse_position);
    parse_position = column_and_parse_position[1];
    int cell_x_coordinate = column_and_parse_position[0];

    if (parse_position < aStringToParse.length()) {
      int[] keypad_and_parse_position = parseKeypad(aStringToParse, parse_position);
      parse_position = keypad_and_parse_position[1];
      int keypad = keypad_and_parse_position[0];

      if (parse_position < aStringToParse.length()) {
        int[] quadrant_and_parse_position = parseQuadrant(aStringToParse, parse_position);
        int quadrant = quadrant_and_parse_position[0];
        coordinate.move(cell_x_coordinate, cell_y_coordinate, keypad, quadrant);
        return coordinate;
      }

      coordinate.move(cell_x_coordinate, cell_y_coordinate, keypad);
      return coordinate;
    }
    coordinate.move(cell_x_coordinate, cell_y_coordinate);
    return coordinate;
  }

  /**
   * @param aStringToParse the string to parse
   * @param aParsePosition the position at which to start parsing
   * @return an array: the first element is the column coordinate
   *         the second parameter is the parseposition in aStringToParse that is the first to be read
   */
  private int[] parseCellColumn(String aStringToParse, int aParsePosition) {
    int start_position = aParsePosition;
    while (aParsePosition < aStringToParse.length() && Character.isLetter(aStringToParse.charAt(aParsePosition))) {
      aParsePosition++;
    }
    if (aParsePosition == start_position) {
      throw new IllegalArgumentException("In CGRS the cell column symbol must exist at least with one letter");
    }

    int cell_column_coordinate = 0;
    for (int index = start_position; index < aParsePosition; index++) {
      char temp_char = aStringToParse.charAt(index);
      int value = temp_char + 1 - 'A';
      // take into account that O and I were left out.
      if (value > 15) {
        value -= 2;
      } else if (value > 9) {
        value--;
      }
      cell_column_coordinate = cell_column_coordinate * 24 + value;
    }
    return new int[]{cell_column_coordinate, aParsePosition};
  }

  /**
   * @param aStringToParse the string to parse
   * @param aParsePosition the position at which to start parsing
   * @return the first element in the array us the row coordinate
   *         the second parameter is the parseposition
   */
  private int[] parseCellRow(String aStringToParse, int aParsePosition) {
    int start_position = aParsePosition;
    while (aParsePosition < aStringToParse.length() && Character.isDigit(aStringToParse.charAt(aParsePosition))) {
      aParsePosition++;
    }
    if (aParsePosition == start_position) {
      throw new IllegalArgumentException("In CGRS the cell row symbol must exist at least with one letter");
    }

    String row_string_coordinate = aStringToParse.substring(start_position, aParsePosition);
    int cell_row_coordinate = Integer.parseInt(row_string_coordinate);

    return new int[]{cell_row_coordinate, aParsePosition};
  }

  /**
   * @param aStringToParse the string to parse
   * @param aParsePosition the position at which to start parsing
   * @return the first parameter is the quadrant, the second parameter is the parseposition
   */
  private int[] parseQuadrant(String aStringToParse, int aParsePosition) {
    String quadrant = aStringToParse.substring(aParsePosition, aParsePosition + 2);
    quadrant = quadrant.toUpperCase();
    if (quadrant.equals("NW")) {
      return new int[]{CGRSGridCoordinate.QUADRANT_NW, aParsePosition + 2};
    } else if (quadrant.equals("NE")) {
      return new int[]{CGRSGridCoordinate.QUADRANT_NE, aParsePosition + 2};
    } else if (quadrant.equals("SW")) {
      return new int[]{CGRSGridCoordinate.QUADRANT_SW, aParsePosition + 2};
    } else if (quadrant.equals("SE")) {
      return new int[]{CGRSGridCoordinate.QUADRANT_SE, aParsePosition + 2};
    } else {
      throw new IllegalArgumentException("The quadrant symbol in CGRS should be a NE,NW,SE or SW, now it was " + quadrant);
    }

  }

  /**
   * @param aStringToParse the string to parse
   * @param aParsePosition the position at which to start parsing
   * @return the first parameter is the keypad value, the second parameter is the parseposition
   */
  private int[] parseKeypad(String aStringToParse, int aParsePosition) {
    char value = aStringToParse.charAt(aParsePosition);
    if (!Character.isDigit(value)) {
      throw new IllegalArgumentException("The keypad symbol in CGRS should be a digit, now it was " + value);
    }
    int digit = Integer.parseInt(aStringToParse.substring(aParsePosition, aParsePosition + 1));

    return new int[]{digit, aParsePosition + 1};
  }

  /**
   * Creates a <code>TLcdCGRSGridCoordinate</code>. This method is called from {@link #parseObject(String)}.
   * @return a TLcdCGRSGridCoordinate instance.
   */
  protected CGRSGridCoordinate createEditableGridCoordinate() {
    return new CGRSGridCoordinate();
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

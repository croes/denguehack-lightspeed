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
package samples.common.text;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.ParsePosition;

/**
 * Class containing some utility methods for parsers.
 */
public final class ParseUtil {

  private ParseUtil() {
  }

  /**
   * Return true if the given character is a white space character, false otherwise.
   * @param aChar The character to test.
   * @return True if the given character is a white space character, false otherwise.
   */
  private static boolean isWhiteSpaceCharacter(char aChar) {
    //the space character, the tab character, the newline character, the carriage-return character, and the form-feed character.
    if (aChar == ' ' || aChar == '\t' || aChar == '\n' || aChar == '\r' || aChar == '\f') {
      return true;
    } else {
      return false;
    }
  }

  /**
   * Advances the parse position after the leading white space.
   * @param aToParse The string to consider
   * @param aParsePosition Input-Output parameter.  Input: parse position to start at.
   * Output: just behind white space encountered just after parse position at input
   */
  public static void parsePositionAfterWhiteSpace(String aToParse, ParsePosition aParsePosition) {
    int parse_index = aParsePosition.getIndex();
    while (parse_index < aToParse.length() && isWhiteSpaceCharacter(aToParse.charAt(parse_index))) {
      parse_index++;
    }
    aParsePosition.setIndex(parse_index);
  }

  /**
   * Advances the parse position just after the given string if it can be found (case sensitive),
   * does nothing otherwise.
   * It will find the string if it is the first substring that will be encountered. It needs
   * to be the first substring, although leading white space will be ignored.
   * This method can be useful to advance the parse position after a unit or a separator symbol.
   * @param aToParse The string to parse
   * @param aParsePosition Input-output.  Input: start position.  Output: just after last
   * character of aString if it could be found, unchanged otherwise
   * @param aString The string to advance the parse position after
   * @return True if the given string was found as the first non white space item, false otherwise.
   */
  public static boolean parsePositionAfterString(String aToParse, ParsePosition aParsePosition, String aString) {
    return parsePositionAfterString(aToParse, aParsePosition, aString, false);
  }

  /**
   * Advances the parse position just after the given string if it can be found, does
   * nothing otherwise.
   * It will find the string if it is the first thing that will be encountered.  It needs
   * to be the first thing, although leading white space will be ignored.
   * This method can be useful to advance the parse position after a unit or a separator symbol.
   * @param aToParse The string to parse
   * @param aParsePosition Input-output.  Input: start position.  Output: just after last
   * character of aString if it could be found, unchanged otherwise
   * @param aString The string to advance the parse position after
   * @param aIgnoreCase Indicates case sensitive checking or not.
   * @return True if the given string was found as the first non white space item, false otherwise.
   */
  public static boolean parsePositionAfterString(String aToParse, ParsePosition aParsePosition, String aString, boolean aIgnoreCase) {
    int original_index = aParsePosition.getIndex();

    parsePositionAfterWhiteSpace(aToParse, aParsePosition);

    String trimmed_string = aString.trim();
    if (aToParse.regionMatches(aIgnoreCase, aParsePosition.getIndex(), trimmed_string, 0, trimmed_string.length())) {
      aParsePosition.setIndex(aParsePosition.getIndex() + trimmed_string.length());
      return true;
    } else {
      aParsePosition.setIndex(original_index);
      return false;
    }
  }

  /**
   * Parses the first number in a string, starting at the given parse position.
   * Leading white space is ignored. The actual parsing of the number is
   * delegated to the given NumberFormat.
   *
   * @param aString The string to parse
   * @param aNumberFormat The number format to delegate the parsing to.  This
   * allows you to control the locale, the number of fraction digits etc.
   * @param aParsePosition Input-output parameter.  Input: start index.
   * Output: just behind last parsed character
   * @return The parsed number.
   * @throws ParseException In case no number could be found.
   */
  public static Number parseFirstNumber(String aString, NumberFormat aNumberFormat, ParsePosition aParsePosition) throws ParseException {
    //ignore white space
    parsePositionAfterWhiteSpace(aString, aParsePosition);

    //parse the number
    Number parsed_number = aNumberFormat.parse(aString, aParsePosition);
    if (parsed_number != null) {
      return parsed_number;
    } else {
      throw new ParseException("Parsing failed at index[" + aParsePosition.getIndex() + "] in string [" + aString + "]", aParsePosition.getIndex());
    }
  }
}

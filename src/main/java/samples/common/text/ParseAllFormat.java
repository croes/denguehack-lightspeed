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

import java.text.AttributedCharacterIterator;
import java.text.FieldPosition;
import java.text.Format;
import java.text.ParseException;
import java.text.ParsePosition;

/**
 * Format which only succeeds in parsing when everything of the given string is parsed.
 * The actual parsing is delegated to another format.
 */
public class ParseAllFormat extends Format {
  private final Format fDelegateFormat;

  public ParseAllFormat(Format aDelegateFormat) {
    fDelegateFormat = aDelegateFormat;
  }

  public Format getDelegateFormat() {
    return fDelegateFormat;
  }

  @Override
  public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
    return fDelegateFormat.format(obj, toAppendTo, pos);
  }

  @Override
  public AttributedCharacterIterator formatToCharacterIterator(Object obj) {
    return fDelegateFormat.formatToCharacterIterator(obj);
  }

  @Override
  public Object parseObject(String source, ParsePosition pos) {
    int before_parse_index = pos.getIndex();
    Object object = fDelegateFormat.parseObject(source, pos);

    //If error in delegate format
    if (pos.getErrorIndex() != -1) {
      //pos should be correct.
      return null;
    }
    //If not everything of the string was parsed, return null to indicate failure
    else if (pos.getIndex() < source.length()) {
      pos.setErrorIndex(pos.getIndex());
      pos.setIndex(before_parse_index);
      return null;
    } else {
      return object;
    }
  }

  @Override
  public Object parseObject(String source) throws ParseException {
    //This is more correct then the implementation in Format (pos.index can be 0 for empty string).
    ParsePosition pos = new ParsePosition(0);
    Object object = parseObject(source, pos);
    if (pos.getErrorIndex() == -1) {
      return object;
    }
    throw new ParseException("", pos.getErrorIndex());
  }
}

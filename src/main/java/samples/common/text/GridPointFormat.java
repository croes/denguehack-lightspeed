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

import java.text.FieldPosition;
import java.text.Format;
import java.text.ParseException;
import java.text.ParsePosition;

import com.luciad.shape.ILcdPoint;
import com.luciad.shape.shape2D.ILcd2DEditablePoint;
import com.luciad.shape.shape3D.TLcdXYZPoint;
import com.luciad.text.TLcdDistanceFormat;
import com.luciad.util.TLcdDistanceUnit;

/**
 * <p>Format that can be used to format grid points.</p>
 *
 * <p>For example</p>
 *
 * <pre class="code">
 *   DecimalFormat decimalFormat = new DecimalFormat();
 *   GridPointFormat gridPointFormat = new GridPointFormat();
 *   gridPointFormat.setDistanceFormat(decimalFormat);
 *   ILcdPoint point = new TLcdXYPoint(123456.7, 987654.3);
 *   System.out.println(gridPointFormat.format(point));
 *
 *   // Results in "123,456.7, 987,654.3"
 * </pre>
 */
public class GridPointFormat extends Format {

  public static final GridPointFormat DEFAULT = new GridPointFormat();

  private Format fDistanceFormat = new TLcdDistanceFormat(TLcdDistanceUnit.METRE_UNIT);
  private boolean fXBeforeY = true;
  private String fOrdinateSeparator = ", ";

  public GridPointFormat() {
  }

  public GridPointFormat(Format aDistanceFormat) {
    fDistanceFormat = aDistanceFormat;
  }

  public Format getDistanceFormat() {
    return fDistanceFormat;
  }

  public void setDistanceFormat(Format aDistanceFormat) {
    fDistanceFormat = aDistanceFormat;
  }

  public boolean isXBeforeY() {
    return fXBeforeY;
  }

  public void setXBeforeY(boolean aXBeforeY) {
    fXBeforeY = aXBeforeY;
  }

  public String getOrdinateSeparator() {
    return fOrdinateSeparator;
  }

  public void setOrdinateSeparator(String aOrdinateSeparator) {
    fOrdinateSeparator = aOrdinateSeparator;
  }

  @Override
  public StringBuffer format(Object aObject,
                             StringBuffer aToAppendTo,
                             FieldPosition aPos) {
    if (aObject instanceof ILcdPoint) {
      formatPointSFCT((ILcdPoint) aObject, aPos, aToAppendTo);
      return aToAppendTo;
    } else {
      throw new IllegalArgumentException("Object to format must be a ILcdPoint");
    }
  }

  public void formatPointSFCT(ILcdPoint aPoint,
                              FieldPosition aPos,
                              StringBuffer aToAppendToSFCT) {
    if (fXBeforeY) {
      formatXSFCT(aPoint.getX(), aPos, aToAppendToSFCT);
      aToAppendToSFCT.append(fOrdinateSeparator);
      formatYSFCT(aPoint.getY(), aPos, aToAppendToSFCT);
    } else {
      formatYSFCT(aPoint.getY(), aPos, aToAppendToSFCT);
      aToAppendToSFCT.append(fOrdinateSeparator);
      formatXSFCT(aPoint.getX(), aPos, aToAppendToSFCT);
    }
  }

  private void formatXSFCT(double aX,
                           FieldPosition aPos,
                           StringBuffer aToAppendToSFCT) {
    fDistanceFormat.format(aX, aToAppendToSFCT, aPos);
  }

  private void formatYSFCT(double aY,
                           FieldPosition aPos,
                           StringBuffer aToAppendToSFCT) {
    fDistanceFormat.format(aY, aToAppendToSFCT, aPos);
  }

  @Override
  public Object parseObject(String aSource, ParsePosition aStatus) {
    int startIndex = aStatus.getIndex();
    try {
      return parsePoint(aSource, aStatus);
    } catch (ParseException e) {
      aStatus.setIndex(startIndex);
      return null;
    }
  }

  public ILcdPoint parsePoint(String aPointString) throws ParseException {
    ParsePosition status = new ParsePosition(0);
    ILcdPoint point = parsePoint(aPointString, status);
    if (status.getIndex() == 0) {
      throw new ParseException(
          "GridPointFormat.parsePoint(String) failed",
          status.getErrorIndex());
    }
    return point;
  }

  public ILcdPoint parsePoint(String aPointString,
                              ParsePosition aStatus) throws ParseException {
    ILcd2DEditablePoint pointSFCT = new TLcdXYZPoint();
    parsePointSFCT(aPointString, aStatus, pointSFCT);
    return pointSFCT;
  }

  public void parsePointSFCT(String aPointString,
                             ParsePosition aStatus,
                             ILcd2DEditablePoint aPointSFCT) throws ParseException {
    double x;
    double y;
    if (fXBeforeY) {
      x = parseX(aPointString, aStatus);
      ParseUtil.parsePositionAfterString(aPointString, aStatus, fOrdinateSeparator, true);

      // Remove leading white space from second part
      ParseUtil.parsePositionAfterWhiteSpace(aPointString, aStatus);
      y = parseY(aPointString, aStatus);
    } else {
      y = parseY(aPointString, aStatus);
      ParseUtil.parsePositionAfterString(aPointString, aStatus, fOrdinateSeparator, true);

      // Remove leading white space from second part
      ParseUtil.parsePositionAfterWhiteSpace(aPointString, aStatus);
      x = parseX(aPointString, aStatus);
    }

    aPointSFCT.move2D(x, y);
  }

  private double parseX(String aXString,
                        ParsePosition aStatus) throws ParseException {
    Number x = (Number) fDistanceFormat.parseObject(aXString, aStatus);
    if (x != null) {
      return x.doubleValue();
    } else {
      throw new ParseException("Distance format returned null for x", aStatus.getErrorIndex());
    }
  }

  private double parseY(String aYString,
                        ParsePosition aStatus) throws ParseException {
    Number y = (Number) fDistanceFormat.parseObject(aYString, aStatus);
    if (y != null) {
      return y.doubleValue();
    } else {
      throw new ParseException("Distance format returned null for y", aStatus.getErrorIndex());
    }
  }
}

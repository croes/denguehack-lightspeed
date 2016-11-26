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
package samples.lightspeed.style.strokedline;

import static com.luciad.view.gxy.TLcdGXYHatchedFillStyle.Pattern.BACK_SLASH;
import static com.luciad.view.lightspeed.style.complexstroke.ALspComplexStroke.*;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Random;

import com.luciad.geodesy.TLcdEllipsoid;
import com.luciad.gui.ILcdIcon;
import com.luciad.shape.ILcdPoint;
import com.luciad.view.gxy.TLcdGXYHatchedFillStyle;
import com.luciad.view.lightspeed.style.ALspStyle;
import com.luciad.view.lightspeed.style.TLspComplexStrokedLineStyle;
import com.luciad.view.lightspeed.style.TLspTextStyle;
import com.luciad.view.lightspeed.style.complexstroke.ALspComplexStroke;

/**
 * Class that is able to create a number of complex stroke styles.
 * For more information about how to create complex strokes, see {@link ALspComplexStroke} or at the
 * knowledge base article that explains step-by-step how to create complex strokes.
 */
class ComplexStrokeFactory {

  private static final ILcdIcon RESTRICTED_ICON;

  static {
    // Create a tileable hatched icon that can be used for restricted airspace styles.
    TLcdGXYHatchedFillStyle hatchStyle = new TLcdGXYHatchedFillStyle(EnumSet.of(BACK_SLASH), Color.white);
    hatchStyle.setPatternSize(new Dimension(5, 5));
    hatchStyle.setLineWidth(2);
    RESTRICTED_ICON = hatchStyle.asIcon();
  }

  enum ComplexStrokeType {
    RAILROAD,
    HIGHWAY,
    POWER_LINE,
    PIPELINE,
    FENCE,
    WARM_FRONT,
    COLD_FRONT,
    OCCLUDED_FRONT
  }

  static List<ALspStyle> create(ComplexStrokeType aType, int aSizeFactor, Color aColor) {
    ALspStyle style;
    double lengthFactor = 1 + (aSizeFactor - 1) / 2.0;
    switch (aType) {
    case RAILROAD: {
      double length = 20 * lengthFactor;

      ALspComplexStroke blackBlock = filledRect().length(length).height(-aSizeFactor * 2, aSizeFactor * 2).fillColor(aColor).build();
      // Compose the black block and a slightly smaller white block to object a white block with a black halo.
      ALspComplexStroke smallWhiteBlock = filledRect().length(length).height(-aSizeFactor, aSizeFactor).fillColor(Color.white).build();
      ALspComplexStroke whiteBlock = compose(blackBlock, smallWhiteBlock);

      // Append the white and black block. By using them as fallback stroke, they will be repeated along the line,
      // and no blocks will be omitted at sharp corners.
      style = TLspComplexStrokedLineStyle.newBuilder()
                                         .fallback(append(blackBlock, whiteBlock))
                                         .build();
      break;
    }
    case HIGHWAY: {
      int fontSize = (int) (12 * lengthFactor);

      // Compose a thick and a thin line to obtain a halo effect.
      ALspComplexStroke thickLine = parallelLine().lineWidth(aSizeFactor * 4).lineColor(aColor).build();
      ALspComplexStroke thinLine = parallelLine().lineWidth(aSizeFactor * 2).lineColor(new Color(1.0f, 0.9f, 0.3f, 1.0f)).build();
      ALspComplexStroke baseLine = compose(thickLine, thinLine);

      // Create a decoration that composes an ellipse and some text
      ALspComplexStroke ellipse = arc().length(20 * lengthFactor).minorRadius(8 * lengthFactor).lineWidth(aSizeFactor).fillColor(new Color(1.0f, 0.9f, 0.3f, 1.0f)).build();
      TLspTextStyle textStyle = TLspTextStyle.newBuilder().font(new Font("SansSerif", Font.BOLD, fontSize)).haloThickness(0).build();
      ALspComplexStroke text = text("42").textStyle(textStyle).build();

      // Because the decoration is added as a regular line, it is repeated along the line. The fallbackStroking call
      // is used to make sure that:
      // 1) The decorations have a spacing of 150 pixels.
      // 2) The fallback stroke can be seen through the space between the decorations. I.e. if only gap(75) was used
      //    iso fallbackStroking(75), the base line would not be visible.
      ALspComplexStroke decoration = append(fallbackStroking(75),
                                            compose(ellipse, text),
                                            fallbackStroking(75));

      style = TLspComplexStrokedLineStyle.newBuilder()
                                         .regular(decoration)
                                         .fallback(baseLine)
                                         .build();
      break;
    }
    case POWER_LINE: {
      int offset = aSizeFactor * 2;
      ALspComplexStroke upperLine = parallelLine().lineColor(aColor).lineWidth(aSizeFactor).offset(offset).build();
      ALspComplexStroke lowerLine = parallelLine().lineColor(aColor).lineWidth(aSizeFactor).offset(-offset).build();
      ALspComplexStroke verticalLine = line().length(0).offset0(offset + 1).offset1(-offset - 1).lineColor(aColor).lineWidth(aSizeFactor).build();

      // Compose an upper and lower line, and use it as fallback stroke to make sure it is never dropped.
      // Use a vertical line as decoration. The fallbackStroking call is used to make sure that:
      // 1) The decorations have a spacing of 20 pixels.
      // 2) The fallback stroke can be seen through the space between the decorations. I.e. if only gap(10) was used
      //    iso fallbackStroking(10), the base line would not be visible.
      style = TLspComplexStrokedLineStyle.newBuilder()
                                         .regular(append(fallbackStroking(10 * lengthFactor),
                                                         verticalLine,
                                                         fallbackStroking(10 * lengthFactor)))
                                         .fallback(compose(upperLine, lowerLine))
                                         .build();
      break;
    }
    case PIPELINE: {
      double circleRadius = 2.5 * lengthFactor;
      ALspComplexStroke line = parallelLine().lineColor(aColor).lineWidth(aSizeFactor).build();
      ALspComplexStroke circle = arc().lineWidth(aSizeFactor).lineColor(aColor).length(2 * circleRadius).minorRadius(circleRadius).build();

      // Use a thin line, and use it as fallback stroke to make sure it is never dropped.
      // Use a circle as decoration. The fallbackStroking call is used to make sure that:
      // 1) The decorations have a spacing of 20 pixels.
      // 2) The fallback stroke can be seen through the space between the decorations. I.e. if only gap(10) was used
      //    iso fallbackStroking(10), the base line would not be visible.
      style = TLspComplexStrokedLineStyle.newBuilder()
                                         .regular(append(fallbackStroking(10 * lengthFactor),
                                                         circle,
                                                         fallbackStroking(10 * lengthFactor)))
                                         .fallback(line)
                                         .build();
      break;
    }
    case FENCE: {
      double length = 10 * lengthFactor;
      ALspComplexStroke line = parallelLine().lineColor(aColor).lineWidth(aSizeFactor).build();
      ALspComplexStroke upperLine = line().length(length / 2).offset0(0).offset1(length / 2).lineColor(aColor).lineWidth(aSizeFactor).build();
      ALspComplexStroke lowerLine = line().length(length / 2).offset0(0).offset1(-length / 2).lineColor(aColor).lineWidth(aSizeFactor).build();

      // Use a thin line, and use it as fallback stroke to make sure it is never dropped.
      // Use a repeating pattern of upper and lower lines as decoration. The fallbackStroking call is used to make sure that:
      // 1) The decorations have the desired spacing.
      // 2) The fallback stroke can be seen through the space between and below the decorations.
      style = TLspComplexStrokedLineStyle.newBuilder()
                                         .regular(append(fallbackStroking(length / 2),
                                                         combineWithFallbackStroking(upperLine),
                                                         fallbackStroking(length),
                                                         combineWithFallbackStroking(lowerLine),
                                                         fallbackStroking(length / 2)))
                                         .fallback(line)
                                         .build();
      break;
    }
    case WARM_FRONT: {
      int lineWidth = aSizeFactor * 2;
      // Use a repeating pattern of half circles as decoration. The fallbackStroking call is used to make sure that:
      // 1) The decorations have the desired spacing.
      // 2) The fallback stroke can be seen through the space between and below the decorations.
      ALspComplexStroke hemiCircle = createFilledHemiCircle(aColor, aSizeFactor * 2, 14, false);
      ALspComplexStroke decoration = append(fallbackStroking(4.5), hemiCircle, fallbackStroking(4.5));

      // Use the base line as fallback to make sure it is never dropped.
      ALspComplexStroke baseLine = parallelLine().lineWidth(lineWidth).lineColor(aColor).build();
      style = TLspComplexStrokedLineStyle.newBuilder()
                                         .regular(decoration)
                                         .fallback(baseLine)
                                         .build();
      break;
    }
    case COLD_FRONT: {
      int lineWidth = aSizeFactor * 2;
      // Use a repeating pattern of triangles as decoration. The fallbackStroking call is used to make sure that:
      // 1) The decorations have the desired spacing.
      // 2) The fallback stroke can be seen through the space between and below the decorations.
      ALspComplexStroke triangle = createFilledTriangle(aColor, aSizeFactor * 2, 14);
      ALspComplexStroke decoration = append(fallbackStroking(4.5), triangle, fallbackStroking(4.5));

      // Use the base line as fallback to make sure it is never dropped.
      ALspComplexStroke baseLine = parallelLine().lineWidth(lineWidth).lineColor(aColor).build();
      style = TLspComplexStrokedLineStyle.newBuilder()
                                         .regular(decoration)
                                         .fallback(baseLine)
                                         .build();
      break;
    }
    case OCCLUDED_FRONT: {
      int lineWidth = aSizeFactor * 2;
      // Use a repeating pattern of triangles and half circles as decoration. The fallbackStroking call is used to make sure that:
      // 1) The decorations have the desired spacing.
      // 2) The fallback stroke can be seen through the space between and below the decorations.
      ALspComplexStroke hemiCircle = createFilledHemiCircle(aColor, aSizeFactor * 2, 14, true);
      ALspComplexStroke triangle = createFilledTriangle(aColor, aSizeFactor * 2, 14);
      ALspComplexStroke decoration = append(fallbackStroking(4.5), triangle, fallbackStroking(4.5), hemiCircle, fallbackStroking(4.5));

      // Use the base line as fallback to make sure it is never dropped.
      ALspComplexStroke baseLine = parallelLine().lineWidth(lineWidth).lineColor(aColor).build();
      style = TLspComplexStrokedLineStyle.newBuilder()
                                         .regular(decoration)
                                         .fallback(baseLine)
                                         .build();
      break;
    }
    default: {
      // Return a simple line
      style = TLspComplexStrokedLineStyle.newBuilder()
                                         .fallback(parallelLine().lineWidth(aSizeFactor).lineColor(aColor).build())
                                         .build();
      break;
    }
    }
    return Collections.singletonList(style);
  }

  private static ALspComplexStroke createFilledTriangle(Color aColor, int aLineWidth, double aSide) {
    // Create a triangle by composing 2 filled lines. A horizontal line is added as well to make sure
    // that the triangle aligns well with the base line.
    ALspComplexStroke left = filledLine().length(aSide / 2).offset(0, aSide).fillColor(aColor).build();
    ALspComplexStroke right = filledLine().length(aSide / 2).offset(aSide, 0).fillColor(aColor).build();
    ALspComplexStroke horizontal = parallelLine().length(aSide).lineWidth(aLineWidth).lineColor(aColor).build();
    return atomic(compose(
        append(left, right),
        horizontal
    ));
  }

  private static ALspComplexStroke createFilledHemiCircle(Color aColor, int aLineWidth, double aSide, boolean aFlipped) {
    return atomic(compose(
        filledArc().startAngle(aFlipped ? 180 : 0).angle(180).length(aSide).minorRadius(aSide / 2).fillColor(aColor).build(),
        parallelLine().length(aSide + 2).lineWidth(aLineWidth).lineColor(aColor).build()
    ));
  }

  static List<ALspStyle> createRouteStyle(ILcdPoint aP1, ILcdPoint aP2, Random aRandom, int aStartEndGap) {
    TLcdEllipsoid ellipsoid = new TLcdEllipsoid();

    TLspComplexStrokedLineStyle.Builder builder = TLspComplexStrokedLineStyle.newBuilder();

    // Add some space for icons
    if (aStartEndGap > 0) {
      builder.decoration(0, gap(aStartEndGap));
    }

    // Add a start azimuth decoration
    Font azimuthFont = new Font("SansSerif", Font.BOLD, 10);
    TLspTextStyle azimuthTextStyle = TLspTextStyle.newBuilder().font(azimuthFont).haloThickness(0).build();
    int startToEnd = (int) Math.toDegrees(ellipsoid.forwardAzimuth2D(aP1, aP2));
    ALspComplexStroke startToEndStroke = text(Integer.toString(startToEnd))
        .textStyle(azimuthTextStyle)
        .build();
    builder.decoration(0, append(gap(2), startToEndStroke, gap(2)));

    // Add an end azimuth decoration
    int endToStart = (int) Math.toDegrees(ellipsoid.forwardAzimuth2D(aP2, aP1));
    ALspComplexStroke endToStartStroke = text(Integer.toString(endToStart))
        .textStyle(azimuthTextStyle)
        .build();
    builder.decoration(1, append(gap(2), endToStartStroke, gap(2)));

    // Add some space for icons
    if (aStartEndGap > 0) {
      builder.decoration(1, gap(aStartEndGap));
    }

    // Add a (randomly) generated call sign
    String callSign = Integer.toString(aRandom.nextInt(1000));
    for (int i = 0; i < 3; i++) {
      callSign = (char) (65 + aRandom.nextInt(26)) + callSign;
    }
    Font callSignFont = new Font("SansSerif", Font.BOLD, 12);
    TLspTextStyle callSignTextStyle = TLspTextStyle.newBuilder()
                                                   .font(callSignFont)
                                                   .textColor(Color.white)
                                                   .haloThickness(0)
                                                   .build();
    ALspComplexStroke callSignStroke = text(callSign)
        .textStyle(callSignTextStyle)
        .build();
    ALspComplexStroke arrowStroke = append(
        filledRect().length(50).height(-8, 8).fillColor(Color.black).build(),
        filledTriangle(0, 12, 0, -12, 8, 0).fillColor(Color.black).build()
    );
    builder.decoration(0.5, atomic(compose(arrowStroke, callSignStroke)));

    builder.fallback(parallelLine().lineWidth(2).build());

    // Make sure the procedure is world sized
    builder.scale(1000.0);
    builder.scalingMode(TLspComplexStrokedLineStyle.ScalingMode.WORLD_SCALING);

    return Collections.<ALspStyle>singletonList(builder.build());
  }

  static List<ALspStyle> createProcedureStyle(String aDirection, String aReverseDirection, String aTextAbove, String aTextBelow, boolean aArrow, Color aColor, int aGapAtStart, int aGapAtEnd) {
    TLspComplexStrokedLineStyle.Builder builder = TLspComplexStrokedLineStyle.newBuilder();

    // Possibly leave a gap for icons
    if (aGapAtStart > 0) {
      builder.decoration(0, gap(aGapAtStart));
    }

    // Create the text style to use for the textual parts of the complex stroke
    TLspTextStyle textStyle = TLspTextStyle.newBuilder()
                                           .font(new Font("SansSerif", Font.BOLD, 10))
                                           .textColor(aColor)
                                           .haloThickness(0)
                                           .build();

    // Direction text decoration (which contains an arrow as well)
    if (aDirection != null) {
      ALspComplexStroke directionTextStroke = text(aDirection).textStyle(textStyle).offset(-8).build();
      ALspComplexStroke directionArrowStroke = compose(
          append(parallelLine().length(6).lineWidth(1).lineColor(aColor).offset(-8).build(), gap(2)),
          append(gap(2), arrow().type(ArrowBuilder.ArrowType.REGULAR_FILLED).fillColor(aColor).size(6).height(6).offset(-8).build())
      );
      ALspComplexStroke directionStroke = atomic(append(directionTextStroke, gap(2), directionArrowStroke));
      builder.decoration(0, combineWithFallbackStroking(directionStroke));
    }

    // Direction decoration
    if (aArrow) {
      ALspComplexStroke arrowStroke = arrow().type(ArrowBuilder.ArrowType.REGULAR_FILLED).fillColor(aColor).size(10).height(8).build();
      // Make sure we only combine half of the arrow stroke with the fallback stroke
      ALspComplexStroke arrowStrokeWithFallback = compose(arrowStroke, append(fallbackStroking(5), gap(5)));
      builder.decoration(1, arrowStrokeWithFallback);
    }

    // Reverse direction text decoration
    if (aReverseDirection != null) {
      ALspComplexStroke reverseDirectionStroke = text(aReverseDirection).textStyle(textStyle).build();
      builder.decoration(1, append(gap(4), reverseDirectionStroke, gap(4)));
    }

    // Text decoration above/below the line
    if (aTextAbove != null || aTextBelow != null) {
      ALspComplexStroke textAboveStroke = null;
      if (aTextAbove != null) {
        textAboveStroke = text(aTextAbove).textStyle(textStyle).offset(8).build();
      }
      ALspComplexStroke textBelowStroke = null;
      if (aTextBelow != null) {
        textBelowStroke = text(aTextBelow).textStyle(textStyle).offset(-8).build();
      }

      ALspComplexStroke textStroke;
      if (textAboveStroke != null && textBelowStroke != null) {
        textStroke = atomic(compose(textAboveStroke, textBelowStroke));
      } else if (textAboveStroke != null) {
        textStroke = textAboveStroke;
      } else {
        textStroke = textBelowStroke;
      }
      builder.decoration(0.5, combineWithFallbackStroking(textStroke));
    }

    // Possibly leave a gap for icons
    if (aGapAtEnd > 0) {
      builder.decoration(1, gap(aGapAtEnd));
    }

    builder.fallback(parallelLine().lineWidth(2).lineColor(aColor).build());

    // Make sure the procedure is world sized
    builder.scale(10.0);
    builder.scalingMode(TLspComplexStrokedLineStyle.ScalingMode.WORLD_SCALING);

    return Collections.<ALspStyle>singletonList(builder.build());
  }

  /**
   * Creates an airspace style that is either restricted or not. Non-restricted (regular) airspaces are painted using
   * a small dark line. They are filled in a region of a few pixels wide along the border. Restricted airspaces are
   * similar to regular airspaces, but they have a hatched fill instead of a plain fill.
   *
   * @param aRestricted if an airspace is restricted or not.
   * @param aColor      the base color of the air space. This is typically red or blue.
   *
   * @return a list of styles that can be used to paint the airspace.
   */
  public static List<ALspStyle> createAirspaceStyle(boolean aRestricted, Color aColor) {
    TLspComplexStrokedLineStyle.Builder builder = TLspComplexStrokedLineStyle.newBuilder();

    Color lineColor = new Color(aColor.getRed(), aColor.getGreen(), aColor.getBlue(), 128);
    Color brighterColor = aColor.brighter();
    Color fillColor = new Color(brighterColor.getRed(), brighterColor.getGreen(), brighterColor.getBlue(), 64);

    int basicLineWidth = 3;
    int fillLineWidth = 12;
    ALspComplexStroke basicLine = ALspComplexStroke.filledRect()
                                                   .lengthRelative(1) // Make sure that the line is painted over the whole length
                                                   .minHeight(-basicLineWidth) // Make sure that the line is painted at the inside
                                                   .maxHeight(0.25) // Make sure that bordering airspaces don't produce artifacts
                                                   .fillColor(lineColor)
                                                   .build();

    RectangleBuilder fillStrokeBuilder = ALspComplexStroke.filledRect()
                                                          .lengthRelative(1) // Make sure that the fill is painted over the whole length
                                                          .minHeight(-fillLineWidth)
                                                          .maxHeight(-basicLineWidth) // Make sure that the basicLine and the fill don't overlap to avoid artifacts
                                                          .fillColor(fillColor); // The fillTexture only uses a white color. This call makes sure that the fill texture gets the right color.
    if (aRestricted) {
      fillStrokeBuilder.fillTexture(RESTRICTED_ICON);
    }
    ALspComplexStroke fillStroke = fillStrokeBuilder.build();

    builder.fallback(ALspComplexStroke.compose(fillStroke, basicLine));
    // Make sure that there are no gaps between line segments and that line segments don't overlap.
    builder.sharpAngleThreshold(0);
    return Collections.<ALspStyle>singletonList(builder.build());
  }
}

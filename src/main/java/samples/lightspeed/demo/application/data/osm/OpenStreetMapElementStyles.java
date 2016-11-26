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
package samples.lightspeed.demo.application.data.osm;

import java.awt.Color;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.ext.DefaultHandler2;
import org.xml.sax.helpers.DefaultHandler;

public class OpenStreetMapElementStyles {

  private final List<Icon> fIconRules;
  private final List<Line> fLineRules;
  private final List<Area> fAreaRules;

  public OpenStreetMapElementStyles(List<Icon> aIconRules, List<Line> aLineRules, List<Area> aAreaRules) {
    fIconRules = aIconRules;
    fLineRules = aLineRules;
    fAreaRules = aAreaRules;
  }

  public Icon getIcon(String aKey, String aValue) {
    return findRule(fIconRules, aKey, aValue);
  }

  public Line getLine(String aKey, String aValue) {
    return findRule(fLineRules, aKey, aValue);
  }

  public Area getArea(String aKey, String aValue) {
    return findRule(fAreaRules, aKey, aValue);
  }

  public Set<String> getAllValues(String aKey) {
    Set<String> values = new HashSet<String>();
    getValues(fIconRules, aKey, values);
    getValues(fLineRules, aKey, values);
    getValues(fAreaRules, aKey, values);
    return values;
  }

  private void getValues(List<? extends PrioritizedRule> aRules, String aKey, Set<String> aValues) {
    for (PrioritizedRule rule : aRules) {
      if (rule.fKey.equals(aKey)) {
        aValues.add(rule.fValue);
      }
    }
  }

  private <T extends PrioritizedRule> T findRule(List<T> aRules, String aKey, String aValue) {
    T result = null;
    for (T rule : aRules) {
      if (matches(rule, aKey, aValue)) {
        if (result == null || rule.fPriority > result.fPriority) {
          result = rule;
        }
      }
    }

    return result;
  }

  private boolean matches(PrioritizedRule aRule, String aKey, String aValue) {
    return (aRule.fKey.equals("") || aRule.fKey.equals(aKey)) &&
           (aRule.fValue.equals("") || aRule.fValue.equals(aValue));
  }

  private static OpenStreetMapElementStyles instance;

  public static OpenStreetMapElementStyles load() {
    if (instance == null) {
      instance = load(OpenStreetMapElementStyles.class.getClassLoader().getResourceAsStream("samples/lightspeed/demo/osm/elemstyles.xml"));
    }
    return instance;
  }

  private static OpenStreetMapElementStyles load(InputStream aXmlStream) {
    try {
      SAXParser parser = SAXParserFactory.newInstance().newSAXParser();

      final List<Icon> iconRules = new ArrayList<Icon>();
      final List<Line> lineRules = new ArrayList<Line>();
      final List<Area> areaRules = new ArrayList<Area>();

      DefaultHandler handler = new DefaultHandler2() {

        private String fKey;
        private String fValue;

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
          if ("condition".equals(qName)) {
            fKey = getString(attributes, "k", "");
            fValue = getString(attributes, "v", "");
          }

          if ("icon".equals(qName)) {
            Icon icon = new Icon();
            icon.fKey = fKey;
            icon.fValue = fValue;
            icon.fPriority = getInt(attributes, "priority", 0);
            icon.fAnnotate = "true".equals(getString(attributes, "annotate", "true"));
            icon.fSrc = getString(attributes, "src", "");

            iconRules.add(icon);
          }

          if ("line".equals(qName)) {
            Line line = new Line();
            line.fKey = fKey;
            line.fValue = fValue;
            line.fPriority = getInt(attributes, "priority", 0);
            line.fWidth = getInt(attributes, "width", 1);
            line.fRealWidth = getInt(attributes, "realwidth", 1);
            line.fColor = getColor(getString(attributes, "colour", "#ff0000ff"));
            line.fAlpha = getAlpha(getString(attributes, "colour", "#000000ff"));
            int[] dash = getDashes(getString(attributes, "dashed", "false"));
            line.fDashed = (dash != null);
            if (dash != null) {
              line.fDashX = dash[0];
              line.fDashY = dash[1];
            }
            line.fDashColor = getColor(getString(attributes, "dashedColour", "#000000ff"));

            lineRules.add(line);
          }

          if ("area".equals(qName)) {
            Area area = new Area();
            area.fKey = fKey;
            area.fValue = fValue;
            area.fPriority = getInt(attributes, "priority", 0);
            area.fClosed = "true".equals(getString(attributes, "closed", "true"));
            area.fColor = getColor(getString(attributes, "colour", "#ff0000"));

            areaRules.add(area);
          }
        }
      };

      parser.parse(aXmlStream, handler);

      return new OpenStreetMapElementStyles(iconRules, lineRules, areaRules);
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }

  private static int getInt(Attributes aAttributes, String aName, int aDefault) {
    return Integer.parseInt(getString(aAttributes, aName, "" + aDefault));
  }

  private static String getString(Attributes aAttributes, String aName, String aDefault) {
    String result = aAttributes.getValue(aName);
    if (result == null || "".equals(result)) {
      result = aDefault;
    }
    return result;
  }

  private static int[] getDashes(String aValue) {
    if ("true".equals(aValue)) {
      return new int[]{3, 3};
    }

    if (aValue.matches("\\d+")) {
      return new int[]{Integer.parseInt(aValue), Integer.parseInt(aValue)};
    }

    if (aValue.matches("\\d+,\\d+")) {
      String[] parts = aValue.split(",");
      return new int[]{Integer.parseInt(parts[0]), Integer.parseInt(parts[1])};
    }

    return null;
  }

  private static Color getColor(String aString) {
    aString = aString.replaceAll(".*#", "");
    return Color.decode("0x" + aString);
  }

  private static double getAlpha(String aString) {
    aString = aString.replaceAll(".*#......", "");
    if ("".equals(aString)) {
      return 1;
    } else {
      return (Integer.parseInt("0x" + aString) / 100.0);
    }
  }

  private static class PrioritizedRule {
    String fKey;
    String fValue;
    Boolean fBool;
    int fPriority;
  }

  public static class Icon extends PrioritizedRule {
    public int fPriority;
    public String fSrc;
    public boolean fAnnotate;

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }

      Icon icon = (Icon) o;

      if (fAnnotate != icon.fAnnotate) {
        return false;
      }
      if (fPriority != icon.fPriority) {
        return false;
      }
      if (!fSrc.equals(icon.fSrc)) {
        return false;
      }

      return true;
    }

    @Override
    public int hashCode() {
      int result = fPriority;
      result = 31 * result + fSrc.hashCode();
      result = 31 * result + (fAnnotate ? 1 : 0);
      return result;
    }
  }

  public static class Line extends PrioritizedRule {
    public int fPriority;
    public int fWidth;
    public int fRealWidth;
    public Color fColor;
    public double fAlpha;
    public boolean fDashed = false;
    public int fDashX;
    public int fDashY;
    public Color fDashColor;

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }

      Line line = (Line) o;

      if (Double.compare(line.fAlpha, fAlpha) != 0) {
        return false;
      }
      if (fDashX != line.fDashX) {
        return false;
      }
      if (fDashY != line.fDashY) {
        return false;
      }
      if (fDashed != line.fDashed) {
        return false;
      }
      if (fPriority != line.fPriority) {
        return false;
      }
      if (fRealWidth != line.fRealWidth) {
        return false;
      }
      if (fWidth != line.fWidth) {
        return false;
      }
      if (fColor != null ? !fColor.equals(line.fColor) : line.fColor != null) {
        return false;
      }
      if (fDashColor != null ? !fDashColor.equals(line.fDashColor) : line.fDashColor != null) {
        return false;
      }

      return true;
    }

    @Override
    public int hashCode() {
      int result;
      long temp;
      result = fPriority;
      result = 31 * result + fWidth;
      result = 31 * result + fRealWidth;
      result = 31 * result + (fColor != null ? fColor.hashCode() : 0);
      temp = fAlpha != +0.0d ? Double.doubleToLongBits(fAlpha) : 0L;
      result = 31 * result + (int) (temp ^ (temp >>> 32));
      result = 31 * result + (fDashed ? 1 : 0);
      result = 31 * result + fDashX;
      result = 31 * result + fDashY;
      result = 31 * result + (fDashColor != null ? fDashColor.hashCode() : 0);
      return result;
    }
  }

  public static class Area extends PrioritizedRule {
    public int fPriority;
    public Color fColor;
    public boolean fClosed;

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }

      Area area = (Area) o;

      if (fClosed != area.fClosed) {
        return false;
      }
      if (fPriority != area.fPriority) {
        return false;
      }
      if (fColor != null ? !fColor.equals(area.fColor) : area.fColor != null) {
        return false;
      }

      return true;
    }

    @Override
    public int hashCode() {
      int result = fPriority;
      result = 31 * result + (fColor != null ? fColor.hashCode() : 0);
      result = 31 * result + (fClosed ? 1 : 0);
      return result;
    }
  }
}

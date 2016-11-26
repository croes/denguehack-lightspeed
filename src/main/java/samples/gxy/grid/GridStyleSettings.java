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
package samples.gxy.grid;

import java.awt.Color;
import java.awt.Font;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import samples.common.MapColors;
import com.luciad.util.ILcdPropertyChangeSource;

/**
 * Base class for grid layer factories.
 */
public class GridStyleSettings implements ILcdPropertyChangeSource {

  public static final int PROMINENT_LINE_WIDTH = 1;
  public static final int ALTERNATE_LINE_WIDTH = 2;
  public static final int EXCEPTIONAL_LINE_WIDTH = 3;

  public static final Font PROMINENT_LABEL_FONT = createDefaultFont(12);
  public static final Font ALTERNATE_LABEL_FONT = createDefaultFont(13);
  public static final Font EXCEPTIONAL_LABEL_FONT = createDefaultFont(14);

  public static final Color PROMINENT_HALO_LABEL_COLOR = new Color(1f, 1f, 1f, 0.5f);
  public static final Color ALTERNATE_HALO_LABEL_COLOR = new Color(1f, 1f, 1f, 0.7f);
  public static final Color EXCEPTIONAL_HALO_LABEL_COLOR = new Color(1f, 1f, 1f, 0.8f);

  public static final int LABEL_HALO_THICKNESS = 1;

  public static final Font OVERLAY_LABEL_FONT = createDefaultFont(24);
  public static final Color OVERLAY_LABEL_COLOR = new Color(1f, 1f, 1f, 0.8f);
  public static final Color OVERLAY_LABEL_HALO_COLOR = new Color(0f, 0f, 0f, 0.3f);
  public static final int OVERLAY_LABEL_HALO_THICKNESS = 1;

  // Prominent line style
  private double fPrimaryLineWidth = PROMINENT_LINE_WIDTH;
  private Color fPrimaryLineColor = MapColors.GRID_PROMINENT_COLOR;

  // Alternate line style
  private double fSecondaryLineWidth = ALTERNATE_LINE_WIDTH;
  private Color fSecondaryLineColor = MapColors.GRID_ALTERNATE_COLOR;

  // Exceptional line style
  private double fTertiaryLineWidth = EXCEPTIONAL_LINE_WIDTH;
  private Color fTertiaryLineColor = MapColors.GRID_EXCEPTIONAL_COLOR;

  // Prominent label style
  private Font fPrimaryLabelFont = PROMINENT_LABEL_FONT;
  private Color fPrimaryLabelTextColor = MapColors.GRID_PROMINENT_LABEL_COLOR;
  private Color fPrimaryLabelHaloColor = PROMINENT_HALO_LABEL_COLOR;
  private int fPrimaryLabelHaloThickness = LABEL_HALO_THICKNESS;

  // Alternate label style
  private Font fSecondaryLabelFont = ALTERNATE_LABEL_FONT;
  private Color fSecondaryLabelTextColor = MapColors.GRID_ALTERNATE_LABEL_COLOR;
  private Color fSecondaryLabelHaloColor = ALTERNATE_HALO_LABEL_COLOR;
  private int fSecondaryLabelHaloThickness = LABEL_HALO_THICKNESS;

  // Exceptional label style
  private Font fTertiaryLabelFont = EXCEPTIONAL_LABEL_FONT;
  private Color fTertiaryLabelTextColor = MapColors.GRID_EXCEPTIONAL_LABEL_COLOR;
  private Color fTertiaryLabelHaloColor = EXCEPTIONAL_HALO_LABEL_COLOR;
  private int fTertiaryLabelHaloThickness = LABEL_HALO_THICKNESS;

  // Overlay label style
  private Font fOverlayLabelFont = OVERLAY_LABEL_FONT;
  private Color fOverlayLabelTextColor = OVERLAY_LABEL_COLOR;
  private Color fOverlayLabelHaloColor = OVERLAY_LABEL_HALO_COLOR;
  private int fOverlayLabelHaloThickness = OVERLAY_LABEL_HALO_THICKNESS;

  private final PropertyChangeSupport fPropertyChangeSupport = new PropertyChangeSupport(this);

  @Override
  public void addPropertyChangeListener(PropertyChangeListener aPropertyChangeListener) {
    fPropertyChangeSupport.addPropertyChangeListener(aPropertyChangeListener);
  }

  @Override
  public void removePropertyChangeListener(PropertyChangeListener aPropertyChangeListener) {
    fPropertyChangeSupport.removePropertyChangeListener(aPropertyChangeListener);
  }

  private static Font createDefaultFont(int aFontSize) {
    return new Font("Default", Font.BOLD, aFontSize);
  }

  public double getPrimaryLineWidth() {
    return fPrimaryLineWidth;
  }

  public void setPrimaryLineWidth(double aPrimaryLineWidth) {
    double oldPrimaryLineWidth = fPrimaryLineWidth;
    if (aPrimaryLineWidth != oldPrimaryLineWidth) {
      fPrimaryLineWidth = aPrimaryLineWidth;
      fPropertyChangeSupport.firePropertyChange("primaryLineWidth", oldPrimaryLineWidth, aPrimaryLineWidth);
    }
  }

  public Color getPrimaryLineColor() {
    return fPrimaryLineColor;
  }

  public void setPrimaryLineColor(Color aPrimaryLineColor) {
    Color oldPrimaryLineColor = fPrimaryLineColor;
    if (aPrimaryLineColor != oldPrimaryLineColor) {
      fPrimaryLineColor = aPrimaryLineColor;
      fPropertyChangeSupport.firePropertyChange("primaryLineColor", oldPrimaryLineColor, aPrimaryLineColor);
    }
  }

  public double getSecondaryLineWidth() {
    return fSecondaryLineWidth;
  }

  public void setSecondaryLineWidth(double aSecondaryLineWidth) {
    double oldSecondaryLineWidth = fSecondaryLineWidth;
    if (aSecondaryLineWidth != oldSecondaryLineWidth) {
      fSecondaryLineWidth = aSecondaryLineWidth;
      fPropertyChangeSupport.firePropertyChange("secondaryLineWidth", oldSecondaryLineWidth, aSecondaryLineWidth);
    }
  }

  public Color getSecondaryLineColor() {
    return fSecondaryLineColor;
  }

  public void setSecondaryLineColor(Color aSecondaryLineColor) {
    Color oldSecondaryLineColor = fSecondaryLineColor;
    if (aSecondaryLineColor != oldSecondaryLineColor) {
      fSecondaryLineColor = aSecondaryLineColor;
      fPropertyChangeSupport.firePropertyChange("secondaryLineColor", oldSecondaryLineColor, aSecondaryLineColor);
    }
  }

  public double getTertiaryLineWidth() {
    return fTertiaryLineWidth;
  }

  public void setTertiaryLineWidth(double aTertiaryLineWidth) {
    double oldTertiaryLineWidth = fTertiaryLineWidth;
    if (aTertiaryLineWidth != oldTertiaryLineWidth) {
      fTertiaryLineWidth = aTertiaryLineWidth;
      fPropertyChangeSupport.firePropertyChange("tertiaryLineWidth", oldTertiaryLineWidth, aTertiaryLineWidth);
    }
  }

  public Color getTertiaryLineColor() {
    return fTertiaryLineColor;
  }

  public void setTertiaryLineColor(Color aTertiaryLineColor) {
    Color oldTertiaryLineColor = fTertiaryLineColor;
    if (aTertiaryLineColor != oldTertiaryLineColor) {
      fTertiaryLineColor = aTertiaryLineColor;
      fPropertyChangeSupport.firePropertyChange("tertiaryLineColor", oldTertiaryLineColor, aTertiaryLineColor);
    }
  }

  public Font getPrimaryLabelFont() {
    return fPrimaryLabelFont;
  }

  public void setPrimaryLabelFont(Font aPrimaryLabelFont) {
    Font oldPrimaryLabelFont = fPrimaryLabelFont;
    if (aPrimaryLabelFont != oldPrimaryLabelFont) {
      fPrimaryLabelFont = aPrimaryLabelFont;
      fPropertyChangeSupport.firePropertyChange("primaryLabelFont", oldPrimaryLabelFont, aPrimaryLabelFont);
    }
  }

  public Color getPrimaryLabelTextColor() {
    return fPrimaryLabelTextColor;
  }

  public void setPrimaryLabelTextColor(Color aPrimaryLabelTextColor) {
    Color oldPrimaryLabelTextColor = fPrimaryLabelTextColor;
    if (aPrimaryLabelTextColor != oldPrimaryLabelTextColor) {
      fPrimaryLabelTextColor = aPrimaryLabelTextColor;
      fPropertyChangeSupport.firePropertyChange("primaryLabelTextColor", oldPrimaryLabelTextColor, aPrimaryLabelTextColor);
    }
  }

  public Color getPrimaryLabelHaloColor() {
    return fPrimaryLabelHaloColor;
  }

  public void setPrimaryLabelHaloColor(Color aPrimaryLabelHaloColor) {
    Color oldPrimaryLabelHaloColor = fPrimaryLabelHaloColor;
    if (aPrimaryLabelHaloColor != oldPrimaryLabelHaloColor) {
      fPrimaryLabelHaloColor = aPrimaryLabelHaloColor;
      fPropertyChangeSupport.firePropertyChange("primaryLabelHaloColor", oldPrimaryLabelHaloColor, aPrimaryLabelHaloColor);
    }
  }

  public int getPrimaryLabelHaloThickness() {
    return fPrimaryLabelHaloThickness;
  }

  public void setPrimaryLabelHaloThickness(int aPrimaryLabelHaloThickness) {
    int oldPrimaryLabelHaloThickness = fPrimaryLabelHaloThickness;
    if (aPrimaryLabelHaloThickness != oldPrimaryLabelHaloThickness) {
      fPrimaryLabelHaloThickness = aPrimaryLabelHaloThickness;
      fPropertyChangeSupport.firePropertyChange("primaryLabelHaloThickness", oldPrimaryLabelHaloThickness, aPrimaryLabelHaloThickness);
    }
  }

  public Font getSecondaryLabelFont() {
    return fSecondaryLabelFont;
  }

  public void setSecondaryLabelFont(Font aSecondaryLabelFont) {
    Font oldSecondaryLabelFont = fSecondaryLabelFont;
    if (aSecondaryLabelFont != oldSecondaryLabelFont) {
      fSecondaryLabelFont = aSecondaryLabelFont;
      fPropertyChangeSupport.firePropertyChange("secondaryLabelFont", oldSecondaryLabelFont, aSecondaryLabelFont);
    }
  }

  public Color getSecondaryLabelTextColor() {
    return fSecondaryLabelTextColor;
  }

  public void setSecondaryLabelTextColor(Color aSecondaryLabelTextColor) {
    Color oldSecondaryLabelTextColor = fSecondaryLabelTextColor;
    if (aSecondaryLabelTextColor != oldSecondaryLabelTextColor) {
      fSecondaryLabelTextColor = aSecondaryLabelTextColor;
      fPropertyChangeSupport.firePropertyChange("secondaryLabelTextColor", oldSecondaryLabelTextColor, aSecondaryLabelTextColor);
    }
  }

  public Color getSecondaryLabelHaloColor() {
    return fSecondaryLabelHaloColor;
  }

  public void setSecondaryLabelHaloColor(Color aSecondaryLabelHaloColor) {
    Color oldSecondaryLabelHaloColor = fSecondaryLabelHaloColor;
    if (aSecondaryLabelHaloColor != oldSecondaryLabelHaloColor) {
      fSecondaryLabelHaloColor = aSecondaryLabelHaloColor;
      fPropertyChangeSupport.firePropertyChange("secondaryLabelHaloColor", oldSecondaryLabelHaloColor, aSecondaryLabelHaloColor);
    }
  }

  public int getSecondaryLabelHaloThickness() {
    return fSecondaryLabelHaloThickness;
  }

  public void setSecondaryLabelHaloThickness(int aSecondaryLabelHaloThickness) {
    int oldSecondaryLabelHaloThickness = fSecondaryLabelHaloThickness;
    if (aSecondaryLabelHaloThickness != oldSecondaryLabelHaloThickness) {
      fSecondaryLabelHaloThickness = aSecondaryLabelHaloThickness;
      fPropertyChangeSupport.firePropertyChange("secondaryLabelHaloThickness", oldSecondaryLabelHaloThickness, aSecondaryLabelHaloThickness);
    }
  }

  public Font getTertiaryLabelFont() {
    return fTertiaryLabelFont;
  }

  public void setTertiaryLabelFont(Font aTertiaryLabelFont) {
    Font oldTertiaryLabelFont = fTertiaryLabelFont;
    if (aTertiaryLabelFont != oldTertiaryLabelFont) {
      fTertiaryLabelFont = aTertiaryLabelFont;
      fPropertyChangeSupport.firePropertyChange("tertiaryLabelFont", oldTertiaryLabelFont, aTertiaryLabelFont);
    }
  }

  public Color getTertiaryLabelTextColor() {
    return fTertiaryLabelTextColor;
  }

  public void setTertiaryLabelTextColor(Color aTertiaryLabelTextColor) {
    Color oldTertiaryLabelTextColor = fTertiaryLabelTextColor;
    if (aTertiaryLabelTextColor != oldTertiaryLabelTextColor) {
      fTertiaryLabelTextColor = aTertiaryLabelTextColor;
      fPropertyChangeSupport.firePropertyChange("tertiaryLabelTextColor", oldTertiaryLabelTextColor, aTertiaryLabelTextColor);
    }
  }

  public Color getTertiaryLabelHaloColor() {
    return fTertiaryLabelHaloColor;
  }

  public void setTertiaryLabelHaloColor(Color aTertiaryLabelHaloColor) {
    Color oldTertiaryLabelHaloColor = fTertiaryLabelHaloColor;
    if (aTertiaryLabelHaloColor != oldTertiaryLabelHaloColor) {
      fTertiaryLabelHaloColor = aTertiaryLabelHaloColor;
      fPropertyChangeSupport.firePropertyChange("tertiaryLabelHaloColor", oldTertiaryLabelHaloColor, aTertiaryLabelHaloColor);
    }
  }

  public int getTertiaryLabelHaloThickness() {
    return fTertiaryLabelHaloThickness;
  }

  public void setTertiaryLabelHaloThickness(int aTertiaryLabelHaloThickness) {
    int oldTertiaryLabelHaloThickness = fTertiaryLabelHaloThickness;
    if (aTertiaryLabelHaloThickness != oldTertiaryLabelHaloThickness) {
      fTertiaryLabelHaloThickness = aTertiaryLabelHaloThickness;
      fPropertyChangeSupport.firePropertyChange("tertiaryLabelHaloThickness", oldTertiaryLabelHaloThickness, aTertiaryLabelHaloThickness);
    }
  }

  public Font getOverlayLabelFont() {
    return fOverlayLabelFont;
  }

  public void setOverlayLabelFont(Font aOverlayLabelFont) {
    Font oldOverlayLabelFont = fOverlayLabelFont;
    if (aOverlayLabelFont != oldOverlayLabelFont) {
      fOverlayLabelFont = aOverlayLabelFont;
      fPropertyChangeSupport.firePropertyChange("overlayLabelFont", oldOverlayLabelFont, aOverlayLabelFont);
    }
  }

  public Color getOverlayLabelTextColor() {
    return fOverlayLabelTextColor;
  }

  public void setOverlayLabelTextColor(Color aOverlayLabelTextColor) {
    Color oldOverlayLabelTextColor = fOverlayLabelTextColor;
    if (aOverlayLabelTextColor != oldOverlayLabelTextColor) {
      fOverlayLabelTextColor = aOverlayLabelTextColor;
      fPropertyChangeSupport.firePropertyChange("overlayLabelTextColor", oldOverlayLabelTextColor, aOverlayLabelTextColor);
    }
  }

  public Color getOverlayLabelHaloColor() {
    return fOverlayLabelHaloColor;
  }

  public void setOverlayLabelHaloColor(Color aOverlayLabelHaloColor) {
    Color oldOverlayLabelHaloColor = fOverlayLabelHaloColor;
    if (aOverlayLabelHaloColor != oldOverlayLabelHaloColor) {
      fOverlayLabelHaloColor = aOverlayLabelHaloColor;
      fPropertyChangeSupport.firePropertyChange("overlayLabelHaloColor", oldOverlayLabelHaloColor, aOverlayLabelHaloColor);
    }
  }

  public int getOverlayLabelHaloThickness() {
    return fOverlayLabelHaloThickness;
  }

  public void setOverlayLabelHaloThickness(int aOverlayLabelHaloThickness) {
    int oldOverlayLabelHaloThickness = fOverlayLabelHaloThickness;
    if (aOverlayLabelHaloThickness != oldOverlayLabelHaloThickness) {
      fOverlayLabelHaloThickness = aOverlayLabelHaloThickness;
      fPropertyChangeSupport.firePropertyChange("overlayLabelHaloThickness", oldOverlayLabelHaloThickness, aOverlayLabelHaloThickness);
    }
  }
}

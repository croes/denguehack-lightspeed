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
package samples.symbology.lightspeed.custom;

import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.SwingUtilities;

import com.luciad.symbology.milstd2525b.model.ILcdMS2525bCoded;
import com.luciad.symbology.milstd2525b.model.TLcdEditableMS2525bObject;
import com.luciad.symbology.milstd2525b.view.gxy.ILcdMS2525bStyle;
import com.luciad.symbology.milstd2525b.view.gxy.TLcdDefaultMS2525bStyle;
import com.luciad.symbology.milstd2525b.view.lightspeed.TLspMS2525bSymbolStyle;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.TLspContext;
import com.luciad.view.lightspeed.camera.ALspViewXYZWorldTransformation;
import com.luciad.view.lightspeed.style.ALspStyle;
import com.luciad.view.lightspeed.style.styler.ALspStyleCollector;
import com.luciad.view.lightspeed.style.styler.ALspStyler;

/**
 * Base class for the customized symbol stylers.
 * <p/>
 * This styler handles:
 * <ul>
 *   <li>firing style changes when the level-of-detail should change</li>
 *   <li>custom symbol code</li>
 * </ul>
 */
public abstract class SymbolStylerBase extends ALspStyler {

  protected static final Color SELECTION_COLOR = Color.red;
  private static final String CUSTOM_CODE_SEPARATOR = "|";
  private static final String CUSTOM_CODE_PREFIX = "C" + CUSTOM_CODE_SEPARATOR;

  private final boolean fSelected;
  private final Set<ILspView> fRegisteredViews = new HashSet<ILspView>();

  protected SymbolStylerBase(boolean aSelected) {
    fSelected = aSelected;
  }

  protected synchronized void checkView(ILspView aView) {
    if (!(fRegisteredViews.contains(aView))) {
      fRegisteredViews.add(aView);
      new StyleInvalidator(aView);
    }
  }

  protected boolean isInScaleRange(ILspView aView) {
    double scale = aView.getViewXYZWorldTransformation().getScale();
    return scale >= 5e-4;
  }

  public boolean isSelected() {
    return fSelected;
  }

  /**
   * Determines whether the incoming objects are standard MS2525b or custom
   * symbols and delegates to either
   * {@link #styleStandardSymbol(Symbol, boolean, ALspStyleCollector, TLspContext)}
   * or {@link #styleCustomSymbol(Symbol, CustomSymbolCodeDescriptor, boolean, ALspStyleCollector, TLspContext)}.
   *
   * @param aObjects        The objects to be styled
   * @param aStyleCollector A style collector to which all styling information should be passed
   * @param aContext        Provides context information that may affect styling
   */
  @Override
  public void style(
      Collection<?> aObjects,
      ALspStyleCollector aStyleCollector,
      TLspContext aContext
  ) {
    checkView(aContext.getView());

    // Check whether the view is within scale range to draw the full symbols.
    // If not, symbols will be replaced with colored dots.
    boolean isInScaleRange = isInScaleRange(aContext.getView());

    for (Object object : aObjects) {
      if (object instanceof Symbol) {
        Symbol symbol = (Symbol) object;
        if (symbol.getSymbolCode().startsWith(CUSTOM_CODE_PREFIX)) {
          CustomSymbolCodeDescriptor descriptor = parseCustomSymbol(symbol.getSymbolCode());
          styleCustomSymbol(symbol, descriptor, isInScaleRange, aStyleCollector, aContext);
        } else {
          styleStandardSymbol(symbol, isInScaleRange, aStyleCollector, aContext);
        }
      }
    }
  }

  /**
   * Styles a symbol with a standard MS2525b symbol code.
   *
   * @param aSymbol         the symbol to be styled
   * @param aInScaleRange   whether the view is within scale range
   * @param aStyleCollector the style collector
   */
  protected abstract void styleStandardSymbol(
      Symbol aSymbol,
      boolean aInScaleRange,
      ALspStyleCollector aStyleCollector,
      TLspContext aContext
  );

  /**
   * Styles a symbol with a custom (non-MS2525b) symbol code.
   *
   * @param aSymbol         the symbol to be styled
   * @param aDescriptor     the code descriptor
   * @param aInScaleRange   whether the view is within scale range
   * @param aStyleCollector the style collector
   *
   * @see CustomSymbolCodeDescriptor
   */
  protected abstract void styleCustomSymbol(
      Symbol aSymbol,
      CustomSymbolCodeDescriptor aDescriptor,
      boolean aInScaleRange,
      ALspStyleCollector aStyleCollector,
      TLspContext aContext
  );

  /**
   * Parses a custom symbol code.
   * <p/>
   * The custom codes used are of the form
   * {@code "C|(text)|(affiliation)|(buffer width)|(country)"}, where
   * <ul>
   * <li>{@code (text)} is displayed as a label on the axis line of the buffer</li>
   * <li>{@code (affiliation)} is F, H or N for friendly, hostile or neutral, respectively</li>
   * <li>{@code (buffer width)} is the width of the symbol in meters</li>
   * <li>{@code (country) is the FIPS country code of the symbol}</li>
   * </ul>
   *
   * @param aSymbolCode the code
   *
   * @return the descriptor
   */
  protected static CustomSymbolCodeDescriptor parseCustomSymbol(String aSymbolCode) {
    // Split the symbol into its parts. The pipe character is used as a separator.
    final String[] parts = aSymbolCode.split("\\" + CUSTOM_CODE_SEPARATOR);
    // Determine the affiliation
    int affiliation = getAffiliation(parts[2]);
    // Determine width
    double width = Double.parseDouble(parts[3]);
    // Create descriptor
    return new CustomSymbolCodeDescriptor(parts[1], affiliation, width, parts[4]);
  }

  private static int getAffiliation(String aCode) {
    if ("F".equals(aCode)) {
      return ILcdMS2525bStyle.AFFILIATION_FRIEND;
    } else if ("H".equals(aCode)) {
      return ILcdMS2525bStyle.AFFILIATION_HOSTILE;
    } else if ("N".equals(aCode)) {
      return ILcdMS2525bStyle.AFFILIATION_NEUTRAL;
    }
    return ILcdMS2525bStyle.AFFILIATION_UNKNOWN;
  }

  protected ALspStyle createSymbologyStyle(Symbol aSymbol) {
    // Create a coded
    TLcdEditableMS2525bObject coded = new TLcdEditableMS2525bObject(aSymbol.getSymbolCode());
    for (Map.Entry<String, String> modifier : aSymbol.getModifiers().entrySet()) {
      coded.putTextModifier(modifier.getKey(), modifier.getValue());
    }
    // Get the style
    TLcdDefaultMS2525bStyle style = getStyle();

    return TLspMS2525bSymbolStyle.newBuilder()
                                 .ms2525bCoded(coded)
                                 .ms2525bStyle(style)
                                 .build();
  }

  protected TLcdDefaultMS2525bStyle getStyle() {
    // Configure the styling
    TLcdDefaultMS2525bStyle style = TLcdDefaultMS2525bStyle.getNewInstance();
    // We don't want the position label to be displayed.
    style.setLabelEnabled(ILcdMS2525bCoded.sLocationLabel, false);
    // Adjust the default color
    style.setColor(Color.BLACK);
    // Adjust the label color
    style.setLabelColor(Color.WHITE);
    // We don't want the label font to scale with he symbol size.
    style.setLabelFontScalingEnabled(false);
    // Other rendering properties.
    style.setLineWidth(2);
    style.setAffiliationColorEnabled(true);
    style.setAffiliationColor(ILcdMS2525bStyle.AFFILIATION_UNKNOWN, Color.BLACK);
    style.setCornerSmoothness(0.5);
    style.setArrowCurvedness(0.5);
    // Add a halo when selected
    if (isSelected()) {
      style.setHaloEnabled(true);
      style.setHaloThickness(2);
      style.setHaloColor(SELECTION_COLOR);
      style.setSelectionColor(null);
    }
    return style;
  }

  /**
   * A descriptor of a custom symbol code.
   */
  protected static class CustomSymbolCodeDescriptor {

    private final String fText;
    private final int fAffiliation;
    private final double fWidth;
    private final String fCountry;

    public CustomSymbolCodeDescriptor(String aText, int aAffiliation, double aWidth, String aCountry) {
      fText = aText;
      fAffiliation = aAffiliation;
      fWidth = aWidth;
      fCountry = aCountry;
    }

    /**
     * @return the text
     */
    public String getText() {
      return fText;
    }

    /**
     * @return the affiliation
     *
     * @see ILcdMS2525bStyle#getAffiliationColor(int)
     */
    public int getAffiliation() {
      return fAffiliation;
    }

    /**
     * @return the buffer width
     */
    public double getWidth() {
      return fWidth;
    }

    /**
     * @return the country code
     */
    public String getCountry() {
      return fCountry;
    }
  }

  /**
   * Class that listens for changes in the level of detail of a given view and triggers a style
   * change event when such a change occurs.
   */
  private class StyleInvalidator implements PropertyChangeListener {
    private ILspView fView;
    private ALspViewXYZWorldTransformation fTransformation;
    private boolean fInScaleRange;

    /**
     * Creates a new style invalidator for the given view.
     *
     * @param aView the view for which to create a style invalidator.
     */
    public StyleInvalidator(ILspView aView) {
      fView = aView;
      fTransformation = fView.getViewXYZWorldTransformation();
      fInScaleRange = isInScaleRange(aView);
      fView.removePropertyChangeListener(this);
      fView.addPropertyChangeListener(this);
      fTransformation.addPropertyChangeListener(this);
    }

    /**
     * Checks whether a style change event should be fired.
     *
     * @return true if a style change event should be fired, false otherwise.
     */
    private boolean shouldFireStyleChangeEvent() {
      boolean isInScaleRange = isInScaleRange(fView);
      boolean result = (fInScaleRange != isInScaleRange);
      fInScaleRange = isInScaleRange;
      return result;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
      if (evt.getSource() == fView && fView.getViewXYZWorldTransformation() != fTransformation) {
        fTransformation = fView.getViewXYZWorldTransformation();
        fInScaleRange = false;
        SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            fTransformation.removePropertyChangeListener(StyleInvalidator.this);
            fTransformation.addPropertyChangeListener(StyleInvalidator.this);
          }
        });
      }
      if (shouldFireStyleChangeEvent()) {
        fireStyleChangeEvent();
      }
    }
  }
}

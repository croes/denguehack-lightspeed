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
package samples.symbology.common.app6;

import java.awt.Color;
import java.awt.Font;
import java.util.Map;

import samples.common.undo.StateAware;
import samples.common.undo.StateException;
import samples.common.undo.StateUtil;
import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelReference;
import com.luciad.shape.ILcdPoint;
import com.luciad.symbology.app6a.model.ELcdAPP6Standard;
import com.luciad.symbology.app6a.model.ILcdAPP6ACoded;
import com.luciad.symbology.app6a.model.ILcdEditableAPP6ACoded;
import com.luciad.symbology.app6a.model.TLcdEditableAPP6AObject;
import com.luciad.symbology.app6a.view.gxy.ILcdAPP6AStyle;
import com.luciad.symbology.app6a.view.gxy.ILcdAPP6AStyled;
import com.luciad.symbology.app6a.view.gxy.TLcdDefaultAPP6AStyle;
import com.luciad.util.TLcdOutOfBoundsException;

/**
 * An APP-6 object that has an associated style. Using <code>ILcdAPP6AStyled</code> objects enables
 * extra functionality in the APP-6 painters and editors, e.g. painting icons with a certain offset
 * with respect to their actual position, different display options of the icons, etc. (Note: Use
 * CTRL key to drag the icon to a different offset.)
 */
public class StyledEditableAPP6Object extends TLcdEditableAPP6AObject implements ILcdAPP6AStyled, StateAware {

  private static final String APP6A_STATE_PREFIX = "APP6Object.";

  private static final String SOURCE_MODEL_REFERENCE_KEY = "sourceModelReference";
  private static final String SYMBOL_CODE_REFERENCE_KEY = "symbolCode";
  private static final String GEOMETRY_REFERENCE_KEY = "geometry";
  private static final String SHAPE_WIDTH_REFERENCE_KEY = "shapeWidth";
  private static final String STYLE_PROPERTIES_REFERENCE_KEY = "styleProperties";
  private static final String TEXT_MODIFIERS_REFERENCE_KEY = "textModifiers";

  private TLcdDefaultAPP6AStyle fStyle;

  public StyledEditableAPP6Object(ELcdAPP6Standard aStandard) {
    super(aStandard);
    fStyle = getDefaultStyle();
  }

  public StyledEditableAPP6Object(ILcdAPP6ACoded aAPP6ACoded) {
    super(aAPP6ACoded);
    fStyle = getDefaultStyle();
    // Copy the customizable style properties.
    if (aAPP6ACoded instanceof ILcdAPP6AStyled) {
      ILcdAPP6AStyle style = ((ILcdAPP6AStyled) aAPP6ACoded).getAPP6AStyle();
      fStyle.setSymbolFrameEnabled(style.isSymbolFrameEnabled());
      fStyle.setSymbolFillEnabled(style.isSymbolFillEnabled());
      fStyle.setSymbolIconEnabled(style.isSymbolIconEnabled());
    }
  }

  public StyledEditableAPP6Object(String aAPP6ACode, ELcdAPP6Standard aStandard) {
    super(aAPP6ACode, aStandard);
    fStyle = getDefaultStyle();
  }

  public StyledEditableAPP6Object(String aAPP6ACode, ELcdAPP6Standard aStandard, TLcdDefaultAPP6AStyle aStyle) {
    super(aAPP6ACode, aStandard);
    fStyle = aStyle;
  }

  public ILcdAPP6AStyle getAPP6AStyle() {
    return fStyle;
  }

  /**
   * Returns a customized TLcdDefaultAPP6AStyle with some nice defaults: a label halo,
   * orange selection color, ...
   *
   * @return a new instance of TLcdDefaultAPP6AStyle
   */
  public static TLcdDefaultAPP6AStyle getDefaultStyle() {
    TLcdDefaultAPP6AStyle style = TLcdDefaultAPP6AStyle.getNewInstance();
    // We don't want the position label to be displayed.
    style.setLabelEnabled(ILcdAPP6ACoded.sLocationLabel, false);

    // Adjust the default color
    style.setColor(Color.black);

    // Adjust the label style
    style.setLabelColor(Color.white);
    style.setLabelHaloEnabled(true);
    style.setLabelHaloThickness(1);
    style.setLabelHaloColor(new Color(60, 60, 60));

    // Adjust the selection color, which is used to emphasize selected symbols.
    style.setSelectionColor(new Color(255, 140, 0, 250));

    // Adjust the default font
    style.setLabelFont(new Font(Font.DIALOG, Font.PLAIN, 10));

    // We don't want the label font to scale with he symbol size.
    style.setLabelFontScalingEnabled(false);

    // Other rendering properties.
    style.setLineWidth(2);
    style.setAffiliationColorEnabled(true);
    style.setArrowCurvedness(0.5);
    return style;
  }

  public Object clone() {
    StyledEditableAPP6Object clone = (StyledEditableAPP6Object) super.clone();
    clone.fStyle = (TLcdDefaultAPP6AStyle) fStyle.clone();
    return clone;
  }

  public void storeState(Map aMap, ILcdModel aSourceModel) throws StateException {
    aMap.put(APP6A_STATE_PREFIX + SYMBOL_CODE_REFERENCE_KEY, this.getAPP6ACode());
    aMap.put(APP6A_STATE_PREFIX + SOURCE_MODEL_REFERENCE_KEY, aSourceModel.getModelReference());
    aMap.put(APP6A_STATE_PREFIX + SHAPE_WIDTH_REFERENCE_KEY, getWidth());
    storeStyleProperties(this, aMap, APP6A_STATE_PREFIX);
    storeTextModifiers(this, aMap, APP6A_STATE_PREFIX);
    StateUtil.storePointLocations(this, aSourceModel.getModelReference(), aMap, APP6A_STATE_PREFIX + GEOMETRY_REFERENCE_KEY + ".");
  }

  public void restoreState(Map aMap, ILcdModel aTargetModel) throws StateException {
    String code = (String) aMap.get(APP6A_STATE_PREFIX + SYMBOL_CODE_REFERENCE_KEY);
    if (code != null) {
      this.setAPP6ACode(code);
    }
    restoreStyleProperties(this, aMap, APP6A_STATE_PREFIX);
    restoreTextModifiers(this, aMap, APP6A_STATE_PREFIX);
    try {
      ILcdModelReference source_ref = (ILcdModelReference) aMap.get(APP6A_STATE_PREFIX + SOURCE_MODEL_REFERENCE_KEY);
      ILcdPoint[] target_points = StateUtil.calculateTargetPoints(source_ref, aTargetModel.getModelReference(), aMap, APP6A_STATE_PREFIX + "geometry.");
      if (target_points != null) {
        StateUtil.adjustNumberOf2DPoints(this.get2DEditablePointList(), target_points);
        for (int i = 0; i < target_points.length; i++) {
          ILcdPoint target_point = target_points[i];
          move2DPoint(i, target_point.getX(), target_point.getY());
        }
      }

    } catch (TLcdOutOfBoundsException e) {
      throw new StateException(e);
    }

    Double width = (Double) aMap.get(APP6A_STATE_PREFIX + SHAPE_WIDTH_REFERENCE_KEY);
    if (width != null) {
      setWidth(width.doubleValue());
    }
  }

  private void storeTextModifiers(ILcdEditableAPP6ACoded aCodedObject, Map aMap, String aPrefix) {
    String[] text_modifiers = new String[aCodedObject.getTextModifierCount()];
    for (int i = 0; i < text_modifiers.length; i++) {
      text_modifiers[i] = aCodedObject.getTextModifierValue(aCodedObject.getTextModifierKey(i));
    }
    aMap.put(aPrefix + TEXT_MODIFIERS_REFERENCE_KEY, text_modifiers);
  }

  private void storeStyleProperties(ILcdAPP6AStyled aStyledObject, Map aMap, String aPrefix) {
    ILcdAPP6AStyle style = aStyledObject.getAPP6AStyle();

    Object[] symbolStyleProperties = new Object[5];
    symbolStyleProperties[0] = style.isSymbolFillEnabled();
    symbolStyleProperties[1] = style.isSymbolFrameEnabled();
    symbolStyleProperties[2] = style.isSymbolIconEnabled();
    symbolStyleProperties[3] = style.getOffsetX();
    symbolStyleProperties[4] = style.getOffsetY();
    aMap.put(aPrefix + STYLE_PROPERTIES_REFERENCE_KEY, symbolStyleProperties);
  }

  private void restoreTextModifiers(ILcdEditableAPP6ACoded aCodedObject, Map aMap, String aPrefix) {
    String[] textModifiers = (String[]) aMap.get(aPrefix + TEXT_MODIFIERS_REFERENCE_KEY);
    if (textModifiers != null) {
      for (int i = 0; i < textModifiers.length; i++) {
        aCodedObject.putTextModifier(aCodedObject.getTextModifierKey(i), textModifiers[i]);
      }
    }
  }

  private void restoreStyleProperties(ILcdAPP6AStyled aStyledObject, Map aMap, String aPrefix) {
    ILcdAPP6AStyle style = aStyledObject.getAPP6AStyle();

    Object[] symbolStyleProperties = (Object[]) aMap.get(aPrefix + STYLE_PROPERTIES_REFERENCE_KEY);
    if (symbolStyleProperties != null) {
      style.setSymbolFillEnabled(((Boolean) symbolStyleProperties[0]).booleanValue());
      style.setSymbolFrameEnabled(((Boolean) symbolStyleProperties[1]).booleanValue());
      style.setSymbolIconEnabled(((Boolean) symbolStyleProperties[2]).booleanValue());
      style.setOffset(((Integer) symbolStyleProperties[3]).intValue(),
                      ((Integer) symbolStyleProperties[4]).intValue());
    }
  }
}

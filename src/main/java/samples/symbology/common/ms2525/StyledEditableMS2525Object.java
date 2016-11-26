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
package samples.symbology.common.ms2525;

import java.awt.Color;
import java.awt.Font;
import java.util.Map;

import samples.common.undo.StateAware;
import samples.common.undo.StateException;
import samples.common.undo.StateUtil;
import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelReference;
import com.luciad.shape.ILcdPoint;
import com.luciad.symbology.milstd2525b.model.ELcdMS2525Standard;
import com.luciad.symbology.milstd2525b.model.ILcdEditableMS2525bCoded;
import com.luciad.symbology.milstd2525b.model.ILcdMS2525bCoded;
import com.luciad.symbology.milstd2525b.model.TLcdEditableMS2525bObject;
import com.luciad.symbology.milstd2525b.view.gxy.ILcdMS2525bStyle;
import com.luciad.symbology.milstd2525b.view.gxy.ILcdMS2525bStyled;
import com.luciad.symbology.milstd2525b.view.gxy.TLcdDefaultMS2525bStyle;
import com.luciad.util.TLcdOutOfBoundsException;

/**
 * An MS2525 object that has an associated style. Using <code>ILcdMS2525bStyled</code> objects
 * enables extra functionality in the MIL-STD 2525b painters and editors, e.g. painting icons with a
 * certain offset with respect to their actual position, different display options of the icons,
 * etc. (Note: Use CTRL key to drag the icon to a different offset.)
 */
public class StyledEditableMS2525Object extends TLcdEditableMS2525bObject implements ILcdMS2525bStyled, StateAware {

  private static final String MS2525B_STATE_PREFIX = "MS2525Object.";

  private static final String SOURCE_MODEL_REFERENCE_KEY = "sourceModelReference";
  private static final String SYMBOL_CODE_REFERENCE_KEY = "symbolCode";
  private static final String GEOMETRY_REFERENCE_KEY = "geometry";
  private static final String SHAPE_WIDTH_REFERENCE_KEY = "shapeWidth";
  private static final String STYLE_PROPERTIES_REFERENCE_KEY = "styleProperties";
  private static final String TEXT_MODIFIERS_REFERENCE_KEY = "textModifiers";

  private TLcdDefaultMS2525bStyle fStyle = getDefaultStyle();

  public StyledEditableMS2525Object() {
    super();
  }

  public StyledEditableMS2525Object(ELcdMS2525Standard aStandard) {
    super(aStandard);
  }

  public StyledEditableMS2525Object(ILcdMS2525bCoded aMS2525bCoded) {
    super(aMS2525bCoded);

    //copy customizable style properties
    if (aMS2525bCoded instanceof ILcdMS2525bStyled) {
      fStyle.setSymbolFrameEnabled(((ILcdMS2525bStyled) aMS2525bCoded).getMS2525bStyle().isSymbolFrameEnabled());
      fStyle.setSymbolFillEnabled(((ILcdMS2525bStyled) aMS2525bCoded).getMS2525bStyle().isSymbolFillEnabled());
      fStyle.setSymbolIconEnabled(((ILcdMS2525bStyled) aMS2525bCoded).getMS2525bStyle().isSymbolIconEnabled());
    }
  }

  public StyledEditableMS2525Object(String aMS2525bCode) {
    super(aMS2525bCode);
  }

  public StyledEditableMS2525Object(String aMS2525bCode, ELcdMS2525Standard aStandard) {
    super(aMS2525bCode, aStandard);
  }

  public StyledEditableMS2525Object(String aMS2525bCode, ELcdMS2525Standard aStandard, TLcdDefaultMS2525bStyle aStyle) {
    super(aMS2525bCode, aStandard);
    fStyle = aStyle;
  }

  public ILcdMS2525bStyle getMS2525bStyle() {
    return fStyle;
  }

  /**
   * Returns a customized TLcdDefaultMS2525bStyle with some nice defaults: a label halo,
   * orange selection color, ...
   *
   * @return a new instance of TLcdDefaultMS2525bStyle
   */
  public static TLcdDefaultMS2525bStyle getDefaultStyle() {
    TLcdDefaultMS2525bStyle style = TLcdDefaultMS2525bStyle.getNewInstance();
    // We don't want the position label to be displayed.
    style.setLabelEnabled(ILcdMS2525bCoded.sLocationLabel, false);

    // Adjust the default color
    style.setColor(Color.black);

    // Adjust the label color
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
    StyledEditableMS2525Object clone = (StyledEditableMS2525Object) super.clone();
    clone.fStyle = (TLcdDefaultMS2525bStyle) fStyle.clone();
    return clone;
  }

  public void storeState(Map aMap, ILcdModel aSourceModel) throws StateException {
    aMap.put(MS2525B_STATE_PREFIX + SYMBOL_CODE_REFERENCE_KEY, this.getMS2525Code());
    aMap.put(MS2525B_STATE_PREFIX + SOURCE_MODEL_REFERENCE_KEY, aSourceModel.getModelReference());
    aMap.put(MS2525B_STATE_PREFIX + SHAPE_WIDTH_REFERENCE_KEY, getWidth());
    // Note: we don't store the symbol standard, since this cannot be changed after object creation.
    storeStyleProperties(this, aMap, MS2525B_STATE_PREFIX);
    storeTextModifiers(this, aMap, MS2525B_STATE_PREFIX);
    StateUtil.storePointLocations(this, aSourceModel.getModelReference(), aMap, MS2525B_STATE_PREFIX + GEOMETRY_REFERENCE_KEY + ".");
  }

  public void restoreState(Map aMap, ILcdModel aTargetModel) throws StateException {
    String code = (String) aMap.get(MS2525B_STATE_PREFIX + SYMBOL_CODE_REFERENCE_KEY);
    if (code != null) {
      this.setMS2525Code(code);
    }
    restoreStyleProperties(this, aMap, MS2525B_STATE_PREFIX);
    restoreTextModifiers(this, aMap, MS2525B_STATE_PREFIX);
    try {
      ILcdModelReference sourceRef = (ILcdModelReference) aMap.get(MS2525B_STATE_PREFIX + SOURCE_MODEL_REFERENCE_KEY);
      ILcdPoint[] targetPoints = StateUtil.calculateTargetPoints(sourceRef, aTargetModel.getModelReference(), aMap, MS2525B_STATE_PREFIX + "geometry.");
      if (targetPoints != null) {
        StateUtil.adjustNumberOf2DPoints(this.get2DEditablePointList(), targetPoints);
        for (int i = 0; i < targetPoints.length; i++) {
          ILcdPoint target_point = targetPoints[i];
          move2DPoint(i, target_point.getX(), target_point.getY());
        }
      }

    } catch (TLcdOutOfBoundsException e) {
      throw new StateException(e);
    }

    Double width = (Double) aMap.get(MS2525B_STATE_PREFIX + SHAPE_WIDTH_REFERENCE_KEY);
    if (width != null) {
      setWidth(width.doubleValue());
    }
  }

  private void storeTextModifiers(ILcdEditableMS2525bCoded aCodedObject, Map aMap, String aPrefix) {
    String[] textModifiers = new String[aCodedObject.getTextModifierCount()];
    for (int i = 0; i < textModifiers.length; i++) {
      textModifiers[i] = aCodedObject.getTextModifierValue(aCodedObject.getTextModifierKey(i));
    }
    aMap.put(aPrefix + TEXT_MODIFIERS_REFERENCE_KEY, textModifiers);
  }

  private void storeStyleProperties(ILcdMS2525bStyled aStyledObject, Map aMap, String aPrefix) {
    ILcdMS2525bStyle style = aStyledObject.getMS2525bStyle();

    Object[] symbolStyleProperties = new Object[5];
    symbolStyleProperties[0] = style.isSymbolFillEnabled();
    symbolStyleProperties[1] = style.isSymbolFrameEnabled();
    symbolStyleProperties[2] = style.isSymbolIconEnabled();
    symbolStyleProperties[3] = style.getOffsetX();
    symbolStyleProperties[4] = style.getOffsetY();
    aMap.put(aPrefix + STYLE_PROPERTIES_REFERENCE_KEY, symbolStyleProperties);
  }

  private void restoreTextModifiers(ILcdEditableMS2525bCoded aCodedObject, Map aMap, String aPrefix) {
    String[] textModifiers = (String[]) aMap.get(aPrefix + TEXT_MODIFIERS_REFERENCE_KEY);
    if (textModifiers != null) {
      for (int i = 0; i < textModifiers.length; i++) {
        aCodedObject.putTextModifier(aCodedObject.getTextModifierKey(i), textModifiers[i]);
      }
    }
  }

  private void restoreStyleProperties(ILcdMS2525bStyled aStyledObject, Map aMap, String aPrefix) {
    ILcdMS2525bStyle style = aStyledObject.getMS2525bStyle();

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

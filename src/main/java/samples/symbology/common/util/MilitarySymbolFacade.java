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
package samples.symbology.common.util;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.luciad.gui.ILcdObjectIconProvider;
import samples.symbology.common.CamelCaseConverter;
import samples.symbology.common.EMilitarySymbology;
import samples.symbology.common.MilitarySymbologyModelDescriptor;
import samples.symbology.common.app6.StyledEditableAPP6Object;
import samples.symbology.common.ms2525.StyledEditableMS2525Object;
import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelDescriptor;
import com.luciad.model.transformation.ALcdTransformingModel;
import com.luciad.shape.ILcdPointList;
import com.luciad.symbology.app6a.model.ELcdAPP6Standard;
import com.luciad.symbology.app6a.model.ILcdAPP6ACoded;
import com.luciad.symbology.app6a.model.ILcdAPP6AModelDescriptor;
import com.luciad.symbology.app6a.model.ILcdAPP6AShape;
import com.luciad.symbology.app6a.model.ILcdEditableAPP6ACoded;
import com.luciad.symbology.app6a.model.ILcdRestrictedLengthPointList;
import com.luciad.symbology.app6a.model.TLcdAPP6AEchelonNode;
import com.luciad.symbology.app6a.model.TLcdAPP6ANode;
import com.luciad.symbology.app6a.model.TLcdEditableAPP6AObject;
import com.luciad.symbology.app6a.view.TLcdAPP6AObjectIconProvider;
import com.luciad.symbology.app6a.view.gxy.TLcdDefaultAPP6AStyle;
import com.luciad.symbology.milstd2525b.model.ELcdMS2525Standard;
import com.luciad.symbology.milstd2525b.model.ILcdEditableMS2525bCoded;
import com.luciad.symbology.milstd2525b.model.ILcdEditableMS2525bShape;
import com.luciad.symbology.milstd2525b.model.ILcdMS2525bCoded;
import com.luciad.symbology.milstd2525b.model.ILcdMS2525bModelDescriptor;
import com.luciad.symbology.milstd2525b.model.ILcdMS2525bShape;
import com.luciad.symbology.milstd2525b.model.TLcdEditableMS2525bObject;
import com.luciad.symbology.milstd2525b.model.TLcdMS2525bEchelonNode;
import com.luciad.symbology.milstd2525b.model.TLcdMS2525bNode;
import com.luciad.symbology.milstd2525b.view.TLcdMS2525bObjectIconProvider;
import com.luciad.symbology.milstd2525b.view.gxy.TLcdDefaultMS2525bStyle;
import com.luciad.view.ILcdLayer;
import com.luciad.view.ILcdLayered;

/**
 * Encapsulates common functionality for manipulating military symbols.
 * This class expects TLcdEditableMS2525bObject or TLcdEditableAPP6AObject instances.
 */
public class MilitarySymbolFacade {

  private static final Map<String, String> s2525ModifierDisplayNames;
  private static final Map<String, String> sAPP6ModifierDisplayNames;
  private static final Map<TLcdEditableMS2525bObject.TextModifierType, TextModifierType> s2525Types;
  private static final Map<TLcdEditableAPP6AObject.TextModifierType, TextModifierType> sAPP6Types;

  public static String getStatusValue(Object aSymbol) {
    checkEditableSymbol(aSymbol);
    return isMS2525Symbol(aSymbol) ?
           ((TLcdEditableMS2525bObject) aSymbol).getStatusValue() :
           ((TLcdEditableAPP6AObject) aSymbol).getStatusValue();
  }

  public static void setStatusValue(Object aSymbol, String aStatusValue) {
    checkEditableSymbol(aSymbol);
    if (aSymbol instanceof TLcdEditableMS2525bObject) {
      ((TLcdEditableMS2525bObject) aSymbol).setStatusValue(aStatusValue);
    } else {
      ((TLcdEditableAPP6AObject) aSymbol).setStatusValue(aStatusValue);
    }
  }

  public static Collection<String> getPossibleStatusValues(Object aSymbol) {
    checkEditableSymbol(aSymbol);
    return isMS2525Symbol(aSymbol) ?
           ((TLcdEditableMS2525bObject) aSymbol).getPossibleStatusValues() :
           ((TLcdEditableAPP6AObject) aSymbol).getPossibleStatusValues();
  }

  public static String getAffiliationValue(Object aSymbol) {
    checkEditableSymbol(aSymbol);
    return isMS2525Symbol(aSymbol) ?
           ((TLcdEditableMS2525bObject) aSymbol).getAffiliationValue() :
           ((TLcdEditableAPP6AObject) aSymbol).getAffiliationValue();
  }

  public static void setAffiliationValue(Object aSymbol, String aAffiliationValue) {
    checkEditableSymbol(aSymbol);
    if (isMS2525Symbol(aSymbol)) {
      ((TLcdEditableMS2525bObject) aSymbol).setAffiliationValue(aAffiliationValue);
    } else {
      ((TLcdEditableAPP6AObject) aSymbol).setAffiliationValue(aAffiliationValue);
    }
  }

  public static Collection<String> getPossibleAffiliationValues(Object aSymbol) {
    checkEditableSymbol(aSymbol);
    return isMS2525Symbol(aSymbol) ?
           ((TLcdEditableMS2525bObject) aSymbol).getPossibleAffiliationValues() :
           ((TLcdEditableAPP6AObject) aSymbol).getPossibleAffiliationValues();
  }

  public static void setSector1Value(Object aSymbol, String aValue) {
    checkEditableSymbol(aSymbol);
    Modifier modifier = getModifier(aSymbol, ILcdAPP6ACoded.sSector1);
    setModifierValue(aSymbol, modifier, aValue);
  }

  public static String getSector1Value(Object aSymbol) {
    checkEditableSymbol(aSymbol);
    Modifier modifier = getModifier(aSymbol, ILcdAPP6ACoded.sSector1);
    return modifier == null ? null : getModifierValue(aSymbol, modifier);
  }

  public static Collection<String> getPossibleSector1Values(Object aSymbol) {
    checkEditableSymbol(aSymbol);
    Modifier modifier = getModifier(aSymbol, ILcdAPP6ACoded.sSector1);
    return modifier != null ? modifier.getPossibleValues() : Collections.<String>emptyList();
  }

  public static void setSector2Value(Object aSymbol, String aValue) {
    checkEditableSymbol(aSymbol);
    Modifier modifier = getModifier(aSymbol, ILcdAPP6ACoded.sSector2);
    setModifierValue(aSymbol, modifier, aValue);
  }

  public static String getSector2Value(Object aSymbol) {
    checkEditableSymbol(aSymbol);
    Modifier modifier = getModifier(aSymbol, ILcdAPP6ACoded.sSector2);
    return modifier == null ? null : getModifierValue(aSymbol, modifier);
  }

  public static Collection<String> getPossibleSector2Values(Object aSymbol) {
    checkEditableSymbol(aSymbol);
    Modifier modifier = getModifier(aSymbol, ILcdAPP6ACoded.sSector2);
    return modifier != null ? modifier.getPossibleValues() : Collections.<String>emptyList();
  }

  public static Modifier getModifier(Object aSymbol, String aModifier) {
    checkEditableSymbol(aSymbol);
    if (isMS2525Symbol(aSymbol)) {
      TLcdEditableMS2525bObject symbol = (TLcdEditableMS2525bObject) aSymbol;
      TLcdEditableMS2525bObject.TextModifier modifier = symbol.getTextModifier(aModifier);
      return modifier == null ? null : new Modifier(modifier);
    } else {
      TLcdEditableAPP6AObject symbol = (TLcdEditableAPP6AObject) aSymbol;
      TLcdEditableAPP6AObject.TextModifier modifier = symbol.getTextModifier(aModifier);
      return modifier == null ? null : new Modifier(modifier);
    }
  }

  public static Color getAffiliationColor(Object aSymbol, String aAffiliation) {
    checkSymbol(aSymbol);
    if (isMS2525Symbol(aSymbol)) {
      TLcdDefaultMS2525bStyle style = aSymbol instanceof StyledEditableMS2525Object &&
                                      ((StyledEditableMS2525Object) aSymbol).getMS2525bStyle() instanceof TLcdDefaultMS2525bStyle ?
                                      (TLcdDefaultMS2525bStyle) ((StyledEditableMS2525Object) aSymbol).getMS2525bStyle() :
                                      TLcdDefaultMS2525bStyle.getInstance();
      return style.getAffiliationColor(((ILcdMS2525bCoded) aSymbol).getMS2525Standard(), aAffiliation);
    } else {
      TLcdDefaultAPP6AStyle style = aSymbol instanceof StyledEditableAPP6Object &&
                                    ((StyledEditableAPP6Object) aSymbol).getAPP6AStyle() instanceof TLcdDefaultAPP6AStyle ?
                                    (TLcdDefaultAPP6AStyle) ((StyledEditableAPP6Object) aSymbol).getAPP6AStyle() :
                                    TLcdDefaultAPP6AStyle.getInstance();
      return style.getAffiliationColor(((ILcdAPP6ACoded) aSymbol).getAPP6Standard(), aAffiliation);
    }
  }

  public static String getCountry(Object aSymbol) {
    checkEditableSymbol(aSymbol);
    return isMS2525Symbol(aSymbol) ?
           ((TLcdEditableMS2525bObject) aSymbol).getCountry() :
           ((TLcdEditableAPP6AObject) aSymbol).getCountry();
  }

  public static void setCountry(Object aSymbol, String aCountry) {
    checkEditableSymbol(aSymbol);
    if (isMS2525Symbol(aSymbol)) {
      ((TLcdEditableMS2525bObject) aSymbol).setCountry(aCountry);
    } else {
      ((TLcdEditableAPP6AObject) aSymbol).setCountry(aCountry);
    }
  }

  public static Collection<String> getPossibleCountries(Object aSymbol) {
    checkEditableSymbol(aSymbol);
    return isMS2525Symbol(aSymbol) ?
           ((TLcdEditableMS2525bObject) aSymbol).getPossibleCountries() :
           ((TLcdEditableAPP6AObject) aSymbol).getPossibleCountries();
  }

  public static String getOrderOfBattleValue(Object aSymbol) {
    checkEditableSymbol(aSymbol);
    return isMS2525Symbol(aSymbol) ?
           ((TLcdEditableMS2525bObject) aSymbol).getOrderOfBattleValue() :
           ((TLcdEditableAPP6AObject) aSymbol).getOrderOfBattleValue();
  }

  public static void setOrderOfBattleValue(Object aSymbol, String aDisplayName) {
    checkEditableSymbol(aSymbol);
    if (isMS2525Symbol(aSymbol)) {
      ((TLcdEditableMS2525bObject) aSymbol).setOrderOfBattleValue(aDisplayName);
    } else {
      ((TLcdEditableAPP6AObject) aSymbol).setOrderOfBattleValue(aDisplayName);
    }
  }

  public static Collection<String> getPossibleOrderOfBattleValues(Object aSymbol) {
    checkEditableSymbol(aSymbol);
    return isMS2525Symbol(aSymbol) ?
           ((TLcdEditableMS2525bObject) aSymbol).getPossibleOrderOfBattleValues() :
           ((TLcdEditableAPP6AObject) aSymbol).getPossibleOrderOfBattleValues();
  }

  public static Collection<Modifier> getPossibleModifiers(Object aSymbol) {
    checkEditableSymbol(aSymbol);
    ArrayList<Modifier> result = new ArrayList<>();
    if (isMS2525Symbol(aSymbol)) {
      Collection<TLcdEditableMS2525bObject.TextModifier> modifiers = ((TLcdEditableMS2525bObject) aSymbol).getPossibleModifiers();
      for (TLcdEditableMS2525bObject.TextModifier modifier : modifiers) {
        result.add(new Modifier(modifier));
      }
    } else {
      Collection<TLcdEditableAPP6AObject.TextModifier> modifiers = ((TLcdEditableAPP6AObject) aSymbol).getPossibleModifiers();
      for (TLcdEditableAPP6AObject.TextModifier modifier : modifiers) {
        result.add(new Modifier(modifier));
      }
    }
    return result;
  }

  public static Collection<Modifier> getPossibleSIDCModifiers(Object aSymbol) {
    checkEditableSymbol(aSymbol);
    ArrayList<Modifier> result = new ArrayList<>();
    if (isMS2525Symbol(aSymbol)) {
      Collection<TLcdEditableMS2525bObject.TextModifier> modifiers = ((TLcdEditableMS2525bObject) aSymbol).getPossibleSIDCModifiers();
      for (TLcdEditableMS2525bObject.TextModifier modifier : modifiers) {
        result.add(new Modifier(modifier));
      }
    } else {
      Collection<TLcdEditableAPP6AObject.TextModifier> modifiers = ((TLcdEditableAPP6AObject) aSymbol).getPossibleSIDCModifiers();
      for (TLcdEditableAPP6AObject.TextModifier modifier : modifiers) {
        result.add(new Modifier(modifier));
      }
    }
    return result;
  }

  public static Collection<Modifier> getPossibleTextModifiers(Object aSymbol) {
    checkEditableSymbol(aSymbol);
    ArrayList<Modifier> result = new ArrayList<>();
    if (isMS2525Symbol(aSymbol)) {
      Collection<TLcdEditableMS2525bObject.TextModifier> modifiers = ((TLcdEditableMS2525bObject) aSymbol).getPossibleTextModifiers();
      for (TLcdEditableMS2525bObject.TextModifier modifier : modifiers) {
        result.add(new Modifier(modifier));
      }
    } else {
      Collection<TLcdEditableAPP6AObject.TextModifier> modifiers = ((TLcdEditableAPP6AObject) aSymbol).getPossibleTextModifiers();
      for (TLcdEditableAPP6AObject.TextModifier modifier : modifiers) {
        result.add(new Modifier(modifier));
      }
    }
    return result;
  }

  public static void setModifierValue(Object aSymbol, Modifier aModifier, String aValue) {
    if (aModifier == null) {
      return;
    }
    if (aSymbol instanceof ILcdEditableMS2525bCoded) {
      ((ILcdEditableMS2525bCoded) aSymbol).putTextModifier(aModifier.getName(), aValue);
    } else if (aSymbol instanceof ILcdEditableAPP6ACoded) {
      ((ILcdEditableAPP6ACoded) aSymbol).putTextModifier(aModifier.getName(), aValue);
    }
  }

  public static String getModifierValue(Object aSymbol, Modifier aModifier) {
    if (aModifier == null) {
      return null;
    }
    if (aSymbol instanceof ILcdMS2525bCoded) {
      return ((ILcdMS2525bCoded) aSymbol).getTextModifierValue(aModifier.getName());
    } else {
      return ((ILcdAPP6ACoded) aSymbol).getTextModifierValue(aModifier.getName());
    }
  }

  public static void getAllPossibleEchelonsSFCT(EMilitarySymbology aSymbology, List<Object> aValues, List<String> aDisplayNames) {
    if (aSymbology.getStandard() instanceof ELcdMS2525Standard) {
      TLcdMS2525bEchelonNode node = TLcdMS2525bEchelonNode.getRoot((ELcdMS2525Standard) aSymbology.getStandard());
      retrieveEchelons(node, null, aValues, aDisplayNames);
    } else {
      TLcdAPP6AEchelonNode node = TLcdAPP6AEchelonNode.getRoot((ELcdAPP6Standard) aSymbology.getStandard());
      retrieveEchelons(node, null, aValues, aDisplayNames);
    }
  }

  public static void getPossibleEchelonsSFCT(Object aSymbol, List<Object> aValues, List<String> aDisplayNames) {
    checkSymbol(aSymbol);
    if (isMS2525Symbol(aSymbol)) {
      TLcdMS2525bEchelonNode node = TLcdMS2525bEchelonNode.getRoot(((ILcdMS2525bCoded) aSymbol).getMS2525Standard());
      retrieveEchelons(node, (ILcdMS2525bCoded) aSymbol, aValues, aDisplayNames);
    } else {
      TLcdAPP6AEchelonNode node = TLcdAPP6AEchelonNode.getRoot(((ILcdAPP6ACoded) aSymbol).getAPP6Standard());
      retrieveEchelons(node, (ILcdAPP6ACoded) aSymbol, aValues, aDisplayNames);
    }
  }

  public static Object getEchelon(Object aSymbol) {
    checkEditableSymbol(aSymbol);
    if (isMS2525Symbol(aSymbol)) {
      return ((TLcdEditableMS2525bObject) aSymbol).getEchelon();
    }
    return ((TLcdEditableAPP6AObject) aSymbol).getEchelon();
  }

  public static void setEchelon(Object aSymbol, Object aValue) {
    checkEditableSymbol(aSymbol);
    if (aValue instanceof TLcdMS2525bEchelonNode) {
      ((TLcdMS2525bEchelonNode) aValue).applyOn((ILcdEditableMS2525bCoded) aSymbol);
    } else {
      ((TLcdAPP6AEchelonNode) aValue).applyOn((ILcdEditableAPP6ACoded) aSymbol);
    }
  }

  public static String getSIDC(Object aSymbol) {
    checkSymbol(aSymbol);
    return aSymbol instanceof ILcdMS2525bCoded ? ((ILcdMS2525bCoded) aSymbol).getMS2525Code() : ((ILcdAPP6ACoded) aSymbol).getAPP6ACode();
  }

  /**
   * Replaces the target's symbol identification code.
   * @param aSymbol the symbol whose code to change
   * @param aSIDC the new symbol code
   */
  public static void setSIDC(Object aSymbol, String aSIDC) {
    checkEditableSymbol(aSymbol);
    if (aSymbol instanceof ILcdEditableMS2525bCoded) {
      ((ILcdEditableMS2525bCoded) aSymbol).setMS2525Code(aSIDC);
    } else {
      ((ILcdEditableAPP6ACoded) aSymbol).setAPP6ACode(aSIDC);
    }
  }

  /**
   * Changes the symbol's type according to the given hierarchy mask,
   * retaining existing symbol modifiers.
   *
   * @param aSymbol the symbol to change
   * @param aHierarchyMask the new symbol type
   */
  public static void changeHierarchy(Object aSymbol, String aHierarchyMask) {
    checkEditableSymbol(aSymbol);
    if (isMS2525Symbol(aSymbol)) {
      TLcdEditableMS2525bObject symbol = (TLcdEditableMS2525bObject) aSymbol;
      TLcdMS2525bNode newHierarchy = TLcdMS2525bNode.get(aHierarchyMask, symbol.getMS2525Standard());
      symbol.setHierarchyType(newHierarchy);
    } else {
      TLcdEditableAPP6AObject symbol = (TLcdEditableAPP6AObject) aSymbol;
      TLcdAPP6ANode newHierarchy = TLcdAPP6ANode.get(aHierarchyMask, symbol.getAPP6Standard());
      symbol.setHierarchyType(newHierarchy);
    }
  }

  /**
   * Returns the canonical SIDC mask of the given SIDC, removing all configurable modifier values.
   * This would, for example, for a friendly Afghan APP-6A space track "SFPA--------AF-"
   * return the generic space track SIDC mask "S*P*------*****".
   *
   * @param aSymbology the symbology of the given SIDC
   * @param aSIDC a symbol identifier
   * @return the expected SIDC mask of the symbol when new hierarchyMask is applied
   */
  public static String getSIDCMask(EMilitarySymbology aSymbology, String aSIDC) {
    if (aSymbology.getStandard() instanceof ELcdAPP6Standard) {
      TLcdAPP6ANode hierarchy = TLcdAPP6ANode.get(aSIDC, (ELcdAPP6Standard) aSymbology.getStandard());
      return hierarchy.getCodeMask();
    } else {
      TLcdMS2525bNode hierarchy = TLcdMS2525bNode.get(aSIDC, (ELcdMS2525Standard) aSymbology.getStandard());
      return hierarchy.getCodeMask();
    }
  }

  /**
   * Returns a name for the given symbol that can be shown to the user.
   */
  public static String getDisplayName(Object aSymbol) {
    checkEditableSymbol(aSymbol);
    if (isMS2525Symbol(aSymbol)) {
      TLcdMS2525bNode hierarchyType = ((TLcdEditableMS2525bObject) aSymbol).getHierarchyType();
      return addParentName(hierarchyType.getName(), hierarchyType.getParent() == null ? null : hierarchyType.getParent().getName());
    } else {
      TLcdAPP6ANode hierarchyType = ((TLcdEditableAPP6AObject) aSymbol).getHierarchyType();
      return addParentName(hierarchyType.getName(), hierarchyType.getParent() == null ? null : hierarchyType.getParent().getName());
    }
  }

  /**
   * The specifications are not very consistent regarding duplication of information between a parent symbol and its children.
   * This method tries to remove redundant information.
   */
  public static String addParentName(String aChildName, String aParentName) {
    if (aParentName == null) {
      return aChildName;
    }
    // replace words that are common in the parent
    String[] parentWords = aParentName.split("[ ,]");
    String[] childWords = aChildName.split("[ ,]");
    StringBuilder uniqueChildWords = new StringBuilder();
    for (int i = 0; i < parentWords.length || i < childWords.length; i++) {
      if (i >= parentWords.length) {
        uniqueChildWords.append(childWords[i]).append(" ");
      } else if (i < childWords.length && !parentWords[i].equals(childWords[i])) {
        uniqueChildWords.append(childWords[i]).append(" ");
      }
    }
    String trimmedChildName = uniqueChildWords.toString();
    if (trimmedChildName.startsWith(" ,")) {
      trimmedChildName = trimmedChildName.replaceFirst(", ", "");
    }
    if (trimmedChildName.startsWith("- ")) {
      trimmedChildName = trimmedChildName.replaceFirst("- ", "");
    }
    trimmedChildName = aParentName + " - " + trimmedChildName;
    return trimmedChildName.trim();
  }

  /**
   * Returns the topmost visible editable military symbology layer.
   *
   * @param aView the view to search
   *
   * @return the topmost visible military symbology layer
   */
  public static ILcdLayer retrieveCompatibleEditableLayer(ILcdLayered aView) {
    for (int i = aView.layerCount() - 1; i >= 0; i--) {
      ILcdLayer layer = aView.getLayer(i);
      ILcdModel model = layer.getModel();
      ILcdModelDescriptor modelDescriptor = model != null ? model.getModelDescriptor() : null;
      modelDescriptor = model instanceof ALcdTransformingModel ? ((ALcdTransformingModel) model).getOriginalModel().getModelDescriptor() : modelDescriptor;
      if (layer.isVisible() && layer.isEditable() && layer.isSelectable() &&
          (modelDescriptor instanceof ILcdAPP6AModelDescriptor || modelDescriptor instanceof ILcdMS2525bModelDescriptor)) {
        return layer;
      }
    }
    return null;
  }

  /**
   * Creates a new, initialized military symbol of the given symbology.
   *
   * @param aSymbology the symbology of the new symbol
   * @param aStyled if the new object should have an associated style
   * @return a new, initialized military symbol (never null)
   */
  public static Object newElement(EMilitarySymbology aSymbology, boolean aStyled) {
    Object result;
    switch (aSymbology) {
    case APP6A:
      result = (aStyled) ?
               new StyledEditableAPP6Object(ELcdAPP6Standard.APP_6A) :
               new TLcdEditableAPP6AObject(ELcdAPP6Standard.APP_6A);
      break;
    case APP6B:
      result = (aStyled) ?
               new StyledEditableAPP6Object(ELcdAPP6Standard.APP_6B) :
               new TLcdEditableAPP6AObject(ELcdAPP6Standard.APP_6B);
      break;
    case APP6C:
      result = (aStyled) ?
               new StyledEditableAPP6Object(ELcdAPP6Standard.APP_6C) :
               new TLcdEditableAPP6AObject(ELcdAPP6Standard.APP_6C);
      break;
    case MILSTD_2525B:
      result = (aStyled) ?
               new StyledEditableMS2525Object(ELcdMS2525Standard.MIL_STD_2525b) :
               new TLcdEditableMS2525bObject(ELcdMS2525Standard.MIL_STD_2525b);
      break;
    case MILSTD_2525C:
      result = (aStyled) ?
               new StyledEditableMS2525Object(ELcdMS2525Standard.MIL_STD_2525c) :
               new TLcdEditableMS2525bObject(ELcdMS2525Standard.MIL_STD_2525c);
      break;
    default:
      throw new IllegalArgumentException("Unsupported symbology " + aSymbology);
    }
    return result;
  }

  /**
   * Creates a new, initialized military symbol that can be added to the given layer.
   *
   * @param aLayer  used to determine the needed symbology. The model descriptor must implement
   *                MilitarySymbologyModelDescriptor.
   * @param aStyled if the new object should have an associated style
   * @return a new, initialized military symbol (never null)
   */
  public static Object newSymbol(ILcdLayer aLayer, boolean aStyled) {
    ILcdModel model = aLayer.getModel();
    ILcdModelDescriptor modelDescriptor = model instanceof ALcdTransformingModel ? ((ALcdTransformingModel) model).getOriginalModel().getModelDescriptor() : model.getModelDescriptor();
    if (modelDescriptor instanceof MilitarySymbologyModelDescriptor) {
      EMilitarySymbology symbology = ((MilitarySymbologyModelDescriptor) modelDescriptor).getSymbology();
      return newElement(symbology, aStyled);
    } else {
      throw new IllegalArgumentException("Cannot determine the user symbology standard, layer must implement " + MilitarySymbologyModelDescriptor.class);
    }
  }

  public static EMilitarySymbology retrieveTopmostCompatibleLayerSymbology(ILcdLayered aView) {
    ILcdLayer layer = retrieveCompatibleEditableLayer(aView);
    if (layer == null) {
      return null;
    }
    ILcdModel model = layer.getModel();
    ILcdModelDescriptor modelDescriptor = model instanceof ALcdTransformingModel ? ((ALcdTransformingModel) model).getOriginalModel().getModelDescriptor() : model.getModelDescriptor();
    if (modelDescriptor instanceof MilitarySymbologyModelDescriptor) {
      return ((MilitarySymbologyModelDescriptor) modelDescriptor).getSymbology();
    }
    throw new RuntimeException("This UI component needs the layer's model descriptor to implement " + MilitarySymbologyModelDescriptor.class);
  }

  public static EMilitarySymbology retrieveSymbology(Object aSymbol) {
    return isMS2525Symbol(aSymbol) ?
           EMilitarySymbology.fromStandard(((ILcdMS2525bCoded) aSymbol).getMS2525Standard()) :
           EMilitarySymbology.fromStandard(((ILcdAPP6ACoded) aSymbol).getAPP6Standard());
  }

  public static boolean isMilitarySymbol(Object aObject) {
    return aObject instanceof ILcdAPP6ACoded || aObject instanceof ILcdMS2525bCoded;
  }

  /**
   * Returns the required number of clicks to create the given instance. If the result is negative,
   * the absolute value is the minimum amount of clicks that is required; but more clicks are
   * possible. This is for instance the case for polyline-like symbols.
   */
  public static int getRequiredNumberOfClicks(Object aSymbol) {
    checkEditableSymbol(aSymbol);
    if (aSymbol instanceof ILcdEditableMS2525bShape) {
      return getRequiredNumberOfClicksMS2525((ILcdEditableMS2525bShape) aSymbol);
    } else {
      return getRequiredNumberOfClicksAPP6((TLcdEditableAPP6AObject) aSymbol);
    }
  }

  /**
   * Returns the number of points in the given instance.
   */
  public static int getPointCount(Object aSymbol) {
    checkEditableSymbol(aSymbol);
    if (aSymbol instanceof ILcdMS2525bShape) {
      ILcdPointList pointList = ((ILcdMS2525bShape) aSymbol).getPointList();
      return pointList == null ? 0 : pointList.getPointCount();
    } else {
      ILcdPointList pointList = ((ILcdAPP6AShape) aSymbol).getPointList();
      return pointList == null ? 0 : pointList.getPointCount();
    }
  }

  public static void move2DPoint(Object aSymbol, int aIndex, double aX, double aY) {
    checkEditableSymbol(aSymbol);
    if (aSymbol instanceof TLcdEditableMS2525bObject) {
      ((TLcdEditableMS2525bObject) aSymbol).move2DPoint(aIndex, aX, aY);
    } else if (aSymbol instanceof TLcdEditableAPP6AObject) {
      ((TLcdEditableAPP6AObject) aSymbol).move2DPoint(aIndex, aX, aY);
    }
  }

  public static boolean isLine(Object aSymbol) {
    checkEditableSymbol(aSymbol);
    if (aSymbol instanceof ILcdAPP6AShape) {
      ILcdAPP6AShape shape = (ILcdAPP6AShape) aSymbol;
      return shape.isLine();
    } else {
      ILcdMS2525bShape shape = (ILcdMS2525bShape) aSymbol;
      return shape.isLine();
    }
  }

  public static ILcdObjectIconProvider createMS2525IconProvider(int aSymbolSize) {
    TLcdMS2525bObjectIconProvider provider = new TLcdMS2525bObjectIconProvider();
    provider.getDefaultStyle().setSizeSymbol(aSymbolSize);
    return provider;
  }

  public static ILcdObjectIconProvider createAPP6IconProvider(int aSymbolSize) {
    TLcdAPP6AObjectIconProvider provider = new TLcdAPP6AObjectIconProvider();
    provider.getDefaultStyle().setSizeSymbol(aSymbolSize);
    return provider;
  }

  public static EMilitarySymbology getMilitarySymbology(Object aSymbol) {
    if (aSymbol instanceof ILcdMS2525bCoded) {
      return EMilitarySymbology.fromStandard(((ILcdMS2525bCoded) aSymbol).getMS2525Standard());
    } else if (aSymbol instanceof ILcdAPP6ACoded) {
      return EMilitarySymbology.fromStandard(((ILcdAPP6ACoded) aSymbol).getAPP6Standard());
    } else {
      return null;
    }
  }

  public static void reversePoints(Object aSymbol) {
    if (aSymbol instanceof TLcdEditableMS2525bObject) {
      ((TLcdEditableMS2525bObject) aSymbol).reversePointOrder();
    } else if (aSymbol instanceof TLcdEditableAPP6AObject) {
      ((TLcdEditableAPP6AObject) aSymbol).reversePointOrder();
    } else {
      throw new IllegalArgumentException("Unrecognized symbol. It should be either TLcdEditableMS2525bObject or TLcdEditableAPP6AObject");
    }
  }

  public static boolean canReversePoints(Object aSymbol) {
    if (aSymbol instanceof TLcdEditableMS2525bObject) {
      return ((TLcdEditableMS2525bObject) aSymbol).canReversePointOrder();
    } else if (aSymbol instanceof TLcdEditableAPP6AObject) {
      return ((TLcdEditableAPP6AObject) aSymbol).canReversePointOrder();
    } else {
      return false;
    }
  }

  /**
   * Combines {@link TLcdEditableMS2525bObject.TextModifier} and {@link TLcdEditableAPP6AObject.TextModifier}.
   */
  public static class Modifier {

    private Object fModifier;

    public Modifier(Object aModifier) {
      fModifier = aModifier;
    }

    public TextModifierType getType() {
      if (fModifier instanceof TLcdEditableMS2525bObject.TextModifier) {
        return s2525Types.get(((TLcdEditableMS2525bObject.TextModifier) fModifier).getType());
      }
      return sAPP6Types.get(((TLcdEditableAPP6AObject.TextModifier) fModifier).getType());
    }

    public Collection<String> getPossibleValues() {
      if (fModifier instanceof TLcdEditableMS2525bObject.TextModifier) {
        return ((TLcdEditableMS2525bObject.TextModifier) fModifier).getPossibleValues();
      }
      return ((TLcdEditableAPP6AObject.TextModifier) fModifier).getPossibleValues();
    }

    public int getLength() {
      if (fModifier instanceof TLcdEditableMS2525bObject.TextModifier) {
        return ((TLcdEditableMS2525bObject.TextModifier) fModifier).getLength();
      }
      return ((TLcdEditableAPP6AObject.TextModifier) fModifier).getLength();
    }

    public String getName() {
      if (fModifier instanceof TLcdEditableMS2525bObject.TextModifier) {
        return ((TLcdEditableMS2525bObject.TextModifier) fModifier).getName();
      }
      return ((TLcdEditableAPP6AObject.TextModifier) fModifier).getName();
    }

    public String getDisplayName() {
      if (fModifier instanceof TLcdEditableMS2525bObject.TextModifier) {
        return ((TLcdEditableMS2525bObject.TextModifier) fModifier).getDisplayName();
      }
      return ((TLcdEditableAPP6AObject.TextModifier) fModifier).getDisplayName();
    }

    public String getShortDisplayName() {
      final String name;
      final String displayName;
      final String shortName;
      if (fModifier instanceof TLcdEditableMS2525bObject.TextModifier) {
        name = ((TLcdEditableMS2525bObject.TextModifier) fModifier).getName();
        displayName = ((TLcdEditableMS2525bObject.TextModifier) fModifier).getDisplayName();
        shortName = s2525ModifierDisplayNames.get(name);
      } else {
        name = ((TLcdEditableAPP6AObject.TextModifier) fModifier).getName();
        displayName = ((TLcdEditableAPP6AObject.TextModifier) fModifier).getDisplayName();
        shortName = sAPP6ModifierDisplayNames.get(name);
      }
      return shortName != null ? shortName : displayName;
    }

    public String getDisplayName(String aValue) {
      if (fModifier instanceof TLcdEditableMS2525bObject.TextModifier) {
        return ((TLcdEditableMS2525bObject.TextModifier) fModifier).getDisplayName(aValue);
      }
      return ((TLcdEditableAPP6AObject.TextModifier) fModifier).getDisplayName(aValue);
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      Modifier that = (Modifier) o;
      return fModifier.equals(that.fModifier);
    }

    @Override
    public int hashCode() {
      return fModifier.hashCode();
    }

    public String toString() {
      return fModifier.toString();
    }
  }

  public enum TextModifierType {
    STRING_ANY,
    STRING_POSITIVE_INTEGER,
    STRING_BOOLEAN,
    STRING_ENUM,
    STRING_DATE_TIME
  }

  private static void checkSymbol(Object aSymbol) {
    if (!(aSymbol instanceof ILcdAPP6ACoded) && !(aSymbol instanceof ILcdMS2525bCoded)) {
      throw new IllegalArgumentException("Unrecognized symbol. Expecting APP6 or MS2525 symbol: " + aSymbol);
    }
  }

  private static void checkEditableSymbol(Object aSymbol) {
    if (!(aSymbol instanceof TLcdEditableAPP6AObject) && !(aSymbol instanceof TLcdEditableMS2525bObject)) {
      throw new IllegalArgumentException("Unrecognized symbol. Expecting editable APP6 or MS2525 symbol: " + aSymbol);
    }
  }

  public static boolean isMS2525Symbol(Object aSymbol) {
    return aSymbol instanceof ILcdMS2525bCoded;
  }

  private static void retrieveEchelons(TLcdMS2525bEchelonNode aNode, ILcdMS2525bCoded aSymbol, List<Object> aList, List<String> aDescriptions) {
    if (aSymbol == null || aNode.canApplyOn(aSymbol)) {
      aList.add(aNode);
      aDescriptions.add(CamelCaseConverter.toTitleCase(aNode.getName()));
    }
    for (TLcdMS2525bEchelonNode node : aNode.getChildren()) {
      retrieveEchelons(node, aSymbol, aList, aDescriptions);
    }
  }

  private static void retrieveEchelons(TLcdAPP6AEchelonNode aNode, ILcdAPP6ACoded aSymbol, List<Object> aList, List<String> aDescriptions) {
    if (aSymbol == null || aNode.canApplyOn(aSymbol)) {
      aList.add(aNode);
      aDescriptions.add(CamelCaseConverter.toTitleCase(aNode.getName()));
    }
    for (TLcdAPP6AEchelonNode node : aNode.getChildren()) {
      retrieveEchelons(node, aSymbol, aList, aDescriptions);
    }
  }

  private static int getRequiredNumberOfClicksMS2525(ILcdEditableMS2525bShape aInstance) {
    if (aInstance.isLine()) {
      if (aInstance.getPointList() instanceof com.luciad.symbology.milstd2525b.model.ILcdRestrictedLengthPointList) {
        com.luciad.symbology.milstd2525b.model.ILcdRestrictedLengthPointList list = (com.luciad.symbology.milstd2525b.model.ILcdRestrictedLengthPointList) aInstance.getPointList();
        return list.getMinPointCount();
      } else {
        return -2;
      }
    }
    return 1;
  }

  private static int getRequiredNumberOfClicksAPP6(TLcdEditableAPP6AObject aInstance) {
    if (aInstance.isLine()) {
      if (aInstance.get2DEditablePointList() instanceof ILcdRestrictedLengthPointList) {
        ILcdRestrictedLengthPointList list = (ILcdRestrictedLengthPointList) aInstance.get2DEditablePointList();
        return list.getMinPointCount();
      } else {
        return -2;
      }
    }
    return 1;
  }

  static {
    s2525ModifierDisplayNames = new HashMap<>();
    s2525ModifierDisplayNames.put(ILcdMS2525bCoded.sUniqueDesignation, "Name");
    s2525ModifierDisplayNames.put(ILcdMS2525bCoded.sHigherFormation, "Superior");
    s2525ModifierDisplayNames.put(ILcdMS2525bCoded.sDateTimeGroup, "Date/time");
    s2525ModifierDisplayNames.put(ILcdMS2525bCoded.sStaffComments, "Commentary");
    s2525ModifierDisplayNames.put(ILcdMS2525bCoded.sAdditionalInformation, "Additional info");
    s2525ModifierDisplayNames.put(ILcdMS2525bCoded.sQuantity, "Quantity");
    s2525ModifierDisplayNames.put(ILcdMS2525bCoded.sReinforcedOrReduced, "Reinforcement");
    s2525ModifierDisplayNames.put(ILcdMS2525bCoded.sEvaluationRating, "Evaluation rating");
    s2525ModifierDisplayNames.put(ILcdMS2525bCoded.sCombatEffectiveness, "Combat efficiency");
    s2525ModifierDisplayNames.put(ILcdMS2525bCoded.sSignatureEquipment, "Signature");
    s2525ModifierDisplayNames.put(ILcdMS2525bCoded.sIFFSIF, "IFF/SIF");
    s2525ModifierDisplayNames.put(ILcdMS2525bCoded.sMovementDirection, "Azimuth");
    s2525ModifierDisplayNames.put(ILcdMS2525bCoded.sTypeLabel, "Type");
    s2525ModifierDisplayNames.put(ILcdMS2525bCoded.sAltitudeDepth, "Altitude/depth");
    s2525ModifierDisplayNames.put(ILcdMS2525bCoded.sSpeedLabel, "Speed");
    s2525ModifierDisplayNames.put(ILcdMS2525bCoded.sEffectiveTime, "Effective date/time");
  }

  static {
    sAPP6ModifierDisplayNames = new HashMap<>();
    sAPP6ModifierDisplayNames.put("hqTaskForceDummy", "HQ/TF/Dummy");
    sAPP6ModifierDisplayNames.put("standardIdentity1", "Std Identity 1");
    sAPP6ModifierDisplayNames.put("standardIdentity2", "Std Identity 2");
    sAPP6ModifierDisplayNames.put(ILcdAPP6ACoded.sUniqueDesignation, "Name");
    sAPP6ModifierDisplayNames.put(ILcdAPP6ACoded.sHigherFormation, "Superior");
    sAPP6ModifierDisplayNames.put(ILcdAPP6ACoded.sDateTimeGroup, "Date/time");
    sAPP6ModifierDisplayNames.put(ILcdAPP6ACoded.sStaffComments, "Commentary");
    sAPP6ModifierDisplayNames.put(ILcdAPP6ACoded.sAdditionalInformation, "Additional info");
    sAPP6ModifierDisplayNames.put(ILcdAPP6ACoded.sQuantityOfEquipment, "Quantity");
    sAPP6ModifierDisplayNames.put(ILcdAPP6ACoded.sReinforcedOrReduced, "Reinforcement");
    sAPP6ModifierDisplayNames.put(ILcdAPP6ACoded.sEvaluationRating, "Evaluation rating");
    sAPP6ModifierDisplayNames.put(ILcdAPP6ACoded.sCombatEffectiveness, "Combat efficiency");
    sAPP6ModifierDisplayNames.put(ILcdAPP6ACoded.sSignatureEquipment, "Signature");
    sAPP6ModifierDisplayNames.put(ILcdAPP6ACoded.sIFFSIF, "IFF/SIF");
    sAPP6ModifierDisplayNames.put(ILcdAPP6ACoded.sMovementDirection, "Azimuth");
    sAPP6ModifierDisplayNames.put(ILcdAPP6ACoded.sTypeOfEquipment, "Type");
    sAPP6ModifierDisplayNames.put(ILcdAPP6ACoded.sAltitudeDepth, "Altitude/depth");
    sAPP6ModifierDisplayNames.put(ILcdAPP6ACoded.sSpeedLabel, "Speed");
    sAPP6ModifierDisplayNames.put(ILcdAPP6ACoded.sEffectiveTime, "Effective date/time");
    sAPP6ModifierDisplayNames.put(ILcdAPP6ACoded.sFrameShapeModifier, "Frame shape modifier");
    sAPP6ModifierDisplayNames.put(ILcdAPP6ACoded.sCapacity, "Capacity");
    sAPP6ModifierDisplayNames.put(ILcdAPP6ACoded.sHostile, "Hostile");
    sAPP6ModifierDisplayNames.put(ILcdAPP6ACoded.sLocationLabel, "Location label");
    sAPP6ModifierDisplayNames.put(ILcdAPP6ACoded.sCountry, "Country");
    sAPP6ModifierDisplayNames.put(ILcdAPP6ACoded.sPlatformType, "Platform type");
    sAPP6ModifierDisplayNames.put(ILcdAPP6ACoded.sTeardownTime, "Teardown time");
    sAPP6ModifierDisplayNames.put(ILcdAPP6ACoded.sCommonIdentifier, "Common identifier");
    sAPP6ModifierDisplayNames.put(ILcdAPP6ACoded.sHeadquartersElement, "Headquarters element");
    sAPP6ModifierDisplayNames.put(ILcdAPP6ACoded.sInstallationComposition, "Installation composition");
    sAPP6ModifierDisplayNames.put(ILcdAPP6ACoded.sPositionAndMovement, "Position and movement");
    sAPP6ModifierDisplayNames.put(ILcdAPP6ACoded.sTrackNumber, "Track number");
    sAPP6ModifierDisplayNames.put(ILcdAPP6ACoded.sName, "Name");
  }

  static {
    s2525Types = new HashMap<>();
    s2525Types.put(TLcdEditableMS2525bObject.TextModifierType.STRING_ANY, TextModifierType.STRING_ANY);
    s2525Types.put(TLcdEditableMS2525bObject.TextModifierType.STRING_POSITIVE_INTEGER, TextModifierType.STRING_POSITIVE_INTEGER);
    s2525Types.put(TLcdEditableMS2525bObject.TextModifierType.STRING_BOOLEAN, TextModifierType.STRING_BOOLEAN);
    s2525Types.put(TLcdEditableMS2525bObject.TextModifierType.STRING_ENUM, TextModifierType.STRING_ENUM);
    s2525Types.put(TLcdEditableMS2525bObject.TextModifierType.STRING_DATE_TIME, TextModifierType.STRING_DATE_TIME);
  }

  static {
    sAPP6Types = new HashMap<>();
    sAPP6Types.put(TLcdEditableAPP6AObject.TextModifierType.STRING_ANY, TextModifierType.STRING_ANY);
    sAPP6Types.put(TLcdEditableAPP6AObject.TextModifierType.STRING_POSITIVE_INTEGER, TextModifierType.STRING_POSITIVE_INTEGER);
    sAPP6Types.put(TLcdEditableAPP6AObject.TextModifierType.STRING_BOOLEAN, TextModifierType.STRING_BOOLEAN);
    sAPP6Types.put(TLcdEditableAPP6AObject.TextModifierType.STRING_ENUM, TextModifierType.STRING_ENUM);
    sAPP6Types.put(TLcdEditableAPP6AObject.TextModifierType.STRING_DATE_TIME, TextModifierType.STRING_DATE_TIME);
  }

}
  

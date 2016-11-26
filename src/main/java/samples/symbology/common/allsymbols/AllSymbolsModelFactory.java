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
package samples.symbology.common.allsymbols;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelDescriptor;
import com.luciad.model.ILcdModelReference;
import com.luciad.model.TLcd2DBoundsIndexedModel;
import com.luciad.model.TLcdModelDescriptor;
import com.luciad.reference.TLcdGeodeticReference;
import com.luciad.shape.ILcdText;
import com.luciad.shape.shape2D.TLcdXYText;
import com.luciad.symbology.app6a.model.ELcdAPP6Standard;
import com.luciad.symbology.app6a.model.TLcdAPP6ANode;
import com.luciad.symbology.milstd2525b.model.ELcdMS2525Standard;
import com.luciad.symbology.milstd2525b.model.TLcdMS2525bNode;

import samples.symbology.common.EMilitarySymbology;
import samples.symbology.common.app6.APP6ModelDescriptor;
import samples.symbology.common.app6.StyledEditableAPP6Object;
import samples.symbology.common.ms2525.MS2525ModelDescriptor;
import samples.symbology.common.ms2525.StyledEditableMS2525Object;
import samples.symbology.common.util.MilitarySymbolFacade;

public class AllSymbolsModelFactory {

  public enum Filter {
    ICONS,
    TACTICAL_GRAPHICS
  }

  private final SymbologyLayout fLayout;
  private final Filter fFilter;
  private final Random fRandom;

  public AllSymbolsModelFactory(SymbologyLayout aLayout, Filter aFilter) {
    fLayout = aLayout;
    fFilter = aFilter;
    fRandom = new Random(11223344);
  }

  public ILcdModel[] createModel(EMilitarySymbology aSymbology) {
    TLcd2DBoundsIndexedModel symbolModel = new TLcd2DBoundsIndexedModel();

    // Model reference and descriptor
    symbolModel.setModelReference(new TLcdGeodeticReference());
    symbolModel.setModelDescriptor(createModelDescriptor(aSymbology));

    TLcd2DBoundsIndexedModel labelModel = new TLcd2DBoundsIndexedModel();

    // Model reference and descriptor
    labelModel.setModelReference(new TLcdGeodeticReference());
    labelModel.setModelDescriptor(new TLcdModelDescriptor(null, "Symbol Description", aSymbology.toString() + " Description"));

    addSymbols(symbolModel, labelModel, aSymbology);

    return new ILcdModel[]{symbolModel, labelModel};
  }

  private static ILcdModelDescriptor createModelDescriptor(EMilitarySymbology aSymbology) {
    if (aSymbology.getStandard() instanceof ELcdAPP6Standard) {
      return new APP6ModelDescriptor(null, "APP-6", aSymbology.toString(), null, aSymbology);
    } else if (aSymbology.getStandard() instanceof ELcdMS2525Standard) {
      return new MS2525ModelDescriptor(null, "MS2525", aSymbology.toString(), null, aSymbology);
    } else {
      throw new IllegalArgumentException("Unknown standard: " + aSymbology);
    }
  }

  private void addSymbols(ILcdModel aSymbolModel, ILcdModel aLabelModel, EMilitarySymbology aSymbology) {
    if (aSymbology.getStandard() instanceof ELcdAPP6Standard) {
      ELcdAPP6Standard standard = (ELcdAPP6Standard) aSymbology.getStandard();
      TLcdAPP6ANode node = TLcdAPP6ANode.getRoot(standard);
      addAPP6Symbols(aSymbolModel, aLabelModel, node);
    } else if (aSymbology.getStandard() instanceof ELcdMS2525Standard) {
      ELcdMS2525Standard standard = (ELcdMS2525Standard) aSymbology.getStandard();
      TLcdMS2525bNode node = TLcdMS2525bNode.getRoot(standard);
      addMS2525Symbols(aSymbolModel, aLabelModel, node);
    } else {
      throw new IllegalArgumentException("Unknown standard: " + aSymbology);
    }
  }

  private void addAPP6Symbols(ILcdModel aSymbolModel, ILcdModel aLabelModel, TLcdAPP6ANode aParent) {

    if (aParent == null || aParent.getChildren() == null || aParent.getChildren().isEmpty()) {
      return;
    }

    for (TLcdAPP6ANode child : aParent.getChildren()) {
      if (!child.isFolderOnly()) {
        double cx = fLayout.getCenterX();
        double cy = fLayout.getCenterY();
        double r = fLayout.getSize();
        StyledEditableAPP6Object object = createAPP6Object(aSymbolModel.getModelReference(), cx, cy, r, child, fRandom);
        if (child.getCodeMask() != null && shouldAddSymbol(object, fFilter)) {
          fLayout.nextObject();
          aSymbolModel.addElement(object, ILcdModel.NO_EVENT);

          String textString = child.getCodeMask() + "\n \n" + child.getName();
          aLabelModel.addElement(new TLcdXYText(textString, cx, cy - r, r / 15, r / 8, ILcdText.ALIGNMENT_CENTER, ILcdText.ALIGNMENT_BOTTOM, 0), ILcdModel.NO_EVENT);
        }
      }
      addAPP6Symbols(aSymbolModel, aLabelModel, child);
    }
  }

  public static StyledEditableAPP6Object createAPP6Object(ILcdModelReference aModelReference, double aCenterX, double aCenterY, double aSize, TLcdAPP6ANode aNode, Random aRandom) {
    StyledEditableAPP6Object symbol = new StyledEditableAPP6Object(aNode.getCodeMask(), aNode.getStandard());

    // Use a suitable geometry
    aNode.applyTemplateShape(aModelReference, aCenterX, aCenterY, aSize, symbol);

    // Add features/labels
    addTextModifiers(symbol);

    // Put random SIDC modifiers
    putRandomSIDCModifiers(symbol, aRandom);

    return symbol;
  }

  private void addMS2525Symbols(ILcdModel aSymbolModel, ILcdModel aLabelModel, TLcdMS2525bNode aParent) {
    if (aParent.getChildren().isEmpty()) {
      return;
    }

    for (TLcdMS2525bNode child : aParent.getChildren()) {
      if (!child.isFolderOnly()) {
        double cx = fLayout.getCenterX();
        double cy = fLayout.getCenterY();
        double r = fLayout.getSize();
        StyledEditableMS2525Object object = createMS2525Object(aSymbolModel.getModelReference(), cx, cy, r, child, fRandom);
        if (child.getCodeMask() != null && shouldAddSymbol(object, fFilter)) {
          fLayout.nextObject();
          aSymbolModel.addElement(object, ILcdModel.NO_EVENT);

          String textString = child.getCodeMask() + "\n \n" + child.getName();
          aLabelModel.addElement(new TLcdXYText(textString, cx, cy - r, r / 15, r / 8, ILcdText.ALIGNMENT_CENTER, ILcdText.ALIGNMENT_BOTTOM, 0), ILcdModel.NO_EVENT);
        }
      }
      addMS2525Symbols(aSymbolModel, aLabelModel, child);
    }
  }

  private StyledEditableMS2525Object createMS2525Object(ILcdModelReference aModelReference, double aCenterX, double aCenterY, double aSize, TLcdMS2525bNode aNode, Random aRandom) {
    StyledEditableMS2525Object symbol = new StyledEditableMS2525Object(aNode.getCodeMask(), aNode.getStandard());

    // Use a suitable geometry
    aNode.applyTemplateShape(aModelReference, aCenterX, aCenterY, aSize, symbol);

    // Add features/labels
    addTextModifiers(symbol);

    // Put random SIDC modifiers
    putRandomSIDCModifiers(symbol, aRandom);

    return symbol;
  }

  private static boolean shouldAddSymbol(Object aSymbol, Filter aFilter) {
    switch (aFilter) {
    case ICONS:
      return !MilitarySymbolFacade.isLine(aSymbol);
    case TACTICAL_GRAPHICS:
      return MilitarySymbolFacade.isLine(aSymbol);
    default:
      throw new IllegalArgumentException("Unknown filter: " + aFilter);
    }
  }

  private static void addTextModifiers(Object aSymbol) {
    for (TextModifier textModifier : TextModifier.values()) {
      try {
        // pretty much all icons support movement direction, so skip these
        if (MilitarySymbolFacade.isLine(aSymbol) || !textModifier.equals(TextModifier.sMovementDirection)) {
          textModifier.putTextModifier(aSymbol);
        }
      } catch (NullPointerException | IllegalArgumentException e) {
        throw new RuntimeException(e);
      }
    }
  }

  private static void putRandomSIDCModifiers(Object aSymbol, Random aRandom) {
    for (MilitarySymbolFacade.Modifier modifier : MilitarySymbolFacade.getPossibleSIDCModifiers(aSymbol)) {
      putRandomModifier(aSymbol, modifier, aRandom);
    }
  }

  private static void putRandomModifier(Object aSymbol, MilitarySymbolFacade.Modifier aModifier, Random aRandom) {
    if (aModifier == null) {
      return;
    }
    List<String> possibleValues = new ArrayList<>(aModifier.getPossibleValues());
    if (!possibleValues.isEmpty()) {
      int index = aRandom.nextInt(possibleValues.size());
      MilitarySymbolFacade.setModifierValue(aSymbol, aModifier, possibleValues.get(index));
    }
  }
}

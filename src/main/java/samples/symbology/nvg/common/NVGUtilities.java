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
package samples.symbology.nvg.common;

import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JToolBar;

import com.luciad.format.nvg.nvg20.model.TLcdNVG20BindingsUtil;
import com.luciad.format.nvg.nvg20.model.TLcdNVG20Content;
import com.luciad.format.nvg.nvg20.model.TLcdNVG20SymbolizedContent;
import com.luciad.gui.ILcdIcon;
import com.luciad.gui.TLcdIconFactory;
import com.luciad.symbology.app6a.model.ELcdAPP6Standard;
import com.luciad.symbology.app6a.model.ILcdAPP6ACoded;
import com.luciad.symbology.milstd2525b.model.ELcdMS2525Standard;
import com.luciad.symbology.milstd2525b.model.ILcdMS2525bCoded;

import samples.gxy.common.toolbar.AToolBar;
import samples.symbology.common.EMilitarySymbology;
import samples.symbology.nvg.gui.ANVGCreateContentAction;

/**
 * Common static methods and properties
 *
 */
public class NVGUtilities {

  /**
   * Icons for NVG Actions
   */
  private static final ILcdIcon[] sNVG_ACTION_ICONS = new ILcdIcon[]{
      TLcdIconFactory.create(TLcdIconFactory.DRAW_ELLIPTICAL_ARC_ICON, TLcdIconFactory.getDefaultTheme(), TLcdIconFactory.Size.SIZE_32),
      TLcdIconFactory.create(TLcdIconFactory.DRAW_ARC_BAND_ICON, TLcdIconFactory.getDefaultTheme(), TLcdIconFactory.Size.SIZE_32),
      TLcdIconFactory.create(TLcdIconFactory.ARROW_ICON, TLcdIconFactory.getDefaultTheme(), TLcdIconFactory.Size.SIZE_32),
      TLcdIconFactory.create(TLcdIconFactory.DRAW_CIRCLE_BY_CENTER_ICON, TLcdIconFactory.getDefaultTheme(), TLcdIconFactory.Size.SIZE_32),
      TLcdIconFactory.create(TLcdIconFactory.DRAW_BUFFER_ICON, TLcdIconFactory.getDefaultTheme(), TLcdIconFactory.Size.SIZE_32),
      TLcdIconFactory.create(TLcdIconFactory.DRAW_ELLIPSE_ICON, TLcdIconFactory.getDefaultTheme(), TLcdIconFactory.Size.SIZE_32),
      TLcdIconFactory.create(TLcdIconFactory.DRAW_POLYPOINT_ICON, TLcdIconFactory.getDefaultTheme(), TLcdIconFactory.Size.SIZE_32),
      TLcdIconFactory.create(TLcdIconFactory.DRAW_GEO_BUFFER_ICON, TLcdIconFactory.getDefaultTheme(), TLcdIconFactory.Size.SIZE_32),
      TLcdIconFactory.create(TLcdIconFactory.DRAW_POINT_ICON, TLcdIconFactory.getDefaultTheme(), TLcdIconFactory.Size.SIZE_32),
      TLcdIconFactory.create(TLcdIconFactory.DRAW_POLYGON_ICON, TLcdIconFactory.getDefaultTheme(), TLcdIconFactory.Size.SIZE_32),
      TLcdIconFactory.create(TLcdIconFactory.DRAW_POLYLINE_ICON, TLcdIconFactory.getDefaultTheme(), TLcdIconFactory.Size.SIZE_32),
      TLcdIconFactory.create(TLcdIconFactory.DRAW_RECTANGLE_ICON, TLcdIconFactory.getDefaultTheme(), TLcdIconFactory.Size.SIZE_32),
      TLcdIconFactory.create(TLcdIconFactory.DRAW_TEXT_ICON, TLcdIconFactory.getDefaultTheme(), TLcdIconFactory.Size.SIZE_32)
  };

  private NVGUtilities() {
    //no instances
    throw new AssertionError("No instances allowed for NVGUtilities.");
  }

  /**
   * Sets symbol of given <code>TLcdNVG20SymbolizedContent</code> with given symbology
   * @param aContentSFCT <code>TLcdNVG20SymbolizedContent</code> instance whose symbol will be set
   * @param aSymbology symbology of the symbol to be used
   * @param aSymbol symbol to be used
   * @return A <code>TLcdNVG20Content</code> object that represents the given symbol
   * @throws IllegalArgumentException if the symbology is not one of <code>MILSTD_2525B, MILSTD_2525C, APP6A, APP6B</code>
   */
  public static TLcdNVG20SymbolizedContent createNVG20Content(TLcdNVG20SymbolizedContent aContentSFCT, EMilitarySymbology aSymbology, Object aSymbol) {
    if (aSymbology == EMilitarySymbology.MILSTD_2525B || aSymbology == EMilitarySymbology.MILSTD_2525C) {
      aContentSFCT.setSymbolFromMS2525((ILcdMS2525bCoded) aSymbol);
    } else if (aSymbology.getStandard() instanceof ELcdAPP6Standard) {
      aContentSFCT.setSymbolFromAPP6((ILcdAPP6ACoded) aSymbol);
    } else {
      throw new IllegalArgumentException(aSymbology + " is not a supported symbol class");
    }
    return aContentSFCT;
  }

  /**
   * @return a <code>JComboBox</code> which lets user to select a <code>EMilitarySymbology</code>
   */
  public static JComboBox<EMilitarySymbology> createSymbologySelectionBox() {
    JComboBox<EMilitarySymbology> symbologySelection = new JComboBox<>();
    symbologySelection.addItem(EMilitarySymbology.APP6A);
    symbologySelection.addItem(EMilitarySymbology.APP6B);
    symbologySelection.addItem(EMilitarySymbology.APP6C);
    symbologySelection.addItem(EMilitarySymbology.MILSTD_2525B);
    symbologySelection.addItem(EMilitarySymbology.MILSTD_2525C);
    return symbologySelection;
  }

  /**
   * Creates all available ANVGCreateContentActions and puts them in provided ToolBar
   * @param aToolBar ToolBar which will be filled with actions
   * @param aBuilder specific builder which contains common features for actions
   * @throws IllegalArgumentException if aToolBar is not an instance of either <code>samples.lightspeed.common.AToolBar</code> or <code>samples.gxy.common.toolbar.AToolBar</code>
   */
  public static void populateNVGActions(JToolBar aToolBar, ANVGCreateContentAction.Builder aBuilder) {
    int i = 0;
    for (TLcdNVG20Content.ShapeType shape : TLcdNVG20Content.ShapeType.values()) {
      aBuilder.content(shape.createShapeInstance())
              .geometryName(shape)
              .icon(sNVG_ACTION_ICONS[i++]);
      if (aToolBar instanceof AToolBar) {
        ((AToolBar) aToolBar).addAction(aBuilder.build());
      } else if ((aToolBar instanceof samples.lightspeed.common.AToolBar)) {
        ((samples.lightspeed.common.AToolBar) aToolBar).addAction(aBuilder.build());
      } else {
        throw new IllegalArgumentException(aToolBar + " should be either samples.lightspeed.common.AToolBar or samples.gxy.common.toolbar.AToolBar");
      }
    }
  }

  /**
   * creates and return a <code>TLcdNVG20SymbolizedContent</code> instance for the given SIDC
   * @param aSIDC SIDC string of a symbol
   * @param aSymbology Military Symbology class of the symbol
   * @return default <code>TLcdNVG20SymbolizedContent</code> instance for the symbol.
   * null if there is no supporting geometry for the symbol.
   */
  public static TLcdNVG20SymbolizedContent getDefaultGeometry(String aSIDC, EMilitarySymbology aSymbology) {
    List<TLcdNVG20Content.ShapeType> geometries;
    if (aSymbology.getStandard() instanceof ELcdAPP6Standard) {
      geometries = TLcdNVG20BindingsUtil.getShapesForHierarchy((ELcdAPP6Standard) aSymbology.getStandard(), aSIDC);
    } else {
      geometries = TLcdNVG20BindingsUtil.getShapesForHierarchy((ELcdMS2525Standard) aSymbology.getStandard(), aSIDC);
    }
    if (geometries != null && !geometries.isEmpty()) {
      return (TLcdNVG20SymbolizedContent) geometries.get(0).createShapeInstance();
    } else {
      throw new RuntimeException("Can't find geometry for: " + aSIDC + " " + aSymbology);
    }
  }
}

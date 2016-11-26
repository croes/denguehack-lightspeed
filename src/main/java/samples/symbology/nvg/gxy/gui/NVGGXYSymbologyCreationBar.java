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
package samples.symbology.nvg.gxy.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComboBox;
import javax.swing.JToolBar;

import com.luciad.format.nvg.nvg20.model.TLcdNVG20Content;
import com.luciad.format.nvg.nvg20.model.TLcdNVG20SymbolizedContent;
import com.luciad.gui.ILcdUndoableListener;
import samples.decoder.nvg.NVGSymbolFilter;
import samples.symbology.common.EMilitarySymbology;
import com.luciad.view.gxy.ILcdGXYLayer;
import com.luciad.view.gxy.ILcdGXYLayerSubsetList;
import com.luciad.view.gxy.ILcdGXYView;

import samples.gxy.editing.GXYCreateAction;
import samples.symbology.gxy.GXYSymbologyCreationBar;
import samples.symbology.nvg.common.NVGUtilities;
import samples.symbology.nvg.gxy.NVGGXYSymbolNewControllerModelFactory;

/**
 * Lets the user create and insert a NVG military symbol by typing in a search field or
 * by clicking on a button that opens a symbol hierarchy tree.
 */
public class NVGGXYSymbologyCreationBar extends GXYSymbologyCreationBar {

  private final ILcdGXYLayer fLayer;

  public NVGGXYSymbologyCreationBar(ILcdGXYView aView, ILcdGXYLayer aLayer, ILcdUndoableListener aUndoManager, ILcdGXYLayerSubsetList aSnappables) {
    super(aView, aUndoManager, aSnappables, false);
    fLayer = aLayer;

    JComboBox symbologySelector = NVGUtilities.createSymbologySelectionBox();
    symbologySelector.addActionListener(new SymbologyChangeListener());
    add(symbologySelector, 0);
    add(new JToolBar.Separator(), 1);

    //init
    NVGSymbolFilter filter = new NVGSymbolFilter();
    EMilitarySymbology selectedItem = (EMilitarySymbology) symbologySelector.getSelectedItem();
    filter.setSymbology(selectedItem);
    filter.setSelectedGeometryType(TLcdNVG20Content.ShapeType.Point);
    setSymbology(selectedItem, null);
  }

  @Override
  protected void symbolSelected(final String aSIDC) {
    TLcdNVG20SymbolizedContent content = NVGUtilities.getDefaultGeometry(aSIDC, getSymbology());
    new GXYCreateAction(
        NVGGXYSymbolNewControllerModelFactory.newInstanceForHierarchy(fLayer, content, aSIDC, getSymbology()),
        getView(),
        getUndoManager(),
        getSnappables()
    ).actionPerformed(null);
  }

  private class SymbologyChangeListener implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
      JComboBox source = (JComboBox) e.getSource();
      EMilitarySymbology selectedItem = (EMilitarySymbology) source.getSelectedItem();
      setSymbology(selectedItem, null);
    }
  }
}

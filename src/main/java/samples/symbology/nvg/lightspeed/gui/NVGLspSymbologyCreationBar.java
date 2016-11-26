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
package samples.symbology.nvg.lightspeed.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JToolBar;

import com.luciad.gui.ILcdUndoableListener;
import samples.symbology.common.EMilitarySymbology;
import samples.symbology.common.gui.SymbolSelectionBar;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.layer.ILspInteractivePaintableLayer;

import samples.lightspeed.editing.LspCreateAction;
import samples.symbology.nvg.common.NVGUtilities;
import samples.symbology.nvg.lightspeed.NVGLspSymbolCreateControllerModelFactory;

/**
 * Lets the user create and insert a NVG military symbol by typing in a search field or
 * by clicking on a button that opens a symbol hierarchy tree.
 */
public class NVGLspSymbologyCreationBar extends SymbolSelectionBar {

  private final ILspInteractivePaintableLayer fLayer;
  private final List<ILspView> fViews;
  private final ILcdUndoableListener fUndoManager;

  public NVGLspSymbologyCreationBar(ILspView aView, ILcdUndoableListener aUndoManager, ILspInteractivePaintableLayer aLayer) {
    this(Collections.singletonList(aView), aUndoManager, aLayer);
  }

  public NVGLspSymbologyCreationBar(List<ILspView> aViews, ILcdUndoableListener aUndoManager, ILspInteractivePaintableLayer aLayer) {
    super(EMilitarySymbology.APP6A, null);
    fViews = new ArrayList<>(aViews);
    fUndoManager = aUndoManager;
    fLayer = aLayer;

    JComboBox symbologySelector = NVGUtilities.createSymbologySelectionBox();
    symbologySelector.addActionListener(new SymbologyChangeListener());
    add(symbologySelector, 0);
    add(new JToolBar.Separator(), 1);
    setSearchHint("Type name to create symbol");
  }

  private List<ILspView> getViews() {
    return fViews;
  }

  private ILcdUndoableListener getUndoManager() {
    return fUndoManager;
  }

  @Override
  protected void symbolSelected(String aHierarchyMask) {
    new LspCreateAction(
        NVGLspSymbolCreateControllerModelFactory.newInstanceForHierarchy(NVGUtilities.getDefaultGeometry(aHierarchyMask, getSymbology()), fLayer, aHierarchyMask,
                                                                         getSymbology()
        ),
        getViews(),
        getUndoManager()
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

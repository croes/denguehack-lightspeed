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
package samples.symbology.common.gui;

import java.awt.Font;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import samples.common.search.SearchFieldWithPopup;
import samples.symbology.common.EMilitarySymbology;
import samples.symbology.common.util.MilitarySymbolFacade;
import com.luciad.symbology.app6a.model.ELcdAPP6Standard;
import com.luciad.symbology.app6a.model.TLcdAPP6ANode;
import com.luciad.symbology.app6a.model.TLcdEditableAPP6AObject;
import com.luciad.symbology.milstd2525b.model.ELcdMS2525Standard;
import com.luciad.symbology.milstd2525b.model.TLcdEditableMS2525bObject;
import com.luciad.symbology.milstd2525b.model.TLcdMS2525bNode;
import com.luciad.util.ILcdFilter;

/**
 * Pop-up search field for all military symbols for the given standard.
 *
 * Override {@link #symbolSelected} to determine what happens when the user selects a symbol.
 * For example, a new instance of the symbol could be created and placed on the map.
 */
public abstract class SymbologySearchField extends SearchFieldWithPopup {

  private final Map<String, String> fSearchStringToSIDC;
  private EMilitarySymbology fSymbology;
  private ILcdFilter<String> fSIDCFilter;

  public SymbologySearchField(EMilitarySymbology aSymbology) {
    fSearchStringToSIDC = new HashMap<>();
    setSymbology(aSymbology);
    // set up a monospaced font for the symbology identifier codes
    setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
    getSearchService().setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
    setWidthFactor(2.0);
  }

  @Override
  protected final void valueSelected(String aValue) {
    symbolSelected(fSearchStringToSIDC.get(aValue));
    setSearchableContent(getSearchService().getSearchContent());
  }

  protected abstract void symbolSelected(String aSIDC);

  public void setSIDCFilter(ILcdFilter<String> aSIDCFilter) {
    fSIDCFilter = aSIDCFilter;
    recalculateContent();
  }

  public EMilitarySymbology getSymbology() {
    return fSymbology;
  }

  public void setSymbology(EMilitarySymbology aSymbology) {
    if (fSymbology == aSymbology) {
      return;
    }
    fSymbology = aSymbology;
    recalculateContent();
  }

  private void recalculateContent() {
    fSearchStringToSIDC.clear();
    Object standard = fSymbology == null ? null : fSymbology.getStandard();
    if (standard instanceof ELcdMS2525Standard) {
      TLcdMS2525bNode node = TLcdMS2525bNode.getRoot((ELcdMS2525Standard) standard);
      dumpNode(node, fSearchStringToSIDC);
    } else if (standard instanceof ELcdAPP6Standard) {
      TLcdAPP6ANode node = TLcdAPP6ANode.getRoot((ELcdAPP6Standard) standard);
      dumpNode(node, fSearchStringToSIDC);
    } else if (standard != null) {
      throw new IllegalArgumentException("Unsupported standard " + fSymbology);
    }
    Set<String> searchStrings = fSearchStringToSIDC.keySet();
    List<String> sortedSearchStrings = new ArrayList<>(searchStrings);
    Collections.sort(sortedSearchStrings);
    setSearchableContent(sortedSearchStrings.toArray(new String[sortedSearchStrings.size()]));
  }

  private void dumpNode(TLcdMS2525bNode aNode, Map<String, String> aSearchStringToSIDC) {
    if (!aNode.isFolderOnly()) {
      String displayName = MilitarySymbolFacade.getDisplayName(new TLcdEditableMS2525bObject(aNode.getCodeMask(), aNode.getStandard()));
      TLcdMS2525bNode parent = aNode.getParent();
      // always try to add one parent
      if (parent != null) {
        displayName = MilitarySymbolFacade.addParentName(displayName, parent.getName());
        parent = parent.getParent();
      }
      // add more context if needed and possible
      while (aSearchStringToSIDC.containsKey(displayName) && parent != null) {
        displayName = MilitarySymbolFacade.addParentName(displayName, parent.getName());
        parent = parent.getParent();
      }
      if (accept(aNode.getCodeMask())) {
        aSearchStringToSIDC.put(displayName, aNode.getCodeMask());
      }
    }
    for (TLcdMS2525bNode node : aNode.getChildren()) {
      dumpNode(node, aSearchStringToSIDC);
    }
  }

  private void dumpNode(TLcdAPP6ANode aNode, Map<String, String> aSearchStringToSIDC) {
    if (!aNode.isFolderOnly()) {
      String displayName = MilitarySymbolFacade.getDisplayName(new TLcdEditableAPP6AObject(aNode.getCodeMask(), aNode.getStandard()));
      TLcdAPP6ANode parent = aNode.getParent();
      // always try to add one parent
      if (parent != null) {
        displayName = MilitarySymbolFacade.addParentName(displayName, parent.getName());
        parent = parent.getParent();
      }
      // add more context if needed and possible
      while (aSearchStringToSIDC.containsKey(displayName) && parent != null) {
        displayName = MilitarySymbolFacade.addParentName(displayName, parent.getName());
        parent = parent.getParent();
      }
      if (accept(aNode.getCodeMask())) {
        aSearchStringToSIDC.put(displayName, aNode.getCodeMask());
      }
    }
    for (TLcdAPP6ANode node : aNode.getChildren()) {
      dumpNode(node, aSearchStringToSIDC);
    }
  }

  private boolean accept(String aCode) {
    return fSIDCFilter == null || fSIDCFilter.accept(aCode);
  }

}

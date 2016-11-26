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
package samples.decoder.nvg;

import java.util.List;

import com.luciad.format.nvg.nvg20.model.TLcdNVG20BindingsUtil;
import com.luciad.format.nvg.nvg20.model.TLcdNVG20Content;
import samples.symbology.common.EMilitarySymbology;
import com.luciad.symbology.app6a.model.ELcdAPP6Standard;
import com.luciad.symbology.milstd2525b.model.ELcdMS2525Standard;
import com.luciad.util.ILcdFilter;

/**
 * Filters the symbols depending on the selected NVG Geometry.
 * Symbol-Geometry mappings are defined in the related properties
 * file per standard
 */
public class NVGSymbolFilter implements ILcdFilter<String> {

  private EMilitarySymbology fSymbology;
  private TLcdNVG20Content.ShapeType fSelectedGeometryType;

  public NVGSymbolFilter() {
  }

  public void setSymbology(EMilitarySymbology aSymbology) {
    fSymbology = aSymbology;
  }

  public void setSelectedGeometryType(TLcdNVG20Content.ShapeType aShapeType) {
    fSelectedGeometryType = aShapeType;
  }

  @Override
  public boolean accept(String aObject) {
    if (fSelectedGeometryType == null) {
      return true;
    }
    if (aObject == null) {
      return false;
    }
    List<TLcdNVG20Content.ShapeType> geometries;
    if (fSymbology.getStandard() instanceof ELcdAPP6Standard) {
      geometries = TLcdNVG20BindingsUtil.getShapesForHierarchy((ELcdAPP6Standard) fSymbology.getStandard(), aObject);
    } else {
      geometries = TLcdNVG20BindingsUtil.getShapesForHierarchy((ELcdMS2525Standard) fSymbology.getStandard(), aObject);
    }
    return geometries.contains(fSelectedGeometryType);
  }
}

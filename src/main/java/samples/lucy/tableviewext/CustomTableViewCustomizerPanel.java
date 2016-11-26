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
package samples.lucy.tableviewext;

import java.awt.Color;

import samples.lucy.tableview.ITableViewLogic;
import samples.lucy.tableview.TableViewCustomizerPanel;
import com.luciad.lucy.ILcyLucyEnv;
import com.luciad.lucy.util.context.TLcyModelContext;
import com.luciad.util.ILcdFilter;

/**
 * Extension of <code>TableViewCustomizerPanel</code> which allows to set the color
 *
 */
final class CustomTableViewCustomizerPanel extends TableViewCustomizerPanel {
  private static final Color[] COLORS = new Color[]{Color.RED, Color.BLUE, Color.BLACK, Color.YELLOW};
  private int fColorIndex = 0;

  CustomTableViewCustomizerPanel(ILcdFilter aModelContextFilter,
                                 String aName,
                                 ILcyLucyEnv aLucyEnv,
                                 ITableViewLogic aTableViewLogic,
                                 TLcyModelContext aModelContext) {
    super(aModelContextFilter, aName, aLucyEnv, aTableViewLogic, aModelContext);
  }

  int getColorIndex() {
    return fColorIndex;
  }

  void setColorIndex(int aColorIndex) {
    if (aColorIndex != fColorIndex) {
      int old = fColorIndex;
      fColorIndex = aColorIndex % COLORS.length;
      firePropertyChange("colorIndex", old, fColorIndex);
    }
  }

  Color getColor() {
    return COLORS[getColorIndex()];
  }
}

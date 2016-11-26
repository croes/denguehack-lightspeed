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
package samples.gxy.printing;

import java.awt.Component;
import java.util.Collections;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.luciad.gui.swing.TLcdOverlayLayout;
import com.luciad.gui.swing.TLcdRotatingIcon;
import com.luciad.gui.swing.TLcdSWIcon;
import com.luciad.util.TLcdPair;
import com.luciad.view.gxy.ILcdGXYView;
import com.luciad.view.gxy.swing.TLcdGXYViewComponentPrintable;

import samples.common.action.APrintAction;
import samples.gxy.common.ModelViewUtil;

public class PrintAction extends APrintAction<ILcdGXYView> {

  public PrintAction(Component aParentComponent, ILcdGXYView aView) {
    super(aParentComponent, aView);
  }

  @Override
  protected TLcdGXYViewComponentPrintable createPrintable(ILcdGXYView aView) {
    return createGXYPrintable(aView);
  }

  public static TLcdGXYViewComponentPrintable createGXYPrintable(ILcdGXYView aView) {
    // Creates a representation of the view for printing.
    Component view = TLcdGXYViewComponentPrintable.createViewComponent(aView, null);
    // Build a custom printable compass.
    JComponent compass = createGXYCompass(aView);
    // Builds a page around it.
    JPanel page = createPage(view, Collections.singletonList(new TLcdPair<>(compass, TLcdOverlayLayout.Location.NORTH_EAST)));
    // Makes it printable.
    return new TLcdGXYViewComponentPrintable(page);
  }

  private static JComponent createGXYCompass(ILcdGXYView aView) {
    TLcdRotatingIcon compass = createRotatableCompassIcon();
    double compassDirection = new ModelViewUtil().viewAzimuth(aView, null);
    compass.rotate(Math.toRadians(compassDirection));
    return new JLabel(new TLcdSWIcon(compass));
  }
}

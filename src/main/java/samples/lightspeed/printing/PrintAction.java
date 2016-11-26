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
package samples.lightspeed.printing;

import static java.util.Collections.singletonList;

import static com.luciad.gui.swing.TLcdOverlayLayout.Location;

import java.awt.Component;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.luciad.gui.swing.TLcdRotatingIcon;
import com.luciad.gui.swing.TLcdSWIcon;
import com.luciad.util.TLcdPair;
import com.luciad.view.lightspeed.ALspAWTView;
import com.luciad.view.lightspeed.ILspAWTView;
import com.luciad.view.lightspeed.swing.TLspViewComponentPrintable;

import samples.common.action.APrintAction;
import samples.lightspeed.LspModelViewUtil;

public class PrintAction extends APrintAction<ILspAWTView> {

  public PrintAction(Component aParentComponent, ILspAWTView aView) {
    super(aParentComponent, aView);
  }

  @Override
  protected TLspViewComponentPrintable createPrintable(ILspAWTView aView) {
    return createLspPrintable(aView);
  }

  public static TLspViewComponentPrintable createLspPrintable(ILspAWTView aView) {
    // Creates a representation of the view for printing.
    Component viewComponent = TLspViewComponentPrintable.createViewComponent(aView);
    // Build a custom printable compass.
    JComponent compass = createCompass(aView);
    // Builds a page around it.
    JPanel printPage = createPage(viewComponent, singletonList(new TLcdPair<>(compass, Location.NORTH_EAST)));
    // Makes it printable.
    return new TLspViewComponentPrintable(printPage);
  }

  private static JComponent createCompass(ILspAWTView aView) {
    TLcdRotatingIcon compass = createRotatableCompassIcon();
    double compassDirection = new LspModelViewUtil().viewAzimuth(aView, aView.getViewXYZWorldTransformation(), null);
    compass.rotate(Math.toRadians(compassDirection));
    return new JLabel(new TLcdSWIcon(compass));
  }

  @Override
  protected void beforePrinting() {
    if (getView() instanceof ALspAWTView) {
      ((ALspAWTView) getView()).beginPrinting();
    }
  }

  @Override
  protected void afterPrinting() {
    if (getView() instanceof ALspAWTView) {
      ((ALspAWTView) getView()).endPrinting();
    }
  }
}

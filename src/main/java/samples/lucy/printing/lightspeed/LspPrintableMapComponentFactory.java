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
package samples.lucy.printing.lightspeed;

import static samples.lucy.printing.PrintableComponentFactorySupport.*;

import java.awt.Component;

import com.luciad.lucy.addons.print.lightspeed.TLcyLspPrintableMapComponentFactory;
import com.luciad.lucy.map.lightspeed.ILcyLspMapComponent;
import com.luciad.lucy.util.TLcyProperties;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.swing.TLspViewComponentPrintable;

/**
 * A custom <code>TLcyLspPrintableMapComponentFactory</code> that rearranges the main components
 * in a custom layout.
 */
public class LspPrintableMapComponentFactory extends TLcyLspPrintableMapComponentFactory {

  @Override
  protected Component createPrintableComponentContent(ILcyLspMapComponent aMapComponent, TLcyProperties aProperties) {
    Component titleText = getComponent(TITLE_TEXT_COMPONENT);
    Component headerText = getComponent(MODIFIABLE_HEADER_TEXT_COMPONENT);
    Component classification = getComponent(CLASSIFICATION_COMPONENT);
    Component legend = getComponent(LEGEND_COMPONENT);
    Component scaleLabel = getComponent(SCALE_LABEL_COMPONENT);
    Component scaleIcon = getComponent(SCALE_ICON_COMPONENT);
    Component overview = (Component) getView(OVERVIEW_VIEW);
    Component view = TLspViewComponentPrintable.createViewComponent((ILspView) getView(MAIN_VIEW));

    return createCompositePanel(
        classification,
        createTitlePanel(headerText, titleText),
        createOverviewPanel(overview),
        createViewPanel(view),
        createLegendPanel(legend, scaleLabel, scaleIcon)
                               );
  }
}

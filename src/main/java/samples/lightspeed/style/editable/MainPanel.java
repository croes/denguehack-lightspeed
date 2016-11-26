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
package samples.lightspeed.style.editable;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.Arrays;
import java.util.Enumeration;

import com.luciad.gui.ALcdAction;
import com.luciad.gui.ILcdAction;
import com.luciad.gui.TLcdIconFactory;
import com.luciad.util.ILcdSelectionListener;
import com.luciad.util.TLcdSelectionChangedEvent;
import com.luciad.view.lightspeed.ILspAWTView;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.style.ALspStyle;
import com.luciad.view.lightspeed.style.ILspWorldElevationStyle.ElevationMode;
import com.luciad.view.lightspeed.style.TLspFillStyle;
import com.luciad.view.lightspeed.style.TLspLineStyle;
import com.luciad.view.lightspeed.style.styler.TLspEditableStyler;

import samples.common.SampleData;
import samples.lightspeed.common.LightspeedSample;
import samples.lightspeed.common.LspDataUtil;
import samples.lightspeed.common.ToolBar;

/**
 * This sample is intended to illustrate the process of editing an object's style at runtime.
 * It creates a setup in which a <code>TLspEditableStyler</code> is used to edit the
 * styles of all the objects. To tweak the values of the provider at runtime, a widget
 * was implemented which provides a GUI for editing the most prominent style properties
 * (see <code>StyleEditor</code> and <code>StyleEditorModel</code>).
 * <p/>
 * Note that for simplicity, this sample focuses on editing area styles, but the concepts used
 * in this sample can be applied to editing other styles as well.
 */
public class MainPanel extends LightspeedSample {

  private TLspEditableStyler createStyler() {
    return new TLspEditableStyler(
        Arrays.<ALspStyle>asList(
            TLspFillStyle.newBuilder().color(new Color(83, 133, 81)).elevationMode(ElevationMode.OBJECT_DEPENDENT).build(),
            TLspLineStyle.newBuilder().color(new Color(237, 237, 237)).elevationMode(ElevationMode.OBJECT_DEPENDENT).build()
        )
    )
        ;
  }

  protected void addData() throws IOException {
    super.addData();
    TLspEditableStyler styler = createStyler();
    LspDataUtil.instance().model(SampleData.COUNTRIES).layer(new LayerFactory(styler)).label("Countries").addToView(getView()).fit();

    // Add button that activates style pop-up to toolbar
    final MyEditStyleAction action = new MyEditStyleAction(getView(), styler);
    action.setShortDescription("Edit the style of the selected object(s)");
    ToolBar[] tbs = getToolBars();
    for (ToolBar tb : tbs) {
      tb.addAction(action, ToolBar.FILE_GROUP);
    }

    makeActionOnlyEnabledIfObjectsSelected(action);
  }

  private void makeActionOnlyEnabledIfObjectsSelected(final ILcdAction action) {
    action.setEnabled(false);
    getView().addLayerSelectionListener(new ILcdSelectionListener() {
      @Override
      public void selectionChanged(TLcdSelectionChangedEvent aTLcdSelectionChangedEvent) {
        // check if anything is selected in the view
        ILspView view = getView();
        Enumeration<?> e = view.layers();
        boolean selected = false;
        while (e.hasMoreElements()) {
          Object object = e.nextElement();
          if (!(object instanceof ILspLayer)) {
            continue;
          }
          ILspLayer layer = (ILspLayer) object;
          if (layer.selectedObjects().hasMoreElements()) {
            selected = true;
            break;
          }
        }
        action.setEnabled(selected);
      }
    });
  }

  /**
   * Toolbar action that launches the style editor dialog.
   */
  private static class MyEditStyleAction extends ALcdAction {

    private TLspEditableStyler fStyler;
    private ILspAWTView fView;

    private MyEditStyleAction(ILspAWTView aView, TLspEditableStyler aStyler) {
      fView = aView;
      fStyler = aStyler;
      setIcon(TLcdIconFactory.create(TLcdIconFactory.EDIT_ICON));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      StyleEditor.editStyle(fView, fStyler);
    }
  }

  public static void main(final String[] aArgs) {
    startSample(MainPanel.class, "Editable style");
  }

}

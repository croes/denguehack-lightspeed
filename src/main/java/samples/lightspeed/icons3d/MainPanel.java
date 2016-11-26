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
package samples.lightspeed.icons3d;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import com.luciad.model.TLcdOpenAction;
import com.luciad.reference.TLcdGeodeticReference;
import com.luciad.shape.ILcdBounds;
import com.luciad.shape.shape2D.TLcdLonLatBounds;
import com.luciad.util.TLcdStringUtil;
import com.luciad.view.lightspeed.ILspAWTView;
import com.luciad.view.lightspeed.ILspView;

import samples.lightspeed.common.FitUtil;
import samples.lightspeed.common.LightspeedSample;
import samples.lightspeed.common.LspDataUtil;
import samples.lightspeed.common.ToolBar;

/**
 * This sample demonstrates how to visualize point data using 3D icons. Icons can
 * be loaded from files in either OpenFlight or WaveFront OBJ format.
 */
public class MainPanel extends LightspeedSample {

  private Styler fStyler;

  @Override
  protected ILspAWTView createView() {
    return super.createView(ILspView.ViewType.VIEW_3D);
  }

  @Override
  protected void createGUI() {
    super.createGUI();
    fStyler = new Styler();

    // Create the file open button
    final JFileChooser sFileChooser = new JFileChooser("");
    sFileChooser.setFileFilter(new FileFilter() {
      @Override
      public boolean accept(File f) {
        return f.isDirectory() ||
               TLcdStringUtil.endsWithIgnoreCase(f.getName(), ".flt")
               || TLcdStringUtil.endsWithIgnoreCase(f.getName(), ".obj")
               || TLcdStringUtil.endsWithIgnoreCase(f.getName(), ".dae");
      }

      @Override
      public String getDescription() {
        return "3D model files";
      }
    });

    TLcdOpenAction openAction = new TLcdOpenAction() {
      @Override
      public void actionPerformed(ActionEvent aEvent) {
        if (sFileChooser.showOpenDialog(MainPanel.this) == JFileChooser.APPROVE_OPTION) {
          String name = sFileChooser.getSelectedFile().getAbsolutePath();
          fStyler.setIcon(name);
          getView().invalidate(true, this, "Loaded icon");
        }
      }
    };
    openAction.setShortDescription("Load a 3D model in Collada, OpenFlight or WaveFront OBJ format for use as an icon");
    getToolBars()[0].addAction(openAction, ToolBar.FILE_GROUP);
  }

  @Override
  protected void addData() throws IOException {
    super.addData();
    LayerFactory layerFactory = new LayerFactory();
    layerFactory.setStyler(fStyler);

    LspDataUtil.instance().model(FlyCircleModelFactory.createPointModel()).layer(layerFactory).label("Planes").addToView(getView());

    // Fit on the points layer.
    final ILcdBounds bounds = new TLcdLonLatBounds(-8, -8, 16, 16);
    FitUtil.fitOnBounds(this, bounds, new TLcdGeodeticReference());
  }

  public static void main(final String[] aArgs) {
    startSample(MainPanel.class, "3D icons");
  }

}

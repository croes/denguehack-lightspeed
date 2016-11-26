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
package samples.opengl.touch;

import com.luciad.view.opengl.swing.navigationcontrols.TLcdGLNavigationControlsFactory;
import samples.opengl.common.Toolbar;
import samples.opengl.common.TouchToolBar;

import java.awt.BorderLayout;

/**
 * This Mainpanel contains a Toolbar with touchcontrollers instead of mouse controllers.
 */
public class MainPanel extends samples.opengl.geocentric.MainPanel {

  private TouchToolBar fToolBar;

  @Override
  protected void createGUI() {
    super.createGUI();
    fToolBar = new TouchToolBar( getCanvas(), this, false );
    add( fToolBar, BorderLayout.NORTH );
  }

  @Override
  protected Toolbar createToolbar() {
    return null;
  }

  @Override
  protected void addOverlayComponents() {
    getOverlayPanel().add( TLcdGLNavigationControlsFactory.createNavigationControls(
        getCanvas(), "images/gui/navigationcontrols/large/", true, null ) );
  }

  public TouchToolBar getTouchToolBar() {
    return fToolBar;
  }


}

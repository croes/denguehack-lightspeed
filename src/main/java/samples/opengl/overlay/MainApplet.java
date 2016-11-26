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
package samples.opengl.overlay;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JOptionPane;

import com.luciad.view.opengl.binding.TLcdGLBindingLog;
import com.luciad.view.opengl.binding.TLcdGLBindingSystemLog;

import samples.common.DefaultExceptionHandler;
import samples.common.LuciadFrame;
import samples.opengl.common.Abstract3DSample;

/**
 * This sample shows how you can use the TLcdGLMapOverlayPanel and the TLcdOverlayLayout to add swing
 * components over the view. You can check out the code in Abstract3DPanel to see how the TLcdGLMapOverlayPanel
 * is wrapped around the view.
 */
public class MainApplet extends Abstract3DSample {

  protected void createGUI() {
    MainPanel samplePanel = new MainPanel();

    // Create a swing window and put the 3D view inside it
    setLayout( new BorderLayout() );
    add( samplePanel, BorderLayout.CENTER );
  }

  public static void main( final String[] aArgs ) {
    EventQueue.invokeLater( new Runnable() {
      public void run() {
        try {
          new LuciadFrame(new MainApplet(), "Luciad3D Overlay Sample" );
        } catch ( UnsatisfiedLinkError e ) {
          JOptionPane.showMessageDialog( null, new String[] {DefaultExceptionHandler.UNSATISFIED_LINK_ERROR_MESSAGE} );
        }
      }
    } );
  }
}

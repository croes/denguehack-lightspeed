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
package samples.lightspeed.demo.framework.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;

import samples.common.SwingUtil;
import samples.lightspeed.demo.framework.application.Framework;

public class LightspeedDemoFrame extends JFrame {

  private final Dimension fWindowSize;
  private ApplicationPanel fContent;

  /**
   * Creates a new frame with the given content.
   *
   * @param aContent The content of the frame
   * @param aName The name of the frame
   * @param aWidth The width of the frame
   * @param aHeight The height of the frame
   */
  public LightspeedDemoFrame(ApplicationPanel aContent, String aName, int aWidth, int aHeight) {
    super(aName);
    fContent = aContent;
    setIconImages(SwingUtil.sLuciadFrameImage);
    fWindowSize = new Dimension(aWidth, aHeight);
    //On the Mac, users can always leave full screen. They can only re-enter full screen if
    //the frame is undecorated
    addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        Framework.destroyInstance();
        System.exit(0);
      }
    });
    setLayout(new BorderLayout());
    add(aContent, BorderLayout.CENTER);
    pack();
  }

  @Override
  public void dispose() {
    if (fContent != null) {
      fContent.removeAll();
      fContent.getView().destroy();
      fContent = null;
    }
  }

  @Override
  public Dimension getPreferredSize() {
    return fWindowSize;
  }

  @Override
  public Dimension getMinimumSize() {
    return fWindowSize;
  }
}

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
package samples.common;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.DisplayMode;
import java.awt.EventQueue;
import java.awt.GraphicsEnvironment;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;

/**
 * Frame with a Luciad icon to contain a sample.
 * On construction the frame will initialize and start the sample it contains and display itself
 * at the center of the screen. On closing the application will be shut down.
 */
public class LuciadFrame extends JFrame {

  private static final String WINDOW_TITLE_SUFFIX = " - LuciadLightspeed";

  private static final int sWidth = 1000;
  private static final int sHeight = 600;

  private final SamplePanel fContentPanel;

  public LuciadFrame(SamplePanel aContentPanel, String aName) {
    this(aContentPanel, aName, sWidth, sHeight);
  }

  @Override
  public void dispose() {
    fContentPanel.tearDown();
    super.dispose();
  }

  public LuciadFrame(SamplePanel aContentPanel, String aName, int aWidth, int aHeight) {
    fContentPanel = aContentPanel;
    fContentPanel.init();
    setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

    Dimension dimension = aContentPanel.getLayout().preferredLayoutSize(aContentPanel);
    // If the content is larger than the size we had in mind, prefer the content size.
    dimension.width = Math.max(dimension.width, aWidth);
    dimension.height = Math.max(dimension.height, aHeight);
    aContentPanel.setPreferredSize(dimension);

    setTitle(aName + WINDOW_TITLE_SUFFIX);
    setLayout(new BorderLayout());
    add(aContentPanel, BorderLayout.CENTER);
    pack();
    setIconImages(SwingUtil.sLuciadFrameImage);

    MacUtil.initMacApplication(SwingUtil.sLuciadFrameImage,
                               this);

    GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
    DisplayMode displayMode = ge.getDefaultScreenDevice().getDisplayMode();
    setLocation((displayMode.getWidth() - getSize().width) / 2, (displayMode.getHeight() - getSize().height) / 2);
    addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        // Allow other listeners to clean up.
        EventQueue.invokeLater(new Runnable() {
          @Override
          public void run() {
            System.exit(0);
          }
        });
      }
    });

    setVisible(true);
  }
}

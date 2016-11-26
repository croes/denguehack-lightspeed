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
package samples.lightspeed.internal.gxylsp;

import static com.luciad.gui.TLcdIconFactory.OPEN_ICON;
import static com.luciad.gui.TLcdIconFactory.create;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JSplitPane;
import javax.swing.WindowConstants;
import javax.swing.filechooser.FileFilter;

import com.luciad.gui.ALcdAction;
import com.luciad.gui.ILcdAction;
import com.luciad.gui.swing.TLcdSWAction;
import com.luciad.gui.swing.TLcdSWIcon;
import com.luciad.view.gxy.ILcdGXYView;
import com.luciad.view.lightspeed.ILspView;

import samples.common.SampleData;
import samples.common.formatsupport.OpenAction;
import samples.common.formatsupport.OpenSupport;
import samples.gxy.common.GXYSample;
import samples.gxy.common.layers.GXYDataUtil;
import samples.lightspeed.common.LightspeedSample;
import samples.lightspeed.common.ViewBoundsMediator;

/**
 * GUI tool which allows to view data in GXY and LSP side-by-side:
 * <ul>
 *   <li>Opens a window with GXY and LSP decoder sample side-by-side</li>
 *   <li>Adds a "open on both maps" action to both samples (highlighted in yellow)</li>
 *   <li>Keeps camera in sync both ways</li>
 *   <li></li>
 *   <li></li>
 * </ul>
 *
 * <b>Customization:</b>
 * <ul>
 *   <li>Override {@link #createGXYSample()} and {@link #createLspSample()} to plug in your own samples</li>
 *   <li>Override {@link #start()} to add GUI or functionality</li>
 *   <li>Use {@link #fFrame} to add GUI elements (use borderlayout N-E-S-W)</li>
 *   <li>Use {@link #openOnMaps(String)} to open data on both maps</li>
 *   <li></li>
 *   <li></li>
 * </ul>
 *
 * @since 2013.1
 */
public class MainPanel {

  protected final JFrame fFrame = new JFrame();
  protected final LightspeedSample fLspSample = createLspSample();
  protected final GXYSample fGXYSample = createGXYSample();

  protected LightspeedSample createLspSample() {
    return new samples.lightspeed.decoder.MainPanel();
  }

  protected GXYSample createGXYSample() {
    return new samples.gxy.decoder.MainPanel() {
      @Override
      protected void addData() throws IOException {
        GXYDataUtil.instance().model(SampleData.COUNTRIES).layer().label("Countries").selectable(false).addToView(getView());
      }
    };
  }

  public void start() {
    fGXYSample.init();
    fLspSample.init();

    fFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

    JSplitPane samplesPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT, fGXYSample, fLspSample);

    fFrame.add(samplesPanel, BorderLayout.CENTER);
    fFrame.pack();
    fFrame.setVisible(true);
    samplesPanel.setDividerLocation(0.4);
    keepBoundsInSync();
  }

  protected void addActionToToolbars(final ALcdAction aAction) {
    JButton button1 = new JButton(new TLcdSWIcon(aAction.getIcon()));
    button1.addActionListener(aAction);
    button1.setBackground(Color.yellow);
    button1.setToolTipText(aAction.getLongDescription());
    fGXYSample.getToolBars()[0].add(button1);

    JButton button2 = new JButton(new TLcdSWIcon(aAction.getIcon()));
    button2.addActionListener(aAction);
    button2.setBackground(Color.yellow);
    button2.setToolTipText(aAction.getLongDescription());
    fLspSample.getToolBars()[0].add(button2);
  }

  public void openOnMaps(String aSourceName) throws Exception {
    findGXYOpenSupport().openSource(aSourceName);
    findLspOpenSupport().openSource(aSourceName);
  }

  private void keepBoundsInSync() {
    ViewBoundsMediator.start(fLspSample.getView(), fGXYSample.getView());
  }

  protected OpenSupport findGXYOpenSupport() throws Exception {
    return findOpenSupportInToolBar(fGXYSample.getToolBars()[0]);
  }

  protected OpenSupport findLspOpenSupport() throws Exception {
    return findOpenSupportInToolBar(fLspSample.getToolBars()[0]);
  }

  protected final ILspView getLspView() {
    return fLspSample.getView();
  }

  protected final ILcdGXYView getGXYView() {
    return fGXYSample.getView();
  }

  private OpenSupport findOpenSupportInToolBar(Container aToolBar) throws Exception {
    Component[] components = aToolBar.getComponents();
    for (Component component : components) {
      if (component instanceof JButton) {
        Action action = ((JButton) component).getAction();
        if (action instanceof TLcdSWAction) {
          //use some reflection to access the delegate action
          for (Field field : TLcdSWAction.class.getDeclaredFields()) {
            if (field.getType().isAssignableFrom(ILcdAction.class)) {
              field.setAccessible(true);
              Object delegateAction = field.get(action);
              if (delegateAction instanceof OpenAction) {
                return ((OpenAction) delegateAction).getOpenSupport();
              }
            }
          }
        }
      }
    }
    return null;
  }

  protected class MyOpenAction extends ALcdAction {
    private final JFileChooser fFileChooser = new JFileChooser();

    public MyOpenAction() {
      super("Open data on both maps", create(OPEN_ICON));

      try {
        OpenSupport gxyOpenSupport = findGXYOpenSupport();
        List<FileFilter> fileFilters = gxyOpenSupport.getFileFilters();
        fFileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fFileChooser.setMultiSelectionEnabled(false);
        for (FileFilter fileFilter : fileFilters) {
          fFileChooser.addChoosableFileFilter(fileFilter);
        }
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      try {
        if (fFileChooser.showOpenDialog(fGXYSample) == JFileChooser.APPROVE_OPTION) {
          File selectedFile = fFileChooser.getSelectedFile();
          openOnMaps(selectedFile.getAbsolutePath());
        }
      } catch (Exception e1) {
        throw new RuntimeException(e1);
      }
    }
  }

  public static void main(final String[] args) {
    EventQueue.invokeLater(new Runnable() {
      @Override
      public void run() {
        final MainPanel mainPanel = new MainPanel();
        mainPanel.start();

        try {
          for (final String arg : args) {
            mainPanel.findLspOpenSupport().openSource(arg);
            mainPanel.findGXYOpenSupport().openSource(arg);
          }
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    });
  }
}

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
package samples.lightspeed.internal.overlay;

import java.awt.Color;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.Panel;
import java.io.IOException;

import javax.swing.*;

import com.luciad.gui.swing.TLcdOverlayLayout;
import com.luciad.symbology.milstd2525b.view.swing.TLcdMS2525bObjectCustomizer;
import com.luciad.view.lightspeed.ILspAWTView;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.TLspViewBuilder;

import samples.common.SampleData;
import samples.lightspeed.common.LightspeedSample;
import samples.lightspeed.common.LspDataUtil;
import samples.lightspeed.common.LuciadLogoIcon;

public class MainPanel extends LightspeedSample {

  @Override
  protected ILspAWTView createView() {
    return TLspViewBuilder.newBuilder()
                          .viewType(ILspView.ViewType.VIEW_2D)
                          .addAtmosphere(true)
                          .buildAWTView();
    //.buildSwingView();
  }

  @Override
  protected void createGUI() {
    super.createGUI();

    Container overlay = getView().getOverlayComponent();
    overlay.removeAll();
    addComponents(overlay);

  }

  private static void addComponents(Container aContainer) {
    GridLayout layout = new GridLayout(4, 6, 20, 20);
    aContainer.setLayout(layout);
    aContainer.add(new JButton("Button"));
    aContainer.add(new JCheckBox());
    aContainer.add(new JComboBox(getManyItems()));
    aContainer.add(new JScrollPane(new JList(getManyItems())));
    aContainer.add(new JSlider(JSlider.HORIZONTAL));
    JSlider transparentSlider = new JSlider(JSlider.VERTICAL);
    transparentSlider.setOpaque(false);
    aContainer.add(transparentSlider);
    aContainer.add(new JSpinner(new SpinnerNumberModel(0, 0, 1000, 1)));
    aContainer.add(new JTextField("Text field"));
    try {
      aContainer.add(new JScrollPane(new JEditorPane("http://www.luciad.com")));
    } catch (IOException e) {
      e.printStackTrace();
    }
    aContainer.add(new JTextPane());
    aContainer.add(new JScrollPane(new JTable(10, 10)));
    aContainer.add(new JScrollPane(new JTree(getManyItems())));
    aContainer.add(new JLabel("Text label"));
    aContainer.add(new JProgressBar(JProgressBar.HORIZONTAL, 0, 1000));
    aContainer.add(new JScrollPane(new JTextArea(getLargeString())));
    aContainer.add(new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JTextArea("Left"), new JTextArea("Right")));
    aContainer.add(new JScrollPane(new JColorChooser()));
    aContainer.add(new JScrollPane(new TLcdMS2525bObjectCustomizer()));
    aContainer.add( /*new JScrollPane*/(createView(ILspView.ViewType.VIEW_2D, true).getHostComponent()));
    aContainer.add( /*new JScrollPane*/(createView(ILspView.ViewType.VIEW_3D, false).getHostComponent()));
  }

  public static void main(final String[] aArgs) {
    startSample(MainPanel.class, "TLspAWTView - overlay stress test");

    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        JFrame frame = new JFrame("JFrame with Panel - reference");
        Panel panel = new Panel();
        panel.setBackground(new Color(163, 193, 222));
        frame.add(panel);
        addComponents(panel);
        frame.setSize(1000, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

      }
    });
  }

  private static ILspAWTView createView(ILspView.ViewType aViewType, boolean aSwing) {
    TLspViewBuilder builder = TLspViewBuilder.newBuilder().viewType(aViewType).addAtmosphere(true);
    ILspAWTView view = aSwing ? builder.buildSwingView() : builder.buildAWTView();
    LspDataUtil.instance().model(SampleData.SAN_FRANCISCO).layer().addToView(view);
    LspDataUtil.instance().grid().addToView(view);

    JLabel luciadLogo = new JLabel(new LuciadLogoIcon());
    view.getOverlayComponent().add(luciadLogo, TLcdOverlayLayout.Location.NORTH_WEST);
    return view;
  }

  private static String[] getManyItems() {
    String[] result = new String[20];
    for (int i = 0; i < 20; i++) {
      result[i] = "item " + i;
    }
    return result;
  }

  private static String getLargeString() {
    StringBuilder builder = new StringBuilder();
    for (int j = 0; j < 20; j++) {
      for (int i = 0; i < 20; i++) {
        builder.append("Text Area ");
      }
      builder.append("\n");
    }
    return builder.toString();
  }
}

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
package samples.common.action;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.print.PageFormat;
import java.awt.print.PrinterJob;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JToolBar;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.luciad.gui.ALcdAction;
import com.luciad.gui.TLcdAWTUtil;
import com.luciad.gui.TLcdIconFactory;
import com.luciad.gui.TLcdPrintPreview;
import com.luciad.gui.swing.TLcdSWAction;
import com.luciad.view.ILcdView;
import com.luciad.view.swing.ALcdViewComponentPrintable;

import samples.common.SwingUtil;
import samples.gxy.common.TitledPanel;

public abstract class APrintPreviewAction<T extends ILcdView, S extends ALcdViewComponentPrintable> extends ALcdAction {

  private final Component fParent;
  private final T fView;

  // The printable that is used to paint the print preview
  private S fPreviewPrintable;

  // Remember the print settings.
  private int fRows = 1;
  private int fColumns = 1;
  private int fDPI = 150;
  private double fFeatureScale = 1;

  public APrintPreviewAction(Component aParent, T aView) {
    fParent = aParent;
    fView = aView;
    setIcon(TLcdIconFactory.create(TLcdIconFactory.PRINT_PREVIEW_ICON));
    setShortDescription("Print preview");
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    beforePrinting();

    fPreviewPrintable = createViewComponentPrintable(fView);
    setRowsAndColumns(fPreviewPrintable, fRows, fColumns);
    setDPI(fPreviewPrintable, fDPI);
    setFeatureScale(fPreviewPrintable, fFeatureScale);

    final Frame parentFrame = TLcdAWTUtil.findParentFrame(fParent);
    // Create a dialog for the preview area.
    JFrame frame = new JFrame("Print preview");
    frame.setIconImages(SwingUtil.sLuciadFrameImage);
    frame.setLayout(new BorderLayout());

    PageFormat format = PrinterJob.getPrinterJob().defaultPage();

    // The preview does the actual work.
    TLcdPrintPreview preview = fPreviewPrintable.createPreview(format);
    frame.add(BorderLayout.CENTER, TitledPanel.createTitledPanel("Preview", preview));

    JPanel settings = new JPanel();
    settings.setLayout(new GridBagLayout());

    // Add a toolbar with a print action.
    JToolBar toolbar = new JToolBar();

    //noinspection unchecked
    APrintAction printAction = new APrintAction(fParent, fView) {
      @Override
      protected ALcdViewComponentPrintable createPrintable(ILcdView aView) {
        S printable = createViewComponentPrintable(fView);
        setDPI(printable, fPreviewPrintable.getDPI());
        setFeatureScale(printable, fPreviewPrintable.getFeatureScale());
        setRowsAndColumns(printable, fRows, fColumns);
        return printable;
      }
    };
    toolbar.add(new TLcdSWAction(printAction));

    settings.setLayout(new GridBagLayout());
    GridBagConstraints c = new GridBagConstraints();
    c.insets = new Insets(5, 5, 5, 5);

    // Wide control for the DPI.

    c.gridwidth = GridBagConstraints.REMAINDER;
    c.gridx = 0;
    c.gridy = 0;
    settings.add(new JLabel("Quality"), c);
    c.gridx = 0;
    c.gridy = 1;
    settings.add(createQualitySlider(), c);

    c.gridwidth = 1;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.anchor = GridBagConstraints.LINE_START;

    // Controls the number of pages. Alternatively, you could set a print scale, e.g. 1 : 10.000.

    c.gridx = 0;
    c.gridy = 2;
    c.weightx = 0;
    settings.add(new JLabel("Width"), c);
    c.gridx = 1;
    c.gridy = 2;
    c.weightx = 0;
    settings.add(createColumnsSpinner(format), c);
    c.gridx = 2;
    c.gridy = 2;
    c.weightx = 1;
    settings.add(new JLabel("page(s)"), c);

    c.gridx = 0;
    c.gridy = 3;
    c.weightx = 0;
    settings.add(new JLabel("Height"), c);
    c.gridx = 1;
    c.gridy = 3;
    c.weightx = 0;
    settings.add(createRowSpinner(format), c);
    c.gridx = 2;
    c.gridy = 3;
    c.weightx = 1;
    settings.add(new JLabel("page(s)"), c);

    // Controls the size of text, icons, labels .

    c.gridx = 0;
    c.gridy = 4;
    c.weightx = 0;
    JLabel featureScale = new JLabel("Feature scale");
    settings.add(featureScale, c);
    c.gridx = 1;
    c.gridy = 4;
    c.weightx = 0;
    settings.add(createFeatureScaleSpinner(), c);
    c.gridx = 2;
    c.gridy = 4;
    c.weightx = 1;
    settings.add(new JLabel("%"), c);
    c.gridx = 0;
    c.gridy = 5;
    c.gridwidth = GridBagConstraints.REMAINDER;
    JLabel info = new JLabel("  Scale factor for line, icon and font sizes");
    info.setFont(info.getFont().deriveFont((float) info.getFont().getSize() - 1));
    info.setForeground(Color.DARK_GRAY);
    settings.add(info, c);

    // Printing on scale is supported by the API, but not implemented in this sample to keep it as
    // simple as possible. Refer to the useMapScale method in sub-classes of ALcdViewComponentPrintable.
    c.gridx = 0;
    c.gridy = 6;
    c.gridwidth = GridBagConstraints.REMAINDER;
    JLabel printOnScale = new JLabel(
        "<html><br/><br/>Printing on a certain map scale - for<br/>" +
        "example 1:10,000 - is also supported.<br/>" +
        "Check out the sample source code.</html>");
//    printOnScale.setFont( printOnScale.getFont().deriveFont( ( float ) printOnScale.getFont().getSize() - 1 ) );
    settings.add(printOnScale, c);

    JPanel outerPanel = new JPanel(new BorderLayout());
    outerPanel.add(settings, BorderLayout.NORTH);//wrap the panel for better vertical resizing behavior

    frame.add(BorderLayout.NORTH, toolbar);
    frame.add(BorderLayout.EAST, TitledPanel.createTitledPanel("Settings", outerPanel, TitledPanel.NORTH | TitledPanel.WEST));

    // Put it all together.
    frame.pack();
    frame.setSize(850, 900);

    TLcdAWTUtil.centerWindow(frame);

    // The original view should not be used while printing.
    parentFrame.setVisible(false);
    frame.setVisible(true);
    frame.addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(WindowEvent e) {
        parentFrame.setVisible(true);
        APrintPreviewAction.this.afterPrinting();
      }
    });
  }

  protected void beforePrinting() {
  }

  protected void afterPrinting() {
  }

  protected abstract S createViewComponentPrintable(T aView);

  protected abstract void setDPI(S aPrintable, int aDpi);

  protected abstract void setRowsAndColumns(S aPrintable, int aRows, int aColumns);

  protected abstract void setFeatureScale(S aPrintable, double aFeatureScale);

  private static final List<Integer> DPI_VALUES = Arrays.asList(75, 150, 300);
  private static final List<String> DPI_DESCRIPTIONS = Arrays.asList("Standard", "High", "Best");

  private Component createQualitySlider() {
    final JSlider slider = new JSlider(0, DPI_VALUES.size() - 1, 0);
    slider.setValue(DPI_VALUES.indexOf(fDPI));
    Hashtable<Integer, JLabel> labels = new Hashtable<Integer, JLabel>();
    for (int i = 0; i < DPI_DESCRIPTIONS.size(); i++) {
      labels.put(i, new JLabel(DPI_DESCRIPTIONS.get(i)));
    }
    slider.setLabelTable(labels);
    slider.setPaintLabels(true);
    slider.addChangeListener(new ChangeListener() {
      @Override
      public void stateChanged(ChangeEvent e) {
        fDPI = DPI_VALUES.get(slider.getValue());
        setDPI(fPreviewPrintable, fDPI);
      }
    });
    return slider;
  }

  private Component createRowSpinner(final PageFormat aPageFormat) {
    final SpinnerNumberModel model = new SpinnerNumberModel(fPreviewPrintable.getPageCountY(aPageFormat), 1, 32, 1);
    final JSpinner spinner = new JSpinner(model);
    model.addChangeListener(new ChangeListener() {
      @Override
      public void stateChanged(ChangeEvent e) {
        fRows = model.getNumber().intValue();
        setRowsAndColumns(fPreviewPrintable, fRows, fColumns);
      }
    });
    return spinner;
  }

  private Component createColumnsSpinner(final PageFormat aPageFormat) {
    final SpinnerNumberModel model = new SpinnerNumberModel(fPreviewPrintable.getPageCountX(aPageFormat), 1, 32, 1);
    final JSpinner spinner = new JSpinner(model);
    model.addChangeListener(new ChangeListener() {
      @Override
      public void stateChanged(ChangeEvent e) {
        fColumns = model.getNumber().intValue();
        setRowsAndColumns(fPreviewPrintable, fRows, fColumns);
      }
    });
    return spinner;
  }

  private Component createFeatureScaleSpinner() {
    final SpinnerNumberModel model = new SpinnerNumberModel(fPreviewPrintable.getFeatureScale() * 100, 25, 200, 25);
    final JSpinner spinner = new JSpinner(model);
    model.addChangeListener(new ChangeListener() {
      @Override
      public void stateChanged(ChangeEvent e) {
        fFeatureScale = model.getNumber().doubleValue() / 100;
        setFeatureScale(fPreviewPrintable, fFeatureScale);
      }
    });
    return spinner;
  }

  protected T getView() {
    return fView;
  }
}

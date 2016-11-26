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
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import com.luciad.gui.ALcdAction;
import com.luciad.gui.TLcdAWTUtil;
import com.luciad.gui.TLcdIconFactory;
import com.luciad.gui.TLcdSVGIcon;
import com.luciad.gui.swing.TLcdOverlayLayout;
import com.luciad.gui.swing.TLcdRotatingIcon;
import com.luciad.util.TLcdPair;
import com.luciad.util.logging.ILcdLogger;
import com.luciad.util.logging.TLcdLoggerFactory;
import com.luciad.view.ILcdView;
import com.luciad.view.swing.ALcdViewComponentPrintable;

import samples.common.gui.ErrorDialog;
import samples.gxy.common.OverlayPanel;

public abstract class APrintAction<T extends ILcdView> extends ALcdAction {

  static ILcdLogger sLogger = TLcdLoggerFactory.getLogger(APrintAction.class.getName());

  private final Component fParentComponent;
  private final T fView;
  private final ALcdViewComponentPrintable fViewComponentPrintable;

  public APrintAction(Component aParentComponent, T aView) {
    this(aParentComponent, aView, null);
  }

  private APrintAction(Component aParentComponent, T aView, ALcdViewComponentPrintable aPrintable) {
    fParentComponent = aParentComponent;
    fView = aView;
    fViewComponentPrintable = aPrintable;
    setIcon(TLcdIconFactory.create(TLcdIconFactory.PRINT_ICON));
    setShortDescription("Print");
  }

  protected abstract ALcdViewComponentPrintable createPrintable(T aView);

  @Override
  public void actionPerformed(ActionEvent e) {
    ALcdViewComponentPrintable printable = fViewComponentPrintable;
    if (printable == null) {
      printable = createPrintable(fView);
    }

    // Get the printer job
    final PrinterJob printerJob = PrinterJob.getPrinterJob();

    // Let the user select a printer and page format.
    printerJob.setPrintable(printable, printerJob.defaultPage());
    if (!printerJob.printDialog()) {
      sLogger.trace("Print cancelled", this);
      return;
    }

    final SynchronousProgressDialog monitor = new SynchronousProgressDialog(
        "Printing", TLcdAWTUtil.findParentFrame(fParentComponent), printable);

    // Actually print the page.
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        beforePrinting();
        try {
          printerJob.print();
          monitor.setVisible(false);
        } catch (PrinterException ex) {
          sLogger.error(ex.getMessage(), ex);
          monitor.setVisible(false);
          monitor.dispose();
          ErrorDialog errorDialog = new ErrorDialog();
          LogRecord errorRecord = new LogRecord(Level.SEVERE, ex.getMessage());
          errorRecord.setThrown(ex);
          errorDialog.setLogMessage(errorRecord);
          JOptionPane.showMessageDialog(fParentComponent, errorDialog, "Printing error", JOptionPane.ERROR_MESSAGE);
        }
        afterPrinting();
      }
    });
  }

  /**
   * Builds a page with the given view and a footer.
   * @param aViewComponent a component representing the view
   * @param aOverlayComponents a list of components to be overlaid on the view and their desired location
   * @return a panel containing the view and a footer
   */
  public static JPanel createPage(Component aViewComponent,
                                  List<TLcdPair<JComponent, TLcdOverlayLayout.Location>> aOverlayComponents) {
    Font font = new Font(Font.SANS_SERIF, Font.BOLD, 14);

    JTextField title = new JTextField("Luciad printing sample");
    title.setFont(font);
    title.setBorder(new EmptyBorder(10, 10, 10, 10));

    DateFormat format = DateFormat.getDateInstance(DateFormat.SHORT);
    if (format instanceof SimpleDateFormat) {
      ((SimpleDateFormat) format).applyPattern("yyyy-MM-dd");
    }
    JTextField date = new JTextField(format.format(new Date()));
    date.setFont(font);
    date.setBorder(new EmptyBorder(10, 10, 10, 10));

    JPanel footer = new JPanel();
    footer.setOpaque(false);
    footer.setBorder(new LineBorder(Color.black));
    footer.setLayout(new BorderLayout());
    footer.add(BorderLayout.CENTER, title);
    footer.add(BorderLayout.EAST, date);

    OverlayPanel panel = new OverlayPanel(aViewComponent);
    for (TLcdPair<JComponent, TLcdOverlayLayout.Location> component : aOverlayComponents) {
      panel.add(component.getKey(), component.getValue());
    }

    JPanel page = new JPanel();
    page.setOpaque(false);
    page.setLayout(new BorderLayout(0, 10));
    page.add(BorderLayout.CENTER, panel);
    page.add(BorderLayout.SOUTH, footer);

    return page;
  }

  public static TLcdRotatingIcon createRotatableCompassIcon() {
    TLcdSVGIcon compass = new TLcdSVGIcon("images/icons/north_direction.svg");
    double ratio = compass.getIconHeight()/compass.getIconWidth();
    compass.setIconHeight(64);
    compass.setIconWidth((int) (64 * ratio));
    return new TLcdRotatingIcon(compass, false);
  }

  protected T getView() {
    return fView;
  }

  protected void beforePrinting() {
  }

  protected void afterPrinting() {
  }

}

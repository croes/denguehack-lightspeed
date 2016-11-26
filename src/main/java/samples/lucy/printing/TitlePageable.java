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
package samples.lucy.printing;

import java.awt.print.PageFormat;
import java.awt.print.Pageable;
import java.awt.print.Printable;

import javax.swing.JLabel;

import com.luciad.gui.TLcdComponentPrintable;

/**
 * A {@code Pageable} that can be used to print a title page.
 */
public class TitlePageable implements Pageable {

  private final PageFormat fPageFormat;

  public TitlePageable(PageFormat aPageFormat) {
    fPageFormat = aPageFormat;
  }

  @Override
  public int getNumberOfPages() {
    return 1;
  }

  @Override
  public PageFormat getPageFormat(int pageIndex) throws IndexOutOfBoundsException {
    return fPageFormat;
  }

  @Override
  public Printable getPrintable(int pageIndex) throws IndexOutOfBoundsException {
    JLabel label = new JLabel("Title page ");
    label.setBounds(0, 0, label.getPreferredSize().width, label.getPreferredSize().height);
    TLcdComponentPrintable printable = new TLcdComponentPrintable(label);
    printable.setBorder(false);
    return printable;
  }

}

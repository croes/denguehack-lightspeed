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
package samples.common.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.Date;
import java.util.logging.LogRecord;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import samples.common.NoopStringTranslator;
import com.luciad.util.ILcdStringTranslator;

/**
 * The error dialog displays details about a specific log message.
 */
public class ErrorDialog extends JPanel {

  private JLabel fDateLabel = new JLabel();
  private JLabel fCategoryLabel = new JLabel();
  private JTextArea fExceptionArea = new JTextArea();
  private JTextArea fMessageArea = new JTextArea();
  private final ILcdStringTranslator fStringTranslator;

  public ErrorDialog() {
    this(new NoopStringTranslator());
  }

  public ErrorDialog(ILcdStringTranslator aStringTranslator) {
    super();
    fStringTranslator = aStringTranslator;
    setLayout(new BorderLayout());

    JPanel headerPanel = setupHeaderPanel();
    JPanel contentPanel = setupContentPanel();

    add(headerPanel, BorderLayout.NORTH);
    add(contentPanel, BorderLayout.CENTER);

  }

  private JPanel setupHeaderPanel() {
    JPanel headerPanel = new JPanel(new BorderLayout());

    JPanel datePanel = new JPanel();
    JPanel categoryPanel = new JPanel();
    FormLayout mgr = new FormLayout("80px,fill:pref:grow", "pref");

    datePanel.setLayout(mgr);
    CellConstraints cc = new CellConstraints();
    datePanel.add(new JLabel(fStringTranslator.translate("Date") + ":"), cc.xy(1, 1));
    datePanel.add(fDateLabel, cc.xy(2, 1));

    mgr = new FormLayout("80px,fill:pref:grow", "pref");
    categoryPanel.setLayout(mgr);

    categoryPanel.add(new JLabel(fStringTranslator.translate("Category") + ": "), cc.xy(1, 1));
    categoryPanel.add(fCategoryLabel, cc.xy(2, 1));
    headerPanel.add(datePanel, BorderLayout.NORTH);
    headerPanel.add(categoryPanel, BorderLayout.CENTER);

    return headerPanel;
  }

  private JPanel setupContentPanel() {
    JPanel contentPanel = new JPanel();

    FormLayout mgr = new FormLayout("fill:pref:grow", "top:pref," +
                                                      "180px," +
                                                      "top:pref," +
                                                      "pref");
    contentPanel.setLayout(mgr);

    fMessageArea.setLineWrap(true);
    fMessageArea.setWrapStyleWord(true);
    JScrollPane messageScroll = insertIntoScrollPane(fMessageArea);
    JScrollPane exceptionScroll = insertIntoScrollPane(fExceptionArea);

    CellConstraints cc = new CellConstraints();
    contentPanel.add(new JLabel(fStringTranslator.translate("Message") + ": "), cc.xy(1, 1));
    contentPanel.add(messageScroll, cc.xy(1, 2));
    contentPanel.add(new JLabel(fStringTranslator.translate("Exception") + ": "), cc.xy(1, 3));
    contentPanel.add(exceptionScroll, cc.xy(1, 4));

    return contentPanel;
  }

  private JScrollPane insertIntoScrollPane(JTextArea aFMessageArea) {
    aFMessageArea.setEditable(false);
    JScrollPane scroll = new JScrollPane(aFMessageArea);
    scroll.setPreferredSize(new Dimension(780, 180));

    return scroll;
  }

  public void setLogMessage(LogRecord aLogMessage) {
    fDateLabel.setText((new Date(aLogMessage.getMillis())).toString());
    fCategoryLabel.setText(aLogMessage.getLevel().getName());
    fExceptionArea.setText(stackTraceToString(aLogMessage.getThrown()));
    fExceptionArea.setCaretPosition(0);
    fMessageArea.setText(aLogMessage.getMessage());
  }

  private String stackTraceToString(Throwable aThrowable) {
    StringBuilder stringBuilder = new StringBuilder();
    if (aThrowable != null) {
      stringBuilder.append(aThrowable.toString());
      stringBuilder.append("\n");
      appendStackTrace(aThrowable, stringBuilder);
      while ((aThrowable = aThrowable.getCause()) != null) {
        stringBuilder.append(fStringTranslator.translate("Caused by")).append(": ").append(aThrowable.toString()).append("\n");
        appendStackTrace(aThrowable, stringBuilder);
      }
      return stringBuilder.toString();
    }
    return fStringTranslator.translate("No exception");
  }

  private void appendStackTrace(Throwable aThrowable, StringBuilder aStringBuilder) {
    for (StackTraceElement stackTraceElement : aThrowable.getStackTrace()) {
      aStringBuilder.append("\t at ");
      aStringBuilder.append(stackTraceElement.toString());
      aStringBuilder.append("\n");
    }
  }

}

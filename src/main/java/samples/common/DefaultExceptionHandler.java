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

import java.awt.Component;
import java.awt.EventQueue;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.Queue;

import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.JWindow;

import com.luciad.util.ILcdStringTranslator;
import com.luciad.util.TLcdLicenseError;
import com.luciad.util.logging.ILcdLogger;
import com.luciad.util.logging.TLcdLoggerFactory;

/**
 * UncaughtExceptionHandler that pops up an error dialog if an exception is not caught.
 * <p/>
 * Typical usage:
 * <code>Thread.setDefaultUncaughtExceptionHandler( new DefaultExceptionHandler() ); </code>
 */
public class DefaultExceptionHandler implements Thread.UncaughtExceptionHandler {

  public static final String UNSATISFIED_LINK_ERROR_MESSAGE = "The application wasn't able to find the required native libraries.\n"
                                                              + "Make sure the required libraries are in the classpath.";
  public static final String INTERNAL_ERROR_MESSAGE = "Internal error occurred.  It might be necessary to restart the application.\nError message: ";
  public static final String OUT_OF_MEMORY_ERROR_MESSAGE = "The application ran out of memory.  Use command line VM option -Xmx, e.g. -Xmx750m on 32-bit systems, or even more on 64-bit systems, to give it more memory.\nIt might be necessary to restart the application.";

  private static ILcdLogger sLogger = TLcdLoggerFactory.getLogger(DefaultExceptionHandler.class.getName());
  private byte[] fSpareMemory = new byte[200000]; // unused memory that is released when an out of memory error occurs
  private boolean fShowAgain = true;
  private final ILcdStringTranslator fTranslator;

  /**
   * A modal dialog blocks until the dialog is hidden, but the modal
   * dialog takes over the event pump which might cause other error dialogs to appear.
   * So you can have multiple modal dialogs at the same time.  This is annoying, as the
   * user might choose not to show the dialog again.  To prevent this, this queue of
   * messages is used, and only one dialog is shown at a time.
   */
  private final Queue<Message> fMessageQueue = new LinkedList<Message>();

  public DefaultExceptionHandler() {
    this(new NoopStringTranslator());
  }

  public DefaultExceptionHandler(ILcdStringTranslator aTranslator) {
    fTranslator = aTranslator;
  }

  //Collections.synchronizedQueue does not exist ...
  private Message pollMessage() {
    synchronized (fMessageQueue) {
      return fMessageQueue.poll();
    }
  }

  private Message peekMessage() {
    synchronized (fMessageQueue) {
      return fMessageQueue.peek();
    }
  }

  private int addMessage(Message aMessage) {
    synchronized (fMessageQueue) {
      fMessageQueue.add(aMessage);
      return fMessageQueue.size();
    }
  }

  private void clearMessageQueue() {
    synchronized (fMessageQueue) {
      fMessageQueue.clear();
    }
  }

  public static void handleUnhandledThrowable(Throwable aThrowableToHandle, ILcdStringTranslator aTranslator) {
    Thread.UncaughtExceptionHandler ueh = Thread.getDefaultUncaughtExceptionHandler();
    if (ueh == null) {
      ueh = new DefaultExceptionHandler(aTranslator);
    }
    ueh.uncaughtException(Thread.currentThread(), aThrowableToHandle);
  }

  @Override
  public void uncaughtException(Thread aThread, Throwable aThrowableToHandle) {
    //Release the spare memory to increase chances of successfully handling the out of memory error.
    if (aThrowableToHandle instanceof OutOfMemoryError) {
      fSpareMemory = null;
    }

    Throwable baseThrowable = getBaseThrowable(aThrowableToHandle);
    Throwable throwableToLog = baseThrowable;
    String messageToLog;

    //the title of the window is likely to be painted by the os, and therefore,
    //even if the message does not get displayed, the title might be.
    String title;
    String message;
    if (baseThrowable instanceof OutOfMemoryError) {
      title = fTranslator.translate("Out of Memory");
      String untranslatedMessage = OUT_OF_MEMORY_ERROR_MESSAGE;
      message = fTranslator.translate(untranslatedMessage);
      messageToLog = untranslatedMessage;
    } else if (baseThrowable instanceof TLcdLicenseError) {
      title = fTranslator.translate("License Error");
      String untranslatedMessage = baseThrowable.getMessage();
      message = fTranslator.translate(untranslatedMessage);
      messageToLog = message;
    } else if (baseThrowable instanceof UnsatisfiedLinkError) {
      title = fTranslator.translate("Unable to link to native libraries");
      String untranslatedMessage = UNSATISFIED_LINK_ERROR_MESSAGE;
      message = fTranslator.translate(untranslatedMessage);
      messageToLog = untranslatedMessage;
    } else {
      title = fTranslator.translate("Internal Error");
      message = fTranslator.translate(INTERNAL_ERROR_MESSAGE) + getThrowableMessage(aThrowableToHandle, fTranslator);
      throwableToLog = aThrowableToHandle;
      messageToLog = getThrowableMessage(aThrowableToHandle, new NoopStringTranslator());
    }

    sLogger.error(messageToLog, throwableToLog);

    if (fShowAgain) {
      int messageCount = addMessage(new Message(title, message));

      // Only show a dialog for the first message, the other messages are handled within that method.
      if (messageCount == 1) {
        showErrorDialog();
      }
    }
  }

  public static String getThrowableMessage(Throwable aThrowableToHandle, ILcdStringTranslator aTranslator) {
    String throwableMessage = aThrowableToHandle.getMessage();
    if (throwableMessage == null) {
      return aThrowableToHandle.getClass().getSimpleName();
    } else {
      return aTranslator.translate(throwableMessage);
    }
  }

  private void showErrorDialog() {
    //Only try showing a dialog for the first message, the other messages are handled by recursively
    //calling this method.
    final Message message = peekMessage();
    if (message != null) {
      Runnable runnable = new Runnable() {
        @Override
        public void run() {
          try {
            Component parent = KeyboardFocusManager.getCurrentKeyboardFocusManager().getActiveWindow();

            // This call blocks.  Note however that the modal dialog does take over the event pump
            // which might cause other errors to happen.
            JOptionPane.showMessageDialog(parent, createContent(message.getMessage()),
                                          message.getTitle(), JOptionPane.ERROR_MESSAGE);

            // Remove the message we've successfully informed the user about
            pollMessage();

            if (fShowAgain) {
              // Allow to show dialogs for messages that are already queued
              showErrorDialog();
            } else {
              clearMessageQueue();
            }
          } catch (Exception e) {
            //Do not re-throw the exception to avoid an endless loop
            sLogger.error(e.getMessage(), e);
          }
        }
      };

      invokeOnFutureEDT(runnable);
    }
  }

  /**
   * Work-around for a Swing issue.
   *
   * During uncaught exception handling the current Event Dispatch Thread (EDT) dies, and a new EDT
   * is started by Swing (with the same name AWT-EventQueue-0).
   * Apparently, calling EventQueue.invokeLater can cause this process to deadlock.
   * As a work-around, we're starting a new thread here that calls the invokeLater method.
   * @param aRunnable The runnable to schedule on the EDT.
   */
  private void invokeOnFutureEDT(final Runnable aRunnable) {
    new Thread(new Runnable() {
      @Override
      public void run() {
        EventQueue.invokeLater(aRunnable);
      }
    }).start();
  }

  /**
   * Creates the content of the error dialog as an object[] that can be given to JOptionPane.
   * @param aMessage The message.
   * @return The content.
   */
  protected Object[] createContent(String aMessage) {
    final JCheckBox show_again = new JCheckBox(fTranslator.translate("Show this dialog again"));
    show_again.setFont(show_again.getFont().deriveFont((float) show_again.getFont().getSize() - 2));

    show_again.setSelected(fShowAgain);
    show_again.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        fShowAgain = show_again.isSelected();
      }
    });
    JTextArea message = createMessageForOptionPane(aMessage);

    return new Object[]{message, Box.createVerticalStrut(10), show_again};
  }

  /**
   * Create a message to be used in a JOptionPane. This message will have a correct
   * preferred size.
   * @param aMessage the message text
   * @return a text area with the correct preferred size
   */
  public static JTextArea createMessageForOptionPane(String aMessage) {
    //Use nice line wrapping for longer messages
    JTextArea message = new JTextArea(aMessage, 0, 60);
    message.setEditable(false);
    message.setOpaque(false);
    message.setWrapStyleWord(true);
    message.setLineWrap(true);

    //Force the text area to properly calculate its preferred size
    //See also http://forums.sun.com/thread.jspa?threadID=5132208
    JWindow window = new JWindow();
    window.getContentPane().add(message);
    window.pack();
    window.dispose();
    return message;
  }

  /**
   * Utility class that stores both a title and a message.
   */
  private static class Message {
    private String fTitle;
    private String fMessage;

    public Message(String aTitle, String aMessage) {
      fTitle = aTitle;
      fMessage = aMessage;
    }

    public String getTitle() {
      return fTitle;
    }

    public String getMessage() {
      return fMessage;
    }
  }

  /**
   * Find the root cause of an exception.
   * @param aThrowable The throwable to examine.
   * @return The root cause of the given aThrowable.
   */
  public static Throwable getBaseThrowable(Throwable aThrowable) {
    Throwable cause = aThrowable.getCause();
    if (cause == null || cause == aThrowable) {
      return aThrowable;
    } else {
      return getBaseThrowable(cause);
    }
  }
}

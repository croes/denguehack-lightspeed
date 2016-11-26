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
package samples.lightspeed.oculus.controller;

import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.KeyEvent;

/**
 * Utility class to determine if a key is pressed. When using the Oculus view, there is no JComponent on which
 * a KeyListener can be registered. The workaround is to use a KeyboardFocusManager, which will capture the key events
 * of the current active window (e.g. the mirror view).
 */
class KeyPressedChecker {

  private static boolean upPressed = false;
  private static boolean downPressed = false;
  private static boolean leftPressed = false;
  private static boolean rightPressed = false;
  private static boolean shiftDown = false;

  public static boolean isUpPressed() {
    synchronized (KeyPressedChecker.class) {
      return upPressed;
    }
  }

  public static boolean isDownPressed() {
    synchronized (KeyPressedChecker.class) {
      return downPressed;
    }
  }

  public static boolean isShiftDown() {
    synchronized (KeyPressedChecker.class) {
      return shiftDown;
    }
  }

  public static boolean isLeftPressed() {
    synchronized (KeyPressedChecker.class) {
      return leftPressed;
    }
  }

  public static boolean isRightPressed() {
    synchronized (KeyPressedChecker.class) {
      return rightPressed;
    }
  }

  public KeyPressedChecker() {
    KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(new KeyEventDispatcher() {

      @Override
      public boolean dispatchKeyEvent(KeyEvent ke) {
        synchronized (KeyPressedChecker.class) {
          shiftDown = ke.isShiftDown();
          switch (ke.getID()) {
          case KeyEvent.KEY_PRESSED:
            if (ke.getKeyCode() == KeyEvent.VK_UP) {
              upPressed = true;
            } else if (ke.getKeyCode() == KeyEvent.VK_DOWN) {
              downPressed = true;
            } else if (ke.getKeyCode() == KeyEvent.VK_LEFT) {
              leftPressed = true;
            } else if (ke.getKeyCode() == KeyEvent.VK_RIGHT) {
              rightPressed = true;
            }
            break;
          case KeyEvent.KEY_RELEASED:
            if (ke.getKeyCode() == KeyEvent.VK_UP) {
              upPressed = false;
            } else if (ke.getKeyCode() == KeyEvent.VK_DOWN) {
              downPressed = false;
            } else if (ke.getKeyCode() == KeyEvent.VK_LEFT) {
              leftPressed = false;
            } else if (ke.getKeyCode() == KeyEvent.VK_RIGHT) {
              rightPressed = false;
            }
            break;
          }
          return false;
        }
      }
    });
  }
}

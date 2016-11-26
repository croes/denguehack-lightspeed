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

import java.awt.Cursor;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.List;

import com.luciad.gui.TLcdAWTUtil;
import com.luciad.util.TLcdPair;
import com.luciad.util.TLcdSystemPropertiesUtil;

/**
 * Cursor factory for the MOVE cursor. On Mac OS X, the MOVE cursor looks identical to the default cursor,
 * which makes it hard to discover some functionality. Making a distinct move cursor on Mac OS X improves this.
 */
public class MouseCursorFactory {

  private static volatile Cursor sMoveCursorMac = Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR);
  private static boolean sCursorLoaded = false;
  private static final List<TLcdPair<Cursor, CursorAccessor>> sRunnables = new ArrayList<>();
  private static final Object sRunnablesLock = new Object();

  static {
    if (hasSpecialMoveCursor()) {
      createMoveCursor();
    }
  }

  private static boolean hasSpecialMoveCursor() {
    return TLcdSystemPropertiesUtil.isMacOS();
  }

  /**
   * <p>Use this method if you want to use the move cursor as a default cursor somewhere.</p>
   *
   * <p>You cannot simply capture the result of {@link #getMoveCursor()} once, since that value
   * can change. So you need to update the default layer. This method does that, but only if
   * the default wasn't overridden by the user of the class yet.</p>
   *
   * @param aAccessor A proxying object that will set the cursor on the class of interest. Should not be <code>null</code>.
   */
  public static void provideMoveCursorAsDefault(CursorAccessor aAccessor) {
    Cursor initialMoveCursor = getMoveCursor();
    aAccessor.setCursor(initialMoveCursor);
    if (!hasSpecialMoveCursor()) {
      return;
    }

    synchronized (sRunnablesLock) {
      if (sCursorLoaded) {
        aAccessor.setCursor(getMoveCursor());
      } else {
        sRunnables.add(new TLcdPair<>(initialMoveCursor, aAccessor));
      }
    }
  }

  /**
   * <p>Returns a move cursor that looks good on all platforms.</p>
   *
   * <p>Otherwise, on a Mac, you get a move cursor that is identical to the default cursor.
   * You usually want it to look different to keep the functionality in your user interface
   * discoverable.</p>
   *
   * <p><strong >Note: don't cache the result of this method</strong>. The return value can change
   * over time, since creating Cursors in Java requires some lengthy and asynchronous setup.</p>
   *
   * @return A move cursor that is always distinct from the default cursor. Never <code>null</code>.
   */
  public static Cursor getMoveCursor() {
    return sMoveCursorMac;
  }

  private static void createMoveCursor() {
    TLcdAWTUtil.invokeNowOrLater(new Runnable() {
      @Override
      public void run() {
        try {
          Image image = Toolkit.getDefaultToolkit().getImage(MouseCursorFactory.class.getResource("/images/icons/move_cursor_20.png"));
          sMoveCursorMac = Toolkit.getDefaultToolkit().createCustomCursor(image, new Point(12, 12), "move_cursor");
          synchronized (sRunnablesLock) {
            sCursorLoaded = true;
            for (TLcdPair<Cursor, CursorAccessor> pair : sRunnables) {
              CursorAccessor accessor = pair.getValue();
              Cursor initialCursor = pair.getKey();
              if (accessor.getCursor() == initialCursor) {
                accessor.setCursor(sMoveCursorMac);
              }
              //else: the default was already changed, so don't override the custom cursor
            }
            sRunnables.clear();
          }
        } catch (HeadlessException aE) {
          //just keep the default move cursor, a cursor is never shown in a headless application anyway
        }
      }
    });
  }

  public interface CursorAccessor {
    void setCursor(Cursor aCursor);

    Cursor getCursor();
  }

}

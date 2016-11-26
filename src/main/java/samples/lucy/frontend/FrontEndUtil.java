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
package samples.lucy.frontend;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;

import com.apple.eawt.Application;
import com.apple.eawt.FullScreenUtilities;
import samples.common.MacUtil;
import com.luciad.lucy.ILcyLucyEnv;
import com.luciad.lucy.util.properties.ALcyProperties;
import com.luciad.util.TLcdSystemPropertiesUtil;

/**
 * <p>
 *   Class containing some support methods for creating a front-end
 * </p>
 *
 */
public final class FrontEndUtil {

  private static final int POINT_VISIBLE_THRESHOLD = 4;

  private FrontEndUtil() {
  }

  /**
   * Stores the state of the main frame into the user preferences
   *
   * @param aMainFrame The main frame
   * @param aPrefix The prefix to use when storing the settings in the user preferences
   * @param aLucyEnv The Lucy back-end
   *
   * @see #restoreFrameState(JFrame, String, ILcyLucyEnv)
   */
  public static void storeFrameState(JFrame aMainFrame, String aPrefix, ILcyLucyEnv aLucyEnv) {
    ALcyProperties prefs = aLucyEnv.getPreferencesManager().getCurrentUserPreferences();

    //Store the frame state in the user preferences
    Rectangle bounds = aMainFrame.getBounds();
    prefs.putInt(aPrefix + "bounds." + "x", bounds.x);
    prefs.putInt(aPrefix + "bounds." + "y", bounds.y);
    prefs.putInt(aPrefix + "bounds." + "width", bounds.width);
    prefs.putInt(aPrefix + "bounds." + "height", bounds.height);
    prefs.putInt(aPrefix + "extendedState", aMainFrame.getExtendedState());
  }

  /**
   * Restores the state of the main frame to match the state which was stored in the user preferences
   *
   * @param aMainFrameSFCT The main frame.
   *                       The state of this frame will be altered.
   * @param aPrefix The prefix which was used to store the settings in the user preferences
   * @param aLucyEnv The Lucy back-end
   *
   * @see #storeFrameState(JFrame, String, ILcyLucyEnv)
   */
  public static void restoreFrameState(JFrame aMainFrameSFCT, String aPrefix, ILcyLucyEnv aLucyEnv) {
    aMainFrameSFCT.pack();

    ALcyProperties prefs = aLucyEnv.getPreferencesManager().getCurrentUserPreferences();

    //set defaults
    Dimension screen_size = Toolkit.getDefaultToolkit().getScreenSize();
    Rectangle screen = new Rectangle(0, 0, screen_size.width, screen_size.height);
    int defaultHeight = screen.height - 40;
    Rectangle bounds = new Rectangle(0, 0, screen.width, defaultHeight);
    Rectangle defaultBounds = new Rectangle(bounds);

    //Read actual values
    bounds.x = prefs.getInt(aPrefix + "bounds." + "x", bounds.x);
    bounds.y = prefs.getInt(aPrefix + "bounds." + "y", bounds.y);
    bounds.width = prefs.getInt(aPrefix + "bounds." + "width", bounds.width);
    bounds.height = prefs.getInt(aPrefix + "bounds." + "height", bounds.height);
    int extended_state = prefs.getInt(aPrefix + "extendedState", Frame.MAXIMIZED_BOTH);

    //Check whether Lucy was running in full screen mode on the Mac
    if ( TLcdSystemPropertiesUtil.isMacOS()) {
      GraphicsDevice defaultScreenDevice = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
      GraphicsConfiguration config = defaultScreenDevice.getDefaultConfiguration();
      Rectangle screenBounds = config.getBounds();
      if ( screenBounds.equals(bounds) && extended_state == Frame.NORMAL ){
        //Lucy was running full screen when she was closed.
        //Restart in full screen mode
        aMainFrameSFCT.setBounds(defaultBounds);
        Application.getApplication().requestToggleFullScreen(aMainFrameSFCT);
      }
    }


    //strip ICONIFIED from the extended state, so that the frame is always visible on startup
    extended_state = extended_state & ~Frame.ICONIFIED;

    //make sure the screen is always completely visible
    //first get all the screens
    List<Rectangle> list = new ArrayList<Rectangle>();
    GraphicsDevice[] devices = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();
    for (GraphicsDevice device : devices) {
      GraphicsConfiguration config = device.getDefaultConfiguration();
      Rectangle screen_bounds = config.getBounds();
      Insets insets = Toolkit.getDefaultToolkit().getScreenInsets(config);
      screen_bounds.x += insets.left;
      screen_bounds.y += insets.top;
      screen_bounds.width -= insets.left + insets.right;
      screen_bounds.height -= insets.top + insets.bottom;
      list.add(screen_bounds);
    }

    //next, see if all corner points if the Lucy frame are visible
    Point[] corner_points = new Point[]{
        new Point(bounds.x, bounds.y),
        new Point(bounds.x + bounds.width, bounds.y),
        new Point(bounds.x, bounds.y + bounds.height),
        new Point(bounds.x + bounds.width, bounds.y + bounds.height)
    };
    boolean completely_visible = true;
    for (Point corner_point : corner_points) {
      completely_visible = completely_visible && isVisible(corner_point, list, POINT_VISIBLE_THRESHOLD);
    }

    //if not, adjust the window so that the window is completely visible
    if (!completely_visible) {
      //move to the nearest screen
      Rectangle nearest_screen = findNearestScreen(bounds, list);
      bounds.x = nearest_screen.x;
      bounds.y = nearest_screen.y;
      bounds = bounds.intersection(nearest_screen);

      //at this point the window is in the top left corner of the screen and completely
      //contained within that screen

      //now move it to the center of that screen.
      int delta_w = nearest_screen.width - bounds.width;
      int delta_h = nearest_screen.height - bounds.height;
      bounds.x += delta_w / 2;
      bounds.y += delta_h / 2;
    }

    //If java 1.4 or above is available, the frame size is set full screen.
    //Regardless that, we always set the window size to its actual size
    //because otherwise the initial map fitting sometimes fails.
    aMainFrameSFCT.setBounds(bounds);

    aMainFrameSFCT.setExtendedState(extended_state);
  }

  private static Rectangle findNearestScreen(Rectangle aBounds, List<Rectangle> aList) {
    double minimal_distance = Double.POSITIVE_INFINITY;
    Rectangle nearest_screen = null;
    for (Rectangle rectangle : aList) {
      double distance_to_move = Math.sqrt(Math.pow(rectangle.x - aBounds.x, 2) +
                                          Math.pow(rectangle.y - aBounds.y, 2));

      if (distance_to_move < minimal_distance) {
        minimal_distance = distance_to_move;
        nearest_screen = rectangle;
      }
    }
    return nearest_screen;
  }

  private static boolean isVisible(Point aPoint, List<Rectangle> aList, int aThreshold) {
    for (Rectangle rectangle : aList) {
      rectangle = new Rectangle(rectangle.x - aThreshold,
                                rectangle.y - aThreshold,
                                rectangle.width + (2 * aThreshold),
                                rectangle.height + (2 * aThreshold));
      if (rectangle.contains(aPoint)) {
        return true;
      }
    }
    return false;
  }
}



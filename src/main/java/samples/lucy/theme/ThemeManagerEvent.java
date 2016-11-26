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
package samples.lucy.theme;

import java.util.EventObject;

/**
 * <p>Event fired when a {@code ThemeManager} is changed. Current changes
 * involve the addition and the removal of a {@code Theme}.</p>
 *
 */
public final class ThemeManagerEvent extends EventObject {
  /**
   * The different types of events
   */
  public static enum Type {
    /**
     * Indicates a theme has been added
     */
    THEME_ADDED,
    /**
     * Indicates a theme has been removed
     */
    THEME_REMOVED
  }

  private final Type fType;
  private final Theme fTheme;

  /**
   * Creates a new event
   * @param aThemeManager The theme manager which has been changed
   * @param aType The type of the event
   * @param aTheme The theme
   */
  public ThemeManagerEvent(ThemeManager aThemeManager, Type aType, Theme aTheme) {
    super(aThemeManager);
    fType = aType;
    fTheme = aTheme;
  }

  @Override
  public ThemeManager getSource() {
    return (ThemeManager) super.getSource();
  }

  /**
   * Returns the type of the event
   * @return the type of the event
   */
  public Type getType() {
    return fType;
  }

  /**
   * Returns the affected theme
   * @return the affected theme
   */
  public Theme getTheme() {
    return fTheme;
  }

  /**
   * Returns the theme manager which has been changed
   * @return the theme manager which has been changed
   */
  public ThemeManager getThemeManager() {
    return getSource();
  }
}

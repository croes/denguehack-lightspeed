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

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import com.luciad.lucy.ILcyLucyEnv;
import com.luciad.util.logging.ILcdLogger;
import com.luciad.util.logging.TLcdLoggerFactory;

/**
 * <p>Manager for the different {@link Theme}s.</p>

 */
public final class ThemeManager {

  private static final ILcdLogger LOGGER = TLcdLoggerFactory.getLogger(ThemeManager.class);

  private final ILcyLucyEnv fLucyEnv;
  private final Set<Theme> fThemes = new LinkedHashSet<Theme>();
  private final PropertyChangeSupport fPropertyChangeSupport = new PropertyChangeSupport(this);
  private final CopyOnWriteArrayList<ThemeManagerListener> fThemeManagerListeners = new CopyOnWriteArrayList<ThemeManagerListener>();
  private Theme fActiveTheme = null;

  /**
   * <p>Create a new theme manager.</p>
   *
   * <p>
   *   Normally you do not need to use this constructor.
   *   Typically, there is one add-on in the application which creates the manager and registers it on the Lucy
   *   back-end.
   *   Other add-ons can retrieve it from the services when needed.
   * </p>
   *
   * @param aLucyEnv The Lucy back-end
   */
  public ThemeManager(ILcyLucyEnv aLucyEnv) {
    fLucyEnv = aLucyEnv;
  }

  /**
   * Add a theme to this manager
   *
   * @param aTheme The theme to add
   */
  public void addTheme(Theme aTheme) {
    boolean added = fThemes.add(aTheme);
    if (added) {
      LOGGER.debug("Theme [" + aTheme.getDisplayName() + "] added");
      ThemeManagerEvent event = new ThemeManagerEvent(this, ThemeManagerEvent.Type.THEME_ADDED, aTheme);
      for (ThemeManagerListener listener : fThemeManagerListeners) {
        listener.themeManagerChanged(event);
      }
    }
  }

  /**
   * Remove a theme from this manager
   *
   * @param aTheme The theme to remove
   */
  public void removeTheme(Theme aTheme) {
    boolean removed = fThemes.remove(aTheme);
    if (removed) {
      LOGGER.debug("Theme [" + aTheme.getDisplayName() + "] removed");
      ThemeManagerEvent event = new ThemeManagerEvent(this, ThemeManagerEvent.Type.THEME_REMOVED, aTheme);
      for (ThemeManagerListener listener : fThemeManagerListeners) {
        listener.themeManagerChanged(event);
      }
    }
  }

  /**
   * <p>Returns the available themes.</p>
   *
   * <p>The returned {@code List} is immutable. Use the {@link #addTheme(Theme)} and
   * {@link #removeTheme(Theme)} methods if you want to alter the registered themes.</p>
   *
   * @return the available themes
   */
  public Collection<Theme> getThemes() {
    return Collections.unmodifiableCollection(fThemes);
  }

  /**
   * <p>Activates the specified theme. If another theme was still active, it will be de-activated
   * first.</p>
   *
   * <p>A {@code PropertyChangeEvent} will be fired when {@code aTheme} is different from the
   * currently active theme.</p>
   *
   * @param aTheme The theme to make active. May be {@code null}, in which case the current theme
   *               will be de-activated but no other theme will be activated.
   */
  public void setActiveTheme(Theme aTheme) {
    if (fActiveTheme == aTheme) {
      return;
    }
    if (aTheme != null && !(fThemes.contains(aTheme))) {
      throw new UnsupportedOperationException("Can only activate themes which are registered to the theme manager");
    }
    Theme oldValue = fActiveTheme;
    if (fActiveTheme != null) {
      LOGGER.debug("Deactivating theme " + fActiveTheme.getDisplayName());
      fActiveTheme.deactivate(fLucyEnv);
    }
    fActiveTheme = aTheme;
    if (fActiveTheme != null) {
      LOGGER.debug("Activating theme " + fActiveTheme.getDisplayName());
      fActiveTheme.activate(fLucyEnv);
    }
    fPropertyChangeSupport.firePropertyChange("activeTheme", oldValue, aTheme);
  }

  /**
   * Returns the currently active theme. May be {@code null}
   *
   * @return the currently active theme. May be {@code null}
   */
  public Theme getActiveTheme() {
    return fActiveTheme;
  }

  /**
   * Adds a property change listener which will be informed when the active theme
   * changes
   * @param aPropertyChangeListener The listener to add
   */
  public void addPropertyChangeListener(PropertyChangeListener aPropertyChangeListener) {
    fPropertyChangeSupport.addPropertyChangeListener(aPropertyChangeListener);
  }

  /**
   * Remove {@code aPropertyChangeListener} so it will no longer be notified when the
   * active theme changes
   * @param aPropertyChangeListener The listener to remove
   */
  public void removePropertyChangeListener(PropertyChangeListener aPropertyChangeListener) {
    fPropertyChangeSupport.removePropertyChangeListener(aPropertyChangeListener);
  }

  /**
   * Register {@code aListener} to be notified when the state of this theme manager changes
   * @param aListener the listener
   */
  public void addThemeManagerListener(ThemeManagerListener aListener) {
    fThemeManagerListeners.add(aListener);
  }

  /**
   * Removes {@code aListener} so it will no longer be notified when the state of this theme manager
   * changes
   * @param aListener the listener
   */
  public void removeThemeManagerListener(ThemeManagerListener aListener) {
    fThemeManagerListeners.remove(aListener);
  }
}

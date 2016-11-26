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

import java.util.Objects;

import com.luciad.gui.TLcdAWTUtil;
import com.luciad.lucy.ILcyLucyEnv;
import com.luciad.lucy.ILcyLucyEnvListener;
import com.luciad.lucy.TLcyLucyEnvEvent;
import com.luciad.lucy.addons.ALcyPreferencesAddOn;
import com.luciad.lucy.util.TLcyVetoException;

/**
 * Base class to create an add-on which provides a {@link Theme}
 * <p/>
 * The configuration allows to specify the following options:
 * <pre class="cocde">
 *   # Set to true when this time should be the default active
 *   # theme on startup
 *   # Only one of the themes registered to Lucy should be marked
 *   # as default. When multiples are marked as default,
 *   # it is undefined which theme will be activated by default
 *   #
 *   # When the property is not specified, it will be assumed the theme is not the default theme
 *   shortPrefix.theme.default = false
 * </pre>
 */
public abstract class AThemeAddOn extends ALcyPreferencesAddOn {

  private static final String THEME_PREFIX = "theme.";
  private static final String DEFAULT_THEME = THEME_PREFIX + "default";
  private Theme fTheme;

  protected AThemeAddOn(String aLongPrefix, String aShortPrefix) {
    super(aLongPrefix, aShortPrefix);
  }

  @Override
  public void plugInto(ILcyLucyEnv aLucyEnv) {
    super.plugInto(aLucyEnv);

    final ThemeManager themeManager = aLucyEnv.getService(ThemeManager.class);
    if (themeManager == null) {
      throw new UnsupportedOperationException("The " + getClass().getSimpleName() + " requires a theme manager. " +
                                              "Make sure the add-on responsible for plugging in the theme manager is loaded before this add-on.");
    }

    fTheme = createTheme(aLucyEnv);
    Objects.requireNonNull(fTheme, "The " + getClass().getSimpleName() + "#createTheme method must not return null");
    themeManager.addTheme(fTheme);

    if (getPreferences().getBoolean(getShortPrefix() + DEFAULT_THEME, false)) {
      aLucyEnv.addLucyEnvListener(new ILcyLucyEnvListener() {
        @Override
        public void lucyEnvStatusChanged(TLcyLucyEnvEvent aEvent) throws TLcyVetoException {
          if (aEvent.getID() == TLcyLucyEnvEvent.INITIALIZED) {
            aEvent.getLucyEnv().removeLucyEnvListener(this);
            TLcdAWTUtil.invokeLater(new Runnable() {
              @Override
              public void run() {
                themeManager.setActiveTheme(fTheme);
              }
            });
          }
        }
      });
    }
  }

  /**
   * Creates the theme of this add-on
   * @param aLucyEnv The Lucy back-end
   * @return The theme of this add-on. Must not be {@code null}
   */
  protected abstract Theme createTheme(ILcyLucyEnv aLucyEnv);

  /**
   * Returns the theme which was created. Method will only create a theme after the {@link #plugInto(ILcyLucyEnv)}
   * has been called
   * @return The theme created by this add-on
   */
  public Theme getTheme() {
    return fTheme;
  }
}

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

import com.luciad.lucy.ILcyLucyEnv;

/**
 * <p>Interface representing a theme.</p>
 *
 * <p>By using the {@link ThemeManager}, the user can switch between
 * different themes at run time. It is up to the theme to decide
 * what should be changed to Lucy when it is activated.</p>
 *
 * <p>When Lucy switches to another theme, it is the responsibility
 * of the old theme to clean up everything it has altered to Lucy.</p>
 */
public interface Theme {
  /**
   * <p>Make this theme active.</p>
   *
   * <p>Activating a theme can for example include adding layers to the map
   * and adding panels to the Lucy UI.</p>
   * @param aLucyEnv The Lucy back-end
   */
  void activate(ILcyLucyEnv aLucyEnv);

  /**
   * <p>De-activate this theme.</p>
   *
   * <p>This should clean up everything that was added to the Lucy env in the
   * {@link #activate(ILcyLucyEnv) activate} method.</p>
   * @param aLucyEnv The Lucy back-end
   */
  void deactivate(ILcyLucyEnv aLucyEnv);

  /**
   * Returns a String representation for this theme which can be used in the UI
   * @return a String representation for this theme which can be used in the UI
   */
  String getDisplayName();
}

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
 * Abstract base class which facilitates the writing of a
 * decorator for a {@code Theme} instance. You only need to
 * override the methods you want to decorate.
 */
public abstract class AThemeDecorator implements Theme {
  private final Theme fDelegate;

  /**
   * Creates a new decorator for {@code aDelegate}
   * @param aDelegate The theme to be wrapped. Must not be {@code null}
   */
  protected AThemeDecorator(Theme aDelegate) {
    if (aDelegate == null) {
      throw new IllegalArgumentException("The delegate theme must not be null");
    }
    fDelegate = aDelegate;
  }

  @Override
  public void activate(ILcyLucyEnv aLucyEnv) {
    fDelegate.activate(aLucyEnv);
  }

  @Override
  public void deactivate(ILcyLucyEnv aLucyEnv) {
    fDelegate.deactivate(aLucyEnv);
  }

  @Override
  public String getDisplayName() {
    return fDelegate.getDisplayName();
  }

  /**
   * Returns the delegate theme
   * @return the delegate theme
   */
  public final Theme getDelegate() {
    return fDelegate;
  }
}

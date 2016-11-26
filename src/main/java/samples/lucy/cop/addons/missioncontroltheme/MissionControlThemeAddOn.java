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
package samples.lucy.cop.addons.missioncontroltheme;

import com.luciad.lucy.ILcyLucyEnv;
import com.luciad.lucy.gui.customizer.TLcyCompositeCustomizerPanelFactory;
import com.luciad.lucy.util.ALcyTool;
import com.luciad.util.enumeration.ILcdMorphingFunction;

import samples.lucy.cop.PathResolver;
import samples.lucy.cop.gazetteer.GazetteerThemeDecorator;
import samples.lucy.cop.theme.ClassifiedThemeDecorator;
import samples.lucy.theme.AThemeAddOn;
import samples.lucy.theme.Theme;
import samples.lucy.theme.ThemeWithConfiguredLayersDecorator;

/**
 * Add-on which provides the mission control theme to the theme manager
 */
public class MissionControlThemeAddOn extends AThemeAddOn {
  public MissionControlThemeAddOn() {
    super(ALcyTool.getLongPrefix(MissionControlThemeAddOn.class),
          ALcyTool.getShortPrefix(MissionControlThemeAddOn.class));
  }

  @Override
  public void plugInto(ILcyLucyEnv aLucyEnv) {
    super.plugInto(aLucyEnv);

    new TLcyCompositeCustomizerPanelFactory(aLucyEnv).addCustomizerPanelFactory(new BlueForcesCustomizerPanelFactory(aLucyEnv));
    new TLcyCompositeCustomizerPanelFactory(aLucyEnv).addCustomizerPanelFactory(new AnnotationsCustomizerPanelFactory(aLucyEnv));
    new TLcyCompositeCustomizerPanelFactory(aLucyEnv).addCustomizerPanelFactory(new SpotReportsCustomizerPanelFactory(getShortPrefix(), getPreferences(), aLucyEnv));
  }

  @Override
  protected Theme createTheme(final ILcyLucyEnv aLucyEnv) {
    Theme missionControlTheme = new MissionControlTheme(getShortPrefix(), getPreferences(), getLongPrefix(), aLucyEnv);
    return new ThemeWithConfiguredLayersDecorator(
        new GazetteerThemeDecorator(
            new ClassifiedThemeDecorator(missionControlTheme, getShortPrefix(), getPreferences()),
            getShortPrefix(),
            getPreferences(),
            getLongPrefix(),
            aLucyEnv
        ),
        aLucyEnv, getShortPrefix(), getPreferences(), new ILcdMorphingFunction<String, String>() {
      @Override
      public String morph(String aObject) {
        PathResolver pathResolver = aLucyEnv.getService(PathResolver.class);
        return pathResolver.convertPath(aObject);
      }
    });
  }
}

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
package samples.lucy.showreadme;

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.JPanel;

import samples.lucy.util.ShowReadMeUtil;
import com.luciad.lucy.ILcyLucyEnv;
import com.luciad.lucy.addons.ALcyPreferencesAddOn;
import com.luciad.lucy.gui.ALcyApplicationPaneTool;

/**
 * This addon can show the readme file used by samples and adds workspace support for the readme pane.
 * The path of the readme file must be overridden in a config file with property "ShowReadMeAddOn.ReadMeFile"
 */
public class ShowReadMeAddOn extends ALcyPreferencesAddOn {
  private ALcyApplicationPaneTool fApplicationPaneTool;

  public ShowReadMeAddOn() {
    super("samples.lucy.showreadme.", "ShowReadMeAddOn.");
  }

  @Override
  public void plugInto(ILcyLucyEnv aLucyEnv) {
    super.plugInto(aLucyEnv);

    fApplicationPaneTool = new MyApplicationPaneTool();
    fApplicationPaneTool.plugInto(aLucyEnv);
  }

  @Override
  public void unplugFrom(ILcyLucyEnv aLucyEnv) {
    super.unplugFrom(aLucyEnv);

    fApplicationPaneTool.unplugFrom(aLucyEnv);
    fApplicationPaneTool = null;
  }

  private String getTextFileName() {
    return getPreferences().getString("ShowReadMeAddOn.ReadMeFile", null);
  }

  private class MyApplicationPaneTool extends ALcyApplicationPaneTool {

    public MyApplicationPaneTool() {
      super(ShowReadMeAddOn.this.getPreferences(),
            ShowReadMeAddOn.this.getLongPrefix(),
            ShowReadMeAddOn.this.getShortPrefix());
    }

    @Override
    protected Component createContent() {
      final JPanel content = new JPanel(new BorderLayout());

      ShowReadMeUtil show_readme_util = new ShowReadMeUtil();
      String filename = getTextFileName();
      if (filename != null) {
        show_readme_util.setReadMeFileName(filename);
      }
      content.add(show_readme_util.getContent());

      return content;
    }
  }
}

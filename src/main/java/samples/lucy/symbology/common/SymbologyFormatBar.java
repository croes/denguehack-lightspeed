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
/**
 *
 */
package samples.lucy.symbology.common;

import java.awt.Component;

import javax.swing.BoxLayout;
import javax.swing.JToolBar;

import samples.lucy.formatbar.LayerAwareFormatBar;
import samples.symbology.common.FlipOrientationAction;
import com.luciad.lucy.ILcyLucyEnv;
import com.luciad.lucy.gui.ILcyToolBar;
import com.luciad.lucy.gui.TLcyActionBarMediatorBuilder;
import com.luciad.lucy.gui.TLcyActionBarUtil;
import com.luciad.lucy.gui.TLcyToolBar;
import com.luciad.lucy.map.ILcyGenericMapComponent;
import com.luciad.lucy.util.properties.ALcyProperties;
import com.luciad.util.ILcdFilter;
import com.luciad.view.ILcdLayer;
import com.luciad.view.ILcdTreeLayered;
import com.luciad.view.ILcdView;

/**
 * {@link LayerAwareFormatBar} for symbologies. This format bar is represented as a toolbar that
 * contains the following set of actions.</p>
 * <ul>
 * <li>Action to insert a new symbology layer</li>
 * <li>Action to create new symbology objects by showing an appropriate customizer</li>
 * <li>Action to easily create often used symbology objects</li>
 * <li>Action to set the affiliation</li>
 * <li>Action to set the status</li>
 * <li>Action to show the recently added symbols</li>
 * </ul>
 */
public abstract class SymbologyFormatBar<S extends ILcdView & ILcdTreeLayered> extends LayerAwareFormatBar<S> {

  private static final String FLIP_ORIENTATION_ACTION_PROPERTY = "flipOrientationAction";
  private static final String APP6 = "APP";
  private static final String MS2525 = "2525";

  private final ILcyToolBar fToolBar;
  private final ALcyProperties fProperties;
  private final String fPropertiesPrefix;
  private final ILcyLucyEnv fLucyEnv;

  public SymbologyFormatBar(ILcyGenericMapComponent<S, ? extends ILcdLayer> aMapComponent, String aToolBarID, ALcyProperties aProperties, String aPropertiesPrefix, ILcyLucyEnv aLucyEnv) {
    super(aMapComponent.getMainView());
    fProperties = aProperties;
    fPropertiesPrefix = aPropertiesPrefix;
    fLucyEnv = aLucyEnv;
    fToolBar = createToolBar(aMapComponent, aToolBarID);
    addFlipOrientationAction(aMapComponent);
  }

  private ILcyToolBar createToolBar(ILcyGenericMapComponent aMapComponent, String aToolBarID) {
    JToolBar toolBar = new JToolBar() {
      @Override
      public void updateUI() {
        super.updateUI();
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
      }
    };
    toolBar.setFloatable(false);
    toolBar.setLayout(new BoxLayout(toolBar, BoxLayout.X_AXIS));
    TLcyToolBar result = new TLcyToolBar(toolBar);
    result.setProperties(fProperties.subset(fPropertiesPrefix + aToolBarID + "."));
    TLcyActionBarMediatorBuilder.newInstance(fLucyEnv.getUserInterfaceManager().getActionBarManager())
                                .sourceActionBar(aToolBarID, aMapComponent)
                                .targetActionBar(result)
                                .bidirectional()
                                .mediate();
    return result;
  }

  @Override
  public boolean canSetLayer(ILcdLayer aLayer) {
    // TLcySafeGuardFormatWrapper already checks the layer
    return true;
  }

  @Override
  public Component getComponent() {
    return fToolBar.getComponent();
  }

  public ILcyToolBar getToolBar() {
    return fToolBar;
  }

  @Override
  protected void updateForLayer(ILcdLayer aPreviousLayer, ILcdLayer aLayer) {
  }

  private void addFlipOrientationAction(ILcyGenericMapComponent aMapComponent) {
    FlipOrientationAction action = new FlipOrientationAction(aMapComponent.getMainView(), new ILcdFilter<String>() {
      @Override
      public boolean accept(String aStandard) {
        String shortPrefix = fPropertiesPrefix;
        return (shortPrefix.contains(APP6) && aStandard.contains(APP6)) ||
               (shortPrefix.contains(MS2525) && aStandard.contains(MS2525));
      }
    });
    action.putValue(TLcyActionBarUtil.ID_KEY, fPropertiesPrefix + FLIP_ORIENTATION_ACTION_PROPERTY);
    TLcyActionBarUtil.insertInConfiguredActionBars(
        action,
        aMapComponent,
        fLucyEnv.getUserInterfaceManager().getActionBarManager(),
        fProperties
    );
  }

}

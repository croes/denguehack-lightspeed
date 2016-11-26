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
package samples.lucy.clustering;

import com.luciad.lucy.ILcyLucyEnv;
import com.luciad.lucy.addons.lightspeed.ALcyLspFormatAddOn;
import com.luciad.lucy.format.lightspeed.ALcyLspFormat;
import com.luciad.lucy.format.lightspeed.TLcyLspFormatTool;
import com.luciad.lucy.format.lightspeed.TLcyLspSafeGuardFormatWrapper;
import com.luciad.lucy.gui.customizer.TLcyCompositeCustomizerPanelFactory;
import com.luciad.lucy.util.ALcyTool;

/**
 * {@code ALcyLspFormatAddOn} which adds support for the visualization of the
 * {@link samples.common.SampleData#HUMANITARIAN_EVENTS} data set.
 *
 * As this dataset is a rather large dataset, a {@link com.luciad.model.transformation.clustering.TLcdClusteringTransformer}
 * will be used to cluster the data when zoomed out.
 */
public final class ClusteringSHPAddOn extends ALcyLspFormatAddOn {

  public ClusteringSHPAddOn() {
    super(ALcyTool.getLongPrefix(ClusteringSHPAddOn.class),
          ALcyTool.getShortPrefix(ClusteringSHPAddOn.class));
  }

  @Override
  public void plugInto(ILcyLucyEnv aLucyEnv) {
    super.plugInto(aLucyEnv);

    addClusteringModelWorkspaceSupport(aLucyEnv);
    addClusterIconStyleCustomizerPanel(aLucyEnv);
    addClusterTextStyleCustomizerPanel(aLucyEnv);
  }

  private void addClusterIconStyleCustomizerPanel(ILcyLucyEnv aLucyEnv) {
    new TLcyCompositeCustomizerPanelFactory(aLucyEnv).addCustomizerPanelFactory(new ClusterIconStyleCustomizerPanelFactory());
  }

  private void addClusterTextStyleCustomizerPanel(ILcyLucyEnv aLucyEnv) {
    new TLcyCompositeCustomizerPanelFactory(aLucyEnv).addCustomizerPanelFactory(new ClusterTextStyleCustomizerPanelFactory());
  }

  private void addClusteringModelWorkspaceSupport(ILcyLucyEnv aLucyEnv) {
    aLucyEnv.getWorkspaceManager().addWorkspaceObjectCodec(new ClusteringModelCodec(getLongPrefix(), getShortPrefix()));
  }

  @Override
  protected ALcyLspFormat createBaseFormat() {
    return new ClusteringFormat(getLucyEnv(), getLongPrefix(), getShortPrefix(), getPreferences());
  }

  @Override
  protected ALcyLspFormat createFormatWrapper(ALcyLspFormat aBaseFormat) {
    return new TLcyLspSafeGuardFormatWrapper(aBaseFormat);
  }

  @Override
  protected TLcyLspFormatTool createFormatTool(ALcyLspFormat aFormat) {
    //To ensure that this format picks up the dataset with humanitarian events,
    //we register everything with a higher-than-normal priority
    //Otherwise the TLcyLspSHPFormatAddOn might pick up the dataset
    return new TLcyLspFormatTool(aFormat, ILcyLucyEnv.PRIORITY_DEFAULT - 1);
  }
}

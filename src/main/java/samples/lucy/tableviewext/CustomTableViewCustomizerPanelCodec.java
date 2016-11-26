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
package samples.lucy.tableviewext;

import java.io.IOException;

import samples.lucy.tableview.TableViewCustomizerPanelCodec;
import com.luciad.lucy.ILcyLucyEnv;
import com.luciad.lucy.gui.customizer.ILcyCustomizerPanel;
import com.luciad.lucy.gui.customizer.ILcyCustomizerPanelFactory;
import com.luciad.lucy.util.properties.ALcyProperties;
import com.luciad.lucy.util.properties.TLcyStringProperties;
import com.luciad.lucy.workspace.ALcyWorkspaceCodec;

/**
 * Extension of TableViewCustomizerPanelCodec that only works for MyTableViewCustomizerPanel, and
 * that saves the extra color index property.
 *
 */
final class CustomTableViewCustomizerPanelCodec extends TableViewCustomizerPanelCodec {
  private static final String COLOR_INDEX = "colorIndex";

  public CustomTableViewCustomizerPanelCodec(String aUID,
                                             String aPrefix,
                                             ILcyCustomizerPanelFactory aCustomizerPanelFactory,
                                             ILcyLucyEnv aLucyEnv) {
    super(aUID, aPrefix, aCustomizerPanelFactory, aLucyEnv);
  }

  @Override
  public boolean canEncodeObject(ALcyWorkspaceCodec aWSCodec, Object aObject, Object aParent) {
    return aObject instanceof CustomTableViewCustomizerPanel &&
           getCustomizerPanelFactory().canCreateCustomizerPanel(((ILcyCustomizerPanel) aObject).getObject());
  }

  @Override
  protected void encodeObjectImp(ALcyWorkspaceCodec aWSCodec, Object aObject, TLcyStringProperties aProperties) throws IOException {
    super.encodeObjectImp(aWSCodec, aObject, aProperties);

    //store the color
    CustomTableViewCustomizerPanel panel = (CustomTableViewCustomizerPanel) aObject;
    aProperties.putInt(getPrefix() + COLOR_INDEX, panel.getColorIndex());
  }

  @Override
  protected Object createObjectImpl(ALcyWorkspaceCodec aWSCodec, ALcyProperties aProperties) throws IOException {
    Object object = super.createObjectImpl(aWSCodec, aProperties);
    if (object instanceof CustomTableViewCustomizerPanel) {
      CustomTableViewCustomizerPanel panel = (CustomTableViewCustomizerPanel) object;
      panel.setColorIndex(aProperties.getInt(getPrefix() + COLOR_INDEX, panel.getColorIndex()));
    }
    return object;
  }
}

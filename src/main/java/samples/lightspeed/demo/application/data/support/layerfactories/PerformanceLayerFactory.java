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
package samples.lightspeed.demo.application.data.support.layerfactories;

import java.util.Arrays;
import java.util.Collection;

import samples.lightspeed.debug.GLResourceOverlay;
import samples.lightspeed.debug.PerformanceOverlay;
import com.luciad.model.ILcdModel;
import com.luciad.view.lightspeed.layer.ILspLayer;

import samples.lightspeed.demo.application.data.support.modelfactories.DummyModelFactory;
import samples.lightspeed.demo.framework.data.AbstractLayerFactory;

/**
 * Layer factory for creating a performance overlay layer.
 * <p/>
 * This layer factory can be configured with following properties:
 * <table cellspacing="10">
 * <tr> <td><b>Key</b></td> <td><b>Type</b></td> <td><b>Default Value</b></td>
 * <td><b>Description</b></td> </tr>
 * <tr> <td>targetFramerate</td> <td>float</td> <td>60</td> <td>Specifies the desired
 * framerate</td></tr>
 * </table>
 */
public class PerformanceLayerFactory extends AbstractLayerFactory {

  @Override
  public Collection<ILspLayer> createLayers(ILcdModel aModel) {
    PerformanceOverlay performanceOverlay = new PerformanceOverlay();
    performanceOverlay.setVisible(false);

    GLResourceOverlay resourceOverlay = new GLResourceOverlay();
    resourceOverlay.setVisible(false);

    return Arrays.<ILspLayer>asList(performanceOverlay, resourceOverlay);
  }

  @Override
  public boolean canCreateLayers(ILcdModel aILcdModel) {
    return DummyModelFactory.isDummyModel(aILcdModel);
  }
}

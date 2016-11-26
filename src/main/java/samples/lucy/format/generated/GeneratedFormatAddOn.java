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
package samples.lucy.format.generated;

import java.util.Enumeration;

import com.luciad.gui.TLcdAWTUtil;
import com.luciad.lucy.ILcyLucyEnv;
import com.luciad.lucy.addons.ALcyFormatAddOn;
import com.luciad.lucy.format.ALcyFormat;
import com.luciad.lucy.format.TLcySafeGuardFormatWrapper;
import com.luciad.lucy.map.ILcyGenericMapManagerListener;
import com.luciad.lucy.map.TLcyGenericMapManagerEvent;
import com.luciad.lucy.util.ALcyTool;
import com.luciad.lucy.workspace.ILcyWorkspaceManagerListener;
import com.luciad.lucy.workspace.TLcyWorkspaceManagerEvent;
import com.luciad.model.ILcdModel;
import com.luciad.model.TLcd2DBoundsIndexedModel;
import com.luciad.reference.TLcdGeodeticReference;
import com.luciad.shape.shape2D.TLcdLonLatPoint;
import com.luciad.view.ILcdLayer;
import com.luciad.view.ILcdLayered;
import com.luciad.view.ILcdView;

import samples.lucy.format.generated.lightspeed.GeneratedLspFormatAddOn;

/**
 * <p>This sample shows how you can add data to every map that is generated or constructed by the application, so for
 * example not loaded from a file using a model decoder.</p>
 *
 * <p>This sample generates an <code>ILcdModel</code> using the {@link #generateModel()} utility method. For the purpose
 * of the sample, it creates a very basic model with just three points in it, but it could of course be anything. For
 * example the result of some computation, the result of some web service request or a connection to a live feed of
 * tracks. The method <code>addGeneratedDataToEveryMap</code> adds this model to all current and future maps, respecting
 * workspace support. The sample creates a new model instance for every map, but could easily be modified to share a
 * single model across maps, depending on the desired behavior.</p>
 *
 * <p>This add-on also needs {@link GeneratedLspFormatAddOn} as
 * the visualization is currently only provided for Lightspeed maps, which is provided by that add-on.</p>
 */
public class GeneratedFormatAddOn extends ALcyFormatAddOn {
  public GeneratedFormatAddOn() {
    super(ALcyTool.getLongPrefix(GeneratedFormatAddOn.class),
          ALcyTool.getShortPrefix(GeneratedFormatAddOn.class));
  }

  @Override
  public void plugInto(final ILcyLucyEnv aLucyEnv) {
    super.plugInto(aLucyEnv);

    addGeneratedDataToEveryMap(aLucyEnv);
  }

  private void addGeneratedDataToEveryMap(final ILcyLucyEnv aLucyEnv) {
    aLucyEnv.getCombinedMapManager().addMapManagerListener(new ILcyGenericMapManagerListener<ILcdView, ILcdLayer>() {
      @Override
      public void mapManagerChanged(TLcyGenericMapManagerEvent<? extends ILcdView, ? extends ILcdLayer> aMapManagerEvent) {
        final ILcdView view = aMapManagerEvent.getMapComponent().getMainView();
        if (aMapManagerEvent.getId() == TLcyGenericMapManagerEvent.MAP_COMPONENT_ADDED) {
          if (!aLucyEnv.getWorkspaceManager().isDecodingWorkspace()) {
            // A map is added, and we're not decoding a workspace (for example using File|New|Map). Add a generated
            // model to it.
            view.addModel(generateModel());
          } else {
            // During workspace decoding, be sure to let the respective ALcyWorkspaceObjectCodec of GeneratedFormat
            // restore the model, don't interfere with it. This makes sure the layer order is correctly restored, and
            // that layer properties (such as visible) can be restored as well.
            //
            // If a workspace was decoded, and it did not contain our generated model, do add one afterwards.
            // The workspace might not contain our generated model because it was saved before this GeneratedFormatAddOn
            // existed, or because a user removed the layer and then saved the workspace. We want the generated data
            // to be part of every map though, even if the user removed it.
            aLucyEnv.getWorkspaceManager().addWorkspaceManagerListener(new ILcyWorkspaceManagerListener() {
              @Override
              public void workspaceStatusChanged(TLcyWorkspaceManagerEvent aEvent) {
                if (aEvent.getID() == TLcyWorkspaceManagerEvent.WORKSPACE_DECODING_ENDED) {
                  // Clean up this listener
                  aEvent.getWorkspaceManager().removeWorkspaceManagerListener(this);

                  TLcdAWTUtil.invokeAndWait(new Runnable() {
                    @Override
                    public void run() {
                      if (findGeneratedModel(view) == null) {
                        view.addModel(generateModel());
                      }
                    }
                  });
                }
              }
            });
          }
        }
      }
    });
  }

  /**
   * <p>Creates the programmatically created data, that is, it is not read from a file using a model decoder.</p>
   *
   * <p>For the sake of simplicity, a very basic model with three points is used here. This could of course be
   * anything. You might for example want to use a model with {@link com.luciad.datamodel.ILcdDataObject}s
   * and with a {@link com.luciad.model.ILcdDataModelDescriptor}, so it integrates with the table view
   * and other functionality. Please refer to {@link samples.gxy.fundamentals.step2.FlightPlanDataTypes} and
   * related classes for such an example.
   *
   * @return the programmatically created data.
   */
  static ILcdModel generateModel() {
    TLcd2DBoundsIndexedModel model = new TLcd2DBoundsIndexedModel(new TLcdGeodeticReference(), new GeneratedModelDescriptor());
    model.addElement(new TLcdLonLatPoint(10, 10), ILcdModel.NO_EVENT);
    model.addElement(new TLcdLonLatPoint(11, 11), ILcdModel.NO_EVENT);
    model.addElement(new TLcdLonLatPoint(12, 12), ILcdModel.NO_EVENT);
    return model;
  }

  public static boolean isGeneratedModel(ILcdModel aModel) {
    return aModel.getModelDescriptor() instanceof GeneratedModelDescriptor;
  }

  /**
   * Finds the generated model from a given view.
   * @param aView The view to search in.
   * @return <code>null</code>, or the generated model.
   */
  private static ILcdModel findGeneratedModel(ILcdView aView) {
    if (aView instanceof ILcdLayered) {
      Enumeration layers = ((ILcdLayered) aView).layers();
      while (layers.hasMoreElements()) {
        ILcdLayer l = (ILcdLayer) layers.nextElement();
        if (isGeneratedModel(l.getModel())) {
          return l.getModel();
        }
      }
    }
    return null;
  }

  @Override
  protected ALcyFormat createBaseFormat() {
    return new GeneratedFormat(getLucyEnv(), getLongPrefix(), getShortPrefix(), getPreferences());
  }

  @Override
  protected ALcyFormat createFormatWrapper(ALcyFormat aBaseFormat) {
    return new TLcySafeGuardFormatWrapper(aBaseFormat);
  }
}

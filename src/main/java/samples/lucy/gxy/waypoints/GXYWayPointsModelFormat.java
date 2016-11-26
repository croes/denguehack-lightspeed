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
package samples.lucy.gxy.waypoints;

import com.luciad.lucy.ILcyLucyEnv;
import com.luciad.lucy.datatransfer.ALcyLayerSelectionTransferHandler;
import com.luciad.lucy.gui.formatbar.ALcyFormatBarFactory;
import com.luciad.lucy.map.ILcyGXYLayerTypeProvider;
import com.luciad.lucy.util.properties.ALcyProperties;
import com.luciad.lucy.workspace.ALcyWorkspaceObjectCodec;
import com.luciad.view.gxy.ILcdGXYLayer;
import com.luciad.view.gxy.ILcdGXYLayerFactory;

import samples.lucy.fundamentals.waypoints.WayPointLayerSelectionTransferHandler;
import samples.lucy.fundamentals.waypoints.model.WayPointsModelFormat;

/**
 * <p>
 *   Extension of the {@link WayPointsModelFormat} from the fundamentals sample which
 *   adds support for visualization and editing of the data on a GXY view.
 * </p>
 *
 * <p>
 *   The fundamentals sample was focused on how to add editable data on a Lightspeed view.
 *   This sample shows how you can add support for an editable data format on a GXY view.
 * </p>
 */
final class GXYWayPointsModelFormat extends WayPointsModelFormat {

  GXYWayPointsModelFormat(ILcyLucyEnv aLucyEnv,
                          String aLongPrefix,
                          String aShortPrefix,
                          ALcyProperties aProperties) {
    super(aLucyEnv, aLongPrefix, aShortPrefix, aProperties);
  }

  @Override
  protected ILcyGXYLayerTypeProvider createGXYLayerTypeProvider() {
    return new ILcyGXYLayerTypeProvider() {
      @Override
      public int getGXYLayerType(ILcdGXYLayer aGXYLayer) {
        // The TLcySafeGuardFormatWrapper avoids that we need to check whether it is a way points layer
        return ILcyGXYLayerTypeProvider.EDITABLE;
      }
    };
  }

  @Override
  protected ILcdGXYLayerFactory createGXYLayerFactory() {
    return new GXYWayPointsLayerFactory();
  }

  @Override
  protected ALcyLayerSelectionTransferHandler[] createGXYLayerSelectionTransferHandlers() {
    return new ALcyLayerSelectionTransferHandler[]{
        new WayPointLayerSelectionTransferHandler()
    };
  }

  @Override
  protected ALcyWorkspaceObjectCodec[] createGXYLayerWorkspaceCodecs() {
    return new ALcyWorkspaceObjectCodec[]{
        new GXYWayPointsLayerWorkspaceCodec(getLongPrefix(), getShortPrefix(), getGXYLayerFactory())
    };
  }

  @Override
  protected ALcyFormatBarFactory createFormatBarFactory() {
    return new GXYWayPointsFormatBarFactory(getLucyEnv(), getProperties(), getShortPrefix());
  }
}

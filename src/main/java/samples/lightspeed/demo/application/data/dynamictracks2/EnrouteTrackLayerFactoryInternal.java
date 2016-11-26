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
package samples.lightspeed.demo.application.data.dynamictracks2;

import com.luciad.model.ILcd2DBoundsIndexedModel;
import com.luciad.model.ILcdModel;

import samples.lightspeed.demo.application.data.dynamictracks.EnrouteTrackLayerFactory;
import samples.lightspeed.demo.framework.application.Framework;
import samples.lightspeed.demo.framework.data.themes.AbstractTheme;

/**
 * Layer factory for Enroute Track models.
 * <p/>
 * This layer factory can be configured with following properties:
 * <table cellspacing="10">
 * <tr> <td><b>Key</b></td> <td><b>Type</b></td> <td><b>Default Value</b></td>
 * <td><b>Description</b></td> </tr>
 * <tr> <td>label.decluttering</td> <td>boolean</td><td>false</td> <td>Sets whether label
 * decluttering is activated (true) or not (false)</td></tr>
 * <tr> <td>track.icon.size</td> <td>int</td><td>10</td> <td>Sets the size of the track
 * icon</td></tr>
 * <tr> <td>track.icon.outline</td> <td>int,int,int</td><td>255,255,0</td> <td>Specifies the
 * outline
 * color of the track symbols (format: R,G,B)</td></tr>
 * <tr> <td>track.icon.fill</td> <td>int,int,int</td><td>0,0,0</td> <td>Specifies the fill of the
 * track symbols (format: R,G,B)</td></tr>
 * <tr> <td>track.history.size</td> <td>int</td><td>7</td> <td>Sets the size of the history icons
 * corresponding to the track</td></tr>
 * <tr> <td>track.history.outline</td> <td>int,int,int</td><td>0,0,255</td> <td>Specifies the
 * outline color of the history symbols (format: R,G,B)</td></tr>
 * <tr> <td>track.history.fill</td> <td>int,int,int</td><td>255,192,0</td> <td>Specifies the fill
 * color of the history symbols (format: R,G,B)</td></tr>
 * <tr> <td>history.point.count</td> <td>int</td> <td>0</td> <td>Specifies the number of history
 * points that are to be drawn for each track</td></tr>
 * <tr> <td>history.point.interval</td> <td>double</td> <td>0</td> <td>Specifies the spacing
 * between
 * the history points of a track</td></tr>
 * </table>
 */
public class EnrouteTrackLayerFactoryInternal extends EnrouteTrackLayerFactory {

  @Override
  public boolean canCreateLayers(ILcdModel aModel) {
    return aModel instanceof ILcd2DBoundsIndexedModel &&
           aModel.getModelReference() != null &&
           Framework.getInstance().getThemeByClass(DynamicTracksThemeInternal.class) != null;
  }

  @Override
  protected AbstractTheme getTheme() {
    return Framework.getInstance().getThemeByClass(DynamicTracksThemeInternal.class);
  }
}

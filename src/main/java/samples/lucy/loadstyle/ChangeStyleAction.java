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
package samples.lucy.loadstyle;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.Enumeration;

import com.luciad.gui.ALcdAction;
import com.luciad.lucy.ILcyLucyEnv;
import com.luciad.lucy.map.ILcyGenericMapComponent;
import com.luciad.lucy.map.ILcyLayerStyle;
import com.luciad.lucy.map.TLcyCompositeGXYLayerDecoder;
import com.luciad.lucy.map.TLcyCompositeLayerStyleProvider;
import com.luciad.lucy.util.properties.ALcyProperties;
import com.luciad.util.logging.ILcdLogger;
import com.luciad.util.logging.TLcdLoggerFactory;
import com.luciad.view.ILcdLayer;
import com.luciad.view.ILcdLayered;
import com.luciad.view.ILcdView;
import com.luciad.view.gxy.ILcdGXYLayer;
import com.luciad.view.gxy.ILcdGXYView;

/**
 * Action which will look for the world layer and apply a new style on it
 *
 * @since 10.1
 */
public class ChangeStyleAction extends ALcdAction {

  private static final ILcdLogger LOGGER = TLcdLoggerFactory.getLogger(ChangeStyleAction.class.getName());

  private static final String COLOR_KEY = "backgroundColor";
  private static final String STYLE_FILE_KEY = "styleFile";

  private final ILcyGenericMapComponent<? extends ILcdView, ? extends ILcdLayer> fMapComponent;
  private final ALcyProperties fProperties;
  private final String fPrefix;
  private final ILcyLucyEnv fLucyEnv;

  /**
   * Create a new action to change the style of the world layer
   *
   * @param aMapComponent the map component in which this action will search for the world layer
   * @param aProperties   the properties which will be used to retrieve the style file and the
   *                      background color
   * @param aPrefix       the prefix used in the properties file
   * @param aLucyEnv      the Lucy back-end
   */
  public ChangeStyleAction(ILcyGenericMapComponent<? extends ILcdView, ? extends ILcdLayer> aMapComponent,
                           ALcyProperties aProperties,
                           String aPrefix,
                           ILcyLucyEnv aLucyEnv) {
    fMapComponent = aMapComponent;
    fProperties = aProperties;
    fPrefix = aPrefix;
    fLucyEnv = aLucyEnv;
  }

  @Override
  public void actionPerformed(ActionEvent aEvent) {
    ILcdLayer worldLayer = findWorldLayer();
    String styleFile = retrieveStyleFile();
    if (worldLayer != null && styleFile != null) {
      try {
        //create a new layer with the new style. The layer will only be used to retrieve the style from
        ILcdGXYLayer layerWithNewStyle = new TLcyCompositeGXYLayerDecoder(fLucyEnv).decodeGXYLayer(worldLayer.getModel(), styleFile);

        ILcyLayerStyle layerStyle = null;
        //retrieve the style from the new layer
        TLcyCompositeLayerStyleProvider layerStyleProvider = new TLcyCompositeLayerStyleProvider(fLucyEnv);
        if (layerStyleProvider.canGetStyle(layerWithNewStyle)) {
          layerStyle = layerStyleProvider.getStyle(layerWithNewStyle);
        }

        if (layerStyle != null) {
          //now apply the style to the world layer
          if (layerStyleProvider.canApplyStyle(layerStyle, worldLayer)) {
            layerStyleProvider.applyStyle(layerStyle, worldLayer);
          }
        }

        //change the background color
        changeBackGroundColor();

      } catch (IOException e) {
        LOGGER.error("IOException", e);
      }
    }
  }

  private void changeBackGroundColor() {
    ILcdView view = fMapComponent.getMainView();
    Color backGround = retrieveBackgroundColor();
    //we can only change the background of an ILcdGXYView
    if (backGround != null && view instanceof ILcdGXYView) {
      ((ILcdGXYView) view).setBackground(backGround);
    }
  }

  /**
   * Find the world layer, based on the label
   *
   * @return the world layer, or <code>null</code> when the layer cannot be found
   */
  private ILcdLayer findWorldLayer() {
    ILcdView view = fMapComponent.getMainView();
    if (view instanceof ILcdLayered) {
      Enumeration layers = ((ILcdLayered) view).layers();
      while (layers.hasMoreElements()) {
        ILcdLayer layer = (ILcdLayer) layers.nextElement();
        if ("world".equalsIgnoreCase(layer.getLabel())) {
          return layer;
        }
      }
    }
    LOGGER.warn("Could not find world layer in map component " + fMapComponent.getMapName());
    return null;
  }

  private Color retrieveBackgroundColor() {
    return fProperties.getColor(fPrefix + COLOR_KEY, null);
  }

  private String retrieveStyleFile() {
    return fProperties.getString(fPrefix + STYLE_FILE_KEY, null);
  }
}

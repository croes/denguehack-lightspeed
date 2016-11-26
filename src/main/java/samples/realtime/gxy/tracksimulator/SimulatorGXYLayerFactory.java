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
package samples.realtime.gxy.tracksimulator;

import java.awt.Color;

import com.luciad.gui.ILcdIcon;
import com.luciad.gui.TLcdSymbol;
import com.luciad.model.ILcdModel;
import com.luciad.util.TLcdInterval;
import com.luciad.view.gxy.ILcdGXYContext;
import com.luciad.view.gxy.ILcdGXYLayer;
import com.luciad.view.gxy.ILcdGXYLayerFactory;
import com.luciad.view.gxy.TLcdGXYLabelPainter;
import com.luciad.view.gxy.TLcdGXYLayer;
import com.luciad.view.gxy.painter.TLcdGXYIconPainter;

import samples.common.gxy.GXYScaleSupport;
import samples.realtime.common.TimeStampedTrack;

/**
 * This is an implementation of ILcdGXYLayerFactory that is used by the
 * TLcdSimulator to create a different layer for the different models of
 * the simulation.
 * The different layers are represented with a different symbol to easily
 * make a visual distinction between the different layers.
 * If the layers are set to be labeled, the current position of the point will
 * be shown in the label.
 */
public class SimulatorGXYLayerFactory implements ILcdGXYLayerFactory {

  private int fLayerCount = 0;

  public SimulatorGXYLayerFactory() {
  }

  public ILcdGXYLayer createGXYLayer(final ILcdModel aModel) {
    TLcdGXYLayer gxy_layer = new TLcdGXYLayer(aModel);
    gxy_layer.setSelectable(true);
    gxy_layer.setLabeled(true);
    gxy_layer.setEditable(false);
    gxy_layer.setLabelsEditable(true);

    //Don't paint the labels when zoomed out (small scale)
    double screenResolution = -1;
    gxy_layer.setLabelScaleRange(new TLcdInterval(
        GXYScaleSupport.mapScale2InternalScale(1.0 / 50000000.0, screenResolution, null), Double.MAX_VALUE));

    TLcdGXYIconPainter track_painter = new TimeStampedTrackPainter();
    track_painter.setIcon(getSymbol());
    gxy_layer.setGXYPainterProvider(track_painter);
    gxy_layer.setGXYEditorProvider(track_painter);

    //Note that we could as well use a TLcdGXYStampLabelPainter here, offering
    //in place label editing capabilities etc.
    MyLabelPainter label_painter = new MyLabelPainter();
    label_painter.setWithPin(true);
    label_painter.setFilled(true);
    label_painter.setFrame(true);
    label_painter.setBackground(new Color(255, 255, 255, 64));
    gxy_layer.setGXYLabelPainterProvider(label_painter);
    gxy_layer.setGXYLabelEditorProvider(label_painter);

    fLayerCount++;

    return gxy_layer;
  }

  /**
   * This is a simple utility method to return a different symbology for the
   * different layers.
   * @return an icon depending on the layers already created.
   */
  private ILcdIcon getSymbol() {
    if (fLayerCount % 2 == 0) {
      return new TLcdSymbol(TLcdSymbol.FILLED_CIRCLE, 8, Color.red);
    } else {
      return new TLcdSymbol(TLcdSymbol.FILLED_RECT, 8, Color.black);
    }
  }

  private class MyLabelPainter extends TLcdGXYLabelPainter {
    protected String[] retrieveLabels(int aMode, ILcdGXYContext aGXYContext) {
      // We know the models only contain TimeStampedTrack's.
      TimeStampedTrack track = (TimeStampedTrack) getObject();
      return new String[]{"Id = " + track.getID(), "Grounded: " + track.isGrounded()};
    }
  }
}

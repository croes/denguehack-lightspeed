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
package samples.gxy.colormap;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JPanel;

import com.luciad.format.raster.TLcdDMEDModelDecoder;
import com.luciad.gui.swing.TLcdColorMapCustomizer;
import com.luciad.imaging.ALcdImage;
import com.luciad.imaging.operator.ALcdImageOperatorChain;
import com.luciad.imaging.operator.TLcdIndexLookupOp;
import com.luciad.imaging.operator.util.TLcdLookupTable;
import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelDecoder;
import com.luciad.text.TLcdAltitudeFormat;
import com.luciad.util.TLcdAltitudeUnit;
import com.luciad.util.TLcdColorMap;
import com.luciad.util.TLcdInterval;
import com.luciad.util.TLcdSharedBuffer;
import com.luciad.util.logging.ILcdLogger;
import com.luciad.util.logging.TLcdLoggerFactory;
import com.luciad.view.gxy.ILcdGXYLayer;
import com.luciad.view.gxy.ILcdGXYPainterProvider;
import com.luciad.view.gxy.TLcdGXYLayer;
import com.luciad.view.gxy.TLcdMultilevelRasterGXYLayerCodec;
import com.luciad.view.gxy.painter.TLcdGXYImagePainter;

import samples.gxy.common.GXYSample;
import samples.gxy.common.TitledPanel;
import samples.gxy.common.layers.GXYLayerUtil;

/**
 * This sample demonstrates the ability to work with <code>TLcdColorMap</code>,
 * <code>TLcdIndexColorModel</code> and <code>TLcdColorMapCustomizer</code>.
 * It shows you how these objects can be used to let the user easily modify
 * raster coloring.<p>
 * <p/>
 * The sample shows a portion of dmed data with some meaningless coloring. The
 * user can now modify the colors using the
 * <code>TLcdColorMapCustomizer</code> on the right. You can drag the arrows
 * with the mouse, or fill in new values in the text field.<p>
 * <p/>
 * For example, one could try to color anything above 1000m transparent red and
 * everything below 1000m fully transparent. To do this, switch from gradient
 * mode to piece wise constant mode, using the far right 'Gradient toggle'
 * button. Now remove all the levels using the minus button, except for one
 * level. Set this level to 1000m. Then you have to assign a partially
 * transparent red color above 1000m and a full transparent color below 1000m by
 * using the color chooser that appears when you click on the color bar.<p>
 * <p/>
 * Under the hood, this coloring is saved to a style file and then immediately
 * restored. This has no visual impact, but it is meant to show you how such a
 * coloring can be saved and loaded from a style file.
 */
public class MainPanel extends GXYSample {

  private static ILcdLogger sLogger = TLcdLoggerFactory.getLogger(MainPanel.class.getName());

  private ILcdGXYLayer fDMEDGXYLayer = null;
  private ColorCustomizerPanel fColorCustomizer = new ColorCustomizerPanel();

  @Override
  protected JPanel createSettingsPanel() {
    return fColorCustomizer;
  }

  protected void addData() throws IOException {
    super.addData();
    fDMEDGXYLayer = createDMEDLayer("");
    fColorCustomizer.updateCustomizerObject();
    // we intend to change the colors of the layer, so it should not be
    // considered a background layer, as it will be frequently updated.
    GXYLayerUtil.addGXYLayer(getView(), fDMEDGXYLayer, false, false);
    GXYLayerUtil.fitGXYLayer(getView(), fDMEDGXYLayer);
  }

  /**
   * Creates some (meaningless) coloring for DMED elevation data.
   * Note that such a coloring could also be applied to GRIB data (e.g.
   * pressure, temperature, ...). It would however not make sense to apply such
   * coloring to a true satellite picture.
   *
   * @return default color map.
   */
  private TLcdColorMap createDefaultDMEDColorMap() {
    // The levels defined below are in meters, simply because dmed data is
    // stored in meters. If we were using e.g. Grib data, the data could be
    // defined in a custom unit, and a conversion would be necessary.
    // Cfr. TLcdGRIBModelDescriptor.getInternalValue( double )
    return new TLcdColorMap(
        //the absolute min/max for the levels. Data outside this range will not
        //be painted.
        new TLcdInterval(-100, 10000),

        //the levels
        new double[]{
            -100,
            0,
            1,
            250,
            500,
            750,
            1000,
            1500,
            2000,
            2500,
            3000
        },

        //the colors
        new Color[]{
            new Color(0x7ebff0),
            new Color(0xbfeafe),
            new Color(0x9bd090),
            new Color(0x9dc67b),
            new Color(0xced796),
            new Color(0xefe9a8),
            new Color(0xded38d),
            new Color(0xc3a158),
            new Color(0xaa8042),
            new Color(0xbaa787),
            new Color(0xf5ecda),
        }
    );
  }

  private ILcdGXYLayer createDMEDLayer(String aCodeBase) {
    //In this sample, we use a DMED model decoder
    ILcdModelDecoder model_decoder = new TLcdDMEDModelDecoder(TLcdSharedBuffer.getBufferInstance());

    //Use this layer encoder/decoder/factory
    TLcdMultilevelRasterGXYLayerCodec raster_codec = new TLcdMultilevelRasterGXYLayerCodec();

    //This file will be loaded
    String data_file = "Data/Dted/Alps/dmed";

    //This accompanying style file will be used to store the layer style. This
    //file does not exist yet, it will be created in this sample. The extension
    //is taken from the codec, to be sure it is correct.
    String style_file = "dmed_style." + raster_codec.getFileExtension();

    if (model_decoder.canDecodeSource(data_file)) {
      try {
        ILcdModel model = model_decoder.decode(data_file);

        // 1) Create an ILcdGXYLayer for the model that is compatible with the
        //    encoder/decoder.
        //
        // Note that we could perfectly use DMEDMultilevelRasterLayerFactory
        // to create a layer for the dmed model. Everything would remain the
        // same, except that the coloring could not be stored and loaded from a
        // style file.
        ILcdGXYLayer layer = raster_codec.createGXYLayer(model);

        if (layer != null) {
            //1) Write the layer to a style file
            raster_codec.encodeGXYLayer(layer, style_file);

            //2) Read the layer again from the style file. This is just to show
            //   that the coloring can be saved to a style file. It has no visual
            //   impact at all.
            layer = raster_codec.decodeGXYLayer(model, style_file);
          ILcdGXYPainterProvider painter_provider = new TLcdGXYImagePainter();
          ((TLcdGXYLayer) layer).setGXYPainterProvider(painter_provider);
          return layer;
        } else {
          sLogger.error("Creating a layer for model[" + model + "] failed.");
        }
      } catch (IOException e) {
        sLogger.error(e.getMessage(), e);
      }
    } else {
      sLogger.error("Can't load " + data_file);
    }
    return null;
  }

  /**
   * This panel contains a TLcdColorMapCustomizer (the gui to edit the colors)
   * and an apply button. When the apply button is pressed, a new
   * image operator is set to the image painter of the dmed data.
   */
  private class ColorCustomizerPanel extends JPanel implements ActionListener {

    private TLcdColorMapCustomizer fCustomizer = new TLcdColorMapCustomizer();
    private JPanel fColorPanel = new JPanel(new BorderLayout());

    public ColorCustomizerPanel() {
      // Set a format to the customizer that nicely formats the values. The
      // program unit is meters, because dmed data is internally stored in
      // meters (cfr. Grib remark in createDefaultDMEDColorMap). The display
      // unit is also meters, but it could be something else.
      TLcdAltitudeFormat level_format = new TLcdAltitudeFormat(
          TLcdAltitudeUnit.METRE, TLcdAltitudeUnit.METRE
      );
      level_format.setFractionDigits(0);
      fCustomizer.setLevelFormat(level_format);

      JButton apply_button = new JButton("Apply");
      apply_button.addActionListener(this);
      JPanel button_panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
      button_panel.add(apply_button);

//      fColorPanel.add( fCustomizer,  BorderLayout.CENTER );
      fColorPanel.add(button_panel, BorderLayout.SOUTH);

      TitledPanel titled_color_panel = TitledPanel.createTitledPanel(
          "DMED Coloring", fColorPanel, TitledPanel.NORTH
      );

      setLayout(new BorderLayout());
      add(titled_color_panel, BorderLayout.CENTER);
    }

    public void updateCustomizerObject() {
      if (fDMEDGXYLayer != null) {
        // Set a TLcdIndexLookupOp operator to the TLcdGXYImagePainter that reflects
        // the default DMED color mapping.
        TLcdColorMap colorMap = createDefaultDMEDColorMap();
        TLcdGXYImagePainter imagePainter = (TLcdGXYImagePainter) ((TLcdGXYLayer) fDMEDGXYLayer).getGXYPainterProvider();
        imagePainter.setOperatorChain(createIndexLookupOperatorChain(colorMap));

        // Set a clone of the TLcdColorMap to avoid possible interference.
        fCustomizer.setDefaultColorMap((TLcdColorMap) colorMap.clone());
        fCustomizer.setObject(colorMap.clone());
        fCustomizer.setMasterTransparencyVisible(true);
        fCustomizer.invalidate();
        fColorPanel.add(fCustomizer, BorderLayout.CENTER);
        fColorPanel.invalidate();
        MainPanel.this.validate();
        MainPanel.this.repaint();
      }
    }

    public void actionPerformed(ActionEvent e) {
      // Set a new TLcdIndexLookupOp operator to the TLcdGXYImagePainter that reflects
      // the changes made by the user.
      TLcdGXYImagePainter raster_painter = retrieveImagePainter();

      final TLcdColorMap colorMap = (TLcdColorMap) fCustomizer.getColorMap().clone();

      ALcdImageOperatorChain operatorChain = createIndexLookupOperatorChain(colorMap);
      raster_painter.setOperatorChain(operatorChain);

      //notify the view that the fDMEDGXYLayer needs a repaint
      getView().invalidateGXYLayer(fDMEDGXYLayer, true, this, "Changed color model");
    }

    private TLcdGXYImagePainter retrieveImagePainter() {
      //I can only make these casts because a TLcdGXYImagePainter was configured on the layer.
      TLcdGXYImagePainter painter_provider = (TLcdGXYImagePainter) ((TLcdGXYLayer) fDMEDGXYLayer).getGXYPainterProvider();
      return painter_provider;
    }
  }

  /**
   * Create a operator chain with a TLcdIndexLookupOp.
   *
   * @param aColorMap the color;ap used to configure the TLcdIndexLookupOp
   * @return the operator chain
   */
  private ALcdImageOperatorChain createIndexLookupOperatorChain(final TLcdColorMap aColorMap) {
    final TLcdLookupTable lookupTable = TLcdLookupTable.newBuilder()
                                                       .fromColorMap(aColorMap)
                                                       .build();
    return new ALcdImageOperatorChain() {
      @Override
      public ALcdImage apply(ALcdImage aInput) {
        return TLcdIndexLookupOp.indexLookup(aInput, lookupTable);
      }
    };
  }

  // Main method
  public static void main(final String[] aArgs) {
    startSample(MainPanel.class, "Color map");
  }

}


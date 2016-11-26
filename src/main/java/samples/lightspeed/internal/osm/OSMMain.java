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
package samples.lightspeed.internal.osm;

import static com.luciad.gui.TLcdIconFactory.SAVE_ICON;
import static com.luciad.gui.TLcdIconFactory.create;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ImageObserver;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.RenderableImage;
import java.io.File;
import java.io.IOException;
import java.text.AttributedCharacterIterator;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Map;
import java.util.Properties;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;

import com.luciad.format.bingmaps.ELcdBingMapsMapStyle;
import com.luciad.format.shp.TLcdSHPModelDecoder;
import com.luciad.gui.ALcdAction;
import com.luciad.gui.TLcdImageIcon;
import com.luciad.model.ILcdModel;
import com.luciad.reference.TLcdGeodeticReference;
import com.luciad.shape.shape2D.TLcdLonLatBounds;
import com.luciad.util.ILcdStatusListener;
import com.luciad.util.TLcdStatusEvent;
import com.luciad.view.lightspeed.ALspAWTView;
import com.luciad.view.lightspeed.ILspAWTView;
import com.luciad.view.lightspeed.TLspViewPrintSettings;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.raster.TLspRasterLayerBuilder;

import samples.decoder.bingmaps.DataSourceFactory;
import samples.lightspeed.common.FitUtil;
import samples.lightspeed.common.LightspeedSample;
import samples.lightspeed.demo.application.data.osm.OpenStreetMapCoastLayerFactory;
import samples.lightspeed.demo.application.data.osm.OpenStreetMapLayerFactory;
import samples.lightspeed.demo.application.data.osm.OpenStreetMapPlacesLayerFactory;
import samples.lightspeed.demo.application.data.osm.OpenStreetMapPointsLayerFactory;
import samples.lightspeed.demo.application.data.osm.WorldLayerFactory;
import samples.lightspeed.demo.application.data.support.modelfactories.FusionModelFactory;
import samples.lightspeed.printing.PrintAction;
import samples.lightspeed.printing.PrintPreviewAction;

/**
 * @author tomn
 * @since 2013.0
 */
public class OSMMain extends LightspeedSample {


  @Override
  protected ILspAWTView createView() {
    ILspAWTView view = super.createView();
    view.setBackground(Color.lightGray);
    return view;
  }

  @Override
  protected void addData() throws IOException {
    super.addData();

    ILcdModel bing = DataSourceFactory.createDefaultBingModel(ELcdBingMapsMapStyle.AERIAL, this);
    if (bing != null) {
      getView().addLayer(TLspRasterLayerBuilder.newBuilder().model(bing).build());
    }

    getView().addLayer(new WorldLayerFactory().createLayer(
        new TLcdSHPModelDecoder().decode("Data/Shp/World/world.shp")
    ));

    Properties p = createLayerFactoryProperties();
    OpenStreetMapLayerFactory general = new OpenStreetMapLayerFactory();
    general.configure(p);
    OpenStreetMapCoastLayerFactory coast = new OpenStreetMapCoastLayerFactory();
    coast.configure(p);
    OpenStreetMapPlacesLayerFactory places = new OpenStreetMapPlacesLayerFactory();
    places.configure(p);
    OpenStreetMapPointsLayerFactory points = new OpenStreetMapPointsLayerFactory();
    points.configure(p);

    String path = "Data/internal/OpenStreetMap/";
    FusionModelFactory fmf = new FusionModelFactory("fusion");

    ArrayList<ILspLayer> layers = new ArrayList<ILspLayer>();

    layers.addAll(general.createLayers(fmf.createModel(path + "landuse.lfn")));
    layers.addAll(general.createLayers(fmf.createModel(path + "waterways.lfn")));
    layers.addAll(general.createLayers(fmf.createModel(path + "railways.lfn")));
    layers.addAll(general.createLayers(fmf.createModel(path + "roads.lfn")));
    layers.addAll(general.createLayers(fmf.createModel(path + "buildings.lfn")));
    layers.addAll(places.createLayers(fmf.createModel(path + "places.lfn")));
    layers.addAll(points.createLayers(fmf.createModel(path + "pointsofinterest.lfn")));
    layers.addAll(coast.createLayers(fmf.createModel(path + "coastline.lfn")));

    for (ILspLayer l : layers) {
      getView().addLayer(l);
    }

    FitUtil.fitOnBounds(this, new TLcdLonLatBounds(4.69, 50.86, 0.03, 0.03), new TLcdGeodeticReference());
  }

  private Properties createLayerFactoryProperties() {
    Properties p = new Properties();
    p.setProperty("osm_buildings.dataDensity", "DETAIL");
    p.setProperty("osm_buildings.body.minScale", "0.3333");
    p.setProperty("osm_buildings.geometryType", "area");

    p.setProperty("osm_roads.dataDensity", "DETAIL");
    p.setProperty("osm_roads.body.minScale", "0.0005");
    p.setProperty("osm_roads.labeled", "true");
    p.setProperty("osm_roads.geometryType", "line");

    p.setProperty("osm_railways.dataDensity", "DETAIL");
    p.setProperty("osm_railways.body.minScale", "0.004");
    p.setProperty("osm_railways.geometryType", "line");

    p.setProperty("osm_landuse.dataDensity", "DETAIL");
    p.setProperty("osm_landuse.body.minScale", "0.1");
    p.setProperty("osm_landuse.geometryType", "area");

    p.setProperty("osm_waterways.dataDensity", "DETAIL");
    p.setProperty("osm_waterways.body.minScale", "0.004");
    p.setProperty("osm_waterways.geometryType", "line");

    p.setProperty("osm_places.dataDensity", "DETAIL");
    p.setProperty("osm_places.labeled", "true");
    p.setProperty("osm_places.body.maxScale", "0.1");
    p.setProperty("osm_places.label.maxScale", "0.1");
    p.setProperty("osm_places.geometryType", "point");

    p.setProperty("osm_pointsofinterest.dataDensity", "DETAIL");
    p.setProperty("osm_pointsofinterest.body.minScale", "0.3333");
    p.setProperty("osm_pointsofinterest.label.minScale", "0.6666");
    p.setProperty("osm_pointsofinterest.labeled", "true");
    p.setProperty("osm_pointsofinterest.geometryType", "point");

    p.setProperty("osm_coastline.dataDensity", "DETAIL");
    p.setProperty("osm_coastline.body.minScale", "0.005");
    p.setProperty("osm_coastline.geometryType", "line");

    return p;
  }

  @Override
  protected void createGUI() {
    super.createGUI();

    getToolBars()[0].addAction(new PrintPreviewAction(this, getView()));
    getToolBars()[0].addAction(new PrintAction(this, getView()));
    getToolBars()[0].addAction(new ImagePreviewAction());
    getToolBars()[0].addAction(new ImageSaveAction());
  }

  /**
   * Prints the view to a BufferedImage and displays it in a new window.
   */
  private class ImagePreviewAction extends ALcdAction {
    private ImagePreviewAction() {
      super("Immediate print preview", new TLcdImageIcon("images/gui/i16_loopplus.gif"));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      if (getView() instanceof ALspAWTView) {
        ALspAWTView v = (ALspAWTView) getView();

        int scale = 4;
        TLspViewPrintSettings settings = TLspViewPrintSettings.newBuilder()
                                                              .featureScale(1.5 / scale)
                                                              .statusListener(new ILcdStatusListener() {
                                                                @Override
                                                                public void statusChanged(TLcdStatusEvent aStatusEvent) {
                                                                  System.out.println(aStatusEvent.toString());
                                                                }
                                                              })
                                                              .build();

        BufferedImage image = new BufferedImage(
            v.getWidth() * scale, v.getHeight() * scale, BufferedImage.TYPE_INT_RGB
        );
        Graphics2D g = image.createGraphics();
        g.scale(scale, scale);
        v.print(g, settings);
        g.dispose();

        try {
          ImageIO.write(image, "JPEG", new File(System.currentTimeMillis() + ".jpg"));
        } catch (IOException e1) {
          e1.printStackTrace();
        }

        JFrame frame = new JFrame("Preview");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        JLabel label = new JLabel(new ImageIcon(image));
        JScrollPane scroll = new JScrollPane(label);
        scroll.setPreferredSize(new Dimension(800, 600));
        frame.getContentPane().add(scroll);
        frame.pack();
        frame.setVisible(true);
      }
    }
  }

  /**
   * Prints the view to a dummy graphics and saves the tiles as PNG files. If you
   * have ImageMagick, the following command concatenates the tiles into a single
   * large image (tested on Windows):
   *
   *  montage -mode concatenate -tile 16x16 tile*.png result.png
   *
   * Where "16x16" is the size of the tile grid and "result.png" is the output file.
   */
  private class ImageSaveAction extends ALcdAction {

    private static final int SCALE = 16;

    private ImageSaveAction() {
      super("Save tiles (" + SCALE + "x" + SCALE + ")", create(SAVE_ICON));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      if (getView() instanceof ALspAWTView) {
        final ALspAWTView v = (ALspAWTView) getView();

        TLspViewPrintSettings settings = TLspViewPrintSettings.newBuilder()
                                                              .featureScale(1.0 / SCALE)
                                                              .statusListener(new ILcdStatusListener() {
                                                                @Override
                                                                public void statusChanged(TLcdStatusEvent aStatusEvent) {
                                                                  System.out.println(aStatusEvent.toString());
                                                                }
                                                              })
                                                              .build();

        Graphics2D g = new Graphics2D() {
          @Override
          public void draw(Shape s) {
          }

          @Override
          public boolean drawImage(Image img, AffineTransform xform, ImageObserver obs) {
            return false;
          }

          @Override
          public void drawImage(BufferedImage img, BufferedImageOp op, int x, int y) {
          }

          @Override
          public void drawRenderedImage(RenderedImage img, AffineTransform xform) {
          }

          @Override
          public void drawRenderableImage(RenderableImage img, AffineTransform xform) {
          }

          @Override
          public void drawString(String str, int x, int y) {
          }

          @Override
          public void drawString(String str, float x, float y) {
          }

          @Override
          public void drawString(AttributedCharacterIterator iterator, int x, int y) {
          }

          @Override
          public void drawString(AttributedCharacterIterator iterator, float x, float y) {
          }

          @Override
          public void drawGlyphVector(GlyphVector g, float x, float y) {
          }

          @Override
          public void fill(Shape s) {
          }

          @Override
          public boolean hit(Rectangle rect, Shape s, boolean onStroke) {
            return false;
          }

          @Override
          public GraphicsConfiguration getDeviceConfiguration() {
            return null;
          }

          @Override
          public void setComposite(Composite comp) {
          }

          @Override
          public void setPaint(Paint paint) {
          }

          @Override
          public void setStroke(Stroke s) {
          }

          @Override
          public void setRenderingHint(RenderingHints.Key hintKey, Object hintValue) {
          }

          @Override
          public Object getRenderingHint(RenderingHints.Key hintKey) {
            return null;
          }

          @Override
          public void setRenderingHints(Map<?, ?> hints) {
          }

          @Override
          public void addRenderingHints(Map<?, ?> hints) {
          }

          @Override
          public RenderingHints getRenderingHints() {
            return null;
          }

          @Override
          public void translate(int x, int y) {
          }

          @Override
          public void translate(double tx, double ty) {
          }

          @Override
          public void rotate(double theta) {
          }

          @Override
          public void rotate(double theta, double x, double y) {
          }

          private AffineTransform fTransform;

          @Override
          public void scale(double sx, double sy) {
            fTransform = AffineTransform.getScaleInstance(sx, sy);
          }

          @Override
          public void shear(double shx, double shy) {
          }

          @Override
          public void transform(AffineTransform Tx) {
          }

          @Override
          public void setTransform(AffineTransform Tx) {
          }

          @Override
          public AffineTransform getTransform() {
            return fTransform;
          }

          @Override
          public Paint getPaint() {
            return null;
          }

          @Override
          public Composite getComposite() {
            return null;
          }

          @Override
          public void setBackground(Color color) {
          }

          @Override
          public Color getBackground() {
            return null;
          }

          @Override
          public Stroke getStroke() {
            return null;
          }

          @Override
          public void clip(Shape s) {
          }

          @Override
          public FontRenderContext getFontRenderContext() {
            return null;
          }

          @Override
          public Graphics create() {
            return null;
          }

          @Override
          public Color getColor() {
            return null;
          }

          @Override
          public void setColor(Color c) {
          }

          @Override
          public void setPaintMode() {
          }

          @Override
          public void setXORMode(Color c1) {
          }

          @Override
          public Font getFont() {
            return null;
          }

          @Override
          public void setFont(Font font) {
          }

          @Override
          public FontMetrics getFontMetrics(Font f) {
            return null;
          }

          @Override
          public Rectangle getClipBounds() {
            return null;
          }

          @Override
          public void clipRect(int x, int y, int width, int height) {
          }

          @Override
          public void setClip(int x, int y, int width, int height) {
          }

          @Override
          public Shape getClip() {
            return null;
          }

          @Override
          public void setClip(Shape clip) {
          }

          @Override
          public void copyArea(int x, int y, int width, int height, int dx, int dy) {
          }

          @Override
          public void drawLine(int x1, int y1, int x2, int y2) {
          }

          @Override
          public void fillRect(int x, int y, int width, int height) {
          }

          @Override
          public void clearRect(int x, int y, int width, int height) {
          }

          @Override
          public void drawRoundRect(int x, int y, int width, int height, int arcWidth, int arcHeight) {
          }

          @Override
          public void fillRoundRect(int x, int y, int width, int height, int arcWidth, int arcHeight) {
          }

          @Override
          public void drawOval(int x, int y, int width, int height) {
          }

          @Override
          public void fillOval(int x, int y, int width, int height) {
          }

          @Override
          public void drawArc(int x, int y, int width, int height, int startAngle, int arcAngle) {
          }

          @Override
          public void fillArc(int x, int y, int width, int height, int startAngle, int arcAngle) {
          }

          @Override
          public void drawPolyline(int[] xPoints, int[] yPoints, int nPoints) {
          }

          @Override
          public void drawPolygon(int[] xPoints, int[] yPoints, int nPoints) {
          }

          @Override
          public void fillPolygon(int[] xPoints, int[] yPoints, int nPoints) {
          }

          private final DecimalFormat fFmt = new DecimalFormat("00");

          @Override
          public boolean drawImage(Image img, int x, int y, ImageObserver observer) {
            try {
              int ty = y / v.getHeight();
              int tx = x / v.getWidth();
              ImageIO.write(
                  (BufferedImage) img,
                  "PNG",
                  new File("tile_r" + fFmt.format(ty) + "_c" + fFmt.format(tx) + ".png")
              );
            } catch (IOException e1) {
              e1.printStackTrace();
            }
            return false;
          }

          @Override
          public boolean drawImage(Image img, int x, int y, int width, int height, ImageObserver observer) {
            return false;
          }

          @Override
          public boolean drawImage(Image img, int x, int y, Color bgcolor, ImageObserver observer) {
            return false;
          }

          @Override
          public boolean drawImage(Image img, int x, int y, int width, int height, Color bgcolor, ImageObserver observer) {
            return false;
          }

          @Override
          public boolean drawImage(Image img, int dx1, int dy1, int dx2, int dy2, int sx1, int sy1, int sx2, int sy2, ImageObserver observer) {
            return false;
          }

          @Override
          public boolean drawImage(Image img, int dx1, int dy1, int dx2, int dy2, int sx1, int sy1, int sx2, int sy2, Color bgcolor, ImageObserver observer) {
            return false;
          }

          @Override
          public void dispose() {
          }
        };

        g.scale(SCALE, SCALE);
        v.print(g, settings);
        g.dispose();
      }
    }
  }
}

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
package samples.lightspeed.demo.application.data.osm;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ColorModel;
import java.awt.image.Kernel;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.Random;

import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.DocumentLoader;
import org.apache.batik.bridge.GVTBuilder;
import org.apache.batik.bridge.UserAgent;
import org.apache.batik.gvt.CompositeGraphicsNode;
import org.apache.batik.gvt.CompositeShapePainter;
import org.apache.batik.gvt.FillShapePainter;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.gvt.ProxyGraphicsNode;
import org.apache.batik.gvt.ShapeNode;
import org.apache.batik.gvt.ShapePainter;
import org.apache.batik.gvt.StrokeShapePainter;
import org.apache.batik.swing.svg.JSVGComponent;
import org.apache.batik.swing.svg.SVGUserAgent;
import org.w3c.dom.Document;
import org.w3c.dom.svg.SVGDocument;

import com.luciad.gui.ILcdIcon;
import com.luciad.gui.ILcdResizeableIcon;
import com.luciad.io.TLcdIOUtil;
import com.luciad.util.logging.ILcdLogger;
import com.luciad.util.logging.TLcdLoggerFactory;

/**
 *  Uses batik to paint svg icons. Also, specific to OSM: inverts the icon colors, and applies a color
 *  theme if needed. It also paints a glow effect behind the icon if enabled
 */
public class OSMSVGIcon implements ILcdIcon, ILcdResizeableIcon {

  public static final String MIME_TYPE = "image/svg+xml";
  private static ILcdLogger sLogger = TLcdLoggerFactory.getLogger(OSMSVGIcon.class.getName());
  private Rectangle2D fOriginalBounds;
  private int fWidth, fHeight;
  private String fSource;
  private GraphicsNode fGvtRoot;
  private Color fColor;
  private SVGDocument fDocument;
  private boolean fPaintGlow = true;

  static {
    System.setProperty("org.apache.batik.warn_destination", "false");
  }

  /**
   * Constructs a new <code>TLcdSVGIcon</code> object to display an SVG icon.
   * The user should specify the source of the SVG icon.
   *
   * @param aSource the source of the icon to be displayed
   */
  public OSMSVGIcon(String aSource) {
    fSource = aSource;
    loadGVTRoot();
  }

  /**
   * Constructs a new <code>TLcdSVGIcon</code> object to display an SVG icon.
   * The user should specify an SVG document.
   *
   * @param aDocument the SVG document
   */
  public OSMSVGIcon(Document aDocument) {
    fDocument = (SVGDocument) aDocument;
    loadGVTFromDocument(fDocument, new BridgeContext(createUserAgent()));
  }

  /**
   * Constructs a new <code>TLcdSVGIcon</code> object to display an SVG
   * (Scalable Vector Graphics) icon. The displayed icon is retrieved from the
   * given source and its size will be as specified in aWidth and aHeight.
   *
   * @param aSource the source of the icon to be displayed
   * @param aWidth the width of the icon to be displayed
   * @param aHeight the height of the icon to be displayed
   */
  public OSMSVGIcon(String aSource, Color aColor, int aWidth, int aHeight) {
    this(aSource);
    fWidth = aWidth;
    fHeight = aHeight;
    setColor(aColor);
    if (fGvtRoot == null) {
      return;
    }
    updateTransform(fWidth, fHeight);
  }

  /**
   * Copy constructor.
   *
   * @param aSVGIcon the SVG icon to be copied into a new object
   */
  public OSMSVGIcon(OSMSVGIcon aSVGIcon) {
    fSource = aSVGIcon.fSource;
    fOriginalBounds = aSVGIcon.fOriginalBounds;
    fWidth = aSVGIcon.fWidth;
    fHeight = aSVGIcon.fHeight;
    fGvtRoot = aSVGIcon.fGvtRoot;
    fColor = aSVGIcon.fColor;
  }

  public Document getSVGDocument() {
    return fDocument;
  }

  //methods of ILcdIcon
  public void paintIcon(Component aComponent, Graphics aGraphics, int aX, int aY) {

    if (fGvtRoot == null) {
      return;
    }
    paintShape(aGraphics, aX, aY);
  }

  private void paintShape(Graphics aGraphics, int aX, int aY) {
    Stroke prev_stroke = null;
    //    //SVG icon has to compensate for the fact that TLcdGXYIconPainter will move the
//    //icon by half its width and half its height.
    int delta_x = aX + fWidth / 2;
    int delta_y = aY + fHeight / 2;
    aGraphics.translate(delta_x, delta_y);
    if (aGraphics instanceof Graphics2D) {
      ((Graphics2D) aGraphics).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      prev_stroke = ((Graphics2D) aGraphics).getStroke();
    }
    fGvtRoot.paint((Graphics2D) aGraphics);
    if (aGraphics instanceof Graphics2D) {
      ((Graphics2D) aGraphics).setStroke(prev_stroke);
    }
    aGraphics.translate(-delta_x, -delta_y);
  }

  public int getIconWidth() {
    return fWidth;
  }

  public int getIconHeight() {
    return fHeight;
  }

  public Object clone() {
    return new OSMSVGIcon(this);
  }

  /**
   * Specifies the width of the SVG icon to be displayed. Overwrites the width
   * specified in the source file, if any.
   *
   * @see #getIconWidth
   * @param aWidth the widht of the icon to be displayed
   */
  @Override
  public void setIconWidth(int aWidth) {
    if (aWidth == fWidth) {
      return;
    }
    fWidth = aWidth;
    updateTransform(fWidth, fHeight);

  }

  /**
   * Specifies the height of the SVG icon to be displayed. Overwrites the height
   * specified in the source file, if any.
   *
   * @see #getIconHeight
   * @param aHeight
   *          the height of the icon to be displayed
   */
  @Override
  public void setIconHeight(int aHeight) {
    if (aHeight == fHeight) {
      return;
    }
    fHeight = aHeight;
    updateTransform(fWidth, fHeight);

  }

  // public methods

  /**
   * Specifies the width and height of the SVG icon to be displayed. Using this
   * method is more efficient than using both the methods {@link #setIconWidth(int)} and
   * {@link #setIconHeight(int)} one after another.
   *
   * @param aWidth the width of the icon to be displayed
   * @param aHeight the height of the icon to be displayed
   */
  public void setSize(int aWidth, int aHeight) {
    if (aWidth == fWidth && aHeight == fHeight) {
      return;
    }
    fWidth = aWidth;
    fHeight = aHeight;
    updateTransform(fWidth, fHeight);
  }

  /**
   * Returns the color that will be used to paint the SVG icon. This color will
   * be used to draw the outline, as well as to fill the icon. It's impossible
   * to specify different line and fill colors.
   *
   * @see #setColor
   * @return the color used to paint the icon to be displayed
   */
  public Color getColor() {
    return fColor;
  }

  /**
   * Specifies a color to be used to paint the SVG icon. This color will be used
   * as line and fill color.
   *
   * @see #getColor
   * @param aColor the color used to paint the icon to be displayed
   */
  public void setColor(Color aColor) {
    if (aColor == null) {
      if (fColor != null) {
        // restore original
        fColor = null;
        int current_width = fWidth;
        int current_height = fHeight;
        loadGVTRoot();
        setSize(current_width, current_height);
      }
    } else {
      fColor = aColor;
      setColor(fGvtRoot, aColor);
    }
  }

  /**
   * Returns a deep copy of this <code>TLcdSVGIcon</code> object.
   *
   * @return a deep copy of this <code>TLcdSVGIcon</code> object
   */
  public OSMSVGIcon deepCopy() {
    OSMSVGIcon new_instance;
    new_instance = new OSMSVGIcon(this.fSource, this.fColor, this.fWidth, this.fHeight);
    if (fColor != null) {
      new_instance.fColor = new Color(fColor.getRGB());
    }

    return new_instance;
  }

  //private methods
  private void loadGVTRoot() {
    // set context classloader to fix bug with xerces in webstart!!!
    Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
    InputStream inputStream = null;
    try {
      UserAgent userAgent = createUserAgent();
      DocumentLoader loader = new DocumentLoader(userAgent);
      File sourceAsFile = new File(fSource);
      String fileURL;
      if (sourceAsFile.exists()) {
        fileURL = sourceAsFile.toURI().toString();
        inputStream = sourceAsFile.toURI().toURL().openStream();
      } else {
        TLcdIOUtil io_util = new TLcdIOUtil();
        io_util.setSourceName(fSource);
        inputStream = io_util.retrieveInputStream();
        fileURL = io_util.getURL() != null ? io_util.getURL().toString() : "file:" + io_util.getFileName();
      }
      fDocument = (SVGDocument) loader.loadDocument(fileURL, inputStream);
      final BridgeContext context = new BridgeContext(userAgent, loader);
      loadGVTFromDocument(fDocument, context);
    } catch (MalformedURLException e) {
      sLogger.error("Error with file " + fSource + " : " + e.getMessage());
    } catch (IOException e) {
      sLogger.error("Error with file " + fSource + " : " + e.getMessage());
    } finally {
      if (inputStream != null) {
        try {
          inputStream.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
  }

  private UserAgent createUserAgent() {
    MySVGComponent svgComponent = new MySVGComponent();
    UserAgent userAgent = svgComponent.createUserAgent();
    return userAgent;
  }

  private void loadGVTFromDocument(SVGDocument document, BridgeContext aContext) {
    GVTBuilder builder = new GVTBuilder();
    fGvtRoot = builder.build(aContext, document);
    fOriginalBounds = fGvtRoot.getBounds();
    fWidth = (int) Math.round(fOriginalBounds.getWidth());
    fHeight = (int) Math.round(fOriginalBounds.getHeight());
    updateTransform(fWidth, fHeight);
  }

  private void updateTransform(int aWidth, int aHeight) {
    if (fGvtRoot == null) {
      return;
    }
    double scale_x = (double) aWidth / fOriginalBounds.getWidth();
    double scale_y = (double) aHeight / fOriginalBounds.getHeight();
    double delta_x = -(fOriginalBounds.getX() + fOriginalBounds.getWidth() / 2.0);
    double delta_y = -(fOriginalBounds.getY() + fOriginalBounds.getHeight() / 2.0);
    AffineTransform transform = new AffineTransform(
        scale_x, 0,
        0, scale_y,
        scale_x * delta_x, scale_y * delta_y);
    fGvtRoot.setTransform(transform);
  }

  private void setColor(GraphicsNode aNode, Color aColor) {
    if (aNode instanceof CompositeGraphicsNode) {
      CompositeGraphicsNode composite_node = (CompositeGraphicsNode) aNode;
      java.util.List children = composite_node.getChildren();
      for (int i = 0; i < children.size(); i++) {
        setColor((GraphicsNode) children.get(i), aColor);
      }
    } else if (aNode instanceof ShapeNode) {
      ShapePainter shape_painter = ((ShapeNode) aNode).getShapePainter();
      setColorOnPainter(shape_painter, aColor);
    } else if (aNode instanceof ProxyGraphicsNode) {
      setColor(((ProxyGraphicsNode) aNode).getSource(), aColor);
    }
  }

  private void setColorOnPainter(ShapePainter aPainter, Color aColor) {
    if (aPainter instanceof StrokeShapePainter) {
      StrokeShapePainter stroke_painter = (StrokeShapePainter) aPainter;
      if (stroke_painter.getPaintedArea() != null) {
        if (stroke_painter.getPaint().equals(Color.decode("#111111")) ||
            stroke_painter.getPaint().equals(Color.decode("#111")) ||
            stroke_painter.getPaint().equals(Color.decode("#eeeeee")) ||
            stroke_painter.getPaint().equals(Color.decode("#eee"))) {
          stroke_painter.setPaint(null);
        } else if (stroke_painter.getPaint().equals(Color.decode("#ffffff")) ||
                   stroke_painter.getPaint().equals(Color.decode("#fff"))) {
          stroke_painter.setPaint(aColor);
        }
      }
    } else if (aPainter instanceof FillShapePainter) {
      FillShapePainter fill_painter = (FillShapePainter) aPainter;
      if (fill_painter.getPaintedArea() != null) {
        if (fill_painter.getPaint().equals(Color.decode("#111111")) ||
            fill_painter.getPaint().equals(Color.decode("#111")) ||
            fill_painter.getPaint().equals(Color.decode("#eeeeee")) ||
            fill_painter.getPaint().equals(Color.decode("#eee"))) {
          fill_painter.setPaint(null);
        } else if (fill_painter.getPaint().equals(Color.decode("#ffffff")) ||
                   fill_painter.getPaint().equals(Color.decode("#fff"))) {
          fill_painter.setPaint(aColor);
        }
      }
    } else if (aPainter instanceof CompositeShapePainter) {
      CompositeShapePainter composite_painter = (CompositeShapePainter) aPainter;
      for (int i = 0; i < composite_painter.getShapePainterCount(); i++) {
        setColorOnPainter(composite_painter.getShapePainter(i), aColor);
      }
    }
  }

  public boolean isPaintGlow() {
    return fPaintGlow;
  }

  public void setPaintGlow(boolean aPaintGlow) {
    fPaintGlow = aPaintGlow;
  }

  //inner class

  /**
   * We use an extension of <code>JSVGComponent</code> that makes
   * the createUserAgent method public.
   */
  private class MySVGComponent extends JSVGComponent {

    public MySVGComponent() {
      this(null, false, false);
      // SVGUserAgent can be null if not yet defined
      // (which is typically the case for this icon implementation).
      // eventsEnabled and selectableText respectively define
      // whether the SVG graphic tree should be reactive to mouse and key events
      // when displayed in a GUI and whether text should be selectable
      // => both are not needed in this icon implementation, so they are set to false.
    }

    public MySVGComponent(SVGUserAgent aUserAgent,
                          boolean aEventsEnabled,
                          boolean aSelectableText) {
      super(aUserAgent, aEventsEnabled, aSelectableText);
    }

    public UserAgent createUserAgent() {
      return super.createUserAgent();
    }
  }

  private static class GaussianFilter extends ConvolveFilter {

    protected float radius;
    protected Kernel kernel;

    /**
     * Construct a Gaussian filter
     */
    public GaussianFilter() {
      this(2);
    }

    /**
     * Construct a Gaussian filter
     * @param radius blur radius in pixels
     */
    public GaussianFilter(float radius) {
      setRadius(radius);
    }

    /**
     * Set the radius of the kernel, and hence the amount of blur. The bigger the radius, the longer this filter will take.
     * @param radius the radius of the blur in pixels.
     */
    public void setRadius(float radius) {
      this.radius = radius;
      kernel = makeKernel(radius);
    }

    /**
     * Get the radius of the kernel.
     * @return the radius
     */
    public float getRadius() {
      return radius;
    }

    public BufferedImage filter(BufferedImage src, BufferedImage dst) {
      int width = src.getWidth();
      int height = src.getHeight();

      if (dst == null) {
        dst = createCompatibleDestImage(src, null);
      }

      int[] inPixels = new int[width * height];
      int[] outPixels = new int[width * height];
      src.getRGB(0, 0, width, height, inPixels, 0, width);

      convolveAndTranspose(kernel, inPixels, outPixels, width, height, alpha, CLAMP_EDGES);
      convolveAndTranspose(kernel, outPixels, inPixels, height, width, alpha, CLAMP_EDGES);

      dst.setRGB(0, 0, width, height, inPixels, 0, width);
      return dst;
    }

    public static void convolveAndTranspose(Kernel kernel, int[] inPixels, int[] outPixels, int width, int height, boolean alpha, int edgeAction) {
      float[] matrix = kernel.getKernelData(null);
      int cols = kernel.getWidth();
      int cols2 = cols / 2;

      for (int y = 0; y < height; y++) {
        int index = y;
        int ioffset = y * width;
        for (int x = 0; x < width; x++) {
          float r = 0, g = 0, b = 0, a = 0;
          int moffset = cols2;
          for (int col = -cols2; col <= cols2; col++) {
            float f = matrix[moffset + col];

            if (f != 0) {
              int ix = x + col;
              if (ix < 0) {
                if (edgeAction == CLAMP_EDGES) {
                  ix = 0;
                } else if (edgeAction == WRAP_EDGES) {
                  ix = (x + width) % width;
                }
              } else if (ix >= width) {
                if (edgeAction == CLAMP_EDGES) {
                  ix = width - 1;
                } else if (edgeAction == WRAP_EDGES) {
                  ix = (x + width) % width;
                }
              }
              int rgb = inPixels[ioffset + ix];
              a += f * ((rgb >> 24) & 0xff);
              r += f * ((rgb >> 16) & 0xff);
              g += f * ((rgb >> 8) & 0xff);
              b += f * (rgb & 0xff);
            }
          }
          int ia = alpha ? PixelUtils.clamp((int) (a + 0.5)) : 0xff;
          int ir = PixelUtils.clamp((int) (r + 0.5));
          int ig = PixelUtils.clamp((int) (g + 0.5));
          int ib = PixelUtils.clamp((int) (b + 0.5));
          outPixels[index] = (ia << 24) | (ir << 16) | (ig << 8) | ib;
          index += height;
        }
      }
    }

    /**
     * Make a Gaussian blur kernel.
     */
    public static Kernel makeKernel(float radius) {
      int r = (int) Math.ceil(radius);
      int rows = r * 2 + 1;
      float[] matrix = new float[rows];
      float sigma = radius / 3;
      float sigma22 = 2 * sigma * sigma;
      float sigmaPi2 = 2 * ImageMath.PI * sigma;
      float sqrtSigmaPi2 = (float) Math.sqrt(sigmaPi2);
      float radius2 = radius * radius;
      float total = 0;
      int index = 0;
      for (int row = -r; row <= r; row++) {
        float distance = row * row;
        if (distance > radius2) {
          matrix[index] = 0;
        } else {
          matrix[index] = (float) Math.exp(-(distance) / sigma22) / sqrtSigmaPi2;
        }
        total += matrix[index];
        index++;
      }
      for (int i = 0; i < rows; i++) {
        matrix[i] /= total;
      }

      return new Kernel(rows, 1, matrix);
    }

    public String toString() {
      return "Blur/Gaussian Blur...";
    }
  }

  private static class ConvolveFilter implements BufferedImageOp {

    public static int ZERO_EDGES = 0;
    public static int CLAMP_EDGES = 1;
    public static int WRAP_EDGES = 2;

    protected Kernel kernel = null;
    public boolean alpha = true;
    private int edgeAction = CLAMP_EDGES;

    /**
     * Construct a filter with a null kernel. This is only useful if you're going to change the kernel later on.
     */
    public ConvolveFilter() {
      this(new float[9]);
    }

    /**
     * Construct a filter with the given 3x3 kernel.
     * @param matrix an array of 9 floats containing the kernel
     */
    public ConvolveFilter(float[] matrix) {
      this(new Kernel(3, 3, matrix));
    }

    /**
     * Construct a filter with the given kernel.
     * @param rows  the number of rows in the kernel
     * @param cols  the number of columns in the kernel
     * @param matrix  an array of rows*cols floats containing the kernel
     */
    public ConvolveFilter(int rows, int cols, float[] matrix) {
      this(new Kernel(cols, rows, matrix));
    }

    /**
     * Construct a filter with the given 3x3 kernel.
     * @param kernel an array of 9 floats containing the kernel
     */
    public ConvolveFilter(Kernel kernel) {
      this.kernel = kernel;
    }

    public void setKernel(Kernel kernel) {
      this.kernel = kernel;
    }

    public Kernel getKernel() {
      return kernel;
    }

    public void setEdgeAction(int edgeAction) {
      this.edgeAction = edgeAction;
    }

    public int getEdgeAction() {
      return edgeAction;
    }

    public BufferedImage filter(BufferedImage src, BufferedImage dst) {
      int width = src.getWidth();
      int height = src.getHeight();

      if (dst == null) {
        dst = createCompatibleDestImage(src, null);
      }

      int[] inPixels = new int[width * height];
      int[] outPixels = new int[width * height];
      getRGB(src, 0, 0, width, height, inPixels);

      convolve(kernel, inPixels, outPixels, width, height, alpha, edgeAction);

      setRGB(dst, 0, 0, width, height, outPixels);
      return dst;
    }

    /**
     * A convenience method for getting ARGB pixels from an image. This tries to avoid the performance
     * penalty of BufferedImage.getRGB unmanaging the image.
     */
    public int[] getRGB(BufferedImage image, int x, int y, int width, int height, int[] pixels) {
      int type = image.getType();
      if (type == BufferedImage.TYPE_INT_ARGB || type == BufferedImage.TYPE_INT_RGB) {
        return (int[]) image.getRaster().getDataElements(x, y, width, height, pixels);
      }
      return image.getRGB(x, y, width, height, pixels, 0, width);
    }

    /**
     * A convenience method for setting ARGB pixels in an image. This tries to avoid the performance
     * penalty of BufferedImage.setRGB unmanaging the image.
     */
    public void setRGB(BufferedImage image, int x, int y, int width, int height, int[] pixels) {
      int type = image.getType();
      if (type == BufferedImage.TYPE_INT_ARGB || type == BufferedImage.TYPE_INT_RGB) {
        image.getRaster().setDataElements(x, y, width, height, pixels);
      } else {
        image.setRGB(x, y, width, height, pixels, 0, width);
      }
    }

    public BufferedImage createCompatibleDestImage(BufferedImage src, ColorModel dstCM) {
      if (dstCM == null) {
        dstCM = src.getColorModel();
      }
      return new BufferedImage(dstCM, dstCM.createCompatibleWritableRaster(src.getWidth(), src.getHeight()), dstCM.isAlphaPremultiplied(), null);
    }

    public Rectangle2D getBounds2D(BufferedImage src) {
      return new Rectangle(0, 0, src.getWidth(), src.getHeight());
    }

    public Point2D getPoint2D(Point2D srcPt, Point2D dstPt) {
      if (dstPt == null) {
        dstPt = new Point2D.Double();
      }
      dstPt.setLocation(srcPt.getX(), srcPt.getY());
      return dstPt;
    }

    public RenderingHints getRenderingHints() {
      return null;
    }

    public static void convolve(Kernel kernel, int[] inPixels, int[] outPixels, int width, int height, int edgeAction) {
      convolve(kernel, inPixels, outPixels, width, height, true, edgeAction);
    }

    public static void convolve(Kernel kernel, int[] inPixels, int[] outPixels, int width, int height, boolean alpha, int edgeAction) {
      if (kernel.getHeight() == 1) {
        convolveH(kernel, inPixels, outPixels, width, height, alpha, edgeAction);
      } else if (kernel.getWidth() == 1) {
        convolveV(kernel, inPixels, outPixels, width, height, alpha, edgeAction);
      } else {
        convolveHV(kernel, inPixels, outPixels, width, height, alpha, edgeAction);
      }
    }

    /**
     * Convolve with a 2D kernel
     */
    public static void convolveHV(Kernel kernel, int[] inPixels, int[] outPixels, int width, int height, boolean alpha, int edgeAction) {
      int index = 0;
      float[] matrix = kernel.getKernelData(null);
      int rows = kernel.getHeight();
      int cols = kernel.getWidth();
      int rows2 = rows / 2;
      int cols2 = cols / 2;

      for (int y = 0; y < height; y++) {
        for (int x = 0; x < width; x++) {
          float r = 0, g = 0, b = 0, a = 0;

          for (int row = -rows2; row <= rows2; row++) {
            int iy = y + row;
            int ioffset;
            if (0 <= iy && iy < height) {
              ioffset = iy * width;
            } else if (edgeAction == CLAMP_EDGES) {
              ioffset = y * width;
            } else if (edgeAction == WRAP_EDGES) {
              ioffset = ((iy + height) % height) * width;
            } else {
              continue;
            }
            int moffset = cols * (row + rows2) + cols2;
            for (int col = -cols2; col <= cols2; col++) {
              float f = matrix[moffset + col];

              if (f != 0) {
                int ix = x + col;
                if (!(0 <= ix && ix < width)) {
                  if (edgeAction == CLAMP_EDGES) {
                    ix = x;
                  } else if (edgeAction == WRAP_EDGES) {
                    ix = (x + width) % width;
                  } else {
                    continue;
                  }
                }
                int rgb = inPixels[ioffset + ix];
                a += f * ((rgb >> 24) & 0xff);
                r += f * ((rgb >> 16) & 0xff);
                g += f * ((rgb >> 8) & 0xff);
                b += f * (rgb & 0xff);
              }
            }
          }
          int ia = alpha ? PixelUtils.clamp((int) (a + 0.5)) : 0xff;
          int ir = PixelUtils.clamp((int) (r + 0.5));
          int ig = PixelUtils.clamp((int) (g + 0.5));
          int ib = PixelUtils.clamp((int) (b + 0.5));
          outPixels[index++] = (ia << 24) | (ir << 16) | (ig << 8) | ib;
        }
      }
    }

    /**
     * Convolve with a kernel consisting of one row
     */
    public static void convolveH(Kernel kernel, int[] inPixels, int[] outPixels, int width, int height, boolean alpha, int edgeAction) {
      int index = 0;
      float[] matrix = kernel.getKernelData(null);
      int cols = kernel.getWidth();
      int cols2 = cols / 2;

      for (int y = 0; y < height; y++) {
        int ioffset = y * width;
        for (int x = 0; x < width; x++) {
          float r = 0, g = 0, b = 0, a = 0;
          int moffset = cols2;
          for (int col = -cols2; col <= cols2; col++) {
            float f = matrix[moffset + col];

            if (f != 0) {
              int ix = x + col;
              if (ix < 0) {
                if (edgeAction == CLAMP_EDGES) {
                  ix = 0;
                } else if (edgeAction == WRAP_EDGES) {
                  ix = (x + width) % width;
                }
              } else if (ix >= width) {
                if (edgeAction == CLAMP_EDGES) {
                  ix = width - 1;
                } else if (edgeAction == WRAP_EDGES) {
                  ix = (x + width) % width;
                }
              }
              int rgb = inPixels[ioffset + ix];
              a += f * ((rgb >> 24) & 0xff);
              r += f * ((rgb >> 16) & 0xff);
              g += f * ((rgb >> 8) & 0xff);
              b += f * (rgb & 0xff);
            }
          }
          int ia = alpha ? PixelUtils.clamp((int) (a + 0.5)) : 0xff;
          int ir = PixelUtils.clamp((int) (r + 0.5));
          int ig = PixelUtils.clamp((int) (g + 0.5));
          int ib = PixelUtils.clamp((int) (b + 0.5));
          outPixels[index++] = (ia << 24) | (ir << 16) | (ig << 8) | ib;
        }
      }
    }

    /**
     * Convolve with a kernel consisting of one column
     */
    public static void convolveV(Kernel kernel, int[] inPixels, int[] outPixels, int width, int height, boolean alpha, int edgeAction) {
      int index = 0;
      float[] matrix = kernel.getKernelData(null);
      int rows = kernel.getHeight();
      int rows2 = rows / 2;

      for (int y = 0; y < height; y++) {
        for (int x = 0; x < width; x++) {
          float r = 0, g = 0, b = 0, a = 0;

          for (int row = -rows2; row <= rows2; row++) {
            int iy = y + row;
            int ioffset;
            if (iy < 0) {
              if (edgeAction == CLAMP_EDGES) {
                ioffset = 0;
              } else if (edgeAction == WRAP_EDGES) {
                ioffset = ((y + height) % height) * width;
              } else {
                ioffset = iy * width;
              }
            } else if (iy >= height) {
              if (edgeAction == CLAMP_EDGES) {
                ioffset = (height - 1) * width;
              } else if (edgeAction == WRAP_EDGES) {
                ioffset = ((y + height) % height) * width;
              } else {
                ioffset = iy * width;
              }
            } else {
              ioffset = iy * width;
            }

            float f = matrix[row + rows2];

            if (f != 0) {
              int rgb = inPixels[ioffset + x];
              a += f * ((rgb >> 24) & 0xff);
              r += f * ((rgb >> 16) & 0xff);
              g += f * ((rgb >> 8) & 0xff);
              b += f * (rgb & 0xff);
            }
          }
          int ia = alpha ? PixelUtils.clamp((int) (a + 0.5)) : 0xff;
          int ir = PixelUtils.clamp((int) (r + 0.5));
          int ig = PixelUtils.clamp((int) (g + 0.5));
          int ib = PixelUtils.clamp((int) (b + 0.5));
          outPixels[index++] = (ia << 24) | (ir << 16) | (ig << 8) | ib;
        }
      }
    }

    public String toString() {
      return "Blur/Convolve...";
    }
  }

  private static class PixelUtils {

    public final static int REPLACE = 0;
    public final static int NORMAL = 1;
    public final static int MIN = 2;
    public final static int MAX = 3;
    public final static int ADD = 4;
    public final static int SUBTRACT = 5;
    public final static int DIFFERENCE = 6;
    public final static int MULTIPLY = 7;
    public final static int HUE = 8;
    public final static int SATURATION = 9;
    public final static int VALUE = 10;
    public final static int COLOR = 11;
    public final static int SCREEN = 12;
    public final static int AVERAGE = 13;
    public final static int OVERLAY = 14;
    public final static int CLEAR = 15;
    public final static int EXCHANGE = 16;
    public final static int DISSOLVE = 17;
    public final static int DST_IN = 18;
    public final static int ALPHA = 19;
    public final static int ALPHA_TO_GRAY = 20;

    private static Random randomGenerator = new Random();

    /**
     * Clamp a value to the range 0..255
     */
    public static int clamp(int c) {
      if (c < 0) {
        return 0;
      }
      if (c > 255) {
        return 255;
      }
      return c;
    }

    public static int interpolate(int v1, int v2, float f) {
      return clamp((int) (v1 + f * (v2 - v1)));
    }

    public static int brightness(int rgb) {
      int r = (rgb >> 16) & 0xff;
      int g = (rgb >> 8) & 0xff;
      int b = rgb & 0xff;
      return (r + g + b) / 3;
    }

    public static boolean nearColors(int rgb1, int rgb2, int tolerance) {
      int r1 = (rgb1 >> 16) & 0xff;
      int g1 = (rgb1 >> 8) & 0xff;
      int b1 = rgb1 & 0xff;
      int r2 = (rgb2 >> 16) & 0xff;
      int g2 = (rgb2 >> 8) & 0xff;
      int b2 = rgb2 & 0xff;
      return Math.abs(r1 - r2) <= tolerance && Math.abs(g1 - g2) <= tolerance && Math.abs(b1 - b2) <= tolerance;
    }

    private final static float hsb1[] = new float[3];//FIXME-not thread safe
    private final static float hsb2[] = new float[3];//FIXME-not thread safe

    // Return rgb1 painted onto rgb2
    public static int combinePixels(int rgb1, int rgb2, int op) {
      return combinePixels(rgb1, rgb2, op, 0xff);
    }

    public static int combinePixels(int rgb1, int rgb2, int op, int extraAlpha, int channelMask) {
      return (rgb2 & ~channelMask) | combinePixels(rgb1 & channelMask, rgb2, op, extraAlpha);
    }

    public static int combinePixels(int rgb1, int rgb2, int op, int extraAlpha) {
      if (op == REPLACE) {
        return rgb1;
      }
      int a1 = (rgb1 >> 24) & 0xff;
      int r1 = (rgb1 >> 16) & 0xff;
      int g1 = (rgb1 >> 8) & 0xff;
      int b1 = rgb1 & 0xff;
      int a2 = (rgb2 >> 24) & 0xff;
      int r2 = (rgb2 >> 16) & 0xff;
      int g2 = (rgb2 >> 8) & 0xff;
      int b2 = rgb2 & 0xff;

      switch (op) {
      case NORMAL:
        break;
      case MIN:
        r1 = Math.min(r1, r2);
        g1 = Math.min(g1, g2);
        b1 = Math.min(b1, b2);
        break;
      case MAX:
        r1 = Math.max(r1, r2);
        g1 = Math.max(g1, g2);
        b1 = Math.max(b1, b2);
        break;
      case ADD:
        r1 = clamp(r1 + r2);
        g1 = clamp(g1 + g2);
        b1 = clamp(b1 + b2);
        break;
      case SUBTRACT:
        r1 = clamp(r2 - r1);
        g1 = clamp(g2 - g1);
        b1 = clamp(b2 - b1);
        break;
      case DIFFERENCE:
        r1 = clamp(Math.abs(r1 - r2));
        g1 = clamp(Math.abs(g1 - g2));
        b1 = clamp(Math.abs(b1 - b2));
        break;
      case MULTIPLY:
        r1 = clamp(r1 * r2 / 255);
        g1 = clamp(g1 * g2 / 255);
        b1 = clamp(b1 * b2 / 255);
        break;
      case DISSOLVE:
        if ((randomGenerator.nextInt() & 0xff) <= a1) {
          r1 = r2;
          g1 = g2;
          b1 = b2;
        }
        break;
      case AVERAGE:
        r1 = (r1 + r2) / 2;
        g1 = (g1 + g2) / 2;
        b1 = (b1 + b2) / 2;
        break;
      case HUE:
      case SATURATION:
      case VALUE:
      case COLOR:
        Color.RGBtoHSB(r1, g1, b1, hsb1);
        Color.RGBtoHSB(r2, g2, b2, hsb2);
        switch (op) {
        case HUE:
          hsb2[0] = hsb1[0];
          break;
        case SATURATION:
          hsb2[1] = hsb1[1];
          break;
        case VALUE:
          hsb2[2] = hsb1[2];
          break;
        case COLOR:
          hsb2[0] = hsb1[0];
          hsb2[1] = hsb1[1];
          break;
        }
        rgb1 = Color.HSBtoRGB(hsb2[0], hsb2[1], hsb2[2]);
        r1 = (rgb1 >> 16) & 0xff;
        g1 = (rgb1 >> 8) & 0xff;
        b1 = rgb1 & 0xff;
        break;
      case SCREEN:
        r1 = 255 - ((255 - r1) * (255 - r2)) / 255;
        g1 = 255 - ((255 - g1) * (255 - g2)) / 255;
        b1 = 255 - ((255 - b1) * (255 - b2)) / 255;
        break;
      case OVERLAY:
        int m, s;
        s = 255 - ((255 - r1) * (255 - r2)) / 255;
        m = r1 * r2 / 255;
        r1 = (s * r1 + m * (255 - r1)) / 255;
        s = 255 - ((255 - g1) * (255 - g2)) / 255;
        m = g1 * g2 / 255;
        g1 = (s * g1 + m * (255 - g1)) / 255;
        s = 255 - ((255 - b1) * (255 - b2)) / 255;
        m = b1 * b2 / 255;
        b1 = (s * b1 + m * (255 - b1)) / 255;
        break;
      case CLEAR:
        r1 = g1 = b1 = 0xff;
        break;
      case DST_IN:
        r1 = clamp((r2 * a1) / 255);
        g1 = clamp((g2 * a1) / 255);
        b1 = clamp((b2 * a1) / 255);
        a1 = clamp((a2 * a1) / 255);
        return (a1 << 24) | (r1 << 16) | (g1 << 8) | b1;
      case ALPHA:
        a1 = a1 * a2 / 255;
        return (a1 << 24) | (r2 << 16) | (g2 << 8) | b2;
      case ALPHA_TO_GRAY:
        int na = 255 - a1;
        return (a1 << 24) | (na << 16) | (na << 8) | na;
      }
      if (extraAlpha != 0xff || a1 != 0xff) {
        a1 = a1 * extraAlpha / 255;
        int a3 = (255 - a1) * a2 / 255;
        r1 = clamp((r1 * a1 + r2 * a3) / 255);
        g1 = clamp((g1 * a1 + g2 * a3) / 255);
        b1 = clamp((b1 * a1 + b2 * a3) / 255);
        a1 = clamp(a1 + a3);
      }
      return (a1 << 24) | (r1 << 16) | (g1 << 8) | b1;
    }

  }

  private static class ImageMath {

    public final static float PI = (float) Math.PI;
    public final static float HALF_PI = (float) Math.PI / 2.0f;
    public final static float QUARTER_PI = (float) Math.PI / 4.0f;
    public final static float TWO_PI = (float) Math.PI * 2.0f;

    /**
     * Apply a bias to a number in the unit interval, moving numbers towards 0 or 1
     * according to the bias parameter.
     * @param a the number to bias
     * @param b the bias parameter. 0.5 means no change, smaller values bias towards 0, larger towards 1.
     * @return the output value
     */
    public static float bias(float a, float b) {
      //		return (float)Math.pow(a, Math.log(b) / Math.log(0.5));
      return a / ((1.0f / b - 2) * (1.0f - a) + 1);
    }

    /**
     * A variant of the gamma function.
     * @param a the number to apply gain to
     * @param b the gain parameter. 0.5 means no change, smaller values reduce gain, larger values increase gain.
     * @return the output value
     */
    public static float gain(float a, float b) {
  /*
      float p = (float)Math.log(1.0 - b) / (float)Math.log(0.5);

  		if (a < .001)
  			return 0.0f;
  		else if (a > .999)
  			return 1.0f;
  		if (a < 0.5)
  			return (float)Math.pow(2 * a, p) / 2;
  		else
  			return 1.0f - (float)Math.pow(2 * (1. - a), p) / 2;
  */
      float c = (1.0f / b - 2.0f) * (1.0f - 2.0f * a);
      if (a < 0.5) {
        return a / (c + 1.0f);
      } else {
        return (c - a) / (c - 1.0f);
      }
    }

    /**
     * The step function. Returns 0 below a threshold, 1 above.
     * @param a the threshold position
     * @param x the input parameter
     * @return the output value - 0 or 1
     */
    public static float step(float a, float x) {
      return (x < a) ? 0.0f : 1.0f;
    }

    /**
     * The pulse function. Returns 1 between two thresholds, 0 outside.
     * @param a the lower threshold position
     * @param b the upper threshold position
     * @param x the input parameter
     * @return the output value - 0 or 1
     */
    public static float pulse(float a, float b, float x) {
      return (x < a || x >= b) ? 0.0f : 1.0f;
    }

    /**
     * A smoothed pulse function. A cubic function is used to smooth the step between two thresholds.
     * @param a1 the lower threshold position for the start of the pulse
     * @param a2 the upper threshold position for the start of the pulse
     * @param b1 the lower threshold position for the end of the pulse
     * @param b2 the upper threshold position for the end of the pulse
     * @param x the input parameter
     * @return the output value
     */
    public static float smoothPulse(float a1, float a2, float b1, float b2, float x) {
      if (x < a1 || x >= b2) {
        return 0;
      }
      if (x >= a2) {
        if (x < b1) {
          return 1.0f;
        }
        x = (x - b1) / (b2 - b1);
        return 1.0f - (x * x * (3.0f - 2.0f * x));
      }
      x = (x - a1) / (a2 - a1);
      return x * x * (3.0f - 2.0f * x);
    }

    /**
     * A smoothed step function. A cubic function is used to smooth the step between two thresholds.
     * @param a the lower threshold position
     * @param b the upper threshold position
     * @param x the input parameter
     * @return the output value
     */
    public static float smoothStep(float a, float b, float x) {
      if (x < a) {
        return 0;
      }
      if (x >= b) {
        return 1;
      }
      x = (x - a) / (b - a);
      return x * x * (3 - 2 * x);
    }

    /**
     * A "circle up" function. Returns y on a unit circle given 1-x. Useful for forming bevels.
     * @param x the input parameter in the range 0..1
     * @return the output value
     */
    public static float circleUp(float x) {
      x = 1 - x;
      return (float) Math.sqrt(1 - x * x);
    }

    /**
     * A "circle down" function. Returns 1-y on a unit circle given x. Useful for forming bevels.
     * @param x the input parameter in the range 0..1
     * @return the output value
     */
    public static float circleDown(float x) {
      return 1.0f - (float) Math.sqrt(1 - x * x);
    }

    /**
     * Clamp a value to an interval.
     * @param a the lower clamp threshold
     * @param b the upper clamp threshold
     * @param x the input parameter
     * @return the clamped value
     */
    public static float clamp(float x, float a, float b) {
      return (x < a) ? a : (x > b) ? b : x;
    }

    /**
     * Clamp a value to an interval.
     * @param a the lower clamp threshold
     * @param b the upper clamp threshold
     * @param x the input parameter
     * @return the clamped value
     */
    public static int clamp(int x, int a, int b) {
      return (x < a) ? a : (x > b) ? b : x;
    }

    /**
     * Return a mod b. This differs from the % operator with respect to negative numbers.
     * @param a the dividend
     * @param b the divisor
     * @return a mod b
     */
    public static double mod(double a, double b) {
      int n = (int) (a / b);

      a -= n * b;
      if (a < 0) {
        return a + b;
      }
      return a;
    }

    /**
     * Return a mod b. This differs from the % operator with respect to negative numbers.
     * @param a the dividend
     * @param b the divisor
     * @return a mod b
     */
    public static float mod(float a, float b) {
      int n = (int) (a / b);

      a -= n * b;
      if (a < 0) {
        return a + b;
      }
      return a;
    }

    /**
     * Return a mod b. This differs from the % operator with respect to negative numbers.
     * @param a the dividend
     * @param b the divisor
     * @return a mod b
     */
    public static int mod(int a, int b) {
      int n = a / b;

      a -= n * b;
      if (a < 0) {
        return a + b;
      }
      return a;
    }

    /**
     * The triangle function. Returns a repeating triangle shape in the range 0..1 with wavelength 1.0
     * @param x the input parameter
     * @return the output value
     */
    public static float triangle(float x) {
      float r = mod(x, 1.0f);
      return 2.0f * (r < 0.5 ? r : 1 - r);
    }

    /**
     * Linear interpolation.
     * @param t the interpolation parameter
     * @param a the lower interpolation range
     * @param b the upper interpolation range
     * @return the interpolated value
     */
    public static float lerp(float t, float a, float b) {
      return a + t * (b - a);
    }

    /**
     * Linear interpolation.
     * @param t the interpolation parameter
     * @param a the lower interpolation range
     * @param b the upper interpolation range
     * @return the interpolated value
     */
    public static int lerp(float t, int a, int b) {
      return (int) (a + t * (b - a));
    }

    /**
     * Linear interpolation of ARGB values.
     * @param t the interpolation parameter
     * @param rgb1 the lower interpolation range
     * @param rgb2 the upper interpolation range
     * @return the interpolated value
     */
    public static int mixColors(float t, int rgb1, int rgb2) {
      int a1 = (rgb1 >> 24) & 0xff;
      int r1 = (rgb1 >> 16) & 0xff;
      int g1 = (rgb1 >> 8) & 0xff;
      int b1 = rgb1 & 0xff;
      int a2 = (rgb2 >> 24) & 0xff;
      int r2 = (rgb2 >> 16) & 0xff;
      int g2 = (rgb2 >> 8) & 0xff;
      int b2 = rgb2 & 0xff;
      a1 = lerp(t, a1, a2);
      r1 = lerp(t, r1, r2);
      g1 = lerp(t, g1, g2);
      b1 = lerp(t, b1, b2);
      return (a1 << 24) | (r1 << 16) | (g1 << 8) | b1;
    }

    /**
     * Bilinear interpolation of ARGB values.
     * @param x the X interpolation parameter 0..1
     * @param y the y interpolation parameter 0..1
     * @param p array of four ARGB values in the order NW, NE, SW, SE
     * @return the interpolated value
     */
    public static int bilinearInterpolate(float x, float y, int[] p) {
      float m0, m1;
      int a0 = (p[0] >> 24) & 0xff;
      int r0 = (p[0] >> 16) & 0xff;
      int g0 = (p[0] >> 8) & 0xff;
      int b0 = p[0] & 0xff;
      int a1 = (p[1] >> 24) & 0xff;
      int r1 = (p[1] >> 16) & 0xff;
      int g1 = (p[1] >> 8) & 0xff;
      int b1 = p[1] & 0xff;
      int a2 = (p[2] >> 24) & 0xff;
      int r2 = (p[2] >> 16) & 0xff;
      int g2 = (p[2] >> 8) & 0xff;
      int b2 = p[2] & 0xff;
      int a3 = (p[3] >> 24) & 0xff;
      int r3 = (p[3] >> 16) & 0xff;
      int g3 = (p[3] >> 8) & 0xff;
      int b3 = p[3] & 0xff;

      float cx = 1.0f - x;
      float cy = 1.0f - y;

      m0 = cx * a0 + x * a1;
      m1 = cx * a2 + x * a3;
      int a = (int) (cy * m0 + y * m1);

      m0 = cx * r0 + x * r1;
      m1 = cx * r2 + x * r3;
      int r = (int) (cy * m0 + y * m1);

      m0 = cx * g0 + x * g1;
      m1 = cx * g2 + x * g3;
      int g = (int) (cy * m0 + y * m1);

      m0 = cx * b0 + x * b1;
      m1 = cx * b2 + x * b3;
      int b = (int) (cy * m0 + y * m1);

      return (a << 24) | (r << 16) | (g << 8) | b;
    }

    /**
     * Return the NTSC gray level of an RGB value.
     * @param rgb the input pixel
     * @return the gray level (0-255)
     */
    public static int brightnessNTSC(int rgb) {
      int r = (rgb >> 16) & 0xff;
      int g = (rgb >> 8) & 0xff;
      int b = rgb & 0xff;
      return (int) (r * 0.299f + g * 0.587f + b * 0.114f);
    }

    // Catmull-Rom splines
    private final static float m00 = -0.5f;
    private final static float m01 = 1.5f;
    private final static float m02 = -1.5f;
    private final static float m03 = 0.5f;
    private final static float m10 = 1.0f;
    private final static float m11 = -2.5f;
    private final static float m12 = 2.0f;
    private final static float m13 = -0.5f;
    private final static float m20 = -0.5f;
    private final static float m21 = 0.0f;
    private final static float m22 = 0.5f;
    private final static float m23 = 0.0f;
    private final static float m30 = 0.0f;
    private final static float m31 = 1.0f;
    private final static float m32 = 0.0f;
    private final static float m33 = 0.0f;

    /**
     * Compute a Catmull-Rom spline.
     * @param x the input parameter
     * @param numKnots the number of knots in the spline
     * @param knots the array of knots
     * @return the spline value
     */
    public static float spline(float x, int numKnots, float[] knots) {
      int span;
      int numSpans = numKnots - 3;
      float k0, k1, k2, k3;
      float c0, c1, c2, c3;

      if (numSpans < 1) {
        throw new IllegalArgumentException("Too few knots in spline");
      }

      x = clamp(x, 0, 1) * numSpans;
      span = (int) x;
      if (span > numKnots - 4) {
        span = numKnots - 4;
      }
      x -= span;

      k0 = knots[span];
      k1 = knots[span + 1];
      k2 = knots[span + 2];
      k3 = knots[span + 3];

      c3 = m00 * k0 + m01 * k1 + m02 * k2 + m03 * k3;
      c2 = m10 * k0 + m11 * k1 + m12 * k2 + m13 * k3;
      c1 = m20 * k0 + m21 * k1 + m22 * k2 + m23 * k3;
      c0 = m30 * k0 + m31 * k1 + m32 * k2 + m33 * k3;

      return ((c3 * x + c2) * x + c1) * x + c0;
    }

    /**
     * Compute a Catmull-Rom spline, but with variable knot spacing.
     * @param x the input parameter
     * @param numKnots the number of knots in the spline
     * @param xknots the array of knot x values
     * @param yknots the array of knot y values
     * @return the spline value
     */
    public static float spline(float x, int numKnots, int[] xknots, int[] yknots) {
      int span;
      int numSpans = numKnots - 3;
      float k0, k1, k2, k3;
      float c0, c1, c2, c3;

      if (numSpans < 1) {
        throw new IllegalArgumentException("Too few knots in spline");
      }

      for (span = 0; span < numSpans; span++) {
        if (xknots[span + 1] > x) {
          break;
        }
      }
      if (span > numKnots - 3) {
        span = numKnots - 3;
      }
      float t = (float) (x - xknots[span]) / (xknots[span + 1] - xknots[span]);
      span--;
      if (span < 0) {
        span = 0;
        t = 0;
      }

      k0 = yknots[span];
      k1 = yknots[span + 1];
      k2 = yknots[span + 2];
      k3 = yknots[span + 3];

      c3 = m00 * k0 + m01 * k1 + m02 * k2 + m03 * k3;
      c2 = m10 * k0 + m11 * k1 + m12 * k2 + m13 * k3;
      c1 = m20 * k0 + m21 * k1 + m22 * k2 + m23 * k3;
      c0 = m30 * k0 + m31 * k1 + m32 * k2 + m33 * k3;

      return ((c3 * t + c2) * t + c1) * t + c0;
    }

    /**
     * Compute a Catmull-Rom spline for RGB values.
     * @param x the input parameter
     * @param numKnots the number of knots in the spline
     * @param knots the array of knots
     * @return the spline value
     */
    public static int colorSpline(float x, int numKnots, int[] knots) {
      int span;
      int numSpans = numKnots - 3;
      float k0, k1, k2, k3;
      float c0, c1, c2, c3;

      if (numSpans < 1) {
        throw new IllegalArgumentException("Too few knots in spline");
      }

      x = clamp(x, 0, 1) * numSpans;
      span = (int) x;
      if (span > numKnots - 4) {
        span = numKnots - 4;
      }
      x -= span;

      int v = 0;
      for (int i = 0; i < 4; i++) {
        int shift = i * 8;

        k0 = (knots[span] >> shift) & 0xff;
        k1 = (knots[span + 1] >> shift) & 0xff;
        k2 = (knots[span + 2] >> shift) & 0xff;
        k3 = (knots[span + 3] >> shift) & 0xff;

        c3 = m00 * k0 + m01 * k1 + m02 * k2 + m03 * k3;
        c2 = m10 * k0 + m11 * k1 + m12 * k2 + m13 * k3;
        c1 = m20 * k0 + m21 * k1 + m22 * k2 + m23 * k3;
        c0 = m30 * k0 + m31 * k1 + m32 * k2 + m33 * k3;
        int n = (int) (((c3 * x + c2) * x + c1) * x + c0);
        if (n < 0) {
          n = 0;
        } else if (n > 255) {
          n = 255;
        }
        v |= n << shift;
      }

      return v;
    }

    /**
     * Compute a Catmull-Rom spline for RGB values, but with variable knot spacing.
     * @param x the input parameter
     * @param numKnots the number of knots in the spline
     * @param xknots the array of knot x values
     * @param yknots the array of knot y values
     * @return the spline value
     */
    public static int colorSpline(int x, int numKnots, int[] xknots, int[] yknots) {
      int span;
      int numSpans = numKnots - 3;
      float k0, k1, k2, k3;
      float c0, c1, c2, c3;

      if (numSpans < 1) {
        throw new IllegalArgumentException("Too few knots in spline");
      }

      for (span = 0; span < numSpans; span++) {
        if (xknots[span + 1] > x) {
          break;
        }
      }
      if (span > numKnots - 3) {
        span = numKnots - 3;
      }
      float t = (float) (x - xknots[span]) / (xknots[span + 1] - xknots[span]);
      span--;
      if (span < 0) {
        span = 0;
        t = 0;
      }

      int v = 0;
      for (int i = 0; i < 4; i++) {
        int shift = i * 8;

        k0 = (yknots[span] >> shift) & 0xff;
        k1 = (yknots[span + 1] >> shift) & 0xff;
        k2 = (yknots[span + 2] >> shift) & 0xff;
        k3 = (yknots[span + 3] >> shift) & 0xff;

        c3 = m00 * k0 + m01 * k1 + m02 * k2 + m03 * k3;
        c2 = m10 * k0 + m11 * k1 + m12 * k2 + m13 * k3;
        c1 = m20 * k0 + m21 * k1 + m22 * k2 + m23 * k3;
        c0 = m30 * k0 + m31 * k1 + m32 * k2 + m33 * k3;
        int n = (int) (((c3 * t + c2) * t + c1) * t + c0);
        if (n < 0) {
          n = 0;
        } else if (n > 255) {
          n = 255;
        }
        v |= n << shift;
      }

      return v;
    }

    /**
     * An implementation of Fant's resampling algorithm.
     * @param source the source pixels
     * @param dest the destination pixels
     * @param length the length of the scanline to resample
     * @param offset the start offset into the arrays
     * @param stride the offset between pixels in consecutive rows
     * @param out an array of output positions for each pixel
     */
    public static void resample(int[] source, int[] dest, int length, int offset, int stride, float[] out) {
      int i, j;
      float intensity;
      float sizfac;
      float inSegment;
      float outSegment;
      int a, r, g, b, nextA, nextR, nextG, nextB;
      float aSum, rSum, gSum, bSum;
      float[] in;
      int srcIndex = offset;
      int destIndex = offset;
      int lastIndex = source.length;
      int rgb;

      in = new float[length + 1];
      i = 0;
      for (j = 0; j < length; j++) {
        while (out[i + 1] < j) {
          i++;
        }
        in[j] = i + (float) (j - out[i]) / (out[i + 1] - out[i]);
      }
      in[length] = length;

      inSegment = 1.0f;
      outSegment = in[1];
      sizfac = outSegment;
      aSum = rSum = gSum = bSum = 0.0f;
      rgb = source[srcIndex];
      a = (rgb >> 24) & 0xff;
      r = (rgb >> 16) & 0xff;
      g = (rgb >> 8) & 0xff;
      b = rgb & 0xff;
      srcIndex += stride;
      rgb = source[srcIndex];
      nextA = (rgb >> 24) & 0xff;
      nextR = (rgb >> 16) & 0xff;
      nextG = (rgb >> 8) & 0xff;
      nextB = rgb & 0xff;
      srcIndex += stride;
      i = 1;

      while (i < length) {
        float aIntensity = inSegment * a + (1.0f - inSegment) * nextA;
        float rIntensity = inSegment * r + (1.0f - inSegment) * nextR;
        float gIntensity = inSegment * g + (1.0f - inSegment) * nextG;
        float bIntensity = inSegment * b + (1.0f - inSegment) * nextB;
        if (inSegment < outSegment) {
          aSum += (aIntensity * inSegment);
          rSum += (rIntensity * inSegment);
          gSum += (gIntensity * inSegment);
          bSum += (bIntensity * inSegment);
          outSegment -= inSegment;
          inSegment = 1.0f;
          a = nextA;
          r = nextR;
          g = nextG;
          b = nextB;
          if (srcIndex < lastIndex) {
            rgb = source[srcIndex];
          }
          nextA = (rgb >> 24) & 0xff;
          nextR = (rgb >> 16) & 0xff;
          nextG = (rgb >> 8) & 0xff;
          nextB = rgb & 0xff;
          srcIndex += stride;
        } else {
          aSum += (aIntensity * outSegment);
          rSum += (rIntensity * outSegment);
          gSum += (gIntensity * outSegment);
          bSum += (bIntensity * outSegment);
          dest[destIndex] =
              ((int) Math.min(aSum / sizfac, 255) << 24) |
              ((int) Math.min(rSum / sizfac, 255) << 16) |
              ((int) Math.min(gSum / sizfac, 255) << 8) |
              (int) Math.min(bSum / sizfac, 255);
          destIndex += stride;
          rSum = gSum = bSum = 0.0f;
          inSegment -= outSegment;
          outSegment = in[i + 1] - in[i];
          sizfac = outSegment;
          i++;
        }
      }
    }

  }

}
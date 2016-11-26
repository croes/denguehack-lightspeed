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
package samples.lightspeed.internal.printing;

import static com.luciad.gui.TLcdIconFactory.*;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.util.Enumeration;
import java.util.regex.Pattern;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.JToolBar;
import javax.swing.WindowConstants;

import com.luciad.format.raster.TLcdRasterModelDescriptor;
import com.luciad.format.raster.TLcdRenderedImageTile;
import com.luciad.format.raster.TLcdSingleTileRaster;
import com.luciad.geodesy.TLcdGeodeticDatum;
import com.luciad.gui.ALcdAction;
import com.luciad.gui.TLcdImageIcon;
import com.luciad.gui.swing.TLcdSWIcon;
import com.luciad.model.ILcdModel;
import com.luciad.model.TLcdVectorModel;
import com.luciad.projection.TLcdEquidistantCylindrical;
import com.luciad.reference.TLcdGridReference;
import com.luciad.shape.ILcdBounds;
import com.luciad.shape.shape2D.TLcdXYBounds;
import com.luciad.view.ILcdLayer;
import com.luciad.view.lightspeed.ALspAWTView;
import com.luciad.view.lightspeed.ILspAWTView;
import com.luciad.view.lightspeed.TLspViewBuilder;
import com.luciad.view.lightspeed.TLspViewPrintSettings;
import com.luciad.view.lightspeed.camera.TLspViewXYZWorldTransformation2D;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.TLspPaintRepresentationState;
import com.luciad.view.lightspeed.layer.raster.TLspRasterLayerBuilder;
import com.luciad.view.lightspeed.style.TLspRasterStyle;
import com.luciad.view.lightspeed.util.TLspViewNavigationUtil;

/**
 * Shows a print preview with the aspect ratio of an A4 page (regardless of the view's aspect
 * ration on screen). A scale factor of 1 is interpreted as 100 DPI.
 */
public class A4PrintPreviewAction extends ALcdAction {

  private final ALspAWTView fView;
  private int fScaleFactor = 1;
  private double fFeatureScaleFactor = 1;

  public A4PrintPreviewAction(ALspAWTView aView) {
    super("Print preview (A4 aspect ratio)", new TLcdImageIcon("images/gui/i16_new.gif"));
    fView = aView;
  }

  public int getScaleFactor() {
    return fScaleFactor;
  }

  public void setScaleFactor(double aScaleFactor) {
    fScaleFactor = (int) aScaleFactor;
  }

  public double getFeatureScaleFactor() {
    return fFeatureScaleFactor;
  }

  public void setFeatureScaleFactor(double aFeatureScaleFactor) {
    fFeatureScaleFactor = aFeatureScaleFactor;
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    BufferedImage bufferedImage = new BufferedImage(
        827 * fScaleFactor,
        1169 * fScaleFactor,
        BufferedImage.TYPE_INT_ARGB
    );
    Graphics2D graphics = bufferedImage.createGraphics();
    try {
      fView.print(
          graphics,
          TLspViewPrintSettings.newBuilder()
                               .printBounds(new Rectangle(0, 0, bufferedImage.getWidth(), bufferedImage.getHeight()))
                               .featureScale(fFeatureScaleFactor)
                               .build()
      );
    } finally {
      graphics.dispose();
    }

    TLcdXYBounds bounds = new TLcdXYBounds(0, 0, bufferedImage.getWidth(), bufferedImage.getHeight());

    final ILspAWTView view = TLspViewBuilder.newBuilder().buildAWTView();

    JToolBar toolBar = new JToolBar();
    toolBar.setFloatable(false);
    toolBar.add(new ScaleAction(1.0 / fScaleFactor, "View size", new TLcdSWIcon(create(FIT_ICON)), view));
    toolBar.add(new ScaleAction(1.0, "Print size", new TLcdSWIcon(create(PRINT_ICON)), view));
    toolBar.addSeparator();
    toolBar.add(new LayerToggleAction("Downsample", "Upsample", new TLcdSWIcon(create(REMOVE_ITEM_ICON)), new TLcdSWIcon(create(ADD_ITEM_ICON)), view, ".*print.*"));

    final JFrame frame = new JFrame("Print preview");
    frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    frame.add(toolBar, BorderLayout.NORTH);
    frame.add(view.getHostComponent(), BorderLayout.CENTER);
    Dimension size = new Dimension(fView.getWidth(), fView.getHeight());
    view.getHostComponent().setPreferredSize(size);
    view.getHostComponent().setSize(size);
    frame.pack();
    frame.setVisible(true);
    frame.addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosed(WindowEvent e) {
        frame.remove(view.getHostComponent());
        view.destroy();
      }
    });

    // Create printed layer
    ILspLayer layer = createLayerForImage(bufferedImage, bounds, "Print");
    view.addLayer(layer);

    // Create smoothly downsampled version
    Image scaledImage = bufferedImage.getScaledInstance(fView.getWidth(), fView.getHeight(), Image.SCALE_AREA_AVERAGING);
    BufferedImage scaledBufferedImage = new BufferedImage(fView.getWidth(), fView.getHeight(), BufferedImage.TYPE_INT_ARGB);
    graphics = scaledBufferedImage.createGraphics();
    graphics.drawImage(scaledImage, 0, 0, fView.getWidth(), fView.getHeight(), null);

    ILspLayer downSampledLayer = createLayerForImage(scaledBufferedImage, bounds, "Print (downsampled)");
    downSampledLayer.setVisible(false);
    view.addLayer(downSampledLayer);
    try {
      new TLspViewNavigationUtil(view).fit(layer);
    } catch (Exception e1) {
      e1.printStackTrace();
    }

    try {
      TLspViewNavigationUtil viewNavigationUtil = new TLspViewNavigationUtil(view);
      viewNavigationUtil.setFitMargin(0.0);
      viewNavigationUtil.fit(layer);
    } catch (Exception ignored) {
    }
  }

  private static ILspLayer createLayerForImage(BufferedImage aImage, ILcdBounds aBounds, String aName) {
    TLcdVectorModel model = new TLcdVectorModel(
        new TLcdGridReference(new TLcdGeodeticDatum(), new TLcdEquidistantCylindrical()),
        new TLcdRasterModelDescriptor(aName, aName, aName)
    );
    model.addElement(
        new TLcdSingleTileRaster(
            aBounds,
            new TLcdRenderedImageTile(aImage),
            0, aImage.getColorModel()
        ),
        ILcdModel.NO_EVENT
    );

    return TLspRasterLayerBuilder.
                                     newBuilder().
                                     styler(TLspPaintRepresentationState.REGULAR_BODY, TLspRasterStyle.newBuilder().startResolutionFactor(Double.POSITIVE_INFINITY).build()).
                                     model(model).
                                     build();
  }

  private static class ScaleAction extends AbstractAction {
    private final ILspAWTView fView;
    private final double fScale;

    public ScaleAction(double aScale, String aName, TLcdSWIcon aIcon, ILspAWTView aView) {
      super(aName, aIcon);
      fView = aView;
      putValue(SHORT_DESCRIPTION, aName);
      fScale = aScale;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      TLspViewXYZWorldTransformation2D v2w = (TLspViewXYZWorldTransformation2D) fView.getViewXYZWorldTransformation();
      v2w.lookAt(v2w.getWorldOrigin(), v2w.getViewOrigin(), fScale, v2w.getRotation());
    }
  }

  private static class LayerToggleAction extends AbstractAction {
    private final ILspAWTView fView;

    private TLcdSWIcon fCurrentIcon;
    private TLcdSWIcon fToggleIcon;

    private String fCurrentName;
    private String fToggleName;

    private final Pattern fLayerPattern;

    public LayerToggleAction(String aName, String aToggleName, TLcdSWIcon aIcon, TLcdSWIcon aToggleIcon, ILspAWTView aView, String aRegex) {
      super(aName, aIcon);
      fView = aView;
      putValue(SHORT_DESCRIPTION, aName);

      fCurrentIcon = aIcon;
      fToggleIcon = aToggleIcon;

      fCurrentName = aName;
      fToggleName = aToggleName;
      fLayerPattern = Pattern.compile(aRegex, Pattern.CASE_INSENSITIVE);
    }

    @Override
    public void actionPerformed(ActionEvent aEvent) {
      Enumeration e = fView.layers();
      while (e.hasMoreElements()) {
        ILcdLayer layer = (ILcdLayer) e.nextElement();
        if (fLayerPattern.matcher(layer.getLabel()).matches()) {
          layer.setVisible(!layer.isVisible());
        }
      }

      putValue(Action.SMALL_ICON, fToggleIcon);

      TLcdSWIcon tempIcon = fCurrentIcon;
      fCurrentIcon = fToggleIcon;
      fToggleIcon = tempIcon;

      putValue(SHORT_DESCRIPTION, fToggleName);
      String tempString = fCurrentName;
      fCurrentName = fToggleName;
      fToggleName = tempString;

    }
  }
}

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
package samples.gxy.common;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;

import com.luciad.gui.TLcdHaloIcon;
import com.luciad.gui.swing.TLcdSWIcon;
import com.luciad.model.ILcdModelReference;
import com.luciad.reference.TLcdGeodeticReference;
import com.luciad.shape.ILcdPoint;
import com.luciad.text.TLcdAltitudeFormat;
import com.luciad.text.TLcdLonLatPointFormat;
import com.luciad.util.ILcdFormatter;
import com.luciad.util.TLcdOutOfBoundsException;
import com.luciad.util.iso19103.ILcdISO19103Measure;
import com.luciad.util.iso19103.TLcdISO19103Measure;
import com.luciad.util.iso19103.TLcdISO19103MeasureTypeCodeExtension;
import com.luciad.util.measure.TLcdMeasureFormatUtil;

import samples.common.gui.TextIcon;

/**
 * Displays the coordinates and additional values of the location under the mouse pointer on a map.
 */
public abstract class AMouseLocationComponent extends JPanel {

  // visualization
  private JLabel fLabel;
  private TextIcon fIcon;

  private TLcdHaloIcon fHaloIcon;

  // coordinates, height, additional values
  private ILcdPoint fMouseLocation;
  private ILcdFormatter fCoordinateFormats[];
  private int fCurrentCoordinateFormatIndex = 0;
  private TLcdAltitudeFormat fAltitudeFormat;
  private boolean fShowValues;
  private ILcdModelReference fModelReference;

  /**
   * @param aComponent the component to track mouse movements for
   */
  public AMouseLocationComponent(Component aComponent) {
    fModelReference = new TLcdGeodeticReference();
    fCoordinateFormats = new ILcdFormatter[]{new TLcdLonLatPointFormat(), new TLcdLonLatPointFormat(TLcdLonLatPointFormat.DEC_DEG_3)};
    fAltitudeFormat = new TLcdAltitudeFormat();
    fShowValues = true;

    fIcon = new TextIcon() {
      @Override
      protected void resized() {
        fLabel.revalidate();
      }
    };
    fHaloIcon = new TLcdHaloIcon(fIcon);
    fHaloIcon.setUseImageCache(false);
    fHaloIcon.setHaloColor(new Color(40, 40, 40, 230));

    fLabel = new JLabel(new TLcdSWIcon(fHaloIcon));
    setLayout(new BorderLayout());
    add(fLabel, BorderLayout.EAST);

    aComponent.addMouseMotionListener(new MyMouseMotionListener());
    setBackground(new Color(1f, 1f, 1f, 0f));
    setOpaque(false);

    addMouseListener(new MouseAdapter() {
      // cycle through the formats
      @Override
      public void mouseClicked(MouseEvent e) {
        fCurrentCoordinateFormatIndex = (fCurrentCoordinateFormatIndex + 1) % fCoordinateFormats.length;
        refreshContent();
      }
    });
  }

  /**
   * @param aColor the new font color
   */
  public void setColor(Color aColor) {
    fIcon.setColor(aColor);
  }

  /**
   * @param aHaloColor the new halo outline color, or null to disable the halo
   */
  public void setHaloColor(Color aHaloColor) {
    fHaloIcon.setHaloEnabled(aHaloColor != null);
    if (aHaloColor != null) {
      fHaloIcon.setHaloColor(aHaloColor);
    }
  }

  public void setFont(Font aFont) {
    super.setFont(aFont);
    if (fIcon != null) {
      fIcon.setFont(aFont);
    }
  }

  /**
   * @param aShowValues if true, the component will also show non-coordinate values under the mouse
   *                    cursor
   */
  public void setShowValues(boolean aShowValues) {
    fShowValues = aShowValues;
  }

  /**
   * @param aCoordinateFormat the instance that will format the coordinates
   */
  public void setCoordinateFormats(ILcdFormatter aCoordinateFormat[]) {
    fCoordinateFormats = aCoordinateFormat;
  }

  /**
   * @param aModelReference the model reference to express the coordinates in
   */
  public void setModelReference(ILcdModelReference aModelReference) {
    fModelReference = aModelReference;
  }

  protected abstract ILcdPoint getCoordinates(Point aAWTPoint, ILcdModelReference aReference) throws TLcdOutOfBoundsException;

  protected abstract TLcdISO19103Measure[] getValues(ILcdPoint aPoint, ILcdModelReference aPointReference);

  protected double getHeight(ILcdPoint aPoint, ILcdModelReference aPointReference) {
    return Double.NaN;
  }

  class MyMouseMotionListener extends MouseMotionAdapter {

    public void mouseDragged(MouseEvent e) {
      this.mouseMoved(e);
    }

    public void mouseMoved(MouseEvent e) {
      try {
        fMouseLocation = getCoordinates(e.getPoint(), fModelReference);
        refreshContent();
      } catch (TLcdOutOfBoundsException ignored) {
      }
    }

  }

  protected final void refreshContent() {
    List<String> lines;
    if (fMouseLocation == null) {
      lines = null;
    } else {
      // Query the terrain height under the cursor
      double height = getHeight(fMouseLocation, fModelReference);
      // Query other measure providers
      ArrayList<TLcdISO19103Measure> values = new ArrayList<>();
      if (fShowValues) {
        values.addAll(Arrays.asList(getValues(fMouseLocation, fModelReference)));
      }
      // If both getHeight() and the measure providers returned a terrain elevation,
      // let the value from the measure provider take precedence.
      for (int i = 0; i < values.size(); i++) {
        TLcdISO19103Measure value = values.get(i);
        if (isTerrain(value)) {
          values.remove(i);
          height = value.getValue();
          break;
        }
      }

      // Now we can build the text to display in the label:
      // 1. start with the coordinates
      String coordinates = fCoordinateFormats[fCurrentCoordinateFormatIndex].format(fMouseLocation);
      // 2. add the height, if available
      if (!Double.isNaN(height)) {
        coordinates += ", " + fAltitudeFormat.formatAltitude(height);
      }
      // 3. finally, add any other available measurement values.
      lines = TLcdMeasureFormatUtil.formatMeasures(
          values.toArray(new ILcdISO19103Measure[values.size()]),
          TLcdMeasureFormatUtil.MeasureTypeMode.AUTO
      );
      lines.add(0, coordinates);
    }
    fIcon.setLines(lines);
    fIcon.recalculateSize(getGraphics());
    repaint();
  }

  /**
   * Determines whether the given measure expresses a terrain elevation value.
   */
  private boolean isTerrain(TLcdISO19103Measure aMeasure) {
    return aMeasure.getUnitOfMeasure().getMeasureType() == TLcdISO19103MeasureTypeCodeExtension.TERRAIN_HEIGHT;
  }
}

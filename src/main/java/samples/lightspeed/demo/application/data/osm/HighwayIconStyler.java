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

import static com.luciad.fusion.client.view.lightspeed.TLspFusionGeometryProvider.LINE;

import static samples.lightspeed.demo.application.data.osm.RoadUtil.*;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.util.Collection;

import com.luciad.datamodel.ILcdDataObject;
import com.luciad.gui.ILcdIcon;
import com.luciad.gui.TLcdIconImageUtil;
import com.luciad.view.lightspeed.TLspContext;
import com.luciad.view.lightspeed.label.algorithm.discrete.TLspOnPathLabelingAlgorithm;
import com.luciad.view.lightspeed.painter.label.style.ALspLabelTextProviderStyle;
import com.luciad.view.lightspeed.style.TLspIconStyle;
import com.luciad.view.lightspeed.style.TLspTextStyle;
import com.luciad.view.lightspeed.style.styler.ALspLabelStyleCollector;
import com.luciad.view.lightspeed.style.styler.ALspLabelStyler;

/**
 * Renders a highway icon style as a label on OpenStreetMap highway data objects.
 */
class HighwayIconStyler extends ALspLabelStyler {

  private static Font NUMBER_FONT = new Font("Dialog", Font.BOLD, 12);
  private static Color[] TEXT_COLORS = {Color.white, Color.darkGray, Color.darkGray};
  private static ILcdIcon[][] ICONS = {
      {
          new ShiftedImageIcon("samples/images/highwayIcons/interstate_small.png", 2),
          new ShiftedImageIcon("samples/images/highwayIcons/us_small.png", 2),
          new ShiftedImageIcon("samples/images/highwayIcons/state_large.png", 2)
      },
      {
          new ShiftedImageIcon("samples/images/highwayIcons/interstate_large.png", 2),
          new ShiftedImageIcon("samples/images/highwayIcons/us_large.png", 2),
          new ShiftedImageIcon("samples/images/highwayIcons/state_large.png", 2)
      }
  };

  private TLspOnPathLabelingAlgorithm fOnPathLabelingAlgorithm = new TLspOnPathLabelingAlgorithm();

  private final ALspLabelTextProviderStyle fHighwayTextProvider = new ALspLabelTextProviderStyle() {
    @Override
    public String[] getText(Object aDomainObject, Object aSubLabelID, TLspContext aContext) {
      int highway_number = getHighwayNumber(aDomainObject);
      return new String[]{Integer.toString(highway_number)};
    }
  };

  public HighwayIconStyler() {
    fOnPathLabelingAlgorithm.setAllowRotation(false);
  }

  @Override
  public void style(Collection<?> aObjects, ALspLabelStyleCollector aStyleCollector, TLspContext aContext) {
    for (Object object : aObjects) {
      if (object instanceof ILcdDataObject) {
        highwayStyle(aStyleCollector, (ILcdDataObject) object, aContext);
      }
    }
  }

  private void highwayStyle(ALspLabelStyleCollector aStyleCollector, ILcdDataObject aDataObject, TLspContext aContext) {
    if (isHighway(aDataObject)) {
      int lod = getLevelOfDetail(aContext.getViewXYZWorldTransformation());
      int priority = getPriority(aDataObject, 0, 1000, lod);
      HighwayType highwayType = getHighwayType(aDataObject);

      // Use a larger icon when the highway number is more than 3 digits long.
      ILcdIcon icon = getHighwayNumber(aDataObject) < 100 ? ICONS[0][highwayType.ordinal()]
                                                          : ICONS[1][highwayType.ordinal()];

      aStyleCollector.object(aDataObject)
                     .algorithm(fOnPathLabelingAlgorithm)
                     .geometry(LINE)
                     .priority(priority)
                     .styles(TLspIconStyle.newBuilder().icon(icon).build(),
                             TLspTextStyle.newBuilder().haloThickness(0)
                                          .textColor(TEXT_COLORS[highwayType.ordinal()])
                                          .font(NUMBER_FONT)
                                          .build(),
                             fHighwayTextProvider
                     )
                     .submit();
    }
  }

  /**
   * An icon that is shifted a bit, such that the center of the icon is not in the center of the
   * image.
   */
  private static class ShiftedImageIcon implements ILcdIcon {
    private final int fVerticalShift;
    private final Image fImage;

    public ShiftedImageIcon(String aFile, int aVerticalShift) {
      fImage = new TLcdIconImageUtil().getImage(aFile);
      fVerticalShift = aVerticalShift;
    }

    @Override
    public int getIconWidth() {
      return fImage.getWidth(null);
    }

    @Override
    public int getIconHeight() {
      return fImage.getHeight(null) + fVerticalShift;
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
      g.drawImage(fImage, x, y, fImage.getWidth(null), fImage.getHeight(null), c);
    }

    @SuppressWarnings({"CloneDoesntCallSuperClone"})
    public Object clone() {
      throw new UnsupportedOperationException();
    }
  }

}

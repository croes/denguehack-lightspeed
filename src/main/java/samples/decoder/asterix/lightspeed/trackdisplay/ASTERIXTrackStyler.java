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
package samples.decoder.asterix.lightspeed.trackdisplay;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.util.Collection;
import java.util.List;

import com.luciad.format.asterix.TLcdASTERIXTrack;
import com.luciad.format.asterix.TLcdASTERIXTrajectory;
import com.luciad.gui.ILcdIcon;
import com.luciad.gui.TLcdSymbol;
import com.luciad.view.lightspeed.TLspContext;
import com.luciad.view.lightspeed.style.TLspIconStyle;
import com.luciad.view.lightspeed.style.styler.ALspStyleCollector;
import com.luciad.view.lightspeed.style.styler.ALspStyleTargetProvider;
import com.luciad.view.lightspeed.style.styler.ALspStyler;

import samples.gxy.common.AntiAliasedIcon;

/**
 * Styler that styles ASTERIX tracks using a trail of history points.
 */
class ASTERIXTrackStyler extends ALspStyler {

  private static final ILcdIcon TRACK_ICON = new TLcdSymbol(TLcdSymbol.FILLED_RECT, 7, Color.gray);
  private static final ILcdIcon HIGHLIGHTED_TRACK_ICON = new HighlightedTrackIcon(TRACK_ICON);
  private static final TLspIconStyle TRACK_ICON_STYLE = TLspIconStyle.newBuilder().icon(TRACK_ICON).build();
  private static final TLspIconStyle HIGHLIGHTED_TRACK_ICON_STYLE = TLspIconStyle.newBuilder().icon(HIGHLIGHTED_TRACK_ICON).zOrder(1).build();

  private static final TLspIconStyle TRACK_HISTORY_ICON_STYLE = TLspIconStyle.newBuilder().icon(new AntiAliasedIcon(new TLcdSymbol(TLcdSymbol.FILLED_CIRCLE, 4, Color.gray))).build();
  private static final ALspStyleTargetProvider TRACK_HISTORY_STYLE_TARGET_PROVIDER = new HistoryPointStyleTargetProvider();

  private final ASTERIXTrackAdditionalData fAdditionalData;

  ASTERIXTrackStyler(ASTERIXTrackAdditionalData aAdditionalData) {
    fAdditionalData = aAdditionalData;
  }

  @Override
  public void style(Collection<?> aObjects, ALspStyleCollector aStyleCollector, TLspContext aContext) {
    for (Object object : aObjects) {
      if (fAdditionalData.isHighlighted(object)) {
        aStyleCollector.object(object).styles(HIGHLIGHTED_TRACK_ICON_STYLE).submit();
      } else {
        aStyleCollector.object(object).styles(TRACK_ICON_STYLE).submit();
      }
    }
    aStyleCollector.objects(aObjects).geometry(TRACK_HISTORY_STYLE_TARGET_PROVIDER).styles(TRACK_HISTORY_ICON_STYLE).submit();
  }

  private static class HistoryPointStyleTargetProvider extends ALspStyleTargetProvider {

    @Override
    public void getStyleTargetsSFCT(Object aObject, TLspContext aContext, List<Object> aResultSFCT) {
      if (!(aObject instanceof TLcdASTERIXTrack)) {
        return;
      }
      TLcdASTERIXTrack asterixTrack = (TLcdASTERIXTrack) aObject;
      TLcdASTERIXTrajectory trajectory = asterixTrack.getTrajectory();
      int historyPointCount = 5;
      for (int i = 0; i < historyPointCount; i++) {
        int index = asterixTrack.getTrajectoryPointIndex() - i;
        if (index >= 0) {
          TLcdASTERIXTrack historyPoint = new TLcdASTERIXTrack(trajectory);
          historyPoint.updateForIndex(index);
          aResultSFCT.add(historyPoint);
        }
      }
    }
  }

  private static class HighlightedTrackIcon implements ILcdIcon {

    private static final int DECORATION_SIZE_X = 4;
    private static final int DECORATION_SIZE_Y = 9;
    private final ILcdIcon fTrackIcon;

    private HighlightedTrackIcon(ILcdIcon aTrackIcon) {
      fTrackIcon = aTrackIcon;
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
      Graphics2D g2d = (Graphics2D) g;

      Object previous = g2d.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
      g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

      g2d.setColor(Color.gray.brighter());
      Stroke oldStroke = g2d.getStroke();
      g2d.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

      g2d.drawLine(1, 1, DECORATION_SIZE_X + 1, DECORATION_SIZE_Y / 2 + 1);
      g2d.drawLine(DECORATION_SIZE_X + 1, DECORATION_SIZE_Y / 2 + 1, 1, DECORATION_SIZE_Y);

      g2d.drawLine(getIconWidth() - 2, 1, getIconWidth() - 2 - DECORATION_SIZE_X, DECORATION_SIZE_Y / 2 + 1);
      g2d.drawLine(getIconWidth() - 2 - DECORATION_SIZE_X, DECORATION_SIZE_Y / 2 + 1, getIconWidth() - 2, DECORATION_SIZE_Y);

      g2d.setStroke(oldStroke);
      g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, previous);

      int offsetX = (getIconWidth() - fTrackIcon.getIconWidth()) / 2;
      int offsetY = (getIconHeight() - fTrackIcon.getIconHeight()) / 2;
      fTrackIcon.paintIcon(c, g2d, x + offsetX, y + offsetY);
    }

    @Override
    public int getIconWidth() {
      return fTrackIcon.getIconWidth() + 2 * DECORATION_SIZE_X + 16;
    }

    @Override
    public int getIconHeight() {
      return Math.max(fTrackIcon.getIconHeight(), DECORATION_SIZE_Y + 2);
    }

    @Override
    public Object clone() {
      try {
        return super.clone();
      } catch (CloneNotSupportedException e) {
        throw new RuntimeException(e);
      }
    }
  }
}

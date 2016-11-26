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
package samples.gxy.highlight;

import java.awt.Graphics;

import com.luciad.model.ILcdModel;
import com.luciad.view.gxy.ILcdGXYPainter;
import com.luciad.view.gxy.ILcdGXYPainterProvider;
import com.luciad.view.gxy.ILcdGXYView;
import com.luciad.view.gxy.TLcdGXYContext;
import com.luciad.view.gxy.TLcdGXYLayer;

/**
 * TLcdGXYLayer that paints highlighted objects using a specific painter provider.
 */
public class HighlightLayer extends TLcdGXYLayer implements HighlightController.HighlightLayer {

  private ILcdGXYPainterProvider fPainterProvider;

  public HighlightLayer(ILcdModel aModel, String aLabel, ILcdGXYPainterProvider aPainterProvider) {
    super(aModel, aLabel);
    fPainterProvider = aPainterProvider;
  }

  @Override
  public void paintHighlightedObject(Object aObject, Graphics aGraphics, ILcdGXYView aGXYView) {
    ILcdGXYPainter painter = fPainterProvider.getGXYPainter(aObject);
    if (painter != null) {
      painter.paint(
          aGraphics, ILcdGXYPainter.DEFAULT | ILcdGXYPainter.BODY, new TLcdGXYContext(aGXYView, this));
    }
  }

}

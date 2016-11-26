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
package samples.lightspeed.imageprojection;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Collection;

import javax.imageio.ImageIO;

import com.luciad.io.TLcdIOUtil;
import com.luciad.view.lightspeed.TLspContext;
import com.luciad.view.lightspeed.layer.imageprojection.TLspImageProjectionStyle;
import com.luciad.view.lightspeed.style.styler.ALspStyleCollector;
import com.luciad.view.lightspeed.style.styler.ALspStyler;
import com.luciad.view.lightspeed.util.opengl.texture.TLsp2DImageTextureObject;

/**
 * Projection Styler that projects a single image from a single projector.
 */
class ProjectionStyler extends ALspStyler {

  private static final String IMAGE_LOC = "Data/2d_icons/eye.png";

  private final BufferedImage fImage;
  private TLsp2DImageTextureObject fTexture;

  public ProjectionStyler() {
    try {
      TLcdIOUtil ioUtil = new TLcdIOUtil();
      ioUtil.setSourceName(IMAGE_LOC);
      fImage = ImageIO.read(ioUtil.retrieveInputStream());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    fTexture = new TLsp2DImageTextureObject(fImage);
  }

  @Override
  public void style(Collection<?> aObjects, ALspStyleCollector aStyleCollector, TLspContext aContext) {
    for (Object object : aObjects) {
      ImageProjector domainObject = (ImageProjector) object;
      domainObject.setAspectRatio(fImage.getWidth() / (double) fImage.getHeight());
      TLspImageProjectionStyle style = TLspImageProjectionStyle
          .newBuilder()
          .image(fTexture)
          .projector(domainObject.getProjector())
          .build();
      aStyleCollector.object(object).style(style).submit();
    }
  }
}

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
package samples.lightspeed.internal;

import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import com.luciad.view.lightspeed.ALspViewAdapter;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.TLspAWTView;
import com.luciad.view.lightspeed.TLspViewBuilder;
import com.luciad.view.lightspeed.util.TLspViewPaintUtil;
import com.luciad.view.lightspeed.util.opengl.texture.TLsp2DImageTextureObject;
import com.luciad.view.opengl.binding.ILcdGL;
import com.luciad.view.opengl.binding.ILcdGLDrawable;

/**
 * Created with IntelliJ IDEA.
 * User: barta
 * Date: 5/17/13
 * Time: 10:25 AM
 * To change this template use File | Settings | File Templates.
 */
public class ContextSharing {


  private static void buildUI() {
    TLspAWTView view1 = TLspViewBuilder.newBuilder().buildAWTView();
    TLspAWTView view2 = TLspViewBuilder.newBuilder().buildAWTView();

    JFrame frame1 = new JFrame();
    frame1.add(view1.getHostComponent());

    JFrame frame2 = new JFrame();
    frame2.add(view2.getHostComponent());

    frame1.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame2.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

    frame1.pack();
    frame2.pack();
    frame1.setVisible(true);
    frame2.setVisible(true);

    MyViewListener listener = new MyViewListener();
    view1.addViewListener(listener);
    view2.addViewListener(listener);
  }

  private static class MyViewListener extends ALspViewAdapter {

    private TLsp2DImageTextureObject fTexture;

    @Override
    public void postRender(ILspView aView, ILcdGLDrawable aGLDrawable) {

      if (fTexture == null) {
        try {
          fTexture = new TLsp2DImageTextureObject(ImageIO.read(new File("Data/internal/accuracy/pool.png")));
        } catch (IOException e) {
          e.printStackTrace();
        }
      }

      ILcdGL gl = aGLDrawable.getGL();
      gl.glEnable(ILcdGL.GL_TEXTURE_2D);
      fTexture.bind(aGLDrawable);

      final int width = aGLDrawable.getSize().width;
      final int height = aGLDrawable.getSize().height;

      TLspViewPaintUtil.beginOrthoRendering(aGLDrawable);
      gl.glBegin(ILcdGL.GL_QUADS);

      gl.glTexCoord2f(0, 0);
      gl.glVertex2i(0, 0);
      gl.glTexCoord2f(1, 0);
      gl.glVertex2i(width, 0);
      gl.glTexCoord2f(1, 1);
      gl.glVertex2i(width, height);
      gl.glTexCoord2f(0, 1);
      gl.glVertex2i(0, height);

      gl.glEnd();

      fTexture.unbind(aGLDrawable);
      TLspViewPaintUtil.endOrthoRendering(aGLDrawable);
    }
  }

}

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
package samples.lightspeed.demo.application.data.uav;

import com.luciad.view.opengl.binding.ILcdGL;
import com.luciad.view.opengl.binding.ILcdGLDrawable;

/**
 * Simple utility class to setup OpenGL for rendering in screen space.
 */
class ScreenSpaceUtil {

  /**
   * Sets up an orthographic projection with the specified dimensions.
   */
  static void beginOrthoRendering(ILcdGLDrawable aGLAutoDrawable) {
    ILcdGL gl = aGLAutoDrawable.getGL();

    int x = 0;
    int y = aGLAutoDrawable.getSize().height;
    int w = aGLAutoDrawable.getSize().width;
    int h = -aGLAutoDrawable.getSize().height;

    // Set up orthographic projection that matches AWT window coordinates.
    gl.glMatrixMode(ILcdGL.GL_PROJECTION);
    gl.glPushMatrix();
    gl.glLoadIdentity();
    gl.glOrtho(x, x + w, y, y + h, -1000, 1000);
    gl.glMatrixMode(ILcdGL.GL_MODELVIEW);
    gl.glPushMatrix();
    gl.glLoadIdentity();
  }

  /**
   * Undoes a previously set orthographic projection.
   */
  static void endOrthoRendering(ILcdGLDrawable aGLAutoDrawable) {
    ILcdGL gl = aGLAutoDrawable.getGL();
    gl.glMatrixMode(ILcdGL.GL_PROJECTION);
    gl.glPopMatrix();
    gl.glMatrixMode(ILcdGL.GL_MODELVIEW);
    gl.glPopMatrix();
  }

}

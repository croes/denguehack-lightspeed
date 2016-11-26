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

import java.nio.IntBuffer;

import com.luciad.view.lightspeed.util.opengl.texture.ALsp2DTextureObject;
import com.luciad.view.opengl.binding.ILcdGL;
import com.luciad.view.opengl.binding.ILcdGLDrawable;

/**
 * Texture object representation of a buffered image. It uses automatic mipmap
 * generation together with linear interpolation for minification and maxification
 * and clamps to the edges of the image.
 * <p/>
 * Note that two different texture objects that are created for equal images
 * are also equal, even though they may return a different texture object id.
 */
public class IntBufferTextureObject extends ALsp2DTextureObject {

  private long fBytes;
  private boolean fDirty = true;
  private int fHash;
  private boolean fTextureCreated = false;
  private IntBuffer fIntBuffer;

  public IntBufferTextureObject(int aWidth, int aHeight) {
    super(aWidth, aHeight);
    fHash = aWidth * aHeight;
    fBytes = aWidth * aHeight * 4;
  }

  /**
   * Sets the given image. The given image should have the same
   * properties as the previously set image.
   */
  public void setBuffer(IntBuffer aIntBuffer) {
    fIntBuffer = aIntBuffer;
    fDirty = true;
  }

  @Override
  protected void init(ILcdGLDrawable aGLDrawable) {
    super.init(aGLDrawable);
    super.bind(aGLDrawable);
    upload(aGLDrawable);
    super.unbind(aGLDrawable);
  }

  // Assumes texture is bound
  private void upload(ILcdGLDrawable aGLDrawable) {
    ILcdGL gl = aGLDrawable.getGL();
    if (fTextureCreated) {
      gl.glTexSubImage2D(ILcdGL.GL_TEXTURE_2D,
                         0,
                         0, 0,
                         getWidth(), getHeight(),
                         ILcdGL.GL_BGRA,
                         ILcdGL.GL_UNSIGNED_BYTE,
                         fIntBuffer
      );
    } else {

      gl.glTexParameteri(ILcdGL.GL_TEXTURE_2D, ILcdGL.GL_GENERATE_MIPMAP, ILcdGL.GL_FALSE);
      gl.glTexParameteri(ILcdGL.GL_TEXTURE_2D, ILcdGL.GL_TEXTURE_MIN_FILTER, ILcdGL.GL_LINEAR);
      gl.glTexParameteri(ILcdGL.GL_TEXTURE_2D, ILcdGL.GL_TEXTURE_MAG_FILTER, ILcdGL.GL_LINEAR);
      gl.glTexParameteri(ILcdGL.GL_TEXTURE_2D, ILcdGL.GL_TEXTURE_WRAP_S, ILcdGL.GL_CLAMP_TO_EDGE);
      gl.glTexParameteri(ILcdGL.GL_TEXTURE_2D, ILcdGL.GL_TEXTURE_WRAP_T, ILcdGL.GL_CLAMP_TO_EDGE);

      gl.glTexImage2D(
          ILcdGL.GL_TEXTURE_2D,
          0,
          ILcdGL.GL_RGBA,
          getWidth(), getHeight(), 0,
          ILcdGL.GL_BGRA,
          ILcdGL.GL_UNSIGNED_BYTE,
          fIntBuffer
      );
    }

    fIntBuffer = null;
    fDirty = false;
    fTextureCreated = true;
  }

  @Override
  public void bind(ILcdGLDrawable aGLDrawable) {
    super.bind(aGLDrawable);
    if (fDirty && fIntBuffer != null) {
      upload(aGLDrawable);
    }
  }

  @Override
  public void destroy(ILcdGLDrawable aGLDrawable) {
    super.destroy(aGLDrawable);
    fIntBuffer = null;
    fDirty = false;
  }

  public long getBytes() {
    return fBytes;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    IntBufferTextureObject that = (IntBufferTextureObject) o;

    if (fHash != that.fHash) {
      return false;
    }
    if (getHeight() != that.getHeight()) {
      return false;
    }
    if (getWidth() != that.getWidth()) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int result = getWidth();
    result = 31 * result + getHeight();
    result = 31 * result + fHash;
    return result;
  }
}

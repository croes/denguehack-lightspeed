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
package samples.lightspeed.debug;

import java.nio.Buffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import com.luciad.util.logging.ILcdLogger;
import com.luciad.util.logging.TLcdLoggerFactory;
import com.luciad.view.lightspeed.services.vertexarray.TLspPrimitiveType;
import com.luciad.view.opengl.binding.ILcdGL;
import com.luciad.view.opengl.binding.TLcdGLPassThroughGL;

/**
 * A {@code ILcdGL} implementation that keeps track of how many vertices have
 * been rendered.
 * <p/>
 * Note that this implementation should at least track all methods that are used
 * in LuciadLightspeed, which might not include all methods that are available.
 */
public class StatisticsGL extends TLcdGLPassThroughGL {

  private static final ILcdLogger LOGGER = TLcdLoggerFactory.getLogger(TLcdGLPassThroughGL.class);

  private MutableStatistics fFrameStatistics;

  private int fDisplayList;
  private MutableStatistics[] fDisplayLists;
  private MutableStatistics fDisplayListStatistics;
  private int fListBase = 0;

  public StatisticsGL(ILcdGL aDelegate) {
    super(aDelegate);
    fFrameStatistics = new MutableStatistics();
    fDisplayLists = new MutableStatistics[0];
  }

  /**
   * Resets the statistics counters.
   */
  public void resetCounters() {
    fFrameStatistics.resetCounters();
  }

  /**
   * Called to signal a number of vertices have been added.
   * @param aCount the number of vertices that was added
   * @param aMode  the mode
   */
  private void verticesAdded(int aCount, int aMode) {
    if (fDisplayListStatistics != null) {
      fDisplayListStatistics.verticesAdded(aCount, aMode);
    } else {
      fFrameStatistics.verticesAdded(aCount, aMode);
    }
  }

  /**
   * Called to signal a number of vertices have been added.
   * @param aCount the number of vertices that was added
   */
  private void verticesAdded(int aCount) {
    if (fDisplayListStatistics != null) {
      fDisplayListStatistics.verticesAdded(aCount);
    } else {
      fFrameStatistics.verticesAdded(aCount);
    }
  }

  /**
   * Returns the number of vertices that have been rendered since the last call
   * to {@link #resetCounters()}.
   */
  public int getVertexCount() {
    return fFrameStatistics.getVertexCount();
  }

  /**
   * Returns the number of triangles that have been rendered since the last call
   * to {@link #resetCounters()}.
   */
  public int getTriangleCount() {
    return fFrameStatistics.getTriangleCount();
  }

  /**
   * Returns the number of lines that have been rendered since the last call
   * to {@link #resetCounters()}.
   */
  public int getLineCount() {
    return fFrameStatistics.getLineCount();
  }

  /**
   * Returns the number of points that have been rendered since the last call
   * to {@link #resetCounters()}.
   */
  public int getPointCount() {
    return fFrameStatistics.getPointCount();
  }

  /**
   * Returns the number of vertices that have been rendered for which the primitive type is unknown
   * since the last call to {@link #resetCounters()}.
   */
  public int getUnknownPrimitiveVertexCount() {
    return fFrameStatistics.getUnknownPrimitiveVertexCount();
  }

  //*************************************************************************//

  private static class MutableStatistics {
    private int fVertexCounter;
    private int fUnknownPrimitiveVertexCounter;
    private int fTriangleCounter;
    private int fLineCounter;
    private int fPointCounter;

    public void verticesAdded(int aCount, int aMode) {
      verticesAdded(aCount, TLspPrimitiveType.getPrimitiveType(aMode));
    }

    private void verticesAdded(int aCount, TLspPrimitiveType aPrimitiveType) {
      fVertexCounter += aCount;
      if (aPrimitiveType == TLspPrimitiveType.TRIANGLES) {
        fTriangleCounter += aCount / 3;
      } else if (aPrimitiveType == TLspPrimitiveType.TRIANGLE_FAN || aPrimitiveType == TLspPrimitiveType.TRIANGLE_STRIP) {
        fTriangleCounter += Math.max(0, aCount - 2);
      } else if (aPrimitiveType == TLspPrimitiveType.QUADS) {
        fTriangleCounter += (aCount * 2) / 3;
      } else if (aPrimitiveType == TLspPrimitiveType.QUAD_STRIP) {
        fTriangleCounter += Math.max(0, aCount - 2);
      } else if (aPrimitiveType == TLspPrimitiveType.POLYGON) {
        fTriangleCounter += Math.max(0, aCount - 2);
      } else if (aPrimitiveType == TLspPrimitiveType.LINES) {
        fLineCounter += aCount / 2;
      } else if (aPrimitiveType == TLspPrimitiveType.LINE_STRIP) {
        fLineCounter += Math.max(0, aCount - 1);
      } else if (aPrimitiveType == TLspPrimitiveType.LINE_LOOP) {
        fLineCounter += aCount;
      } else if (aPrimitiveType == TLspPrimitiveType.POINTS) {
        fPointCounter += aCount;
      }
    }

    public void verticesAdded(int aCount) {
      fVertexCounter += aCount;
      fUnknownPrimitiveVertexCounter += aCount;
    }

    public void resetCounters() {
      fVertexCounter = 0;
      fTriangleCounter = 0;
    }

    public int getVertexCount() {
      return fVertexCounter;
    }

    public int getTriangleCount() {
      return fTriangleCounter;
    }

    public int getLineCount() {
      return fLineCounter;
    }

    public int getPointCount() {
      return fPointCounter;
    }

    public int getUnknownPrimitiveVertexCount() {
      return fUnknownPrimitiveVertexCounter;
    }

    public void merge(MutableStatistics aOther) {
      if (aOther != null) {
        fVertexCounter += aOther.getVertexCount();
        fTriangleCounter += aOther.getTriangleCount();
        fLineCounter += aOther.getLineCount();
        fPointCounter += aOther.getPointCount();
        fUnknownPrimitiveVertexCounter += aOther.getUnknownPrimitiveVertexCount();
      }
    }
  }

  //*************************************************************************//

  /**
   * Called to indicate the start of a display list.
   * @param aDisplayList the display list identifier
   */
  private void displayListStarted(int aDisplayList) {
    fDisplayList = aDisplayList;
    fDisplayListStatistics = new MutableStatistics();
  }

  /**
   * Called to indicate the end of a display list.
   */
  private void displayListEnded() {
    if (fDisplayLists.length <= fDisplayList) {
      MutableStatistics[] newDisplayLists = new MutableStatistics[fDisplayList + 1];
      System.arraycopy(fDisplayLists, 0, newDisplayLists, 0, fDisplayLists.length);
      fDisplayLists = newDisplayLists;
    }

    fDisplayLists[fDisplayList] = fDisplayListStatistics;
    fDisplayListStatistics = null;
    fDisplayList = 0;
  }

  /**
   * Called to indicate a display list was deleted.
   * @param aDisplayList the display list identifier
   */
  private void displayListDeleted(int aDisplayList) {
    if (aDisplayList < fDisplayLists.length) {
      fDisplayLists[aDisplayList] = null;
    }
  }

  /**
   * Called to indicate the display list base was changed
   * @param aListBase the new display list base
   */
  private void displayListBaseChanged(int aListBase) {
    fListBase = aListBase;
  }

  /**
   * Called to indicate a display list was executed.
   * @param aList the display list identifier
   */
  private void displayListCalled(int aList) {
    try {
      fFrameStatistics.merge(fDisplayLists[aList]);
    } catch (ArrayIndexOutOfBoundsException e) {
      if (fDisplayLists.length <= aList) {
        MutableStatistics[] newDisplayLists = new MutableStatistics[aList + 1];
        System.arraycopy(fDisplayLists, 0, newDisplayLists, 0, fDisplayLists.length);
        fDisplayLists = newDisplayLists;
      }
    }
  }

  public void glNewList(int aList, int aMode) {
    super.glNewList(aList, aMode);
    displayListStarted(aList);
  }

  public void glEndList() {
    super.glEndList();
    displayListEnded();
  }

  public void glDeleteLists(int aList, int aRange) {
    super.glDeleteLists(aList, aRange);
    for (int i = 0; i < aRange; i++) {
      displayListDeleted(aList + i);
    }
  }

  public void glListBase(int aListBase) {
    super.glListBase(aListBase);
    displayListBaseChanged(aListBase);
  }

  public void glCallList(int aList) {
    super.glCallList(aList);
    displayListCalled(aList);
  }

  public void glCallLists(int aCount, int aType, Buffer aLists) {
    super.glCallLists(aCount, aType, aLists);
    if (aLists instanceof IntBuffer) {
      IntBuffer lists = (IntBuffer) aLists;
      for (int i = 0; i < aCount; i++) {
        displayListCalled(fListBase + lists.get(i));
      }
    } else {
      LOGGER.warn("glCallLists() is only tracked when called with an IntBuffer argument!");
    }
  }

  //*************************************************************************//

  public void glDrawArrays(int mode, int i1, int count) {
    super.glDrawArrays(mode, i1, count);
    verticesAdded(count, mode);
  }

  public void glDrawElements(int mode, int count, int i2, Buffer aBuffer) {
    super.glDrawElements(mode, count, i2, aBuffer);
    verticesAdded(count, mode);
  }

  public void glDrawElements(int mode, int count, int i2, long l) {
    super.glDrawElements(mode, count, i2, l);
    verticesAdded(count, mode);
  }

  public void glDrawRangeElements(int mode, int i1, int i2, int count, int i4, Buffer aBuffer) {
    super.glDrawRangeElements(mode, i1, i2, count, i4, aBuffer);
    verticesAdded(count, mode);
  }

  public void glDrawRangeElements(int mode, int i1, int i2, int count, int i4, long l) {
    super.glDrawRangeElements(mode, i1, i2, count, i4, l);
    verticesAdded(count, mode);
  }

  @Override
  public void glDrawElementsBaseVertex(int aMode, int aCount, int aType, Buffer aIndices, int aBaseVertex) {
    verticesAdded(aCount, aMode);
    super.glDrawElementsBaseVertex(aMode, aCount, aType, aIndices, aBaseVertex);
  }

  public void glArrayElement(int i) {
    super.glArrayElement(i);
    verticesAdded(1);
  }

  public void glVertex2d(double v, double v1) {
    super.glVertex2d(v, v1);
    verticesAdded(1);
  }

  public void glVertex2dv(DoubleBuffer aDoubleBuffer) {
    super.glVertex2dv(aDoubleBuffer);
    verticesAdded(1);
  }

  public void glVertex2dv(double[] aDoubles, int i) {
    super.glVertex2dv(aDoubles, i);
    verticesAdded(1);
  }

  public void glVertex2f(float v, float v1) {
    super.glVertex2f(v, v1);
    verticesAdded(1);
  }

  public void glVertex2fv(FloatBuffer aFloatBuffer) {
    super.glVertex2fv(aFloatBuffer);
    verticesAdded(1);
  }

  public void glVertex2fv(float[] aFloats, int i) {
    super.glVertex2fv(aFloats, i);
    verticesAdded(1);
  }

  public void glVertex2i(int i, int i1) {
    super.glVertex2i(i, i1);
    verticesAdded(1);
  }

  public void glVertex2iv(IntBuffer aIntBuffer) {
    super.glVertex2iv(aIntBuffer);
    verticesAdded(1);
  }

  public void glVertex2iv(int[] aInts, int i) {
    super.glVertex2iv(aInts, i);
    verticesAdded(1);
  }

  public void glVertex2s(short i, short i1) {
    super.glVertex2s(i, i1);
    verticesAdded(1);
  }

  public void glVertex2sv(ShortBuffer aShortBuffer) {
    super.glVertex2sv(aShortBuffer);
    verticesAdded(1);
  }

  public void glVertex2sv(short[] aShorts, int i) {
    super.glVertex2sv(aShorts, i);
    verticesAdded(1);
  }

  public void glVertex3d(double v, double v1, double v2) {
    super.glVertex3d(v, v1, v2);
    verticesAdded(1);
  }

  public void glVertex3dv(DoubleBuffer aDoubleBuffer) {
    super.glVertex3dv(aDoubleBuffer);
    verticesAdded(1);
  }

  public void glVertex3dv(double[] aDoubles, int i) {
    super.glVertex3dv(aDoubles, i);
    verticesAdded(1);
  }

  public void glVertex3f(float v, float v1, float v2) {
    super.glVertex3f(v, v1, v2);
    verticesAdded(1);
  }

  public void glVertex3fv(FloatBuffer aFloatBuffer) {
    super.glVertex3fv(aFloatBuffer);
    verticesAdded(1);
  }

  public void glVertex3fv(float[] aFloats, int i) {
    super.glVertex3fv(aFloats, i);
    verticesAdded(1);
  }

  public void glVertex3i(int i, int i1, int i2) {
    super.glVertex3i(i, i1, i2);
    verticesAdded(1);
  }

  public void glVertex3iv(IntBuffer aIntBuffer) {
    super.glVertex3iv(aIntBuffer);
    verticesAdded(1);
  }

  public void glVertex3iv(int[] aInts, int i) {
    super.glVertex3iv(aInts, i);
    verticesAdded(1);
  }

  public void glVertex3s(short i, short i1, short i2) {
    super.glVertex3s(i, i1, i2);
    verticesAdded(1);
  }

  public void glVertex3sv(ShortBuffer aShortBuffer) {
    super.glVertex3sv(aShortBuffer);
    verticesAdded(1);
  }

  public void glVertex3sv(short[] aShorts, int i) {
    super.glVertex3sv(aShorts, i);
    verticesAdded(1);
  }

  public void glVertex4d(double v, double v1, double v2, double v3) {
    super.glVertex4d(v, v1, v2, v3);
    verticesAdded(1);
  }

  public void glVertex4dv(DoubleBuffer aDoubleBuffer) {
    super.glVertex4dv(aDoubleBuffer);
    verticesAdded(1);
  }

  public void glVertex4dv(double[] aDoubles, int i) {
    super.glVertex4dv(aDoubles, i);
    verticesAdded(1);
  }

  public void glVertex4f(float v, float v1, float v2, float v3) {
    super.glVertex4f(v, v1, v2, v3);
    verticesAdded(1);
  }

  public void glVertex4fv(FloatBuffer aFloatBuffer) {
    super.glVertex4fv(aFloatBuffer);
    verticesAdded(1);
  }

  public void glVertex4fv(float[] aFloats, int i) {
    super.glVertex4fv(aFloats, i);
    verticesAdded(1);
  }

  public void glVertex4i(int i, int i1, int i2, int i3) {
    super.glVertex4i(i, i1, i2, i3);
    verticesAdded(1);
  }

  public void glVertex4iv(IntBuffer aIntBuffer) {
    super.glVertex4iv(aIntBuffer);
    verticesAdded(1);
  }

  public void glVertex4iv(int[] aInts, int i) {
    super.glVertex4iv(aInts, i);
    verticesAdded(1);
  }

  public void glVertex4s(short i, short i1, short i2, short i3) {
    super.glVertex4s(i, i1, i2, i3);
    verticesAdded(1);
  }

  public void glVertex4sv(ShortBuffer aShortBuffer) {
    super.glVertex4sv(aShortBuffer);
    verticesAdded(1);
  }

  public void glVertex4sv(short[] aShorts, int i) {
    super.glVertex4sv(aShorts, i);
    verticesAdded(1);
  }
}

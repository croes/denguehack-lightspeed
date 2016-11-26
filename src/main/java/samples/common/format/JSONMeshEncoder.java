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
package samples.common.format;

import java.io.IOException;
import java.nio.Buffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;

import com.luciad.shape.ILcdPoint;

public class JSONMeshEncoder {

  /**
   * Creates a JSON file with properties "location", "positions", "indices" and optionally "colors".
   *
   * For example:
   * <pre class="code">
   *   {
   *     "location": [6, 50, 100],
   *     "positions": [0, 0, 0,   //Vertex 0
   *                   10, 0, 0,  //Vertex 1
   *                   0, 10, 0], //Vertex 2
   *     "indices": [0, 1, 2]     //Triangle 0 - 1 - 2
   *   }
   * </pre>
   *
   * Note that unlike in the example above, the file is not formatted in such a readable way.
   *
   * @param aFileName the file name
   * @param aLocation the location of the mesh
   * @param aPositions the positions of the mesh
   * @param aIndices the indices of the mesh
   * @param aColors the vertex colors of the mesh
   */
  public void exportMeshData(String aFileName, ILcdPoint aLocation, Buffer aPositions, Buffer aIndices, Buffer aColors) {
    StringBuilder stringBuilder = new StringBuilder();
    stringBuilder.append("{");

    appendPropertyIdentifier(stringBuilder, "location");
    stringBuilder.append("[");
    stringBuilder.append(aLocation.getX());
    stringBuilder.append(", ");
    stringBuilder.append(aLocation.getY());
    stringBuilder.append(", ");
    stringBuilder.append(aLocation.getZ());
    stringBuilder.append("]");
    stringBuilder.append(",");

    appendBuffer(stringBuilder, "positions", aPositions);

    stringBuilder.append(",");
    appendBuffer(stringBuilder, "indices", aIndices);

    if (aColors != null && aColors.hasRemaining()) {
      stringBuilder.append(",");
      appendBuffer(stringBuilder, "colors", aColors);
    }

    stringBuilder.append("}");

    Path path = Paths.get(aFileName);
    try {
      Files.write(path, Collections.singleton(stringBuilder.toString()), Charset.forName("UTF-8"));
    } catch (IOException e) {
      throw new RuntimeException("Could not export mesh");
    }
  }

  private void appendBuffer(StringBuilder aStringBuilder, String propertyName, Buffer aBuffer) {
    appendPropertyIdentifier(aStringBuilder, propertyName);
    aStringBuilder.append("[ ");
    while (aBuffer.hasRemaining()) {
      appendBufferValue(aStringBuilder, aBuffer);
      aStringBuilder.append(",");
    }
    aStringBuilder.deleteCharAt(aStringBuilder.length() - 1);
    aStringBuilder.append("]");
  }

  private void appendPropertyIdentifier(StringBuilder aStringBuilder, String propertyName) {
    aStringBuilder.append("\"");
    aStringBuilder.append(propertyName);
    aStringBuilder.append("\": ");
  }

  private void appendBufferValue(StringBuilder aStringBuilder, Buffer aBuffer) {
    if (aBuffer instanceof DoubleBuffer) {
      double number = ((DoubleBuffer) aBuffer).get();
      aStringBuilder.append(Double.toString(number));
    } else if (aBuffer instanceof FloatBuffer){
      float number = ((FloatBuffer) aBuffer).get();
      aStringBuilder.append(Float.toString(number));
    } else if (aBuffer instanceof IntBuffer){
      int number = ((IntBuffer) aBuffer).get();
      aStringBuilder.append(Integer.toString(number));
    } else {
      throw new IllegalArgumentException("Could not export mesh");
    }
  }

}

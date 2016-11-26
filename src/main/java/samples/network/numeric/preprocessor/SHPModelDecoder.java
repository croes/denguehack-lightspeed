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
package samples.network.numeric.preprocessor;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.luciad.format.shp.TLcdSHPModelDecoder;
import com.luciad.model.ILcd2DBoundsIndexedModel;
import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelDecoder;
import com.luciad.model.TLcd2DBoundsIndexedModelList;

/**
 * A decoder that decodes a directory with SHP files (recursively, with subdirectories).
 * <p/>
 * The directory structure should contain:
 * <ul>
 * <li>edge files: ending with edges.shp</li>
 * <li>node files: ending with nodes.shp</li>
 * </ul>
 */
public class SHPModelDecoder implements ILcdModelDecoder {

  private TLcdSHPModelDecoder fModelDecoder = new TLcdSHPModelDecoder();

  // Implementations for ILcdModelDecoder.

  public boolean canDecodeSource(String aSourceName) {
    return false;
  }

  public String getDisplayName() {
    return null;
  }

  public ILcdModel decode(String aSourceName) throws IOException {
    TLcd2DBoundsIndexedModelList model = new TLcd2DBoundsIndexedModelList();
    model.addModel(createModel(collectFiles(aSourceName, "edges.shp")));
    model.addModel(createModel(collectFiles(aSourceName, "nodes.shp")));
    return model;
  }

  private ILcdModel createModel(List<File> aFiles) throws IOException {
    TLcd2DBoundsIndexedModelList modelList = new TLcd2DBoundsIndexedModelList();

    for (File file : aFiles) {
      ILcd2DBoundsIndexedModel model = (ILcd2DBoundsIndexedModel) fModelDecoder.decode(file.getAbsolutePath());
      modelList.addModel(model);
    }

    return modelList;
  }

  private static List<File> collectFiles(String aDirectory, final String aSuffix) {
    List<File> files = new ArrayList<File>();

    File dir = new File(aDirectory);
    File[] edgeFiles = dir.listFiles();
    Arrays.sort(edgeFiles);
    for (File file : edgeFiles) {
      if (file.isDirectory()) {
        files.addAll(collectFiles(file.getAbsolutePath(), aSuffix));
      } else if (file.getName().endsWith(aSuffix)) {
        files.add(file);
      }
    }

    return files;
  }
}

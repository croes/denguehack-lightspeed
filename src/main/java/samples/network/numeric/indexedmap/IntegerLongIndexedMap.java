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
package samples.network.numeric.indexedmap;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.luciad.io.ILcdInputStreamFactory;
import com.luciad.io.TLcdIOUtil;

/**
 * A map from integer to long values, which is additionally indexed to be able to perform fast
 * look-ups in large maps.
 */
public class IntegerLongIndexedMap {

  private String fBaseDir;
  private String fSourceName;
  private ILcdInputStreamFactory fInputStreamFactory;

  private List<Integer> fBaseIndices = new ArrayList<Integer>();
  private List<SoftReference<SubMap>> fIndexList = new ArrayList<SoftReference<SubMap>>();

  public IntegerLongIndexedMap(String aSourceName,
                               ILcdInputStreamFactory aInputStreamFactory) throws IOException {
    fBaseDir = TLcdIOUtil.getDirectoryPath(aSourceName);
    fSourceName = TLcdIOUtil.getFileName(aSourceName);
    fInputStreamFactory = aInputStreamFactory;

    DataInputStream dis = new DataInputStream(new BufferedInputStream(fInputStreamFactory.createInputStream(aSourceName)));
    while (dis.available() > 0) {
      fBaseIndices.add(dis.readInt());
      fIndexList.add(null);
    }
  }

  public long getValue(int aKey) {
    int fileIndex = 0;
    while (fileIndex < fBaseIndices.size() && fBaseIndices.get(fileIndex) <= aKey) {
      fileIndex++;
    }
    return getSubMap(fileIndex - 1).getValue(aKey);
  }

  private SubMap getSubMap(int aIndex) {
    SubMap page = null;
    if (fIndexList.get(aIndex) != null) {
      page = fIndexList.get(aIndex).get();
    }
    if (page == null) {
      try {
        page = readSubMap(aIndex);
      } catch (IOException e) {
        e.printStackTrace();
      }
      fIndexList.set(aIndex, new SoftReference<SubMap>(page));
    }
    return page;
  }

  private SubMap readSubMap(int aIndex) throws IOException {
    File file = new File(fBaseDir, fSourceName.substring(0, fSourceName.lastIndexOf('.')) + aIndex + ".map");
    DataInputStream dis = new DataInputStream(new BufferedInputStream(fInputStreamFactory.createInputStream(file.getAbsolutePath())));

    int nrValues = (int) file.length() / 12;
    int[] keys = new int[nrValues];
    long[] values = new long[nrValues];
    for (int i = 0; i < nrValues; i++) {
      keys[i] = dis.readInt();
      values[i] = dis.readLong();
    }
    SubMap page = new SubMap(keys, values);
    return page;
  }

  private static class SubMap {

    private int[] fKeys;
    private long[] fValues;

    private SubMap(int[] aKeys, long[] aValues) {
      fKeys = aKeys;
      fValues = aValues;
    }

    public long getValue(int aKey) {
      return fValues[Arrays.binarySearch(fKeys, aKey)];
    }
  }

}

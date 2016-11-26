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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.SortedMap;
import java.util.TreeMap;

import com.luciad.io.ILcdInputStreamFactory;
import com.luciad.io.ILcdOutputStreamFactory;
import com.luciad.io.TLcdFileOutputStreamFactory;
import com.luciad.io.TLcdInputStreamFactory;

/**
 * An encoder for converting a single file containing a long-long mapping into an indexed
 * structure with multiple smaller files.
 */
public class LongLongIndexedMapEncoder {

  private ILcdInputStreamFactory fInputStreamFactory = new TLcdInputStreamFactory();
  private ILcdOutputStreamFactory fOutputStreamFactory = new TLcdFileOutputStreamFactory();

  public void buildIndexedMap(String aSourceName,
                              String aDestinationName) throws IOException {
    SortedMap<Long, Long> map = new TreeMap<Long, Long>();
    DataInputStream dis1 = new DataInputStream(fInputStreamFactory.createInputStream(aSourceName));
    while (dis1.available() > 0) {
      long key = dis1.readLong();
      long value = dis1.readLong();
      map.put(key, value);
    }
    int counter = 0;
    int fileCounter = 0;
    DataOutputStream dos1 = null;
    DataOutputStream dos2 = new DataOutputStream(fOutputStreamFactory.createOutputStream(aDestinationName));

    for (SortedMap.Entry<Long, Long> entry : map.entrySet()) {
      if (counter % 10000 == 0) {
        if (dos1 != null) {
          dos1.close();
        }
        String destination = aDestinationName.substring(0, aDestinationName.lastIndexOf('.')) + fileCounter + ".map";
        dos1 = new DataOutputStream(fOutputStreamFactory.createOutputStream(destination));
        dos2.writeLong(entry.getKey());
        fileCounter++;
      }
      dos1.writeLong(entry.getKey());
      dos1.writeLong(entry.getValue());
      counter++;
    }
    map.clear();

    if (dos1 != null) {
      dos1.close();
    }
    dos2.close();
  }
}

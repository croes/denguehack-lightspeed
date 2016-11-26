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
package samples.wms.client.ecdis.gxy;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class S57ObjectClassMapDecoder {

  public static List<ObjectClass> decodeObjectClassMap(String aSource) throws IOException {
    List<ObjectClass> objectClassList = new ArrayList<ObjectClass>();
    InputStream is = S57ObjectClassMapDecoder.class.getClassLoader().getResourceAsStream(aSource);
    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
    String line;
    while ((line = reader.readLine()) != null) {
      StringTokenizer tokenizer = new StringTokenizer(line, " ");
      int code = Integer.parseInt(tokenizer.nextToken());
      String acronym = tokenizer.nextToken();
      objectClassList.add(new ObjectClass(code, acronym));
    }
    return objectClassList;
  }

  public static class ObjectClass {

    private int fCode;
    private String fAcronym;

    public ObjectClass(int aCode, String aAcronym) {
      fCode = aCode;
      fAcronym = aAcronym;
    }

    public int getCode() {
      return fCode;
    }

    public String getAcronym() {
      return fAcronym;
    }

    @Override
    public String toString() {
      return fAcronym;
    }
  }

}

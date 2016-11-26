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
package samples.opengl.terrain.onthefly;

import com.luciad.io.*;

import java.io.*;
import java.util.Hashtable;

/**
 * This class implements both ILcdInputStreamFactory and
 * ILcdOutputStreamFactory. It is used to preprocess a terrain in memory. A
 * hash table is used to couple each created OutputStream to an InputStream
 * that can retrieve the written data later on.
 */
class IOStreamFactory implements ILcdInputStreamFactory, ILcdOutputStreamFactory {

  private Hashtable fOutputs = new Hashtable();

  public InputStream createInputStream( final String aFileName ) throws IOException {

    OutputStream out = (OutputStream) fOutputs.get( aFileName );
    if ( out != null ) {
      out.flush();
      out.close();
      ByteArrayInputStream in = new ByteArrayInputStream( ( (ByteArrayOutputStream) out ).toByteArray() ) {
        public String name = aFileName;
      };
      return in;
    }
    return null;
  }

  public OutputStream createOutputStream( final String aFileName ) throws IOException {

    OutputStream out = (OutputStream) fOutputs.get( aFileName );
    if ( out == null ) {
      out = new ByteArrayOutputStream() {
        public String name = aFileName;
      };
      fOutputs.put( aFileName, out );
    }
    return out;
  }
}

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
package samples.gxy.common;

import java.io.IOException;

import com.luciad.model.ILcdModelReference;
import com.luciad.model.ILcdModelReferenceDecoder;
import com.luciad.reference.TLcdModelReferenceDecoder;
import com.luciad.reference.format.TLcdEPSGModelReferenceDecoder;
import com.luciad.reference.format.TLcdWKTModelReferenceDecoder;

/**
 * Composite ILcdModelReferenceDecoder.
 * Will first try to decode the model reference from a <code>.prj</code>
 * file and will fall back on <code>.ref</code> file if unsuccessful.
 */
public class CompositeModelReferenceDecoder implements ILcdModelReferenceDecoder {

  private ILcdModelReferenceDecoder[] fModelReferenceDecoders = new ILcdModelReferenceDecoder[]{
      new TLcdEPSGModelReferenceDecoder(),
      new TLcdWKTModelReferenceDecoder(),
      new TLcdModelReferenceDecoder(),
  };

  public ILcdModelReference decodeModelReference(String aSource) throws IOException {
    for (int i = 0; i < fModelReferenceDecoders.length; i++) {
      try {
        return fModelReferenceDecoders[i].decodeModelReference(aSource);
      } catch (IOException e) {
        // unable to decode using the current model reference decoder.
        // try the next one...
      }
    }
    throw new IOException("Could not decode model reference for [" + aSource + "].");
  }

}

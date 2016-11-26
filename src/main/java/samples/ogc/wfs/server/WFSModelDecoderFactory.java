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
package samples.ogc.wfs.server;

import com.luciad.format.gml31.xml.TLcdGML31ModelDecoder;
import com.luciad.format.shp.TLcdSHPModelDecoder;
import com.luciad.model.ILcdModelDecoder;
import com.luciad.ogc.common.ILcdOGCModelDecoderFactory;

/**
 * A simple <code>ILcdOGCModelDecoderFactory</code> implementation that reads SHP and GML 3.1.1
 * files.
 */
class WFSModelDecoderFactory implements ILcdOGCModelDecoderFactory {

  private TLcdSHPModelDecoder fSHPModelDecoder;
  private TLcdGML31ModelDecoder fGML31ModelDecoder;

  public WFSModelDecoderFactory() {
    fSHPModelDecoder = new TLcdSHPModelDecoder();
    fSHPModelDecoder.setWithLazyFeaturing(true);
    fGML31ModelDecoder = new TLcdGML31ModelDecoder();
  }

  public ILcdModelDecoder createModelDecoder(String aSource) {

    if (fSHPModelDecoder.canDecodeSource(aSource)) {
      return fSHPModelDecoder;
    } else if (fGML31ModelDecoder.canDecodeSource(aSource)) {
      return fGML31ModelDecoder;
    }

    return null;
  }
}

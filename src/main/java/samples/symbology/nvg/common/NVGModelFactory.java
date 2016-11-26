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
package samples.symbology.nvg.common;

import java.io.IOException;
import java.net.URISyntaxException;

import com.luciad.format.nvg.xml.TLcdNVGModelDecoder;
import com.luciad.io.TLcdIOUtil;
import com.luciad.model.ILcdModel;

/**
 * Model factory for NVG samples
 */
public class NVGModelFactory {

  private NVGModelFactory() {
    throw new AssertionError("No instances allowed");
  }

  /**
   * Decodes and creates an NVG model for the provided source file
   * @return An <code>ILcdModel</code> which has NVG data
   * @throws IOException
   */
  public static ILcdModel createNVGModel() throws IOException {
    //Decode the sample file as a file instead of a resource so the model encoder can find back the file location
    TLcdIOUtil ioUtil = new TLcdIOUtil();
    ioUtil.setSourceName("Data/NVG/NVG20/San_Francisco_invasion.nvg");
    try {
      String fileName = ioUtil.getURL() != null ? ioUtil.getURL().toURI().getPath() : ioUtil.getFileName();
      ILcdModel model = new TLcdNVGModelDecoder().decode(fileName);
      return model;
    } catch (URISyntaxException e) {
      throw new IOException("Couldn't find the path of the sample file",e);
    }
  }
}

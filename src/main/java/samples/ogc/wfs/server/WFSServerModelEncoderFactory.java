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

import java.io.IOException;

import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelEncoder;
import com.luciad.ogc.wfs.ILcdWFSServerModelEncoderFactory;

/**
 * Server-side model encoder factory, used to commit changes made to the data
 * via WFS transactions.
 */
class WFSServerModelEncoderFactory implements ILcdWFSServerModelEncoderFactory {

  public WFSServerModelEncoderFactory() {
  }

  public ILcdModelEncoder createModelEncoder(ILcdModel aModel) throws IllegalArgumentException {
    //Return a model encoder that does not save the file, this way the changes are not persistent. 
    return new ILcdModelEncoder() {

      public void save(ILcdModel aModel) throws IllegalArgumentException, IOException {
      }

      public String getDisplayName() {
        return "Dummy Encoder";
      }

      public void export(ILcdModel aModel, String aDestinationName) throws IllegalArgumentException, IOException {

      }

      public boolean canSave(ILcdModel aModel) {
        return true;
      }

      public boolean canExport(ILcdModel aModel, String aDestinationName) {
        return false;
      }
    };
  }
}

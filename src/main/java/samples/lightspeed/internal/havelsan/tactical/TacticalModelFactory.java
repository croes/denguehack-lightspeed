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
package samples.lightspeed.internal.havelsan.tactical;

import com.luciad.model.ILcdModel;
import com.luciad.model.TLcdVectorModel;
import com.luciad.reference.TLcdGeodeticReference;

/**
 * @author tomn
 * @since 2012.0
 */
public class TacticalModelFactory {

  public static final int UNIT_COUNT = 4000;

  private static final String[] CODES = new String[]{
      "SUAPCF------***",
      "SHAPMF------***",
      "SFAPCH------***",
  };

  private TacticalModelFactory() {
  }

  public static ILcdModel createTacticalModel() {
    final TLcdVectorModel model = new TLcdVectorModel(
        new TLcdGeodeticReference(),
        new TacticalModelDescriptor()
    );

    for (int i = 0; i < UNIT_COUNT; i++) {
      TacticalObject object = new TacticalObject(
          CODES[i % CODES.length],
          -120 + Math.random() * 5,
          37 + Math.random() * 5,
          0.1 + Math.random()
      );
      object.setTime(Math.random());
      model.addElement(object, ILcdModel.NO_EVENT);
    }

    return model;
  }
}

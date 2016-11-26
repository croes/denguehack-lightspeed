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
package samples.lightspeed.density;

import java.io.IOException;

import com.luciad.format.shp.TLcdSHPModelDecoder;
import com.luciad.model.ILcdModel;
import com.luciad.util.logging.ILcdLogger;
import com.luciad.util.logging.TLcdLoggerFactory;

import samples.common.SampleData;
import samples.lightspeed.common.tracks.EnrouteTrajectoryModelFactory;

/**
 * A model factory that produces a trajectories model
 */
public class TrajectoriesModelFactory {

  private static ILcdLogger sLogger = TLcdLoggerFactory.getLogger(TrajectoriesModelFactory.class.getName());

  /**
   * Returns a trajectories model
   * @return a trajectories model
   */
  public static ILcdModel createTrajectoriesModel() {
    TLcdSHPModelDecoder modelDecoder = new TLcdSHPModelDecoder();
    try {
      ILcdModel trajectoriesModel = modelDecoder.decode(SampleData.US_TRAJECTORIES);
      return EnrouteTrajectoryModelFactory.deriveTrajectoriesModel(trajectoriesModel);
    } catch (IOException e) {
      sLogger.error("Make sure that '" + SampleData.US_TRAJECTORIES + "' is in your classpath\n" + e.getMessage());
      System.exit(1);
      return null;
    }
  }
}

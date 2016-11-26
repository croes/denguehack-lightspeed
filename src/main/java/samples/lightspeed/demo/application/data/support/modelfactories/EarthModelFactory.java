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
package samples.lightspeed.demo.application.data.support.modelfactories;

import java.io.IOException;
import java.util.Properties;

import com.luciad.earth.model.TLcdEarthRepositoryModelDecoder;
import com.luciad.earth.tileset.ILcdEarthTileSet;
import com.luciad.earth.tileset.ILcdEarthTileSetCoverage;
import com.luciad.earth.tileset.util.ALcdEarthCoverageFilterTileSet;
import com.luciad.model.ILcdModel;

import samples.lightspeed.demo.framework.data.AbstractModelFactory;

/**
 * Model factory that can create Earth models.
 */
public class EarthModelFactory extends AbstractModelFactory {

  private boolean fElevation;

  public EarthModelFactory(String aType) {
    super(aType);
  }

  @Override
  public void configure(Properties aProperties) {
    super.configure(aProperties);
    fElevation = Boolean.valueOf(aProperties.getProperty("tileset.elevation", "false"));
  }

  @Override
  public ILcdModel createModel(String aSource) throws IOException {
    TLcdEarthRepositoryModelDecoder decoder = new TLcdEarthRepositoryModelDecoder();
    ILcdModel model = decoder.decode(aSource);

    ILcdEarthTileSet tileSet = (ILcdEarthTileSet) model.elements().nextElement();
    ALcdEarthCoverageFilterTileSet filtered;
    if (fElevation) {
      filtered = new ALcdEarthCoverageFilterTileSet(tileSet) {
        @Override
        protected boolean accept(ILcdEarthTileSetCoverage aCoverage) {
          return aCoverage.getCoverageType() == ILcdEarthTileSetCoverage.CoverageType.ELEVATION;
        }
      };
    } else {
      filtered = new ALcdEarthCoverageFilterTileSet(tileSet) {
        @Override
        protected boolean accept(ILcdEarthTileSetCoverage aCoverage) {
          return aCoverage.getCoverageType() != ILcdEarthTileSetCoverage.CoverageType.ELEVATION;
        }
      };
    }
    model.removeAllElements(ILcdModel.NO_EVENT);
    model.addElement(filtered, ILcdModel.NO_EVENT);

    // Return the decoded model
    return model;
  }

}

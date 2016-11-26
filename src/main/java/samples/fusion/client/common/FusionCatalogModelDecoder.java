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
package samples.fusion.client.common;

import static com.luciad.util.logging.TLcdLoggerFactory.getLogger;
import static com.luciad.util.service.LcdService.LOW_PRIORITY;

import java.io.IOException;
import java.util.List;

import com.luciad.fusion.tilestore.metadata.ALfnCoverageMetadata;
import com.luciad.fusion.tilestore.model.ALfnTileStoreModel;
import com.luciad.fusion.tilestore.model.TLfnTileStoreDataSource;
import com.luciad.fusion.tilestore.model.TLfnTileStoreModelDecoder;
import com.luciad.fusion.tilestore.model.TLfnTileStoreModelDescriptor;
import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelDecoder;
import com.luciad.model.TLcdModelTreeNode;
import com.luciad.reference.TLcdGeodeticReference;
import com.luciad.util.logging.ILcdLogger;
import com.luciad.util.service.LcdService;

/**
 * A model decoder that can decode a Tile Store and all its coverages.
 * It uses the {@code LcdService} mechanism to register itself with the default {@code lightspeed.decoder} sample.
 * To fit in the {@code lightspeed.decoder} sample's paradigm, it decodes an {@code TLcdModelTreeNode} for a Tile Store, with child models for all its coverages.
 */
@LcdService(service = ILcdModelDecoder.class, priority = LOW_PRIORITY)
public class FusionCatalogModelDecoder implements ILcdModelDecoder {

  private static final ILcdLogger LOGGER = getLogger(FusionCatalogModelDecoder.class);

  private final TLfnTileStoreModelDecoder fDelegate = new TLfnTileStoreModelDecoder();

  @Override
  public String getDisplayName() {
    return fDelegate.getDisplayName();
  }

  @Override
  public boolean canDecodeSource(String aSource) {
    return fDelegate.canDecodeSource(aSource);
  }

  @Override
  public ILcdModel decode(String aSource) throws IOException {
    ALfnTileStoreModel model = fDelegate.decode(aSource);
    TLfnTileStoreModelDescriptor modelDescriptor = (TLfnTileStoreModelDescriptor) model.getModelDescriptor();
    ALfnCoverageMetadata coverageMetadata = modelDescriptor.getDataSource().getCoverageMetadata();
    if (coverageMetadata != null) {
      return model;
    }
    List<TLfnTileStoreDataSource> dataSources = fDelegate.discoverDataSources(aSource);
    TLcdModelTreeNode catalogModel = new TLcdModelTreeNode();
    catalogModel.setModelDescriptor(new FusionCatalogModelDescriptor(aSource));
    catalogModel.setModelReference(new TLcdGeodeticReference());
    for (TLfnTileStoreDataSource dataSource : dataSources) {
      try {
        ILcdModel coverageModel = fDelegate.decodeSource(dataSource);
        catalogModel.addModel(coverageModel);
      } catch (IOException e) {
        String message = e.getMessage();
        LOGGER.warn("Could not decode coverage: " + dataSource + (message != null ? ": " + message : ""));
      }
    }
    return catalogModel;
  }
}

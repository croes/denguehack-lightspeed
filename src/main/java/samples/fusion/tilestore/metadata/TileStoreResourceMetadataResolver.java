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
package samples.fusion.tilestore.metadata;

import static com.luciad.util.logging.TLcdLoggerFactory.getLogger;

import java.io.IOException;

import com.luciad.fusion.tilestore.ALfnTileStore;
import com.luciad.fusion.tilestore.TLfnServiceException;
import com.luciad.fusion.tilestore.metadata.ALfnResourceMetadata;
import com.luciad.fusion.tilestore.metadata.ILfnResourceMetadataResolver;
import com.luciad.util.logging.ILcdLogger;

/**
 * Implementation of a resource metadata resolver that is backed by a Tile Store.
 * Since resolving resource metadata through a Tile Store is generally fairly expensive, an instance of this class is typically decorated with a {@link CachingResourceMetadataResolver}.
 * <p/>
 * Instances of this class are <em>not</em> thread-safe.
 *
 * @see CachingResourceMetadataResolver
 * @since 10.0
 */
public class TileStoreResourceMetadataResolver implements ILfnResourceMetadataResolver {

  private final ILcdLogger sLogger = getLogger(TileStoreResourceMetadataResolver.class);

  private final ALfnTileStore fTileStore;

  /**
   * Constructs a new resource metadata resolver backed by a given Tile Store.
   *
   * @param aTileStore the Tile Store to back the resource metadata resolver, never {@code null}
   */
  public TileStoreResourceMetadataResolver(ALfnTileStore aTileStore) {
    fTileStore = aTileStore;
  }

  /**
   * Gets the resource metadata through the backing Tile Store. The resource metadata is {@code null} in any of the following cases:
   * <ul>
   * <li>the resource metadata does not exist on the Tile Store</li>
   * <li>an {@link IOException} happened while getting the resource metadata from the Tile Store</li>
   * <li>a {@link TLfnServiceException} happened while getting the resource metadata from the Tile Store</li>
   * </ul>
   * In the latter two cases, a warning will have been logged.
   *
   * @return a resource metadata, possibly {@code null}
   */
  public ALfnResourceMetadata getResourceMetadata(String aId) {
    ALfnResourceMetadata resourceMetadata = null;
    try {
      resourceMetadata = fTileStore.getResourceMetadata(aId);
    } catch (IOException | TLfnServiceException e) {
      sLogger.warn(
          "Failed to get the resource metadata for: " + aId + " from: " + fTileStore + ", returning null instead.", e);
      // Return null.
    }
    return resourceMetadata;
  }

  /**
   * Returns a string representation of this resource metadata resolver.
   *
   * @return a string representation of this resource metadata resolver
   */
  @Override
  public String toString() {
    return getClass().getSimpleName() + "[tileStore=" + fTileStore + "]";
  }
}

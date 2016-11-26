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

import static java.util.Collections.EMPTY_LIST;

import java.io.IOException;
import java.lang.ref.SoftReference;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.luciad.fusion.tilestore.TLfnServiceException;
import com.luciad.fusion.tilestore.metadata.ALfnResourceMetadata;
import com.luciad.fusion.tilestore.metadata.ILfnResourceMetadataResolver;

/**
 * A resource metadata resolver that decorates another resource metadata resolver with a LRU cache.
 * The capacity of the LRU cache can be configured.
 * The caching mechanism is a simple one that does not provide a means to control cache invalidation: a cached resource metadata is never refreshed explicitly.
 * Note that it is possible that cached resources are implicitly refreshed because
 * <ul>
 * <li>some entries from the cache may have been garbage collected since the cache uses soft references</li>
 * <li>the eldest entry was removed from the cache because the cache capacity limit was reached</li>
 * </ul>
 * Use this decorator to reduce the cost of more expensive resource metadata resolvers, for example implementations that do network calls and such.
 * Implementations of this class are <em>not</em> thread-safe.
 *
 * @since 10.0
 */
public class CachingResourceMetadataResolver implements ILfnResourceMetadataResolver {

  private final ILfnResourceMetadataResolver fDelegate;

  private final LinkedHashMap<String, SoftReference<ALfnResourceMetadata>> fCache;

  /**
   * Constructs a new caching resource metadata resolver decorating a delegate resource metadata resolver.
   *
   * @param aDelegate                 a resource metadata resolver to be decorated
   * @param aCapacity                 the capacity of cache, or {@code -1} for unlimited capacity (beware of memory leaks)
   * @param aInitialResourceMetadatas the initial resource metadatas to populate the cache with, may be empty but never {@code null}.
   *                                  Note that cache capacity applies to the initial resource metadatas as well: when the number of initial resource metadatas exceeds the cache capacity, the first entries will be dropped from the cache
   */
  public CachingResourceMetadataResolver(ILfnResourceMetadataResolver aDelegate, final int aCapacity,
                                         List<ALfnResourceMetadata> aInitialResourceMetadatas) {
    fDelegate = aDelegate;
    fCache = aCapacity < 0 ?
             new LinkedHashMap<String, SoftReference<ALfnResourceMetadata>>() :
             // LRU cache as suggested by the Javadoc of LinkedHashMap.
             new LinkedHashMap<String, SoftReference<ALfnResourceMetadata>>(aCapacity) {

               @Override
               protected boolean removeEldestEntry(Map.Entry<String, SoftReference<ALfnResourceMetadata>> aEldest) {
                 // Remember that, at this point, the new entry has already been added, so it contributes to the size.
                 return size() > aCapacity;
               }
             };
    // Populate the cache with the initial resource metadatas.
    // Note that capacity constraints apply to these initial resource metadatas as well!
    for (ALfnResourceMetadata resourceMetadata : aInitialResourceMetadatas) {
      putIntoCache(resourceMetadata.getId(), resourceMetadata);
    }
  }

  /**
   * Constructs a new caching resource metadata resolver decorating a delegate resource metadata resolver.
   *
   * @param aDelegate a resource metadata resolver to be decorated
   * @param aCapacity the capacity of cache, or {@code -1} for unlimited capacity (beware of memory leaks)
   */
  public CachingResourceMetadataResolver(ILfnResourceMetadataResolver aDelegate, int aCapacity) {
    this(aDelegate, aCapacity, EMPTY_LIST);
  }

  /**
   * Gets the resource metadata from the cache if possible, if not falls back to the delegate and puts it in the cache.
   * Invocation of this method causes the cached entry to become the most recently used (MRU) one.
   * If necessary, the least recently used (LRU) cached entry is dropped from the cache to meet capacity constraints.
   *
   * @return a resource metadata, possibly {@code null}
   */
  public ALfnResourceMetadata getResourceMetadata(String aId) throws IOException, TLfnServiceException {
    ALfnResourceMetadata resourceMetadata = getFromCache(aId);
    if (resourceMetadata != null) {
      return resourceMetadata;
    }
    resourceMetadata = fDelegate.getResourceMetadata(aId);
    // Cache the resource metadata, even if it is null.
    putIntoCache(aId, resourceMetadata);
    return resourceMetadata;
  }

  /**
   * Returns a string representation of this caching resource metadata resolver.
   *
   * @return a string representation of this caching resource metadata resolver
   */
  @Override
  public String toString() {
    return getClass().getSimpleName() + "[cacheSize=" + fCache.size() + ", delegate=" + fDelegate + "]";
  }

  private void putIntoCache(String aId, ALfnResourceMetadata aResourceMetadata) {
    fCache.put(aId, new SoftReference<>(aResourceMetadata));
  }

  private ALfnResourceMetadata getFromCache(String aId) {
    SoftReference<ALfnResourceMetadata> resourceMetadataRef = fCache.get(aId);
    if (resourceMetadataRef == null) {
      return null;
    }
    return resourceMetadataRef.get();
  }
}

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

import java.util.List;

import com.luciad.fusion.tilestore.ALfnTileStore;
import com.luciad.fusion.tilestore.metadata.ALfnResourceMetadata;

/**
 * A handler for receiving updated resource infos when querying a Tile Store for its resources (coverages).
 * To be used in conjunction with {@link QueryPanel}.
 */
public interface ResourceHandler {

  void updateTileStore(ALfnTileStore aTileStore);

  void updateResources(List<ResourceInfo> aResourceInfos);

  /**
   * A business object containing ID, name and type of a resource.
   */
  class ResourceInfo {

    public static final String TYPE_ASSET = "Asset";
    public static final String TYPE_COVERAGE = "Coverage";
    public static final String TYPE_THEME = "Theme";

    private final String fId;

    private final String fName;

    private final String fType;

    private ALfnResourceMetadata fMetadata;

    public ResourceInfo(String aId, String aName, String aType, ALfnResourceMetadata aMetadata) {
      fId = aId;
      fName = aName;
      fType = aType;
      fMetadata = aMetadata;
    }

    public String getId() {
      return fId;
    }

    public String getName() {
      return fName;
    }

    public String getType() {
      return fType;
    }

    public ALfnResourceMetadata getMetadata() {
      return fMetadata;
    }

    @Override
    public String toString() {
      return fName != null ? fName : fId;
    }
  }
}

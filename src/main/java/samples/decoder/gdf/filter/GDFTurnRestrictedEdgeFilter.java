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
package samples.decoder.gdf.filter;

import com.luciad.format.gdf.ILcdGDFFeature;
import com.luciad.format.gdf.ILcdGDFLineFeature;
import com.luciad.format.gdf.ILcdGDFRelationship;
import com.luciad.util.ILcdFilter;

/**
 * This sample filter filters out all GDF line features that are involved in a
 * turn restriction. Turn restrictions are stored in GDF as relationships with
 * relationship type 'Prohibited Manoeuvre' (relationship type code 2103).
 */
public class GDFTurnRestrictedEdgeFilter implements ILcdFilter {

  public boolean accept(Object aObject) {
    ILcdGDFFeature feature = (ILcdGDFFeature) aObject;
    if (feature instanceof ILcdGDFLineFeature) {
      for (int i = 0; i < feature.getRelationshipCount(); i++) {
        ILcdGDFRelationship relationship = feature.getRelationship(i);
        if (relationship.getRelationshipType().getRelationshipTypeCode() == 2103) {
          return true;
        }
      }
    }
    return false;
  }
}

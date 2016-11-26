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

import com.luciad.fusion.client.view.gxy.TLfnGXYVectorLayer;
import com.luciad.view.TLcdLabelIdentifier;
import com.luciad.view.gxy.ILcdAWTPath;
import com.luciad.view.gxy.ILcdGXYContext;
import com.luciad.view.gxy.labeling.algorithm.discrete.ILcdGXYLabelingPathProvider;

/**
 * A {@linkplain ILcdGXYLabelingPathProvider} that uses the available tiled geometry
 * to create paths, instead of the original, most detailed geometry.
 *
 * @see TLfnGXYVectorLayer#getTiledGeometry(Object)
 */
public class LabelingPathProviderDecorator implements ILcdGXYLabelingPathProvider {

  private final ILcdGXYLabelingPathProvider fDelegate;
  private final TLfnGXYVectorLayer fLayer;

  public LabelingPathProviderDecorator(ILcdGXYLabelingPathProvider aDelegate, TLfnGXYVectorLayer aLayer) {
    fDelegate = aDelegate;
    fLayer = aLayer;
  }

  public boolean getPathsSFCT(Object aObject, List<TLcdLabelIdentifier> aLabels, ILcdGXYContext aContext,
                              List<ILcdAWTPath> aPaths) {
    Object tiledGeometry = fLayer.getTiledGeometry(aObject);
    return fDelegate.getPathsSFCT(tiledGeometry, aLabels, aContext, aPaths);
  }
}

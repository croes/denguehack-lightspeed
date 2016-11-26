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
package samples.symbology.lightspeed;

import java.beans.PropertyChangeListener;

import com.luciad.model.ILcdModel;
import com.luciad.model.transformation.TLcdTransformingModelFactory;
import com.luciad.model.transformation.clustering.ILcdClassifier;
import com.luciad.model.transformation.clustering.TLcdClusteringTransformer;
import com.luciad.util.ILcdInterval;
import com.luciad.view.lightspeed.layer.ALspSingleLayerFactory;
import com.luciad.view.lightspeed.layer.ILspLayer;

/**
 * A layer factory for models containing military symbols.
 *
 * @since 2016.0
 */
abstract class MilitarySymbologyLayerFactory extends ALspSingleLayerFactory {

  public enum Clustering {ENABLED, DISABLED}

  private final ILcdInterval fLabelScaleRange;
  private final PropertyChangeListener fPropertyChangeListener;
  private final Clustering fClustering;

  public MilitarySymbologyLayerFactory(ILcdInterval aLabelScaleRange, PropertyChangeListener aPropertyChangeListener, Clustering aClustering) {
    fLabelScaleRange = aLabelScaleRange;
    fPropertyChangeListener = aPropertyChangeListener;
    fClustering = aClustering;
  }

  @Override
  public ILspLayer createLayer(ILcdModel aModel) {
    ILcdModel model = aModel;
    if (cluster()) {
      ILcdClassifier classifier = new MilitarySymbolClassifier();
      TLcdClusteringTransformer transformer = TLcdClusteringTransformer.newBuilder()
                                                                       .classifier(classifier)
                                                                       .defaultParameters()
                                                                         .clusterSize(75)
                                                                         .minimumPoints(2)
                                                                         .build()
                                                                       .forClass("SEA")
                                                                         .noClustering()
                                                                         .build()
                                                                       .build();
      model = TLcdTransformingModelFactory.createTransformingModel(aModel, transformer);
    }

    ILspLayer layer = createLayer(model, fLabelScaleRange);
    if (fPropertyChangeListener != null) {
      layer.addPropertyChangeListener(fPropertyChangeListener);
    }
    return layer;
  }

  protected abstract ILspLayer createLayer(ILcdModel aModel, ILcdInterval aLabelScaleRange);

  private boolean cluster() {
    return fClustering == Clustering.ENABLED;
  }

}

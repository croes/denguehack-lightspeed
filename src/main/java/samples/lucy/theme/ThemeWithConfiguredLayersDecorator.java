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
package samples.lucy.theme;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.luciad.io.TLcdInputStreamFactory;
import com.luciad.lucy.ILcyLucyEnv;
import com.luciad.lucy.map.ILcyGenericMapComponent;
import com.luciad.lucy.map.ILcyLayerStyle;
import com.luciad.lucy.map.TLcyCompositeLayerStyleCodec;
import com.luciad.lucy.map.TLcyCompositeLayerStyleProvider;
import com.luciad.lucy.map.lightspeed.TLcyLspCompositeLayerFactory;
import com.luciad.lucy.map.lightspeed.TLcyLspMapManager;
import com.luciad.lucy.model.TLcyCompositeModelDecoder;
import com.luciad.lucy.util.properties.ALcyProperties;
import com.luciad.model.ILcdModel;
import com.luciad.util.TLcdNoBoundsException;
import com.luciad.util.TLcdOutOfBoundsException;
import com.luciad.util.enumeration.ILcdMorphingFunction;
import com.luciad.util.logging.ILcdLogger;
import com.luciad.util.logging.TLcdLoggerFactory;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.TLspPaintRepresentation;
import com.luciad.view.lightspeed.util.TLspViewNavigationUtil;

/**
 * <p>Decorator for a {@link Theme} which creates the layers which
 * are configured in the configuration file.</p>
 *
 * <p>The layers are created during the construction of this decorator based on the model
 * decoders and layer factories in Lucy. When the theme is activated, the layers will
 * be added to the active map component. When the theme is deactivated, the layers will
 * be removed from the active map component.</p>
 *
 * <p>
 *   The configuration file should contain the following properties:
 * </p>
 *
 * <pre class="code">
 *   # Defines the number of sources.
 *   # For each of the sources, you need to configure at least
 *   # the source (see below)
 *   configPrefix.source.numberOfSources = x
 *
 *   # For each source, you can use the following options:
 *   # sourceName: link to the source file. May be relative to the class path. This option is mandatory.
 *   # styleFile: link to the style file. May be relative to the class path. This option is optional.
 *   # label: label which will be used for the layers of this source. This option is optional.
 *   # labeled: true or false. Controls whether the layer is labeled by default (when
 *   # labeling is supported for that layer).
 *   #
 *   # The prefix for each of those options is the regular prefix followed by "source." + the number of the source (numbers start at 1)
 *   # For example for the first source
 *   configPrefix.source.1.sourceName=Data/geojson/airspaces.json
 *   configPrefix.source.1.styleFile=samples/cop/addons/airpicturetheme/airspaces.sty
 *   configPrefix.source.1.label=Airspaces
 *   configPrefix.source.1.labeled=false
 * </pre>
 *
 * <p>This decorator can only be used when Lucy contains only one map component, and no
 * new map components will be created. This assumption is valid in the COP sample.</p>
 */
public final class ThemeWithConfiguredLayersDecorator extends AThemeDecorator {

  private static final ILcdLogger LOGGER = TLcdLoggerFactory.getLogger(ThemeWithConfiguredLayersDecorator.class);

  private static final String NUMBER_OF_SOURCES = "numberOfSources";
  private static final String SOURCE_NAME = "sourceName";
  private static final String STYLE_FILE = "styleFile";
  private static final String LABEL = "label";
  private static final String LABELED = "labeled";

  private final ILcyLucyEnv fLucyEnv;
  private final ILcdMorphingFunction<String, String> fPathConvertor;

  private final List<ILspLayer> fThemeLayers = new ArrayList<>();
  private final List<Collection<ILspLayer>> fThemeLayersPerSource = new ArrayList<>();

  private boolean fShouldFit = true;

  public ThemeWithConfiguredLayersDecorator(Theme aDelegate,
                                            ILcyLucyEnv aLucyEnv,
                                            String aPropertyPrefix,
                                            ALcyProperties aProperties) {
    this(aDelegate, aLucyEnv, aPropertyPrefix, aProperties, new ILcdMorphingFunction<String, String>() {
      @Override
      public String morph(String aPath) {
        return aPath;
      }
    });
  }

  /**
   *
   * @param aDelegate The delegate format
   * @param aLucyEnv The Lucy back-end
   * @param aPropertyPrefix The prefix used in the properties
   * @param aProperties The properties
   * @param aPathConvertor The function will be applied on each of the paths defined in the properties, before trying to
   *                       read the data for that path.
   *                       This allows to use wildcards in the properties, and replace them before actually trying to read the
   *                       data.
   */
  public ThemeWithConfiguredLayersDecorator(Theme aDelegate,
                                            ILcyLucyEnv aLucyEnv,
                                            String aPropertyPrefix,
                                            ALcyProperties aProperties,
                                            ILcdMorphingFunction<String, String> aPathConvertor) {
    super(aDelegate);
    fLucyEnv = aLucyEnv;
    fPathConvertor = aPathConvertor;

    List<SourceContainer> sourceContainers = retrieveSourceContainers(aPropertyPrefix + "source.", aProperties, aLucyEnv);
    for (SourceContainer sourceContainer : sourceContainers) {
      List<ILspLayer> layersForSource = createLayers(sourceContainer);
      fThemeLayers.addAll(layersForSource);
      fThemeLayersPerSource.add(layersForSource);
    }
  }

  private List<ILspLayer> createLayers(SourceContainer aSourceContainer) {
    List<ILspLayer> result = new ArrayList<ILspLayer>();
    TLcyCompositeModelDecoder modelDecoder = new TLcyCompositeModelDecoder(fLucyEnv);
    TLcyLspCompositeLayerFactory layerFactory = new TLcyLspCompositeLayerFactory(fLucyEnv);
    if (modelDecoder.canDecodeSource(aSourceContainer.fSourceName)) {
      try {
        ILcdModel model = modelDecoder.decode(aSourceContainer.fSourceName);
        if (layerFactory.canCreateLayers(model)) {
          result.addAll(layerFactory.createLayers(model));
          applyStylesOnLayers(result, aSourceContainer);
        }
      } catch (IOException e) {
        LOGGER.error("Could not decode [" + aSourceContainer.fSourceName + "]. Make sure the file exists.", e);
      }
    } else {
      LOGGER.warn("Cannot decode [" + aSourceContainer.fSourceName + "]. Are you certain there is a model decoder available ?");
    }
    return result;
  }

  private void applyStylesOnLayers(List<ILspLayer> aLayers, SourceContainer aSourceContainer) {
    TLcyCompositeLayerStyleCodec layerStyleCodec = new TLcyCompositeLayerStyleCodec(fLucyEnv);
    TLcyCompositeLayerStyleProvider layerStyleProvider = new TLcyCompositeLayerStyleProvider(fLucyEnv);
    for (ILspLayer layer : aLayers) {
      if (layer.getPaintRepresentations().contains(TLspPaintRepresentation.LABEL)) {
        layer.setVisible(TLspPaintRepresentation.LABEL, aSourceContainer.fLabeled);
      }
      if (aSourceContainer.fLabel != null) {
        layer.setLabel(aSourceContainer.fLabel);
      }
      if (aSourceContainer.fStyleFile != null) {
        if (layerStyleProvider.canGetStyle(layer)) {
          ILcyLayerStyle layerStyle = layerStyleProvider.getStyle(layer);
          if (layerStyleCodec.canDecode(layer, layerStyle)) {
            try {
              TLcdInputStreamFactory inputStreamFactory = new TLcdInputStreamFactory();
              InputStream inputStream = inputStreamFactory.createInputStream(aSourceContainer.fStyleFile);
              layerStyleCodec.decode(layer, layerStyle, inputStream);
            } catch (IOException e) {
              LOGGER.error("Could not decode style [" + aSourceContainer.fStyleFile + "]", e);
            }
          }
        }
      }
    }
  }

  /**
   * Indicate whether the theme should fit on the created layers on activation.
   *
   * @param aShouldFit {@code true} when the theme should fit on the created layers on activation.
   */
  public void setShouldFit(boolean aShouldFit) {
    fShouldFit = aShouldFit;
  }

  /**
   * Returns the created layers, grouped by their source as defined in the configuration file.
   * The order in the returned list matches the numbering used for the sources in the configuration file
   * (see class javadoc for more info).
   *
   * @return the created layers, grouped by their source as defined in the configuration file.
   */
  public List<Collection<ILspLayer>> getThemeLayersPerSource() {
    return fThemeLayersPerSource;
  }

  @Override
  public void activate(ILcyLucyEnv aLucyEnv) {
    assertSameLucyEnv(aLucyEnv);
    super.activate(aLucyEnv);
    addThemeLayersToActiveMapComponent(aLucyEnv);
    if (fShouldFit) {
      fitOnThemeLayers(aLucyEnv);
    }
  }

  private void fitOnThemeLayers(ILcyLucyEnv aLucyEnv) {
    ILcyGenericMapComponent<ILspView, ILspLayer> mapComponent = getActiveMapComponent(aLucyEnv);
    try {
      new TLspViewNavigationUtil(mapComponent.getMainView()).animatedFit(fThemeLayers);
    } catch (TLcdNoBoundsException | TLcdOutOfBoundsException e) {
      //ignore
    }
  }

  private ILcyGenericMapComponent<ILspView, ILspLayer> getActiveMapComponent(ILcyLucyEnv aLucyEnv) {
    TLcyLspMapManager mapManager = aLucyEnv.getService(TLcyLspMapManager.class);
    return mapManager.getActiveMapComponent();
  }

  @Override
  public void deactivate(ILcyLucyEnv aLucyEnv) {
    assertSameLucyEnv(aLucyEnv);
    super.deactivate(aLucyEnv);
    removeThemeLayersFromActiveMapComponent(aLucyEnv);
  }

  private void addThemeLayersToActiveMapComponent(ILcyLucyEnv aLucyEnv) {
    ILcyGenericMapComponent<ILspView, ILspLayer> mapComponent = getActiveMapComponent(aLucyEnv);
    for (ILspLayer layer : fThemeLayers) {
      mapComponent.getMainView().addLayer(layer);
    }
  }

  private void removeThemeLayersFromActiveMapComponent(ILcyLucyEnv aLucyEnv) {
    ILcyGenericMapComponent<ILspView, ILspLayer> mapComponent = getActiveMapComponent(aLucyEnv);
    for (ILspLayer layer : fThemeLayers) {
      mapComponent.getMainView().removeLayer(layer);
    }
  }

  private void assertSameLucyEnv(ILcyLucyEnv aLucyEnv) {
    if (aLucyEnv != fLucyEnv) {
      throw new UnsupportedOperationException("The theme can only be used for one ILcyLucyEnv.");
    }
  }

  private List<SourceContainer> retrieveSourceContainers(String aPropertyPrefix, ALcyProperties aProperties, ILcyLucyEnv aLucyEnv) {
    int numberOfSources = aProperties.getInt(aPropertyPrefix + NUMBER_OF_SOURCES, 0);
    List<SourceContainer> containers = new ArrayList<SourceContainer>();
    for (int i = 1; i <= numberOfSources; i++) {
      String prefix = aPropertyPrefix + i + ".";
      String sourceName = fPathConvertor.morph(aProperties.getString(prefix + SOURCE_NAME, null));
      String styleFile = fPathConvertor.morph(aProperties.getString(prefix + STYLE_FILE, null));
      String label = aProperties.getString(prefix + LABEL, null);
      boolean labeled = aProperties.getBoolean(prefix + LABELED, false);
      if (sourceName != null) {
        containers.add(new SourceContainer(sourceName, styleFile, label, labeled));
      }
    }
    return containers;
  }

  private static final class SourceContainer {
    private final String fSourceName;
    private final String fStyleFile;
    private final String fLabel;
    private final boolean fLabeled;

    private SourceContainer(String aSourceName, String aStyleFile, String aLabel, boolean aLabeled) {
      fSourceName = aSourceName;
      fStyleFile = aStyleFile;
      fLabel = aLabel;
      fLabeled = aLabeled;
    }
  }
}

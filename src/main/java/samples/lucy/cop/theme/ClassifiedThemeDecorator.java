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
package samples.lucy.cop.theme;

import static samples.lucy.cop.map.ClassificationMapComponentFactory.Classification;

import com.luciad.lucy.ILcyLucyEnv;
import com.luciad.lucy.map.lightspeed.ILcyLspMapComponent;
import com.luciad.lucy.map.lightspeed.TLcyLspMapManager;
import com.luciad.lucy.util.properties.ALcyProperties;

import samples.lucy.cop.map.ClassificationMapComponentFactory;
import samples.lucy.theme.AThemeDecorator;
import samples.lucy.theme.Theme;

/**
 * <p>Decorator around a {@code Theme} which updates the
 * {@link ClassificationMapComponentFactory#CLASSIFICATION_KEY}
 * in the active map component with the classification specified in
 * the properties.</p>
 *
 * <p>This assumes that there is only one Lightspeed map component available in
 * Lucy, which is a valid assumption for the COP demo sample. This also requires
 * that the {@link ClassificationMapComponentFactory}
 * is used. Otherwise updating that property will have no effect.</p>
 */
public final class ClassifiedThemeDecorator extends AThemeDecorator {
  private static final String CLASSIFICATION_PROPERTY_KEY = "mapClassification";

  private final Classification fClassification;

  public ClassifiedThemeDecorator(Theme aDelegate, String aPropertyPrefix, ALcyProperties aProperties) {
    super(aDelegate);
    fClassification = Classification.valueOf(aProperties.getString(aPropertyPrefix + CLASSIFICATION_PROPERTY_KEY, Classification.DEFAULT.name()));
  }

  @Override
  public void activate(ILcyLucyEnv aLucyEnv) {
    super.activate(aLucyEnv);
    ILcyLspMapComponent activeMapComponent = (ILcyLspMapComponent) aLucyEnv.getService(TLcyLspMapManager.class).getActiveMapComponent();
    activeMapComponent.getProperties().put(ClassificationMapComponentFactory.CLASSIFICATION_KEY, fClassification.name());
  }

  @Override
  public void deactivate(ILcyLucyEnv aLucyEnv) {
    super.deactivate(aLucyEnv);
    ILcyLspMapComponent activeMapComponent = (ILcyLspMapComponent) aLucyEnv.getService(TLcyLspMapManager.class).getActiveMapComponent();
    activeMapComponent.getProperties().put(ClassificationMapComponentFactory.CLASSIFICATION_KEY, Classification.DEFAULT.name());
  }
}

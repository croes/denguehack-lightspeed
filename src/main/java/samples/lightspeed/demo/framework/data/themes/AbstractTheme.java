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
package samples.lightspeed.demo.framework.data.themes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JPanel;

import com.luciad.util.ILcdFilter;
import com.luciad.view.TLcdDomainObjectContext;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.layer.ILspLayer;

import samples.lightspeed.demo.application.gui.menu.DefaultPanelFactory;
import samples.lightspeed.demo.simulation.SimulationSupport;

/**
 * Base class that implements a lot of the basic functionality for themes.
 * <p/>
 * The following things are done by this class:
 * <ul>
 * <li>General attributes such as name, info message , etc. are stored by this class.</li>
 * <li>Layers are created in the initialize method. They are stored in a list, so that
 * can be made visible/invisible in the activate/deactivate methods.</li>
 * </ul>
 */
public abstract class AbstractTheme {

  private String fName;
  private String fCategory;
  private Icon fIcon;
  private String fInfoMessage;
  private boolean fShowInfoMessage;

  private Properties fThemeProperties;
  private ThemeAnimation fAnimation;
  private DefaultPanelFactory fDefaultPanelFactory;
  private List<ILspLayer> fThemeLayers;

  private ILcdFilter<TLcdDomainObjectContext> fShowPropertiesActionFilter;

  /**
   * Creates a new theme.
   */
  protected AbstractTheme() {
    fName = "Nameless Theme";
    fCategory = "Other";
    fInfoMessage = "No information available.";
    fShowInfoMessage = true;

    fThemeProperties = new Properties();
    fDefaultPanelFactory = new DefaultPanelFactory();
    fThemeLayers = new ArrayList<ILspLayer>();
  }

  protected void loadRequiredClassForQuickFail(Class aClass) {
    // Load a class from the required Additional or Industry Specific Component to provoke a NoClassDefFoundError
    // if the jar file is not present.  If not, the NoClassDefFoundError might happen at an
    // unexpected location, which could crash the application.
    aClass.getName();
  }

  /**
   * Initializes the theme with the given properties and for the given view.
   * <p/>
   * This method calls {@link #createLayers(List)} to create the theme's layers.
   * It will then store a reference to these layers so that
   *
   * @param aViews the views for which the theme is to be initialized
   * @param aProps the properties that were defined in the index file of the dataset.
   */
  public void initialize(List<ILspView> aViews, Properties aProps) {
    fThemeProperties.putAll(aProps);
    fThemeLayers = createLayers(aViews);

    // We do not want the layers to be immediately visible,
    // otherwise the view will be swamped when the demo starts
    for (ILspLayer layer : fThemeLayers) {
      layer.setVisible(false);
    }

    fShowPropertiesActionFilter = createShowPropertiesActionFilter();
  }

  /**
   * Creates the theme's layers.
   * <p/>
   * This method is called from {@link #initialize(List, Properties)}.
   * Note that the implementer is responsible for correctly adding the layers to the views.
   * <p/>
   * Note also that if theme properties are required for layer creation, you can use the
   * appropriate getter to retrieve the properties: see <code>getThemeProperties()</code>.
   *
   * @param aViews the views that are part of the application
   *
   * @return the list of layers that was created
   */
  protected abstract List<ILspLayer> createLayers(List<ILspView> aViews);

  /**
   * Activates the theme.
   * <p/>
   * This method performs all necessary actions to correctly visualize
   * the theme in all the views. By default, this method sets all the
   * associated layers to visible and starts the simulator if necessary.
   */
  public void activate() {
    for (ILspLayer layer : fThemeLayers) {
      layer.setVisible(true);
    }
    if (isSimulated()) {
      SimulationSupport.getInstance().activateTheme(this);
      SimulationSupport.getInstance().startSimulator();
    } else {
      SimulationSupport.getInstance().stopSimulator();
    }
  }

  /**
   * Deactivates the theme.
   * <p/>
   * This method performs all necessary actions to correctly hide the
   * theme in all the views. By default, this method sets all the
   * associated layers to NOT visible.
   */
  public void deactivate() {
    for (ILspLayer layer : fThemeLayers) {
      layer.setVisible(false);
    }
  }

  /**
   * Destroys the theme.
   * <p/>
   * By default this method clears the list of associated layers.
   * Overwrite this method if you need to perform other clean up
   * related operations.
   */
  public void destroy() {
    fThemeLayers.clear();
  }

  /**
   * Returns the theme properties that were defined in the index file of the data set.
   *
   * @return the theme's properties
   */
  public Properties getThemeProperties() {
    return fThemeProperties;
  }

  /**
   * Returns the name associated to the theme.
   *
   * @return the name of the theme
   */
  public String getName() {
    return fName;
  }

  /**
   * Sets the name of the theme to the given name.
   *
   * @param aName the new name of the theme
   */
  public void setName(String aName) {
    fName = aName;
  }

  /**
   * Gets the iconic representation of this theme.
   *
   * @return an icon representing the theme, can be {@code null}
   */
  public Icon getIcon() {
    return fIcon;
  }

  /**
   * Sets the iconic representation of this theme.
   *
   * @param aIcon an icon representing the theme
   */
  public void setIcon(Icon aIcon) {
    fIcon = aIcon;
  }

  /**
   * Gets the category for which the theme belongs
   *
   * @return the category of this theme
   */
  public String getCategory() {
    return fCategory;
  }

  /**
   * Sets the category for this theme
   *
   * @param aCategory the category for this theme
   */
  public void setCategory(String aCategory) {
    fCategory = aCategory;
  }

  /**
   * Returns the theme animation that is associated with the theme.
   *
   * @return the animation associated with the theme, or
   *         <code>null</code> if the theme has no animation
   */
  public ThemeAnimation getAnimation() {
    return fAnimation;
  }

  /**
   * Sets the animation of the theme to the given animation.
   *
   * @param aThemeAnimation the animation for the theme (can be <code>null</code>)
   */
  public void setAnimation(ThemeAnimation aThemeAnimation) {
    fAnimation = aThemeAnimation;
  }

  /**
   * Checks whether the theme depends on the application's simulator.
   * Returns <code>false</code> by default.
   * <p/>
   * Note, the application's simulator is an instance of <code>TLcdSimulator</code>
   * that is internally kept in the {@link samples.lightspeed.demo.simulation.SimulationSupport}
   * class.
   *
   * @return <code>true</code> if this theme depends on the application's simulator,
   *         <code>false</code> otherwise.
   */
  public boolean isSimulated() {
    return false;
  }

  /**
   * Returns the GUI panels that are associated to the theme.
   * These panels will be visualized under the main theme panel.
   * <p/>
   * By default this method will return nothing.
   *
   * @return a collection of theme panels that are associated to the theme
   */
  public List<JPanel> getThemePanels() {
    return Collections.emptyList();
  }

  public JComponent getSouthDockedComponent() {
    return null;
  }

  /**
   * Returns the list of layers that is associated to this theme. Note that the returned list is a
   * copy of the actual list, so modifying the returned list will not affect the theme.
   *
   * @return a copy of the list of associated layers
   */
  public List<ILspLayer> getLayers() {
    return new ArrayList<ILspLayer>(fThemeLayers);
  }

  /**
   * Returns the information message associated to the theme.
   * <p/>
   * The message provides information on what the theme does,
   * how it works and other relevant things.
   *
   * @return the information message of the theme
   */
  public String getInfoMessage() {
    return fInfoMessage;
  }

  /**
   * Sets the information message of the theme to the given message.
   *
   * @param aMessage the new information message of the the theme
   */
  public void setInfoMessage(String aMessage) {
    fInfoMessage = aMessage;
  }

  /**
   * Checks whether the info message of the theme should be shown.
   *
   * @return <code>true</code> when the info message should be shown, <code>false</code> otherwise
   */
  public boolean isShowInfoMessage() {
    return fShowInfoMessage;
  }

  /**
   * Sets whether the info message of this theme should be shown or not.
   *
   * @param aShowInfoMessage <code>true</code> when the info message should be shown,
   *                         <code>false</code> otherwise
   */
  public void setShowInfoMessage(boolean aShowInfoMessage) {
    fShowInfoMessage = aShowInfoMessage;
  }

  /**
   * Creates a filter that determines if a popup should be shown for the given domain object.
   * @return a filter that determines if a popup should be shown for the given domain object.
   */
  protected ILcdFilter<TLcdDomainObjectContext> createShowPropertiesActionFilter() {
    return new ILcdFilter<TLcdDomainObjectContext>() {
      @Override
      public boolean accept(TLcdDomainObjectContext aObject) {
        return true;
      }
    };
  }

  /**
   * Returns a filter that determines if a popup should be shown for the given domain object.
   * @return a filter that determines if a popup should be shown for the given domain object.
   */
  public final ILcdFilter<TLcdDomainObjectContext> getShowPropertiesActionFilter() {
    return fShowPropertiesActionFilter;
  }
}

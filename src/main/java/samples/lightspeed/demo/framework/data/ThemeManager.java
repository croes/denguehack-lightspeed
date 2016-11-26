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
package samples.lightspeed.demo.framework.data;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import org.jdom.Element;
import org.jdom.Namespace;

import com.luciad.io.TLcdInputStreamFactory;
import com.luciad.util.logging.ILcdLogger;
import com.luciad.util.logging.TLcdLoggerFactory;
import com.luciad.view.lightspeed.ILspView;

import samples.lightspeed.demo.framework.application.Framework;
import samples.lightspeed.demo.framework.data.themes.AbstractTheme;
import samples.lightspeed.demo.framework.exception.DataSetException;

/**
 * The theme manager is responsible for creating and managing all themes that are used in the
 * application.
 */
class ThemeManager {

  private final static ILcdLogger sLogger = TLcdLoggerFactory.getLogger(ThemeManager.class.getName());

  // List of themes
  private ArrayList<AbstractTheme> fThemes;

  // Mapping of themes to their associated list of properties
  private Map<AbstractTheme, Properties> fThemePropertiesMap;

  /**
   * Constructs a new theme manager without any themes.
   */
  ThemeManager() {
    fThemes = new ArrayList<AbstractTheme>();
    fThemePropertiesMap = new IdentityHashMap<AbstractTheme, Properties>();
  }

  /**
   * Returns the list of loaded themes.
   *
   * @return the list of loaded themes
   */
  public AbstractTheme[] getThemes() {
    return fThemes.toArray(new AbstractTheme[fThemes.size()]);
  }

  /**
   * Returns the loaded theme of the specified class
   * @param aThemeClass The class of the theme
   * @param <T> The type of theme
   * @return the loaded theme of the specified class, or {@code null} when no such theme is loaded
   */
  public <T extends AbstractTheme> T getThemeByClass(Class<T> aThemeClass) {
    for (AbstractTheme theme : fThemes) {
      if (aThemeClass.isInstance(theme)) {
        return (T) theme;
      }
    }
    return null;
  }

  /**
   * Initializes the loaded themes for the given list of views.
   *
   * @param aViews the list of views that were created in the demo
   */
  void initializeThemes(List<ILspView> aViews) {
    ArrayList<AbstractTheme> failed = new ArrayList<AbstractTheme>();

    for (AbstractTheme theme : fThemes) {
      try {
        Properties props = fThemePropertiesMap.get(theme);
        theme.initialize(aViews, props);
      } catch (NoClassDefFoundError e) {
        sLogger.info("Could not initialize " + theme.getName() + " theme: " + e.getMessage());
        sLogger.trace("Could not initialize " + theme.getName() + " theme", e);
        failed.add(theme);
      } catch (Exception e) {
        sLogger.error("Could not initialize " + theme.getName() + " theme", e);
        failed.add(theme);
      }
    }

    // Remove the themes that failed during initialization
    for (AbstractTheme theme : failed) {
      fThemes.remove(theme);
      fThemePropertiesMap.remove(theme);
    }
  }

  /**
   * Loads the set of themes defined by the given XML hierarchy.
   *
   * @param aRoot      root element of the given XML hierarchy
   * @param aNamespace namespace in which the XML hierarchy is defined
   *
   * @throws samples.lightspeed.demo.framework.exception.DataSetException
   *          When the data set could not be
   *          found, or there was difficulty parsing the data set.
   */
  public void loadThemeSet(Element aRoot, Namespace aNamespace) throws DataSetException {
    fThemes.clear();
    fThemePropertiesMap.clear();
    try {
      loadThemes(aRoot, aNamespace);
    } catch (Exception e) {
      throw new DataSetException(e.getMessage(), e);
    }
  }

  private void loadThemes(Element aRoot, Namespace aNamespace) {
    List<Element> themeElements = aRoot.getChildren("Theme", aNamespace);
    String className;
    String name = null;

    // Keep a list of themes which could not be loaded
    List<String> badThemes = new ArrayList<String>();

    List<String> excludedThemes = Arrays.asList(System.getProperty("luciad.samples.demo.excludedThemes", "").split(","));

    for (Element tel : themeElements) {
      try {
        // First parse the attributes that were defined in the index file
        className = AliasResolver.resolve(tel, "class");
        name = AliasResolver.resolve(tel, "name");
        if (excludedThemes.contains(name)) {
          continue;
        }
        String infoFile = AliasResolver.resolve(tel, "infoFile");
        String iconFile = AliasResolver.resolve(tel, "iconFile");

        // We create a new instance of the theme via reflection
        AbstractTheme theme = createThemeInstance(className);
        if (name != null && !name.isEmpty()) {
          theme.setName(name);
        }

        // Read the information message from file and add it to the theme
        String infoMessage = readInfoFile(infoFile);
        theme.setInfoMessage(infoMessage);

        // Read an icon image to be used for touch UI's
        Icon icon = readIconFile(iconFile);
        theme.setIcon(icon);

        // Store the properties defined in the index file, so that
        // the theme can be initialized properly later on
        Properties props = extractProperties(tel, aNamespace);
        fThemePropertiesMap.put(theme, props);

        fThemes.add(theme);
      } catch (Exception e) {
        StringBuilder builder = new StringBuilder(name);
        builder.append("\n\t\t\t-> Reason: ").append(e.getMessage());
        badThemes.add(builder.toString());
      }
    }

    // Print out a clear error message when the loading of certain themes went wrong
    if (!badThemes.isEmpty()) {
      StringBuilder log = new StringBuilder();
      log.append("Some themes failed to load successfully:\n\n");
      for (String badTheme : badThemes) {
        log.append("\t\t-> ").append(badTheme).append("\n\n");
      }
      log.append("The demo will launch without the themes listed above.");
      String message = log.toString();
      sLogger.error(message);

      JOptionPane.showMessageDialog(null, "Could not load all themes, check log message for more information.", "Failed to properly load all themes.", JOptionPane.WARNING_MESSAGE);
    }
  }

  private Icon readIconFile(String aFileName) {
    if (aFileName == null) {
      return null;
    }

    try {
      String iconPath = Framework.getInstance().getProperty("theme.icon.path");
      BufferedImage image = IOUtil.readImage(iconPath, aFileName);
      return new ImageIcon(image);
    } catch (Exception e) {
      sLogger.error(e.getMessage(), e);
      return null;
    }
  }

  private String readInfoFile(String aFileName) {
    if (aFileName == null) {
      return null;
    }

    String infoPath = Framework.getInstance().getProperty("info.path");
    if (infoPath != null) {
      aFileName = infoPath + "/" + aFileName;
    }

    try {
      TLcdInputStreamFactory inputStreamFactory = new TLcdInputStreamFactory();
      StringBuilder builder = new StringBuilder();
      BufferedReader reader = new BufferedReader(new InputStreamReader(inputStreamFactory.createInputStream(aFileName)));
      String line;
      while ((line = reader.readLine()) != null) {
        builder.append(line);
      }
      return builder.toString();
    } catch (FileNotFoundException e) {
      sLogger.warn("Could not find information file: " + aFileName, e);
      return null;
    } catch (IOException e) {
      sLogger.warn("Error while reading information file: " + aFileName, e);
      return null;
    }
  }

  private AbstractTheme createThemeInstance(String aClassName) throws DataSetException {
    if (aClassName == null) {
      throw new IllegalArgumentException("Cannot create theme instance, given class name is NULL!");
    }
    try {
      Class<?> themeClass = Class.forName(aClassName);

      if (AbstractTheme.class.isAssignableFrom(themeClass)) {
        Constructor<AbstractTheme> c = (Constructor<AbstractTheme>) themeClass.getConstructor();
        AbstractTheme theme = c.newInstance();
        return theme;
      } else {
        throw new DataSetException("Invalid theme class [" + aClassName + "], theme must implement AbstractTheme");
      }
    } catch (NoClassDefFoundError e) {
      throw new DataSetException("Failed to create new instance of theme class, " +
                                 "because certain dependencies were not found. " +
                                 "Did you install all the necessary zip files? ", e);
    } catch (ClassNotFoundException e) {
      throw new DataSetException("Failed to create new instance of theme class, " +
                                 "because certain dependencies were not found. " +
                                 "Did you install all the necessary zip files? ", e);
    } catch (InvocationTargetException e) {
      if (e.getCause() instanceof NoClassDefFoundError ||
          e.getCause() instanceof ClassNotFoundException) {
        throw new DataSetException("Failed to create new instance of theme class, " +
                                   "because certain dependencies were not found. " +
                                   "Did you install all the necessary zip files? ", e.getCause());
      } else {
        throw new DataSetException("Failed to create new instance of theme class, " +
                                   "because an exception was thrown.\n\t\t\t   " +
                                   e.getCause().toString(), e.getCause());
      }
    } catch (NoSuchMethodException e) {
      throw new DataSetException("Could not find default constructor for theme class, " +
                                 "make sure that this constructor is available in your theme's implementation.", e);
    } catch (InstantiationException e) {
      throw new DataSetException("Failed to create new instance of theme class, because it is an abstract class.", e);
    } catch (IllegalAccessException e) {
      throw new DataSetException("Failed to create new instance of theme class, its constructor is inaccessible (it should be public).", e);
    }
  }

  /**
   * Extracts properties that are defined in the given XML element.
   *
   * @param aElement   the XML element that defines the theme properties
   * @param aNamespace the XML namespace in which the element is defined
   *
   * @return a java Properties object
   *
   * @throws DataSetException
   */
  private Properties extractProperties(Element aElement, Namespace aNamespace) throws DataSetException {
    Properties props = new Properties();
    List<Element> propElements = aElement.getChildren("Property", aNamespace);
    for (Element prop : propElements) {
      String key = AliasResolver.resolve(prop, "key");
      String value = AliasResolver.resolve(prop, "value");
      props.put(key, value);
    }
    return props;
  }

}

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
package samples.wms.server.config.editor;

import java.awt.Component;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;

import com.luciad.io.TLcdInputStreamFactory;

/**
 * A class that maps components to help hints which can be displayed in the
 * application's status bar. The components have to be registered with this
 * class by means of a unique name. This name is used to look up the help hint
 * from a properties file.
 */
public final class WMSEditorHelp {

  private static Map sComponentLUT = new Hashtable();
  private static Properties sHelpStringLUT = new Properties();

  private WMSEditorHelp() {
    // No need to instantiate this class, as all utility methods are static.
  }

  /**
   * Load help strings from a properties file.
   */
  public static void loadHelpData(String aFilename) throws IOException {
    TLcdInputStreamFactory isf = new TLcdInputStreamFactory();
    sHelpStringLUT.load(isf.createInputStream(aFilename));
  }

  /**
   * Registers a component with the help system.
   *
   * @param aComponent    the component to be registered.
   * @param aComponentUID a string that uniquely identifies the component.
   */
  public static void registerComponent(Component aComponent, String aComponentUID) {
    sComponentLUT.put(aComponent, aComponentUID);
    aComponent.addMouseMotionListener(MainPanel.get().getHelpMouseListener());
  }

  /**
   * Retrieve the help string associated with the given component.
   */
  public static String getHelpString(Component aComponent) {

    if (aComponent == null) {
      return null;
    }

    Component c = aComponent;
    String key = null;
    boolean found = false;

    /* Start with the given component, and travel up in the component hierarchy
       until we find a component that has a help string. This allows you to
       associate a single help message with all components within a container,
       which can be convenient. */
    while (c != null) {
      key = (String) sComponentLUT.get(aComponent);
      if (key != null) {
        found = true;
        String help = sHelpStringLUT.getProperty(key);
        if (help != null) {
          return help;
        }
      }

      c = c.getParent();
    }
    /* If we found the component but not a help message, display a warning.
       Else, the component hasn't been registered at all so we can return null.
     */
    return (found ? "No help message has been associated with this item yet." : null);
  }
}

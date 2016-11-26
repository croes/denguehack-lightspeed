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
package samples.common.gui.blacklime;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Enumeration;

import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.plaf.basic.BasicLookAndFeel;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;

import com.luciad.util.TLcdSystemPropertiesUtil;
import com.luciad.util.logging.ILcdLogger;
import com.luciad.util.logging.TLcdLoggerFactory;

/**
 * <p>
 *   Extension of the {@code NimbusLookAndFeel} which adds the Mac-specific
 *   shortcuts (for example cmd-c to copy the selected text in a text field)
 *   when running on a Mac.
 * </p>
 */
class MacNimbusLookAndFeel extends NimbusLookAndFeel {

  private static final ILcdLogger LOGGER = TLcdLoggerFactory.getLogger(MacNimbusLookAndFeel.class);

  private BasicLookAndFeel fBaseLookAndFeel = null;

  public MacNimbusLookAndFeel() {
    if (TLcdSystemPropertiesUtil.isMacOS()) {
      String name = UIManager.getSystemLookAndFeelClassName();
      try {
        fBaseLookAndFeel = (BasicLookAndFeel) Class.forName(name).newInstance();
      } catch (InstantiationException | IllegalAccessException | ClassNotFoundException aE) {
        if (LOGGER.isDebugEnabled()) {
          LOGGER.error("Could not create the BasicLookAndFeel instance", aE);
        }
      }
    }
  }

  private void callInit(String method, UIDefaults defaults) {
    try {
      final Method superMethod = BasicLookAndFeel.class.getDeclaredMethod(method, UIDefaults.class);
      superMethod.setAccessible(true);
      superMethod.invoke(fBaseLookAndFeel, defaults);
    } catch (Exception ignore) {
      if (LOGGER.isTraceEnabled()) {
        LOGGER.error("Error while trying to call the [" + method + "] through reflection", ignore);
      }
    }
  }

  private void call(String method) {
    try {
      final Method superMethod = BasicLookAndFeel.class.getDeclaredMethod(method);
      superMethod.setAccessible(true);
      superMethod.invoke(fBaseLookAndFeel);
    } catch (Exception ignore) {
      if (LOGGER.isTraceEnabled()) {
        LOGGER.error("Error while trying to call the [" + method + "] through reflection", ignore);
      }
    }
  }

  @Override
  protected void loadSystemColors(UIDefaults table, String[] systemColors, boolean useNative) {
    if (fBaseLookAndFeel != null) {
      try {
        Method superMethod = BasicLookAndFeel.class.getDeclaredMethod("loadSystemColors", UIDefaults.class, String[].class, boolean.class);
        superMethod.setAccessible(true);
        superMethod.invoke(fBaseLookAndFeel, table, systemColors, useNative);
      } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException aE) {
        if (LOGGER.isTraceEnabled()) {
          LOGGER.error("Error while trying to call the [loadSystemColors] through reflection", aE);
        }
      }
    }
    super.loadSystemColors(table, systemColors, useNative);
  }

  @Override
  protected void initComponentDefaults(UIDefaults table) {
    if (fBaseLookAndFeel != null) {
      callInit("initComponentDefaults", table);
    }
    super.initComponentDefaults(table);
  }

  @Override
  protected void initSystemColorDefaults(UIDefaults table) {
    if (fBaseLookAndFeel != null) {
      callInit("initSystemColorDefaults", table);
    }
    super.initSystemColorDefaults(table);
  }

  @Override
  protected void initClassDefaults(UIDefaults table) {
    if (fBaseLookAndFeel != null) {
      callInit("initClassDefaults", table);
    }
    super.initClassDefaults(table);
  }

  @Override
  public void initialize() {
    if (fBaseLookAndFeel != null) {
      call("initialize");
    }
    super.initialize();

    UIDefaults defaults = super.getDefaults();
    if (fBaseLookAndFeel != null) {
      //Copy the input mappings from the system look and feel
      //This ensures for example that cmd-v allows to paste in a textfield
      //Otherwise we would end up with windows shortcuts (ctrl-v to paste)
      UIDefaults aquaDefaults = fBaseLookAndFeel.getDefaults();
      Enumeration<Object> keys = aquaDefaults.keys();
      while (keys.hasMoreElements()) {
        Object key = keys.nextElement();
        if (key instanceof String && ((String) key).toLowerCase().contains("inputmap")) {
          defaults.put(key, aquaDefaults.get(key));
        }
      }
    }
  }

  @Override
  public void uninitialize() {
    super.uninitialize();
    if (fBaseLookAndFeel != null) {
      call("unitialize");
    }
  }
}

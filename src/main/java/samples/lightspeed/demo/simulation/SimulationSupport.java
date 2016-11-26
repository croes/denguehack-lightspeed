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
package samples.lightspeed.demo.simulation;

import java.util.Collections;
import java.util.Set;

import com.luciad.util.logging.ILcdLogger;
import com.luciad.util.logging.TLcdLoggerFactory;

import samples.lightspeed.demo.framework.data.themes.AbstractTheme;

/**
 * Manages a centralized TLcdSimulator instance for use by the LuciadLightspeed demo.
 * <p/>
 * Note that this class does not directly depend on the realtime package, as opposed to
 * {@link SimulationSupportImpl}. This allows using this class, even when the realtime package is
 * not available.
 */
public class SimulationSupport {

  private static ILcdLogger sLogger = TLcdLoggerFactory.getLogger(SimulationSupport.class);

  private static SimulationSupport sSimulationSupport;

  public static SimulationSupport getInstance() {
    if (sSimulationSupport == null) {
      sSimulationSupport = new SimulationSupport();
    }
    return sSimulationSupport;
  }

  //////////////////////////////////////////

  private final ISimulationSupportImpl fImpl;

  private SimulationSupport() {
    ISimulationSupportImpl impl;
    try {
      // We do this check to prevent the demo from crashing
      // when the realtime package is not available
      Class.forName("com.luciad.realtime.TLcdSimulator");

      Class<?> simulationSupportImpl = Class.forName("samples.lightspeed.demo.simulation.SimulationSupportImpl");
      impl = (ISimulationSupportImpl) simulationSupportImpl.newInstance();
    } catch (Exception e) {
      // Do nothing
      impl = null;
      if (sLogger.isDebugEnabled()) {
        sLogger.debug("Could not load simulation support", e);
      }
    }
    fImpl = impl;
  }

  public boolean isAvailable() {
    return fImpl != null;
  }

  public boolean isRunning() {
    return fImpl != null && fImpl.isRunning();
  }

  public void startSimulator() {
    if (fImpl != null) {
      fImpl.startSimulator();
    }
  }

  public void pauseSimulator() {
    if (fImpl != null) {
      fImpl.pauseSimulator();
    }
  }

  public void stopSimulator() {
    if (fImpl != null) {
      fImpl.stopSimulator();
    }
  }

  public void activateTheme(AbstractTheme aTheme) {
    if (fImpl != null) {
      fImpl.activateTheme(aTheme);
    }
  }

  public void setSharedSimulatorModel(String aName, Object aSimulatorModel) {
    if (fImpl != null) {
      fImpl.setSharedSimulatorModel(aName, aSimulatorModel);
    }
  }

  public Object getSharedSimulatorModel(String aName) {
    if (fImpl != null) {
      return fImpl.getSharedSimulatorModel(aName);
    }
    return null;
  }

  public double getTimeFactor() {
    if (fImpl != null) {
      return fImpl.getTimeFactor();
    } else {
      return 1.0;
    }
  }

  public void setTimeFactor(double aTimeFactor) {
    if (fImpl != null) {
      fImpl.setTimeFactor(aTimeFactor);
    }
  }

  public Set getSimulatorModelsForTheme(AbstractTheme aAbstractTheme) {
    if (fImpl != null) {
      return fImpl.getSimulatorModelsForTheme(aAbstractTheme);
    }
    return Collections.emptySet();
  }

  public void addSimulatorModelForTheme(Object aSimulatorModel, AbstractTheme aAbstractTheme) {
    if (fImpl != null) {
      fImpl.addSimulatorModelForTheme(aSimulatorModel, aAbstractTheme);
    }
  }

  public void removeSimulatorModelForTheme(Object aSimulatorModel, AbstractTheme aAbstractTheme) {
    if (fImpl != null) {
      fImpl.removeSimulatorModelForTheme(aSimulatorModel, aAbstractTheme);
    }
  }
}

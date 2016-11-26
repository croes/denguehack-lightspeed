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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.luciad.realtime.ILcdSimulatorModel;
import com.luciad.realtime.TLcdSimulator;
import com.luciad.realtime.TLcdSimulatorModelList;

import samples.lightspeed.demo.framework.application.Framework;
import samples.lightspeed.demo.framework.data.themes.AbstractTheme;

/**
 * Implementation of {@link SimulationSupport}, depends on the realtime package.
 */
public class SimulationSupportImpl implements ISimulationSupportImpl {

  private final TLcdSimulator fSimulator;
  private final Map<AbstractTheme, Set<ILcdSimulatorModel>> fThemeToSimulatorModelsMap = new HashMap<AbstractTheme, Set<ILcdSimulatorModel>>();
  private final HashMap<String, ILcdSimulatorModel> fSharedSimulatorModels = new HashMap<String, ILcdSimulatorModel>();

  public SimulationSupportImpl() {
    double timeFactor = Double.parseDouble(Framework.getInstance().getProperty("simulator.timeFactor", "50"));
    fSimulator = new TLcdSimulator();
    fSimulator.setPauseOnMousePressed(false);
    fSimulator.setTimeFactor(timeFactor);
    fSimulator.setDelayBetweenUpdateMs(2);
    fSimulator.setPlayInLoop(true);
    int maxCPU = Integer.parseInt(Framework.getInstance().getProperty("simulator.maxCPU", "" + fSimulator.getMaxCPUUsage()));
    fSimulator.setMaxCPUUsage(maxCPU);
  }

  @Override
  public boolean isRunning() {
    return fSimulator != null && fSimulator.getStatus() == TLcdSimulator.RUNNING;
  }

  @Override
  public void startSimulator() {
    if (fSimulator != null) {
      fSimulator.run();
      setCanUpdateModelAsynchronously(true);
    }
  }

  @Override
  public void pauseSimulator() {
    if (fSimulator != null) {
      fSimulator.pause();
      setCanUpdateModelAsynchronously(false);
    }
  }

  private void setCanUpdateModelAsynchronously(boolean aAsynchronous) {
    ILcdSimulatorModel simulatorModel = fSimulator.getSimulatorModel();
    if (simulatorModel != null) {
      setCanUpdateAsynchronously(simulatorModel, aAsynchronous);
    }
  }

  private void setCanUpdateAsynchronously(ILcdSimulatorModel aSimulatorModel, boolean aAsynchronous) {
    if (aSimulatorModel instanceof AsynchronousSimulatorModel) {
      ((AsynchronousSimulatorModel) aSimulatorModel).setAsynchronous(aAsynchronous);
    } else if (aSimulatorModel instanceof TLcdSimulatorModelList) {
      TLcdSimulatorModelList list = (TLcdSimulatorModelList) aSimulatorModel;
      for (int i = 0; i < list.getSimulatorModelCount(); i++) {
        setCanUpdateAsynchronously(list.getSimulatorModel(i), aAsynchronous);
      }
    }
  }

  @Override
  public void stopSimulator() {
    if (fSimulator != null) {
      fSimulator.stop();
    }
  }

  @Override
  public double getTimeFactor() {
    if (fSimulator != null) {
      return fSimulator.getTimeFactor();
    }
    return 1;
  }

  @Override
  public void setTimeFactor(double aTimeFactor) {
    if (fSimulator != null) {
      fSimulator.setTimeFactor(aTimeFactor);
    }
  }

  @Override
  public void activateTheme(AbstractTheme aTheme) {
    if (fSimulator != null) {
      Set<ILcdSimulatorModel> simulatorModels = fThemeToSimulatorModelsMap.get(aTheme);
      if (simulatorModels == null || simulatorModels.isEmpty()) {
        fSimulator.setSimulatorModel(null);
      } else {
        TLcdSimulatorModelList modelList = new TLcdSimulatorModelList();
        for (ILcdSimulatorModel simulatorModel : simulatorModels) {
          modelList.addSimulatorModel(simulatorModel);
        }
        fSimulator.setSimulatorModel(modelList);
      }
      if (isRunning()) {
        setCanUpdateModelAsynchronously(true);
      }
    }
  }

  @Override
  public void setSharedSimulatorModel(String aName, Object aSimulatorModel) {
    fSharedSimulatorModels.put(aName, (ILcdSimulatorModel) aSimulatorModel);
  }

  @Override
  public Object getSharedSimulatorModel(String aName) {
    return fSharedSimulatorModels.get(aName);
  }

  @Override
  public Set getSimulatorModelsForTheme(AbstractTheme aAbstractTheme) {
    if (fSimulator != null) {
      return fThemeToSimulatorModelsMap.get(aAbstractTheme);
    }
    return Collections.emptySet();
  }

  @Override
  public void addSimulatorModelForTheme(Object aSimulatorModel, AbstractTheme aAbstractTheme) {
    if (fSimulator != null) {
      ILcdSimulatorModel simulatorModel = (ILcdSimulatorModel) aSimulatorModel;
      Set<ILcdSimulatorModel> simulatorModels = fThemeToSimulatorModelsMap.get(aAbstractTheme);
      if (simulatorModels == null) {
        simulatorModels = new HashSet<ILcdSimulatorModel>();
        fThemeToSimulatorModelsMap.put(aAbstractTheme, simulatorModels);
      }
      simulatorModels.add(simulatorModel);
    }
  }

  @Override
  public void removeSimulatorModelForTheme(Object aSimulatorModel, AbstractTheme aAbstractTheme) {
    if (fSimulator != null) {
      ILcdSimulatorModel simulatorModel = (ILcdSimulatorModel) aSimulatorModel;
      Set<ILcdSimulatorModel> simulatorModels = fThemeToSimulatorModelsMap.get(aAbstractTheme);
      if (simulatorModels != null) {
        simulatorModels.remove(simulatorModel);
      }
    }
  }
}

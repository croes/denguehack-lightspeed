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
package samples.lightspeed.demo.application.data.milsym;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.InputStream;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.swing.JToggleButton;

import com.luciad.model.transformation.clustering.TLcdCluster;
import com.luciad.symbology.app6a.model.ILcdAPP6ACoded;
import com.luciad.symbology.app6a.model.TLcdAPP6AEchelonNode;
import com.luciad.symbology.milstd2525b.model.ILcdMS2525bCoded;
import com.luciad.symbology.milstd2525b.model.TLcdMS2525bEchelonNode;
import com.luciad.util.ILcdChangeListener;
import com.luciad.util.ILcdDynamicFilter;
import com.luciad.util.TLcdChangeEvent;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.camera.ALspViewXYZWorldTransformation;

import samples.symbology.common.BattleDimension;
import samples.symbology.common.EMilitarySymbology;
import samples.symbology.common.util.MilitarySymbolFacade;

public class MilSymFilter implements ILcdDynamicFilter, ItemListener, PropertyChangeListener {

  private final Set<ILcdChangeListener> fChangeListeners;
  private final Map<BattleDimension, Boolean> fVisibleBattleDimensions;
  private final Map<EMilitarySymbology, Properties> fEchelonScaleProperties;

  private ALspViewXYZWorldTransformation fTransformation;

  public MilSymFilter() {
    fChangeListeners = new HashSet<ILcdChangeListener>();
    fVisibleBattleDimensions = new EnumMap<BattleDimension, Boolean>(BattleDimension.class);
    for (BattleDimension bd : BattleDimension.values()) {
      fVisibleBattleDimensions.put(bd, true);
    }
    fEchelonScaleProperties = new HashMap<EMilitarySymbology, Properties>();
  }

  public void attachToView(ILspView aView) {
    fTransformation = aView.getViewXYZWorldTransformation();
    aView.addPropertyChangeListener(this);
    fTransformation.addPropertyChangeListener(this);
  }

  @Override
  public void addChangeListener(ILcdChangeListener aListener) {
    fChangeListeners.add(aListener);
  }

  @Override
  public void removeChangeListener(ILcdChangeListener aListener) {
    fChangeListeners.remove(aListener);
  }

  private void notifyChangeListeners() {
    for (ILcdChangeListener listener : fChangeListeners) {
      listener.stateChanged(new TLcdChangeEvent(this));
    }
  }

  @Override
  public boolean accept(Object aObject) {
    Object object = aObject;
    if (object instanceof TLcdCluster) {
      TLcdCluster cluster = (TLcdCluster) object;
      object = cluster.getComposingElements().iterator().next();
    }
    if (MilitarySymbolFacade.isMilitarySymbol(object)) {
      if (!echelonIsVisible(object)) {
        return false;
      }
      BattleDimension bd = BattleDimension.from(object);
      return fVisibleBattleDimensions.get(bd);
    } else {
      return true;
    }
  }

  private boolean echelonIsVisible(Object aSymbol) {
    EMilitarySymbology symbology = EMilitarySymbology.fromObject((aSymbol));
    Object echelon = MilitarySymbolFacade.getEchelon(aSymbol);
    if (echelon == null) {
      // Weather symbols don't have an echelon => make sure they are not dropped
      echelon = getDefaultEchelon(aSymbol);
    }
    Properties properties = fEchelonScaleProperties.get(symbology);
    if (properties == null) {
      properties = getScaleProperties(symbology);
      fEchelonScaleProperties.put(symbology, properties);
    }
    String key;
    if (echelon instanceof TLcdMS2525bEchelonNode) {
      key = ((TLcdMS2525bEchelonNode) echelon).getCodeMask().replace("*", "");
    } else if (echelon instanceof TLcdAPP6AEchelonNode) {
      key = ((TLcdAPP6AEchelonNode) echelon).getCodeMask().replace("*", "");
    } else {
      return false;
    }
    String minScale = properties.getProperty(key);
    return (minScale != null) && fTransformation.getScale() > Double.parseDouble(minScale);
  }

  private Object getDefaultEchelon(Object aSymbol) {
    if (aSymbol instanceof ILcdMS2525bCoded) {
      return TLcdMS2525bEchelonNode.getDefault(((ILcdMS2525bCoded) aSymbol).getMS2525Standard());
    } else if (aSymbol instanceof ILcdAPP6ACoded) {
      return TLcdAPP6AEchelonNode.getDefault(((ILcdAPP6ACoded) aSymbol).getAPP6Standard());
    } else {
      return null;
    }
  }

  private Properties getScaleProperties(EMilitarySymbology aMilitarySymbology) {
    Properties properties = new Properties();
    InputStream in = getClass().getResourceAsStream("/samples/lightspeed/demo/echelon/" +
                                                    aMilitarySymbology.name().toLowerCase() +
                                                    ".prop");
    try {
      properties.load(in);
    } catch (IOException ignored) {
    }
    return properties;
  }

  @Override
  public void itemStateChanged(ItemEvent e) {
    if (e.getSource() instanceof JToggleButton) {
      JToggleButton checkBox = (JToggleButton) e.getSource();
      fVisibleBattleDimensions.put((BattleDimension) checkBox.getClientProperty(BattleDimension.class),
                                   checkBox.isSelected());
      notifyChangeListeners();
    }
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
    if (evt.getSource() instanceof ILspView) {
      ILspView view = (ILspView) evt.getSource();
      ALspViewXYZWorldTransformation transformation = view.getViewXYZWorldTransformation();
      if (transformation != fTransformation) {
        fTransformation.removePropertyChangeListener(this);
        fTransformation = transformation;
        fTransformation.addPropertyChangeListener(this);
        notifyChangeListeners();
      }
    } else {
      notifyChangeListeners();
    }
  }
}

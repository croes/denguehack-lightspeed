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
package samples.lucy.tableview;

import java.beans.PropertyChangeEvent;

import javax.swing.JComponent;

import com.luciad.util.ALcdWeakPropertyChangeListener;

/**
 * <p>This listener listens for changes in the default formats and units of ILcyLucyEnv, and repaints
 * a component when any changes are detected.</p>
 *
 * <p>It uses a {@code ALcdWeakPropertyChangeListener} to automatically clean up this listener as soon as the given {@code JComponent}
 * does not exist anymore.</p>
 */
class LucyFormatListener extends ALcdWeakPropertyChangeListener<JComponent> {

  public LucyFormatListener(JComponent aComponent) {
    super(aComponent);
  }

  @Override
  protected void propertyChangeImpl(JComponent aComponent, PropertyChangeEvent aPropertyChangeEvent) {
    String prop = aPropertyChangeEvent.getPropertyName();
    if (prop == null || prop.matches("default.*")) { // for example 'defaultAltitudeFormat'
      aComponent.repaint();
    }
  }
}

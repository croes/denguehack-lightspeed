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
package samples.gxy.modelChanges;

import java.util.Enumeration;

import javax.swing.JTextArea;

import com.luciad.model.ILcdModelListener;
import com.luciad.model.TLcdModelChangedEvent;

/**
 * Model listener that shows the model changes in a JTextArea.
 */
public class ModelUpdateListener extends JTextArea implements ILcdModelListener {

  public ModelUpdateListener() {
    setEditable(false);
    setRows(8);
  }

  public void modelChanged(TLcdModelChangedEvent aEvent) {
    if ((aEvent.getCode() & TLcdModelChangedEvent.ALL_OBJECTS_CHANGED) != 0) {
      this.append("ALL_OBJECTS_CHANGED (" + aEvent.elementCount() + " elements)\n");
    }
    if ((aEvent.getCode() & TLcdModelChangedEvent.ALL_OBJECTS_REMOVED) != 0) {
      this.append("ALL_OBJECTS_REMOVED (" + aEvent.elementCount() + " elements)\n");
    }
    if ((aEvent.getCode() & TLcdModelChangedEvent.OBJECTS_ADDED) != 0) {
      this.append("OBJECTS_ADDED (" + aEvent.elementCount() + " elements)\n");
    }
    if ((aEvent.getCode() & TLcdModelChangedEvent.OBJECTS_CHANGED) != 0) {
      this.append("OBJECTS_CHANGED (" + aEvent.elementCount() + " elements)\n");
    }
    if ((aEvent.getCode() & TLcdModelChangedEvent.SOME_OBJECTS_REMOVED) != 0) {
      this.append("OBJECTS_REMOVED (" + aEvent.elementCount() + " elements)\n");
    }

    // Display the list of objects that have been added/removed or changed.
    Enumeration en = aEvent.elements();
    while (en.hasMoreElements()) {
      this.append("\t" + en.nextElement() + "\n");
    }
  }
}

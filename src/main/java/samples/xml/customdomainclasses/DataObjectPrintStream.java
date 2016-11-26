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
package samples.xml.customdomainclasses;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Collection;

import com.luciad.datamodel.ILcdDataObject;
import com.luciad.datamodel.TLcdDataProperty;

public class DataObjectPrintStream extends PrintStream {

  public DataObjectPrintStream(OutputStream aOut) {
    super(aOut);
  }

  public void printDataObject(ILcdDataObject object) {
    dump(object, "");
  }

  private void dump(Object value, String infix) {
    if (value instanceof ILcdDataObject) {
      dumpDataObject((ILcdDataObject) value, infix);
    } else {
      println(value);
    }
  }

  private void dumpDataObject(ILcdDataObject object, String infix) {
    println(object.getDataType().getDisplayName());
    infix = infix + "  ";
    for (TLcdDataProperty p : object.getDataType().getProperties()) {
      if (p.isCollection()) {
        Collection<?> c = (Collection<?>) object.getValue(p);
        int i = 1;
        for (Object v : c) {
          print(infix + p.getDisplayName() + "(" + i++ + ") : ");
          dump(v, infix);
        }
      } else {
        Object v = object.getValue(p);
        if (v != null) {
          print(infix + p.getDisplayName() + " : ");
          dump(v, infix);
        }
      }
    }
  }

}

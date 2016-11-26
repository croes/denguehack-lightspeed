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

import java.util.concurrent.CopyOnWriteArrayList;

import org.jdesktop.swingx.renderer.StringValue;

/**
 * <p>Composite implementation of <code>StringValue</code>.</p>
 *
 * <p>Implementations of <code>StringValue</code> added to this composite implementation may return
 * <code>null</code> for an object in the {@link StringValue#getString(Object)}
 * method, although it is documented in the interface this is not allowed. This composite
 * implementation will make sure that a non-<code>null</code> value is returned, even when all
 * <code>StringValue</code> instances return <code>null</code>. In that particular case, either the
 * <code>toString</code> or "null" will be returned.</p>
 */
class CompositeStringValue implements StringValue {

  private CopyOnWriteArrayList<StringValue> fStringValues = new CopyOnWriteArrayList<StringValue>();

  @Override
  public String getString(Object aObject) {
    for (StringValue stringValue : fStringValues) {
      String string = stringValue.getString(aObject);
      if (string != null) {
        return string;
      }
    }
    return aObject != null ? aObject.toString() : "null";
  }

  public void addStringValue(StringValue aStringValue) {
    fStringValues.add(aStringValue);
  }

  public void removeStringValue(StringValue aStringValue) {
    fStringValues.remove(aStringValue);
  }

  public int getStringValueCount() {
    return fStringValues.size();
  }

  public StringValue getStringValue(int aIndex) {
    return fStringValues.get(aIndex);
  }
}

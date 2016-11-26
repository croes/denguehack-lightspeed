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
package samples.decoder.ecdis.common.filter.columns;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import com.luciad.format.s52.TLcdS52DisplaySettings;
import samples.decoder.ecdis.common.ObjectClass;

/**
 * Extension of a {@link ARowObjectTableColumn} that allows changing the selected object classes for a given
 * {@link TLcdS52DisplaySettings display settings instance}.
 */
public class ObjectClassSelectionTableColumn extends ARowObjectTableColumn<ObjectClass> {

  private final TLcdS52DisplaySettings fDisplaySettings;
  private final IsShownFunction fDisplayValueFunction;

  /**
   * Creates a new instance.
   *
   * @param aDisplaySettings the S-52 display settings instance
   */
  public ObjectClassSelectionTableColumn(TLcdS52DisplaySettings aDisplaySettings) {
    super("Show", Boolean.class);
    fDisplaySettings = aDisplaySettings;
    fDisplayValueFunction = new IsShownFunction(fDisplaySettings);
  }

  @Override
  public Object getColumnValue(ObjectClass aRowObject) {
    return fDisplayValueFunction.apply(aRowObject);
  }

  @Override
  public void setColumnValue(Object aNewValue, ObjectClass aRowValue) {
    boolean showObjectClass = (boolean) aNewValue;
    if (showObjectClass) {
      addObjectClass(aRowValue);
    } else {
      removeObjectClass(aRowValue);
    }
  }

  private void addObjectClass(ObjectClass aObjectClass) {
    int[] objectClasses = fDisplaySettings.getObjectClasses();
    if (objectClasses == null) {
      fDisplaySettings.setObjectClasses(new int[]{aObjectClass.getCode()});
      return;
    }

    int[] newObjectClasses = Arrays.copyOf(objectClasses, objectClasses.length + 1);
    newObjectClasses[newObjectClasses.length - 1] = aObjectClass.getCode();
    fDisplaySettings.setObjectClasses(newObjectClasses);
  }

  private void removeObjectClass(ObjectClass aObjectClass) {
    int[] objectClasses = fDisplaySettings.getObjectClasses();
    if (objectClasses == null) {
      return;
    }

    List<Integer> resultList = new ArrayList<>();
    for (int objectClassCode : objectClasses) {
      if (objectClassCode != aObjectClass.getCode()) {
        resultList.add(objectClassCode);
      }
    }

    int[] result = new int[resultList.size()];
    for (int i = 0; i < resultList.size(); i++) {
      result[i] = resultList.get(i);
    }

    fDisplaySettings.setObjectClasses(result);
  }
}

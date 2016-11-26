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
package samples.hana.lightspeed.model;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import com.luciad.datamodel.ILcdDataObject;
import com.luciad.format.hana.TLcdHanaModelDescriptor;
import com.luciad.model.ALcdModel;
import com.luciad.reference.TLcdGeodeticReference;
import com.luciad.util.TLcdPair;

public class StormsModel extends ALcdModel {

  private final HanaModelSupport fModelSupport = new HanaModelSupport();

  private final HanaConnectionExecutorService fExecutorService;
  private final String fTableName;

  private final Collection<ILcdDataObject> fDomainObjects = new ArrayList<ILcdDataObject>(100);
  private final TreeMap<Long, Collection<ILcdDataObject>> fDomainObjectsByTime = new TreeMap<Long, Collection<ILcdDataObject>>();

  public StormsModel(HanaConnectionExecutorService aExecutorService, String aTableName) {
    setModelReference(new TLcdGeodeticReference());
    fExecutorService = aExecutorService;
    fTableName = aTableName;

    aExecutorService.submitQueryAndWait("select STORMTIME, SPEED, SHAPE from " + fTableName, new HanaConnectionExecutorService.ResultHandler() {
      @Override
      public void handleRow(ResultSet aResultSet) throws IOException, SQLException {
        TLcdHanaModelDescriptor modelDescriptor = (TLcdHanaModelDescriptor) getModelDescriptor();
        if (modelDescriptor == null) {
          modelDescriptor = fModelSupport.createHanaModelDescriptor(aResultSet.getMetaData(), "SHAPE", fExecutorService.getURL(), fTableName);
          setModelDescriptor(modelDescriptor);
        }

        ILcdDataObject dataObject = fModelSupport.createObject(aResultSet, modelDescriptor);
        fDomainObjects.add(dataObject);

        Long time = (Long) dataObject.getValue("STORMTIME");
        Collection<ILcdDataObject> objectsForTime = fDomainObjectsByTime.get(time);
        if (objectsForTime == null) {
          objectsForTime = new ArrayList<ILcdDataObject>();
          fDomainObjectsByTime.put(time, objectsForTime);
        }
        objectsForTime.add(dataObject);
      }
    });
  }

  @Override
  public Enumeration elements() {
    return new Enumeration() {

      private final Iterator it = fDomainObjects.iterator();

      @Override
      public boolean hasMoreElements() {
        return it.hasNext();
      }

      @Override
      public Object nextElement() {
        return it.next();
      }
    };
  }

  public TLcdPair<Long, Long> getActiveTimeRange() {
    if (fDomainObjectsByTime.isEmpty()) {
      return new TLcdPair<Long, Long>(0L, 0L);
    }
    return new TLcdPair<Long, Long>(fDomainObjectsByTime.firstKey(), fDomainObjectsByTime.lastKey());
  }

  /**
   * Get the storm objects within a day of the given time.
   */
  public Collection<ILcdDataObject> getActiveStorms(long aTime) {
    Map.Entry<Long, Collection<ILcdDataObject>> objects = fDomainObjectsByTime.floorEntry(aTime);
    if (objects != null) {
      if ((aTime - objects.getKey()) < 24 * 60 * 60 * 1000) {
        return objects.getValue();
      }
    }
    return Collections.emptySet();
  }
}

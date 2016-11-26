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
import java.util.Enumeration;
import java.util.concurrent.atomic.AtomicInteger;

import com.luciad.format.hana.TLcdHanaModelDescriptor;
import com.luciad.model.ALcdModel;
import com.luciad.model.ILcd2DBoundsIndexedModel;
import com.luciad.reference.TLcdGeodeticReference;
import com.luciad.shape.ILcdBounds;
import com.luciad.shape.shape2D.TLcdLonLatBounds;
import com.luciad.util.ILcdFunction;

/**
 * Model that is a bounds-indexed model, that queries data each time applyOnInteract2D is called. This makes
 * this model well suited for plot layers (TLspPlotLayerBuilder).
 *
 * Loads only the geometry.  Override {@link #createObject} to create your own instances.
 */
public class BoundsIndexedHanaModel extends ALcdModel implements ILcd2DBoundsIndexedModel {

  private final HanaModelSupport fModelSupport = new HanaModelSupport();

  private final String fTableName;
  private final String fGeometryColumn;
  private final HanaConnectionExecutorService fHanaConnectionExecutorService;

  public BoundsIndexedHanaModel(String aTableName, String aGeometryColumn, HanaConnectionExecutorService aHanaConnectionExecutorService) {
    fTableName = aTableName;
    fGeometryColumn = aGeometryColumn;
    fHanaConnectionExecutorService = aHanaConnectionExecutorService;
    setModelReference(new TLcdGeodeticReference());

    String query = "select * from " + fTableName + " limit 1";
    fHanaConnectionExecutorService.submitQueryAndWait(query, new HanaConnectionExecutorService.ResultHandler() {
      @Override
      public void handleRow(ResultSet aResultSet) throws IOException, SQLException {
        setModelDescriptor(fModelSupport.createHanaModelDescriptor(aResultSet.getMetaData(),
                                                                   fGeometryColumn,
                                                                   fHanaConnectionExecutorService.getURL(),
                                                                   fTableName));
      }
    });
  }

  public HanaConnectionExecutorService getHanaConnectionExecutorService() {
    return fHanaConnectionExecutorService;
  }

  @Override
  public int applyOnInteract2DBounds(ILcdBounds aBounds, boolean aStrictInteract, ILcdFunction aFunction, double aPrecisionX, double aPrecisionY) {
    return applyOnInteract2DBounds(aBounds, aStrictInteract, aFunction, aPrecisionX, aPrecisionY, 0, 0, true);
  }

  @Override
  public int applyOnInteract2DBounds(ILcdBounds aBounds, boolean aStrictInteract, final ILcdFunction aFunction, double aPrecisionX, double aPrecisionY, double aMinSizeX, double aMinSizeY, boolean aIncludePoints) {
    final AtomicInteger count = new AtomicInteger();
    double startX = aBounds.getLocation().getX();
    double startY = aBounds.getLocation().getY();
    double endX = startX + aBounds.getWidth();
    double endY = startY + aBounds.getHeight();
    String query = getTileQuery(fGeometryColumn + ".ST_IntersectsRect(new ST_Point(" + startX + ", " + startY + "), new ST_Point(" + endX + ", " + endY + ")) = 1");

    fHanaConnectionExecutorService.submitQueryAndWait(query, new HanaConnectionExecutorService.ResultHandler() {
      @Override
      public void handleRow(ResultSet aResultSet) throws IOException, SQLException {
        aFunction.applyOn(createObject(aResultSet));
        count.incrementAndGet();
      }
    });

    return count.get();
  }

  protected String getTileQuery(String aSpatialQuery) {
    return "select * from " + fTableName + " where " + aSpatialQuery;
  }

  protected Object createObject(ResultSet aResultSet) {
    TLcdHanaModelDescriptor modelDescriptor = (TLcdHanaModelDescriptor) getModelDescriptor();
    return fModelSupport.createObject(aResultSet, modelDescriptor);
  }

  @Override
  public ILcdBounds getBounds() {
    return new TLcdLonLatBounds(-180, 19, 120, 50);
  }

  @Override
  public Enumeration elements() {
    throw new UnsupportedOperationException();
  }
}

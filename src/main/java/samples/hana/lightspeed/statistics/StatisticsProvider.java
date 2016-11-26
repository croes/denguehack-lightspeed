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
package samples.hana.lightspeed.statistics;

import static samples.hana.lightspeed.domain.AdministrativeLevel.*;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.luciad.datamodel.ILcdDataObject;
import com.luciad.datamodel.TLcdDataProperty;
import com.luciad.util.ILcdChangeListener;
import com.luciad.util.ILcdChangeSource;
import com.luciad.util.TLcdChangeEvent;
import com.luciad.util.TLcdChangeSupport;
import com.luciad.util.TLcdPair;

import samples.hana.lightspeed.common.Configuration;
import samples.hana.lightspeed.domain.AdministrativeLevel;
import samples.hana.lightspeed.domain.InsuranceCompany;
import samples.hana.lightspeed.model.HanaConnectionExecutorService;
import samples.hana.lightspeed.model.HanaDatabaseUtil;

/**
 * Asynchronously loads statistics for the active storm shapes.
 *
 * This provider is thread-safe.
 */
public class StatisticsProvider implements ILcdChangeSource {

  private static final String SCHEMA_NAME = Configuration.get("database.schema");
  private static final String CUSTOMERS_TABLE = Configuration.get("database.customers.tableName");
  private static final String TABLE_NAME = SCHEMA_NAME + "." + CUSTOMERS_TABLE;

  private static final String TOTALS_QUERY = "select sc.STATE_ID, sc.COUNTY_ID, sc.INSURANCE_ID, count(*) COUNT, sum(POLICY_VALUE) VALUE from " + TABLE_NAME + " sc group by sc.STATE_ID, sc.COUNTY_ID, sc.INSURANCE_ID";
  private static final String AFFECTED_QUERY_A = "select sc.STATE_ID, sc.COUNTY_ID, sc.INSURANCE_ID, count(*) COUNT, sum(POLICY_VALUE) VALUE from " + TABLE_NAME + " sc where (sc.LOCATION.ST_Intersects(ST_GeomFromWKB(x'";
  private static final String AFFECTED_QUERY_B = "')) = 1) group by sc.STATE_ID, sc.COUNTY_ID, sc.INSURANCE_ID";

  private final ObjectFactory<Statistics> fNewStatisticsFactory = new ObjectFactory<Statistics>() {
    @Override
    public Statistics create() {
      return createStatistics();
    }
  };
  private final ObjectFactory<StormStatistics> fNewStormCustomerStatisticsFactory = new ObjectFactory<StormStatistics>() {
    @Override
    public StormStatistics create() {
      return new StormStatistics(createStatistics(), createStatistics(), createStatistics());
    }
  };
  private final ObjectFactory<Map<Object, StormStatistics>> fNewMapFactory = new ObjectFactory<Map<Object, StormStatistics>>() {
    @Override
    public Map<Object, StormStatistics> create() {
      return new ConcurrentHashMap<Object, StormStatistics>();
    }
  };
  private final ObjectFactory<Statistics> fEmptyStatisticsFactory = new SingleObjectFactory<Statistics>(createStatistics());
  private final ObjectFactory<StormStatistics> fEmptyStormStatisticsFactory = new SingleObjectFactory<StormStatistics>(new StormStatistics(fEmptyStatisticsFactory.create(), fEmptyStatisticsFactory.create(), fEmptyStatisticsFactory.create()));

  private final TLcdChangeSupport fChangeSupport = new TLcdChangeSupport();
  private final HanaConnectionExecutorService fHanaConnectionExecutorService;

  private final Map<Object, Statistics> fTotals = new ConcurrentHashMap<Object, Statistics>();
  private final Map<Object, Map<Object, StormStatistics>> fAffected = new ConcurrentHashMap<Object, Map<Object, StormStatistics>>();
  private Collection<ILcdDataObject> fActiveStorms = Collections.emptySet();

  private Set<Object> fScheduledQueries = Collections.synchronizedSet(new HashSet<Object>());
  private Set<Object> fCompletedQueries = Collections.synchronizedSet(new HashSet<Object>());

  public StatisticsProvider(HanaConnectionExecutorService aHanaConnectionExecutorService) {
    fHanaConnectionExecutorService = aHanaConnectionExecutorService;

    fHanaConnectionExecutorService.submitQuery(getTotalsQuery(), new HanaConnectionExecutorService.ResultHandler() {
      @Override
      public void handleRow(ResultSet aResultSet) throws IOException, SQLException {
        String stateFips = aResultSet.getString("STATE_ID");
        String stateCountyFips = aResultSet.getString("COUNTY_ID");
        accumulate(get(fTotals, state + "-" + stateFips, fNewStatisticsFactory), aResultSet);
        accumulate(get(fTotals, county + "-" + stateFips + "-" + stateCountyFips, fNewStatisticsFactory), aResultSet);
        accumulate(get(fTotals, country + "-" + "US", fNewStatisticsFactory), aResultSet);
      }

      @Override
      public void handleEnd() throws IOException, SQLException {
        fCompletedQueries.add("TOTAL");
        fireChangeEvent();
      }
    });
  }

  private Statistics createStatistics() {
    return new Statistics();
  }

  private String getTotalsQuery() {
    return TOTALS_QUERY;
  }

  private String[] getAffectedQuery() {
    return new String[]{AFFECTED_QUERY_A, AFFECTED_QUERY_B};
  }

  private static void accumulate(Statistics aStatistics, ResultSet aResultSet) throws SQLException {
    int insuranceId = aResultSet.getInt("INSURANCE_ID");
    int count = aResultSet.getInt("COUNT");
    int value = aResultSet.getInt("VALUE");
    InsuranceCompany customerCategory = InsuranceCompany.valueOfDb(insuranceId);
    aStatistics.add(customerCategory, count, value);
  }

  @Override
  public void removeChangeListener(ILcdChangeListener aListener) {
    fChangeSupport.removeChangeListener(aListener);
  }

  @Override
  public void addChangeListener(ILcdChangeListener aListener) {
    fChangeSupport.addChangeListener(aListener);
  }

  public void fireChangeEvent() {
    fChangeSupport.fireChangeEvent(new TLcdChangeEvent(this));
  }

  public void setActiveStorms(Collection<ILcdDataObject> aStorms) {
    fActiveStorms = aStorms;
    getOrScheduleActiveStormKey(aStorms);
    fireChangeEvent();
  }

  public Statistics getTotal(AdministrativeLevel aLevel, String aKey) {
    if (!fCompletedQueries.contains("TOTAL")) {
      return null;
    }
    return get(fTotals, aLevel + "-" + aKey, fEmptyStatisticsFactory);
  }

  public StormStatistics getAffected(AdministrativeLevel aLevel, String aKey) {
    if (fActiveStorms == null) {
      // No storms model available
      return fEmptyStormStatisticsFactory.create();
    }
    Object stormKey = getOrScheduleActiveStormKey(fActiveStorms);
    if (stormKey == null) {
      return null;
    }

    Map<Object, StormStatistics> map = fAffected.get(stormKey);
    return get(map, aLevel + "-" + aKey, fEmptyStormStatisticsFactory);
  }

  private Object getOrScheduleActiveStormKey(Collection<ILcdDataObject> aStorms) {
    if (aStorms == null) {
      return null;
    }
    boolean complete = true;
    if (aStorms.isEmpty()) {
      complete = schedule(null, aStorms);
    } else {
      for (ILcdDataObject storm : aStorms) {
        complete &= schedule(storm, aStorms);
      }
    }
    if (!complete) {
      return null;
    }

    return aStorms;
  }

  private boolean schedule(final ILcdDataObject aStorm, Object aStormKey) {

    final Object stormKey = aStormKey;
    final int speed = aStorm == null ? 0 : ((Number) aStorm.getValue("SPEED")).intValue();
    final Object queryKey = new TLcdPair<Object, Integer>(stormKey, speed);

    if (fCompletedQueries.contains(queryKey)) {
      return true;
    }

    get(fAffected, stormKey, fNewMapFactory);

    if (aStorm == null) {
      fCompletedQueries.add(queryKey);
      fireChangeEvent();
      return false;
    }

    if (fScheduledQueries.add(queryKey)) {
      String polygon;
      try {
        TLcdDataProperty shapeProperty = aStorm.getDataType().getProperty("SHAPE");
        byte[] geometry = (byte[]) aStorm.getValue(shapeProperty);
        polygon = new String(HanaDatabaseUtil.toHex(geometry));
      } catch (IOException e) {
        throw new IllegalStateException(e);
      }

      String[] affectedQuery = getAffectedQuery();
      fHanaConnectionExecutorService.submitQuery(affectedQuery[0] + polygon + affectedQuery[1], new HanaConnectionExecutorService.ResultHandler() {
        @Override
        public void handleRow(ResultSet aResultSet) throws IOException, SQLException {
          String stateFips = aResultSet.getString("STATE_ID");
          String stateCountyFips = aResultSet.getString("COUNTY_ID");
          accumulate(get(get(fAffected, stormKey, fNewMapFactory), state + "-" + stateFips, fNewStormCustomerStatisticsFactory).get(speed), aResultSet);
          accumulate(get(get(fAffected, stormKey, fNewMapFactory), county + "-" + stateFips + "-" + stateCountyFips, fNewStormCustomerStatisticsFactory).get(speed), aResultSet);
          accumulate(get(get(fAffected, stormKey, fNewMapFactory), country + "-" + "US", fNewStormCustomerStatisticsFactory).get(speed), aResultSet);
        }

        @Override
        public void handleEnd() throws IOException, SQLException {
          fCompletedQueries.add(queryKey);
          fireChangeEvent();
        }
      });
    }

    return false;
  }

  public interface ObjectFactory<T> {
    public T create();
  }

  public static class SingleObjectFactory<T> implements ObjectFactory<T> {
    private final T fInstance;

    public SingleObjectFactory(T aInstance) {
      fInstance = aInstance;
    }

    @Override
    public T create() {
      return fInstance;
    }
  }

  private static <T> T get(Map<Object, T> aMap, Object aKey, ObjectFactory<T> aFactory) {
    T statistics = aMap.get(aKey);
    if (statistics == null) {
      statistics = aFactory.create();
      aMap.put(aKey, statistics);
    }
    return statistics;
  }
}

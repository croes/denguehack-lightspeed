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
package samples.lucy.fundamentals.tracks.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import com.luciad.datamodel.ILcdDataObject;
import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelDescriptor;
import com.luciad.model.ILcdModelReference;
import com.luciad.model.TLcd2DBoundsIndexedModel;
import com.luciad.multidimensional.ILcdDimension;
import com.luciad.multidimensional.ILcdMultiDimensionalModel;
import com.luciad.multidimensional.TLcdDimension;
import com.luciad.multidimensional.TLcdDimensionAxis;
import com.luciad.multidimensional.TLcdDimensionFilter;
import com.luciad.multidimensional.TLcdDimensionInterval;
import com.luciad.realtime.ALcdTimeIndexedSimulatorModel;
import com.luciad.shape.shape3D.TLcdLonLatHeightPoint;

/**
 * <p>
 *   {@code ILcdMultiDimensionalModel} implementation for the track models.
 * </p>
 *
 * <p>
 *   Internally, this model uses a {@link ALcdTimeIndexedSimulatorModel} to keep a time-based index and
 *   update the tracks when necessary.
 * </p>
 */
final class TracksModel extends TLcd2DBoundsIndexedModel implements ILcdMultiDimensionalModel {

  private final ALcdTimeIndexedSimulatorModel fTimeIndexedSimulatorModel;
  private final List<ILcdDimension<Date>> fDimensions = new ArrayList<>();

  private TLcdDimensionFilter fDimensionFilter = TLcdDimensionFilter.EMPTY_FILTER;

  public TracksModel(ILcdModelReference aModelReference,
                     ILcdModelDescriptor aModelDescriptor,
                     final Collection<ILcdDataObject> aTracks) {
    super(aModelReference, aModelDescriptor);

    fDimensions.add(buildDateDimension(aTracks));

    fTimeIndexedSimulatorModel = new ALcdTimeIndexedSimulatorModel() {
      {
        init(TracksModel.this, aTracks);
      }

      @Override
      protected long getBeginTime(Object aTrack) {
        List<Long> timeStamps = (List<Long>) ((ILcdDataObject) aTrack).getValue(TracksDataTypes.TIME_STAMPS);
        return timeStamps.get(0);
      }

      @Override
      protected long getEndTime(Object aTrack) {
        List<Long> timeStamps = (List<Long>) ((ILcdDataObject) aTrack).getValue(TracksDataTypes.TIME_STAMPS);
        return timeStamps.get(timeStamps.size() - 1);
      }

      @Override
      protected boolean updateTrackForDateSFCT(ILcdModel aILcdModel, Object aTrack, Date aDate) {
        List<Long> timeStamps = (List<Long>) ((ILcdDataObject) aTrack).getValue(TracksDataTypes.TIME_STAMPS);
        List<TLcdLonLatHeightPoint> locations = (List<TLcdLonLatHeightPoint>) ((ILcdDataObject) aTrack).getValue(TracksDataTypes.RECORDED_POSITIONS);

        //Find the index of the time stamp matching with the new date
        int binarySearchIndex = Collections.binarySearch(timeStamps, aDate.getTime());
        int index;
        if (binarySearchIndex >= 0) {
          index = binarySearchIndex;
        } else {
          index = Math.min(timeStamps.size() - 1, Math.max(0, -1 * (binarySearchIndex + 1) - 1));
        }

        Object oldPosition = ((ILcdDataObject) aTrack).getValue(TracksDataTypes.CURRENT_POSITION);
        //Retrieve the position matching the new time stamp
        TLcdLonLatHeightPoint newPosition = locations.get(index);
        //Store this position in the object
        ((ILcdDataObject) aTrack).setValue(TracksDataTypes.CURRENT_POSITION, newPosition);
        //Compare the new and old position to indicate whether the track was updated or not
        return Objects.equals(oldPosition, newPosition);
      }
    };
  }

  /**
   * Create a {@code TLcdDimension} representing the union of all the individual timestamps contained in the flight.
   */
  private TLcdDimension<Date> buildDateDimension(Collection<ILcdDataObject> aTracks) {
    Long startDate = Long.MAX_VALUE;
    Long endDate = Long.MIN_VALUE;
    for (ILcdDataObject track : aTracks) {
      List<Long> timeStamps = (List<Long>) track.getValue(TracksDataTypes.TIME_STAMPS);

      startDate = Math.min(startDate, timeStamps.get(0));
      endDate = Math.max(endDate, timeStamps.get(timeStamps.size() - 1));
    }

    return TLcdDimension.<Date>newBuilder()
        .axis(TLcdDimensionAxis.TIME_AXIS)
        .addInterval(TLcdDimensionInterval.create(Date.class, new Date(startDate), new Date(endDate)))
        .build();
  }

  @Override
  public void applyDimensionFilter(TLcdDimensionFilter aFilter, int aEventMode) {
    fDimensionFilter = aFilter;

    Set<TLcdDimensionAxis<?>> axes = aFilter.getAxes();
    for (TLcdDimensionAxis<?> ax : axes) {
      if (ax.getType().equals(Date.class)) {
        TLcdDimensionInterval<Date> interval = (TLcdDimensionInterval<Date>) aFilter.getInterval(ax);
        //We opted for a simple implementation, where we only use the max value of the interval
        //instead of the whole interval
        fTimeIndexedSimulatorModel.setDate(interval.getMax());
      }
    }
  }

  @Override
  public TLcdDimensionFilter getDimensionFilter() {
    return fDimensionFilter;
  }

  @Override
  public List<? extends ILcdDimension<?>> getDimensions() {
    return Collections.unmodifiableList(fDimensions);
  }
}

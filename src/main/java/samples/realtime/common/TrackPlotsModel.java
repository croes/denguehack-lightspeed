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
package samples.realtime.common;

import java.util.Date;

import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelDescriptor;
import com.luciad.model.ILcdModelReference;
import com.luciad.realtime.TLcdTrackModel;
import com.luciad.shape.ILcdBounded;
import com.luciad.shape.ILcdBounds;

/**
 * A model for track plots.
 */
public class TrackPlotsModel extends TLcdTrackModel implements ILcdModel, ILcdBounded {

  private static final int TARGET_BIN_SIZE = 5000;
  private final Date fBeginDate, fEndDate;
  private final ILcdBounds fBounds;

  public TrackPlotsModel(ILcdModelReference aModelReference, ILcdModelDescriptor aModelDescriptor, Date aBeginDate, Date aEndDate, ILcdBounds aBounds, int aExpectedSize) {
    super(aBounds, getRowCount(aBounds, aExpectedSize), getColumnCount(aBounds, aExpectedSize));
    setModelDescriptor(aModelDescriptor);
    setModelReference(aModelReference);
    fBeginDate = aBeginDate;
    fEndDate = aEndDate;
    fBounds = aBounds;
  }

  private static int getRowCount(ILcdBounds aBounds, int aExpectedSize) {
    // Ideal row count assuming a uniform distribution
    return Math.max(2, (int) Math.round(Math.sqrt(aExpectedSize / TARGET_BIN_SIZE * aBounds.getHeight() / aBounds.getWidth())));
  }

  private static int getColumnCount(ILcdBounds aBounds, int aExpectedSize) {
    // Ideal column count assuming a uniform distribution
    return Math.max(2, (int) Math.round(Math.sqrt(aExpectedSize / TARGET_BIN_SIZE * aBounds.getWidth() / aBounds.getHeight())));
  }

  /**
   * <p>Returns the begin Date of the track plots.</p>
   *
   * @return The begin Date of track plots.
   */
  public Date getBeginDate() {
    return fBeginDate;
  }

  /**
   * <p>Returns the end Date of the track plots.</p>
   *
   * @return The end Date of track plots.
   */
  public Date getEndDate() {
    return fEndDate;
  }

  @Override
  public ILcdBounds getBounds() {
    return fBounds;
  }
}

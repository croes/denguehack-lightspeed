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
package samples.decoder.kml22.common.timetoolbar.advanced;

import com.luciad.realtime.ILcdSimulatorModel;
import samples.decoder.kml22.common.timetoolbar.common.SimulatorModel;
import samples.decoder.kml22.common.timetoolbar.common.TimeMediator;

/**
 * <p>An extension of <code>ILcdSimulatorModel</code> that also includes support for interval lengths.</p>
 * <p>This <code>ILcdSimulatorModel</code> can be used in a <code>TLcdSimulator</code>.</p>
 */
public interface IntervalSimulatorModel extends SimulatorModel, ILcdSimulatorModel {
  /**
   * Sets the interval length of this IntervaledSimulatorModel
   * @return the interval length of this simulator model
   */
  public long getIntervalLength();

  /**
   * Sets the interval length of this simulator model
   * @param aIntervalLength An interval length in milliseconds
   */
  public void setIntervalLength(long aIntervalLength);
    /**
   * Gets the <code>TimeMediator</code> used by this <code>SimulatorModel</code>
   * @return the <code>TimeMediator</code>
   */
  public TimeMediator getTimeMediator();
  /**
   * Returns whether this <code>SimulatorModel</code> has any valid time data.
   * @return true if this simulator model has valid time data, false otherwise.
   */
  public boolean hasTimeData();
}

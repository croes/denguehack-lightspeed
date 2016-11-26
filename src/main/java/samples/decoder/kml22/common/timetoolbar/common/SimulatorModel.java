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
package samples.decoder.kml22.common.timetoolbar.common;

import java.beans.PropertyChangeListener;
import java.util.Date;

/**
 * <p>An interface for defining a model in a time toolbar. It offers support for</p>
 * <lu>
 * <li>An interval date</li>
 * <li>An interval length</li>
 * <li>A global begin date</li>
 * <li>A global end date</li>
 * <li>Property change listeners</li>
 * </lu>
 * <p>The interval range is defined as the interval that lies between the global range, and in which
 * the elements of the <code>SimulatorModel</code> must be displayed.</p>
 * <p>The global range is defined as the absolute time range in which elements (whether they are visible or not)
 * can exist.</p>
 */
public interface SimulatorModel {
  /**
   * Sets the end date of the interval range of this simulator model
   * @param date a date
   */
  public void setDate(Date date);

  /**
   * Gets the end date of the interval.
   * @return the end date of the interval
   */
  public Date getDate();

  /**
   * Gets the global begin date of this simulator model.
   * @return Gets the global begin date.
   */
  public Date getBeginDate();

  /**
   * Gets the global end date of this simulator model.
   * @return Gets the global end date.
   */
  public Date getEndDate();

  /**
   * <p>Adds a property change listener to this <code>SimulatorModel</code></p>
   * <p>The following property changes will be thrown:</p>
   * <ul>
   * <li>hasTimeData</li>
   * <li>intervalEndDate</li>
   * <li>intervalLength</li>
   * <li>globalEndDate</li>
   * <li>globalBeginDate</li>
   * <li>modelChanged</li>
   * </ul>
   * @param propertychangelistener a PropertyChangeListener
   */
  public void addPropertyChangeListener(PropertyChangeListener propertychangelistener);

  /**
   * Adds a property change listener to this <code>SimulatorModel</code>
   * @param propertychangelistener a PropertyChangeListener
   */
  public void removePropertyChangeListener(PropertyChangeListener propertychangelistener);
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

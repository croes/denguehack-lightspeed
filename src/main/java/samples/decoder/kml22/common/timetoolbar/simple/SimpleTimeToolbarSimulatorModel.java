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
package samples.decoder.kml22.common.timetoolbar.simple;

import com.luciad.view.ILcdLayered;
import samples.decoder.kml22.common.timetoolbar.common.SimulatorModel;
import samples.decoder.kml22.common.timetoolbar.common.TimeToolbarModel;

import java.util.Date;

/**
 * <p>A simple simulator model that extends directly from TimeToolbarModel and SimulatorModel, so there
 * is no dependency on the RealTime optional module</p> 
 */
public class SimpleTimeToolbarSimulatorModel extends TimeToolbarModel implements SimulatorModel {
  /**
   * Creates a new <code>TimeToolbarModel</code> given a <code>ILcdLayered</code>. This constructor
   * will scan through the given <code>ILcdLayered</code> and register necessary listeners to make
   * this SimulatorModel completely self sufficient.
   *
   * @param aLayered an <code>ILcdLayered</code>
   */
  public SimpleTimeToolbarSimulatorModel( ILcdLayered aLayered ) {
    super( aLayered );
  }

  public void setDate( Date aDate ) {
    setIntervalEndDate( aDate.getTime() );
  }

  public Date getDate() {
    return new Date(getIntervalEndDate());
  }

  public Date getBeginDate() {
    return new Date(getGlobalBeginDate());
  }

  public Date getEndDate() {
    return new Date(getGlobalEndDate());
  }
}

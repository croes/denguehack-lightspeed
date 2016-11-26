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
package samples.decoder.asdi.datamodelviewer;

import java.awt.EventQueue;

import com.luciad.datamodel.TLcdDataType;
import com.luciad.format.asdi.TLcdASDIDataTypes;
import com.luciad.format.asdi.TLcdASDIFlightPlanDataTypes;
import com.luciad.format.asdi.TLcdASDITrackTODataTypes;
import com.luciad.format.asdi.TLcdASDITrackTZDataTypes;
import samples.common.LuciadFrame;

import samples.common.dataModelViewer.DataModelViewerSample;

/**
 * This sample shows the data model of all supported ASDI categories, for tracks, trajectories, 
 * flightplans and flight plan histories.
 *
 * By clicking on a property, this sample will generate a <code>String</code> that can be used in 
 * conjunction with a {@link com.luciad.datamodel.expression.TLcdDataObjectExpressionLanguage}
 * to obtain the value of a data property. This includes nested properties.<p/>
 */
public class MainPanel extends DataModelViewerSample {

  public MainPanel() {
    super(new TLcdDataType[]{
        TLcdASDIDataTypes.getTrajectoryType(),
        TLcdASDIDataTypes.getFlightPlanHistoryType(),
        TLcdASDIFlightPlanDataTypes.FlightPlanType,
        TLcdASDITrackTODataTypes.TrackTOType,
        TLcdASDITrackTZDataTypes.TrackTZType
    });

  }

  public static void main(final String[] aArgs) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        new LuciadFrame(new MainPanel(), "ASDI Data Model Viewer");
      }
    });
  }
}

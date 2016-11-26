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
package samples.realtime.lightspeed.offsetIcon;

import java.io.IOException;

import com.luciad.reference.TLcdGeodeticReference;
import com.luciad.shape.ILcdBounds;
import com.luciad.shape.shape2D.TLcdLonLatBounds;

import samples.common.SampleData;
import samples.lightspeed.common.FitUtil;
import samples.lightspeed.common.LightspeedSample;
import samples.lightspeed.common.LspDataUtil;

/**
 * This sample demonstrates how to combine offset label placement and continuous label decluttering.
 * It also shows how to customize the labeling algorithm by
 * <ul>
 *   <li>adjusting the desired label locations.</li>
 *   <li>configuring the master-slave dependency.</li>
 * </ul>
 */
public class MainPanel extends LightspeedSample {

  protected void addData() throws IOException {
    super.addData();
    LspDataUtil.instance().model(SampleData.US_CITIES).layer(new OffsetIconLayerFactory()).addToView(getView());
    final ILcdBounds bounds = new TLcdLonLatBounds(-80, 35, 10, 10);
    FitUtil.fitOnBounds(this, bounds, new TLcdGeodeticReference());
  }

  public static void main(final String[] aArgs) {
    startSample(MainPanel.class, "Offset icons sample");
  }

}

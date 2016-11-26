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
package samples.tea.lightspeed.los.view;

import com.luciad.tea.lightspeed.los.view.TLspLOSCoverageStyle;
import com.luciad.view.lightspeed.TLspContext;
import com.luciad.view.lightspeed.style.styler.ALspStyleCollector;
import com.luciad.view.lightspeed.style.styler.ALspStyler;

import java.util.Collection;

/**
 * <p>
 *   A styler implementation for LOS Coverages.
 * </p>
 * <p>
 *   This styler is used in the sample to generate custom styles for the calculated LOS
 *   coverages. The styles can be set using the {@link #setLOSCoverageStyle(TLspLOSCoverageStyle)}
 *   method. In this sample, this method is called by the
 *   {@link samples.tea.lightspeed.los.gui.LOSStylePanel LOSStylePanel} class.
 * </p>
 * <p>
 *   Note: If no style was set, it will generate a default style.
 * </p>
 */
public class LOSCoverageStyler extends ALspStyler {

  private static final TLspLOSCoverageStyle sDefaultStyle = new TLspLOSCoverageStyle.Builder().build();

  private TLspLOSCoverageStyle fLOSCoverageStyle;

  public LOSCoverageStyler() {
    // Default constructor
  }

  public void setLOSCoverageStyle( TLspLOSCoverageStyle aStyle ) {
    fLOSCoverageStyle = aStyle;
    fireStyleChangeEvent();
  }

  public TLspLOSCoverageStyle getLOSCoverageStyle() {
    if ( fLOSCoverageStyle != null ) {
      return fLOSCoverageStyle;
    }
    else {
      return sDefaultStyle;
    }
  }

  @Override
  public void style( Collection<?> aObjects, ALspStyleCollector aStyleCollector, TLspContext aContext ) {
    if ( fLOSCoverageStyle != null ) {
      aStyleCollector.objects( aObjects ).style( fLOSCoverageStyle ).submit();
    }
    else {
      aStyleCollector.objects( aObjects ).style( sDefaultStyle ).submit();
    }
  }

}

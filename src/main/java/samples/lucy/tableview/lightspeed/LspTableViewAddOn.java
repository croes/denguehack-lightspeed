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
package samples.lucy.tableview.lightspeed;

import samples.lucy.tableview.ITableViewLogic;
import samples.lucy.tableview.TableViewAddOn;
import com.luciad.lucy.ILcyLucyEnv;

/**
 * <p>This add-on is the Lightspeed equivalent of the <code>TableViewAddOn</code>. It registers a customizer
 * panel factory, which creates table views for Lightspeed layers.</p>
 *
 * <p>There are two differences between the 2D and Lightspeed table add-on: the first one is how the fitting/centering of the
 * view works. This dependency is created in the {@link samples.lucy.tableview.TableViewAddOn#createTableViewLogic(ILcyLucyEnv)}
 * method. Therefore, this add-on can extend from the <code>TableViewAddOn</code> and override
 * that particular method.<br />
 * The second difference can be found in how filters are added or retrieved to and from layers.
 * This dependency is created in the {@link samples.lucy.tableview.TableViewAddOn#createTableViewLogic(ILcyLucyEnv)}</p>
 */
public class LspTableViewAddOn extends TableViewAddOn {

  public LspTableViewAddOn() {
    this("samples.lucy.tableview.lightspeed.LspTableViewAddOn.",
         "LspTableViewAddOn.");
  }

  public LspTableViewAddOn(String aLongPrefix, String aShortPrefix) {
    super(aLongPrefix, aShortPrefix);
  }

  @Override
  protected ITableViewLogic createTableViewLogic(ILcyLucyEnv aLucyEnv) {
    return new LspTableViewLogic(aLucyEnv);
  }

}

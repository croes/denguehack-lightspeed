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
package samples.lucy.datarepository;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;

import com.luciad.gui.ALcdAction;
import com.luciad.lucy.TLcyDataFormatManager;
import com.luciad.lucy.map.ILcyMapManager;

/**
 * An {@link ALcdAction ALcdAction} which passes a data source to a
 * data source handler.
 */
public class AddDataAction extends ALcdAction {
  private TLcyDataFormatManager fDataFormatManager;
  private String fPathToFile;
  private ILcyMapManager fMapManager;
  private String[] fMenuItemArray;
  private List<String> fMenuItemList = new ArrayList<String>();
  private ExecutorService fExecutorService;

  public AddDataAction(TLcyDataFormatManager aDataFormatManager,
                       String aName,
                       ILcyMapManager aMapManager,
                       String[] aMenuItemArray,
                       String aPathToFile,
                       ExecutorService aExecutorService) {
    super(aName);
    fDataFormatManager = aDataFormatManager;
    fPathToFile = aPathToFile;
    fMapManager = aMapManager;
    fMenuItemArray = aMenuItemArray;
    fMenuItemList.addAll(Arrays.asList(aMenuItemArray));
    fExecutorService = aExecutorService;
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    fExecutorService.execute(new Runnable() {
      @Override
      public void run() {
        //handle the data source
        fDataFormatManager.handleDataSources(new String[]{fPathToFile},
                                             fMapManager.getActiveMapComponent(),
                                             null,
                                             fMapManager.getActiveMapComponent().getComponent());
      }
    });
  }

  public String[] getMenuItemArray() {
    return fMenuItemArray;
  }

  public List<String> getMenuItemList() {
    return fMenuItemList;
  }
}

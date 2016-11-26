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
package samples.lightspeed.datamodel;

import java.io.IOException;
import java.util.Iterator;

import com.luciad.util.collections.ILcdCollectionListener;
import com.luciad.util.collections.TLcdCollectionEvent;
import com.luciad.view.ILcdLayer;

import samples.common.SampleData;
import samples.lightspeed.common.LspDataUtil;

/**
 * This sample shows the TLcdDataModel of an ILcdModel that has an
 * ILcdDataModelDescriptor as a model descriptor.
 * <p/>
 * The data model tree contains all declared TLcdDataType instances of the TLcdDataModel. Each
 * TLcdDataType contains a series of properties and TLcdDataProperties.
 * All TLcdDataTypes that are also model element TLcdDataType instances are highlighted in a special
 * color.
 * <p/>
 * Note that TLcdDataModel instances can define data types that are cyclical. There is no explicit
 * check for this issue in the sample code, because it is not needed here.</p>
 */
public class MainPanel extends samples.lightspeed.decoder.MainPanel {

  DataModelTree fModelTree;

  @Override
  protected void createGUI() {
    super.createGUI();

    fModelTree = new DataModelTree();

    // synchronize the selected layers with the data model tree panel
    getSelectedLayers().addCollectionListener(new ILcdCollectionListener<ILcdLayer>() {
      @Override
      public void collectionChanged(TLcdCollectionEvent<ILcdLayer> aCollectionEvent) {
        Iterator<ILcdLayer> iterator = aCollectionEvent.getSource().iterator();
        fModelTree.setLayer(iterator.hasNext() ? iterator.next() : null);
      }
    });

    addComponentToRightPanel(fModelTree);
  }

  @Override
  protected void addData() throws IOException {
    super.addData();
    LspDataUtil.instance().model(SampleData.COUNTRIES).layer().label("Countries").addToView(getView());
  }

  public static void main(String[] args) {
    startSample(MainPanel.class, "Data model viewer");
  }
}

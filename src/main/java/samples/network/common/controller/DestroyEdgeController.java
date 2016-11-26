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
package samples.network.common.controller;

import java.awt.Rectangle;
import java.awt.event.MouseListener;
import java.util.Enumeration;

import com.luciad.gui.TLcdIconFactory;
import com.luciad.shape.ILcdPoint;
import com.luciad.view.gxy.ILcdGXYLayerSubsetList;
import com.luciad.view.gxy.ILcdGXYView;
import com.luciad.view.gxy.controller.TLcdGXYSelectController2;
import com.luciad.view.gxy.controller.TLcdGXYSelectControllerModel2;

import samples.network.common.graph.GraphManager;

/**
 * A controller for destroying edges.
 */
public class DestroyEdgeController extends TLcdGXYSelectController2 implements MouseListener {

  private GraphManager fGraphManager;

  public DestroyEdgeController(GraphManager aGraphManager) {
    setName("Destroy edge");
    setShortDescription("Destroy edge");
    setIcon(TLcdIconFactory.create(TLcdIconFactory.CLOSE_ICON));

    fGraphManager = aGraphManager;

    setSelectControllerModel(new TLcdGXYSelectControllerModel2() {
                               @Override
                               public void applySelection(ILcdGXYView aGXYView, Rectangle aSelectionBounds, int aMouseMode, int aX, int aY, int aSelectByWhatMode, int aSelectHowMode, ILcdGXYLayerSubsetList aSelectionCandidates) {
                                 Enumeration elementsEnum = aSelectionCandidates.elements();
                                 if ((aSelectByWhatMode & SELECT_BY_WHAT_BODIES_ON_CLICK) != 0) {
                                   while (elementsEnum.hasMoreElements()) {
                                     Object element = elementsEnum.nextElement();
                                     if (!(element instanceof ILcdPoint) && fGraphManager.canDestroyEdge(element)) {
                                       fGraphManager.destroyEdge(element);
                                       break;
                                     }
                                   }
                                 }
                               }
                             }

    );
  }

}

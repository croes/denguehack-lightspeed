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
package samples.common.layerControls.actions;

import java.awt.Component;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Map;
import java.util.logging.LogRecord;

import javax.swing.JOptionPane;

import com.luciad.gui.TLcdAWTUtil;
import com.luciad.gui.TLcdIconFactory;
import com.luciad.gui.TLcdResizeableIcon;
import samples.common.LayerPaintExceptionHandler;
import samples.common.NoopStringTranslator;
import samples.common.gui.ErrorDialog;
import samples.common.layerControls.LayerTreeNodeCellRendererWithActions;
import com.luciad.util.ILcdChangeListener;
import com.luciad.util.ILcdStringTranslator;
import com.luciad.util.TLcdChangeEvent;
import com.luciad.view.ILcdLayer;
import com.luciad.view.ILcdTreeLayered;
import com.luciad.view.gxy.asynchronous.ILcdGXYAsynchronousLayerWrapper;

/**
 * Shows a dialog with the most recent layer paint exception that was stored by LayerPaintExceptionHandler.
 * The action could be added to a {@link LayerTreeNodeCellRendererWithActions layer tree cell renderer}.
 *
 * @see LayerPaintExceptionHandler
 */
public class LayerPaintExceptionDialogAction extends AbstractLayerTreeAction {

  private Component fParent;
  private LayerPaintExceptionHandler fHandler;
  private ILcdStringTranslator fStringTranslator;

  public LayerPaintExceptionDialogAction(ILcdTreeLayered aLayered, Component aParent, final LayerPaintExceptionHandler aHandler) {
    this(aLayered, aParent, aHandler, new NoopStringTranslator());
  }

  public LayerPaintExceptionDialogAction(ILcdTreeLayered aLayered, Component aParent, LayerPaintExceptionHandler aHandler, ILcdStringTranslator aStringTranslator) {
    super(aLayered, true, 1, 1);
    fParent = aParent;
    fHandler = aHandler;
    fStringTranslator = aStringTranslator;
    setIcon(new TLcdResizeableIcon(TLcdIconFactory.create(TLcdIconFactory.ERROR_ICON), 12, 12));
    setAutoHide(true);
    aHandler.addChangeListener(new ILcdChangeListener() {
      @Override
      public void stateChanged(TLcdChangeEvent aChangeEvent) {
        updateState();
      }
    });
    updateState();
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    Map<ILcdLayer, LogRecord> exceptions = fHandler.getExceptions();

    ArrayList<ILcdLayer> filteredLayers = getFilteredLayers();
    ILcdLayer layer = filteredLayers.get(0);
    if (exceptions.containsKey(layer)) {
      LogRecord logRecord = exceptions.get(layer);
      ErrorDialog dialog = new ErrorDialog(fStringTranslator);
      dialog.setLogMessage(logRecord);
      Window parentWindow = TLcdAWTUtil.findParentWindow(fParent);

      int result = JOptionPane.showOptionDialog(parentWindow, dialog,
                                                   "An error occurred while rendering the layer",
                                                JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE, null,
                                                   new String[]{"OK", "Clear"}, "OK");
      // clear
      if (result == JOptionPane.NO_OPTION) {
        exceptions.remove(layer);
        fHandler.fireChangeEvent();
      }
    }
  }

  @Override
  protected boolean shouldBeEnabled() {
    ArrayList<ILcdLayer> filteredLayers = getFilteredLayers();
    Map<ILcdLayer, LogRecord> exceptions = fHandler.getExceptions();
    if (filteredLayers.size() == 1) {
      ILcdLayer layer = filteredLayers.get(0);
      if (exceptions.containsKey(layer)) {
        return true;
      }
      if (layer instanceof ILcdGXYAsynchronousLayerWrapper) {
        layer = ((ILcdGXYAsynchronousLayerWrapper) layer).getGXYLayer();
        if (exceptions.containsKey(layer)) {
          return true;
        }
      }
    }
    return false;
  }

}

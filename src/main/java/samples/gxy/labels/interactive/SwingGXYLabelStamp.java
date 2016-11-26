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
package samples.gxy.labels.interactive;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.CellRendererPane;
import javax.swing.JComponent;

import com.luciad.util.TLcdNoBoundsException;
import com.luciad.view.gxy.ALcdGXYLabelStamp;
import com.luciad.view.gxy.ILcdGXYContext;

/**
 * This ALcdGXYLabelStamp implementation renders a Swing component to paint the label.
 *
 * Note that when extending an ALcdGXYLabelStamp, there is absolutely no need to use Swing.
 * One could for example implement the paint method by calling aGraphics.drawString directly,
 * which would provide faster rendering compared to Swing. Using Swing on the other hand
 * offers a very easy to use mechanism for laying out and rendering more complex labels.
 */
abstract class SwingGXYLabelStamp extends ALcdGXYLabelStamp {

  private final CellRendererPane fCellRendererPane = new CellRendererPane();

  public void dimensionSFCT(Graphics aGraphics, Object aObject, int aLabelIndex, int aSubLabelIndex, int aMode, ILcdGXYContext aContext, Dimension aDimensionSFCT) throws TLcdNoBoundsException {
    Component component = retrieveLabelComponent(aObject, aLabelIndex, aSubLabelIndex, aContext);
    aDimensionSFCT.setSize(component.getPreferredSize());
  }

  public void paint(Graphics aGraphics,
                    Object aObject, int aLabelIndex, int aSubLabelIndex,
                    int aMode, ILcdGXYContext aContext,
                    int x, int y, double aRotation) {
    Component component = retrieveLabelComponent(aObject, aLabelIndex, aSubLabelIndex, aContext);
    Dimension dim = component.getPreferredSize();

    // None of the child components should use double buffering, as the components should be painted
    // directly as a label.
    disableDoubleBufferingOnHierarchy(component);

    // Add the cell renderer pane to the Swing hierarchy
    ((Container) aContext.getGXYView()).add(fCellRendererPane);

    // Use the cell renderer pane to actually paint the component as a label
    fCellRendererPane.paintComponent(aGraphics, component, null, 0, 0, dim.width, dim.height, true);

    // Since the cell renderer pane keeps a reference to component, remove
    // the cell renderer pane to avoid possible memory leaks
    ((Container) aContext.getGXYView()).remove(fCellRendererPane);
  }

  /**
   * This method retrieves the label component for the given label.
   *
   * @param aObject        the label Object.
   * @param aLabelIndex    the label index.
   * @param aSubLabelIndex the sublabel index.
   * @param aContext       the gxy context.
   *
   * @return a Swing label component for the given label.
   */
  protected abstract Component retrieveLabelComponent(Object aObject, int aLabelIndex, int aSubLabelIndex, ILcdGXYContext aContext);

  /**
   * Disables double buffering on the specified component and all its sub children.
   *
   * @param component The component for which to disable the double buffering.
   */
  private void disableDoubleBufferingOnHierarchy(Component component) {
    if (component instanceof JComponent) {
      JComponent j_component = (JComponent) component;
      j_component.setDoubleBuffered(false);
    }
    if (component instanceof Container) {
      Container container = (Container) component;
      for (int i = 0; i < container.getComponentCount(); i++) {
        disableDoubleBufferingOnHierarchy(container.getComponent(i));
      }
    }
  }
}

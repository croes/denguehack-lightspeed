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
package samples.common.dimensionalfilter.ui;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JToolTip;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import samples.common.HaloLabel;
import samples.common.NoopStringTranslator;
import samples.common.dimensionalfilter.model.DimensionalFilterGroup;
import samples.common.dimensionalfilter.model.DimensionalFilterManager;
import com.luciad.util.ILcdStringTranslator;

/**
 * A panel with slider and max/min labels to let user make filtering on vertical and time dimensions
 * This panel can have vertical or horizontal orientation. Depending on the orientation
 * some behaviour can change like way of showing tooltip or parent of the panel.
 * @since 2015.0
 */
class DimensionalFilterSliderPanel extends JPanel {

  private JSlider fSlider;
  private HaloLabel fMaxLabel;
  private HaloLabel fMinLabel;
  private JToolTip fToolTip;
  private Timer fToolTipTimer;
  private DimensionalFilterManager fDimensionalFilterManager;
  private DimensionalFilterBoundedRangeModel fSliderModel;
  private ILcdStringTranslator fStringTranslator;

  private static final int sMIN_UPDATE_PERIOD = 50;

  public DimensionalFilterSliderPanel(int aOrientation, DimensionalFilterBoundedRangeModel aSliderModel, DimensionalFilterManager aDimensionalFilterManager) {
    setOpaque(false);
    setLayout(new GridBagLayout());
    fSlider = new JSlider(aSliderModel) {
      @Override
      public Dimension getPreferredSize() {
        if (getOrientation() == JSlider.VERTICAL) {
          return super.getPreferredSize();
        } else {
          return new Dimension(250, (int) super.getPreferredSize().getHeight());
        }
      }
    };
    fSliderModel = aSliderModel;
    fSlider.setOpaque(false);
    fSlider.setPaintTicks(true);
    fSlider.putClientProperty("JSlider.isFilled", Boolean.FALSE);
    fMaxLabel = new HaloLabel();
    Font labelFont = new Font(Font.SANS_SERIF, Font.BOLD, 11);
    fMaxLabel.setFont(labelFont);
    fMinLabel = new HaloLabel();
    fMinLabel.setFont(labelFont);
    fToolTip = new JToolTip();
    fToolTip.setVisible(false);
    createToolTipTimer();
    fSlider.setOrientation(aOrientation);
    fDimensionalFilterManager = aDimensionalFilterManager;
    fStringTranslator = new NoopStringTranslator();

    UIVerticalSliderListener sliderListener;

    if (aOrientation == JSlider.HORIZONTAL) {
      //create a horizontal layout
      createHorizontal();
      //ui listener is is horizontal imp.
      sliderListener = new UIHorizontalSliderListener();
    } else if (aOrientation == JSlider.VERTICAL) {
      createVertical(null);
      //ui listener is is vertical imp.
      sliderListener = new UIVerticalSliderListener();
    } else {
      throw new IllegalArgumentException(aOrientation + " is not an orientation value");
    }

    //set listeners
    fSlider.addMouseMotionListener(sliderListener);
    fSlider.addMouseListener(sliderListener);
    fSlider.addChangeListener(sliderListener);

  }

  /**
   * Creates a vertical layout
   */
  private void createVertical(Insets aInsets) {
    removeAll();
    GridBagConstraints constraints = new GridBagConstraints();
    constraints.gridx = 0;
    constraints.weightx = 1;
    constraints.gridy = GridBagConstraints.RELATIVE;
    constraints.fill = GridBagConstraints.NONE;
    constraints.anchor = GridBagConstraints.WEST;
    if (aInsets != null) {
      constraints.insets = aInsets;
    }
    add(fMaxLabel, constraints);
    constraints.weighty = 1;
    constraints.anchor = GridBagConstraints.WEST;
    add(fSlider, constraints);
    constraints.weighty = 0;
    constraints.anchor = GridBagConstraints.WEST;
    add(fMinLabel, constraints);
  }

  /**
   * Creates a horizontal layout
   */
  private void createHorizontal() {
    removeAll();
    GridBagConstraints constraints = new GridBagConstraints();
    constraints.gridy = 0;
    constraints.gridx = 0;
    constraints.anchor = GridBagConstraints.CENTER;
    constraints.fill = GridBagConstraints.HORIZONTAL;
    constraints.gridwidth = 2;
    add(fSlider, constraints);
    constraints.gridy = 1;
    constraints.gridx = 0;
    constraints.anchor = GridBagConstraints.WEST;
    constraints.fill = GridBagConstraints.NONE;
    constraints.gridwidth = 1;
    add(fMinLabel, constraints);
    constraints.gridx = 1;
    constraints.anchor = GridBagConstraints.EAST;
    add(fMaxLabel, constraints);
  }

  /**
   * Creates and sets the timer to hide the tooltip
   */
  private void createToolTipTimer() {
    //will display the panel for some time.
    fToolTipTimer = new Timer(1000, new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        fToolTip.setVisible(false);
      }
    });
    fToolTipTimer.setRepeats(false);
  }

  public JSlider getSlider() {
    return fSlider;
  }

  public HaloLabel getMaxLabel() {
    return fMaxLabel;
  }

  public HaloLabel getMinLabel() {
    return fMinLabel;
  }

  public JToolTip getToolTip() {
    return fToolTip;
  }

  public void setToolTip(JToolTip aToolTip) {
    fToolTip = aToolTip;
  }

  public void setStringTranslator(ILcdStringTranslator aStringTranslator) {
    fStringTranslator = aStringTranslator;
  }

  public void setSliderInsets(Insets aSliderInsets) {
    createVertical(aSliderInsets);
    invalidate();
  }

  /**
   * Calculates tooltip position in the screen according to UI interaction,
   * Sets tooltip text and displays it for vertical oriented slider
   */
  private class UIVerticalSliderListener implements MouseMotionListener, MouseListener, ChangeListener {
    protected boolean fMouseDown = false;
    protected long fPrevTime = 0;

    protected boolean canProcessEvent() {
      long now = System.currentTimeMillis();
      if (now - fPrevTime > sMIN_UPDATE_PERIOD) {
        fPrevTime = now;
        return true;
      }
      return false;
    }

    /**
     * When updating the value of slider, display a tooltip next to the slider
     * which shows information about the filter
     * @param e change event
     */
    @Override
    public void mouseDragged(MouseEvent e) {
      if (!canProcessEvent() || !refreshToolTipContent()) {
        return;
      }

      //keep tooltip in slider limits
      //yCoord will be updated with mouse
      int yCoord = e.getY();
      if (yCoord > fSlider.getHeight()) {
        yCoord = fSlider.getHeight();
      } else if (yCoord < 0) {
        yCoord = 0;
      }

      //xCoord is always next to slider
      int xCoord = getParent().getX() + getX() + fSlider.getX() + fSlider.getWidth();
      //project y coord for application pane
      yCoord = getParent().getY() + getY() + fSlider.getY() + yCoord;

      displayToolTip(yCoord, xCoord);
    }

    @Override
    public void mouseMoved(MouseEvent e) {

    }

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {
      fMouseDown = true;
    }

    @Override
    public void mouseReleased(MouseEvent e) {
      fMouseDown = false;
      repositionToolTip();
    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void stateChanged(ChangeEvent e) {
      if (getParent() != null) {
        repositionToolTip();
      }
    }

    protected void repositionToolTip() {
      if (fMouseDown || !refreshToolTipContent()) {
        return;
      }

      double diff = fSlider.getMaximum() - fSlider.getMinimum();
      double yOffset = fSlider.getHeight() - fSlider.getHeight() * fSlider.getValue() / diff;

      if (fSlider.getInverted()) {
        yOffset = fSlider.getHeight() - yOffset;
      }

      int yCoord = getParent().getY() + (int) (getY() + fSlider.getY() + yOffset);
      int xCoord = getParent().getX() + fSlider.getX() + fSlider.getWidth();
      displayToolTip(yCoord, xCoord);
      fToolTipTimer.restart();
    }

    protected void displayToolTip(final int aYCoord, final int aXCoord) {
      SwingUtilities.invokeLater(new Runnable() {
        @Override
        public void run() {
          fToolTip.setBounds(aXCoord, aYCoord, (int) fToolTip.getPreferredSize().getWidth(), (int) fToolTip.getPreferredSize().getHeight());
          fToolTip.setVisible(true);
        }
      });

    }

    protected boolean refreshToolTipContent() {
      List<String> selectedModelNames = fDimensionalFilterManager.getFilterTargetNames(fSliderModel.getCurrentDimensionalFilterGroup());
      //if no selection on the filter just return
      if (selectedModelNames.size() == 0) {
        return false;
      }
      fToolTipTimer.stop();
      String htmlText = createFilterInfo(selectedModelNames);
      if (null == htmlText) {
        return false;
      }
      fToolTip.setTipText(htmlText);
      return true;
    }

    private String createFilterInfo(List<String> aSelectedModelNames) {
      DimensionalFilterGroup currentDimensionalFilterGroup = fSliderModel.getCurrentDimensionalFilterGroup();
      if (fDimensionalFilterManager.getMinValueInt(currentDimensionalFilterGroup) > fSliderModel.getValue() ||
          fDimensionalFilterManager.getMaxValueInt(currentDimensionalFilterGroup) < fSliderModel.getValue()) {
        return null;
      }

      Object currentValue = fDimensionalFilterManager.getValue(currentDimensionalFilterGroup, fSliderModel.getValue());

      //create info text
      StringBuilder htmlText = new StringBuilder("<html><h4>");
      htmlText.append(DimensionalFilterManagerUI.convertToString(currentValue));
      htmlText.append("</h4>");
      htmlText.append(fStringTranslator.translate("Applies to"));

      for (String name : aSelectedModelNames) {
        htmlText.append("<br>- ");
        htmlText.append(name);
      }

      return htmlText.toString();
    }
  }

  /**
   * Calculates tooltip position in the screen according to UI interaction,
   * Sets tooltip text and displays it for horizontal oriented slider
   */
  private class UIHorizontalSliderListener extends UIVerticalSliderListener {
    @Override
    public void mouseDragged(MouseEvent e) {
      if (!canProcessEvent() || !refreshToolTipContent()) {
        return;
      }

      //keep tooltip in slider limits
      //xCoord will be updated with mouse
      int xCoord = e.getX();
      if (xCoord > fSlider.getWidth()) {
        xCoord = fSlider.getWidth();
      } else if (xCoord < 0) {
        xCoord = 0;
      }
      //project x coord for application pane
      xCoord = getParent().getX() + getX() + fSlider.getX() + xCoord;

      int yCoord = getParent().getY() + getY() + fSlider.getY() + fSlider.getHeight();

      displayToolTip(yCoord, xCoord);
    }

    @Override
    protected void repositionToolTip() {
      if (fMouseDown || !refreshToolTipContent()) {
        return;
      }
      double diff = fSlider.getMaximum() - fSlider.getMinimum();
      double xOffset = fSlider.getWidth() * fSlider.getValue() / diff - fSlider.getWidth();
      int xCoord = fSlider.getWidth() + (int) (getX() + fSlider.getX() + xOffset);
      int yCoord = getParent().getY() + getY() + fSlider.getY() + fSlider.getHeight();
      displayToolTip(yCoord, xCoord);
      fToolTipTimer.restart();
    }
  }

}

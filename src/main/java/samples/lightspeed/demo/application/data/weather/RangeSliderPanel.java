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
package samples.lightspeed.demo.application.data.weather;

import java.awt.BorderLayout;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.AbstractButton;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JToolBar;

import com.luciad.gui.swing.TLcdRangeSlider;
import samples.common.HaloLabel;
import samples.common.OptionsPanelBuilder;

/**
 * Panel that contains a title, a range slider, labels to display the values of currently set min and max values of that slider
 * and a series of buttons to change the unit of measure of the slider
 * @param <T> type of the items for which there are buttons made.
 * @param <U> type of the TLcdRangeSlider used to select a range.
 */
class RangeSliderPanel<T, U extends TLcdRangeSlider> extends JPanel {

  private static final int RANGE_LABEL_FONT_SIZE = 13;
  private static final int TITLE_FONT_SIZE = 15;

  private final U fRangeSlider;

  private HaloLabel fFromLabel;
  private HaloLabel fToLabel;
  private Map<AbstractButton, T> fUnitButtons = new LinkedHashMap<>();
  private T fSelectedItem;

  RangeSliderPanel(String aTitle, U aRangeSlider, T... aItems) {
    super(new BorderLayout(0, 10));
    fRangeSlider = aRangeSlider;
    add(new HaloLabel(aTitle, TITLE_FONT_SIZE, false), BorderLayout.NORTH);
    add(createRangeSettings(aItems), BorderLayout.CENTER);
    add(aRangeSlider, BorderLayout.SOUTH);
    setOpaque(false);

    setSize(getLayout().preferredLayoutSize(this));
  }

  private JToolBar createRangeSettings(T[] aItems) {
    JToolBar toolBar = new JToolBar();
    toolBar.setFloatable(false);

    fFromLabel = new HaloLabel("", RANGE_LABEL_FONT_SIZE, false);
    HaloLabel to = new HaloLabel(" to ", RANGE_LABEL_FONT_SIZE, false);
    fToLabel = new HaloLabel("", RANGE_LABEL_FONT_SIZE, false);
    toolBar.add(fFromLabel);
    toolBar.add(to);
    toolBar.add(fToLabel);
    toolBar.add(Box.createHorizontalGlue());

    JToolBar unitButtonsToolbar = new JToolBar();
    unitButtonsToolbar.setFloatable(false);
    unitButtonsToolbar.setBorderPainted(false);
    ButtonGroup buttonGroup = new ButtonGroup();
    for (T item : aItems) {
      AbstractButton button = OptionsPanelBuilder.createUnderlinedButton(item.toString());
      fUnitButtons.put(button, item);
      unitButtonsToolbar.add(button);
      buttonGroup.add(button);
    }
    fSelectedItem = aItems[0];
    getFirstButton().setSelected(true);
    toolBar.add(unitButtonsToolbar);
    toolBar.setBorderPainted(false);

    toolBar.setOpaque(false);
    return toolBar;
  }

  public void setFrom(String text) {
    fFromLabel.setText(text);
  }

  public void setTo(String text) {
    fToLabel.setText(text);
  }

  public Collection<AbstractButton> getUnitButtons() {
    return fUnitButtons.keySet();
  }

  public void setButtonPressed(AbstractButton aButton) {
    fSelectedItem = getUnitOfMeasure(aButton);
  }

  private T getUnitOfMeasure(AbstractButton aButton) {
    return fUnitButtons.get(aButton);
  }

  public T getSelectedItem() {
    return fSelectedItem;
  }

  public U getRangeSlider() {
    return fRangeSlider;
  }

  public void reset() {
    getRangeSlider().reset();
    getFirstButton().doClick();
  }

  private AbstractButton getFirstButton() {
    return fUnitButtons.entrySet().iterator().next().getKey();
  }

}

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
package samples.lightspeed.demo.framework.gui;

import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.Field;
import java.text.NumberFormat;
import java.util.Hashtable;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.SliderUI;

import com.luciad.view.lightspeed.ILspView;

/**
 * A slider that changes the Z scale on a 3D view between 1 and 5.
 */
public class ZSlider extends JPanel {

  private static double FACTOR = 100;
  private final Dimension fSize;

  private JSlider fSlider;
  private ILspView fView;

  /**
   * Construct a new ZSlider that controls the altitude exaggeration of
   * {@code aView}.
   *
   * @param aOrientation The orientation of the slider.
   * @param aView The view who's altitude exaggeration will be changed.
   */
  public ZSlider(int aOrientation, ILspView aView) {
    this(aOrientation, aView, 1, 5, 0.1, 1);
  }

  private ZSlider(int aOrientation,
                  ILspView aView,
                  double aMinimum,
                  double aMaximum,
                  double aMinorTickSpacing,
                  double aMayorTickSpacing) {
    fSlider = new JSlider(aOrientation, toSliderValue(aMinimum), toSliderValue(aMaximum), toSliderValue(aMinimum));

    // Small UI hack to avoid painting the (wrong)
    // value over the thumb of the slider
    try {
      Class<? extends SliderUI> ui = fSlider.getUI().getClass();
      Field field = ui.getDeclaredField("paintValue");
      field.setAccessible(true);
      field.set(fSlider.getUI(), false);
    } catch (Exception ignored) {
    }

    NumberFormat format = NumberFormat.getNumberInstance();
    format.setMinimumFractionDigits(aMayorTickSpacing == ((int) aMayorTickSpacing) ? 0 : 1);
    format.setMaximumFractionDigits(aMayorTickSpacing == ((int) aMayorTickSpacing) ? 0 : 1);

    fSlider.setMajorTickSpacing(toSliderValue(aMayorTickSpacing));
    fSlider.setMinorTickSpacing(toSliderValue(aMinorTickSpacing));
    fSlider.setPaintTicks(false);
    fSlider.setSnapToTicks(false);

    Hashtable<Integer, JLabel> label_table = new Hashtable<Integer, JLabel>();
    label_table.put(fSlider.getMinimum(), new JLabel(format.format(aMinimum)));
    for (int i = 0; i < fSlider.getMaximum(); i += fSlider.getMajorTickSpacing()) {
      label_table.put(i, new JLabel(format.format(fromSliderValue(i))));
    }
    label_table.put(fSlider.getMaximum(), new JLabel(format.format(aMaximum)));
    fSlider.setLabelTable(label_table);
    fSlider.setPaintLabels(false);

    fView = aView;
    fView.addPropertyChangeListener(new PropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName() == null || evt.getPropertyName().equals("altitudeExaggerationFactor")) {
          fSlider.setValue(toSliderValue(fView.getAltitudeExaggerationFactor()));
        }
      }
    });
    fSlider.setValue(toSliderValue(fView.getAltitudeExaggerationFactor()));

    fSlider.addChangeListener(new ZScaleSetter());

    if (aOrientation == JSlider.VERTICAL) {
      fSize = new Dimension(50, 210);
    } else {
      fSize = new Dimension(210, 50);
    }
    fSlider.setOpaque(false);
    setOpaque(false);
    add(fSlider);
  }

  @Override
  public Dimension getPreferredSize() {
    return fSize;
  }

  private static int toSliderValue(double aValue) {
    return (int) (aValue * FACTOR);
  }

  private static double fromSliderValue(int aValue) {
    return aValue / FACTOR;
  }

  private class ZScaleSetter implements ChangeListener {
    public void stateChanged(ChangeEvent e) {
      fView.setAltitudeExaggerationFactor(fromSliderValue(fSlider.getValue()));
    }
  }
}

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
package samples.hana.lightspeed.ui;

import static java.lang.Math.max;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import javax.swing.BorderFactory;
import javax.swing.DefaultBoundedRangeModel;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import samples.hana.lightspeed.common.ColorPalette;
import samples.hana.lightspeed.common.FontStyle;

/**
 * UI element for selecting the time for a storm.
 */
public class StormTimeComponent extends JPanel {

  public enum Mode {
    actual(0), predicted_5(5), predicted_10(10), predicted_20(20), predicted_30(30), predicted_50(50);

    private int fProbability;

    Mode(int aProbability) {
      fProbability = aProbability;
    }

    @Override
    public String toString() {
      if (this == actual) {
        return name();
      }
      return "predicted (" + fProbability + "% probability)";
    }
  }

  private final JSlider fTimeSlider;
  private final JLabel fDateLabel = FontStyle.createHaloLabel("", FontStyle.H2);

  @SuppressWarnings("unchecked")
  public StormTimeComponent() {
    fTimeSlider = new JSlider() {
      @Override
      public Dimension getPreferredSize() {
        Dimension size = super.getPreferredSize();
        size.width = 600;
        return size;
      }

      @Override
      public void setValue(int n) {
        super.setValue(n);
        fireStateChanged();
      }
    };
    SwingUtilities.updateComponentTreeUI(fTimeSlider);

    fTimeSlider.addKeyListener(new KeyListener() {
      @Override
      public void keyTyped(KeyEvent e) {
      }

      @Override
      public void keyPressed(KeyEvent e) {
      }

      @Override
      public void keyReleased(KeyEvent e) {
        setDate(getCurrentTime());
        for (PropertyChangeListener listener : getPropertyChangeListeners("time")) {
          listener.propertyChange(new PropertyChangeEvent(StormTimeComponent.this, "time", null, getCurrentTime()));
        }
      }
    });

    fTimeSlider.addMouseListener(new MouseListener() {
      @Override
      public void mouseClicked(MouseEvent e) {
      }

      @Override
      public void mousePressed(MouseEvent e) {
      }

      @Override
      public void mouseReleased(MouseEvent e) {
        setDate(getCurrentTime());
      }

      @Override
      public void mouseEntered(MouseEvent e) {
      }

      @Override
      public void mouseExited(MouseEvent e) {
      }
    });
    fTimeSlider.setValue(0);
    fTimeSlider.addChangeListener(new ChangeListener() {
      @Override
      public void stateChanged(ChangeEvent e) {

        setDate(getCurrentTime());
        for (PropertyChangeListener listener : getPropertyChangeListeners("time")) {
          listener.propertyChange(new PropertyChangeEvent(StormTimeComponent.this, "time", null, getCurrentTime()));
        }
      }
    });

    JPanel text = new JPanel(new FlowLayout());
    text.setOpaque(false);

    text.add(FontStyle.createHaloLabel("Time:", FontStyle.H2));
    text.add(fDateLabel);

    setLayout(new BorderLayout());
    add(text, BorderLayout.NORTH);
    add(fTimeSlider, BorderLayout.CENTER);

    setOpaque(true);
    setBackground(ColorPalette.ui_background);
    setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
  }

  private void setDate(long aDate) {
    DateFormat format = new SimpleDateFormat("MM/dd/yyyy - hh:mm a");
    fDateLabel.setText(format.format(new java.util.Date(aDate)));
  }

  public void setTimeRange(long aMin, long aMax) {
    long value = getCurrentTime();  // keep set time if possible

    if (value < aMin || value > aMax) {
      value = 1351405800000L; // if not, choose 2012.10.28 07:30, good startup position for Sandy
    }

    if (value < aMin || value > aMax) {
      value = aMin + ((aMax - aMin) / 2); // if not, choose middle of time range for the rest
    }

    fTimeSlider.setModel(new DefaultBoundedRangeModel((int) (value / 1000L), 1, (int) (aMin / 1000L), max(1, (int) (aMax / 1000L))));
    fTimeSlider.setValue((int) (value / 1000L));
  }

  public void setTime(long aTime) {
    fTimeSlider.setValue((int) (aTime / 1000L));
  }

  public long getCurrentTime() {
    return 1000L * fTimeSlider.getValue();
  }
}

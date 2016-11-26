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
package samples.lightspeed.common;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import samples.common.SwingUtil;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.camera.ALspViewXYZWorldTransformation;
import com.luciad.view.lightspeed.camera.TLspViewXYZWorldTransformation2D;
import com.luciad.view.lightspeed.camera.TLspViewXYZWorldTransformation3D;

/**
 * Toolbar that lets the user toggle between different world references and projections.
 */
public class ProjectionControls {

  public static JToolBar createProjectionControls(ILspView aView) {
    final ProjectionComboBox projectionComboBox = new ProjectionComboBox(aView);

    JToolBar bar = new JToolBar(JToolBar.HORIZONTAL);
    SwingUtil.makeFlat(bar);

    final JToggleButton mode3DButton = new JToggleButton("3D");
    final JToggleButton mode2DButton = new JToggleButton("2D");
    if (aView.getViewXYZWorldTransformation() instanceof TLspViewXYZWorldTransformation3D) {
      mode3DButton.setSelected(true);
      mode2DButton.setSelected(false);
    } else {
      mode2DButton.setSelected(true);
      mode3DButton.setSelected(false);
    }
    mode2DButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        if (mode2DButton.isSelected()) {
          projectionComboBox.setMode(Mode.MODE_2D);
        }
      }
    });

    mode3DButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        if (mode3DButton.isSelected()) {
          projectionComboBox.setMode(Mode.MODE_3D);
        }
      }
    });

    bar.add(mode2DButton);
    bar.add(mode3DButton);
    bar.add(Box.createRigidArea(new Dimension(10, 10)));
    bar.add(projectionComboBox);

    ButtonGroup bg = new ButtonGroup();
    bg.add(mode2DButton);
    bg.add(mode3DButton);

    projectionComboBox.setToolTipText("Switch between 2D and 3D views and various map projections");
    return bar;
  }

  /**
   * A mode that determines which world references are available
   * in the item list of the projection combobox.
   */
  private static enum Mode {
    MODE_2D,
    MODE_3D
  }

  private static class ProjectionComboBox extends JPanel {

    private final ProjectionModel fModel;
    private final ProjectionSupport fProjectionSupport;
    private final JComboBox fComboBox = new JComboBox();
    private final JSpinner fUTMSpinner = new JSpinner();

    public ProjectionComboBox(ILspView aView) {
      super();
      setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
      fProjectionSupport = new ProjectionSupport(aView);
      fModel = new ProjectionModel(aView, fProjectionSupport);
      fComboBox.setModel(fModel);
      fComboBox.setMaximumRowCount(25); // avoid scroll bar in combo box
      fUTMSpinner.setToolTipText("Current UTM zone");
      fUTMSpinner.setModel(new SpinnerNumberModel(30, 1, 60, 1));
      fUTMSpinner.addChangeListener(new ChangeListener() {
        @Override
        public void stateChanged(ChangeEvent e) {
          int zone = (Integer) fUTMSpinner.getModel().getValue();
          if (zone > 0) {
            fProjectionSupport.setUTMZone(zone);
          }
        }
      });
      add(fComboBox);
      add(fUTMSpinner);
      fUTMSpinner.setVisible(false);

      fProjectionSupport.addPropertyChangeListener(new PropertyChangeListener() {
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
          fUTMSpinner.setVisible(fProjectionSupport.getUTMZone() > 0);
          fUTMSpinner.setValue(fProjectionSupport.getUTMZone());
        }
      });
    }

    public void setMode(Mode aMode) {
      fModel.setMode(aMode);
    }

    @Override
    public Dimension getMaximumSize() {
      return getPreferredSize();
    }

  }

  /**
   * Utility class that determines which items are
   * present in the ProjectionCombobox' list.
   */
  private static final class ProjectionModel extends DefaultComboBoxModel {

    private Object fOtherModeObject = null;
    private String[] fItems2D;
    private String[] fItems3D;
    private Mode fMode;
    private ProjectionSupport fProjectionSupport;

    private ProjectionModel(ILspView aView, ProjectionSupport aProjectionSupport) {
      fProjectionSupport = aProjectionSupport;
      fItems2D = ProjectionSupport.get2DWorldRefNames();
      fItems3D = ProjectionSupport.get3DWorldRefNames();
      setMode(aView);
      fProjectionSupport.addPropertyChangeListener(new PropertyChangeListener() {
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
          if ("projection".equals(evt.getPropertyName())) {
            setSelectedItem(fProjectionSupport.getProjection());
          }
        }
      });
    }

    private void setMode(ILspView aView) {
      ALspViewXYZWorldTransformation w2v = aView.getViewXYZWorldTransformation();
      if (w2v instanceof TLspViewXYZWorldTransformation3D) {
        fMode = Mode.MODE_3D;
        super.setSelectedItem(fProjectionSupport.toString(aView.getXYZWorldReference(), true));
      } else if (w2v instanceof TLspViewXYZWorldTransformation2D) {
        fMode = Mode.MODE_2D;
        super.setSelectedItem(fProjectionSupport.toString(aView.getXYZWorldReference(), false));
      } else {
        throw new IllegalArgumentException("Cannot determine Projection mode based on given view: " + aView);
      }
    }

    private void setMode(Mode aMode) {
      if (fMode == aMode) {
        return;
      }

      fMode = aMode;

      Object otherModeObject = getSelectedItem();

      switch (fMode) {
      case MODE_2D:
        setSelectedItem(fOtherModeObject == null ? fItems2D[0] : fOtherModeObject);
        break;
      case MODE_3D:
        setSelectedItem(fOtherModeObject == null ? fItems3D[0] : fOtherModeObject);
        break;
      }

      fOtherModeObject = otherModeObject;
      fireContentsChanged(this, 0, getSize());
    }

    public Mode getMode() {
      return fMode;
    }

    @Override
    public int getSize() {
      switch (fMode) {
      case MODE_2D:
        return fItems2D.length;
      case MODE_3D:
        return fItems3D.length;
      default:
        return -1;
      }
    }

    @Override
    public Object getElementAt(int index) {
      switch (fMode) {
      case MODE_2D:
        return fItems2D[index];
      case MODE_3D:
        return fItems3D[index];
      default:
        return -1;
      }
    }

    @Override
    public void setSelectedItem(Object anObject) {
      fProjectionSupport.setProjection(String.valueOf(anObject));
      super.setSelectedItem(anObject);
    }
  }
}

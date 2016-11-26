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
package samples.lightspeed.labels.interactive;

import static javax.swing.BorderFactory.*;

import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.luciad.gui.ILcdIcon;
import com.luciad.gui.TLcdIconFactory;
import com.luciad.gui.swing.TLcdSWIcon;
import com.luciad.input.touch.TLcdTouchEvent;
import com.luciad.model.ILcdModel;
import com.luciad.util.concurrent.TLcdLockUtil;
import com.luciad.view.lightspeed.TLspContext;

import samples.gxy.labels.interactive.PinchRecognizer;

/**
 * Swing component that can be used to create an interactive label stamp.
 */
public class InteractiveSwingLabelComponent extends JPanel implements LabelComponentProvider {

  private static final int MINIMAL_NUMBER_OF_COLUMNS = 10;
  private static final int MAXIMAL_NUMBER_OF_COLUMNS = 30;

  private static final Color BACKGROUND_COLOR = new Color(153, 180, 209);
  private static final Color FOREGROUND_COLOR = new Color(0, 0, 0);

  private final JLabel fTitle = new JLabel();
  private final JTextField fCommentsField = new JTextField(15) {
    @Override
    protected void processEvent(AWTEvent aEvent) {
      // Override the method to accept TLcdTouchEvent instances
      if (aEvent instanceof TLcdTouchEvent) {
        handleTouchEvent((TLcdTouchEvent) aEvent);
      } else {
        super.processEvent(aEvent);
      }
    }
  };

  private final PinchRecognizer fPinchRecognizer = new PinchRecognizer();
  private int fOriginalNumberOfColumns;

  private final Map<Object, String> fCitiesCommentMap;
  private Object fCurrentObject;
  private ILcdModel fCurrentModel;

  // Listeners
  private final List<MouseListener> fCrossIconMouseListeners = new CopyOnWriteArrayList<>();
  private final List<MouseListener> fTickIconMouseListeners = new CopyOnWriteArrayList<>();

  public InteractiveSwingLabelComponent(Map<Object, String> aCitiesCommentMap) {
    fCitiesCommentMap = aCitiesCommentMap;
    setLayout(new GridBagLayout());
    initUI();
    fOriginalNumberOfColumns = fCommentsField.getColumns();
  }

  @Override
  public JComponent getComponent(Object aObject, Object aSubLabelID, TLspContext aContext) {
    setObject(aObject, aContext.getModel());
    return this;
  }

  private void setObject(Object aObject, ILcdModel aModel) {
    fCurrentObject = aObject;
    fCurrentModel = aModel;
    String comment = fCitiesCommentMap.get(fCurrentObject);
    fTitle.setText(fCurrentObject.toString());
    fCommentsField.setText(comment);
  }

  /**
   * @return {@code true} if something changed, {@code false} otherwise
   */
  public boolean commitCommentChanges() {
    String comment = fCommentsField.getText();
    if (comment != null && comment.trim().equals("")) {
      comment = null;
    }
    String oldComment = putComment(comment);
    boolean changed = !Objects.equals(comment, oldComment);
    if (changed && fCurrentModel != null) {
      TLcdLockUtil.writeLock(fCurrentModel);
      try {
        fCurrentModel.elementChanged(fCurrentObject, ILcdModel.FIRE_LATER);
      } finally {
        TLcdLockUtil.writeUnlock(fCurrentModel);
        fCurrentModel.fireCollectedModelChanges();
      }
    }
    return changed;
  }

  private String putComment(String aComment) {
    if (aComment == null) {
      return fCitiesCommentMap.remove(fCurrentObject);
    } else {
      return fCitiesCommentMap.put(fCurrentObject, aComment);
    }
  }

  public void addPressedEnterActionListener(ActionListener aActionListener) {
    fCommentsField.addActionListener(aActionListener);
  }

  public void addCommentsFieldFocusListener(FocusListener aFocusListener) {
    fCommentsField.addFocusListener(aFocusListener);
  }

  public void addCrossIconMouseListener(MouseListener aMouseListener) {
    fCrossIconMouseListeners.add(aMouseListener);
  }

  public void addTickIconMouseListener(MouseListener aMouseListener) {
    fTickIconMouseListeners.add(aMouseListener);
  }

  private void fireCrossIconMouseClickedEvent(MouseEvent aMouseEvent) {
    for (MouseListener listener : fCrossIconMouseListeners) {
      listener.mouseClicked(aMouseEvent);
    }
  }

  private void fireTickIconMouseClickedEvent(MouseEvent aMouseEvent) {
    for (MouseListener listener : fTickIconMouseListeners) {
      listener.mouseClicked(aMouseEvent);
    }
  }

  private void initUI() {
    setBorder(BorderFactory.createLineBorder(Color.black, 1));
    Component title_panel = createTitleComponent();

    GridBagConstraints gbc = new GridBagConstraints(
        0, 0, 1, 1,
        1.0, 0.0,
        GridBagConstraints.CENTER,
        GridBagConstraints.HORIZONTAL,
        new Insets(0, 0, 0, 0),
        0, 0
    );

    add(title_panel, gbc);

    gbc.gridy++;
    gbc.insets = new Insets(2, 2, 2, 2);
    fCommentsField.setBorder(createCompoundBorder(createEmptyBorder(1, 1, 1, 1),
                                                  createCompoundBorder(createEtchedBorder(), createEmptyBorder(1, 0, 1, 1))));
    fCommentsField.setFont(Font.decode("Default-BOLD-12"));
    add(fCommentsField, gbc);
  }

  private Component createTitleComponent() {
    JLabel cross = createIconLabel(TLcdIconFactory.create(TLcdIconFactory.CANCEL_CHANGES_ICON));
    cross.setToolTipText("Cancel");

    JLabel tick = createIconLabel(TLcdIconFactory.create(TLcdIconFactory.APPLY_CHANGES_ICON));
    tick.setToolTipText("Accept");

    // cancel the label interaction when we click the cross
    cross.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        e.getComponent().requestFocusInWindow();
        fireCrossIconMouseClickedEvent(e);
      }
    });

    // accept the label interaction when we click the tick
    tick.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        e.getComponent().requestFocusInWindow();
        fireTickIconMouseClickedEvent(e);
      }
    });

    // configure the title label
    fTitle.setOpaque(true);
    fTitle.setBackground(BACKGROUND_COLOR);
    fTitle.setForeground(FOREGROUND_COLOR);
    fTitle.setHorizontalAlignment(JLabel.CENTER);

    // add all the components with the correct layout
    JPanel title_panel = new MyPanel(new GridBagLayout());
    title_panel.setBackground(BACKGROUND_COLOR);
    title_panel.setOpaque(true);

    GridBagConstraints gbc = new GridBagConstraints();
    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.weightx = 1.0;
    gbc.weighty = 1.0;
    gbc.anchor = GridBagConstraints.WEST;
    gbc.insets = new Insets(0, 5, 0, 0);
    gbc.ipadx = 4;
    gbc.ipady = 0;
    gbc.fill = GridBagConstraints.VERTICAL;
    title_panel.add(fTitle, gbc);

    gbc.gridx++;
    gbc.fill = GridBagConstraints.NONE;
    gbc.weightx = 0.0d;
    gbc.ipadx = 0;
    gbc.insets = new Insets(1, 0, 1, 0);
    title_panel.add(tick, gbc);
    gbc.gridx++;
    gbc.insets = new Insets(1, 0, 1, 1);
    title_panel.add(cross, gbc);

    return title_panel;
  }

  private JLabel createIconLabel(ILcdIcon aIcon) {
    JLabel icon_label = new MyLabel(new TLcdSWIcon(aIcon));
    icon_label.setOpaque(true);
    icon_label.setBackground(BACKGROUND_COLOR);
    icon_label.setCursor(Cursor.getDefaultCursor());
    return icon_label;
  }

  private void handleTouchEvent(TLcdTouchEvent aEvent) {
    // Pass the event to the pinch recognizer
    fPinchRecognizer.handleAWTEvent(aEvent);
    if (fPinchRecognizer.isPinchInProgress()) {
      // If a pinch is in progress, adjust the number of columns on the text field
      double scale = fPinchRecognizer.getTotalPinchFactor();
      int column_count = new Double(fOriginalNumberOfColumns * scale).intValue();
      column_count = Math.min(MAXIMAL_NUMBER_OF_COLUMNS, Math.max(MINIMAL_NUMBER_OF_COLUMNS, column_count));
      fCommentsField.setColumns(column_count);
      fCommentsField.revalidate();
      setPreferredSize(new Dimension(new Double(fCommentsField.getPreferredSize().getWidth()).intValue(),
                                     new Double(getPreferredSize().getHeight()).intValue()));
    } else {
      fOriginalNumberOfColumns = fCommentsField.getColumns();
    }
  }

  /**
   * Subclass of JPanel that paints a visual indication that the title can be dragged.
   */
  private static class MyPanel extends JPanel {
    private static final int OFFSET = 2;
    private static final int PATTERN_WIDTH = 2;
    private static final int PATTERN_HEIGHT = 2;

    private final TexturePaint fPaint;

    public MyPanel(GridBagLayout aLayout) {
      super(aLayout);
      BufferedImage image = new BufferedImage(PATTERN_WIDTH, PATTERN_HEIGHT, BufferedImage.TYPE_INT_ARGB);
      image.setRGB(0, 0, toInt(255, 255, 255, 100));
      fPaint = new TexturePaint(image, new Rectangle(OFFSET, OFFSET, PATTERN_WIDTH, PATTERN_HEIGHT));
    }

    private static int toInt(int aR, int aG, int aB, int aA) {
      return new Color(aR, aG, aB, aA).getRGB();
    }

    @Override
    protected void paintComponent(Graphics g) {
      super.paintComponent(g);
      Graphics2D g2d = (Graphics2D) g;
      g2d.setPaint(fPaint);
      g2d.fillRect(OFFSET, OFFSET, getWidth() - 2 * OFFSET, getHeight() - 2 * OFFSET);
    }
  }

  /**
   * We use this subclass merely for tagging purposes, in order to dispatch the mouse events
   * to these labels when appropriate. See the dispatchMouseEvent method for more details.
   */
  private static class MyLabel extends JLabel {
    public MyLabel(Icon aIcon) {
      super(aIcon);
    }
  }
}

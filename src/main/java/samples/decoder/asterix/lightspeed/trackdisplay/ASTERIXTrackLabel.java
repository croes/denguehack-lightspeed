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
package samples.decoder.asterix.lightspeed.trackdisplay;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.luciad.format.asterix.TLcdASTERIXTrack;
import com.luciad.model.ILcdModel;
import com.luciad.util.concurrent.TLcdLockUtil;
import com.luciad.view.lightspeed.TLspContext;

import samples.lightspeed.labels.interactive.LabelComponentProvider;

/**
 * Asterix track label that shows basic information about the track.
 */
class ASTERIXTrackLabel extends JPanel implements LabelComponentProvider {

  private static final Font LABEL_FONT = new Font("Dialog", Font.BOLD, 12);

  private final boolean fShowExtraInfo; // Display more content
  private final boolean fInteractive; // Replace the comments label by a text field

  // Used in the non-highlighted, non-interactive case
  private final JLabel fLabel1 = new JLabel();
  private final JLabel fLabel2 = new JLabel();
  private final JLabel fLabel3 = new JLabel();

  // Used in the highlighted case
  private final JLabel fLabel4 = new JLabel();
  private final JLabel fCommentsLabel = new JLabel();

  // Used in the highlighted + interactive case
  private final JTextField fCommentsField = new JTextField();
  private boolean fCommentsFieldHasFocus = false;

  private final ASTERIXTrackAdditionalData fAdditionalData;
  private Object fCurrentObject;
  private ILcdModel fCurrentModel;

  public ASTERIXTrackLabel(boolean aShowExtraInfo, boolean aInteractive, ASTERIXTrackAdditionalData aAdditionalData) {
    if (!aShowExtraInfo && aInteractive) {
      throw new IllegalArgumentException("ASTERIXTrackLabel only supports interactive labels when they are highlighted");
    }
    fShowExtraInfo = aShowExtraInfo;
    fInteractive = aInteractive;
    fAdditionalData = aAdditionalData;
    initUI();

    // Used to make sure that comments are not reset while being edited in the comments text field.
    fCommentsField.addFocusListener(new FocusListener() {
      @Override
      public void focusGained(FocusEvent e) {
        fCommentsFieldHasFocus = true;
      }

      @Override
      public void focusLost(FocusEvent e) {
        fCommentsFieldHasFocus = false;
      }
    });
  }

  @Override
  public JComponent getComponent(Object aObject, Object aSubLabelID, TLspContext aContext) {
    if (!(aObject instanceof TLcdASTERIXTrack)) {
      throw new IllegalArgumentException("Object should be a TLcdASTERIXTrack");
    }
    TLcdASTERIXTrack track = (TLcdASTERIXTrack) aObject;
    configureComponent(track, aContext.getModel());

    fCurrentObject = aObject;
    fCurrentModel = aContext.getModel();

    return this;
  }

  public void updateLabelContent() {
    TLcdASTERIXTrack track = (TLcdASTERIXTrack) fCurrentObject;
    configureComponent(track, fCurrentModel);
  }

  public Object getCurrentObject() {
    return fCurrentObject;
  }

  public ILcdModel getCurrentModel() {
    return fCurrentModel;
  }

  private void configureComponent(TLcdASTERIXTrack aTrack, ILcdModel aModel) {
    String[] content = retrieveContent(aTrack, aModel);
    fLabel1.setText(content[0]);
    fLabel2.setText(content[1]);
    fLabel3.setText(content[2]);
    if (fShowExtraInfo) {
      fLabel4.setText(content[3]);
      if (!fCommentsFieldHasFocus) {
        String comment = fAdditionalData.getComment(aTrack);
        if (fInteractive) {
          fCommentsField.setText(comment == null ? "" : comment);
        } else {
          fCommentsLabel.setText(comment == null ? "-" : comment);
        }
      }
    }
  }

  private String[] retrieveContent(TLcdASTERIXTrack aTrack, ILcdModel aModel) {
    TLcdLockUtil.readLock(aModel);
    try {
      return ASTERIXTrackLabelContentProvider.provideContent(aTrack, fShowExtraInfo);
    } finally {
      TLcdLockUtil.readUnlock(aModel);
    }
  }

  private void initUI() {
    setOpaque(false);
    setLayout(new GridBagLayout());
    GridBagConstraints gbc = new GridBagConstraints(
        0, 0, 1, 1,
        1.0, 0.0,
        GridBagConstraints.CENTER,
        GridBagConstraints.HORIZONTAL,
        new Insets(0, 0, 0, 0),
        0, 0
    );

    fLabel1.setFont(LABEL_FONT);
    fLabel1.setForeground(ASTERIXTrackLabelStyler.LABEL_COLOR);
    add(fLabel1, gbc);

    fLabel2.setFont(LABEL_FONT);
    fLabel2.setForeground(ASTERIXTrackLabelStyler.LABEL_COLOR);
    gbc.gridy++;
    add(fLabel2, gbc);

    fLabel3.setFont(LABEL_FONT);
    fLabel3.setForeground(ASTERIXTrackLabelStyler.LABEL_COLOR);
    gbc.gridy++;
    add(fLabel3, gbc);

    if (fShowExtraInfo) {
      fLabel4.setFont(LABEL_FONT);
      fLabel4.setForeground(ASTERIXTrackLabelStyler.LABEL_COLOR);
      gbc.gridy++;
      add(fLabel4, gbc);

      fCommentsField.setFont(LABEL_FONT);
      fCommentsField.setForeground(ASTERIXTrackLabelStyler.LABEL_COLOR);
      fCommentsField.setBackground(new Color(0, 0, 0, 0));
      fCommentsField.setBorder(BorderFactory.createLineBorder(ASTERIXTrackLabelStyler.LABEL_COLOR));
      fCommentsField.setCaretColor(ASTERIXTrackLabelStyler.LABEL_COLOR);

      fCommentsLabel.setFont(LABEL_FONT);
      fCommentsLabel.setForeground(ASTERIXTrackLabelStyler.LABEL_COLOR);

      gbc.gridy++;
      if (fInteractive) {
        add(fCommentsField, gbc);
      } else {
        gbc.insets = new Insets(1, 1, 1, 0);
        add(fCommentsLabel, gbc);
      }
    }
  }

  /**
   * @return {@code true} if something changed, {@code false} otherwise
   */
  public boolean commitCommentChanges() {
    String comment = fCommentsField.getText();
    if (comment != null && comment.trim().equals("")) {
      comment = null;
    }
    String oldComment = comment == null ?
                        fAdditionalData.removeComment(fCurrentObject) :
                        fAdditionalData.putComment(fCurrentObject, comment);
    boolean changed = !(comment == null ? "" : comment).equals(oldComment == null ? "" : oldComment);
    ILcdModel model = fCurrentModel;
    if (changed && model != null) {
      TLcdLockUtil.writeLock(model);
      try {
        model.elementChanged(fCurrentObject, ILcdModel.FIRE_LATER);
      } finally {
        TLcdLockUtil.writeUnlock(model);
        model.fireCollectedModelChanges();
      }
    }
    return changed;
  }

  public void addPressedEnterActionListener(ActionListener aActionListener) {
    fCommentsField.addActionListener(aActionListener);
  }

  public void addCommentsFieldFocusListener(FocusListener aFocusListener) {
    fCommentsField.addFocusListener(aFocusListener);
  }
}

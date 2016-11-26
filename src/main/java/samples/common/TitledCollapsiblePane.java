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
package samples.common;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.luciad.gui.TLcdIconFactory;
import com.luciad.gui.swing.TLcdSWIcon;

/**
 * A panel that can hide its contents, while a title header remains visible.
 */
public class TitledCollapsiblePane extends JPanel {

  private String fTitle;
  private CollapsiblePane fCollapsiblePane = new CollapsiblePane(CollapsiblePane.Orientation.VERTICAL);
  private TitledSeparator fTitledSeparator;

  public TitledCollapsiblePane(String aTitle, Component aContent) {
    this(aTitle, aContent, true, true);
  }

  public TitledCollapsiblePane(String aTitle, Component aContent, boolean aCollapsible) {
    this(aTitle, aContent, aCollapsible, true);
  }

  public TitledCollapsiblePane(String aTitle, Component aContent, boolean aCollapsible, boolean aAnimate) {
    fTitle = aTitle;
    setLayout(new BorderLayout());
    fCollapsiblePane.setCollapsed(false);
    JPanel header = new JPanel(new BorderLayout(5, 5));
    fTitledSeparator = new TitledSeparator(fTitle);
    addPropertyChangeListener(new TitleMediator(fTitledSeparator));
    header.add(fTitledSeparator, BorderLayout.CENTER);

    if (aCollapsible) {
      JLabel iconLabel = new JLabel();
      addPropertyChangeListener(new IconMediator(false, iconLabel));
      header.add(iconLabel, BorderLayout.EAST);

      final MouseAdapter mouseListener = new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) {
          if (isEnabled()) {
            setCollapsed(!isCollapsed());
          }
        }
      };
      header.addMouseListener(mouseListener);
    }
    add(header, BorderLayout.NORTH);
    if (aCollapsible && aAnimate) {
      JPanel wrapper = new JPanel(AnimatedLayoutManager.create(new BorderLayout()));
      wrapper.add(fCollapsiblePane, BorderLayout.CENTER);
      add(wrapper, BorderLayout.CENTER);
    } else {
      add(fCollapsiblePane, BorderLayout.CENTER);
    }
    getContentPane().setLayout(new FormLayout("fill:default:grow", "top:pref:none"));
    CellConstraints cc = new CellConstraints();
    getContentPane().add(aContent, cc.xy(1, 1));
  }

  @Override
  public void setEnabled(boolean enabled) {
    super.setEnabled(enabled);
    fTitledSeparator.setEnabled(enabled);
  }

  public Container getContentPane() {
    return fCollapsiblePane.getContentPane();
  }

  public String getTitle() {
    return fTitle;
  }

  public void setTitle(String aTitle) {
    String old = fTitle;
    fTitle = aTitle;
    firePropertyChange("title", old, fTitle);
  }

  public boolean isCollapsed() {
    return fCollapsiblePane.isCollapsed();
  }

  public void setCollapsed(boolean aCollapsed) {
    boolean old = fCollapsiblePane.isCollapsed();
    fCollapsiblePane.setCollapsed(aCollapsed);
    firePropertyChange("collapsed", old, aCollapsed);
  }

  private static class TitleMediator implements PropertyChangeListener {
    private final TitledSeparator fLabel;

    public TitleMediator(TitledSeparator aTitle) {
      fLabel = aTitle;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
      if ("title".equals(evt.getPropertyName())) {
        fLabel.setTitle((String) evt.getNewValue());
      }
    }
  }

  private static class IconMediator implements PropertyChangeListener {
    private final Icon fCollapsedPanelIcon = new TLcdSWIcon(TLcdIconFactory.create(TLcdIconFactory.COLLAPSED_PANEL_ICON));
    private final Icon fExpandedPanelIcon = new TLcdSWIcon(TLcdIconFactory.create(TLcdIconFactory.EXPANDED_PANEL_ICON));
    private final JLabel fIconLabel;

    public IconMediator(boolean aCollapsed, JLabel aIconLabel) {
      fIconLabel = aIconLabel;
      updateIcon(aCollapsed);
    }

    private void updateIcon(boolean aCollapsed) {
      fIconLabel.setIcon(aCollapsed ? fCollapsedPanelIcon : fExpandedPanelIcon);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
      if ("collapsed".equals(evt.getPropertyName())) {
        Boolean collapsed = (Boolean) evt.getNewValue();
        updateIcon(collapsed);
      }
    }
  }
}

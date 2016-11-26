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
package samples.lucy.cop.addons.missioncontroltheme;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.imageio.ImageIO;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingWorker;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import com.luciad.datamodel.ILcdDataObject;
import com.luciad.gui.ILcdIcon;
import com.luciad.gui.TLcdImageIcon;
import com.luciad.gui.TLcdResizeableIcon;
import com.luciad.gui.swing.TLcdSWIcon;
import com.luciad.lucy.ILcyLucyEnv;
import com.luciad.lucy.gui.TLcyTwoColumnLayoutBuilder;
import com.luciad.lucy.gui.customizer.ALcyCustomizerPanelFactory;
import com.luciad.lucy.gui.customizer.ALcyDomainObjectCustomizerPanel;
import com.luciad.lucy.gui.customizer.ILcyCustomizerPanel;
import com.luciad.lucy.util.context.TLcyDomainObjectContext;
import com.luciad.lucy.util.properties.ALcyProperties;
import com.luciad.model.ILcdModel;
import com.luciad.shape.ILcdPoint;
import com.luciad.shape.ILcdShapeList;
import com.luciad.shape.shape2D.ILcd2DEditablePoint;
import com.luciad.text.TLcdLonLatPointFormat;
import com.luciad.util.ILcdFilter;
import com.luciad.util.concurrent.TLcdLockUtil;

import samples.lucy.cop.PathResolver;
import samples.lucy.util.ValidatingTextField;

/**
 * {@code ILcyCustomizerPanelFactory} for the spot reports domain objects
 *
 */
final class SpotReportsCustomizerPanelFactory extends ALcyCustomizerPanelFactory {

  private static final ILcdFilter<Object> SPOT_REPORTS_DOMAIN_OBJECT_FILTER = new ILcdFilter<Object>() {
    @Override
    public boolean accept(Object aObject) {
      if (aObject instanceof TLcyDomainObjectContext) {
        TLcyDomainObjectContext domainObjectContext = ((TLcyDomainObjectContext) aObject);
        Object domainObject = domainObjectContext.getDomainObject();
        return domainObjectContext.getModel() instanceof SpotReportsModel &&
               domainObject instanceof GeoJsonRestModelElement;
      }
      return false;
    }
  };

  private final String fPropertiesPrefix;
  private final ALcyProperties fProperties;
  private final ILcyLucyEnv fLucyEnv;

  SpotReportsCustomizerPanelFactory(String aPropertiesPrefix, ALcyProperties aProperties, ILcyLucyEnv aLucyEnv) {
    super(SPOT_REPORTS_DOMAIN_OBJECT_FILTER);
    fPropertiesPrefix = aPropertiesPrefix;
    fProperties = aProperties;
    fLucyEnv = aLucyEnv;

  }

  @Override
  protected ILcyCustomizerPanel createCustomizerPanelImpl(Object aObject) {
    return new SpotReportCustomizerPanel(fPropertiesPrefix, fProperties, fLucyEnv);
  }

  private static class SpotReportCustomizerPanel extends ALcyDomainObjectCustomizerPanel {

    private static final String[] POSSIBLE_CODES = {
        "SHGPEWRR----***", "SFGPUCI-----***", "SHGPEVAT----***",
        "SHAPMHA-----***", "SNAPMFFI----***", "SUAPWB------***",
        "SFAPMHO-----***"
    };

    private final ILcyLucyEnv fLucyEnv;
    private final MS2525IconProvider fMS2525IconProvider = new MS2525IconProvider();
    private final String fBaseURL;

    private ValidatingTextField fLocationField;
    private JTextField fActivityField;
    private JComboBox fCodeComboBox;
    private JLabel fAttachmentLabel;

    private boolean fManuallyUpdatingUI = false;

    private SpotReportCustomizerPanel(String aPropertiesPrefix, ALcyProperties aProperties, ILcyLucyEnv aLucyEnv) {
      super(SPOT_REPORTS_DOMAIN_OBJECT_FILTER, "Spot report");
      fLucyEnv = aLucyEnv;
      PathResolver pathResolver = fLucyEnv.getService(PathResolver.class);
      fBaseURL = pathResolver.convertPath(aProperties.getString(aPropertiesPrefix + SpotReportsModel.SPOT_REPORT_PREFIX + "attachmentsBaseURL", "http://localhost:8072/db/dataobject/report/"));
      initUI();
    }

    private void initUI() {
      TLcdLonLatPointFormat pointFormat = new TLcdLonLatPointFormat("lat(+DMS2),lon(+DMS2)");
      fLocationField = new ValidatingTextField(pointFormat, fLucyEnv);

      fActivityField = new JTextField(15);

      fCodeComboBox = new JComboBox(POSSIBLE_CODES);
      fCodeComboBox.setRenderer(new DefaultListCellRenderer() {
        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
          super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
          if (fMS2525IconProvider.canGetIcon(value)) {
            setIcon(new TLcdSWIcon(new TLcdResizeableIcon(fMS2525IconProvider.getIcon(value), 32, 32)));
          }
          return this;
        }
      });

      fAttachmentLabel = new JLabel();
      JPanel attachmentLabelPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
      attachmentLabelPanel.add(fAttachmentLabel);

      TLcyTwoColumnLayoutBuilder builder = TLcyTwoColumnLayoutBuilder.newBuilder();

      builder.addTitledSeparator("Spot report info");
      builder.row().columnOne(new JLabel("Type"), fCodeComboBox).build();
      builder.row().columnOne(new JLabel("Activity"), fActivityField).build();
      builder.row().columnOne(attachmentLabelPanel).build();

      builder.addTitledSeparator("Position");
      builder.row().spanBothColumns(fLocationField).build();

      builder.populate(this);

      addListenersToUI();
    }

    private void addListenersToUI() {
      fLocationField.addPropertyChangeListener("value", new PropertyChangeListener() {
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
          if (!fManuallyUpdatingUI) {
            setChangesPending(true);
          }
        }
      });
      fActivityField.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
          if (!fManuallyUpdatingUI) {
            setChangesPending(true);
          }
        }
      });
      fCodeComboBox.addItemListener(new ItemListener() {
        @Override
        public void itemStateChanged(ItemEvent e) {
          if (e.getStateChange() == ItemEvent.SELECTED && !fManuallyUpdatingUI) {
            setChangesPending(true);
          }
        }
      });
    }

    @Override
    protected boolean applyChangesImpl() {
      ILcdDataObject domainObject = (ILcdDataObject) getDomainObject();
      if (domainObject != null) {
        ILcdModel model = getModel();
        try (TLcdLockUtil.Lock autoUnlock = TLcdLockUtil.writeLock(model)) {
          ((ILcd2DEditablePoint) ((ILcdShapeList) domainObject).getShape(0)).move2D((ILcdPoint) fLocationField.getValue());
          domainObject.setValue(SpotReportsModel.ACTIVITY_PROPERTY, fActivityField.getText());
          domainObject.setValue(SpotReportsModel.CODE_PROPERTY, fCodeComboBox.getSelectedItem());
          model.elementChanged(domainObject, ILcdModel.FIRE_LATER);
        } finally {
          model.fireCollectedModelChanges();
        }

        return true;
      }
      return false;
    }

    @Override
    protected void updateCustomizerPanelFromObject(boolean aPanelEditable) {
      fLocationField.setEditable(aPanelEditable);
      fActivityField.setEditable(aPanelEditable);
      fCodeComboBox.setEditable(false);
      fCodeComboBox.setEnabled(aPanelEditable);
      ILcdDataObject domainObject = (ILcdDataObject) getDomainObject();
      if (domainObject != null) {
        boolean oldValue = fManuallyUpdatingUI;
        fManuallyUpdatingUI = true;
        try (TLcdLockUtil.Lock autoUnlock = TLcdLockUtil.readLock(getModel())) {
          String code = (String) domainObject.getValue(SpotReportsModel.CODE_PROPERTY);
          addCodeToComboBoxIfNeeded(code);
          fCodeComboBox.setSelectedItem(code);
          fActivityField.setText((String) domainObject.getValue(SpotReportsModel.ACTIVITY_PROPERTY));
          fLocationField.setValue(((ILcdShapeList) domainObject).getShape(0));

          List attachments = (List) domainObject.getValue(SpotReportsModel.ATTACHMENTS_PROPERTY);
          if (attachments != null && !(attachments.isEmpty()) && !((String) attachments.get(0)).isEmpty()) {
            new ImageRetriever(fAttachmentLabel, fBaseURL + attachments.get(0)).execute();
          }

        } finally {
          fManuallyUpdatingUI = oldValue;
        }
      }
    }

    private void addCodeToComboBoxIfNeeded(String aCode) {
      int count = fCodeComboBox.getItemCount();
      for (int i = 0; i < count; i++) {
        if (aCode.equals(fCodeComboBox.getItemAt(i))) {
          return;
        }
      }
      fCodeComboBox.addItem(aCode);
    }
  }

  private static class ImageRetriever extends SwingWorker<BufferedImage, Void> {
    private final JLabel fLabelToUpdate;
    private final String fURL;

    private ImageRetriever(JLabel aLabelToUpdate, String aURL) {
      fLabelToUpdate = aLabelToUpdate;
      fLabelToUpdate.setPreferredSize(new Dimension(200, 150));
      fURL = aURL;
    }

    @Override
    protected BufferedImage doInBackground() throws Exception {
      DefaultHttpClient httpClient = new DefaultHttpClient();
      HttpGet httpGet = new HttpGet(fURL);
      HttpResponse response = httpClient.execute(httpGet);
      try {
        StatusLine statusLine = response.getStatusLine();
        if (statusLine.getStatusCode() == 200) {
          HttpEntity entity = response.getEntity();
          return ImageIO.read(entity.getContent());
        }
      } finally {
        httpGet.releaseConnection();
      }
      return null;
    }

    @Override
    protected void done() {
      try {
        BufferedImage bufferedImage = get();
        fLabelToUpdate.setVisible(bufferedImage != null);
        if (bufferedImage != null) {
          ILcdIcon icon = new TLcdImageIcon(bufferedImage);
          if (icon.getIconHeight() > icon.getIconWidth()) {
            icon = new TLcdResizeableIcon(icon, -1, 150);
          } else {
            icon = new TLcdResizeableIcon(icon, 200, -1);
          }
          fLabelToUpdate.setIcon(new TLcdSWIcon(icon));
          fLabelToUpdate.setPreferredSize(new Dimension(icon.getIconWidth(), icon.getIconHeight()));
          JComponent parent = (JComponent) fLabelToUpdate.getParent();
          parent.revalidate();
          parent.repaint();
        }
      } catch (InterruptedException e) {
        //ignore
      } catch (ExecutionException e) {
        //ignore
      }
    }
  }
}

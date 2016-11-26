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
package samples.lightspeed.demo.application.data.maritime;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.JLabel;

import com.luciad.datamodel.ILcdDataObject;
import com.luciad.datamodel.TLcdDataProperty;
import com.luciad.datamodel.TLcdDataType;
import com.luciad.format.s57.TLcdS57SoundingPoint;
import com.luciad.view.ILcdLayer;
import com.luciad.view.lightspeed.layer.ILspLayer;

import samples.lightspeed.demo.framework.gui.TooltipMouseListener;

public final class EcdisTooltipLogic implements TooltipMouseListener.TooltipLogic {

  private final List<ILspLayer> fNOAALayers;

  public EcdisTooltipLogic(List<ILspLayer> aNOAALayers) {
    fNOAALayers = aNOAALayers;
  }

  @Override
  public boolean canHandleLayer(ILcdLayer aLayer) {
    return fNOAALayers.contains(aLayer);
  }

  @Override
  public boolean shouldConsiderLayer(ILcdLayer aLayer) {
    return canHandleLayer(aLayer);
  }

  @Override
  public void willRecalculateTooltip() {
    //do nothing
  }

  @Override
  public Component createLabelComponent(Object aModelElement) {
    final int minLabelWidth = 75;
    return new JLabel(EcdisTooltipLogic.retrieveTooltipContents((ILcdDataObject) aModelElement)) {
      @Override
      protected void paintComponent(Graphics g) {
        g.setColor(new Color(0.3f, 0.3f, 0.3f, 0.9f));
        g.fillRect(0, 0, Math.max(minLabelWidth, getSize().width), getSize().height);
        g.setColor(Color.black);
        g.drawRect(0, 0, Math.max(minLabelWidth, getSize().width) - 1, getSize().height - 1);
        super.paintComponent(g);
      }

      @Override
      public Dimension getMinimumSize() {
        Dimension minimumSize = super.getMinimumSize();
        return new Dimension(Math.max(minimumSize.width, minLabelWidth), minimumSize.height);
      }
    };
  }

  @Override
  public void updateForFoundModelElement(Object aModelElement, ILcdLayer aLayer, MouseEvent aMouseEvent, TooltipMouseListener aTooltipMouseListener) {
    aTooltipMouseListener.showTooltip(aModelElement, aLayer, aMouseEvent.getX(), aMouseEvent.getY());
  }

  @Override
  public int getQuerySensitivity() {
    return 1;
  }

  static String retrieveTooltipContents(ILcdDataObject aModelElement) {
    TLcdDataType dataType = aModelElement.getDataType();

    StringBuilder builder = new StringBuilder();
    builder.append("<html><body>");
    addTypeDisplayName(dataType, builder);
    if ("DEPAREType".equals(dataType.getName())) {
      addAdditionalInfo(null, aModelElement.getValue("DRVAL1") + " m - " + aModelElement.getValue("DRVAL2") + " m", builder);
    } else if ("DWRTPTType".equals(dataType.getName())) {
      addAdditionalInfo("Depth", aModelElement.getValue("DRVAL1") + " m", builder);
    } else if ("DRGAREType".equals(dataType.getName())) {
      addAdditionalInfo("Depth", aModelElement.getValue("DRVAL1") + " m", builder);
    } else if ("BOYSPPType".equals(dataType.getName())) {
      addAdditionalInfo("Color", "COLOUR", dataType, aModelElement, builder);
      addAdditionalInfo("Color pattern", "COLPAT", dataType, aModelElement, builder);
    } else if ("BCNLATType".equals(dataType.getName())) {
      addAdditionalInfo("Color", "COLOUR", dataType, aModelElement, builder);
      addAdditionalInfo("Color pattern", "COLPAT", dataType, aModelElement, builder);
    } else if ("TOPMARType".equals(dataType.getName())) {
      addAdditionalInfo("Color", "COLOUR", dataType, aModelElement, builder);
      addAdditionalInfo("Color pattern", "COLPAT", dataType, aModelElement, builder);
    } else if ("PYLONSType".equals(dataType.getName())) {
      addAdditionalInfo("Color", "COLOUR", dataType, aModelElement, builder);
      addAdditionalInfo("Color pattern", "COLPAT", dataType, aModelElement, builder);
    } else if ("BCNISDType".equals(dataType.getName())) {
      addAdditionalInfo("Color", "COLOUR", dataType, aModelElement, builder);
      addAdditionalInfo("Color pattern", "COLPAT", dataType, aModelElement, builder);
    } else if ("DAYMARType".equals(dataType.getName())) {
      addAdditionalInfo("Color", "COLOUR", dataType, aModelElement, builder);
      addAdditionalInfo("Color pattern", "COLPAT", dataType, aModelElement, builder);
    } else if ("BOYLATType".equals(dataType.getName())) {
      addAdditionalInfo("Color", "COLOUR", dataType, aModelElement, builder);
      addAdditionalInfo("Color pattern", "COLPAT", dataType, aModelElement, builder);
    } else if ("SLCONSType".equals(dataType.getName())) {
      addAdditionalInfo("Color", "COLOUR", dataType, aModelElement, builder);
      addAdditionalInfo("Color pattern", "COLPAT", dataType, aModelElement, builder);
    } else if ("OFSPLFType".equals(dataType.getName())) {
      addAdditionalInfo("Color", "COLOUR", dataType, aModelElement, builder);
      addAdditionalInfo("Color pattern", "COLPAT", dataType, aModelElement, builder);
    } else if ("BCNSAWType".equals(dataType.getName())) {
      addAdditionalInfo("Color", "COLOUR", dataType, aModelElement, builder);
      addAdditionalInfo("Color pattern", "COLPAT", dataType, aModelElement, builder);
    } else if ("BUISGLType".equals(dataType.getName())) {
      addAdditionalInfo("Color", "COLOUR", dataType, aModelElement, builder);
      addAdditionalInfo("Color pattern", "COLPAT", dataType, aModelElement, builder);
    } else if ("FNCLNEType".equals(dataType.getName())) {
      addAdditionalInfo("Color", "COLOUR", dataType, aModelElement, builder);
      addAdditionalInfo("Color pattern", "COLPAT", dataType, aModelElement, builder);
    } else if ("CRANESType".equals(dataType.getName())) {
      addAdditionalInfo("Color", "COLOUR", dataType, aModelElement, builder);
      addAdditionalInfo("Color pattern", "COLPAT", dataType, aModelElement, builder);
    } else if ("BCNSPPType".equals(dataType.getName())) {
      addAdditionalInfo("Color", "COLOUR", dataType, aModelElement, builder);
      addAdditionalInfo("Color pattern", "COLPAT", dataType, aModelElement, builder);
    } else if ("DAMCONType".equals(dataType.getName())) {
      addAdditionalInfo("Color", "COLOUR", dataType, aModelElement, builder);
      addAdditionalInfo("Color pattern", "COLPAT", dataType, aModelElement, builder);
    } else if ("MORFACType".equals(dataType.getName())) {
      addAdditionalInfo("Color", "COLOUR", dataType, aModelElement, builder);
      addAdditionalInfo("Color pattern", "COLPAT", dataType, aModelElement, builder);
    } else if ("BOYCARType".equals(dataType.getName())) {
      addAdditionalInfo("Color", "COLOUR", dataType, aModelElement, builder);
      addAdditionalInfo("Color pattern", "COLPAT", dataType, aModelElement, builder);
    } else if ("BOYISDType".equals(dataType.getName())) {
      addAdditionalInfo("Color", "COLOUR", dataType, aModelElement, builder);
      addAdditionalInfo("Color pattern", "COLPAT", dataType, aModelElement, builder);
    } else if ("FLODOCType".equals(dataType.getName())) {
      addAdditionalInfo("Color", "COLOUR", dataType, aModelElement, builder);
      addAdditionalInfo("Color pattern", "COLPAT", dataType, aModelElement, builder);
    } else if ("PILPNTType".equals(dataType.getName())) {
      addAdditionalInfo("Color", "COLOUR", dataType, aModelElement, builder);
      addAdditionalInfo("Color pattern", "COLPAT", dataType, aModelElement, builder);
    } else if ("LITFLTType".equals(dataType.getName())) {
      addAdditionalInfo("Color", "COLOUR", dataType, aModelElement, builder);
      addAdditionalInfo("Color pattern", "COLPAT", dataType, aModelElement, builder);
    } else if ("BRIDGEType".equals(dataType.getName())) {
      addAdditionalInfo("Color", "COLOUR", dataType, aModelElement, builder);
      addAdditionalInfo("Color pattern", "COLPAT", dataType, aModelElement, builder);
    } else if ("BOYSAWType".equals(dataType.getName())) {
      addAdditionalInfo("Color", "COLOUR", dataType, aModelElement, builder);
      addAdditionalInfo("Color pattern", "COLPAT", dataType, aModelElement, builder);
    } else if ("HULKESType".equals(dataType.getName())) {
      addAdditionalInfo("Color", "COLOUR", dataType, aModelElement, builder);
      addAdditionalInfo("Color pattern", "COLPAT", dataType, aModelElement, builder);
    } else if ("RETRFLType".equals(dataType.getName())) {
      addAdditionalInfo("Color", "COLOUR", dataType, aModelElement, builder);
      addAdditionalInfo("Color pattern", "COLPAT", dataType, aModelElement, builder);
    } else if ("CONVYRType".equals(dataType.getName())) {
      addAdditionalInfo("Color", "COLOUR", dataType, aModelElement, builder);
      addAdditionalInfo("Color pattern", "COLPAT", dataType, aModelElement, builder);
    } else if ("STSLNEType".equals(dataType.getName())) {
      addAdditionalInfo("Nationality", "NATION", dataType, aModelElement, builder);
    } else if ("EXEZNEType".equals(dataType.getName())) {
      addAdditionalInfo("Nationality", "NATION", dataType, aModelElement, builder);
    } else if ("FSHZNEType".equals(dataType.getName())) {
      addAdditionalInfo("Nationality", "NATION", dataType, aModelElement, builder);
    } else if ("TESAREType".equals(dataType.getName())) {
      addAdditionalInfo("Nationality", "NATION", dataType, aModelElement, builder);
    } else if ("PIPSOLType".equals(dataType.getName())) {
      addAdditionalInfo("Product", "PRODCT", dataType, aModelElement, builder);
    } else if ("PRDAREType".equals(dataType.getName())) {
      addAdditionalInfo("Product", "PRODCT", dataType, aModelElement, builder);
    } else if ("SILTNKType".equals(dataType.getName())) {
      addAdditionalInfo("Product", "PRODCT", dataType, aModelElement, builder);
    } else if ("PIPOHDType".equals(dataType.getName())) {
      addAdditionalInfo("Product", "PRODCT", dataType, aModelElement, builder);
    } else if ("PIPAREType".equals(dataType.getName())) {
      addAdditionalInfo("Product", "PRODCT", dataType, aModelElement, builder);
    } else if ("SOUNDGType".equals(dataType.getName())) {
      double soundingValue = ((TLcdS57SoundingPoint) aModelElement).getSoundingValue();
      addAdditionalInfo("Value", "" + (Math.round(soundingValue * 100.0) / 100.0), builder);
      addAdditionalInfo("Quality", "QUASOU", dataType, aModelElement, builder);
      addAdditionalInfo("Measurement technique", "TECSOU", dataType, aModelElement, builder);
    } else if ("FOGSIGType".equals(dataType.getName())) {
      addAdditionalInfo("Category", "CATFOG", dataType, aModelElement, builder);
    } else if ("LIGHTSType".equals(dataType.getName())) {
      addAdditionalInfo("Category", "CATLIT", dataType, aModelElement, builder);
      addAdditionalInfo("Color", "COLOUR", dataType, aModelElement, builder);
    } else if ("DMPGRDType".equals(dataType.getName())) {
      addAdditionalInfo("Restriction", "RESTRN", dataType, aModelElement, builder);
    } else if ("DISMARType".equals(dataType.getName())) {
      addAdditionalInfo("Category", "CATDIS", dataType, aModelElement, builder);
    } else if ("DEPCNTType".equals(dataType.getName())) {
      addAdditionalInfo(null, aModelElement.getValue("VALDCO") + " m ", builder);
    } else if ("MIPAREType".equals(dataType.getName())) {
      addAdditionalInfo("Category", "CATMPA", dataType, aModelElement, builder);
      addAdditionalInfo("Restriction", "RESTRN", dataType, aModelElement, builder);
    } else if ("RESAREType".equals(dataType.getName())) {
      addAdditionalInfo("Category", "CATREA", dataType, aModelElement, builder);
      addAdditionalInfo("Restriction", "RESTRN", dataType, aModelElement, builder);
    } else if ("UWTROCType".equals(dataType.getName())) {
      addAdditionalInfo("Nature of surface", "NATSUR", dataType, aModelElement, builder);
    } else if ("OBSTRNType".equals(dataType.getName())) {
      addAdditionalInfo("Nature of construction", "NATCON", dataType, aModelElement, builder);
    } else if ("LNDELVType".equals(dataType.getName())) {
      addAdditionalInfo(null, aModelElement.getValue("ELEVAT") + " m ", builder);
    } else if ("WRECKSType".equals(dataType.getName())) {
      addAdditionalInfo("Category", "CATWRK", dataType, aModelElement, builder);
    }
    builder.append("</body></html>");

    return builder.toString();
  }

  private static void addAdditionalInfo(String label, String propertyName, TLcdDataType aDataType, ILcdDataObject aModelElement, StringBuilder aBuilderSFCT) {
    TLcdDataProperty property = aDataType.getProperty(propertyName);
    if (property.getCollectionType() == null) {
      Object propertyValue = aModelElement.getValue(propertyName);
      addAdditionalInfo(label, propertyValue != null ? property.getType().getDisplayName(propertyValue) : "", aBuilderSFCT);
    } else {
      Collection propertyValue = (Collection) aModelElement.getValue(propertyName);
      List<String> values = new ArrayList<String>() {
        @Override
        public String toString() {
          String s = super.toString();
          return s.replace("[", "").replace("]", "");
        }
      };
      for (Object value : propertyValue) {
        if (value != null) {
          values.add(property.getType().getDisplayName(value));
        }
      }
      addAdditionalInfo(label, values.size() == 1 ? values.get(0) : values.toString(), aBuilderSFCT);
    }
  }

  private static void addAdditionalInfo(String label, String entry, StringBuilder aBuilderSFCT) {
    if (entry != null && (entry.length() > 0 && !entry.equals("[]"))) {
      aBuilderSFCT.append("<br>");
      aBuilderSFCT.append("&nbsp;&nbsp;");
      if (label != null && label.length() > 0) {
        aBuilderSFCT.append("<i>").append(label).append(": </i>");
        aBuilderSFCT.append(entry);
      } else {
        aBuilderSFCT.append("<i>").append(entry).append("</i>");
      }
    }
  }

  private static void addTypeDisplayName(TLcdDataType aDataType, StringBuilder aBuilderSFCT) {
    aBuilderSFCT.append("&nbsp;&nbsp;<b>").append(aDataType.getDisplayName()).append("</b>");
  }
}

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
package samples.lightspeed.demo.application.data.support.modelfactories;

import java.io.IOException;
import java.util.Enumeration;

import com.luciad.datamodel.ILcdDataObject;
import com.luciad.format.shp.TLcdSHPModelDecoder;
import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelDescriptor;
import com.luciad.model.TLcdVectorModel;
import com.luciad.shape.ILcdPoint;
import com.luciad.shape.ILcdShapeList;
import com.luciad.shape.shape2D.ILcd2DEditablePointList;
import com.luciad.symbology.app6a.model.ELcdAPP6Standard;
import com.luciad.symbology.app6a.model.TLcdEditableAPP6AObject;

import samples.lightspeed.demo.framework.data.AbstractModelFactory;
import samples.symbology.common.EMilitarySymbology;
import samples.symbology.common.app6.APP6ModelDescriptor;

/**
 * Model factory for the military symbology theme.
 */
public class MilSymModelFactory extends AbstractModelFactory {

  public MilSymModelFactory(String aType) {
    super(aType);
  }

  @Override
  public ILcdModel createModel(String aSource) throws IOException {
    try {
      APP6ModelDescriptor.class.getName();
    } catch (NoClassDefFoundError e) {
      throw new IOException("Can't create model: Military Symbology module not present");
    }

    TLcdSHPModelDecoder decoder = new TLcdSHPModelDecoder();
    ILcdModel model = decoder.decode(aSource);
    return convertToMilSymModel(model);
  }

  private ILcdModel convertToMilSymModel(ILcdModel aInput) {
    ILcdModelDescriptor indesc = aInput.getModelDescriptor();
    TLcdVectorModel model = new TLcdVectorModel(
        aInput.getModelReference(),
        new APP6ModelDescriptor(
            indesc.getSourceName(),
            "MilSym",
            indesc.getDisplayName(),
            new String[]{},
            EMilitarySymbology.APP6B
        )
    );

    Enumeration elements = aInput.elements();
    while (elements.hasMoreElements()) {
      Object o = elements.nextElement();
      TLcdEditableAPP6AObject mso = convertToAPP6BObject(o);
      if (mso != null) {
        model.addElement(mso, ILcdModel.NO_EVENT);
      }
    }
    return model;
  }

  private TLcdEditableAPP6AObject convertToAPP6BObject(Object aObject) {
    ILcd2DEditablePointList pointList = findPointList(aObject);
    if (pointList != null) {
      TLcdEditableAPP6AObject mo = new TLcdEditableAPP6AObject(
          ((ILcdDataObject) aObject).getValue("APP6A_Code").toString(),
          ELcdAPP6Standard.APP_6B
      );
      for (int i = 0; i < pointList.getPointCount(); ++i) {
        ILcdPoint p = pointList.getPoint(i);
        if (i < mo.getPointCount()) {
          mo.move2DPoint(i, p.getX(), p.getY());
        } else {
          mo.insert2DPoint(i, p.getX(), p.getY());
        }
      }
      mo.setWidth((Double) (((ILcdDataObject) aObject).getValue("Width")));

      return mo;
    } else {
      return null;
    }
  }

  private ILcd2DEditablePointList findPointList(Object aObject) {
    if (aObject instanceof ILcd2DEditablePointList) {
      return (ILcd2DEditablePointList) aObject;
    } else {
      if (aObject instanceof ILcdShapeList) {
        ILcdShapeList sl = (ILcdShapeList) aObject;
        for (int i = 0; i < sl.getShapeCount(); i++) {
          ILcd2DEditablePointList pl = findPointList(sl.getShape(i));
          if (pl != null) {
            return pl;
          }
        }
      }
      return null;
    }
  }

}

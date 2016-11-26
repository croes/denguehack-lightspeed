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
package samples.lightspeed.demo.application.data.airplots;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.luciad.datamodel.ILcdDataObject;
import com.luciad.datamodel.TLcdCoreDataTypes;
import com.luciad.datamodel.TLcdDataModel;
import com.luciad.datamodel.TLcdDataModelBuilder;
import com.luciad.datamodel.TLcdDataObject;
import com.luciad.datamodel.TLcdDataProperty;
import com.luciad.datamodel.TLcdDataType;
import com.luciad.datamodel.TLcdDataTypeBuilder;
import com.luciad.io.ILcdInputStreamFactory;
import com.luciad.io.TLcdInputStreamFactory;
import com.luciad.model.ILcd2DBoundsInteractable;
import com.luciad.model.ILcdDataModelDescriptor;
import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelDecoder;
import com.luciad.model.ILcdModelReference;
import com.luciad.model.ILcdTileDecoder;
import com.luciad.model.TLcdModelDescriptor;
import com.luciad.model.TLcdVectorModel;
import com.luciad.reference.TLcdModelReferenceDecoder;
import com.luciad.shape.shape3D.TLcdLonLatHeightPoint;
import com.luciad.util.ILcdOriented;

import samples.lightspeed.plots.datamodelstyling.EnumAnnotation;
import samples.lightspeed.plots.datamodelstyling.RangeAnnotation;

/**
 * Reads binary ExactAIS data.
 *
 * @see samples.lightspeed.demo.application.data.maritime.CSVModelDecoder
 * @see AirPlotsSHPToBinConverter
 */
class AirPlotsBinModelDecoder implements ILcdModelDecoder, ILcdTileDecoder {

  private ILcdInputStreamFactory fInputStreamFactory;

  public AirPlotsBinModelDecoder(ILcdInputStreamFactory aInputStreamFactory) {
    fInputStreamFactory = aInputStreamFactory;
  }

  public AirPlotsBinModelDecoder() {
    this(new TLcdInputStreamFactory());
  }

  @Override
  public String getDisplayName() {
    return "CSV";
  }

  public ILcdInputStreamFactory getInputStreamFactory() {
    return fInputStreamFactory;
  }

  @Override
  public boolean canDecodeSource(String aSourceName) {
    return aSourceName.toLowerCase().endsWith(".bin");
  }

  @Override
  public ILcd2DBoundsInteractable decodeTile(String aTileSourceName) throws IOException {
    return (ILcd2DBoundsInteractable) decode(aTileSourceName);
  }

  @Override
  public ILcdModel decode(String aSourceName) throws IOException {

    InputStream is = new BufferedInputStream(
        fInputStreamFactory.createInputStream(aSourceName),
        1024 * 1024 * 8
    );
    DataInputStream in = new DataInputStream(is);
    try {
      TLcdVectorModel model = new TLcdVectorModel();

      TLcdModelReferenceDecoder referenceDecoder = new TLcdModelReferenceDecoder();
      ILcdModelReference reference = referenceDecoder.decodeModelReference(aSourceName);
      model.setModelReference(reference);
      MyModelDescriptor modelDescriptor = new MyModelDescriptor(aSourceName, aSourceName, "Air Plots");
      model.setModelDescriptor(modelDescriptor);

      TLcdDataModel dataModel = decodeDataModel(in);
      modelDescriptor.setDataModel(dataModel);
      // There's only one type on this data model
      TLcdDataType type = dataModel.getTypes().iterator().next();

      List<TLcdDataProperty> properties = type.getProperties();

      EnumAnnotation.Builder<Object>[] enumBuilders = new EnumAnnotation.Builder[properties.size()];
      RangeAnnotation.Builder<Number>[] rangeBuilders = new RangeAnnotation.Builder[properties.size()];

      for (int i = 0; i < properties.size(); i++) {
        enumBuilders[i] = EnumAnnotation.newBuilder();
        rangeBuilders[i] = RangeAnnotation.newBuilder();
      }

      try {
        while (true) {
          double x = in.readDouble();
          double y = in.readDouble();
          double z = in.readDouble();

          MyPoint point = new MyPoint(type, x, y, z);

          for (int i = 0; i < properties.size(); i++) {
            TLcdDataProperty property = properties.get(i);

            Object value = null;
            if (property.getType() == TLcdCoreDataTypes.STRING_TYPE) {
              value = readString(in);
            } else if (property.getType() == TLcdCoreDataTypes.DOUBLE_TYPE) {
              value = in.readDouble();
            } else if (property.getType() == TLcdCoreDataTypes.INTEGER_TYPE) {
              value = in.readInt();
            } else if (property.getType() == TLcdCoreDataTypes.LONG_TYPE) {
              value = in.readLong();
            } else if (property.getType() == TLcdCoreDataTypes.SHORT_TYPE) {
              value = in.readShort();
            } else {
              throw new UnsupportedOperationException("Not supported: " + property.getType());
            }

            value = enumBuilders[i].accumulate(value);
            if (value instanceof Number) {
              rangeBuilders[i].accumulate((Number) value);
            }

            point.setValue(property, value);
          }

          model.addElement(point, ILcdModel.NO_EVENT);
        }

      } catch (EOFException e) {
        // eof
      }

      for (int i = 0; i < properties.size(); i++) {
        if (enumBuilders[i].build() != null) {
          properties.get(i).addAnnotation(enumBuilders[i].build());
        }
        if (rangeBuilders[i].build() != null) {
          properties.get(i).addAnnotation(rangeBuilders[i].build());
        }
      }

      return model;
    } finally {
      in.close();
    }
  }

  private static class MyModelDescriptor extends TLcdModelDescriptor implements ILcdDataModelDescriptor {

    private TLcdDataModel fDataModel = TLcdCoreDataTypes.getDataModel();

    private MyModelDescriptor() {
    }

    private MyModelDescriptor(String aSourceName, String aTypeName, String aDisplayName) {
      super(aSourceName, aTypeName, aDisplayName);
    }

    public TLcdDataModel getDataModel() {
      return fDataModel;
    }

    void setDataModel(TLcdDataModel aDataModel) {
      fDataModel = aDataModel;
    }

    /**
     * Returns a set containing the single type of which all model elements are instances.
     * @return a set containing the single type of which all model elements are instances
     */
    public Set<TLcdDataType> getModelElementTypes() {
      return fDataModel.getDeclaredTypes();
    }

    public Set<TLcdDataType> getModelTypes() {
      return fDataModel.getTypes();
    }
  }

  private static final class MyPoint extends TLcdLonLatHeightPoint implements ILcdDataObject, ILcdOriented {
    private TLcdDataObject fDelegate;

    private MyPoint(TLcdDataType aType, double aLon, double aLat, double aZ) {
      super(aLon, aLat, aZ);
      fDelegate = new TLcdDataObject(aType);
    }

    @Override
    public double getOrientation() {
      Object result = fDelegate.getValue("Heading");
      if (result instanceof Double) {
        return (Double) result;
      }
      return 0;
    }

    @Override
    public Object getValue(TLcdDataProperty aProperty) {
      return fDelegate.getValue(aProperty);
    }

    @Override
    public TLcdDataType getDataType() {
      return fDelegate.getDataType();
    }

    @Override
    public void setValue(TLcdDataProperty aProperty, Object aValue) {
      fDelegate.setValue(aProperty, aValue);
    }

    @Override
    public boolean hasValue(TLcdDataProperty aProperty) {
      return fDelegate.hasValue(aProperty);
    }

    public TLcdDataObject clone(Map aObjectDictionary) {
      throw new UnsupportedOperationException();
    }

    @Override
    public MyPoint clone() {
      MyPoint result = (MyPoint) super.clone();
      result.fDelegate = fDelegate.clone();
      return result;
    }

    @Override
    public Object getValue(String aPropertyName) {
      return fDelegate.getValue(aPropertyName);
    }

    @Override
    public void setValue(String aPropertyName, Object aValue) {
      fDelegate.setValue(aPropertyName, aValue);
    }

    @Override
    public boolean hasValue(String aPropertyName) {
      return fDelegate.hasValue(aPropertyName);
    }

    @Override
    public String toString() {
      return "";
    }
  }

  private static TLcdDataModel decodeDataModel(DataInputStream in) throws IOException {
    TLcdDataModelBuilder dataModelBuilder = new TLcdDataModelBuilder("AirPlotsModel");
    TLcdDataTypeBuilder typeBuilder = dataModelBuilder.typeBuilder("AirPlotsType");
    int nbProperties = in.readInt();
    for (int i = 0; i < nbProperties; i++) {
      String name = readString(in);
      char type = in.readChar();
      if ('s' == type) {
        typeBuilder.addProperty(name, TLcdCoreDataTypes.STRING_TYPE);
      } else if ('d' == type) {
        typeBuilder.addProperty(name, TLcdCoreDataTypes.DOUBLE_TYPE);
      } else if ('i' == type) {
        typeBuilder.addProperty(name, TLcdCoreDataTypes.INTEGER_TYPE);
      } else if ('h' == type) {
        typeBuilder.addProperty(name, TLcdCoreDataTypes.SHORT_TYPE);
      } else if ('l' == type) {
        typeBuilder.addProperty(name, TLcdCoreDataTypes.LONG_TYPE);
      }
    }
    return dataModelBuilder.createDataModel();
  }

  private static String readString(DataInputStream in) throws IOException {
    int length = in.readInt();
    byte[] bytes = new byte[length];
    in.readFully(bytes);
    return new String(bytes);
  }
}

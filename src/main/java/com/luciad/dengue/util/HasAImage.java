package com.luciad.dengue.util;

import com.luciad.datamodel.*;
import com.luciad.imaging.ALcdImage;
import com.luciad.shape.ILcdBounded;
import com.luciad.shape.ILcdBounds;
import com.luciad.shape.shape2D.TLcdLonLatBounds;
import com.luciad.util.TLcdHasGeometryAnnotation;

/**
 * @author Thomas De Bodt
 */
public class HasAImage extends TLcdDataObject implements ILcdBounded {
  private static final TLcdDataModel DATA_MODEL;
  private static final TLcdDataType DATA_TYPE;
  private static final TLcdDataProperty IMAGE_PROPERTY;

  static {
    DATA_MODEL = new TLcdDataModelBuilder("HasAImage")
        .typeBuilder("data")
        .addProperty("image", ALcdImage.IMAGE_DATA_TYPE)
        .getDeclaringTypeBuilder()
        .getDataModelBuilder()
        .createDataModel();
    DATA_TYPE = DATA_MODEL.getDeclaredType("data");
    IMAGE_PROPERTY = DATA_TYPE.getProperty("image");
    DATA_TYPE.addAnnotation(new TLcdHasGeometryAnnotation(IMAGE_PROPERTY));
  }

  public HasAImage() {
    super(DATA_TYPE);
  }

  public ALcdImage getImage() {
    return (ALcdImage)getValue(IMAGE_PROPERTY);
  }

  public void setImage(ALcdImage aImage) {
    setValue(IMAGE_PROPERTY, aImage);
  }

  @Override
  public ILcdBounds getBounds() {
    ALcdImage image = getImage();
    return image == null ? new TLcdLonLatBounds() : image.getBounds();
  }
}

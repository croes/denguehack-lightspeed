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
package samples.gxy.decoder.custom1;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Enumeration;

import com.luciad.io.TLcdFileOutputStreamFactory;
import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelEncoder;
import com.luciad.shape.ILcdPoint;
import com.luciad.shape.ILcdPointList;
import com.luciad.shape.shape2D.ILcd2DEditableCircle;
import com.luciad.shape.shape2D.ILcd2DEditablePoint;
import com.luciad.shape.shape2D.ILcd2DEditablePolygon;
import com.luciad.shape.shape2D.ILcd2DEditablePolyline;
import com.luciad.shape.shape3D.ILcd3DEditablePoint;
import com.luciad.util.service.LcdService;

/**
 * This ILcdModelEncoder encodes data to a format created for demonstration
 * purposes. Only ILcd2DEditablePoint, ILcd3DEditablePoint,
 * ILcd2DEditablePolyline, ILcd2DEditablePolygon and ILcd2DEditableCircle
 * objects are encoded.
 */
@LcdService
public class Custom1ModelEncoder
    implements ILcdModelEncoder {

  static final String HEADER_CHECK = "#SimpleDecoderEncoderV1.0";

  private TLcdFileOutputStreamFactory fOutputStreamFactory = new TLcdFileOutputStreamFactory();

  public Custom1ModelEncoder() {
  }

  public String getDisplayName() {
    return "Custom1ModelEncoder";
  }

  protected void write2DEditablePoint(ILcdPoint aPoint, BufferedWriter aBufferedWriter)
      throws IOException {
    aBufferedWriter.write("" + aPoint.getX() +
                          " " + aPoint.getY());
    aBufferedWriter.newLine();
  }

  protected void write3DEditablePoint(ILcdPoint aPoint, BufferedWriter aBufferedWriter)
      throws IOException {
    aBufferedWriter.write("" + aPoint.getX() +
                          " " + aPoint.getY() +
                          " " + aPoint.getZ());
    aBufferedWriter.newLine();
  }

  protected void write2DEditablePointList(ILcdPointList aPointList, BufferedWriter aBufferedWriter)
      throws IOException {
    aBufferedWriter.write("" + aPointList.getPointCount());
    aBufferedWriter.newLine();
    for (int index = 0; index < aPointList.getPointCount(); index++) {
      write2DEditablePoint(aPointList.getPoint(index), aBufferedWriter);
    }
  }

  protected void write2DEditableCircle(ILcd2DEditableCircle a2DEditableCircle, BufferedWriter aBufferedWriter)
      throws IOException {
    write2DEditablePoint(a2DEditableCircle.getCenter(), aBufferedWriter);
    aBufferedWriter.write("" + a2DEditableCircle.getRadius());
    aBufferedWriter.newLine();
  }

  public boolean canExport(ILcdModel aModel, String aDestination) {
    return false;
  }

  public void export(ILcdModel aModel, String aDestination)
      throws IllegalArgumentException, IOException {
    throw new IllegalArgumentException("Cannot export[" + aModel + "]");
  }

  public boolean canSave(ILcdModel aModel) {
    // Can only save the type of model this encoder was created for.
    return aModel.getModelDescriptor().getTypeName().equals(Custom1ModelDecoder.TYPE_NAME);
  }

  public void save(ILcdModel aModel)
      throws IllegalArgumentException, IOException {

    if (!canSave(aModel)) {
      throw new IllegalArgumentException(
          "Cannot save [" + aModel + "]. Need type name[" + Custom1ModelDecoder.TYPE_NAME + "], " +
          "but is [" + aModel.getModelDescriptor().getTypeName() + "]");
    }

    OutputStream source_output_stream = fOutputStreamFactory.createOutputStream(
        aModel.getModelDescriptor().getSourceName());

    BufferedWriter buffered_writer =
        new BufferedWriter(new OutputStreamWriter(source_output_stream));

    try {
      buffered_writer.write(HEADER_CHECK);
      buffered_writer.newLine();
      Object element;
      for (Enumeration e = aModel.elements(); e.hasMoreElements(); ) {
        element = e.nextElement();
        if (element instanceof ILcd3DEditablePoint) {
          buffered_writer.write("point3d");
          buffered_writer.newLine();
          write3DEditablePoint((ILcdPoint) element, buffered_writer);
        } else if (element instanceof ILcd2DEditablePoint) {
          buffered_writer.write("point2d");
          buffered_writer.newLine();
          write2DEditablePoint((ILcdPoint) element, buffered_writer);
        }
// ...
        else if (element instanceof ILcd2DEditableCircle) {
          buffered_writer.write("circle2d");
          buffered_writer.newLine();
          write2DEditableCircle((ILcd2DEditableCircle) element, buffered_writer);
        } else if (element instanceof ILcd2DEditablePolyline) {
          buffered_writer.write("polyline2d");
          buffered_writer.newLine();
          write2DEditablePointList((ILcdPointList) element, buffered_writer);
        } else if (element instanceof ILcd2DEditablePolygon) {
          buffered_writer.write("polygon2d");
          buffered_writer.newLine();
          write2DEditablePointList((ILcdPointList) element, buffered_writer);
        }
      }
      buffered_writer.write("EOF");
      buffered_writer.flush();
    } finally {
      source_output_stream.close();
    }
  }
}

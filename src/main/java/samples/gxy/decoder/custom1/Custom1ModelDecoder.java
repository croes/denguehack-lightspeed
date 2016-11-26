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

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.StringTokenizer;

import com.luciad.io.TLcdInputStreamFactory;
import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelDecoder;
import com.luciad.model.ILcdModelReference;
import com.luciad.model.TLcd2DBoundsIndexedModel;
import com.luciad.model.TLcdModelDescriptor;
import com.luciad.reference.ILcdGeoReference;
import com.luciad.reference.ILcdGridReference;
import com.luciad.reference.TLcdModelReferenceDecoder;
import com.luciad.shape.ILcdShape;
import com.luciad.shape.shape2D.*;
import com.luciad.shape.shape3D.ILcd3DEditablePoint;
import com.luciad.shape.shape3D.TLcdLonLatHeightPoint;
import com.luciad.shape.shape3D.TLcdXYZPoint;
import com.luciad.util.ILcdFireEventMode;
import com.luciad.util.service.LcdService;

/**
 * This ILcdModelDecoder implementation decodes data in a format created for
 * demonstration purposes.
 * Only points, polylines, polygons and circles are decoded.
 */
@LcdService
public class Custom1ModelDecoder
    implements ILcdModelDecoder {

  public static final String EXTENSION = ".ctm";
  public static final String TYPE_NAME = "custom1";

  private static final int GRID = 0;
  private static final int GEODETIC = 1;

  private final TLcdInputStreamFactory fInputStreamFactory = new TLcdInputStreamFactory();

  public Custom1ModelDecoder() {
  }

  @Override
  public String getDisplayName() {
    return "Custom1 shapes";
  }

  @Override
  public boolean canDecodeSource(String aSourceName) {
    if (aSourceName == null || !aSourceName.endsWith(EXTENSION)) {
      return false;
    }
    InputStream in = null;
    try {
      in = fInputStreamFactory.createInputStream(aSourceName);

      BufferedReader buffered_reader =
          new BufferedReader(new InputStreamReader(in));

      String header = buffered_reader.readLine();
      return Custom1ModelEncoder.HEADER_CHECK.equalsIgnoreCase(header);
    } catch (Exception ex) {
      // We can't decode this file
      return false;
    } finally {
      try {
        if (in != null) {
          in.close();
        }
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }


  @Override
  public ILcdModel decode(String aSourceName) throws IOException {
    InputStream in = fInputStreamFactory.createInputStream(aSourceName);
    BufferedReader buffered_reader = new BufferedReader(new InputStreamReader(in));

    TLcd2DBoundsIndexedModel model = new TLcd2DBoundsIndexedModel();

    // Decode the model reference (.ref file).
    // This is required to create the correct type of shape.
    TLcdModelReferenceDecoder ref_decoder = new TLcdModelReferenceDecoder();
    ILcdModelReference ref = ref_decoder.decodeModelReference(aSourceName);
    model.setModelReference(ref);
    model.setModelDescriptor(new TLcdModelDescriptor(aSourceName,
                                                     TYPE_NAME,
                                                     getDisplayName()));

    int reference_type;
    if (ref instanceof ILcdGridReference) {
      reference_type = GRID;
    } else {
      reference_type = GEODETIC;
    }

    ILcdShape read_shape = null;
    boolean eof;

    // read the file header
    buffered_reader.readLine();

    String line = buffered_reader.readLine();
    eof = (line == null) || (line.equalsIgnoreCase("eof"));
    while (!eof) {
      try {
        if (line.equalsIgnoreCase("polyline2d")) {
          read_shape = readPolyline(buffered_reader, reference_type);
        } else if (line.equalsIgnoreCase("polygon2d")) {
          read_shape = readPolygon(buffered_reader, reference_type);
        } else if (line.equalsIgnoreCase("circle2d")) {
          read_shape = readCircle(buffered_reader, reference_type, (ILcdGeoReference) ref);
        } else if (line.equalsIgnoreCase("point2d")) {
          read_shape = read2DEditablePoint(buffered_reader, reference_type);
        } else if (line.equalsIgnoreCase("point3d")) {
          read_shape = read3DEditablePoint(buffered_reader, reference_type);
// ...
        }
        line = buffered_reader.readLine();
        eof = (line == null) || (line.equalsIgnoreCase("eof"));
      } catch (EOFException ex) {
        eof = true;
      }
      if (read_shape != null) {
        model.addElement(read_shape, ILcdFireEventMode.NO_EVENT);
      }
    }
    in.close();
    model.setModelEncoder(new Custom1ModelEncoder());
    return model;
  }

  protected ILcd2DEditablePoint read2DEditablePoint(BufferedReader aBufferedReader, int aReferenceType)
      throws IOException {
    String line;
    line = aBufferedReader.readLine();
    StringTokenizer string_tokenizer = new StringTokenizer(line);
    double x = Double.parseDouble(string_tokenizer.nextToken());
    double y = Double.parseDouble(string_tokenizer.nextToken());
    if (aReferenceType == GRID) {
      return new TLcdXYPoint(x, y);
    } else {
      return new TLcdLonLatPoint(x, y);
    }
  }

  protected ILcd3DEditablePoint read3DEditablePoint(BufferedReader aBufferedReader, int aReferenceType)
      throws IOException {
    String line;
    line = aBufferedReader.readLine();
    StringTokenizer string_tokenizer = new StringTokenizer(line);
    double x = Double.parseDouble(string_tokenizer.nextToken());
    double y = Double.parseDouble(string_tokenizer.nextToken());
    double z = Double.parseDouble(string_tokenizer.nextToken());
    if (aReferenceType == GRID) {
      return new TLcdXYZPoint(x, y, z);
    } else {
      return new TLcdLonLatHeightPoint(x, y, z);
    }
  }

  protected ILcd2DEditablePolyline readPolyline(BufferedReader aBufferedReader, int aReferenceType)
      throws IOException {
    ILcd2DEditablePointList point_list = read2DEditablePointList(aBufferedReader, aReferenceType);
    if (aReferenceType == GRID) {
      return new TLcdXYPolyline(point_list);
    } else {
      return new TLcdLonLatPolyline(point_list);
    }
  }

  protected ILcd2DEditablePolygon readPolygon(BufferedReader aBufferedReader, int aReferenceType)
      throws IOException {
    ILcd2DEditablePointList point_list = read2DEditablePointList(aBufferedReader, aReferenceType);
    if (aReferenceType == GEODETIC) {
      return new TLcdLonLatPolygon(point_list);
    } else {
      return new TLcdXYPolygon(point_list);
    }
  }

  protected ILcd2DEditablePointList read2DEditablePointList(BufferedReader aBufferedReader, int aReferenceType)
      throws IOException {
    String line = aBufferedReader.readLine();
    int n_point = Double.valueOf(line).intValue();
    ILcd2DEditablePoint[] point_array = new ILcd2DEditablePoint[n_point];
    for (int index = 0; index < n_point; index++) {
      point_array[index] = read2DEditablePoint(aBufferedReader, aReferenceType);
    }
    return new TLcd2DEditablePointList(point_array, false);
  }

  protected ILcd2DEditableCircle readCircle(BufferedReader aBufferedReader, int aReferenceType, ILcdGeoReference aGeoReference)
      throws IOException {
    ILcd2DEditablePoint center = read2DEditablePoint(aBufferedReader, aReferenceType);
    String line = aBufferedReader.readLine();
    double radius = Double.parseDouble(line);
    if (aReferenceType == GRID) {
      return new TLcdXYCircle(center, radius);
    } else {
      return new TLcdLonLatCircle(center, radius, aGeoReference.getGeodeticDatum().getEllipsoid());
    }
  }
}


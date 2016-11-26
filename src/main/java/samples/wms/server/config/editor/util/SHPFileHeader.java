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
package samples.wms.server.config.editor.util;

import java.io.BufferedInputStream;
import java.io.IOException;

import com.luciad.io.TLcdDataInputStream;
import com.luciad.io.TLcdIOUtil;
import com.luciad.util.logging.ILcdLogger;
import com.luciad.util.logging.TLcdLoggerFactory;

/**
 * A class that reads in the header of an ESRI Shape file. It can be used to determine the type of
 * the shapes without having to decode the whole file.
 */
public final class SHPFileHeader {

  private static ILcdLogger sLogger = TLcdLoggerFactory.getLogger(SHPFileHeader.class.getName());

  public static final int SHP_NULL = 0;
  public static final int SHP_POINT = 1;
  public static final int SHP_POLYLINE = 3;
  public static final int SHP_POLYGON = 5;
  public static final int SHP_MULTIPOINT = 8;
  public static final int SHP_POINTZ = 11;
  public static final int SHP_POLYLINEZ = 13;
  public static final int SHP_POLYGONZ = 15;
  public static final int SHP_MULTIPOINTZ = 18;
  public static final int SHP_POINTM = 21;
  public static final int SHP_POLYLINEM = 23;
  public static final int SHP_POLYGONM = 25;
  public static final int SHP_MULTIPOINTM = 28;
  public static final int SHP_MULTIPATCH = 31;

  private int fCode;
  private int fFileLength;
  private int fVersion;
  private int fShapeType;
  private double fXMin;
  private double fXMax;
  private double fYMin;
  private double fYMax;
  private double fZMin;
  private double fZMax;
  private double fMMin;
  private double fMMax;

  public SHPFileHeader(String aSource) {

    try {
      TLcdIOUtil ioutil = new TLcdIOUtil();
      ioutil.setSourceName(aSource);
      TLcdDataInputStream dis = new TLcdDataInputStream(new BufferedInputStream(ioutil.retrieveInputStream()));
      fCode = dis.readBIInt();
      for (int i = 0; i < 5; i++) {
        dis.readBIInt(); // unused
      }
      fFileLength = dis.readBIInt();
      fVersion = dis.readLIInt();
      fShapeType = dis.readLIInt();
      fXMin = dis.readLIDouble();
      fYMin = dis.readLIDouble();
      fXMax = dis.readLIDouble();
      fYMax = dis.readLIDouble();
      fZMin = dis.readLIDouble();
      fZMax = dis.readLIDouble();
      fMMin = dis.readLIDouble();
      fMMax = dis.readLIDouble();
      dis.close();
    } catch (IOException e) {
      sLogger.trace("Note: " + e.getMessage());
    }
  }

  public int getCode() {
    return fCode;
  }

  public int getFileLength() {
    return fFileLength;
  }

  public int getVersion() {
    return fVersion;
  }

  public int getShapeType() {
    return fShapeType;
  }

  public double getXMin() {
    return fXMin;
  }

  public double getXMax() {
    return fXMax;
  }

  public double getYMin() {
    return fYMin;
  }

  public double getYMax() {
    return fYMax;
  }

  public double getZMin() {
    return fZMin;
  }

  public double getZMax() {
    return fZMax;
  }

  public double getMMin() {
    return fMMin;
  }

  public double getMMax() {
    return fMMax;
  }
}

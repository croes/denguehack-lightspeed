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
package samples.lightspeed.demo.framework.data;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import javax.imageio.ImageIO;

import com.luciad.io.TLcdIOUtil;
import com.luciad.io.TLcdInputStreamFactory;
import com.luciad.util.logging.ILcdLogger;
import com.luciad.util.logging.TLcdLoggerFactory;

/**
 * Utility class for IO in the Lightspeed demo.
 * <p/>
 * This class offers 2 approaches for IO:
 * <ul>
 *   <li>String based: this approach should be preferred since it also supports reading from a jar</li>
 *   <li>File based: use {@link #getFile} to resolve the file from the class path</li>
 * </ul>
 */
public class IOUtil {

  private static final ILcdLogger sLogger = TLcdLoggerFactory.getLogger(IOUtil.class);

  public static File getFile(String aDirectory, String aFileName) {
    String sourceName = getSourceName(aDirectory, aFileName);
    return getFile(sourceName);
  }

  public static File getFile(String aSourceName) {
    assert aSourceName != null;
    TLcdIOUtil ioUtil = new TLcdIOUtil();
    ioUtil.setSourceName(aSourceName);
    String fileName = ioUtil.getFileName();
    if (fileName != null) {
      return new File(fileName);
    }
    URL url = ioUtil.getURL();
    if (url != null) {
      try {
        URI uri = url.toURI();
        if (uri.getScheme().equalsIgnoreCase("file")) {
          return new File(uri);
        }
      } catch (URISyntaxException ignored) {
        sLogger.warn("Could not resolve file for url [" + url + "]");
      } catch (IllegalArgumentException ignored) {
        sLogger.warn("Could not resolve file for url [" + url + "]");
      }
    }
    return new File(aSourceName);
  }

  public static BufferedReader createReader(String aDirectory, String aFileName) throws IOException {
    return createReader(getSourceName(aDirectory, aFileName));
  }

  /**
   * Creates a reader for the given file.
   *
   * @param aFile the file to open
   *
   * @return a BufferedReader
   *
   * @throws IOException if the file cannot be opened
   */
  public static BufferedReader createReader(File aFile) throws IOException {
    return new BufferedReader(new InputStreamReader(createInputStream(aFile)));
  }

  public static BufferedReader createReader(String aSource) throws IOException {
    return new BufferedReader(new InputStreamReader(createInputStream(aSource)));
  }

  public static BufferedWriter createWriter(String aSource) throws IOException {
    return new BufferedWriter(new FileWriter(getFile(aSource)));
  }

  public static BufferedWriter createWriter(File aFile) throws IOException {
    return new BufferedWriter(new FileWriter(aFile));
  }

  public static InputStream createInputStream(String aSource) throws IOException {
    return new TLcdInputStreamFactory().createInputStream(aSource);
  }

  public static InputStream createInputStream(File aFile) throws IOException {
    return new BufferedInputStream(new FileInputStream(aFile));
  }

  public static BufferedImage readImage(String aDirectory, String aFileName) throws IOException {
    InputStream isf = createInputStream(getSourceName(aDirectory, aFileName));
    try {
      return ImageIO.read(isf);
    } finally {
      isf.close();
    }
  }

  public static BufferedImage readImage(String aSource) throws IOException {
    InputStream isf = createInputStream(aSource);
    try {
      return ImageIO.read(isf);
    } finally {
      isf.close();
    }
  }

  public static String getSourceName(String aDirectory, String aFileName) {
    assert aFileName != null;
    return aDirectory != null ? aDirectory + "/" + aFileName : aFileName;
  }

  public static String resolveSourceName(String aSourceName) {
    return getFile(aSourceName).getPath();
  }
}

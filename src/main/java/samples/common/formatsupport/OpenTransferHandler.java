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
package samples.common.formatsupport;

import com.luciad.util.TLcdSystemPropertiesUtil;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.swing.TransferHandler;

/**
 * Transfer handler that supports dropping of files. Dropped files will be decoded
 * using the OpenSupport instance that is passed to the constructor.
 */
public class OpenTransferHandler extends TransferHandler {
  private static final DataFlavor URI_LIST_FLAVOR;

  static {
    try {
      URI_LIST_FLAVOR = new DataFlavor("text/uri-list;class=java.lang.String");
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  private OpenSupport fOpenSupport;

  public OpenTransferHandler(OpenSupport aOpenSupport) {
    fOpenSupport = aOpenSupport;
  }

  @Override
  public boolean canImport(TransferSupport support) {
    return support.isDataFlavorSupported(DataFlavor.javaFileListFlavor) ||
           support.isDataFlavorSupported(DataFlavor.stringFlavor) ||
           support.isDataFlavorSupported(URI_LIST_FLAVOR);
  }

  @Override
  public boolean importData(TransferSupport support) {
    if (!canImport(support)) {
      return false;
    }

    try {
      for (String source : getSources(support.getTransferable())) {
        fOpenSupport.openSource(source, null);
      }

      return true;
    } catch (UnsupportedFlavorException e) {
      return false;
    } catch (IOException e) {
      return false;
    }
  }

  @SuppressWarnings({"unchecked"})
  private List<String> getSources(Transferable aTransferable) throws UnsupportedFlavorException, IOException {
    if (aTransferable.isDataFlavorSupported(URI_LIST_FLAVOR)) {
      return extractSources((String) aTransferable.getTransferData(URI_LIST_FLAVOR));
    }
    if (aTransferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
      return extractSources((List<File>) aTransferable.getTransferData(DataFlavor.javaFileListFlavor));
    }
    if (aTransferable.isDataFlavorSupported(DataFlavor.stringFlavor)) {
      return singletonList(aTransferable.getTransferData(DataFlavor.stringFlavor).toString());
    }
    return emptyList();
  }

  private List<String> extractSources(List<File> aFiles) {
    List<String> sources = new ArrayList<String>();
    for (File file : aFiles) {
      sources.add(file.getAbsolutePath());
    }

    return sources;
  }

  private static List<String> extractSources(String aURIData) {
    List<String> sources = new ArrayList<String>();
    BufferedReader r = new BufferedReader(new StringReader(aURIData));
    try {
      String line;
      while ((line = r.readLine()) != null) {
        line = line.trim();
        if (line.startsWith("#") || line.length() == 0) {
          continue;
        }
        String source;
        //if a Mac OS file path has spaces we should replace it with %20 before
        //creating the URI
        if(TLcdSystemPropertiesUtil.isMacOS() && line.contains(" ")){
          source = URI.create(line.replace(" ", "%20")).toASCIIString().replace("%20", " ");
        }else {
          URI uri = URI.create(line);
          source= uri.toASCIIString();
        }
        sources.add(source);
      }
    } catch (IOException e) {
      // Ignored
    } finally {
      try {
        r.close();
      } catch (IOException e) {
        // Ignored
      }
    }

    return sources;
  }

}

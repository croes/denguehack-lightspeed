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

import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import com.luciad.io.TLcdIOUtil;

/**
 * This static class reads icons from disk and keeps them in a cache. The
 * returned icons are javax.swing.ImageIcon objects.
 */
public final class GUIIconProvider {

  private static Map fIconCache = new Hashtable();

  private GUIIconProvider() {
    // No need to instantiate this class, as all utility methods are static.
  }

  public static Icon getIcon(String aSource) {
    Icon cached = (Icon) fIconCache.get(aSource);

    if (cached == null) {
      try {
        TLcdIOUtil io = new TLcdIOUtil();
        io.setSourceName(aSource);
        InputStream is = io.retrieveInputStream();

        byte buf[] = new byte[is.available()];
        is.read(buf);

        cached = new ImageIcon(buf);
        fIconCache.put(aSource, cached);
      } catch (IOException ioe) {
        cached = new ImageIcon();
      }
    }

    return cached;
  }
}

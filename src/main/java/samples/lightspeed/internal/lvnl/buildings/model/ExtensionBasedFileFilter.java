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
package samples.lightspeed.internal.lvnl.buildings.model;

import java.io.File;

import javax.swing.filechooser.FileFilter;

/**
 * Date: Jan 25, 2007
 * Time: 8:55:29 AM
 *
 * @author Tom Nuydens
 */
class ExtensionBasedFileFilter extends FileFilter {
  private final String[] fExtensions;
  private final String fDescription;

  public ExtensionBasedFileFilter(String aDescription, String[] aExtensions) {
    fDescription = aDescription;
    fExtensions = aExtensions.clone();
  }

  public String getDescription() {
    return fDescription;
  }

  public final boolean accept(File f) {
    if (f.isDirectory()) {
      return true;
    }

    String name = f.getName();
    for (String supportedExtension : fExtensions) {
      if (name.length() >= supportedExtension.length()) {
        if (name.substring(name.length() - supportedExtension.length()).equalsIgnoreCase(supportedExtension)) {
          return true;
        }
      }
    }

    return false;
  }
}

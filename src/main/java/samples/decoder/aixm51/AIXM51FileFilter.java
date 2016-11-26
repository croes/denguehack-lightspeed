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
package samples.decoder.aixm51;

import javax.swing.filechooser.FileFilter;
import java.io.File;

/**
 * Extension of <code>FileFilter</code> that accepts
 * files with typical AIXM5.1 extensions.
 */
public class AIXM51FileFilter extends FileFilter {

  public static final String DEFAULT_EXTENSION = "xml";
  private static final String[] ALL_EXTENSIONS = {"xml", "aixm51","aixm"};

  public boolean accept(File aFile) {
    if (aFile.isDirectory()) {
      return true;
    }
    else {
      String extension = getExtension( aFile );
      for ( String defaultExtension : ALL_EXTENSIONS ) {
        if(defaultExtension.equals( extension )){
          return true;
        }
      }
      return false;
    }
  }

  public String getDescription() {
    String description = "AIXM 5.1 (";
    for ( String defaultExtension : ALL_EXTENSIONS ) {
      description += "*."+defaultExtension+", ";
    }
    description = description.substring( 0,description.length()-2 );
    description +=  ")";
    return description;
  }

  /**
   * Returns the extension of a file,
   * converted to lowercase.
   *
   * @param aFile a file
   * @return the extension of a file converted to lower case.
   */
  public static String getExtension( File aFile ) {
    String extension = null;
    String fileName = aFile.getName();
    int i = fileName.lastIndexOf('.');

    if (i > 0 && i < fileName.length() - 1) {
      extension = fileName.substring(i + 1).toLowerCase();
    }
    return extension;
  }

}

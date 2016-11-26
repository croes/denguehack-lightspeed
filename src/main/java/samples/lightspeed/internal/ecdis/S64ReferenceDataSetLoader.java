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
package samples.lightspeed.internal.ecdis;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import com.luciad.format.s52.TLcdS52DisplaySettings;

/**
 * Retrieve the files + predefined settings from the S64 reference data set
 *
 * @since 2013.1
 */
final class S64ReferenceDataSetLoader {
  static final String ECDIS_DATA_DIR = System.getProperty("data2.directory", "server1 Data: use system property data2.directory") + "/ECDIS/";
  static final String ECDIS_TEST_DATA_DIR = System.getProperty("data.directory", "server1 TestData: use system property data.directory") + "/ECDIS/";
  static final String S_64_DATA_DIR = ECDIS_DATA_DIR + "S64/";

  private static final List<S64ReferenceDataSet> DATA_SETS = new ArrayList<S64ReferenceDataSet>();

  public static List<S64ReferenceDataSet> getDataSets() {
    if (DATA_SETS.isEmpty()) {
      try {
        initDataSets();
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
    return Collections.unmodifiableList(DATA_SETS);
  }

  private static void initDataSets() throws IOException {
    final String referencePDFsBaseDir = S_64_DATA_DIR + "ECDIS_Test_Data_Sets/ENC_Test_Data_Sets/ENC_TDS_Plots/";
    File root = new File(ECDIS_TEST_DATA_DIR + "ENC/S64/Data/GOODB1/ENC_ROOT");
    File[] propertyFiles = root.listFiles(new FilenameFilter() {
      @Override
      public boolean accept(File dir, String name) {
        return name.endsWith("s64");
      }
    });

    for (int i = 0; i < propertyFiles.length; i++) {
      File propertyFile = propertyFiles[i];
      Properties properties = new Properties();
      InputStream is = null;
      try {
        is = new FileInputStream(propertyFile.getAbsolutePath());
        properties.load(is);
      } finally {
        is.close();
      }
      TLcdS52DisplaySettings displaySettings = new TLcdS52DisplaySettings();
      S52PropertiesCodec.fromProperties(properties, displaySettings);
      DATA_SETS.add(new S64ReferenceDataSet(propertyFile.getName(),
                                            Collections.singletonList(new File(propertyFile.getParent(), properties.getProperty("source")).getAbsolutePath()),
                                            referencePDFsBaseDir + "S57ed3_1 S52ed3_3 PLOT " + (i + 1) + ".pdf",
                                            displaySettings));
    }
  }

  public static final class S64ReferenceDataSet {
    private final String fDisplayName;
    public final List<String> fSourceFiles;
    public final String fReferencePDFSOurceFile;
    public final TLcdS52DisplaySettings fDisplaySettings;

    public S64ReferenceDataSet(String aDisplayName, List<String> aSourceFiles, String aReferencePDFSOurceFile, TLcdS52DisplaySettings aDisplaySettings) {
      fDisplayName = aDisplayName;
      fSourceFiles = aSourceFiles;
      fReferencePDFSOurceFile = aReferencePDFSOurceFile;
      fDisplaySettings = aDisplaySettings;
    }

    @Override
    public String toString() {
      return fDisplayName;
    }
  }
}

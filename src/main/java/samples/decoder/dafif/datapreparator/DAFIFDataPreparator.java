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
package samples.decoder.dafif.datapreparator;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.JFileChooser;

import com.luciad.ais.model.TLcdAISDataObjectFactory;
import com.luciad.ais.model.airspace.ILcdAirspace;
import com.luciad.ais.model.parachutejumparea.ILcdParachuteJumpArea;
import com.luciad.ais.model.refuelingtrack.ILcdRefuelingAirspace;
import com.luciad.format.dafif.decoder.TLcdDAFIFAirspaceDecoder;
import com.luciad.format.dafif.decoder.TLcdDAFIFModelDecoderSupport;
import com.luciad.format.dafif.decoder.TLcdDAFIFParachuteJumpAreaDecoder;
import com.luciad.format.dafif.decoder.TLcdDAFIFRefuelingTrackDecoder;
import com.luciad.format.dafif.decoder.TLcdDAFIFSpecialUseAirspaceDecoder;
import com.luciad.format.dafif.model.airspace.ILcdDAFIFAirspaceFeature;
import com.luciad.format.dafif.model.parachutejumparea.ILcdDAFIFParachuteJumpAreaFeature;
import com.luciad.format.dafif.model.refuelingtrack.ILcdDAFIFRefuelingAirspaceFeature;
import com.luciad.format.dafif.util.TLcdDAFIFLonLatParser;
import com.luciad.io.TLcdIOUtil;
import com.luciad.model.ILcdFeatureIndexedAnd2DBoundsIndexedModel;
import com.luciad.model.TLcdModelList;
import com.luciad.shape.ILcdBounds;
import com.luciad.shape.ILcdPoint;
import com.luciad.shape.shape2D.TLcdLonLatBounds;
import com.luciad.shape.shape2D.TLcdLonLatPoint;
import com.luciad.util.ILcdStatusListener;
import com.luciad.util.ILcdStatusSource;
import com.luciad.util.TLcdStatusEvent;
import com.luciad.util.logging.ILcdLogger;
import com.luciad.util.logging.TLcdLoggerFactory;

/**
 * A tool to split large DAFIF files into smaller ones. The division of the DAFIF data
 * is based on the two letter ICAO region found in each record.
 * <p/>
 * Given a directory containing one or more DAFIF files, the <code>TLcdDAFIFDataPreparatorMain</code> class
 * will create a directory tree. The the top directory, that should be specified in the config file,
 * will contain a subdirectory for each ICAO region found in the source data.
 * Each subdirectory contains
 * <ul>
 * <li>the records having the ICAO region mentioned in the subdirectory's name, organized in files having the
 * same names as the source files,</li>
 * <li>for each data file a matching .bnd file containing the bounds surrounding the data in the data file,</li>
 * <li>a file named dafif_&lt;ICAO region&gt;.toc, containing the relationship between the model objects and
 * their data file names: source.&lt;model object name&gt;File=&lt;filename&gt;</li>
 * </ul>
 * <p/>
 * Besides reorganizing a given data set and calculating bounds, the <code>TLcdDAFIFDataPreparatorMain</code> class
 * also writes a dafif.toc file into the top directory.
 * The <code>dafif.toc</code> file is a properties file containing:
 * <ul>
 * <li>ICAO.regioni=&lt;ICAO code&gt; i=0 .. number of ICAO regions : an enumeration of
 * all ICAO regions (subdirectories),</li>
 * <li>source.&lt;model object name&gt;File=&lt;filename&gt; : for each model object
 * the name of its data file.</li>
 * </ul>
 * <p/>
 * The result of running the <code>TLcdDAFIFDataPreparatorMain</code> class  will look like this:
 * <pre>
 *     - &lt;split_directory&gt;
 *            - AG
 *              FILE0
 *              FILE0.bnd
 *              FILE2
 *              FILE2.bnd
 *              ...
 *              dafif_AG.toc
 *            - AN
 *              FILE0
 *              FILE0.bnd
 *              FILE2
 *              FILE2.bnd
 *              ...
 *              dafif_AN.toc
 *            ...
 *            dafif.toc
 * </pre>
 * <p/>
 * Important remark:
 * If the <code>TLcdDAFIFDataPreparatorMain</code> class is used to split data in a source directory,
 * it will always look for a dafif.toc file in that source directory. The dafif.toc file in the source
 * directory is slightly different from the one in the splitting result. It only contains the filename for
 * each model object: source.&lt;model object name&gt;File=&lt;filename&gt;.
 * If the source directory does not contain a dafif.toc file, the default file names for DAFIF data will be
 * used (e.g. FILE0 for aerodromes, FILE1 for helipads, ...).
 * In other words, if the file names in the source directory differ from the default files, there should
 * be a dafif.toc file specifying the right names.
 * <p/>
 * Limitations of the splitter: All entities, such as waypoints and routes, are split independently.
 * This means, for example, that when
 * a certain route depends on waypoints not in its own ICAO region, this waypoint will not
 * be available in the waypoint file for this region, and the affected route segments
 * will be omitted from the route.
 */
public class DAFIFDataPreparator implements ILcdStatusSource {

private static ILcdLogger sLogger = TLcdLoggerFactory.getLogger(DAFIFDataPreparator.class.getName());
  private static boolean sWriteBounds = true;
  private static final int RECORD_LENGTH = 142;
  private static final String DAFIF_CONFIG_FILE_NAME = "dafif.toc";

  private Properties fProperties;
  private Properties fSourceDAFIFProperties = null;
  private String fOutputDirectory;
  private File fInputDirectory;

  //key: file name, value: hashtable (with key: ICAO code,  value: TLcdLonLatBounds object)
  private Hashtable<String, Hashtable<String, TLcdLonLatBounds>> fICAOBounds = new Hashtable<String, Hashtable<String, TLcdLonLatBounds>>();
  private Properties fTopTargetDAFIFProperties;  //properties to be written in the top dir
  private Properties fSubTargetDAFIFProperties;  //properties to be written in each sub dir
  private List<String> fICAORegions;

  private ILcdFeatureIndexedAnd2DBoundsIndexedModel fAirspaceModel;
  private List<String> fAirspaceKeyFeatureNames;
  private String [] fAirspaceKeyFeatureValues = new String[1];

  private ILcdFeatureIndexedAnd2DBoundsIndexedModel fSUASModel;
  private List<String> fSUASKeyFeatureNames;
  private String [] fSUASKeyFeatureValues = new String[2];

  private ILcdFeatureIndexedAnd2DBoundsIndexedModel fRefuelingAirspaceModel;
  private List<String> fRefuelingAirspaceKeyFeatureNames;
  private String [] fRefuelingAirspaceKeyFeatureValues = new String[2];

  private ILcdFeatureIndexedAnd2DBoundsIndexedModel fParachuteJumpAreaAirspaceModel;
  private List<String> fParachuteJumpAreaAirspaceKeyFeatureNames;
  private String [] fParachuteJumpAreaAirspaceKeyFeatureValues = new String[1];

  private String fDecoderErrorMessages;

  private final CopyOnWriteArrayList<ILcdStatusListener> fStatusListeners = new CopyOnWriteArrayList<ILcdStatusListener>();

  /**
   * Adds an <code>ILcdStatusListener</code>, which will be triggered with
   * progress messages during the data preparation process.
   *
   * @param aListener an <code>ILcdStatusListener</code>
   */
  @Override
  public void addStatusListener(ILcdStatusListener aListener) {
    fStatusListeners.add( aListener );
  }

  /**
   * Removes the specified status listener.
   *
   * @param aListener an <code>ILcdStatusListener</code>
   */
  @Override
  public void removeStatusListener(ILcdStatusListener aListener) {
    fStatusListeners.remove( aListener );
  }

  /**
   * Sends a progress message to the registered status listeners.
   */
  private void sendStatusMessage( String aMessage ) {
    for (ILcdStatusListener statusListener : fStatusListeners) {
      TLcdStatusEvent.sendMessage(statusListener, this, aMessage, TLcdStatusEvent.Severity.INFO);
    }
  }

  //public methods

  /**
   * Separates all DAFIF data files found in the source directory
   * over a set of subdirectories. Each subdirectory contains the
   * data located in a certain ICAO region.
   * <p/>
   * The data in each subdirectory is organized in files like the data
   * in the source directory (same file names are used).
   * <p/>
   * In addition to data files, the subdirectories will contain .bnd files and .toc files
   * (there is one .bnd file for each data file and only one .toc file for each subdirectory).
   * The .bnd file contains the bounds of the matching data file.
   * The .toc file contains the relationship between model objects and data file names.
   * <p/>
   * Finally, this method will add a <code>dafif.toc</code> file in the
   * destination directory.
   */
  public void prepareData() {
    fTopTargetDAFIFProperties = new Properties();
    fSubTargetDAFIFProperties = new Properties();
    fICAORegions = new ArrayList<String>();
    setupDirectories();
    String dafif_config_path = getPath( fInputDirectory.getAbsolutePath(), DAFIF_CONFIG_FILE_NAME );
    fSourceDAFIFProperties = TLcdDAFIFModelDecoderSupport.getCFGProperties( dafif_config_path );
    setupICAOBounds();
    String[] inputFileNames = getInputFileNames();
    for ( int i = 0; i < inputFileNames.length ; i++ ) {
      String fileName = inputFileNames[ i ];
      sendStatusMessage( "-- Processing file : " + fileName );
      prepareData( fileName );
    }
    writeBndFiles();
    writeSubDirDAFIFPropertiesFiles();
    writeDAFIFPropertiesFile();
  }

  /**
   * Specifies a properties object. This properties object should contain
   * <ul>
   * <li>the source directory or a directory that can be used as initial directory of a file chooser,</li>
   * <li>the destination directory,</li>
   * <li>the deviation used when calculating the bounds stored in the .bnd files.</li>
   * </ul>
   *
   * @param aProperties the properties object to be used during the splitting process
   * @see #main
   */
  public void setProperties( Properties aProperties ) {
    fProperties = aProperties;
  }

  /**
   * Specifies whether or not .bnd files should be generated. Defaults to true.
   * Setting this to false results in a performance increase.
   */
  public static void setWriteBounds( boolean aWriteBounds ) {
    sWriteBounds = aWriteBounds;
  }

  /**
   * Indicates whether or not .bnd files will be generated.
   */
  public static boolean isWriteBounds() {
    return sWriteBounds;
  }

  /**
   * Returns the concatenated error messages of the different decoders used to split the DAFIF data.
   * This message is available (and changed) after each <code>prepareData</code> call.
   */
  public String getDecoderErrorMessages() {
    return fDecoderErrorMessages;
  }

  //private methods

  /**
   * Adds the ICAO region names to the DAFIF
   * properties file of the target directory.
   */
  private void completeDAFIFProperties() {
    for ( int i = 0; i < fICAORegions.size() ; i++ ) {
      fTopTargetDAFIFProperties.put( "ICAO.region" + i, fICAORegions.get( i ) );
    }
  }

  /**
   * Deletes the destination directory. The destination directory has a
   * sub directory for each ICAO region. Each sub directory contains a file
   * for each file type.
   * For each sub directory, all files should be deleted. Then the
   * sub directory itself can be deleted and finally, if all sub directories
   * are deleted, we can remove the destination directory.
   * Note: this method is not recursively written. So if you change the directory
   * structure (you add sub directories in the sub directories for example),
   * then you 'll have to change this method.
   *
   * @param aDirectory
   */
  private boolean deleteOutputDirectory( File aDirectory ) {
    boolean all_deleted = true;
    File[] sub_directories = aDirectory.listFiles();
    if ( sub_directories != null ) {
      for ( int i = 0; i < sub_directories.length ; i++ ) {
        File[] files = sub_directories[ i ].listFiles();
        if ( files != null ) {
          for ( int j = 0; j < files.length ; j++ ) {
            boolean is_file_deleted = files[ j ].delete();
            if ( !is_file_deleted ) {
              all_deleted = is_file_deleted;
            }
          }
        }
        boolean is_sub_deleted = sub_directories[ i ].delete();
        if ( !is_sub_deleted ) {
          all_deleted = is_sub_deleted;
        }
      }
    }
    boolean dir_deleted = aDirectory.delete();
    if ( !dir_deleted ) {
      all_deleted = dir_deleted;
    }
    return all_deleted;
  }

  /**
   * Creates and returns a BufferedWriter to write the fileX.bnd files.
   *
   * @param aDirectoryName
   * @param aFileName
   * @throws IOException
   */
  private BufferedWriter getBndWriter( String aDirectoryName, String aFileName ) throws IOException {
    return new BufferedWriter( new OutputStreamWriter( new FileOutputStream( getPath( aDirectoryName, aFileName + ".bnd" ) ) ) );
  }

  /**
   * Returns a <code>File</code> object pointing to the
   * directory chosen by the user. The user may write
   * this directory in the "fullall_directory" property
   * in the properties file or he may choose it with a
   * fileChooser.
   */
  private File getDirectory() {
    String result_name = fProperties.getProperty( "fullall_directory" );
    if ( result_name != null ) {
      File result = new File( result_name );
      if ( result.exists() ) {
        return result;
      } else {
        sLogger.error("Directory " + result_name + " does not exist!");
      }
    }

    File default_dir = new File( fProperties.getProperty( "filechooser_initial_directory", System.getProperties().getProperty( "user.dir" ) ) );
    JFileChooser file_chooser = new JFileChooser( default_dir );

    file_chooser.setDialogTitle( "Choose a directory whose files have to be split:" );
    file_chooser.setDialogType( JFileChooser.OPEN_DIALOG );
    file_chooser.setFileSelectionMode( JFileChooser.DIRECTORIES_ONLY );

    int return_value = file_chooser.showOpenDialog( null );
    if ( return_value == JFileChooser.APPROVE_OPTION ) {
      return file_chooser.getSelectedFile();
    } else {
      return null;
    }
  }

  /**
   * Returns an array of file names.
   * The array contains an entry for each DAFIF data source file.
   * Remark: We consider source files, not domain objects. Since aerodromes,
   * runways, ILS and procedures are stored in one data file, there will be only
   * one entry for these four domain objects.
   */
  private String[] getInputFileNames() {
    return new String[] {
            TLcdDAFIFModelDecoderSupport.getAerodromeFileName( fSourceDAFIFProperties ),
            TLcdDAFIFModelDecoderSupport.getAirspaceFileName( fSourceDAFIFProperties ),
            TLcdDAFIFModelDecoderSupport.getATSRouteFileName( fSourceDAFIFProperties ),
            TLcdDAFIFModelDecoderSupport.getHelipadFileName( fSourceDAFIFProperties ),
            TLcdDAFIFModelDecoderSupport.getHoldingFileName( fSourceDAFIFProperties ),
            TLcdDAFIFModelDecoderSupport.getMilitaryTrainingRouteFileName( fSourceDAFIFProperties ),
            TLcdDAFIFModelDecoderSupport.getNavaidFileName( fSourceDAFIFProperties ),
            TLcdDAFIFModelDecoderSupport.getOrtcaFileName( fSourceDAFIFProperties ),
            TLcdDAFIFModelDecoderSupport.getParachuteJumpAreaFileName( fSourceDAFIFProperties ),
            TLcdDAFIFModelDecoderSupport.getRefuelingFileName( fSourceDAFIFProperties ),
            TLcdDAFIFModelDecoderSupport.getSpecialUseAirspaceFileName( fSourceDAFIFProperties ),
            TLcdDAFIFModelDecoderSupport.getVFRFileName( fSourceDAFIFProperties ),
            TLcdDAFIFModelDecoderSupport.getWaypointFileName( fSourceDAFIFProperties )
    };
  }

  /**
   * Returns an <code>InputStream</code> object pointing to the file
   * called aFileName in the directory aDir.
   *
   * @param aDir
   * @param aFileName
   * @return
   * @throws IOException
   */
  private InputStream getInputStream( File aDir, String aFileName ) throws IOException {
    if ( aDir == null ) {
      throw new NullPointerException( "The given source is null." );
    }

    String path = getPath( aDir.getAbsolutePath(), aFileName );

    TLcdIOUtil IO_util = new TLcdIOUtil();
    IO_util.setSourceName( path );
    return IO_util.retrieveInputStream();
  }

  /**
   * Returns the concatenation of aDirectoryName and
   * aFileName.
   *
   * @param aDirectoryName
   * @param aFileName
   */
  private String getPath( String aDirectoryName, String aFileName ) {
    String path = aDirectoryName.replace( '\\', '/' );
    if ( path.endsWith( "/" ) ) {
      path += aFileName;
    } else {
      path += "/" + aFileName;
    }
    return path;
  }

  private Hashtable<String, Writer> fOutputFileTable = new Hashtable<String, Writer>();

  private void closeWriters() {
    Enumeration<Writer> elements = fOutputFileTable.elements();
    while ( elements.hasMoreElements() ) {
      Writer w = (Writer) elements.nextElement();
      try {
        w.close();
      }
      catch ( IOException e ) {
        e.printStackTrace();
      }
    }

    // Remove all closed writers
    fOutputFileTable.clear();
  }

  /**
   * Returns a <code>BufferedWriter</code> object.
   * If aDirectoryName is the same as in the previous method call,
   * the content of a buffer (fPreviousWriter) will be returned.
   *
   * @param aDirectoryName
   * @param aFileName
   * @return
   * @throws IOException
   */
  private Writer getWriter( String aDirectoryName, String aFileName ) throws IOException {
    if ( sLogger.isTraceEnabled() ) {
      sLogger.trace( "Getting writer for " + aDirectoryName + aFileName );
    }

    String path = getPath( aDirectoryName, aFileName );

    Writer writer = (Writer) fOutputFileTable.get( path );
    if ( writer == null ) {
      File out_file = new File( aDirectoryName );
      out_file.mkdirs();
      writer = new BufferedWriter(
              new OutputStreamWriter( new FileOutputStream( path, true ) ),
              10000
      );
      fOutputFileTable.put( path, writer );
    }
    return writer;
  }

  /**
   * Reads the file with aDAFIFFileName as file name
   * found in fInputDirectory.
   * For each ICAO region found in the input file,
   * a new directory with name fOutputDirectory+ICAO_region
   * is created.
   * All records with the same ICAO region are written to
   * the file with aDAFIFFileName in this new directory.
   *
   * @param aDAFIFFileName
   */
  private void prepareData( String aDAFIFFileName ) {
    if ( sLogger.isTraceEnabled() ) {
      sLogger.trace(" Preparing data: " + aDAFIFFileName);
    }

    fDecoderErrorMessages = "";

    BufferedReader reader = null;
    try {
      InputStream input_stream = getInputStream( fInputDirectory, aDAFIFFileName );
      reader = new BufferedReader( new InputStreamReader( input_stream ), 10000 );

      // To compute the bounds some of the models need to be decoded.
      if ( isWriteBounds() ) {
        //decode the airspace model if needed
        if ( aDAFIFFileName.equals( TLcdDAFIFModelDecoderSupport.getAirspaceFileName( fSourceDAFIFProperties ) ) ) {
          TLcdDAFIFAirspaceDecoder airspace_decoder = new TLcdDAFIFAirspaceDecoder( new TLcdAISDataObjectFactory(), fSourceDAFIFProperties );
          try {
            fAirspaceModel = (ILcdFeatureIndexedAnd2DBoundsIndexedModel) airspace_decoder.decode( fInputDirectory.getAbsolutePath() );
            fDecoderErrorMessages = fDecoderErrorMessages + "\nAirspace decoder problems:\n" + airspace_decoder.getErrorMessage();
            fAirspaceKeyFeatureNames = Arrays.asList( new String[] { ILcdDAFIFAirspaceFeature.BOUNDARY_IDENTIFICATION } );
            fAirspaceModel.addIndex( fAirspaceKeyFeatureNames, true );
          }
          catch ( IOException e ) {
            sLogger.error(e.getMessage(), e);
          }
        }

        //decode the SUAS model if needed
        if ( aDAFIFFileName.equals( TLcdDAFIFModelDecoderSupport.getSpecialUseAirspaceFileName( fSourceDAFIFProperties ) ) )
        {
          TLcdDAFIFSpecialUseAirspaceDecoder suas_decoder = new TLcdDAFIFSpecialUseAirspaceDecoder( new TLcdAISDataObjectFactory(), fSourceDAFIFProperties );
          try {
            fSUASModel = (ILcdFeatureIndexedAnd2DBoundsIndexedModel) suas_decoder.decode( fInputDirectory.getAbsolutePath() );
            fDecoderErrorMessages = fDecoderErrorMessages + "\nSpecial Use Airspace decoder problems:\n" + suas_decoder.getErrorMessage();
            fSUASKeyFeatureNames = Arrays.asList( new String[] {
                    ILcdDAFIFAirspaceFeature.BOUNDARY_IDENTIFICATION,
                    ILcdDAFIFAirspaceFeature.SECTOR
            } );
            fSUASModel.addIndex( fSUASKeyFeatureNames, true );
          }
          catch ( IOException e ) {
            sLogger.error(e.getMessage(), e);
          }
        }

        //decode refueling track model if needed
        if ( aDAFIFFileName.equals( TLcdDAFIFModelDecoderSupport.getRefuelingFileName( fSourceDAFIFProperties ) ) ) {
          TLcdDAFIFRefuelingTrackDecoder refueling_decoder = new TLcdDAFIFRefuelingTrackDecoder( new TLcdAISDataObjectFactory(), fSourceDAFIFProperties );
          try {
            fRefuelingAirspaceModel = (ILcdFeatureIndexedAnd2DBoundsIndexedModel) ( (TLcdModelList) refueling_decoder.decode( fInputDirectory.getAbsolutePath() ) ).getModel( TLcdDAFIFRefuelingTrackDecoder.AIRSPACE_INDEX );
            fDecoderErrorMessages = fDecoderErrorMessages + "\nRefueling track decoder problems:\n" + refueling_decoder.getErrorMessage();
            fRefuelingAirspaceKeyFeatureNames = Arrays.asList( new String[] {
                    ILcdDAFIFRefuelingAirspaceFeature.IDENTIFIER,
                    ILcdDAFIFRefuelingAirspaceFeature.DIRECTION
            } );
            fRefuelingAirspaceModel.addIndex( fRefuelingAirspaceKeyFeatureNames, true );
          }
          catch ( IOException e ) {
            sLogger.error(e.getMessage(), e);
          }
        }

        //decode parachute jump area model if needed
        if ( aDAFIFFileName.equals( TLcdDAFIFModelDecoderSupport.getParachuteJumpAreaFileName( fSourceDAFIFProperties ) ) )
        {
          TLcdDAFIFParachuteJumpAreaDecoder pja_decoder = new TLcdDAFIFParachuteJumpAreaDecoder( new TLcdAISDataObjectFactory(), fSourceDAFIFProperties );
          try {
            fParachuteJumpAreaAirspaceModel = (ILcdFeatureIndexedAnd2DBoundsIndexedModel) pja_decoder.decode( fInputDirectory.getAbsolutePath() );
            fDecoderErrorMessages = fDecoderErrorMessages + "\nParachute Jump Area decoder problems:\n" + pja_decoder.getErrorMessage();
            fParachuteJumpAreaAirspaceKeyFeatureNames = Arrays.asList( new String[] { ILcdDAFIFParachuteJumpAreaFeature.IDENTIFICATION } );
            fParachuteJumpAreaAirspaceModel.addIndex( fParachuteJumpAreaAirspaceKeyFeatureNames, true );
          }
          catch ( IOException e ) {
            sLogger.error(e.getMessage(), e);
          }
        }
      }

      double lat = 0.0, lon;
      char[] record = new char[RECORD_LENGTH];
      int num_rec = 0;
      while ( reader.read( record ) != -1 ) {
        if ( num_rec % 1000 == 0 ) {
          sendStatusMessage( "  " + num_rec + " records read." );
        }
        num_rec++;

        String ICAO_region = readICAO( record, aDAFIFFileName );
        if ( sLogger.isTraceEnabled() && ICAO_region.length() == 0 ) {
          sLogger.trace( "No ICAO region found in file" + aDAFIFFileName + " record = " + record.toString() );
        }
        if ( ICAO_region.length() != 0 && !ICAO_region.equals( "^^" ) ) {
          //write record to file in output_dir
          String output_dir = fOutputDirectory + ICAO_region + "/";
          Writer writer = getWriter( output_dir, aDAFIFFileName );
          writer.write( record );

          //add info about file name to config file
          updateDAFIFProperties( ICAO_region, aDAFIFFileName );

          if ( !isWriteBounds() ) continue;

          //read lon and lat and compare them with min/max values
          //waypoint (file3)
          if ( aDAFIFFileName.equals( TLcdDAFIFModelDecoderSupport.getWaypointFileName( fSourceDAFIFProperties ) ) ) {
            updateBounds( aDAFIFFileName, ICAO_region, record );
          }
          //aerodrome (file0) and heliport (file1)
          else if ( aDAFIFFileName.equals( TLcdDAFIFModelDecoderSupport.getAerodromeFileName( fSourceDAFIFProperties ) )
                  || aDAFIFFileName.equals( TLcdDAFIFModelDecoderSupport.getHeliportFileName( fSourceDAFIFProperties ) ) )
          {
            if ( readFormat( record ) == 1 ) {
              updateBounds( aDAFIFFileName, ICAO_region, record );
            }
          }
          //navaid (file2)
          else
          if ( aDAFIFFileName.equals( TLcdDAFIFModelDecoderSupport.getNavaidFileName( fSourceDAFIFProperties ) ) ) {
            if ( readFormat( record ) == 1 ) {
              lat = readLat( record, aDAFIFFileName );
            } else if ( readFormat( record ) == 2 ) {
              lon = readLon( record, aDAFIFFileName );
              updateBounds( aDAFIFFileName, ICAO_region, lon, lat );
            }
          }
          //airspace (file5)
          else
          if ( aDAFIFFileName.equals( TLcdDAFIFModelDecoderSupport.getAirspaceFileName( fSourceDAFIFProperties ) ) ) {
            int format = readFormat( record );
            if ( format == 1 ) {
              fAirspaceKeyFeatureValues[ 0 ] = new String( record, 7, 7 ).trim();
              ILcdAirspace airspace = ( (ILcdAirspace) fAirspaceModel.retrieveByUniqueIndex( fAirspaceKeyFeatureNames, Arrays.asList( fAirspaceKeyFeatureValues ) ) );
              if ( airspace != null ) {
                ILcdBounds bounds = airspace.getBounds();
                updateBounds( aDAFIFFileName, ICAO_region, bounds );
              }
            }
          }
          //SUAS (file6)
          else
          if ( aDAFIFFileName.equals( TLcdDAFIFModelDecoderSupport.getSpecialUseAirspaceFileName( fSourceDAFIFProperties ) ) )
          {
            int format = readFormat( record );
            if ( format == 1 ) {
              fSUASKeyFeatureValues[ 0 ] = new String( record, 7, 12 ).trim();
              fSUASKeyFeatureValues[ 1 ] = new String( record, 19, 2 ).trim();
              ILcdAirspace special_use_airspace = ( (ILcdAirspace) fSUASModel.retrieveByUniqueIndex( fSUASKeyFeatureNames, Arrays.asList( fSUASKeyFeatureValues ) ) );
              if ( special_use_airspace != null ) {
                ILcdBounds bounds = special_use_airspace.getBounds();
                updateBounds( aDAFIFFileName, ICAO_region, bounds );
              }
            }
          }
//          Refueling Track (file7)
          else
          if ( aDAFIFFileName.equals( TLcdDAFIFModelDecoderSupport.getRefuelingFileName( fSourceDAFIFProperties ) ) ) {
            int format = readFormat( record );
            if ( format == 4 ) {
              updateBounds( aDAFIFFileName, ICAO_region, readLon( record, aDAFIFFileName, format ), readLat( record, aDAFIFFileName, format ) );
            } else if ( format == 6 ) {
              fRefuelingAirspaceKeyFeatureValues[ 0 ] = new String( record, 7, 15 ).trim();
              fRefuelingAirspaceKeyFeatureValues[ 1 ] = new String( record, 22, 2 ).trim();
              ILcdRefuelingAirspace refueling_airspace = ( (ILcdRefuelingAirspace) fRefuelingAirspaceModel.retrieveByUniqueIndex( fRefuelingAirspaceKeyFeatureNames, Arrays.asList( fRefuelingAirspaceKeyFeatureValues ) ) );
              if ( refueling_airspace != null ) {
                ILcdBounds bounds = refueling_airspace.getBounds();
                updateBounds( aDAFIFFileName, ICAO_region, bounds );
              }
            }
          }
          //Military Training Route (file8)
          else
          if ( aDAFIFFileName.equals( TLcdDAFIFModelDecoderSupport.getMilitaryTrainingRouteFileName( fSourceDAFIFProperties ) ) )
          {
            int format = readFormat( record );
            if ( format == 4 ) {
              updateBounds( aDAFIFFileName, ICAO_region, record );
            }
          }
          //Parachute Jump Area (file10)
          else
          if ( aDAFIFFileName.equals( TLcdDAFIFModelDecoderSupport.getParachuteJumpAreaFileName( fSourceDAFIFProperties ) ) )
          {
            if ( readFormat( record ) == 2 ) {
              fParachuteJumpAreaAirspaceKeyFeatureValues[ 0 ] = new String( record, 5, 7 ).trim();

              ILcdParachuteJumpArea parachute_jump_area = ( (ILcdParachuteJumpArea) fParachuteJumpAreaAirspaceModel.retrieveByUniqueIndex( fParachuteJumpAreaAirspaceKeyFeatureNames, Arrays.asList( fParachuteJumpAreaAirspaceKeyFeatureValues ) ) );
              if ( parachute_jump_area != null ) {
                updateBounds( aDAFIFFileName, ICAO_region, parachute_jump_area.getBounds() );
              }
            }
          }
          //VFR (file15)
          else if ( aDAFIFFileName.equals( TLcdDAFIFModelDecoderSupport.getVFRFileName( fSourceDAFIFProperties ) ) ) {
            int format = readFormat( record );
            if ( ( format == 1 ) || ( format == 3 ) ) {    //1=aerodrome coordinates 3=route point coordinates
              updateBounds( aDAFIFFileName, ICAO_region, readLon( record, aDAFIFFileName, format ), readLat( record, aDAFIFFileName, format ) );
            } else if ( format == 4 ) {    //4=offset route point
              if ( new String( record, 30, 9 ).trim().length() > 0 ) {         //right
                updateBounds( aDAFIFFileName, ICAO_region, readLon( record, 39 ), readLat( record, 30 ) );
              }
              if ( new String( record, 49, 9 ).trim().length() > 0 ) {        //left
                updateBounds( aDAFIFFileName, ICAO_region, readLon( record, 58 ), readLat( record, 49 ) );
              }
            }
          }
        }
      }
    }
    catch ( IOException ioe ) {
      sLogger.error(ioe.getMessage(), ioe);
    }
    finally {
      try {
        if ( reader != null ) {
          reader.close();
        }
      }
      catch ( IOException e ) {
        sLogger.error(e.getMessage(), e);
      }

      closeWriters();
    }
  }

  private void writeBndFiles() {
    if ( !isWriteBounds() ) return;

    int deviation = Integer.parseInt( fProperties.getProperty( "bounds_deviation", "1" ) );
    BufferedWriter bnd_writer = null;
    for ( Enumeration<String> file_enum = fICAOBounds.keys(); file_enum.hasMoreElements(); ) {
      String file_name = (String) file_enum.nextElement();
      Hashtable<?, ?> file_table = (Hashtable<?, ?>) fICAOBounds.get( file_name );
      for ( Enumeration<?> ICAO_enum = file_table.keys(); ICAO_enum.hasMoreElements(); ) {
        String ICAO_region = (String) ICAO_enum.nextElement();
        TLcdLonLatBounds current_bounds = (TLcdLonLatBounds) file_table.get( ICAO_region );
        if ( current_bounds != null ) {
          try {
            bnd_writer = getBndWriter( fOutputDirectory + ICAO_region + "/", file_name );
            bnd_writer.write( "# This file contains the bounds of " + ICAO_region + " in " + file_name + "." );
            bnd_writer.newLine();
            bnd_writer.write( "y=" + ( current_bounds.getLocation().getY() - deviation ) );
            bnd_writer.newLine();
            bnd_writer.write( "x=" + ( current_bounds.getLocation().getX() - deviation ) );
            bnd_writer.newLine();
            bnd_writer.write( "width=" + ( current_bounds.getWidth() + 2 * deviation ) );
            bnd_writer.newLine();
            bnd_writer.write( "height=" + ( current_bounds.getHeight() + 2 * deviation ) );
            bnd_writer.close();
          }
          catch ( IOException ioe ) {
            sLogger.error(ioe.getMessage(), ioe);
          }
          finally {
            try {
              if ( bnd_writer != null ) {
                bnd_writer.close();
              }
            }
            catch ( IOException e ) {
              sLogger.error(e.getMessage(), e);
            }
          }
        }
      }
    }
  }

  private void writeSubDirDAFIFPropertiesFiles() {
    for ( int i = 0; i < fICAORegions.size() ; i++ ) {
      String ICAO_region = (String) fICAORegions.get( i );
      String output_dir = fOutputDirectory + ICAO_region + "/";
      try {
        File tocFile = new File( output_dir + "dafif_" + ICAO_region + ".toc" );
        // add current ICAO region to the properties file.
        fSubTargetDAFIFProperties.put( "ICAO.region"+0, ICAO_region );
        fSubTargetDAFIFProperties.store( new FileOutputStream( tocFile ), "DAFIF Properties" );
      }
      catch ( IOException e ) {
        sLogger.error(e.getMessage(), e);
      }
    }
  }

  /**
   * Reads the format of a record.
   *
   * @param aRecord
   */
  private int readFormat( char[] aRecord ) {
    return Integer.parseInt( new String( aRecord, 0, 2 ) );
  }

  /**
   * Reads the ICAO region of aRecord.
   * aFileName specifies the name of the data source file. The offset
   * of the ICAO region depends from this data source file.
   *
   * @param aRecord
   * @param aFileName
   */
  private String readICAO( char[] aRecord, String aFileName ) {
    String result;
    if ( aFileName.equals( TLcdDAFIFModelDecoderSupport.getHoldingFileName( fSourceDAFIFProperties ) ) ) {
      result = new String( aRecord, 10, 2 ).trim();
    } else if ( aFileName.equals( TLcdDAFIFModelDecoderSupport.getOrtcaFileName( fSourceDAFIFProperties ) ) ) {
      result = "";      //the ortca data file does not have an ICAO code!
    } else {
      result = new String( aRecord, 3, 2 ).trim();
    }
    if ( sLogger.isTraceEnabled() ) {
      sLogger.trace( "ICAO code found with readICAO = " + result + " in file " + aFileName );
    }
    return result;
  }

  /**
   * Reads the latitude of aRecord.
   * aFileName specifies the name of the data source file. The offset
   * of the latitude depends from this data source file.
   *
   * @param aRecord
   * @param aFileName
   */
  private double readLat( char[] aRecord, String aFileName ) {
    int offset = 0;
    if ( aFileName.equals( TLcdDAFIFModelDecoderSupport.getAerodromeFileName( fSourceDAFIFProperties ) )
            || aFileName.equals( TLcdDAFIFModelDecoderSupport.getHeliportFileName( fSourceDAFIFProperties ) )
            || aFileName.equals( TLcdDAFIFModelDecoderSupport.getWaypointFileName( fSourceDAFIFProperties ) ) ) {
      offset = 83;
    } else if ( aFileName.equals( TLcdDAFIFModelDecoderSupport.getNavaidFileName( fSourceDAFIFProperties ) ) ) {
      offset = 116;
    } else
    if ( aFileName.equals( TLcdDAFIFModelDecoderSupport.getMilitaryTrainingRouteFileName( fSourceDAFIFProperties ) ) ) {
      offset = 98;
    }
    return readLat( aRecord, offset );
  }

  /**
   * Returns the latitude found in aRecord. This method needs an extra parameter
   * because there is more than one set of coordinates and the different sets are
   * found in records of different formats at different positions.
   *
   * @param aRecord
   * @param aFileName
   * @param aFormat
   */
  private double readLat( char[] aRecord, String aFileName, int aFormat ) {
    int offset = 0;
    if ( ( aFileName.equals( TLcdDAFIFModelDecoderSupport.getAirspaceFileName( fSourceDAFIFProperties ) ) )
            && ( aFormat == 3 ) ) {
      offset = 78;
    } else if ( ( aFileName.equals( TLcdDAFIFModelDecoderSupport.getAirspaceFileName( fSourceDAFIFProperties ) ) )
            && ( aFormat == 4 ) ) {
      offset = 19;
    } else
    if ( ( aFileName.equals( TLcdDAFIFModelDecoderSupport.getSpecialUseAirspaceFileName( fSourceDAFIFProperties ) ) )
            && ( aFormat == 3 ) ) {
      offset = 85;
    } else
    if ( ( aFileName.equals( TLcdDAFIFModelDecoderSupport.getSpecialUseAirspaceFileName( fSourceDAFIFProperties ) ) )
            && ( aFormat == 4 ) ) {
      offset = 26;
    } else if ( ( aFileName.equals( TLcdDAFIFModelDecoderSupport.getRefuelingFileName( fSourceDAFIFProperties ) ) )
            && ( aFormat == 4 ) ) {
      offset = 63;
    } else if ( ( aFileName.equals( TLcdDAFIFModelDecoderSupport.getRefuelingFileName( fSourceDAFIFProperties ) ) )
            && ( aFormat == 6 ) ) {
      offset = 31;
    } else if ( ( aFileName.equals( TLcdDAFIFModelDecoderSupport.getVFRFileName( fSourceDAFIFProperties ) ) )
            && ( aFormat == 1 ) ) {
      offset = 58;
    } else if ( ( aFileName.equals( TLcdDAFIFModelDecoderSupport.getVFRFileName( fSourceDAFIFProperties ) ) )
            && ( aFormat == 3 ) ) {
      offset = 116;
    }
    return readLat( aRecord, offset );
  }

  /**
   * Reads the latitude from aRecord knowing
   * that this latitude is found at aOffset.
   *
   * @param aRecord
   * @param aOffset
   */
  private double readLat( char[] aRecord, int aOffset ) {
    try {
      double result = TLcdDAFIFLonLatParser.readDoubleLat( aRecord, aOffset );
      if ( sLogger.isTraceEnabled() ) {
        sLogger.trace( "Lat found with readLat = " + result );
      }
      return result;
    }
    catch ( NumberFormatException nfe ) {
      sLogger.error(nfe.getMessage(), nfe);
      return 0;
    }
  }

  /**
   * Reads the longitude of aRecord.
   * aFileName specifies the name of the data source file. The offset
   * of the longitude depends from this data source file.
   *
   * @param aRecord
   * @param aFileName
   */
  private double readLon( char[] aRecord, String aFileName ) {
    int offset = 0;
    if ( aFileName.equals( TLcdDAFIFModelDecoderSupport.getAerodromeFileName( fSourceDAFIFProperties ) )
            || aFileName.equals( TLcdDAFIFModelDecoderSupport.getHeliportFileName( fSourceDAFIFProperties ) )
            || aFileName.equals( TLcdDAFIFModelDecoderSupport.getWaypointFileName( fSourceDAFIFProperties ) ) ) {
      offset = 92;
    } else if ( aFileName.equals( TLcdDAFIFModelDecoderSupport.getNavaidFileName( fSourceDAFIFProperties ) ) ) {
      offset = 16;
    } else
    if ( aFileName.equals( TLcdDAFIFModelDecoderSupport.getMilitaryTrainingRouteFileName( fSourceDAFIFProperties ) ) ) {
      offset = 107;
    }
    return readLon( aRecord, offset );
  }

  /**
   * Returns the longitude found in aRecord. This method needs an extra parameter
   * because there is more than one set of coordinates and the different sets are
   * found in records of different formats at different positions.
   *
   * @param aRecord
   * @param aFileName
   * @param aFormat
   */
  private double readLon( char[] aRecord, String aFileName, int aFormat ) {
    int offset = 0;
    if ( ( aFileName.equals( TLcdDAFIFModelDecoderSupport.getAirspaceFileName( fSourceDAFIFProperties ) ) )
            && ( aFormat == 3 ) ) {
      offset = 87;
    } else if ( ( aFileName.equals( TLcdDAFIFModelDecoderSupport.getAirspaceFileName( fSourceDAFIFProperties ) ) )
            && ( aFormat == 4 ) ) {
      offset = 28;
    } else
    if ( ( aFileName.equals( TLcdDAFIFModelDecoderSupport.getSpecialUseAirspaceFileName( fSourceDAFIFProperties ) ) )
            && ( aFormat == 3 ) ) {
      offset = 94;
    } else
    if ( ( aFileName.equals( TLcdDAFIFModelDecoderSupport.getSpecialUseAirspaceFileName( fSourceDAFIFProperties ) ) )
            && ( aFormat == 4 ) ) {
      offset = 35;
    } else if ( ( aFileName.equals( TLcdDAFIFModelDecoderSupport.getRefuelingFileName( fSourceDAFIFProperties ) ) )
            && ( aFormat == 4 ) ) {
      offset = 72;
    } else if ( ( aFileName.equals( TLcdDAFIFModelDecoderSupport.getRefuelingFileName( fSourceDAFIFProperties ) ) )
            && ( aFormat == 6 ) ) {
      offset = 40;
    } else if ( ( aFileName.equals( TLcdDAFIFModelDecoderSupport.getVFRFileName( fSourceDAFIFProperties ) ) )
            && ( aFormat == 1 ) ) {
      offset = 67;
    } else if ( ( aFileName.equals( TLcdDAFIFModelDecoderSupport.getVFRFileName( fSourceDAFIFProperties ) ) )
            && ( aFormat == 3 ) ) {
      offset = 125;
    }
    return readLon( aRecord, offset );
  }

  /**
   * Reads the longitude from aRecord knowing
   * that this longitude is found at aOffset.
   *
   * @param aRecord
   * @param aOffset
   */
  private double readLon( char[] aRecord, int aOffset ) {
    try {
      double result = TLcdDAFIFLonLatParser.readDoubleLon( aRecord, aOffset );
      if ( sLogger.isTraceEnabled() ) {
        sLogger.trace( "Lon found with readLon = " + result );
      }
      return result;
    }
    catch ( NumberFormatException nfe ) {
      sLogger.error(nfe.getMessage(), nfe);
      return 0;
    }
  }

  /**
   * Initializes fInputDirectory and fOutputDirectory.
   * If fOutputDirectory exists, it will be deleted. The latest is
   * done because otherwise new data would be appended to old data.
   */
  private void setupDirectories() {
    //initialize input directory
    fInputDirectory = getDirectory();

    //initialize output directory
    fOutputDirectory = fProperties.getProperty( "split_directory", "split/" );
    if ( !fOutputDirectory.endsWith( "/" ) ) {
      fOutputDirectory += "/";
    }

    //delete output directory if it already exists
    File output_dir = new File( fOutputDirectory );
    if ( output_dir.isDirectory() && output_dir.exists() ) {
      boolean is_deleted = deleteOutputDirectory( output_dir );
      if ( sLogger.isTraceEnabled() ) {
        sLogger.trace( "is_deleted = " + is_deleted );
      }
    }

    //some tracing
    if ( sLogger.isTraceEnabled() ) {
      sLogger.trace( "setupDirectories(): InputDirectory = " + fInputDirectory );
      sLogger.trace( "setupDirectories(): OutputDirectory = " + fOutputDirectory );
    }
  }

  /**
   * Creates a new Hashtable for each file in fICAOBounds.
   * This Hashtable will, in the end, contain for each
   * ICAO region the bounds of that region.
   */
  private void setupICAOBounds() {
    if ( isWriteBounds() ) {
      String[] inputFileNames = getInputFileNames();
      for ( int index = 0; index < inputFileNames.length ; index++ ) {
        fICAOBounds.put( inputFileNames[index], new Hashtable<String, TLcdLonLatBounds>() );
      }
    }
  }

  /**
   * Updates the bounds of the model created from aDAFIFFileName situated in aICAORegion
   * with the data found in aRecord.
   * If no bounds exist yet for the specified file in the specified region, a new
   * <code>ICAOBounds</code> object will be created.
   * Else, the min and max values in the <code>ICAOBounds</code> object will be compared
   * with the longitude and latitude read from aRecord.
   *
   * @param aDAFIFFileName
   * @param aICAORegion
   * @param aRecord
   */
  private void updateBounds( String aDAFIFFileName, String aICAORegion, char[] aRecord ) {
    if ( isWriteBounds() ) {
      double lat = readLat( aRecord, aDAFIFFileName );
      double lon = readLon( aRecord, aDAFIFFileName );
      updateBounds( aDAFIFFileName, aICAORegion, lon, lat );
    }
  }

  /**
   * Updates the bounds of the model created from aDAFIFFileName situated in aICAORegion
   * with the specified coordinates.
   * If no bounds exist yet for the specified file in the specified region, a new
   * <code>ICAOBounds</code> object will be created.
   * Else, the min and max values in the <code>ICAOBounds</code> object will be compared
   * with aLon and aLat.
   *
   * @param aDAFIFFileName
   * @param aICAORegion
   * @param aLon
   * @param aLat
   */
  private void updateBounds( String aDAFIFFileName, String aICAORegion, double aLon, double aLat ) {
    if ( isWriteBounds() ) {
      TLcdLonLatBounds ICAO_bounds;
      ICAO_bounds = (TLcdLonLatBounds) ((Hashtable<?, ?>) fICAOBounds.get( aDAFIFFileName )).get( aICAORegion );
      if ( ICAO_bounds == null ) {
        ILcdPoint lonlatpoint = new TLcdLonLatPoint( aLon, aLat );
        ICAO_bounds = new TLcdLonLatBounds( lonlatpoint );
        ((Hashtable<String, TLcdLonLatBounds>) fICAOBounds.get( aDAFIFFileName )).put( aICAORegion, ICAO_bounds );
      } else {
        ICAO_bounds.setToIncludePoint2D( aLon, aLat );
      }
    }
  }

  private void updateBounds( String aDAFIFFileName, String aICAORegion, ILcdBounds aBounds ) {
    if ( isWriteBounds() ) {
      TLcdLonLatBounds ICAO_bounds;
      ICAO_bounds = (TLcdLonLatBounds) ((Hashtable<?, ?>) fICAOBounds.get( aDAFIFFileName )).get( aICAORegion );
      if ( ICAO_bounds == null ) {
        ICAO_bounds = new TLcdLonLatBounds( aBounds );
        ((Hashtable<String, TLcdLonLatBounds>) fICAOBounds.get( aDAFIFFileName )).put( aICAORegion, ICAO_bounds );
      } else {
        ICAO_bounds.setTo2DUnion( aBounds );
      }
    }
  }


  private void updateDAFIFProperties( String aICAORegion, String aDAFIFFileName ) {
    //write aDAFIFFileName in the DAFIF properties of the target directory
    if ( aDAFIFFileName.equals( TLcdDAFIFModelDecoderSupport.getAerodromeFileName( fSourceDAFIFProperties ) ) ) {
      fTopTargetDAFIFProperties.put( TLcdDAFIFModelDecoderSupport.SOURCE_AERODROMES, aDAFIFFileName );
      fSubTargetDAFIFProperties.put( TLcdDAFIFModelDecoderSupport.SOURCE_AERODROMES, aDAFIFFileName );
      fTopTargetDAFIFProperties.put( TLcdDAFIFModelDecoderSupport.SOURCE_ILS, aDAFIFFileName );
      fSubTargetDAFIFProperties.put( TLcdDAFIFModelDecoderSupport.SOURCE_ILS, aDAFIFFileName );
      fTopTargetDAFIFProperties.put( TLcdDAFIFModelDecoderSupport.SOURCE_PROCEDURES, aDAFIFFileName );
      fSubTargetDAFIFProperties.put( TLcdDAFIFModelDecoderSupport.SOURCE_PROCEDURES, aDAFIFFileName );
      fTopTargetDAFIFProperties.put( TLcdDAFIFModelDecoderSupport.SOURCE_RUNWAYS, aDAFIFFileName );
      fSubTargetDAFIFProperties.put( TLcdDAFIFModelDecoderSupport.SOURCE_RUNWAYS, aDAFIFFileName );
    } else if ( aDAFIFFileName.equals( TLcdDAFIFModelDecoderSupport.getAirspaceFileName( fSourceDAFIFProperties ) ) ) {
      fTopTargetDAFIFProperties.put( TLcdDAFIFModelDecoderSupport.SOURCE_AIRSPACES, aDAFIFFileName );
      fSubTargetDAFIFProperties.put( TLcdDAFIFModelDecoderSupport.SOURCE_AIRSPACES, aDAFIFFileName );
    } else if ( aDAFIFFileName.equals( TLcdDAFIFModelDecoderSupport.getATSRouteFileName( fSourceDAFIFProperties ) ) ) {
      fTopTargetDAFIFProperties.put( TLcdDAFIFModelDecoderSupport.SOURCE_ATS_ROUTES, aDAFIFFileName );
      fSubTargetDAFIFProperties.put( TLcdDAFIFModelDecoderSupport.SOURCE_ATS_ROUTES, aDAFIFFileName );
    } else if ( aDAFIFFileName.equals( TLcdDAFIFModelDecoderSupport.getHeliportFileName( fSourceDAFIFProperties ) ) ) {
      fTopTargetDAFIFProperties.put( TLcdDAFIFModelDecoderSupport.SOURCE_HELIPORTS, aDAFIFFileName );
      fSubTargetDAFIFProperties.put( TLcdDAFIFModelDecoderSupport.SOURCE_HELIPORTS, aDAFIFFileName );
      fTopTargetDAFIFProperties.put( TLcdDAFIFModelDecoderSupport.SOURCE_HELIPADS, aDAFIFFileName );
      fSubTargetDAFIFProperties.put( TLcdDAFIFModelDecoderSupport.SOURCE_HELIPADS, aDAFIFFileName );
    } else if ( aDAFIFFileName.equals( TLcdDAFIFModelDecoderSupport.getHoldingFileName( fSourceDAFIFProperties ) ) ) {
      fTopTargetDAFIFProperties.put( TLcdDAFIFModelDecoderSupport.SOURCE_HOLDINGS, aDAFIFFileName );
      fSubTargetDAFIFProperties.put( TLcdDAFIFModelDecoderSupport.SOURCE_HOLDINGS, aDAFIFFileName );
    } else
    if ( aDAFIFFileName.equals( TLcdDAFIFModelDecoderSupport.getMilitaryTrainingRouteFileName( fSourceDAFIFProperties ) ) )
    {
      fTopTargetDAFIFProperties.put( TLcdDAFIFModelDecoderSupport.SOURCE_MILITARY_TRAINING_ROUTES, aDAFIFFileName );
      fSubTargetDAFIFProperties.put( TLcdDAFIFModelDecoderSupport.SOURCE_MILITARY_TRAINING_ROUTES, aDAFIFFileName );
    } else if ( aDAFIFFileName.equals( TLcdDAFIFModelDecoderSupport.getNavaidFileName( fSourceDAFIFProperties ) ) ) {
      fTopTargetDAFIFProperties.put( TLcdDAFIFModelDecoderSupport.SOURCE_NAVAIDS, aDAFIFFileName );
      fSubTargetDAFIFProperties.put( TLcdDAFIFModelDecoderSupport.SOURCE_NAVAIDS, aDAFIFFileName );
    } else if ( aDAFIFFileName.equals( TLcdDAFIFModelDecoderSupport.getOrtcaFileName( fSourceDAFIFProperties ) ) ) {
      fTopTargetDAFIFProperties.put( TLcdDAFIFModelDecoderSupport.SOURCE_ORTCA, aDAFIFFileName );
      fSubTargetDAFIFProperties.put( TLcdDAFIFModelDecoderSupport.SOURCE_ORTCA, aDAFIFFileName );
    } else
    if ( aDAFIFFileName.equals( TLcdDAFIFModelDecoderSupport.getParachuteJumpAreaFileName( fSourceDAFIFProperties ) ) )
    {
      fTopTargetDAFIFProperties.put( TLcdDAFIFModelDecoderSupport.SOURCE_PARACHUTE_JUMP_AREAS, aDAFIFFileName );
      fSubTargetDAFIFProperties.put( TLcdDAFIFModelDecoderSupport.SOURCE_PARACHUTE_JUMP_AREAS, aDAFIFFileName );
    } else if ( aDAFIFFileName.equals( TLcdDAFIFModelDecoderSupport.getRefuelingFileName( fSourceDAFIFProperties ) ) ) {
      fTopTargetDAFIFProperties.put( TLcdDAFIFModelDecoderSupport.SOURCE_REFUELINGTRACK, aDAFIFFileName );
      fSubTargetDAFIFProperties.put( TLcdDAFIFModelDecoderSupport.SOURCE_REFUELINGTRACK, aDAFIFFileName );
    } else
    if ( aDAFIFFileName.equals( TLcdDAFIFModelDecoderSupport.getSpecialUseAirspaceFileName( fSourceDAFIFProperties ) ) )
    {
      fTopTargetDAFIFProperties.put( TLcdDAFIFModelDecoderSupport.SOURCE_SPECIAL_USE_AIRSPACES, aDAFIFFileName );
      fSubTargetDAFIFProperties.put( TLcdDAFIFModelDecoderSupport.SOURCE_SPECIAL_USE_AIRSPACES, aDAFIFFileName );
    } else
    if ( aDAFIFFileName.equalsIgnoreCase( TLcdDAFIFModelDecoderSupport.getVFRFileName( fSourceDAFIFProperties ) ) ) {
      fTopTargetDAFIFProperties.put( TLcdDAFIFModelDecoderSupport.SOURCE_VFR, aDAFIFFileName );
      fSubTargetDAFIFProperties.put( TLcdDAFIFModelDecoderSupport.SOURCE_VFR, aDAFIFFileName );
    } else
    if ( aDAFIFFileName.equalsIgnoreCase( TLcdDAFIFModelDecoderSupport.getWaypointFileName( fSourceDAFIFProperties ) ) )
    {
      fTopTargetDAFIFProperties.put( TLcdDAFIFModelDecoderSupport.SOURCE_WAYPOINTS, aDAFIFFileName );
      fSubTargetDAFIFProperties.put( TLcdDAFIFModelDecoderSupport.SOURCE_WAYPOINTS, aDAFIFFileName );
    }

    //add the ICAO region to the vector of ICAO regions if it's not yet added.
    if ( !fICAORegions.contains( aICAORegion ) ) {
      fICAORegions.add( aICAORegion );
    }
  }

  /**
   * Writes a toc file in the top directory created by the prepareDate method.
   */
  private void writeDAFIFPropertiesFile() {
    completeDAFIFProperties();
    try {
      File tocFile = new File( fOutputDirectory + "dafif.toc" );
      fTopTargetDAFIFProperties.store( new FileOutputStream( tocFile ), "DAFIF Properties" );
    }
    catch ( IOException e ) {
      sLogger.error(e.getMessage(), e);
    }
  }

  // Main method

}

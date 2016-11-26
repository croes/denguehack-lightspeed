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

import java.io.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.JFileChooser;

import com.luciad.format.dafift.decoder.ILcdDAFIFTRecordFilter;
import com.luciad.format.dafift.decoder.TLcdDAFIFTAerodromeRecordFilter;
import com.luciad.format.dafift.decoder.TLcdDAFIFTICAORegionFilter;
import com.luciad.format.dafift.decoder.TLcdDAFIFTILSRecordFilter;
import com.luciad.format.dafift.decoder.TLcdDAFIFTModelDecoderSupport;
import com.luciad.format.dafift.decoder.TLcdDAFIFTNavaidRecordFilter;
import com.luciad.format.dafift.decoder.TLcdDAFIFTRunwayRecordFilter;
import com.luciad.format.dafift.decoder.TLcdDAFIFTWayPointRecordFilter;
import com.luciad.io.TLcdIOUtil;
import com.luciad.util.ILcdStatusListener;
import com.luciad.util.ILcdStatusSource;
import com.luciad.util.TLcdStatusEvent;
import com.luciad.util.logging.ILcdLogger;
import com.luciad.util.logging.TLcdLoggerFactory;

/**
 * A tool to split large DAFIFT files into smaller ones. The division of the DAFIFT data is based
 * on the two letter ICAO region found in some records and the corresponding object identifiers
 * (e.g. aerodrome id, airspace id,...).
 * <p/>
 * Given a directory containing one or more DAFIFT files, the <code>DAFIFTDataPreparator</code>
 * class will create a directory tree. The top directory, that should be specified in the config file,
 * will contain a sub directory for each ICAO region found in the source data.
 * Each sub directory contains
 * <ul>
 *  <li>the records having the ICAO region mentioned in the sub directory's name, organized in files
 *      having the same names as the source files and</li>
 *  <li>a file named dafift.toc, containing the property: icao.region=&lt;ICAO code&gt;. This code
 *      represents the ICAO region of this data structure.</li>
 * </ul>
 * <p/>
 * Besides reorganizing a given data set, the <code>DAFIFTDataPreparator</code> class also writes
 * a dafif.toc file into the top directory. This <code>dafif.toc</code> properties file is empty.
 * <p/>
 * The result of running the <code>DAFIFTDataPreparator</code> class will look like this:
 * <pre>
 *     - &lt;split_directory&gt;
 *            - AG
 *              - ARPT
 *                ACOM.TXT
 *                ACOM_RMK.TXT
 *                ...
 *              - BDRY
 *                BDRY.TXT
 *                BDRY_PAR.TXT
 *                ...
 *              ...
 *              dafift.toc
 *            - AN
 *              - ARPT
 *                ACOM.TXT
 *                ACOM_RMK.TXT
 *                ...
 *              - BDRY
 *                BDRY.TXT
 *                BDRY_PAR.TXT
 *                ...
 *              ...
 *              dafift.toc
 *            ...
 *            dafift.toc
 * </pre>
 * <p/>
 * Note that entities, such as waypoints and routes, are not split independently. This means, for example,
 * that when a certain route depends on waypoints not in its own ICAO region, this waypoint will be added
 * to the waypoint file for this region, and the affected route segments will be added to the route.
 *
 * @since 7.2
 */
public class DAFIFTDataPreparator implements ILcdStatusSource {

private static ILcdLogger sLogger = TLcdLoggerFactory.getLogger(DAFIFTDataPreparator.class.getName());

  private static final String DAFIFT_CONFIG_FILE_NAME = "dafift.toc";

  private static final int AERODROME_NAVAID_IDENTIFIER   = 1;
  private static final int AERODROME_NAVAID_TYPE         = 2;
  private static final int AERODROME_NAVAID_COUNTRY_CODE = 3;
  private static final int AERODROME_NAVAID_KEY_CODE     = 4;

  private static final int ATS_ROUTE_WAYPOINT_1_IDENTIFIER   = 11;
  private static final int ATS_ROUTE_WAYPOINT_1_COUNTRY_CODE = 12;
  private static final int ATS_ROUTE_WAYPOINT_2_IDENTIFIER   = 23;
  private static final int ATS_ROUTE_WAYPOINT_2_COUNTRY_CODE = 24;

  private static final int HELIPAD_HELIPORT_IDENTIFIER = 0;

  private static final int HOLDING_WAYPOINT_IDENTIFIER   = 0;
  private static final int HOLDING_WAYPOINT_COUNTRY_CODE = 1;

  private static final int ILS_AERODROME_IDENTIFIER = 0;
  private static final int ILS_RUNWAY_IDENTIFIER    = 1;

  private static final int RUNWAY_AERODROME_IDENTIFIER = 0;

  private static final int TERMINAL_SEGMENT_AERODROME_IDENTIFIER  =  0;
  private static final int TERMINAL_SEGMENT_TRANSITION_IDENTIFIER =  5;
  private static final int TERMINAL_SEGMENT_WAYPOINT_IDENTIFIER   =  8;
  private static final int TERMINAL_SEGMENT_WAYPOINT_COUNTRY_CODE =  9;
  private static final int TERMINAL_SEGMENT_NAVAID_1_IDENTIFIER   = 15;
  private static final int TERMINAL_SEGMENT_NAVAID_1_TYPE         = 16;
  private static final int TERMINAL_SEGMENT_NAVAID_1_COUNTRY_CODE = 17;
  private static final int TERMINAL_SEGMENT_NAVAID_1_KEY_CODE     = 18;
  private static final int TERMINAL_SEGMENT_NAVAID_2_IDENTIFIER   = 21;
  private static final int TERMINAL_SEGMENT_NAVAID_2_TYPE         = 22;
  private static final int TERMINAL_SEGMENT_NAVAID_2_COUNTRY_CODE = 23;
  private static final int TERMINAL_SEGMENT_NAVAID_2_KEY_CODE     = 24;


  private Properties fProperties;
  private File       fInputDirectory;
  private String     fOutputDirectory;
  private Hashtable<String, Writer> fOutputFileTable = new Hashtable<String, Writer>();

  private String fDecoderErrorMessages;

  private final CopyOnWriteArrayList<ILcdStatusListener> fStatusListeners = new CopyOnWriteArrayList<>();

  // Hash tables containing record objects needed by other ICAO regions than the one it is defined in.
  // E.g.:
  // A procedure object from ICAO region 'EB' needs a waypoint from ICAO region 'LF' to create a
  // terminal segment. Therefore the key defining the required waypoint is added to the hash table
  // fWayPointFilters.

  private Map<String, ILcdDAFIFTRecordFilter> fAerodromeFilters = new Hashtable<String, ILcdDAFIFTRecordFilter>();
  private Map<String, ILcdDAFIFTRecordFilter> fHeliportFilters = new Hashtable<String, ILcdDAFIFTRecordFilter>();
  private Map<String, ILcdDAFIFTRecordFilter> fILSFilters = new Hashtable<String, ILcdDAFIFTRecordFilter>();
  private Map<String, ILcdDAFIFTRecordFilter> fNavaidFilters = new Hashtable<String, ILcdDAFIFTRecordFilter>();
  private Map<String, ILcdDAFIFTRecordFilter> fRunwayFilters = new Hashtable<String, ILcdDAFIFTRecordFilter>();
  private Map<String, ILcdDAFIFTRecordFilter> fWayPointFilters = new Hashtable<String, ILcdDAFIFTRecordFilter>();

  //////////////////////////////////////////////
  //////       Status event methods       //////
  //////////////////////////////////////////////

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
   * @param aMessage The message to send.
   */
  private void sendStatusMessage( String aMessage ) {
    for (ILcdStatusListener statusListener : fStatusListeners) {
      TLcdStatusEvent.sendMessage(statusListener, this, aMessage, TLcdStatusEvent.Severity.INFO);
    }
  }

  ////////////////////////////////////////
  //////       public methods       //////
  ////////////////////////////////////////

  /**
   * Returns the concatenated error messages of the different decoders used to split the DAFIFT data.
   * This message is available (and changed) after each <code>prepareData</code> call.
   *
   * @return the concatenated error messages of the different decoders used to split the DAFIFT data
   */
  public String getDecoderErrorMessages() {
    return fDecoderErrorMessages;
  }

  /**
   * Specifies a properties object. This properties object should contain
   * <ul>
   *   <li>the source directory or a directory that can be used as initial directory of a fileChooser,</li>
   *   <li>the destination directory.</li>
   * </ul>
   *
   * @param aProperties the properties object to be used during the splitting process
   * @see #main
   */
  public void setProperties( Properties aProperties ) {
    fProperties = aProperties;
  }

  /**
   * Separates all DAFIFT data files found in the source directory over a set of subdirectories.
   * Each sub directory contains the data located in a certain ICAO region.
   * <p/>
   * The data in each sub directory is organized in files like the data in the source directory
   * (same file names are used).
   */
  public void prepareData() {
    setupDirectories();

    String[] input_file_names = new String[] {
            // 1) Process dependent files which needs other dependent files.
            // e.g. TerminalSegment needs ILS which needs Runway which needs Aerodromes
            TLcdDAFIFTModelDecoderSupport.getTerminalSegmentFileName             (),
            TLcdDAFIFTModelDecoderSupport.getILSFileName                         (),

            // 2) Process dependent files which do not need other dependent files.
            // e.g. Runway only needs Aerodromes
            TLcdDAFIFTModelDecoderSupport.getAerodromeNavaidFileName             (),
            TLcdDAFIFTModelDecoderSupport.getATSRouteFileName                    (),
            TLcdDAFIFTModelDecoderSupport.getHelipadFileName                     (),
            TLcdDAFIFTModelDecoderSupport.getHoldingFileName                     (),
            TLcdDAFIFTModelDecoderSupport.getRunwayFileName                      (),

            // 3) Process independent files.
            TLcdDAFIFTModelDecoderSupport.getAerodromeFileName                   (),
            TLcdDAFIFTModelDecoderSupport.getAerodromeCommunicationFileName      (),
            TLcdDAFIFTModelDecoderSupport.getAerodromeCommunicationRemarkFileName(),
            TLcdDAFIFTModelDecoderSupport.getAirspaceFileName                    (),
            TLcdDAFIFTModelDecoderSupport.getAirspaceSegmentFileName             (),
            TLcdDAFIFTModelDecoderSupport.getArrestingGearFileName               (),
            TLcdDAFIFTModelDecoderSupport.getHeliportFileName                    (),
            TLcdDAFIFTModelDecoderSupport.getHeliportCommunicationFileName       (),
            TLcdDAFIFTModelDecoderSupport.getHeliportCommunicationRemarkFileName (),
            TLcdDAFIFTModelDecoderSupport.getMilitaryTrainingRouteFileName       (),
            TLcdDAFIFTModelDecoderSupport.getMilitaryTrainingRouteOverlayFileName(),
            TLcdDAFIFTModelDecoderSupport.getMilitaryTrainingRouteSegmentFileName(),
            TLcdDAFIFTModelDecoderSupport.getNavaidFileName                      (),
            TLcdDAFIFTModelDecoderSupport.getOrtcaFileName                       (),
            TLcdDAFIFTModelDecoderSupport.getParachuteJumpAreaFileName           (),
            TLcdDAFIFTModelDecoderSupport.getParachuteJumpAreaSegmentFileName    (),
            TLcdDAFIFTModelDecoderSupport.getProcedureFileName                   (),
            TLcdDAFIFTModelDecoderSupport.getRefuelingTrackFileName              (),
            TLcdDAFIFTModelDecoderSupport.getRefuelingTrackCenterFileName        (),
            TLcdDAFIFTModelDecoderSupport.getRefuelingTrackPointsFileName        (),
            TLcdDAFIFTModelDecoderSupport.getRefuelingTrackSchedulingFileName    (),
            TLcdDAFIFTModelDecoderSupport.getRefuelingTrackSegmentFileName       (),
            TLcdDAFIFTModelDecoderSupport.getSpecialUseAirspaceFileName          (),
            TLcdDAFIFTModelDecoderSupport.getSpecialUseAirspaceSegmentFileName   (),
            TLcdDAFIFTModelDecoderSupport.getTerminalClimbRateFileName           (),
            TLcdDAFIFTModelDecoderSupport.getVFRRouteFileName                    (),
            TLcdDAFIFTModelDecoderSupport.getVFRRouteSegmentFileName             (),
            TLcdDAFIFTModelDecoderSupport.getWayPointFileName                    (),
    };

    for ( int i = 0; i < input_file_names.length ; i++ ) {
      String fileName = input_file_names[ i ];
      sendStatusMessage( "-- Processing file : " + fileName );
      processData( fileName );
    }
    for ( int i = 0; i < input_file_names.length ; i++ ) {
      String fileName = input_file_names[ i ];
      sendStatusMessage( "-- Post-Processing file : " + fileName );
      postProcessData( fileName );
    }

    writeDAFIFTPropertiesFile();
  }

  /////////////////////////////////////////
  //////       private methods       //////
  /////////////////////////////////////////

  /**
   * Initializes fInputDirectory and fOutputDirectory.
   * If fOutputDirectory exists, it will be deleted. The latest is
   * done because otherwise new data would be appended to old data.
   */
  private void setupDirectories() {
    //initialize input directory
    fInputDirectory = getDirectory();

    //initialize output directory
    fOutputDirectory = fProperties.getProperty( "split_directory", "split/" ).replace( '\\', '/' );
    if ( !fOutputDirectory.endsWith( "/" ) ) fOutputDirectory += "/";

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
   * Returns a <code>File</code> object pointing to the directory chosen by the user. The user may
   * write this directory in the "fullall_directory" property in the properties file or he may choose
   * it with a fileChooser.
   *
   * @return a <code>File</code> object pointing to the directory chosen by the user.
   */
  private File getDirectory() {
    String result_name = fProperties.getProperty( "fullall_directory" );
    if ( result_name != null ) {
      File result = new File( result_name );
      if ( result.exists() ) {
        return result;
      }
      else {
        sLogger.error("Directory " + result_name + " does not exist!");
      }
    }

    File default_dir = new File( fProperties.getProperty( "filechooser_initial_directory", System.getProperties().getProperty( "user.dir" ) ) );
    JFileChooser file_chooser = new JFileChooser( default_dir );

    file_chooser.setDialogTitle( "Choose a directory whose files have to be split:" );
    file_chooser.setDialogType( JFileChooser.OPEN_DIALOG );
    file_chooser.setFileSelectionMode( JFileChooser.DIRECTORIES_ONLY );

    int return_value = file_chooser.showOpenDialog( null );
    return return_value == JFileChooser.APPROVE_OPTION ? file_chooser.getSelectedFile() : null;
  }

  /**
   * Deletes the destination directory. The destination directory has a sub directory
   * for each ICAO region. Each sub directory contains a file for each file type.
   * <p/>
   * For each sub directory, all files should be deleted. Then the sub directory itself
   * can be deleted and finally, if all sub directories are deleted, we can remove the
   * destination directory.
   * <p/>
   * Note: this method is not recursively written. So if you change the directory
   * structure (you add sub directories in the sub directories for example),
   * then you'll have to change this method.
   *
   * @param aDirectory The directory to be deleted.
   * @return a boolean indicating whether the directory is deleted or not.
   */
  private boolean deleteOutputDirectory( File aDirectory ) {
    boolean all_deleted = true;
    if ( aDirectory.isDirectory() ) {
      String[] children = aDirectory.list();
      for ( int i = 0; i < children.length ; i++ ) {
        all_deleted &= deleteOutputDirectory( new File( aDirectory, children[ i ] ) );
      }
    }
    return all_deleted && aDirectory.delete();
  }

  /**
   * Reads the file with aDAFIFTFileName as file name found in fInputDirectory. For each ICAO region
   * found in the input file, a new directory with name fOutputDirectory+ICAO_region is created. All
   * records with the same ICAO region are written to the file with aDAFIFTFileName in this new directory.
   *
   * @param aDAFIFTFileName The file name of the DAFIFT data to read.
   */
  private void processData( String aDAFIFTFileName ) {
    if ( sLogger.isTraceEnabled() ) {
      sLogger.trace(" Preparing data using ICAO regions: " + aDAFIFTFileName);
    }

    int icao_region_field = TLcdDAFIFTICAORegionFilter.findICAOCodeField( aDAFIFTFileName );
    if ( icao_region_field == -1 ) {
      // Use the method 'postProcessData' to split this file.
      return;
    }

    fDecoderErrorMessages = "";

    BufferedReader reader = null;
    try {
      InputStream input_stream = getInputStream( fInputDirectory, aDAFIFTFileName );
      reader = new BufferedReader( new InputStreamReader( input_stream ), 10000 );

      // The first record should not be handles, because it contains the field headers.
      int      record_count  = TLcdDAFIFTModelDecoderSupport.getRecordFieldCount( aDAFIFTFileName );
      String[] record_header = readRecord( reader, record_count );    // heading record

      //there are some differences in record field count between different dafif releases, this code
      //takes this into account
      while ( record_header[ record_header.length - 1 ] == null ) {
        final String[] newHeader = new String[record_header.length - 1];
        System.arraycopy( record_header, 0, newHeader, 0, record_header.length - 1 );
        record_header = newHeader;
        record_count = record_header.length;
      }

      int num_rec = 0;
      String[] record;
      while ( ( record = readRecord( reader, record_count ) ) != null ) {
        if ( num_rec % 1000 == 0 ) {
          sendStatusMessage( "  " + num_rec + " records read." );
        }
        num_rec++;

        // An ICAO record can contain an ICAO code, therefore only use first two characters.
        String icao_region = record[ icao_region_field ].substring( 0, 2 );
        if ( sLogger.isTraceEnabled() && icao_region.length() == 0 ) {
          sLogger.trace( "No ICAO region found in file" + aDAFIFTFileName + " record = " + record.toString() );
        }

        if ( icao_region.length() != 0 && !icao_region.equals( "^^" ) ) {
          // Write record to file in output_dir
          String output_dir = fOutputDirectory + icao_region + "/";
          Writer writer     = getWriter( output_dir, aDAFIFTFileName, record_header );
          writeRecord( writer, record );

          ///////////////////////////////////////////////////////////////////
          //////      Extra processing needed for dependent files      //////
          ///////////////////////////////////////////////////////////////////

          // Records from a dependent file may require an object from other ICAO regions than the current
          // one. This part will add the required records to the corresponding hash table filter.
          // E.g.:
          // A procedure object from ICAO region 'EB' needs a waypoint from ICAO region 'LF' to create a
          // terminal segment. Therefore the key defining the required waypoint is added to the hash table
          // fWayPointFilters.

          if ( TLcdDAFIFTModelDecoderSupport.getAerodromeNavaidFileName().equals( aDAFIFTFileName ) ) {
            updateNavaidFilter(
                    icao_region,
                    record[ AERODROME_NAVAID_IDENTIFIER   ], record[ AERODROME_NAVAID_TYPE     ],
                    record[ AERODROME_NAVAID_COUNTRY_CODE ], record[ AERODROME_NAVAID_KEY_CODE ]
            );
          }

          if ( TLcdDAFIFTModelDecoderSupport.getATSRouteFileName().equals( aDAFIFTFileName ) ) {
            updateWayPointFilter( icao_region, record[ ATS_ROUTE_WAYPOINT_1_IDENTIFIER ], record[ ATS_ROUTE_WAYPOINT_1_COUNTRY_CODE ] );
            updateWayPointFilter( icao_region, record[ ATS_ROUTE_WAYPOINT_2_IDENTIFIER ], record[ ATS_ROUTE_WAYPOINT_2_COUNTRY_CODE ] );
          }

          if ( TLcdDAFIFTModelDecoderSupport.getHelipadFileName().equals( aDAFIFTFileName ) ) {
            updateHeliportFilter( icao_region, record[ HELIPAD_HELIPORT_IDENTIFIER ] );
          }

          if ( TLcdDAFIFTModelDecoderSupport.getHoldingFileName().equals( aDAFIFTFileName ) ) {
            updateWayPointFilter(
                    icao_region,
                    record[ HOLDING_WAYPOINT_IDENTIFIER   ],
                    record[ HOLDING_WAYPOINT_COUNTRY_CODE ]
            );
          }

          if ( TLcdDAFIFTModelDecoderSupport.getILSFileName().equals( aDAFIFTFileName ) ) {
            updateRunwayFilter(
                    icao_region,
                    record[ ILS_AERODROME_IDENTIFIER ],
                    record[ ILS_RUNWAY_IDENTIFIER    ]
            );
          }

          if ( TLcdDAFIFTModelDecoderSupport.getRunwayFileName().equals( aDAFIFTFileName ) ) {
            updateAerodromeFilter( icao_region, record[ RUNWAY_AERODROME_IDENTIFIER ] );
          }

          if ( TLcdDAFIFTModelDecoderSupport.getTerminalSegmentFileName().equals( aDAFIFTFileName ) ) {
            String aerodrome_identifier = record[ TERMINAL_SEGMENT_AERODROME_IDENTIFIER ];
            updateAerodromeFilter( icao_region, aerodrome_identifier );
            updateHeliportFilter ( icao_region, aerodrome_identifier );
            updateNavaidFilter(
                    icao_region,
                    record[ TERMINAL_SEGMENT_NAVAID_1_IDENTIFIER   ], record[ TERMINAL_SEGMENT_NAVAID_1_TYPE     ],
                    record[ TERMINAL_SEGMENT_NAVAID_1_COUNTRY_CODE ], record[ TERMINAL_SEGMENT_NAVAID_1_KEY_CODE ]
            );
            updateNavaidFilter   ( icao_region,
                    record[ TERMINAL_SEGMENT_NAVAID_2_IDENTIFIER   ], record[ TERMINAL_SEGMENT_NAVAID_2_TYPE     ],
                    record[ TERMINAL_SEGMENT_NAVAID_2_COUNTRY_CODE ], record[ TERMINAL_SEGMENT_NAVAID_2_KEY_CODE ]
            );

            // Transition identifier
            String transition_identifier = record[ TERMINAL_SEGMENT_TRANSITION_IDENTIFIER ];
            if ( transition_identifier.length() > 2 ) {
              String runway_id = transition_identifier.substring( 2 );
              updateILSFilter   ( icao_region, aerodrome_identifier, runway_id, record[ TERMINAL_SEGMENT_NAVAID_1_TYPE ] );    // Type navaid 1
              updateILSFilter   ( icao_region, aerodrome_identifier, runway_id, record[ TERMINAL_SEGMENT_NAVAID_2_TYPE ] );    // Type navaid 2
              updateRunwayFilter( icao_region, aerodrome_identifier, runway_id );
            }

            // Waypoint identifier
            String waypoint_identifier = record[ TERMINAL_SEGMENT_WAYPOINT_IDENTIFIER ];
            updateWayPointFilter( icao_region, waypoint_identifier, record[ TERMINAL_SEGMENT_WAYPOINT_COUNTRY_CODE ] );
            if ( waypoint_identifier.length() > 2 ) {
              updateRunwayFilter( icao_region, aerodrome_identifier, waypoint_identifier.substring( 2 ) );
            }
          }

          ///////////////////////////////////////////////////////////////////
          //////     Extra processing needed for independent files     //////
          ///////////////////////////////////////////////////////////////////

          // The processed record may be needed by objects from other ICAO regions, therefore it will be added
          // to the target file of that ICAO region.
          // E.g.:
          // A procedure object from ICAO region 'EB' needs a waypoint from ICAO region 'LF' to create a
          // terminal segment. Therefore the current waypoint record is added to the waypoint file of the
          // ICAO region 'EB'.

          if ( TLcdDAFIFTModelDecoderSupport.getAerodromeFileName().equals( aDAFIFTFileName ) ) {
            addToOtherICAORegions( fAerodromeFilters, icao_region, aDAFIFTFileName, record_header, record );
          }

          if ( TLcdDAFIFTModelDecoderSupport.getHeliportFileName().equals( aDAFIFTFileName ) ) {
            addToOtherICAORegions( fHeliportFilters, icao_region, aDAFIFTFileName, record_header, record );
          }

          if ( TLcdDAFIFTModelDecoderSupport.getILSFileName().equals( aDAFIFTFileName ) ) {
            addToOtherICAORegions( fILSFilters, icao_region, aDAFIFTFileName, record_header, record );
          }

          if ( TLcdDAFIFTModelDecoderSupport.getNavaidFileName().equals( aDAFIFTFileName ) ) {
            addToOtherICAORegions( fNavaidFilters, icao_region, aDAFIFTFileName, record_header, record );
          }

          if ( TLcdDAFIFTModelDecoderSupport.getRunwayFileName().equals( aDAFIFTFileName ) ) {
            addToOtherICAORegions( fRunwayFilters, icao_region, aDAFIFTFileName, record_header, record );
          }

          if ( TLcdDAFIFTModelDecoderSupport.getWayPointFileName().equals( aDAFIFTFileName ) ) {
            addToOtherICAORegions( fWayPointFilters, icao_region, aDAFIFTFileName, record_header, record );
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

  /**
   * Reads the file with aDAFIFTFileName as file name found in fInputDirectory. For each ICAO region
   * found in the input file, a new directory with name fOutputDirectory+ICAO_region is created. All
   * records with the same ICAO region are written to the file with aDAFIFTFileName in this new directory.
   *
   * @param aDAFIFTFileName The file name of the DAFIFT data to read.
   */
  private void postProcessData( String aDAFIFTFileName ) {
    if ( sLogger.isTraceEnabled() ) {
      sLogger.trace(" Preparing data using identifiers: " + aDAFIFTFileName);
    }

    int icao_region_field = TLcdDAFIFTICAORegionFilter.findICAOCodeField( aDAFIFTFileName );
    if ( icao_region_field != -1 ) {
      // File already split using the method 'processData'
      return;
    }

    // File that pass the previous filter...
    //   - ARPT\\ACOM.TXT
    //   - ARPT\\ACOM_RMK.TXT
    //   - ARPT\\ANAV.TXT
    //   - ARPT\\AGEAR.TXT
    //   - ARPT\\ILS.TXT
    //   - ARPT\\RWY.TXT
    //   - HLPT\\PAD.TXT
    //   - HLPT\\HCOM.TXT
    //   - HLPT\\HCOM_RMK.TXT
    //   - MTR\\MTR_OV.TXT
    //   - ORTCA\\ORTCA.TXT
    //   - VFR\\VFR_RTE_SEG.TXT
    //
    // The ARPT and HLPT subfiles will check if the aerodrome identifier is available in the parent file.
    // The MTR subfiles will check if the military training route identifier is available in the parent file.
    // The VFR subfiles will check if the heliport identifier is available in the parent file.
    //
    // The ORTCA file does not have an ICAO region and does not have a parent file. Therefor the file will
    // not be added to the split directories.

    String dafift_parent_file_name = TLcdDAFIFTModelDecoderSupport.getMainObjectSourceFileName( aDAFIFTFileName );
    if ( dafift_parent_file_name == null || dafift_parent_file_name.equals( aDAFIFTFileName ) ) {
      sLogger.trace(" No parent source file found for " + aDAFIFTFileName);
      return;
    }

    fDecoderErrorMessages = "";

    try {
      // Read all records from the source file.
      Map<String, List<String[]>> records = new Hashtable<String, List<String[]>>();
      String[] record_header = readRecordsFromFile( aDAFIFTFileName, records );

      // Post process records.
      postProcessRecords( records, record_header, aDAFIFTFileName, dafift_parent_file_name );

      // Are all records processed ?
      sendStatusMessage( "    Remaining record " + records.size() );
    }
    catch ( IOException ioe ) {
      sLogger.error(ioe.getMessage(), ioe);
    }
    finally {
      closeWriters();
    }
  }

  private String[] readRecordsFromFile( String aFileName, Map<String, List<String[]>> aRecords ) throws IOException {
    BufferedReader reader = null;
    try {
      // Find input file
      InputStream stream = getInputStream( fInputDirectory, aFileName );
      reader = new BufferedReader( new InputStreamReader( stream ), 10000 );

      // The first record should not be handles, because it contains the field headers.
      int      record_count  = TLcdDAFIFTModelDecoderSupport.getRecordFieldCount( aFileName );
      String[] record_header = readRecord( reader, record_count );
      String[] record;

      int number_records  = 0;
      while ( ( record = readRecord( reader, record_count ) ) != null ) {
        if ( number_records % 1000 == 0 ) {
          sendStatusMessage( "  " + number_records + " records read." );
        }
        number_records++;

        String key  = record[ 0 ];
        List<String[]> list = aRecords.get( key );
        if ( list == null ) {
          aRecords.put( key, (list = new ArrayList<String[]>()) );
        }
        list.add( record );
      }
      return record_header;
    }
    finally {
      if ( reader != null ) {
        reader.close();
      }
    }
  }

  private void postProcessRecords( Map<String, List<String[]>> aDAFIFTRecords,
                                   String[] aDAFIFTRecordHeader,
                                   String   aDAFIFTSourceFileName,
                                   String   aDAFIFTParentFileName ) throws IOException {
    File output_directory = new File( fOutputDirectory );
    if ( !output_directory.isDirectory() ) {
      sLogger.error(" The output directory " + fOutputDirectory + " is not a directory!");
      return;
    }

    // Process all ICAO regions in the output directory.
    String[] children = output_directory.list();
    for ( int i = 0; i < children.length ; i++ ) {
      // for every icao region found, process file
      postProcessICAORegion( children[ i ], aDAFIFTRecords, aDAFIFTRecordHeader, aDAFIFTSourceFileName, aDAFIFTParentFileName );
    }
  }

  private void postProcessICAORegion( String   aICAORegion,
 Map<String, List<String[]>> aDAFIFTRecords,
                                      String[] aDAFIFTRecordHeader,
                                      String   aDAFIFTSourceFileName,
                                      String   aDAFIFTParentFileName ) throws IOException {
    sendStatusMessage( "  -- Post-Processing ICAO region '" + aICAORegion + "'" );
    BufferedReader parent_reader = null;
    try {
      // Create output directory
      String output_directory = fOutputDirectory + aICAORegion + "/";

      // Find parent file for this ICAO region
      InputStream parent_input_stream = getInputStream( output_directory, aDAFIFTParentFileName );
      parent_reader = new BufferedReader( new InputStreamReader( parent_input_stream ), 10000 );

      // Read only first field of parent source file.
      String[] parent_record = readRecord( parent_reader, 1 );
      if ( parent_record == null ) {
        sLogger.error(" Cannot read records from parent source file " + aDAFIFTParentFileName);
        return;
      }

      // Store the identifiers in a set.
      while ( ( parent_record = readRecord( parent_reader, 1 ) ) != null ) {
        String identifier = parent_record[0];

        List<?> list = aDAFIFTRecords.get( identifier );
        if ( list != null ) {
          for ( int i = 0 ; i < list.size() ; i++ ) {
            // Write record to file in output_directory
            Writer writer = getWriter( output_directory, aDAFIFTSourceFileName, aDAFIFTRecordHeader );
            writeRecord( writer, (String[]) list.get( i ) );
          }

          // Since the list is already processed, it can be removed.
          aDAFIFTRecords.remove( identifier );
        }
      }
    }
    catch ( FileNotFoundException aException ) {
      // Parent file not found, this can happen
      // do not process this aDAFIFTFileName.
    }
    finally {
      if ( parent_reader != null ) {
        parent_reader.close();
      }
    }
  }

  /**
   * Returns an <code>InputStream</code> object pointing to the file called aFileName in the directory aDirectory.
   *
   * @param aDirectory The directory to create the inputstream for.
   * @param aFileName  The file name to create the inputstream for.
   * @return an <code>InputStream</code> object pointing to the file called aFileName in the directory aDirectory.
   * @throws IOException if the <code>InputStream</code> could not be created for the given arguments.
   */
  private InputStream getInputStream( File aDirectory, String aFileName ) throws IOException {
    if ( aDirectory == null ) {
      throw new NullPointerException( "The given source is null." );
    }
    return getInputStream( aDirectory.getAbsolutePath(), aFileName );
  }

  /**
   * Returns an <code>InputStream</code> object pointing to the file called aFileName in the directory aDirectoryName.
   *
   * @param aDirectoryName The directory to create the inputstream for.
   * @param aFileName      The file name to create the inputstream for.
   * @return an <code>InputStream</code> object pointing to the file called aFileName in the directory aDirectoryName.
   * @throws IOException if the <code>InputStream</code> could not be created for the given arguments.
   */
  private InputStream getInputStream( String aDirectoryName, String aFileName ) throws IOException {
    TLcdIOUtil IO_util = new TLcdIOUtil();
    IO_util.setSourceName( getPath( aDirectoryName, aFileName ) );
    return IO_util.retrieveInputStream();
  }

  /**
   * Returns a <code>BufferedWriter</code> object. For performance reasons, the buffered writer is stored
   * in a hashtable using the path as key. If a writer for an already used path is needed, the corresponding
   * entry in the hashtable is returned.
   * <p/>
   * The given <code>aRecordHeader</code> is added to new files as first record.
   *
   * @param aDirectoryName The directory name to retrieve the writer for.
   * @param aFileName      The file name to retrieve the writer for.
   * @param aRecordHeader  The first record of a new file containing the header information.
   * @return a <code>BufferedWriter</code> object.
   * @throws IOException if the <code>BufferedWriter</code> could not be created for the given arguments.
   */
  private Writer getWriter( String aDirectoryName, String aFileName, String[] aRecordHeader ) throws IOException {
    if ( sLogger.isTraceEnabled() ) {
      sLogger.trace( "Getting writer for " + aDirectoryName + aFileName );
    }

    String path   = getPath( aDirectoryName, aFileName );
    Writer writer = fOutputFileTable.get( path );
    if ( writer == null ) {
      File out_file = new File( path );
      out_file.getParentFile().mkdirs();
      writer = new BufferedWriter( new OutputStreamWriter( new FileOutputStream( out_file, true ) ), 10000 );
      if ( out_file.length() == 0 ) {
        writeRecord( writer, aRecordHeader );
      }
      fOutputFileTable.put( path, writer );
    }
    return writer;
  }

  /**
   * Closes all open <code>Writer</code>s.
   */
  private void closeWriters() {
    Enumeration<Writer> elements = fOutputFileTable.elements();
    while ( elements.hasMoreElements() ) {
      Writer w = elements.nextElement();
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
   * Reads one record from the given reader.
   *
   * @param aReader     The reader to be read of.
   * @param aFieldCount The number of record field to read.
   * @return the record read from aReader or NULL if the end of the stream has been reached
   * @throws IOException if an error occurs during the reading process
   */
  private static String[] readRecord( BufferedReader aReader, int aFieldCount ) throws IOException {
    String line = aReader.readLine();
    if ( line != null ) {
      int index = 0, start_index = 0, stop_index;

      String[] result = new String[aFieldCount];
      while ( ( stop_index = line.indexOf( '\t', start_index ) ) != -1 && index < aFieldCount ) {
        result[ index++ ] = line.substring( start_index, stop_index );
        start_index = stop_index + 1;
      }
      if ( index < aFieldCount ) {
        result[ index ] = line.substring( start_index );
      }
      return result;
    }
    return null;
  }

  /**
   * Write the given record to the specified writer.
   *
   * @param aRecord The record to write.
   * @param aWriter The writer to write to.
   * @throws IOException if the writing fails.
   */
  private static void writeRecord( Writer aWriter, String[] aRecord ) throws IOException {
    for ( int i = 0 ; i < aRecord.length - 1 ; i++ ) {
      aWriter.write( aRecord[i] + '\t' );
    }
    aWriter.write( aRecord[aRecord.length - 1] + "\r\n" );
  }

  /**
   * Returns the concatenation of aDirectoryName and aFileName.
   *
   * @param aDirectoryName The directory name to concatenate.
   * @param aFileName      The file name to concatenate.
   * @return the concatenation of aDirectoryName and aFileName.
   */
  private static String getPath( String aDirectoryName, String aFileName ) {
    String path = aDirectoryName.replace( '\\', '/' );
    if ( !path.endsWith( "/" ) ) {
      path += "/";
    }
    return path + aFileName;
  }

  /**
   * Writes a toc file in the top directory and all ICAO region subdirectories.
   */
  private void writeDAFIFTPropertiesFile() {
    File output_directory = new File( fOutputDirectory );
    if ( !output_directory.isDirectory() ) {
      sLogger.error(" The output directory " + fOutputDirectory + " is not a directory!");
      return;
    }

    // Add dafift.toc file to each ICAO region subdirectories.
    Properties properties = new Properties();
    String[]   children   = output_directory.list();
    for ( int i = 0; i < children.length ; i++ ) {
      // for every icao region found, process file
      String icao_region = children[ i ];
      properties.put( "icao.region", icao_region );
      writeDAFIFTPropertiesFile( properties, fOutputDirectory + icao_region + "/", "DAFIFT Properties for ICAO region '" + icao_region + "'" );
    }

    // Add dafift.toc file to the top directory.
    writeDAFIFTPropertiesFile( new Properties(), fOutputDirectory, "DAFIFT Properties" );
  }

  private void writeDAFIFTPropertiesFile( Properties aProperties, String aDirectory, String aComment ) {
    try {
      File toc_file = new File( aDirectory + DAFIFT_CONFIG_FILE_NAME );
      aProperties.store( new FileOutputStream( toc_file ), aComment );
    }
    catch ( IOException e ) {
      sLogger.error(e.getMessage(), e);
    }
  }

  private void updateAerodromeFilter( String aICAORegion, String aIdentifier ) {
    TLcdDAFIFTAerodromeRecordFilter filter
            = (TLcdDAFIFTAerodromeRecordFilter) fAerodromeFilters.get( aICAORegion );
    if ( filter == null ) {
      fAerodromeFilters.put( aICAORegion, (filter = new TLcdDAFIFTAerodromeRecordFilter()) );
    }
    filter.addAerodrome( aIdentifier );
  }

  private void updateHeliportFilter( String aICAORegion, String aIdentifier ) {
    TLcdDAFIFTAerodromeRecordFilter filter
            = (TLcdDAFIFTAerodromeRecordFilter) fHeliportFilters.get( aICAORegion );
    if ( filter == null ) {
      fHeliportFilters.put( aICAORegion, (filter = new TLcdDAFIFTAerodromeRecordFilter()) );
    }
    filter.addAerodrome( aIdentifier );
  }

  private void updateILSFilter( String aICAORegion, String aAerodromeIdentifier, String aRunwayIdentifier, String aComponentType ) {
    TLcdDAFIFTILSRecordFilter filter
            = (TLcdDAFIFTILSRecordFilter) fILSFilters.get( aICAORegion );
    if ( filter == null ) {
      fILSFilters.put( aICAORegion, (filter = new TLcdDAFIFTILSRecordFilter()) );
    }
    filter.addILS( aAerodromeIdentifier, aRunwayIdentifier, aComponentType );
  }

  private void updateNavaidFilter( String aICAORegion, String aIdentifier, String aType, String aCountryCode, String aKeyCode ) {
    TLcdDAFIFTNavaidRecordFilter filter
            = (TLcdDAFIFTNavaidRecordFilter) fNavaidFilters.get( aICAORegion );
    if ( filter == null ) {
      fNavaidFilters.put( aICAORegion, (filter = new TLcdDAFIFTNavaidRecordFilter()) );
    }
    filter.addNavaid( aIdentifier, aType, aCountryCode, aKeyCode );
  }

  private void updateRunwayFilter( String aICAORegion, String aAerodromeIdentifier, String aRunwayIdentifier ) {
    TLcdDAFIFTRunwayRecordFilter filter
            = (TLcdDAFIFTRunwayRecordFilter) fRunwayFilters.get( aICAORegion );
    if ( filter == null ) {
      fRunwayFilters.put( aICAORegion, (filter = new TLcdDAFIFTRunwayRecordFilter()) );
    }
    filter.addRunway( aAerodromeIdentifier, aRunwayIdentifier );
  }

  private void updateWayPointFilter( String aICAORegion, String aIdentifier, String aCountryCode ) {
    TLcdDAFIFTWayPointRecordFilter filter
            = (TLcdDAFIFTWayPointRecordFilter) fWayPointFilters.get( aICAORegion );
    if ( filter == null ) {
      fWayPointFilters.put( aICAORegion, (filter = new TLcdDAFIFTWayPointRecordFilter()) );
    }
    filter.addWayPoint( aIdentifier, aCountryCode );
  }

  private void addToOtherICAORegions( Map<String, ILcdDAFIFTRecordFilter> aFilterList,
                                      String   aICAORegion,
                                      String   aDAFIFTFileName,
                                      String[] aRecordHeader,
                                      String[] aRecord ) throws IOException {
    for ( Iterator<String> it = aFilterList.keySet().iterator(); it.hasNext(); ) {
      String icao_region_key = it.next();
      // Only process other ICAO regions.
      if ( !icao_region_key.equals( aICAORegion ) ) {
        // Find filter.
        ILcdDAFIFTRecordFilter filter
                = aFilterList.get( icao_region_key );
        if ( filter != null && filter.accept( aRecord, aDAFIFTFileName ) ) {
          // If record is accepted, add record to different ICAO region file.
          String output_dir = fOutputDirectory + icao_region_key + "/";
          Writer writer     = getWriter( output_dir, aDAFIFTFileName, aRecordHeader );
          writeRecord( writer, aRecord );
        }
      }
    }
  }

  /////////////////////////////////////
  //////       main method       //////
  /////////////////////////////////////


}

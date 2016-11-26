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
package samples.fusion.tilestore.coverageverifier;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;

import com.luciad.fusion.core.ALfnEnvironment;
import com.luciad.fusion.core.TLfnTileCoordinates;
import com.luciad.fusion.tilestore.ALfnCoverage;
import com.luciad.fusion.tilestore.ALfnTileStore;
import com.luciad.fusion.tilestore.ILfnReadCallback;
import com.luciad.fusion.tilestore.TLfnDigestTileStore;
import com.luciad.fusion.tilestore.TLfnFileSystemTileStore;

/**
 * Utility for verifying the integrity of a coverage by comparing the computed signature on the tile data with the
 * signature that was originally recorded when the tiles were put in the Tile Store.
 * <p/>
 * Usage: CoverageVerifier pathToTileStore coverageName
 */
public class CoverageVerifier {

  public static void main(String[] aArgs) throws Throwable {
    if (aArgs.length != 2) {
      System.out.println("Verifies the integrity of a coverage on the file system.");
      System.out.println("The coverage needs to have message digest signatures enabled.");
      System.out.println("It scans all tiles and verifies their integrity, logging a message upon");
      System.out.println("mismatch.");
      System.out.println("It only works for Tile Stores on the file system, not for remote Tile Stores.");
      System.out.println();
      System.out.println("USAGE");
      System.out.println();
      System.out.println("    fusion.tilestore.coverageverifier <path-to-tile store> <coverage-name>");
      System.exit(-1);
    }
    File tileStore = new File(aArgs[0]);
    String coverage = aArgs[1];
    verifyCoverage(tileStore, coverage, new InvalidMessageDigestHandler());
  }

  public static void verifyCoverage(File aTileStore, String aCoverageName,
                                    TLfnDigestTileStore.InvalidMessageDigestHandler aInvalidMessageDigestHandler)
      throws Exception {
    ALfnEnvironment environment = null;
    TLfnFileSystemTileStore tileStore = null;
    try {
      environment = ALfnEnvironment.newInstance();
      tileStore = new TLfnFileSystemTileStore(aTileStore, false, environment);
      TLfnDigestTileStore digestTileStore = new TLfnDigestTileStore(tileStore, true, aInvalidMessageDigestHandler);
      ALfnCoverage coverage = digestTileStore.getCoverage(aCoverageName);
      if (coverage == null) {
        System.out.println("Coverage " + aCoverageName + " doesn't exist on this Tile Store.");
        System.exit(0);
      }
      if (coverage.getMetadata().getMessageDigestAlgorithm() == null) {
        //If checksums for the given coverage are disabled, show a warning message
        System.out.println("Verification for coverage " +
                           aCoverageName +
                           " had nothing to do because the coverage has no checksums.");
        return;
      }

      ReadTileCallback tileCallback = new ReadTileCallback();
      EmptyWritableByteChannel data = new EmptyWritableByteChannel();

      int tileCounter = 0;
      for (TLfnTileCoordinates coords : coverage.getTileCoordinates()) {
        coverage.getTile(coords, -1, data, null, tileCallback).get();
        tileCounter++;
      }
      System.out.println("Verification done (" + tileCounter + " tiles verified).");
    } finally {
      if (tileStore != null) {
        tileStore.close();
      }
      if (environment != null) {
        environment.close();
      }
    }
  }

  /**
   * Handler which logs invalid signatures on the console output.
   */
  private static class InvalidMessageDigestHandler extends TLfnDigestTileStore.InvalidMessageDigestHandler {

    @Override
    public void invalidMessageDigest(ALfnCoverage aCoverage, TLfnTileCoordinates aTileCoordinates) {
      System.out.println("Invalid tile signature for tile " + aTileCoordinates.toString());
    }
  }

  /**
   * Dummy handler which only logs errors on the console.
   */
  private static class ReadTileCallback implements ILfnReadCallback {
    @Override
    public void found(long aContentLength, long aModificationTime) {
      //do nothing
    }

    @Override
    public void completed(ALfnTileStore.ReadResult aResult) {
      //do nothing
    }

    @Override
    public void threw(Throwable aThrowable) {
      log(aThrowable);
    }
    private void log(Throwable aThrowable) {
      aThrowable.printStackTrace();
    }
  }

  /**
   * Dummy writable byte channel which does nothing.
   */
  private static class EmptyWritableByteChannel implements WritableByteChannel {

    @Override
    public int write(ByteBuffer aData) throws IOException {
      return aData.remaining();
    }

    @Override
    public boolean isOpen() {
      return true;
    }

    @Override
    public void close() throws IOException {
    }
  }

}


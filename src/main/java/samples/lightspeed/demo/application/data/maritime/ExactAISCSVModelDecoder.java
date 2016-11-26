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
package samples.lightspeed.demo.application.data.maritime;

import static samples.lightspeed.demo.application.data.maritime.CSVConstants.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.luciad.io.ILcdInputStreamFactory;
import com.luciad.reference.TLcdGeodeticReference;
import com.luciad.util.logging.ILcdLogger;
import com.luciad.util.logging.TLcdLoggerFactory;

import samples.lightspeed.demo.application.data.maritime.ExactAISModelDescriptor.NavigationalStatus;

/**
 * Model decoder for ExactAIS csv files.
 */
public class ExactAISCSVModelDecoder extends ExactAISModelDecoder {

  private static final ILcdLogger sLogger = TLcdLoggerFactory.getLogger(ExactAISCSVModelDecoder.class);

  public ExactAISCSVModelDecoder() {
    super();
  }

  public ExactAISCSVModelDecoder(ILcdInputStreamFactory aInputStreamFactory) {
    super(aInputStreamFactory);
  }

  @Override
  public String getDisplayName() {
    return "CSV";
  }

  @Override
  public boolean canDecodeSource(String aSourceName) {
    return aSourceName.toLowerCase().endsWith(".csv");
  }

  private static String unquote(String aToken) {
    if (aToken == null) {
      return "";
    }
    if (aToken.length() == 0) {
      return "";
    }
    if (aToken.contains("\"")) {
      return aToken.replaceAll("\"", "");
    }
    return aToken;
  }

  @Override
  public void stream(String aSourceName, Callback aCallback, final ExecutorService aExecutorService) throws IOException {
    aCallback.modelReference(new TLcdGeodeticReference());

    long tmin = Long.MAX_VALUE;
    long tmax = Long.MIN_VALUE;

    if (aExecutorService != null) {
      // Read thread
      ArrayBlockingQueue<Future> records = new ArrayBlockingQueue<>(100000);
      Future<Void> readFuture = aExecutorService.submit(new ReadTask(aSourceName, records, aExecutorService, this.getInputStreamFactory()));

      // Pass results to callback
      try {
        while (true) {
          Future future = records.take();
          if (future == ReadTask.TERMINATOR_RECORD) {
            break;
          }
          Object o = future.get();
          callbackRecord(o, aCallback);
          if (o instanceof AISPlot) {
            tmin = Math.min(tmin, ((AISPlot) o).getTimeStamp());
            tmax = Math.max(tmax, ((AISPlot) o).getTimeStamp());
          }
        }
      } catch (Exception aE) {
        try {
          readFuture.cancel(true);
          Future f;
          while ((f = records.poll(10, TimeUnit.SECONDS)) != null) {
            f.cancel(true);
          }
        } catch (Exception ignored) {
        }
        throw new IOException(aE);
      }
    } else {
      // Simple single-threaded implementation
      try (BufferedReader reader = createReader(aSourceName, getInputStreamFactory())) {
        int lnum = 0;

        // Parse the header
        Header header = readHeader(reader);
        lnum++;

        // Parse the data
        String ln;
        while ((ln = reader.readLine()) != null) {
          lnum++;
          Object o = decodeRecord(ln, header);
          callbackRecord(o, aCallback);
          if (o instanceof AISPlot) {
            tmin = Math.min(tmin, ((AISPlot) o).getTimeStamp());
            tmax = Math.max(tmax, ((AISPlot) o).getTimeStamp());
          }
        }
      }
    }

    aCallback.timeRange(tmin, tmax);
  }

  private static Header readHeader(BufferedReader aReader) throws IOException {
    return new Header(Arrays.asList(split(aReader.readLine())));
  }

  private static Object decodeRecord(String aLine, Header aHeader) {
    String[] tokens = split(aLine);

    int plotId;
    try {
      plotId = getInt(tokens, aHeader.iID);
    } catch (Exception e) {
      if (sLogger.isDebugEnabled()) {
        sLogger.debug("Could not parse plot id for " + aLine + "(ID @ " + aHeader.iID + ")", e);
      }
      return null;
    }

    int msgID = getInt(tokens, aHeader.iMsgID, -1);

    if (msgID == SHIP_DESCRIPTOR_MESSAGE_ID) {
      String name = unquote(tokens[aHeader.iName]);
      String callSign = unquote(tokens[aHeader.iCallSign]);
      int type = getInt(tokens, aHeader.iShipType, -1);
      String dest = unquote(tokens[aHeader.iDest]);

      int dimToBow = getInt(tokens, aHeader.iDimToBow, -1);
      int dimToStern = getInt(tokens, aHeader.iDimToStern, -1);
      int dimToPort = getInt(tokens, aHeader.iDimToPort, -1);
      int dimToStarboard = getInt(tokens, aHeader.iDimToStarboard, -1);
      int draught = getInt(tokens, aHeader.iDraught, -1);
      int length = -1;
      if (dimToBow >= 0 && dimToBow < 512 && dimToStern >= 0 && dimToStern < 512 && dimToBow + dimToStern > 0) {
        length = dimToBow + dimToStern;
      }
      int width = -1;
      if (dimToPort >= 0 && dimToPort < 512 && dimToStarboard >= 0 && dimToStarboard < 512 && dimToPort + dimToStarboard > 0) {
        width = dimToPort + dimToStarboard;
      }

      return new ExactAISModelDescriptor.ShipDescriptor(
          plotId, name, callSign, type, dest, length, width, draught
      );
    } else {
      double lon, lat;
      try {
        String x = unquote(tokens[aHeader.iX]);
        String y = unquote(tokens[aHeader.iY]);
        lon = Double.parseDouble(x);
        lat = Double.parseDouble(y);
      } catch (Exception e) {
        return null;
      }

      double heading = getDouble(tokens, aHeader.iO, 0);

      long timestamp = 0;
      try {
        Date date = new SimpleDateFormat("yyyyMMdd_HHmmss").parse(unquote(tokens[aHeader.iTSec]));
        int tmsec = getInt(tokens, aHeader.iTMSec, 0);
        timestamp = date.getTime() + tmsec;
      } catch (Exception ignored) {
      }

      int status = getInt(tokens, aHeader.iNavStatus, NavigationalStatus.NOT_DEFINED);

      return new AISPlot(
          lon,
          lat,
          heading,
          timestamp,
          plotId,
          status
      );
    }
  }

  private void callbackRecord(Object aO, Callback aCallback) throws IOException {
    if (aO instanceof ExactAISModelDescriptor.ShipDescriptor) {
      aCallback.shipDescriptor((ExactAISModelDescriptor.ShipDescriptor) aO);
    } else if (aO instanceof AISPlot) {
      aCallback.plot(0, (AISPlot) aO);
    }
  }

  private static int getInt(String[] aTokens, int aId, int aDefaultValue) {
    if (aId >= 0 && aId < aTokens.length) {
      try {
        return getInt(aTokens, aId);
      } catch (NumberFormatException ignored) {
      }
    }
    return aDefaultValue;
  }

  private static int getInt(String[] aTokens, int aId) {
    return Integer.parseInt(unquote(aTokens[aId]));
  }

  private static double getDouble(String[] aTokens, int aId, double aDefaultValue) {
    if (aId >= 0 && aId < aTokens.length) {
      try {
        return getDouble(aTokens, aId);
      } catch (NumberFormatException ignored) {
      }
    }
    return aDefaultValue;
  }

  private static double getDouble(String[] aTokens, int aId) {
    return Double.parseDouble(unquote(aTokens[aId]));
  }

  private static String[] split(String aLine) {
    ArrayList<String> tokens = new ArrayList<String>();

    int i = 0;
    boolean quote = false;
    int start = 0;
    while (i < aLine.length()) {
      if (aLine.charAt(i) == '"') {
        quote = !quote;
      } else if (aLine.charAt(i) == ',') {
        if (!quote) {
          tokens.add(aLine.substring(start, i));
          start = i + 1;
        }
      }
      i++;
    }

    return tokens.toArray(new String[tokens.size()]);

//    return aLine.split( ",", -1 );
  }

  private static BufferedReader createReader(String aSourceName, ILcdInputStreamFactory aInputStreamFactory) throws IOException {
    return new BufferedReader(new InputStreamReader(aInputStreamFactory.createInputStream(aSourceName)), 1024 * 1024 * 8);
  }

  private static class Header {
    final int iMsgID;
    final int iX;
    final int iY;
    final int iO;
    final int iID;
    final int iTSec;
    final int iTMSec;
    final int iNavStatus;
    final int iName;
    final int iCallSign;
    final int iShipType;
    final int iDest;
    final int iDimToBow;
    final int iDimToStern;
    final int iDimToPort;
    final int iDimToStarboard;
    final int iDraught;

    Header(List<String> aHeaders) {
      iMsgID = aHeaders.indexOf(MSG_TYPE);

      iX = aHeaders.indexOf(POS_X);
      iY = aHeaders.indexOf(POS_Y);
      iO = aHeaders.indexOf(HEADING);
      iID = aHeaders.indexOf(ID);
      iTSec = aHeaders.indexOf(TIME_SEC);
      iTMSec = aHeaders.indexOf(TIME_MSEC);
      iNavStatus = aHeaders.indexOf(NAV_STATUS);

      iName = aHeaders.indexOf(VESSEL_NAME);
      iCallSign = aHeaders.indexOf(CALL_SIGN);
      iShipType = aHeaders.indexOf(SHIP_TYPE);
      iDest = aHeaders.indexOf(DESTINATION);
      iDimToBow = aHeaders.indexOf(DIMENSION_TO_BOW);
      iDimToStern = aHeaders.indexOf(DIMENSION_TO_STERN);
      iDimToPort = aHeaders.indexOf(DIMENSION_TO_PORT);
      iDimToStarboard = aHeaders.indexOf(DIMENSION_TO_STARBOARD);
      iDraught = aHeaders.indexOf(DRAUGHT);
    }
  }

  private static class ReadTask implements Callable<Void> {
    private static final Future TERMINATOR_RECORD = new Future() {
      @Override
      public boolean cancel(boolean mayInterruptIfRunning) {
        return false;
      }

      @Override
      public boolean isCancelled() {
        return false;
      }

      @Override
      public boolean isDone() {
        return true;
      }

      @Override
      public Object get() throws InterruptedException, ExecutionException {
        return null;
      }

      @Override
      public Object get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return null;
      }
    };

    private final String fSourceName;
    private final BlockingQueue<Future> fRecords;
    private final ExecutorService fExecutorService;
    private final ILcdInputStreamFactory fInputStreamFactory;

    ReadTask(String aSourceName, BlockingQueue<Future> aRecords, ExecutorService aExecutorService, ILcdInputStreamFactory aInputStreamFactory) {
      fSourceName = aSourceName;
      fRecords = aRecords;
      fExecutorService = aExecutorService;
      fInputStreamFactory = aInputStreamFactory;
    }

    @Override
    public Void call() throws Exception {
      try (BufferedReader reader = createReader(fSourceName, fInputStreamFactory)) {
        if (Thread.interrupted()) {
          throw new InterruptedException();
        }
        // Parse the header
        final Header header = readHeader(reader);

        // Parse records
        String line;
        while ((line = reader.readLine()) != null) {
          if (line.length() > 0) {
            fRecords.put(fExecutorService.submit(new DecodeTask(line, header)));
          }
        }
      } finally {
        fRecords.put(TERMINATOR_RECORD);
      }
      return null;
    }
  }

  private static class DecodeTask implements Callable<Object> {
    private final String fLine;
    private final Header fHeader;

    DecodeTask(String aLine, Header aHeader) {
      fLine = aLine;
      fHeader = aHeader;
    }

    @Override
    public Object call() throws Exception {
      return decodeRecord(fLine, fHeader);
    }
  }
}

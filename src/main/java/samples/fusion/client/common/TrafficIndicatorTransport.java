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
package samples.fusion.client.common;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;

import com.luciad.io.transport.ILcdTransport;
import com.luciad.io.transport.TLcdTransportRequest;
import com.luciad.io.transport.TLcdTransportResponse;
import com.luciad.util.ILcdDisposable;

/**
 * An example of a custom transport implementation that decorates another transport and notifies a traffic indicator
 * upon incoming traffic. It also counts the number of bytes transferred. Note that only the <em>content</em> of each
 * response is counted as traffic, but not the headers and such.
 */
public class TrafficIndicatorTransport implements ILcdTransport, ILcdDisposable {

  private final ILcdTransport fDelegate;

  private final TrafficIndicator fTrafficIndicator;

  public TrafficIndicatorTransport(ILcdTransport aDelegate, TrafficIndicator aTrafficIndicator) {
    fDelegate = aDelegate;
    fTrafficIndicator = aTrafficIndicator;
  }

  public TLcdTransportResponse performRequest(TLcdTransportRequest aRequest) throws IOException {
    // Indicate that there is a new request
    final TrafficIndicator.RequestProgress progress = fTrafficIndicator.newRequest();
    try {
      // Perform the request
      TLcdTransportResponse response = fDelegate.performRequest(aRequest);
      // Create a response with a channel that indicates the progress of a request
      final ReadableByteChannel responseContent = response.getContent();
      if (response.getContent() == null) {
        progress.done();
        return response;
      }
      ReadableByteChannel wrappedResponseContent = new ReadableByteChannel() {

        public int read(ByteBuffer aTarget) throws IOException {
          int numBytesReceived = responseContent.read(aTarget);
          // Indicate that we received some data
          progress.received(numBytesReceived);
          return numBytesReceived;
        }

        public boolean isOpen() {
          return responseContent.isOpen();
        }

        public void close() throws IOException {
          if (isOpen()) {
            responseContent.close();
            // Indicate that the request is done
            progress.done();
          }
        }
      };
      return new TLcdTransportResponse(response.getResponseCode(), response.getResponseReason(), wrappedResponseContent,
                                       response.getHeaders());
    } catch (IOException | RuntimeException ex) {
      progress.done();
      throw ex;
    }
  }

  @Override
  public void dispose() {
    if (fDelegate instanceof ILcdDisposable) {
      ((ILcdDisposable) fDelegate).dispose();
    }
  }
}

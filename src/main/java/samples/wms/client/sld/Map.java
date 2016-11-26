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
package samples.wms.client.sld;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Point;
import java.awt.Toolkit;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Vector;

import javax.swing.JPanel;

/**
 * This class is used as a proxy that constructs the url to send to the Web Map Server
 * and that paints the image retrieved from the Luciad Web Map Server.
 */
class Map extends JPanel {

  private static final String REQUEST_STRING = "REQUEST";
  private static final String GETMAP_REQUEST_STRING = REQUEST_STRING + "=GetMap";
  private static final String SRS_STRING = "SRS";
  private static final String WIDTH_STRING = "WIDTH";
  private static final String HEIGHT_STRING = "HEIGHT";
  private static final String BBOX_STRING = "BBOX";
  private static final String LAYERS_STRING = "LAYERS";
  private static final String STYLES_STRING = "STYLES";
  private static final String SLD_STRING = "SLD";
  private static final String BGCOLOR_STRING = "BGCOLOR";
  private static final String TRANSPARENT_STRING = "TRANSPARENT";
  private static final String FORMAT_STRING = "FORMAT";

  private static final String PNG_FORMAT = "image/png";
  private static final String JPEG_FORMAT = "image/jpeg";
  private static final String GIF_FORMAT = "image/gif";

  private String fServletURL, fSLDURL;
  private String fSRS = "EPSG:4326";
  private String fLayers = "Usa_states,Usa_rivers,Usa_cities";
  private String fStyles = ",,";
  private String fFormat = PNG_FORMAT;
  private String fTransparent = "FALSE";

  private double fScale = 1.0;

  private double fCenterX = (-126.05384792138014 - 66.05384792138014) / 2.0;
  private double fCenterY = (12.382414804864258 + 67.6312835831448) / 2.0;
  private double fWidth = (126.05384792138014 - 66.05384792138014);


  private Image fMapImage = null;
  private byte[] fByteArray = new byte[10000];

  private String fPrevUri = "";

  /**
   * Sets the url of the servlet to which requests for maps should be send.
   * If the url does not end with '?' a '?' is appended.
   *
   * @param aServletURL a <code>String</code> representation of the servlet running the
   *                    Luciad Web Map Server.
   */
  public void setServletURL(String aServletURL) {
    if (aServletURL.endsWith("?")) {
      fServletURL = aServletURL;
    } else {
      fServletURL = aServletURL + "?";
    }
  }

  /**
   * Sets the url of the remote SLD, which will be used as a style library
   * for all map requests.
   *
   * @param aSLDURL a <code>String</code> representation of the SLD url.
   */
  public void setSLDURL(String aSLDURL) {
    fSLDURL = aSLDURL;
  }

  /**
   * Sets the layer and style IDs to be retrieved for each map.
   *
   * @param aLayers  A list of <code>String</code> objects representing layer IDs.
   * @param aStyles  A list of <code>String</code> objects representing style IDs.
   * @param aRepaint Whether the view must be updated or not.
   * @throws UnsupportedEncodingException if the platform does not support UTF-8 character encoding.
   */
  public void setLayers(Vector aLayers, Vector aStyles, boolean aRepaint) throws UnsupportedEncodingException {
    fLayers = "";
    fStyles = "";
    for (int i = 0; i < aLayers.size(); i++) {
      if (i > 0) {
        fLayers += ",";
        fStyles += ",";
      }
      fLayers += URLEncoder.encode((String) aLayers.get(i), "UTF-8");
      fStyles += URLEncoder.encode((String) aStyles.get(i), "UTF-8");
    }

    if (aRepaint) {
      if (fServletURL != null) {
        updateImage();
      }
      drawImage(getGraphics());
    }
  }

  void setMapFormat(String aMapFormat) {
    fFormat = aMapFormat;
  }

  public void paint(Graphics aGraphics) {
    super.paint(aGraphics);
    if (fMapImage == null && fServletURL != null) {
      updateImage();
    }
    drawImage(aGraphics);
  }

  private void updateImage() {
    fScale = fWidth / getWidth();

    Color color = getBackground();
    int rgb = color.getRGB();
    String bg_color = "0x" + Integer.toHexString(rgb & 0xffffff);

    String width = "" + getWidth();
    String height = "" + getHeight();

    double bbox_height = fWidth * getHeight() / getWidth();
    double bbox_min_x = fCenterX - fWidth / 2;
    double bbox_min_y = fCenterY - bbox_height / 2;
    double bbox_max_x = fCenterX + fWidth / 2;
    double bbox_max_y = fCenterY + bbox_height / 2;
    String bbox = "" + bbox_min_x + "," + bbox_min_y + "," + bbox_max_x + "," + bbox_max_y;

    try {
      String uri = fServletURL + GETMAP_REQUEST_STRING + "&" +
                   SRS_STRING + "=" + fSRS + "&" +
                   BBOX_STRING + "=" + bbox + "&" +
                   LAYERS_STRING + "=" + fLayers + "&" +
                   STYLES_STRING + "=" + fStyles + "&" +
                   SLD_STRING + "=" + fSLDURL + "&" +
                   WIDTH_STRING + "=" + width + "&" +
                   HEIGHT_STRING + "=" + height + "&" +
                   FORMAT_STRING + "=" + URLEncoder.encode(fFormat, "UTF-8") + "&" +
                   BGCOLOR_STRING + "=" + bg_color + "&" +
                   TRANSPARENT_STRING + "=" + fTransparent +
                   "&VERSION=1.1.1";

      if (!uri.equals(fPrevUri)) {
        loadImage(uri);
        fPrevUri = uri;
      }
    } catch (UnsupportedEncodingException exc) {
      exc.printStackTrace();
    }
  }

  private int loadImage(String aURI) {
    fMapImage = null;
    int current_pos = 0;
    InputStream is = null;
    URL url;
    URLConnection connection = null;
    try {
      boolean use_get = true;
      if (use_get) {
        url = new URL(aURI);
        connection = url.openConnection();
        is = connection.getInputStream();
      } else if (aURI.startsWith("http")) {
        int index = aURI.indexOf("?");
        String str_url = aURI.substring(0, index);
        String str_uri = (index == -1) ? null : aURI.substring(index + 1);
        url = new URL(str_url);
        connection = url.openConnection();
        connection.setUseCaches(false);
        connection.setDoOutput(true);
        ByteArrayOutputStream byte_stream = new ByteArrayOutputStream(512);
        PrintWriter out = new PrintWriter(byte_stream, true);
        out.print(str_uri);
        out.flush();
        connection.setRequestProperty("Content-Length", String.valueOf(byte_stream.size()));
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        byte_stream.writeTo(connection.getOutputStream());
        is = connection.getInputStream();
      }

      if (connection != null && connection.getContentType() != null &&
          (connection.getContentType().startsWith(PNG_FORMAT) ||
           connection.getContentType().startsWith(JPEG_FORMAT) ||
           connection.getContentType().startsWith(GIF_FORMAT))) {
        fMapImage = null;
        BufferedInputStream bis = new BufferedInputStream(is);
        int c;
        while ((c = bis.read()) != -1) {
          if (current_pos >= fByteArray.length) {
            byte[] new_byte_array = new byte[fByteArray.length * 2];
            System.arraycopy(fByteArray, 0, new_byte_array, 0, fByteArray.length);
            fByteArray = new_byte_array;
          }
          fByteArray[current_pos++] = (byte) c;
        }
        bis.close();

        try {
          fMapImage = Toolkit.getDefaultToolkit().createImage(fByteArray, 0, current_pos);

          MediaTracker media_tracker = new MediaTracker(this);
          media_tracker.addImage(fMapImage, 0);
          media_tracker.waitForAll();
        } catch (InterruptedException e) {
          // thread interrupted...
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      if (is != null) {
        try {
          is.close();
        } catch (IOException e) {
          // I/O error while closing inputstream -> nothing to do
        }
      }
    }
    return current_pos;
  }

  private void drawImage(Graphics aGraphics) {
    if (fMapImage != null) {
      aGraphics.drawImage(fMapImage, 0, 0, this);
    }
  }

  /**
   * Retrieves a new image from the Luciad Web Map Server of the map panned over
   * a distance X and Y and paints the retrieved image.
   *
   * @param aDeltaX the distance to pan in the direction of the X-axis.
   * @param aDeltaY the distance to pan in the direction of the Y-axis.
   */
  public void pan(int aDeltaX, int aDeltaY) {
    translate(aDeltaX, aDeltaY);
    if (fServletURL != null) {
      updateImage();
    }
    drawImage(getGraphics());
  }

  private void translate(int aDeltaX, int aDeltaY) {
    double dx = aDeltaX * fScale;
    double dy = aDeltaY * fScale;
    fCenterX -= dx;
    fCenterY += dy;
  }

  /**
   * Retrieves a new image from the Luciad Web Map Server of the map zoomed on
   * a specified point with a specified zoom factor and paints the retrieved image.
   *
   * @param aPoint  the point to zoom on.
   * @param aFactor the zooming factor.
   */
  public void zoom(Point aPoint, double aFactor) {
    int dx = getWidth() / 2 - aPoint.x;
    int dy = getHeight() / 2 - aPoint.y;
    translate(dx, dy);
    fScale /= aFactor;
    fWidth /= aFactor;

    if (fServletURL != null) {
      updateImage();
    }
    drawImage(getGraphics());
  }

}

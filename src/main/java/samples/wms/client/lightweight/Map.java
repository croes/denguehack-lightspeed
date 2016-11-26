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
package samples.wms.client.lightweight;

import java.awt.Color;
import java.awt.Font;
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
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import com.luciad.datamodel.ILcdDataObject;
import com.luciad.datamodel.TLcdCoreDataTypes;
import com.luciad.datamodel.TLcdDataModel;
import com.luciad.datamodel.TLcdDataModelBuilder;
import com.luciad.datamodel.TLcdDataObject;
import com.luciad.datamodel.TLcdDataProperty;
import com.luciad.model.ILcdModel;

import samples.common.dataObjectDisplayTree.DataObjectDisplayTree;
import samples.wms.common.WMSGetFeatureInfoModelFactory;

/**
 * This class is used as a proxy that constructs the GetMap and GetFeatureInfo urls
 * to send to the Luciad Web Map Server, and that paints the retrieved results.
 */
class Map extends JPanel {

  private static final String REQUEST_STRING = "REQUEST";
  private static final String GETMAP_REQUEST_STRING = REQUEST_STRING + "=GetMap";
  private static final String SRS_STRING = "SRS";
  private static final String WIDTH_STRING = "WIDTH";
  private static final String HEIGHT_STRING = "HEIGHT";
  private static final String BBOX_STRING = "BBOX";
  private static final String LAYERS_STRING = "LAYERS";
  private static final String BGCOLOR_STRING = "BGCOLOR";
  private static final String TRANSPARENT_STRING = "TRANSPARENT";
  private static final String FORMAT_STRING = "FORMAT";

  private static final String GET_FEATURE_INFO_REQUEST_STRING = REQUEST_STRING + "=GetFeatureInfo";
  private static final String INFO_FORMAT_STRING = "INFO_FORMAT";
  private static final String QUERY_LAYERS_STRING = "QUERY_LAYERS";
  private static final String FEATURE_COUNT_STRING = "FEATURE_COUNT";
  private static final String X_STRING = "X";
  private static final String Y_STRING = "Y";

  private static final String PNG_FORMAT = "image/png";
  private static final String JPEG_FORMAT = "image/jpeg";
  private static final String GIF_FORMAT = "image/gif";
  private static final String JSON_FORMAT = "application/json";

  private static final String MODEL = "model";
  private static final String SELECTION = "selection";

  private static final Font ARIAL = new Font("Arial", Font.BOLD, 14);

  private String fServletURL;
  private String fSRS = "EPSG:4326";
  private String fLayers = "Usa_states,Usa_rivers,Washington_DC_raster,Washington_DC_streets,Usa_cities";
  private String fFormat = JPEG_FORMAT;
  private String fTransparent = "FALSE";
  private String fFeatureInfoFormat = JSON_FORMAT;
  private String fQueryLayers = "Usa_cities,Usa_rivers,Washington_DC_streets";
  private int fFeatureCount = 1;

  private double fScale = 1.0;

  private double fCenterX = (-77.14874681843894 - 76.91437181843894) / 2.0;
  private double fCenterY = (38.872224971719454 + 38.930818721719454) / 2.0;
  private double fWidth = (77.14874681843894 - 76.91437181843894) / 2;


  private Image fMapImage = null;
  private byte[] fByteArray = new byte[10000];

  private String fPrevUri = "";

  // UI components to display GetFeatureInfo results
  private DataObjectDisplayTree fDataObjectDisplayTree;
  private JFrame fFrame;

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
   * Sets the image format (MIME type) for requested maps.
   *
   * @param aMapFormat the image format (MIME type) for requested maps.
   */
  void setMapFormat(String aMapFormat) {
    fFormat = aMapFormat;
  }

  /**
   * Sets the format (MIME type) for requested feature information.
   *
   * @param aFeatureInfoFormat the format (MIME type) for requested feature information.
   */
  void setFeatureInfoFormat(String aFeatureInfoFormat) {
    fFeatureInfoFormat = aFeatureInfoFormat;
  }

  public void paint(Graphics aGraphics) {
    super.paint(aGraphics);
    if (fMapImage == null && fServletURL != null) {
      updateImage();
    }
    drawImage(aGraphics);
  }

  /**
   * Loads the map from the Luciad Web Map Server corresponding to the
   * current settings, if these have been changed since the
   * last map request.
   */
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
                   WIDTH_STRING + "=" + width + "&" +
                   HEIGHT_STRING + "=" + height + "&" +
                   FORMAT_STRING + "=" + URLEncoder.encode(fFormat, "UTF-8") + "&" +
                   BGCOLOR_STRING + "=" + bg_color + "&" +
                   TRANSPARENT_STRING + "=" + fTransparent +
                   "&VERSION=1.1.1&STYLES=";

      if (!uri.equals(fPrevUri)) {
        loadImage(uri);
        fPrevUri = uri;
      }
    } catch (UnsupportedEncodingException exc) {
      exc.printStackTrace();
    }
  }

  /**
   * Executes the given GetMap request and tries to decode the result as an image.
   * If successful, the image is painted on the screen.
   *
   * @param aURI A GetMap request.
   */
  private void loadImage(String aURI) {
    fMapImage = null;

    InputStream inputstream = null;
    URLConnection connection = null;

    try {
      boolean use_get = true;
      if (use_get) {
        URL url = new URL(aURI);
        connection = url.openConnection();
        inputstream = connection.getInputStream();
      } else if (aURI.startsWith("http")) {
        int index = aURI.indexOf("?");
        String str_url = aURI.substring(0, index);
        String str_uri = (index == -1) ? null : aURI.substring(index + 1);
        URL url = new URL(str_url);
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
        inputstream = connection.getInputStream();
      }

      if (connection != null && connection.getContentType() != null &&
          (connection.getContentType().startsWith(PNG_FORMAT) ||
           connection.getContentType().startsWith(JPEG_FORMAT) ||
           connection.getContentType().startsWith(GIF_FORMAT))) {
        fMapImage = null;
        BufferedInputStream buffered_inputstream = new BufferedInputStream(inputstream);
        int current_pos = 0;
        int c;

        while ((c = buffered_inputstream.read()) != -1) {
          if (current_pos >= fByteArray.length) {
            byte[] new_byte_array = new byte[fByteArray.length * 2];
            System.arraycopy(fByteArray, 0, new_byte_array, 0, fByteArray.length);
            fByteArray = new_byte_array;
          }
          fByteArray[current_pos++] = (byte) c;
        }
        buffered_inputstream.close();

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
      if (inputstream != null) {
        try {
          inputstream.close();
        } catch (IOException e) {
          // I/O error while closing inputstream -> nothing to do
        }
      }
    }
  }

  /**
   * Draws the latest map image on the given Graphics.
   * If no map image is available, nothing is painted.
   *
   * @param aGraphics The Graphics on which the latest map image must be painted.
   */
  private void drawImage(Graphics aGraphics) {
    if (fMapImage != null) {
      aGraphics.drawImage(fMapImage, 0, 0, this);
    }
  }

  /**
   * Shows a set of ILcdDataObject's with their properties in a JTree.
   *
   * @param aDataObjects the list of data objects to be shown
   */
  private void showObjectTree(List<ILcdDataObject> aDataObjects) {
    if (fDataObjectDisplayTree == null) {
      fDataObjectDisplayTree = new DataObjectDisplayTree();
      fDataObjectDisplayTree.setRootVisible(false);
      fFrame = new JFrame();
      fFrame.getContentPane().add(new JScrollPane(fDataObjectDisplayTree));
    }

    fDataObjectDisplayTree.setDataObject(groupSelection(aDataObjects));
    for (int i = 0; i < fDataObjectDisplayTree.getRowCount(); i++) {
      fDataObjectDisplayTree.expandRow(i);
    }

    if (!fFrame.isVisible()) {
      fFrame.pack();
      fFrame.setVisible(true);
    }
    fFrame.toFront();
  }

  /**
   * Utility method for grouping a number of ILcdDataObject's into a single root data object, for display in a DataObjectDisplayTree.
   *
   * @param aDataObjects the list of data objects to be grouped
   * @return a single data object grouping the given list of data objects
   */
  private TLcdDataObject groupSelection(List<ILcdDataObject> aDataObjects) {
    TLcdDataModelBuilder rootModelBuilder = new TLcdDataModelBuilder("http://www.luciad.com/sample");
    rootModelBuilder.typeBuilder(MODEL)
                    .addProperty(SELECTION, TLcdCoreDataTypes.OBJECT_TYPE).collectionType(TLcdDataProperty.CollectionType.LIST);
    TLcdDataModel rootModel = rootModelBuilder.createDataModel();
    TLcdDataObject dataObject = new TLcdDataObject(rootModel.getDeclaredType(MODEL));
    dataObject.setValue(SELECTION, aDataObjects);
    return dataObject;
  }

  /**
   * Retrieves a new image from the Luciad Web Map Server of the map panned over
   * a distance X and Y and paints the retrieved image.
   *
   * @param aDeltaX the distance to pan in the direction of the X-axis, in pixels.
   * @param aDeltaY the distance to pan in the direction of the Y-axis, in pixels.
   */
  public void pan(int aDeltaX, int aDeltaY) {
    translate(aDeltaX, aDeltaY);
    if (fServletURL != null) {
      updateImage();
    }
    drawImage(getGraphics());
  }

  /**
   * Translates the center of the map over the distance aDeltaX
   * in the direction of the X-axis and over the distance aDeltaY
   * in the direction of the Y-axis.
   *
   * @param aDeltaX A distance in the X direction, in pixels.
   * @param aDeltaY A distance in the Y direction, in pixels.
   */
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

  /**
   * Requests feature information from the Luciad Web Map Server for the given coordinates,
   * and paints the result on the map at that position.
   *
   * @param aX The X coordinate of the position for which feature information must be requested, in pixels.
   * @param aY The Y coordinate of the position for which feature information must be requested, in pixels.
   */
  void selectAt(int aX, int aY) {
    if (fFeatureInfoFormat == null || fFeatureInfoFormat.length() == 0) {
      System.err.println("Can not perform selection: no feature info format set!");
    }

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

    String uri = fServletURL + GET_FEATURE_INFO_REQUEST_STRING + "&" +
                 SRS_STRING + "=" + fSRS + "&" +
                 BBOX_STRING + "=" + bbox + "&" +
                 LAYERS_STRING + "=" + fLayers + "&" +
                 QUERY_LAYERS_STRING + "=" + fQueryLayers + "&" +
                 WIDTH_STRING + "=" + width + "&" +
                 HEIGHT_STRING + "=" + height + "&" +
                 FORMAT_STRING + "=" + fFormat + "&" +
                 INFO_FORMAT_STRING + "=" + fFeatureInfoFormat + "&" +
                 FEATURE_COUNT_STRING + "=" + fFeatureCount + "&" +
                 X_STRING + "=" + aX + "&" +
                 Y_STRING + "=" + aY + "&" +
                 BGCOLOR_STRING + "=" + bg_color + "&" +
                 TRANSPARENT_STRING + "=" + fTransparent +
                 "&VERSION=1.1.1&STYLES=";

    InputStream inputStream = null;
    try {
      URL url = new URL(uri);
      inputStream = url.openStream();
      BufferedReader buffered_reader = new BufferedReader(new InputStreamReader(new BufferedInputStream(inputStream)));
      List<ILcdDataObject> features = new ArrayList<ILcdDataObject>();

      ILcdModel getFeatureInfoModel = WMSGetFeatureInfoModelFactory.convertToModel(inputStream, fFeatureInfoFormat, null, null, "FeatureInfo");
      if (getFeatureInfoModel != null) {
        Enumeration objects = getFeatureInfoModel.elements();
        while (objects.hasMoreElements()) {
          Object object = objects.nextElement();
          if (object instanceof ILcdDataObject) {
            features.add((ILcdDataObject) object);
          }
        }
      }
      inputStream.close();

      // Draw the result.
      Graphics graphics = getGraphics();
      drawImage(graphics);
      if (features.size() > 0) {
        showObjectTree(features);
      }

      buffered_reader.close();

    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      if (inputStream != null) {
        try {
          inputStream.close();
        } catch (IOException e) {
          // I/O error while closing inputstream -> nothing to do
        }
      }
    }
  }
}

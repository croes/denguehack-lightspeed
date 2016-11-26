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
package samples.gxy.rectification.util;

import java.awt.image.ColorModel;

import com.luciad.format.raster.ILcdRaster;
import com.luciad.format.raster.ILcdTile;
import com.luciad.shape.ILcdBounds;
import com.luciad.shape.ILcdPoint;
import com.luciad.shape.shape2D.ILcd2DEditableBounds;
import com.luciad.shape.shape2D.TLcdXYBounds;
import com.luciad.shape.shape3D.ILcd3DEditableBounds;
import com.luciad.util.ILcdAssocSet;
import com.luciad.util.TLcdAssocList;

/**
 * This class is a combination of an ILcdRaster and an ILcd2DEditableBounds. Its only purpose is to
 * allows us to modify the raster bounds.
 */
public class EditableRaster implements ILcdRaster, ILcd2DEditableBounds {
  private ILcdBounds fBounds;
  private ILcdTile[][] fTiles;

  private ColorModel fColorModel;

  private EmptyTile fEmptyTile;
  private ILcdTile fRecentTile;

  private double fRecentTileX;
  private double fRecentTileY;
  private double fRecentTileWidthFactor;
  private double fRecentTileHeightFactor;
  private double fTileWidth;
  private double fTileHeight;
  private double fPixelDensity;
  private int fRecentTileWidth;
  private int fRecentTileHeight;
  private int fType = -1;
  private int fPixelSize = -1;
  private int fDefaultValue;

  private transient ILcdAssocSet fCache;

  /**
   * Creates a new TLcdRaster. The tiling is regular, except possibly for the tiles in the
   * right-most column and in the bottom row. If the sums of the tile widths and the tile heights
   * extend beyond the bounds of the raster, the extending parts are considered to be padding and
   * they are ignored. For a raster with a uniform pixel density, the tile width (and similarly the
   * tile height) in model coordinates can be computed as follows:
   *
   * tile width = tile width in pixels / raster width in pixels * raster width
   *
   * @param aBounds       - the bounds of the raster in model coordinates.
   * @param aTiles        - the array of tiles of the raster.
   * @param aPixelDensity - an estimate of the number of pixels per unit area in model coordinates.
   * @param aDefaultValue - a default value in case the raster cannot resolve a point.
   * @param aColorModel   - optional ColorModel that overrides the ColorModel of the raster's
   *                      tiles.
   */
  public EditableRaster(ILcdBounds aBounds, ILcdTile[][] aTiles, double aPixelDensity,
                        int aDefaultValue, ColorModel aColorModel) {

    this(aBounds,
         aBounds.getWidth() / (double) aTiles[0].length, aBounds.getHeight() / (double) aTiles.length,
         aTiles, aPixelDensity, aDefaultValue, aColorModel);
  }

  /**
   * Creates a new TLcdRaster. The tiling is regular, except possibly for the tiles in the
   * right-most column and in the bottom row. If the sums of the tile widths and the tile heights
   * extend beyond the bounds of the raster, the extending parts are considered to be padding and
   * they are ignored. For a raster with a uniform pixel density, the tile width (and similarly
   * the tile height) in model coordinates can be computed as follows:
   *
   * tile width = tile width in pixels / raster width in pixels * raster width
   *
   * @param aBounds       - the bounds of the raster in model coordinates.
   * @param aTileWidth    - the width of the tiles in model coordinates.
   * @param aTileHeight   - the height of the tiles in model coordinates.
   * @param aTiles        - the array of tiles of the raster.
   * @param aPixelDensity - an estimate of the number of pixels per unit area in model
   *                      coordinates.
   * @param aDefaultValue - a default value in case the raster cannot resolve a point.
   * @param aColorModel   - optional ColorModel that overrides the ColorModel of the raster's
   *                      tiles.
   */
  public EditableRaster(ILcdBounds aBounds,
                        double aTileWidth,
                        double aTileHeight,
                        ILcdTile[][] aTiles,
                        double aPixelDensity,
                        int aDefaultValue,
                        ColorModel aColorModel) {

    fBounds = aBounds;
    fTileWidth = aTileWidth;
    fTileHeight = aTileHeight;
    fTiles = aTiles;
    fPixelDensity = aPixelDensity;
    fDefaultValue = aDefaultValue;
    fColorModel = aColorModel;
    fEmptyTile = new EmptyTile(getType(), fDefaultValue, aColorModel);
  }

  @Override
  public boolean isDefined() {
    return fBounds.isDefined();
  }

  /**
   * Sets the ColorModel of this ILcdRaster  to aColorModel.
   *
   * @param aColorModel - of type ColorModel.
   */
  public void setColorModel(ColorModel aColorModel) {
    fColorModel = aColorModel;
    clearCache();
  }

  /**
   * Returns the color model.
   *
   * @return the color model.
   */
  public ColorModel getColorModel() {
    if (fColorModel != null) {
      return fColorModel;
    }

    ILcdTile tile = fRecentTile != null ? fRecentTile : fTiles[0][0];
    fColorModel = tile.getColorModel();
    if (fColorModel != null) {
      return fColorModel;
    }

    for (int row = 0; row < fTiles.length; row++) {
      for (int col = 0; col < fTiles[row].length; col++) {
        fColorModel = fTiles[row][col].getColorModel();
        if (fColorModel != null) {
          return fColorModel;
        }
      }
    }
    return null;
  }

  /**
   * Returns the tile width
   *
   * @return the tile width
   */
  public double getTileWidth() {
    return fTileWidth;
  }

  /**
   * Returns the tile height.
   *
   * @return the tile height.
   */
  public double getTileHeight() {
    return fTileHeight;
  }

  /**
   * Returns the numbers of rows.
   *
   * @return the numbers of rows.
   */
  public int getTileRowCount() {
    return fTiles.length;
  }

  /**
   * Returns the number of columns.
   *
   * @return the number of columns.
   */
  public int getTileColumnCount() {
    return fTiles[0].length;
  }

  /**
   * Returns the pixel density.
   *
   * @return the pixel density
   */
  public double getPixelDensity() {
    return fPixelDensity;
  }

  /**
   * Returns the type.
   *
   * @return the type.
   */
  public int getType() {
    if (fType != -1) {
      return fType;
    }

    ILcdTile tile = fRecentTile != null ? fRecentTile : fTiles[0][0];
    fType = tile.getType();
    if (fType != -1) {
      return fType;
    }

    for (int row = 0; row < fTiles.length; row++) {
      for (int col = 0; col < fTiles[row].length; col++) {
        fType = fTiles[row][col].getType();
        if (fType != -1) {
          return fType;
        }
      }
    }
    return ILcdTile.INT;
  }

  /**
   * Returns the pixel size.
   *
   * @return the pixel size.
   */
  public int getPixelSize() {
    if (fPixelSize != -1) {
      return fPixelSize;
    }

    for (int row = 0; row < fTiles.length; row++) {
      for (int col = 0; col < fTiles[row].length; col++) {
        fPixelSize = fTiles[row][col].getPixelSize();
        if (fPixelSize != -1) {
          return fPixelSize;
        }
      }
    }

    return 32;
  }

  /**
   * Returns the default value.
   *
   * @return the default value.
   */
  public int getDefaultValue() {
    return fDefaultValue;
  }

  /**
   * Sets the default value
   *
   * @param aDefaultValue - a default value of type Integer.
   */
  public void setDefaultValue(int aDefaultValue) {
    fDefaultValue = aDefaultValue;
    fEmptyTile.setDefaultValue(aDefaultValue);
    clearCache();
  }

  /**
   * Retrieves a tile from this ILcdRaster.
   *
   * @param aRow    - the row in this ILcdRaster matrix of the desired tile.
   * @param aColumn - the column in this ILcdRaster matrix of the desired tile.
   *
   * @return a tile.
   */
  public ILcdTile retrieveTile(int aRow, int aColumn) {
    return ((aRow >= 0 && aRow < fTiles.length && aColumn >= 0 && aColumn < fTiles[aRow].length) ? fTiles[aRow][aColumn] : fEmptyTile);
  }

  /**
   * Retrieves an integer value within the raster at a given location.
   *
   * @param aX the abscissa of the required value.
   * @param aY the ordinate of the required value.
   *
   * @return a value of type integer.
   */
  public int retrieveValue(double aX, double aY) {
    int tile_x;
    int tile_y;

    if (fRecentTile == null ||
        (tile_x = (int) Math.floor((aX - fRecentTileX) * fRecentTileWidthFactor)) < 0 ||
        tile_x >= fRecentTileWidth ||
        (tile_y = (int) Math.floor((fRecentTileY - aY) * fRecentTileHeightFactor)) < 0 ||
        tile_y >= fRecentTileHeight) {

      double raster_x = fBounds.getLocation().getX();
      double raster_y = fBounds.getLocation().getY() + fBounds.getHeight();

      double dx = aX - raster_x;
      double dy = raster_y - aY;

      if (dx < 0.0 ||
          dy < 0.0 ||
          dx >= fBounds.getWidth() ||
          dy >= fBounds.getHeight()) {
        return fDefaultValue;
      }

      dx /= fTileWidth;
      dy /= fTileHeight;

      int col = (int) dx;
      int row = (int) dy;

      if (row >= fTiles.length || col >= fTiles[0].length) {
        return fDefaultValue;
      }

      dx -= (double) col;
      dy -= (double) row;

      fRecentTile = fTiles[row][col];
      fRecentTileX = raster_x + col * fTileWidth;
      fRecentTileY = raster_y - row * fTileHeight;
      fRecentTileWidth = fRecentTile.getWidth();
      fRecentTileHeight = fRecentTile.getHeight();
      fRecentTileWidthFactor = (double) fRecentTileWidth / fTileWidth;
      fRecentTileHeightFactor = (double) fRecentTileHeight / fTileHeight;

      int border_tile_width = (int) ((fBounds.getWidth() - col * fTileWidth) * fRecentTileWidthFactor + 0.5);
      if (fRecentTileWidth > border_tile_width) {
        fRecentTileWidth = border_tile_width;
      }

      int border_tile_height = (int) ((fBounds.getHeight() - row * fTileHeight) * fRecentTileHeightFactor + 0.5);
      if (fRecentTileHeight > border_tile_height) {
        fRecentTileHeight = border_tile_height;
      }

      tile_x = (int) (dx * fRecentTileWidth);
      tile_y = (int) (dy * fRecentTileHeight);
    }

    return fRecentTile.retrieveValue(tile_x, tile_y);
  }

  /**
   * Gets the bounds of this raster.
   *
   * @return an ILcdBounds.
   */
  public ILcdBounds getBounds() {
    return fBounds;
  }

  /**
   * Inserts a cache Object corresponding to the given key Object.
   *
   * @param aKey    - the key Object that will be used to identify the Object. The key must
   *                therefore be a unique identifier, typically the caller itself:
   *                insertIntoCache(this, ...).
   * @param aObject - the Object to be cached.
   */
  public void insertIntoCache(Object aKey, Object aObject) {
    if (fCache == null) {
      fCache = new TLcdAssocList();
    }
    fCache.put(aKey, aObject);
  }

  /**
   * Looks up and returns the cached Object corresponding to the given key.
   *
   * @param aKey - the key Object that was used for storing the cache Object.
   *
   * @return the cached Object, or null if there is no Object corresponding to the given key.
   */
  public Object getCachedObject(Object aKey) {
    return fCache == null ? null : fCache.getValue(aKey);
  }

  /**
   * Looks up and removes the cached Object corresponding to the given key.
   *
   * @param aKey - the key Object that was used for storing the cache Object.
   *
   * @return the cached Object, or null if there was no Object corresponding to the given key.
   */
  public Object removeCachedObject(Object aKey) {
    Object value = null;
    if (fCache != null) {
      value = fCache.remove(aKey);
      if (fCache.size() == 0) {
        fCache = null;
      }
    }
    return value;
  }

  /**
   * Clears the cache.
   */
  public void clearCache() {
    fRecentTile = null;
    fCache = null;
    update();
  }

  /**
   * Returns the location of this raster.
   *
   * @return - of type ILcdPoint.
   */
  public ILcdPoint getLocation() {
    return fBounds.getLocation();
  }

  /**
   * Moves this ILcd2DEditableShape to the given point in the 2D space. The focus point is used as
   * the handle by which the shape is moved. Only the first two dimensions of the ILcdShape are
   * considered. The third dimension is left unchanged.
   *
   * @param ax - the x coordinate of the point.
   * @param ay - the y coordinate of the point.
   */
  public void move2D(double ax, double ay) {
    ((ILcd2DEditableBounds) fBounds).move2D(ax, ay);
    clearCache();
  }

  /**
   * Moves this ILcd2DEditableShape to the given point in the 2D space. The focus point is used as
   * the handle by which the shape is moved. Only the first two dimensions of the ILcdShape and the
   * ILcdPoint are considered. The third dimension is left unchanged.
   *
   * @param aPoint - the ILcdPoint to move to.
   */
  public void move2D(ILcdPoint aPoint) {
    ((ILcd2DEditableBounds) fBounds).move2D(aPoint);
    clearCache();
  }

  /**
   * Translates this ILcd2DEditableShape from its current position over the given translation vector
   * in the 2D space. Only the first two dimensions of the ILcdShape are considered. The third
   * dimension is left unchanged.
   *
   * @param aDeltaX - the x coordinate of the translation vector.
   * @param aDeltaY - the y coordinate of the translation vector.
   */
  public void translate2D(double aDeltaX, double aDeltaY) {
    ((ILcd2DEditableBounds) fBounds).translate2D(aDeltaX, aDeltaY);
    clearCache();
  }

  /**
   * Calculates the 2D intersection of this ILcd2DEditableBounds object and a given rectangle. The
   * result contains at least all the points that are contained both in this ILcdBounds objects and
   * in the rectangle. Only the first two dimensions of this ILcdBounds object are considered. It is
   * updated with the result. Its third dimension is left unchanged.
   *
   * @param aX      - the x coordinate of the rectangle.
   * @param aY      - the y coordinate of the rectangle.
   * @param aWidth  - the width of the rectangle.
   * @param aHeight - the height of the rectangle.
   */
  public void setTo2DIntersection(double aX, double aY, double aWidth, double aHeight) {
    ((ILcd2DEditableBounds) fBounds).setTo2DIntersection(aX, aY, aWidth, aHeight);
    clearCache();
  }

  /**
   * Calculates the 2D intersection of this ILcd2DEditableBounds and a given ILcdBounds. The result
   * contains at least all the points that are contained in both ILcdBounds objects. Only the first
   * two dimensions of the ILcdBounds objects are considered. This ILcd2DEditableBounds object is
   * updated with the result. Its third dimension is left unchanged.
   *
   * @param aBounds - the other ILcdBounds operand for the intersection.
   */
  public void setTo2DIntersection(ILcdBounds aBounds) {
    ((ILcd2DEditableBounds) fBounds).setTo2DIntersection(aBounds);
    clearCache();
  }

  /**
   * Calculates the 2D union of this ILcd2DEditableBounds object and a given rectangle. The result
   * contains at least all the points that are contained in the ILcdBounds object and in the
   * rectangle (and typically more). Only the first two dimensions of this ILcdBounds object are
   * considered. It is updated with the result. Its third dimension is left unchanged.
   *
   * @param aX      - the x coordinate of the rectangle.
   * @param aY      - the y coordinate of the rectangle.
   * @param aWidth  - the width of the rectangle.
   * @param aHeight - the height of the rectangle.
   */
  public void setTo2DUnion(double aX, double aY, double aWidth, double aHeight) {
    ((ILcd2DEditableBounds) fBounds).setTo2DUnion(aX, aY, aWidth, aHeight);
    clearCache();
  }

  /**
   * Calculates the 2D union of this ILcd2DEditableBounds and a given ILcdBounds. The result
   * contains at least all the points that are contained in either of the ILcdBounds objects (and
   * typically more). Only the first two dimensions of the ILcdBounds objects are considered. This
   * ILcd2DEditableBounds object is updated with the result. Its third dimension is left unchanged.
   *
   * @param aBounds - the other ILcdBounds operand for the union.
   */
  public void setTo2DUnion(ILcdBounds aBounds) {
    ((ILcd2DEditableBounds) fBounds).setTo2DUnion(aBounds);
    clearCache();
  }

  /**
   * Calculates the 2D extension of this ILcd2DEditableBounds object that contains a given point.
   * The result contains at least the given point and all the points that are contained in this
   * ILcd2DEditableBounds (and typically more). Only the first two dimensions of this ILcdBounds
   * object are considered. It is updated with the result. Its third dimension is left unchanged.
   *
   * @param aX - the x coordinate of the point.
   * @param aY - the y coordinate of the point.
   */
  public void setToIncludePoint2D(double aX, double aY) {
    ((ILcd2DEditableBounds) fBounds).setToIncludePoint2D(aX, aY);
    clearCache();
  }

  /**
   * Calculates the 2D extension of this ILcd2DEditableBounds object that contains a given
   * ILcdPoint. The result contains at least the given point and all the points that are contained
   * in this ILcd2DEditableBounds (and typically more). Only the first two dimensions of this
   * ILcdBounds object and the ILcdPoint are considered. This ILcdBounds object is updated with the
   * result. Its third dimension is left unchanged.
   *
   * @param aPoint - the ILcdPoint operand for the extension.
   */
  public void setToIncludePoint2D(ILcdPoint aPoint) {
    ((ILcd2DEditableBounds) fBounds).setToIncludePoint2D(aPoint);
    clearCache();
  }

  /**
   * Sets the width of the bounds (the extent along the x-axis).
   *
   * @param aWidth - the new width.
   */
  public void setWidth(double aWidth) {

    ((ILcd2DEditableBounds) fBounds).setWidth(aWidth);
    clearCache();
  }

  /**
   * Sets the height of the bounds (the extent along the y-axis).
   *
   * @param aHeight - the new height.
   */
  public void setHeight(double aHeight) {

    ((ILcd2DEditableBounds) fBounds).setHeight(aHeight);
    clearCache();
  }

  /**
   * Returns the width of the bounds of this raster.
   *
   * @return width, type integer.
   */
  public double getWidth() {
    return fBounds.getWidth();
  }

  /**
   * Returns the height of the bounds of this raster.
   *
   * @return height, type integer.
   */
  public double getHeight() {
    return fBounds.getHeight();
  }

  /**
   * Returns the depth of this raster.
   *
   * @return depth, type integer.
   */
  public double getDepth() {
    return fBounds.getDepth();
  }

  /**
   * Checks whether this ILcdBounds object interacts with the given ILcdBounds  object in the 2D
   * space. Only the first two dimensions of the ILcdBounds objects are considered.
   *
   * @param aBounds - the ILcdBounds to compare with.
   *
   * @return the boolean result of the interaction test.
   */
  public boolean interacts2D(ILcdBounds aBounds) {
    return fBounds.interacts2D(aBounds);
  }

  /**
   * Checks whether this ILcdBounds object interacts with the given rectangle in the 2D space. Only
   * the first two dimensions of the ILcdBounds object are considered.
   *
   * @param v  - the x coordinate of the rectangle.
   * @param v1 - the y coordinate of the rectangle.
   * @param v2 - the width of the rectangle.
   * @param v3 - the height of the rectangle.
   *
   * @return true if this ILcdBounds object touches or overlaps to any extent with the given
   *         rectangle, false otherwise.
   */
  public boolean interacts2D(double v, double v1, double v2, double v3) {
    return fBounds.interacts2D(v, v1, v2, v3);
  }

  /**
   * Checks whether this ILcdBounds object contains the given ILcdBounds  object in the 2D space.
   * Only the first two dimensions of the ILcdBounds objects are considered.
   *
   * @param aBounds - the ILcdBounds to compare with.
   *
   * @return the boolean result of the containment test.
   */
  public boolean contains2D(ILcdBounds aBounds) {
    return fBounds.contains2D(aBounds);
  }

  /**
   * Checks whether this ILcdBounds object contains the given rectangle in the 2D space. Only the
   * first two dimensions of the ILcdBounds object are considered.
   *
   * @param v  - the x coordinate of the rectangle.
   * @param v1 - the y coordinate of the rectangle.
   * @param v2 - the width of the rectangle.
   * @param v3 - the height of the rectangle.
   *
   * @return the boolean result of the containment test.
   */
  public boolean contains2D(double v, double v1, double v2, double v3) {
    return fBounds.contains2D(v, v1, v2, v3);
  }

  /**
   * Checks whether this ILcdBounds object interacts with the given ILcdBounds  object.
   *
   * @param aBounds - the ILcdBounds to compare with.
   *
   * @return the boolean result of the interaction test.
   */
  public boolean interacts3D(ILcdBounds aBounds) {
    return fBounds.interacts3D(aBounds);
  }

  /**
   * Checks whether this ILcdBounds object interacts with the given box in the 3D space.
   *
   * @param v  - the x coordinate of the box.
   * @param v1 - the y coordinate of the box.
   * @param v2 - the z coordinate of the box.
   * @param v3 - the width of the box.
   * @param v4 - the height of the box.
   * @param v5 - the depth of the box.
   *
   * @return true if this ILcdBounds object touches or overlaps to any extent with the given box,
   *         false otherwise.
   */
  public boolean interacts3D(double v, double v1, double v2, double v3, double v4, double v5) {
    return fBounds.interacts3D(v, v1, v2, v3, v4, v5);
  }

  /**
   * Checks whether this ILcdBounds object contains the given ILcdBounds  object in the 3D space.
   *
   * @param aBounds - the ILcdBounds to compare with.
   *
   * @return the boolean result of the containment test.
   */
  public boolean contains3D(ILcdBounds aBounds) {
    return fBounds.contains3D(aBounds);
  }

  /**
   * Checks whether this ILcdBounds object contains the given box in the 3D space.
   *
   * @param v  - the x coordinate of the point.
   * @param v1 - the y coordinate of the point.
   * @param v2 - the z coordinate of the point.
   *
   * @return the boolean result of the containment test.
   */
  public boolean contains3D(double v, double v1, double v2, double v3, double v4, double v5) {
    return fBounds.contains3D(v, v1, v2, v3, v4, v5);
  }

  /**
   * @return a copy of this ILcdBounds object that is also an ILcd2DEditableBounds. This makes sure
   *         that the first two dimensions of the copy are writable, even if the original ILcdBounds
   *         object may be read-only.
   */
  public ILcd2DEditableBounds cloneAs2DEditableBounds() {
    //return fBounds.cloneAs2DEditableBounds();
    /*ILcd2DEditableBounds fBoundsClone = new TLcdXYBounds(fBounds.getLocation().getX(),
                                                         fBounds.getLocation().getY(),
                                                         fBounds.getWidth(),
                                                         fBounds.getHeight());*/
    return fBounds.cloneAs2DEditableBounds();
  }

  /**
   * @return a copy of this ILcdBounds object that is also an ILcd3DEditableBounds. This makes sure
   *         that all three dimensions of the copy are writable, even if the original ILcdBounds
   *         object may be read-only.
   */
  public ILcd3DEditableBounds cloneAs3DEditableBounds() {
    return fBounds.cloneAs3DEditableBounds();
  }

  /**
   * @return the focus point of this ILcdShape.
   */
  public ILcdPoint getFocusPoint() {
    return fBounds.getFocusPoint();
  }

  /**
   * Checks whether this ILcdShape contains the given ILcdPoint in the 2D space. Only the first two
   * dimensions of the ILcdShape and the ILcdPoint are considered.
   *
   * @param aPoint - the ILcdPoint to test.
   *
   * @return the boolean result of the containment test.
   */
  public boolean contains2D(ILcdPoint aPoint) {
    return fBounds.contains2D(aPoint);
  }

  /**
   * Checks whether this ILcdShape contains the given point in the 2D space. Only the first two
   * dimensions of the ILcdShape  are considered.
   *
   * @param v  - the x coordinate of the point.
   * @param v1 - the y coordinate of the point.
   *
   * @return the boolean result of the containment test.
   */
  public boolean contains2D(double v, double v1) {
    return fBounds.contains2D(v, v1);
  }

  /**
   * Checks whether this ILcdShape contains the given ILcdPoint in the 3D space.
   *
   * @param aPoint - the ILcdPoint to test.
   *
   * @return the boolean result of the containment test.
   */
  public boolean contains3D(ILcdPoint aPoint) {
    return fBounds.contains3D(aPoint);
  }

  /**
   * Checks whether this ILcdShape contains the given point in the 3D space.
   *
   * @param v  - the x coordinate of the point.
   * @param v1 - the y coordinate of the point.
   * @param v2 - the z coordinate of the point.
   *
   * @return the boolean result of the containment test.
   */
  public boolean contains3D(double v, double v1, double v2) {
    return fBounds.contains3D(v, v1, v2);
  }

  /**
   * Makes Object.clone() public.
   *
   * @return clone type object.
   */
  public Object clone() {
    ILcdBounds bounds = new TLcdXYBounds(this.getBounds().getLocation().getX(),
                                         this.getBounds().getLocation().getY(),
                                         this.getBounds().getWidth(),
                                         this.getBounds().getHeight());

    return new EditableRaster(bounds, fTiles.clone(), getPixelDensity(), getDefaultValue(), getColorModel());
  }

  private boolean update() {

    // Assume tiles do not overlap with the raster bounds.
    fTileWidth = fBounds.getWidth() / getTileColumnCount();
    fTileHeight = fBounds.getHeight() / getTileRowCount();
    fPixelDensity = (fTiles[0][0].getWidth() * fTiles[0][0].getHeight()) / (getWidth() * getHeight());

    if (Double.isInfinite(fPixelDensity) || Double.isNaN(fPixelDensity)) {
      return false;
    } else {
      return true;
    }
  }

}

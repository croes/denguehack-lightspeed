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
package samples.earth.preprocessor.gui;

import javax.swing.SwingUtilities;

import com.luciad.earth.metadata.ILcdEarthAsset;
import com.luciad.earth.metadata.preprocessor.ILcdEarthPreprocessorProgressListener;
import com.luciad.earth.tileset.ILcdEarthTileSet;
import com.luciad.earth.tileset.ILcdEarthTileSetCoverage;

/**
 * A progress listener that shows the progress in a 2d view and updates the preprocessor panel.
 */
class PreprocessorProgressListener implements ILcdEarthPreprocessorProgressListener {

  private PreprocessorPanel fPreprocessorPanel;

  private boolean fStopped;

  private int fTotNumLevels;
  private long fTotNumTiles; // the total number of tiles for the entire processing of the assets (some part may already be done)
  private long fTotNumTilesToProcess; // the total number of tiles that still will be processed
  private long fTotNumTilesDone;

  private ILcdEarthAsset fCurrAsset;
  private long fCurrRegionW;
  private long fCurrRegionH;

  private long fTileUpdateRate;
  private long fNumTileUpdatesLeft;
  private long fCurrAssetTilesDone;

  public PreprocessorProgressListener(PreprocessorPanel aPreprocessorPanel) {
    fPreprocessorPanel = aPreprocessorPanel;
    fStopped = false;
    fTotNumLevels = -1;
  }

  public boolean progress(ILcdEarthTileSet aTileSet, ILcdEarthTileSetCoverage[] aCoverages,
                          final int aLevel, long aUpdateRegionX, long aUpdateRegionY,
                          long aUpdateRegionWidth, long aUpdateRegionHeight,
                          long aCurrentTileX, long aCurrentTileY) {
    if (isStopped()) {
      return false;
    }

    fTotNumTilesDone++;
    fCurrAssetTilesDone++;
    fNumTileUpdatesLeft--;
    if (fNumTileUpdatesLeft <= 0) {
      SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          fPreprocessorPanel.setAssetProgress(fCurrAsset, fCurrRegionW * fCurrRegionH, fCurrAssetTilesDone);
          fPreprocessorPanel.setOverallProgress(fTotNumTiles, (fTotNumTiles - fTotNumTilesToProcess) + fTotNumTilesDone,
                                                fTotNumTilesToProcess, fTotNumTilesDone);
        }
      });
      fNumTileUpdatesLeft = Math.min(fCurrRegionW * fCurrRegionH - fCurrAssetTilesDone, fTileUpdateRate);
    }

    return true;
  }

  public void preprocessingStarted(ILcdEarthTileSet aTileSet, ILcdEarthTileSetCoverage[] aCoverages,
                                   int aTotNumLevels, ILcdEarthAsset[] aAssets,
                                   long[] aNumAssetTiles, long[] aNumAssetTilesLeft) {
    fTotNumLevels = aTotNumLevels;
    fTotNumTiles = 0;
    for (long numTiles : aNumAssetTiles) {
      fTotNumTiles += numTiles;
    }
    fTotNumTilesToProcess = 0;
    for (long numTiles : aNumAssetTilesLeft) {
      fTotNumTilesToProcess += numTiles;
    }
    fTotNumTilesDone = 0;

    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        fPreprocessorPanel.setLevelProgress(fTotNumLevels, 0);
        fPreprocessorPanel.setOverallProgress(fTotNumTiles, (fTotNumTiles - fTotNumTilesToProcess) + fTotNumTilesDone,
                                              fTotNumTilesToProcess, fTotNumTilesDone);
      }
    });
  }

  public void preprocessingComplete() {
  }

  public void levelStarted(final int aLevel) {
    // recalculate level progress
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        int numLevelsDone = fTotNumLevels - (aLevel + 1);
        fPreprocessorPanel.setLevelProgress(fTotNumLevels, numLevelsDone);
      }
    });
  }

  public void levelComplete() {
  }

  public void assetStarted(final ILcdEarthAsset aAsset,
                           long aUpdateRegionX, long aUpdateRegionY,
                           long aUpdateRegionWidth, long aUpdateRegionHeight,
                           long aStartX, long aStartY) {
    fCurrAsset = aAsset;
    fCurrRegionW = aUpdateRegionWidth;
    fCurrRegionH = aUpdateRegionHeight;

    fTileUpdateRate = 32;
    fCurrAssetTilesDone = aUpdateRegionWidth * aUpdateRegionHeight -
                          ((aUpdateRegionX + aUpdateRegionWidth - aStartX - 1) * aUpdateRegionHeight +
                           (aUpdateRegionY + aUpdateRegionHeight - aStartY));
    fNumTileUpdatesLeft = Math.min(fCurrRegionW * fCurrRegionH - fCurrAssetTilesDone, fTileUpdateRate);

    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        fPreprocessorPanel.setAssetProgress(aAsset, fCurrRegionW * fCurrRegionH, fCurrAssetTilesDone);
        fPreprocessorPanel.setOverallProgress(fTotNumTiles, (fTotNumTiles - fTotNumTilesToProcess) + fTotNumTilesDone,
                                              fTotNumTilesToProcess, fTotNumTilesDone);
      }
    });
  }

  @Override
  public void assetComplete() {
  }

  public boolean isStopped() {
    return fStopped;
  }

  public void stop() {
    fStopped = true;
  }
}

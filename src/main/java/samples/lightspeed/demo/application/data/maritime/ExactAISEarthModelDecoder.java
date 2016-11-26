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

import java.io.IOException;
import java.util.Date;
import java.util.Properties;

import com.luciad.earth.model.TLcdEarthRepositoryModelDecoder;
import com.luciad.earth.repository.codec.TLcdEarthCompressorTileDataCodec;
import com.luciad.earth.tileset.ALcdEarthTile;
import com.luciad.earth.tileset.ILcdEarthTileSet;
import com.luciad.earth.tileset.ILcdEarthTileSetCoverage;
import com.luciad.earth.tileset.util.TLcdEarthMemoryCachingTileSet;
import com.luciad.model.ILcd2DBoundsInteractable;
import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelDecoder;
import com.luciad.model.ILcdTileDecoder;
import com.luciad.model.ILcdTileProvider;
import com.luciad.model.TLcdRegularTiled2DBoundsIndexedModel;
import com.luciad.reference.TLcdGeodeticReference;
import com.luciad.shape.shape2D.TLcdLonLatBounds;

/**
 * @author tomn
 * @since 2012.1
 */
@SuppressWarnings("deprecation")
public class ExactAISEarthModelDecoder implements ILcdModelDecoder {

  private TLcdEarthRepositoryModelDecoder fDelegate;

  public ExactAISEarthModelDecoder() {
    fDelegate = new TLcdEarthRepositoryModelDecoder();
    fDelegate.addTileDataCodec(
        new TLcdEarthCompressorTileDataCodec(new EarthTileCodec())
    );
  }

  @Override
  public String getDisplayName() {
    return "ExactAIS";
  }

  @Override
  public boolean canDecodeSource(String aSourceName) {
    return fDelegate.canDecodeSource(aSourceName);
  }

  @Override
  public ILcdModel decode(String aSourceName) throws IOException {
    ILcdModel m = fDelegate.decode(aSourceName);
    ILcdEarthTileSet ts = (ILcdEarthTileSet) m.elements().nextElement();

    TLcdRegularTiled2DBoundsIndexedModel t = new TLcdRegularTiled2DBoundsIndexedModel();
    ExactAISModelDescriptor desc = new ExactAISModelDescriptor("ExactAIS");
    desc.setMinTime(new Date(112, 0, 1, 0, 0).getTime());
    desc.setMaxTime(new Date(112, 3, 1, 0, 0).getTime());
    t.setModelDescriptor(desc);
    t.setModelReference(new TLcdGeodeticReference());
    t.setModelBounds(new TLcdLonLatBounds(-180, -90, 360, 180));
    t.setTileProvider(new MyTileProvider(ts));

    return t;
  }

  private static class MyTileProvider implements ILcdTileProvider {
    private ILcdEarthTileSet fTileSet;

    public MyTileProvider(ILcdEarthTileSet aTileSet) {
      fTileSet = new TLcdEarthMemoryCachingTileSet(aTileSet, 100);
    }

    @Override
    public int getNoOfRows() {
      return (int) fTileSet.getTileRowCount(0);
    }

    @Override
    public int getNoOfCols() {
      return (int) fTileSet.getTileColumnCount(0);
    }

    @Override
    public ILcd2DBoundsInteractable getTile(int aRow, int aColumn) {
//      System.out.println( "ExactAISEarthModelDecoder$MyTileProvider.getTile" );
//      System.out.println( "aRow = " + aRow );
//      System.out.println( "aColumn = " + aColumn );
      ILcdEarthTileSetCoverage coverage = fTileSet.getTileSetCoverage(0);
      try {
        ALcdEarthTile tile = fTileSet.produceTile(
            coverage,
            0, aColumn, aRow,
            coverage.getNativeGeoReference(),
            EarthTileCodec.DEC_FORMAT,
            null
        );

        if (tile != null) {
          ILcd2DBoundsInteractable data = (ILcd2DBoundsInteractable) tile.getData();
//          if ( data != null ) {
//            System.out.println( ( ( ILcdIntegerIndexedModel ) data ).size() + " plots" );
//          }
          return data;
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
      return null;
    }

    @Override
    public void loadProperties(String aPrefix, Properties aProperties) {
    }

    @Override
    public void setBasePath(String aBasePath) {
    }

    @Override
    public void setTileDecoder(ILcdTileDecoder aTileDecoder) {
    }
  }
}

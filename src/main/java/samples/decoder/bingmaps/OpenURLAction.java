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
package samples.decoder.bingmaps;

import com.luciad.gui.ALcdAction;
import com.luciad.gui.TLcdCompositeIcon;
import com.luciad.gui.TLcdImageIcon;
import com.luciad.gui.TLcdResizeableIcon;
import com.luciad.gui.TLcdTranslatedIcon;

import javax.swing.JOptionPane;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.IOException;

/**
 * Abstract action which allows to open a URL on a map
 */
public abstract class OpenURLAction extends ALcdAction{
  private final Component fParentComponent;

  public OpenURLAction( Component aParentComponent ) {
    fParentComponent = aParentComponent;
    setName( "Open url" );
    setIcon( createIcon() );
    setShortDescription( "Open a url" );
  }

  private TLcdCompositeIcon createIcon() {
    TLcdResizeableIcon raster = new TLcdResizeableIcon( new TLcdImageIcon( "images/icons/raster_layer_16.png" ) );
    TLcdResizeableIcon deco =
        new TLcdResizeableIcon(
            new TLcdTranslatedIcon(
                new TLcdImageIcon( "images/icons/add_deco_16.png" ), 7, 5 ) );
    TLcdCompositeIcon icon = new TLcdCompositeIcon();
    icon.addIcon( raster );
    icon.addIcon( deco );
    icon.setIconHeight( 16 );
    icon.setIconWidth( 16 );
    return icon;
  }

  @Override
  public void actionPerformed( ActionEvent e ) {
    String url = JOptionPane.showInputDialog(  fParentComponent,
                                               "Enter a valid Bing compatible url.", "Open url",
                                               JOptionPane.PLAIN_MESSAGE );
    if ( url != null ){
      url = url.trim();
      if ( !( url.isEmpty() ) ){
        try{
          loadModel( url );
        } catch ( IOException ex ){
          ex.printStackTrace();
          JOptionPane.showMessageDialog(
              fParentComponent,
              new String[] {
                  ex.getMessage(), "Check the url " + url, "No data will be displayed."
              },
              "Error decoding",
              JOptionPane.ERROR_MESSAGE
          );
        }
      }
    }
  }

  /**
   * Load {@code aURL} onto the view
   * @param aURL The URL. Will never be {@code null}
   * @throws IOException When loading the URL fails
   */
  protected abstract void loadModel( String aURL ) throws IOException;
}

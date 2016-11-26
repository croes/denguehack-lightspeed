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
package samples.decoder.kml22.common.modelcontenttree;

import com.luciad.format.kml22.model.feature.TLcdKML22AbstractFeature;

import javax.swing.JLabel;
import javax.swing.SwingConstants;
import java.awt.Dimension;

public class ModelContentTreeNodeCellRendererUtil {
  /**
   * <p>Creates a description snippet for a given abstract feature by modifying the given label.</p>
   * @param aLabel An existant label that will be modified.
   * @param aAbstractFeature An abstract feature from which to gather data
   */
  public static void updateRendererFromAbstractFeature( JLabel aLabel, TLcdKML22AbstractFeature aAbstractFeature ) {
    if(aAbstractFeature==null){
      //do nothing if aAbstractFeature is null.
      return;
    }
    String name        = aAbstractFeature.getName()==null?"no_name":aAbstractFeature.getName();
    String description = aAbstractFeature.getDescription();
    String snippet     = aAbstractFeature.getSnippet() != null ? aAbstractFeature.getSnippet().getValueObject() : "";
    int snippetMaxLines= aAbstractFeature.getSnippet() != null ? aAbstractFeature.getSnippet().getMaxLines() : 2;
    updateRendererFromAbstractFeature( aLabel, name, description, snippet, snippetMaxLines );
  }

  /**
   * <p>Creates a description snippet for a given set of parameters.</p>
   * @param aLabel An existant label that will be modified
   * @param aName The name to give the label
   * @param aDescription The description found in the label (can be html)
   * @param aSnippet A snippet if available.
   * @param aSnippetMaxLines A maximum amount of lines of code the description/snippet may be.
   */
  private static void updateRendererFromAbstractFeature( JLabel aLabel, String aName, String aDescription, String aSnippet, int aSnippetMaxLines ) {
    int fRowHeight =  15;
    int fRowWidth  = 175;
    if ( aSnippet != null && !aSnippet.equals("")) {
      aSnippet = formatSnippet( aSnippet);
      aLabel.setText( "<html><body><u>" + aName + "</u><br><font color=\"#808080\">" + aSnippet + "</font></body></html>" );
      aLabel.setVerticalAlignment( SwingConstants.TOP );
      aLabel.setVerticalTextPosition( SwingConstants.TOP );
      aLabel.setPreferredSize( new Dimension( fRowWidth, fRowHeight * (aSnippetMaxLines+1) - 1 ) );
    }
    else if ( aDescription != null ) {
      aDescription = formatDescription( aDescription );
      aLabel.setText( "<html><body><u>" + aName + "</u><br><font color=\"#808080\">" + aDescription + "</font></body></html>" );
      aLabel.setVerticalAlignment( SwingConstants.TOP );
      aLabel.setVerticalTextPosition( SwingConstants.TOP );
      aLabel.setPreferredSize( new Dimension( fRowWidth, fRowHeight * 3 - 1 ) );
    }
    else {
      aLabel.setText( "<html><body>" + aName + "</body></html>" );
      aLabel.setPreferredSize( null );
      Dimension preferred_size = aLabel.getPreferredSize();
      if ( preferred_size.width > fRowWidth ) {
        aLabel.setVerticalAlignment( SwingConstants.TOP );
        aLabel.setVerticalTextPosition( SwingConstants.TOP );
        aLabel.setPreferredSize( new Dimension( fRowWidth, fRowHeight * 2 - 1 ) );
      }
      else {
        aLabel.setVerticalAlignment( SwingConstants.CENTER );
        aLabel.setVerticalTextPosition( SwingConstants.CENTER );
        aLabel.setPreferredSize( new Dimension( fRowWidth, fRowHeight ) );
      }
    }
  }

  /**
   * <p>Formats the description of an abstract feature so that it can be displayed in a tree.</p>
   * @param aDescription A String to be formatted.
   * @return a String that has "img" html tags removed. It also filters out html head, so that only the body of the description is returned.
   */
  private static String formatDescription( String aDescription ) {
    String trimmedDescription = aDescription.toLowerCase().trim();
    int bodyIndex = trimmedDescription.indexOf( "<body>" );
    int endBodyIndex = trimmedDescription.indexOf( "</body>" );
    if(bodyIndex!=-1 && endBodyIndex!=-1){
      aDescription = aDescription.substring( bodyIndex+6, endBodyIndex );
    }
    String description = aDescription.toLowerCase();
    int index = description.indexOf( "<img" );
    if ( index != -1 ) {
      aDescription = aDescription.substring( 0, index );
    }
    return aDescription;
  }
  private static String formatSnippet( String aSnippet){
    return aSnippet.trim();
  }
}

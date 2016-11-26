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
package samples.opengl.common;

import com.luciad.gui.ALcdAction;
import com.luciad.gui.ILcdAction;
import com.luciad.gui.ILcdIcon;
import com.luciad.gui.TLcdActionAtLocationEvent;
import com.luciad.gui.swing.TLcdSWIcon;
import com.luciad.util.logging.ILcdLogger;
import com.luciad.util.logging.TLcdLoggerFactory;
import com.luciad.view.opengl.ILcdGLView;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import java.awt.Component;
import java.awt.event.ActionEvent;

/**
 * This ILcdAction implementation pops up a PopupMenu to which you can add a set of actions
 * to execute.
 */
public class ShowPopupAction
        extends ALcdAction
        implements ILcdAction {

private static ILcdLogger sLogger = TLcdLoggerFactory.getLogger(ShowPopupAction.class.getName());

  private ILcdAction[] fActionArray;
  private ILcdGLView fGLView;

  public ShowPopupAction( ILcdAction[] aActionArray, ILcdGLView aGLView ) {
    super( "PopupActionChoice" );
    fActionArray = aActionArray;
    fGLView = aGLView;
  }

  public void actionPerformed( ActionEvent e ) {
    if ( fGLView instanceof Component ) {
      int x, y;
      if ( e instanceof TLcdActionAtLocationEvent ) {
        x = ( ( TLcdActionAtLocationEvent ) e ).getLocation().x;
        y = ( ( TLcdActionAtLocationEvent ) e ).getLocation().y;
      }
      else {
        x = fGLView.getWidth() / 2;
        y = fGLView.getHeight() / 2;
      }
      JPopupMenu popup_menu = makePopupMenu();
      popup_menu.show( (Component) fGLView, x, y );
    }
    else
      sLogger.error( "actionPerformed: cannot get Component for ILcdGXYView" );
  }

  protected JPopupMenu makePopupMenu() {
    JPopupMenu popup_menu = new JPopupMenu( "Actions:" );
    JMenuItem menu_item;
    String menu_item_name;
    for ( int index = 0; index < fActionArray.length ; index++ ) {
      ILcdAction action = fActionArray[ index ];
      if ( action == null ) {
        popup_menu.addSeparator();
      }
      else {
        menu_item_name = action.getValue( ILcdAction.NAME ).toString();
        if ( menu_item_name == null ) {
          menu_item_name = action.toString();
        }
        menu_item = new JMenuItem( menu_item_name );
        if ( action.getValue( ILcdAction.SMALL_ICON ) != null ) {
          ILcdIcon icon = (ILcdIcon) action.getValue( ILcdAction.SMALL_ICON );
          menu_item.setIcon( new TLcdSWIcon( icon ) );
        }
        menu_item.addActionListener( action );
        popup_menu.add( menu_item );       
      }
    }
    return popup_menu;
  }

}

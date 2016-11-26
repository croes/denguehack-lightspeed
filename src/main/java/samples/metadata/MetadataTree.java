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
package samples.metadata;

import com.luciad.datamodel.ILcdDataObject;
import com.luciad.datamodel.TLcdAssociationClassAnnotation;
import com.luciad.datamodel.TLcdDataProperty;
import com.luciad.format.metadata.model.TLcdGCODataTypes;
import com.luciad.format.metadata.model.metadataentityset.TLcdISO19115Metadata;
import com.luciad.format.metadata.model.util.TLcdISO19115Code;
import com.luciad.format.metadata.model.util.TLcdISO19115Optional;

import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;
import java.awt.Color;
import java.awt.Component;
import java.util.Collection;
import java.util.Locale;

/**
 * This extension of <code>JTree</code> can be used to visualize an <code>ILcdISO19115Metadata</code>
 * instance in a tree view.
 */
public class MetadataTree extends JTree {

  /**
   * Creates a new <code>MetadataTree</code> instance with an empty tree model.
   */
  public MetadataTree() {
    super();
    setModel( new DefaultTreeModel( new MetadataTreeNode() ) );
    setRootVisible( false );
    setCellRenderer( new MetadataTreeCellRenderer() );
  }

  /**
   * Ensures that the node identified by the specified path is
   * collapsed and viewable. Note that the root path will not
   * be collapsed.
   *
   * @param path the <code>TreePath</code> identifying a node
   */
  public void collapsePath( TreePath path ) {
    // if the root is visible, check if needs to collapse (root row = 0)
    if ( !isRootVisible() || path != getPathForRow( 0 ) ) {
      super.collapsePath( path );
    }
  }

  /**
   * Initializes this tree with the given <code>ILcdISO19115Metadata</code> instance.
   *
   * @param aMetadata the <code>ILcdISO19115Metadata</code> instance to be visualized.
   */
  public void setMetadata( TLcdISO19115Metadata aMetadata ) {
    MetadataTreeNode root = new MetadataTreeNode( aMetadata );
    if ( aMetadata != null ) {
      createNodes( aMetadata, root );
    }
    removeAll();
    setRootVisible( true );
    setModel( new DefaultTreeModel( root ) );
  }

  /**
   * Adds the object (and all its children) of the given <code>ILcdDataObject</code> instance to the given node.
   */
  private void createNodes( ILcdDataObject aObject, MetadataTreeNode aTopSFCT ) {
    if ( aObject.getDataType() == TLcdGCODataTypes.CharacterString_PropertyType ) {
      ILcdDataObject valueObject = (( TLcdISO19115Optional<ILcdDataObject> ) aObject).getValueObject();
      Object value = valueObject.getValue( valueObject.getDataType().getProperties().get( 0 ) );
      aTopSFCT.setUserObject( value );
      return;
    }
    TLcdAssociationClassAnnotation annotation = aObject.getDataType().getAnnotation( TLcdAssociationClassAnnotation.class );
    if ( annotation != null ) {
      Object value = aObject.getValue( annotation.getRoleProperty() );
      aTopSFCT.setUserObject( value );
      if ( value instanceof ILcdDataObject ) {
        createNodes( (ILcdDataObject) value, aTopSFCT );
      }
      return;
    }
    createNodesForDataObject( aObject, aTopSFCT );
  }

  private void createNodesForDataObject( ILcdDataObject aObject, MetadataTreeNode aTopSFCT ) {
    for ( TLcdDataProperty p : aObject.getDataType().getProperties() ) {
      Object value = aObject.getValue( p );
      if ( value != null ) {
        if ( value instanceof Collection<?> ) {
          Collection<?> elements = (Collection<?>) value;
          if ( !elements.isEmpty() ) {
            MetadataTreeNode enumeration_node = new MetadataTreeNode( value, p );
            aTopSFCT.add( enumeration_node );
            int count = 1;
            for ( Object elem : ( Collection<?>) value ) {
              if ( elem != null ) {
                MetadataTreeNode node = new MetadataTreeNode( elem, count );
                enumeration_node.add( node );
                if ( elem instanceof ILcdDataObject ) {
                  createNodes( (ILcdDataObject ) elem, node );
                }
              }
              count++;
            }
          }
        } else {
          MetadataTreeNode node = new MetadataTreeNode( value, p );
          aTopSFCT.add( node );
          if ( value instanceof ILcdDataObject ) {
            createNodes( (ILcdDataObject) value, node );
          }
        }
      }
    }
  }

  /**
   * A <code>TreeCellRenderer</code> implementation that renders nodes
   * of type <code>MetadataTreeNode</code>.
   */
  private static class MetadataTreeCellRenderer implements TreeCellRenderer {

    private JLabel fRenderer;

    MetadataTreeCellRenderer() {
      fRenderer = new JLabel();
      fRenderer.setOpaque( true );
    }

    public Component getTreeCellRendererComponent( JTree aTree,
        Object aValue, boolean aIsSelected, boolean aExpanded,
        boolean aLeaf, int aRow, boolean aHasFocus ) {

      // Change background color based on selected state.
      Color background = ( aIsSelected ? Color.lightGray : Color.white );
      fRenderer.setBackground( background );

      // Set display name.
      if ( aValue instanceof MetadataTreeNode ) {
        fRenderer.setText( ( (MetadataTreeNode) aValue ).getDisplayName() );
      }
      return fRenderer;
    }

  }

  /**
   * A <code>DefaultMutableTreeNode</code> extension that adds functionality
   * to retrieve a display name.
   */
  private class MetadataTreeNode extends DefaultMutableTreeNode {

    private String fDisplayName;
    private TLcdDataProperty fProperty;

    public MetadataTreeNode() {
      super();
      fDisplayName = "";
    }

    public MetadataTreeNode( Object aUserObject ) {
      super( aUserObject );
      fDisplayName = getDisplayName( aUserObject );
    }

    public MetadataTreeNode( Object aUserObject, int aIndex ) {
      super( aUserObject );
      fDisplayName = aIndex + ".  " + getDisplayName( aUserObject );
    }

    public MetadataTreeNode( Object aUserObject, TLcdDataProperty aProperty ) {
      fProperty = aProperty;
      setUserObject( aUserObject );
    }

    public String getDisplayName() {
      return fDisplayName;
    }
        
    @Override
    public void setUserObject( Object aUserObject ) {      
      super.setUserObject( aUserObject );
      fDisplayName = getDisplayName( aUserObject );
    }

    private String getDisplayName( Object aUserObject ) {
      if ( fProperty != null && fProperty.isCollection() ) {
        return fProperty.getDisplayName();
      }
      String result = extractName( aUserObject );
      if ( result != null && fProperty != null ) {
        result = fProperty.getDisplayName() + " = " + result;
      }
      return result;
    }

    private String extractName( Object aUserObject ) {
      if ( aUserObject != null ) {
        if ( aUserObject instanceof TLcdISO19115Code ) {
          return ( ( TLcdISO19115Code ) aUserObject ).getValueObject();
        }
        if ( aUserObject instanceof ILcdDataObject ) {
          return ( (ILcdDataObject) aUserObject ).getDataType().getDisplayName();
        } else if ( aUserObject instanceof Locale ) {
          return ( (Locale) aUserObject ).getDisplayName();
        } else {
          return aUserObject.toString();
        }
      }
      return null;
    }
  }
}

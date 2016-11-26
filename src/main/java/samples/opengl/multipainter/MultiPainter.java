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
package samples.opengl.multipainter;

import com.luciad.shape.ILcdEllipse;
import com.luciad.shape.ILcdPolygon;
import com.luciad.shape.shape3D.ILcd3DEditableBounds;
import com.luciad.shape.shape3D.ILcd3DEditablePoint;
import com.luciad.util.TLcdNoBoundsException;
import com.luciad.view.opengl.ILcdGLContext;
import com.luciad.view.opengl.binding.ILcdGLDrawable;
import com.luciad.view.opengl.paintablefactory.TLcdGLExtrudedEllipsePaintableFactory;
import com.luciad.view.opengl.paintablefactory.TLcdGLExtrudedPolygonPaintableFactory;
import com.luciad.view.opengl.painter.ILcdGLPaintMode;
import com.luciad.view.opengl.painter.ILcdGLPainter;
import com.luciad.view.opengl.painter.TLcdGLPaintablePainter2;
import com.luciad.view.opengl.style.ILcdGLObjectStyleProvider;
import com.luciad.view.opengl.style.TLcdGLFillStyle;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 * MultiPainter.
 */
class MultiPainter implements ILcdGLPainter {
  private PropertyChangeSupport fPropertyChangeSupport = new PropertyChangeSupport( this );
  private TLcdGLPaintablePainter2 fEllipsePainter;
  private TLcdGLPaintablePainter2 fPolygonPainter;

  private ChildPaintersPropertyChangeListener fChildPaintersPropertyChangeListener = new ChildPaintersPropertyChangeListener();


  public MultiPainter() {
    TLcdGLExtrudedEllipsePaintableFactory ellipsePaintableFactory =
            new TLcdGLExtrudedEllipsePaintableFactory();
    ellipsePaintableFactory.setDefaultMinimumZ( 0 );
    ellipsePaintableFactory.setDefaultMaximumZ( 50000 );
    fEllipsePainter = new TLcdGLPaintablePainter2( ellipsePaintableFactory );

    fEllipsePainter.addPropertyChangeListener( fChildPaintersPropertyChangeListener );

    TLcdGLExtrudedPolygonPaintableFactory polygonPaintableFactory =
            new TLcdGLExtrudedPolygonPaintableFactory();
    polygonPaintableFactory.setDefaultMinimumZ( 0 );
    polygonPaintableFactory.setDefaultMaximumZ( 50000 );
    fPolygonPainter = new TLcdGLPaintablePainter2( polygonPaintableFactory );
    fPolygonPainter.addPropertyChangeListener(
            fChildPaintersPropertyChangeListener
    );
  }


  public void addPropertyChangeListener( PropertyChangeListener aPropertyChangeListener ) {
    fPropertyChangeSupport.addPropertyChangeListener( aPropertyChangeListener );
  }

  public void removePropertyChangeListener( PropertyChangeListener aPropertyChangeListener ) {
    fPropertyChangeSupport.addPropertyChangeListener( aPropertyChangeListener );
  }

  public void anchorPointSFCT( ILcdGLDrawable aGLDrawable, Object aObject, ILcdGLPaintMode aMode, ILcdGLContext aContext, ILcd3DEditablePoint aPointSFCT ) throws TLcdNoBoundsException {
    ILcdGLPainter painter = getPainter( aObject );
    if ( painter != null )
      painter.anchorPointSFCT( aGLDrawable, aObject, aMode, aContext, aPointSFCT );
    else
      throw new TLcdNoBoundsException();
  }

  public void boundsSFCT( ILcdGLDrawable aGLDrawable, Object aObject, ILcdGLPaintMode aMode, ILcdGLContext aContext, ILcd3DEditableBounds aBoundsSFCT ) throws TLcdNoBoundsException {
    ILcdGLPainter painter = getPainter( aObject );
    if ( painter != null )
      painter.boundsSFCT( aGLDrawable, aObject, aMode, aContext, aBoundsSFCT );
    else
      throw new TLcdNoBoundsException();
  }


  public void paint(
          ILcdGLDrawable aGLDrawable,
          Object aObject,
          ILcdGLPaintMode aMode,
          ILcdGLContext aContext
  ) {
    ILcdGLPainter painter = getPainter( aObject );
    if ( painter != null )
      painter.paint( aGLDrawable, aObject, aMode, aContext );
  }


  public void setPaintFill( boolean aFlag ) {
    fPolygonPainter.setPaintFill( aFlag );
    fEllipsePainter.setPaintFill( aFlag );
  }

  public void setPaintOutline( boolean aFlag ) {
    fPolygonPainter.setPaintOutline( aFlag );
    fEllipsePainter.setPaintOutline( aFlag );
  }

  public void setFillStyle( ILcdGLObjectStyleProvider<TLcdGLFillStyle> aStyleProvider ) {
    fPolygonPainter.setFillStyleProvider( aStyleProvider );
    fEllipsePainter.setFillStyleProvider( aStyleProvider );
  }


  private ILcdGLPainter getPainter( Object aObject ) {
    if ( aObject instanceof ILcdEllipse )
      return fEllipsePainter;
    else if ( aObject instanceof ILcdPolygon )
      return fPolygonPainter;
    else
      return null;
  }

  private class ChildPaintersPropertyChangeListener implements PropertyChangeListener {
    public void propertyChange( PropertyChangeEvent evt ) {
      PropertyChangeEvent event = new PropertyChangeEvent(
              this, null, null, null );
      event.setPropagationId( evt.getSource() );
      fPropertyChangeSupport.firePropertyChange( event );
    }
  }
}

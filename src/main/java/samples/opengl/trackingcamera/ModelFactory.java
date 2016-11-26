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
package samples.opengl.trackingcamera;

import com.luciad.geodesy.TLcdGeodeticDatum;
import com.luciad.model.ILcdModel;
import com.luciad.model.TLcd2DBoundsIndexedModel;
import com.luciad.model.TLcdModelDescriptor;
import com.luciad.model.TLcdVectorModel;
import com.luciad.reference.TLcdGeodeticReference;
import com.luciad.util.ILcdFireEventMode;
import com.luciad.view.map.TLcdLonLatGrid;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


/**
 * Model factory for the trackingcamera samples.
 */
public class ModelFactory {

  private static double HEIGHT = 150000;
  public static ILcdModel createGridModel() {
    TLcdLonLatGrid grid = new TLcdLonLatGrid( 10, 10 );
    TLcdModelDescriptor descriptor = new TLcdModelDescriptor( "Grid", "Grid", "Grid" );
    TLcdGeodeticReference reference = new TLcdGeodeticReference( new TLcdGeodeticDatum() );
    TLcdVectorModel model = new TLcdVectorModel( reference, descriptor );
    model.addElement( grid, ILcdFireEventMode.NO_EVENT );
    return model;
  }

  public static ILcdModel createMultiPointModel() {
    final TLcd2DBoundsIndexedModel model = new TLcd2DBoundsIndexedModel();
    model.setModelReference(new TLcdGeodeticReference());
    model.setModelDescriptor(new TLcdModelDescriptor("Point", "Point", "Point"));

    // create a few 3D oriented elements
    final OrientedLonLatHeightPoint p1 = new OrientedLonLatHeightPoint(0.2, 0.0, HEIGHT, 90, 0, 0);
    model.addElement(p1, ILcdFireEventMode.NO_EVENT);
    final OrientedLonLatHeightPoint p2 = new OrientedLonLatHeightPoint(0.0, -0.2, HEIGHT, 90, 0, 0);
    model.addElement(p2, ILcdFireEventMode.NO_EVENT);
    final OrientedLonLatHeightPoint p3 = new OrientedLonLatHeightPoint(0.0, 0.2, HEIGHT, 90, 0, 0);
    model.addElement(p3, ILcdFireEventMode.NO_EVENT);

    // setup a timer to update the elements periodically
    final int dt = 50;//milliseconds
    Timer timer = new Timer(dt, new ActionListener() {
      private double fTime = 0;
      public void actionPerformed(ActionEvent e) {
        fTime += ((double)dt)/1000.0;

        // update the 3D elements position and orientation
        double dh = 0.25*HEIGHT*Math.sin(fTime);
        p1.move3D(p1.getX() + 0.02, p1.getY(), HEIGHT + dh);
        p1.setPitch(15*Math.cos(-fTime));
        p1.setRoll(15*Math.sin(fTime));
        model.elementChanged(p1, ILcdFireEventMode.FIRE_NOW);

        p2.move3D(p2.getX() + 0.02, p2.getY(), HEIGHT + dh);
        p2.setPitch(p1.getPitch());
        model.elementChanged(p2, ILcdFireEventMode.FIRE_NOW);

        p3.move3D(p3.getX() + 0.02, p3.getY(),HEIGHT + dh);
        p3.setPitch(p1.getPitch());
        model.elementChanged(p3, ILcdFireEventMode.FIRE_NOW);
      }
    });
    timer.start();

    return model;
  }
}

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
package samples.lightspeed.balloon;

import static com.luciad.util.concurrent.TLcdLockUtil.Lock;
import static com.luciad.util.concurrent.TLcdLockUtil.writeLock;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.ref.WeakReference;

import javax.swing.Timer;

import com.luciad.geodesy.TLcdGeodeticDatum;
import com.luciad.model.ALcdModel;
import com.luciad.model.ILcd2DBoundsIndexedModel;
import com.luciad.model.TLcd2DBoundsIndexedModel;
import com.luciad.model.TLcdModelDescriptor;
import com.luciad.reference.TLcdGeodeticReference;
import com.luciad.shape.shape2D.TLcdLonLatPoint;
import com.luciad.util.ILcdFireEventMode;

/**
 * Creates a model with some ILcdPoints, one of which is moving.
 */
public class ModelFactory {

  private static final int DELAY_IN_MILLISECONDS = 50;

  public static ILcd2DBoundsIndexedModel createBalloonModel() {

    TLcd2DBoundsIndexedModel model = new TLcd2DBoundsIndexedModel();

    TLcdGeodeticDatum datum = new TLcdGeodeticDatum();
    model.setModelReference(new TLcdGeodeticReference(datum));
    model.setModelDescriptor(new TLcdModelDescriptor(
        "Sample code",    // source name
        "MyPoint",        // data type
        "Point Model"      // display name (user)
    ));

    model.addElement(new TLcdLonLatPoint(-122.45, 37.75), ILcdFireEventMode.NO_EVENT);
    final TLcdLonLatPoint movingPoint = new TLcdLonLatPoint(-122.455, 37.745);
    model.addElement(movingPoint, ILcdFireEventMode.NO_EVENT);
    model.addElement(new TLcdLonLatPoint(-122.445, 37.748), ILcdFireEventMode.NO_EVENT);
    model.addElement(new TLcdLonLatPoint(-122.448, 37.755), ILcdFireEventMode.NO_EVENT);


    // Allow the model (and its view) to be garbage collected.
    final WeakReference<ALcdModel> weakModel = new WeakReference<ALcdModel>(model);

    // Setup a timer to update the elements periodically.
    Timer timer = new Timer(DELAY_IN_MILLISECONDS, new ActionListener() {
      private double fTime = 0;

      @Override
      public void actionPerformed(ActionEvent e) {

        ALcdModel model = weakModel.get();
        if (model == null) {
          Timer timer = (Timer) e.getSource();
          timer.stop();
          return;
        }

        fTime += ((double) DELAY_IN_MILLISECONDS) / 1000.0;

        try (Lock autoUnlock = writeLock(model)) {
          movingPoint.translate2D(0.0001 * Math.cos(-fTime), 0.0001 * Math.sin(fTime));
          model.elementChanged(movingPoint, ILcdFireEventMode.FIRE_LATER);
        } finally {
          model.fireCollectedModelChanges();
        }
      }
    });
    timer.start();

    return model;
  }
}

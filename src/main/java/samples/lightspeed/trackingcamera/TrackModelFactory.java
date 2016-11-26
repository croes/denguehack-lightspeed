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
package samples.lightspeed.trackingcamera;

import static com.luciad.util.concurrent.TLcdLockUtil.Lock;
import static com.luciad.util.concurrent.TLcdLockUtil.writeLock;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.ref.WeakReference;

import javax.swing.Timer;

import com.luciad.model.ALcdModel;
import com.luciad.model.ILcdModel;
import com.luciad.model.TLcdModelDescriptor;
import com.luciad.model.TLcdVectorModel;
import com.luciad.reference.TLcdGeodeticReference;
import com.luciad.util.ILcdFireEventMode;
import com.luciad.util.TLcdPair;
import com.luciad.util.concurrent.TLcdLockUtil;

/**
 * Model factory for the tracking camera samples, it creates a multipoint model
 * that moves in 3D at a constant speed.
 */
public class TrackModelFactory {

  // The initial height of the points in the model in meters.
  private static final double HEIGHT = 150000;

  public static TLcdPair<ILcdModel, Timer> createMultiPointModel() {
    TLcdVectorModel model = new TLcdVectorModel();
    model.setModelReference(new TLcdGeodeticReference());
    model.setModelDescriptor(new TLcdModelDescriptor("Tracking Points", "Tracking Points", "Tracking Points"));

    // Create a few 3D oriented elements.
    final OrientationLonLatHeightPoint p1 = new OrientationLonLatHeightPoint(0.2, 40.0, HEIGHT, 90, 0, 0);
    model.addElement(p1, ILcdFireEventMode.NO_EVENT);
    final OrientationLonLatHeightPoint p2 = new OrientationLonLatHeightPoint(0.0, 39.8, HEIGHT, 90, 0, 0);
    model.addElement(p2, ILcdFireEventMode.NO_EVENT);
    final OrientationLonLatHeightPoint p3 = new OrientationLonLatHeightPoint(0.0, 40.2, HEIGHT, 90, 0, 0);
    model.addElement(p3, ILcdFireEventMode.NO_EVENT);

    // Setup a timer to update the elements periodically.
    final int DELAY_IN_MILLISECONDS = 50;
    // Allow the model (and its view) to be garbage collected.
    final WeakReference<ALcdModel> weakModel = new WeakReference<ALcdModel>(model);
    Timer timer = new Timer(DELAY_IN_MILLISECONDS, new ActionListener() {
      private double fTime = 0;

      public void actionPerformed(ActionEvent e) {

        ALcdModel model = weakModel.get();
        if (model == null) {
          Timer timer = (Timer) e.getSource();
          timer.stop();
          return;
        }

        fTime += ((double) DELAY_IN_MILLISECONDS) / 1000.0;

        try (Lock autoUnlock = writeLock(model)) {

          // Update the 3D elements position and orientation.
          double dh = 0.25 * HEIGHT * Math.sin(fTime);

          p1.move3D(p1.getX() + 0.1, p1.getY(), HEIGHT + dh);
          p1.setPitch(15 * Math.cos(-fTime));
          p1.setRoll(15 * Math.sin(fTime));

          p2.move3D(p2.getX() + 0.1, p2.getY(), HEIGHT + dh);
          p2.setPitch(p1.getPitch());

          p3.move3D(p3.getX() + 0.1, p3.getY(), HEIGHT + dh);
          p3.setPitch(p1.getPitch());

          model.allElementsChanged(ILcdFireEventMode.FIRE_NOW);
        }
      }
    });
    timer.start();

    return new TLcdPair<ILcdModel, Timer>(model, timer);
  }
}

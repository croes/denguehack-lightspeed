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
package samples.decoder.asterix.lightspeed.radarvideo.radar;

import java.awt.Color;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 * All properties related to radar styling. Used by the stylers and the UI.
 */
public class RadarStyleProperties {

  private Color fBlipColor;
  private Color fAfterglowColor;
  private Color fBackgroundColor;
  private Color fGridColor;
  private Color fSweepLineColor;
  private double fThreshold;
  private double fBlipAfterglowDuration;
  private double fIntensity;
  private boolean fGridEnabled = true;
  private boolean fSweepLineEnabled = true;

  private PropertyChangeSupport fPropertyChangeSupport = new PropertyChangeSupport(this);

  public RadarStyleProperties(
      Color aBlipColor,
      Color aAfterglowColor,
      Color aBackgroundColor,
      Color aGridColor,
      Color aSweepLineColor,
      double aBlipAfterglowDuration,
      double aIntensity,
      double aThreshold) {
    fBlipColor = aBlipColor;
    fAfterglowColor = aAfterglowColor;
    fBackgroundColor = aBackgroundColor;
    fGridColor = aGridColor;
    fSweepLineColor = aSweepLineColor;
    fBlipAfterglowDuration = aBlipAfterglowDuration;
    fIntensity = aIntensity;
    fThreshold = aThreshold;
  }

  public Color getBlipColor() {
    return fBlipColor;
  }

  public void setBlipColor(Color aBlipColor) {
    Color oldValue = fBlipColor;
    fBlipColor = aBlipColor;
    fPropertyChangeSupport.firePropertyChange("blipColor", oldValue, fBlipColor);
  }

  public Color getAfterglowColor() {
    return fAfterglowColor;
  }

  public void setAfterglowColor(Color aAfterglowColor) {
    Color oldValue = fAfterglowColor;
    fAfterglowColor = aAfterglowColor;
    fPropertyChangeSupport.firePropertyChange("blipAfterglowColor", oldValue, fAfterglowColor);
  }

  public Color getBackgroundColor() {
    return fBackgroundColor;
  }

  public void setBackgroundColor(Color aBackgroundColor) {
    Color oldValue = fBackgroundColor;
    fBackgroundColor = aBackgroundColor;
    fPropertyChangeSupport.firePropertyChange("backgroundColor", oldValue, fBackgroundColor);
  }

  public double getBlipAfterglowDuration() {
    return fBlipAfterglowDuration;
  }

  public void setBlipAfterglowDuration(double aBlipAfterglowDuration) {
    double oldValue = fBlipAfterglowDuration;
    fBlipAfterglowDuration = aBlipAfterglowDuration;
    fPropertyChangeSupport.firePropertyChange("blipAfterglowDuration", oldValue, fBlipAfterglowDuration);
  }

  public double getIntensity() {
    return fIntensity;
  }

  public void setIntensity(double aIntensity) {
    double oldValue = fIntensity;
    fIntensity = aIntensity;
    fPropertyChangeSupport.firePropertyChange("intensity", oldValue, fIntensity);
  }

  public boolean isGridEnabled() {
    return fGridEnabled;
  }

  public void setGridEnabled(boolean aGridEnabled) {
    boolean oldValue = fGridEnabled;
    fGridEnabled = aGridEnabled;
    fPropertyChangeSupport.firePropertyChange("gridEnabled", oldValue, fGridEnabled);
  }

  public void addPropertyChangeListener(PropertyChangeListener listener) {
    fPropertyChangeSupport.addPropertyChangeListener(listener);
  }

  public void removePropertyChangeListener(PropertyChangeListener listener) {
    fPropertyChangeSupport.removePropertyChangeListener(listener);
  }

  public double getThreshold() {
    return fThreshold;
  }

  public void setThreshold(double aThreshold) {
    double oldValue = fThreshold;
    fThreshold = aThreshold;
    fPropertyChangeSupport.firePropertyChange("threshHold", oldValue, fThreshold);
  }

  public Color getGridColor() {
    return fGridColor;
  }

  public void setGridColor(Color aGridColor) {
    Color oldValue = fGridColor;
    fGridColor = aGridColor;
    fPropertyChangeSupport.firePropertyChange("gridColor", oldValue, fGridColor);

  }

  public Color getSweepLineColor() {
    return fSweepLineColor;
  }

  public void setSweepLineColor(Color aSweepLineColor) {
    Color oldValue = fSweepLineColor;
    fSweepLineColor = aSweepLineColor;
    fPropertyChangeSupport.firePropertyChange("sweepColor", oldValue, fSweepLineColor);

  }

  public boolean isSweepLineEnabled() {
    return fSweepLineEnabled;
  }

  public void setSweepLineEnabled(boolean aSweepLineEnabled) {
    boolean oldValue = fSweepLineEnabled;
    fSweepLineEnabled = aSweepLineEnabled;
    fPropertyChangeSupport.firePropertyChange("sweepEnabled", oldValue, fSweepLineEnabled);
  }
}

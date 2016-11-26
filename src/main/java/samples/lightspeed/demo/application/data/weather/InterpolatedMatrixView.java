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
package samples.lightspeed.demo.application.data.weather;

import com.luciad.shape.ILcdMatrixView;

/**
 * Interpolates between 2 <code>ILcdMatrixView</code>s.
 */
class InterpolatedMatrixView implements ILcdMatrixView {

  private final ILcdMatrixView fFirstMatrixView;
  private final ILcdMatrixView fSecondMatrixView;
  private final double fBlendFactor;

  public InterpolatedMatrixView(ILcdMatrixView aFirstMatrixView, ILcdMatrixView aSecondMatrixView, double aBlendFactor) {
    fFirstMatrixView = aFirstMatrixView;
    fSecondMatrixView = aSecondMatrixView;
    fBlendFactor = aBlendFactor;
    validate();
  }

  private void validate() {
    if (!isValid()) {
      throw new IllegalArgumentException("MatrixViews not compatible");
    }
  }

  private boolean isValid() {
    if (fFirstMatrixView.getColumnCount() != fSecondMatrixView.getColumnCount()) {
      return false;
    }
    if (fFirstMatrixView.getRowCount() != fSecondMatrixView.getRowCount()) {
      return false;
    }
    if (fFirstMatrixView.retrieveAssociatedPointX(0, 0) != fSecondMatrixView.retrieveAssociatedPointX(0, 0)) {
      return false;
    }
    if (fFirstMatrixView.retrieveAssociatedPointY(0, 0) != fSecondMatrixView.retrieveAssociatedPointY(0, 0)) {
      return false;
    }
    return true;

  }

  @Override
  public double getValue(int i, int j) {
    return fBlendFactor* fFirstMatrixView.getValue(i, j) + (1 - fBlendFactor)* fSecondMatrixView.getValue(i, j);
  }

  @Override
  public double retrieveAssociatedPointX(int i, int j) {
    return fFirstMatrixView.retrieveAssociatedPointX(i, j);
  }

  @Override
  public double retrieveAssociatedPointY(int i, int j) {
    return fFirstMatrixView.retrieveAssociatedPointY(i, j);
  }

  @Override
  public int getColumnCount() {
    return fFirstMatrixView.getColumnCount();
  }

  @Override
  public int getRowCount() {
    return fFirstMatrixView.getRowCount();
  }

}

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
package samples.lightspeed.internal.lvnl.buildings.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Date: Jan 25, 2007
 * Time: 8:56:34 AM
 *
 * @author Tom Nuydens
 */
abstract class HierarchicalProgressListener {
  private float fMinimum;
  private float fMaximum;
  private String fMessage;

  public HierarchicalProgressListener() {
    this(0.0f, 1.0f);
  }

  private HierarchicalProgressListener(float aMinimum, float aMaximum) {
    fMaximum = aMaximum;
    fMinimum = aMinimum;
  }

  public final void setProgress(float aProgress) {
    setProgress((String) null, aProgress);
  }

  public final void setProgress(String aMessage, float aProgress) {
    fMessage = aMessage;
    List<String> messages = new ArrayList<String>();
    setProgress(messages, aProgress);
  }

  private void setProgress(List<String> aMessages, float aProgress) {
    if (fMessage != null) {
      aMessages.add(0, fMessage);
    }

    float localProgress = Math.min(1.0f, Math.max(0.0f, aProgress));
    float globalProgess = fMinimum + (fMaximum - fMinimum) * localProgress;

    progressChanged(aMessages, Math.min(fMaximum, Math.max(fMinimum, globalProgess)));
  }

  protected abstract void progressChanged(List<String> aMessage, float aProgress);

  public final HierarchicalProgressListener createChildListener(float aMinimum, float aMaximum) {
    if (aMinimum < 0.0f) {
      throw new IllegalArgumentException();
    } else if (aMaximum > 1.0f) {
      throw new IllegalArgumentException();
    } else if (aMinimum > aMaximum) {
      throw new IllegalArgumentException();
    }

    return new ChildListener(this, aMinimum, aMaximum);
  }

  private static class ChildListener extends HierarchicalProgressListener {
    private HierarchicalProgressListener fParent;

    public ChildListener(HierarchicalProgressListener aParent, float aMinimum, float aMaximum) {
      super(aMinimum, aMaximum);
      fParent = aParent;
    }

    protected void progressChanged(List<String> aMessages, float aProgress) {
      fParent.setProgress(aMessages, aProgress);
    }
  }
}

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
package samples.lightspeed.labels.util;

import com.luciad.shape.ILcdPoint;
import com.luciad.view.lightspeed.TLspContext;
import com.luciad.view.lightspeed.painter.label.style.ALspLabelTextProviderStyle;
import com.luciad.view.lightspeed.style.ALspStyle;

public class PointTextProviderStyle extends ALspLabelTextProviderStyle {

  private final String fPrefix;

  protected PointTextProviderStyle() {
    this(newBuilder());
  }

  protected PointTextProviderStyle(Builder<?> aBuilder) {
    fPrefix = aBuilder.fPrefix;
  }

  /**
   * Returns the used prefix.
   * @return the used prefix.
   */
  public String getPrefix() {
    return fPrefix;
  }

  @Override
  public String[] getText(Object aDomainObject, Object aSubLabelID, TLspContext aContext) {
    ILcdPoint p = (ILcdPoint) aDomainObject;
    return new String[]{fPrefix + " " + (int) p.getX() + "," + (int) p.getY()};
  }

  @Override
  public Builder<?> asBuilder() {
    return newBuilder().all(this);
  }

  public static Builder<?> newBuilder() {
    return new Builder();
  }

  @SuppressWarnings("unchecked")
  public static class Builder<B extends Builder<B>> extends ALspLabelTextProviderStyle.Builder<B> {

    private String fPrefix = "point";

    protected Builder() {
    }

    /**
     * Sets the prefix.
     * @param aPrefix the prefix
     * @return {@code this}
     */
    public B prefix(String aPrefix) {
      fPrefix = aPrefix;
      return (B) this;
    }

    /**
     * Copies all properties from the given style.
     *
     * @param aStyle the style to copy
     *
     * @return {@code this}
     *
     */
    @Override
    public B all(ALspStyle aStyle) {
      if (aStyle instanceof PointTextProviderStyle) {
        PointTextProviderStyle style = (PointTextProviderStyle) aStyle;
        prefix(style.getPrefix());
      }
      return (B) this;
    }

    /**
     * Constructs a point text provider style with the set parameters.
     * @return the resulting point label text provider style.
     */
    @Override
    public PointTextProviderStyle build() {
      return new PointTextProviderStyle(this);
    }
  }
}

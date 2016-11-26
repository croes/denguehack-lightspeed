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

import com.luciad.view.lightspeed.TLspContext;
import com.luciad.view.lightspeed.painter.label.style.ALspLabelTextProviderStyle;
import com.luciad.view.lightspeed.style.ALspStyle;

/**
 * Text provider style that always returns the same string.
 */
public class FixedTextProviderStyle extends ALspLabelTextProviderStyle {

  private final String fText;

  private FixedTextProviderStyle(Builder aBuilder) {
    fText = aBuilder.fText;
  }

  /**
   * Returns the used text.
   * @return the used text.
   */
  public String getText() {
    return fText;
  }

  @Override
  public String[] getText(Object aDomainObject, Object aSubLabelID, TLspContext aContext) {
    return new String[]{fText};
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }

    FixedTextProviderStyle that = (FixedTextProviderStyle) o;

    return !(fText != null ? !fText.equals(that.fText) : that.fText != null);
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + (fText != null ? fText.hashCode() : 0);
    return result;
  }

  @Override
  public Builder<?> asBuilder() {
    return newBuilder().all(this);
  }

  /**
   * Returns a new builder that can be used to create a FixedTextProviderStyle.
   * @return a new builder that can be used to create a FixedTextProviderStyle.
   */
  public static Builder<?> newBuilder() {
    return new Builder();
  }

  @SuppressWarnings("unchecked")
  public static class Builder<B extends Builder<B>> extends ALspLabelTextProviderStyle.Builder<B> {

    private String fText = "unknown";

    protected Builder() {
    }

    /**
     * Sets the text.
     * @param aText the text
     * @return {@code this}
     */
    public B text(String aText) {
      fText = aText;
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
      if (aStyle instanceof FixedTextProviderStyle) {
        FixedTextProviderStyle style = (FixedTextProviderStyle) aStyle;
        text(style.getText());
      }
      return (B) this;
    }

    /**
     * Constructs a fixed text provider style with the set parameters.
     * @return the resulting fixed label text provider style.
     */
    @Override
    public FixedTextProviderStyle build() {
      return new FixedTextProviderStyle(this);
    }
  }
}

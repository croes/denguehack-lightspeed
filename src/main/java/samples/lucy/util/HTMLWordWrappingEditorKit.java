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
package samples.lucy.util;

import javax.swing.SizeRequirements;
import javax.swing.text.Element;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.ParagraphView;

/**
 * This extension of HTMLEditorKit can split long words over multiple lines.
 *
 * See http://stackoverflow.com/a/7036671/1076463
 */
public class HTMLWordWrappingEditorKit extends HTMLEditorKit {

  @Override
  public ViewFactory getViewFactory() {

    return new HTMLFactory() {
      @Override
      public View create(Element e) {
        View v = super.create(e);
        if (v instanceof ParagraphView) {
          return new ParagraphView(e) {
            @Override
            protected SizeRequirements calculateMinorAxisRequirements(int axis, SizeRequirements r) {
              if (r == null) {
                r = new SizeRequirements();
              }
              float pref = layoutPool.getPreferredSpan(axis);
              float min = layoutPool.getMinimumSpan(axis);
              // Don't include insets, Box.getXXXSpan will include them.
              r.minimum = (int) min;
              r.preferred = Math.max(r.minimum, (int) pref);
              r.maximum = Integer.MAX_VALUE;
              r.alignment = 0.5f;
              return r;
            }

          };
        }
        return v;
      }
    };
  }
}

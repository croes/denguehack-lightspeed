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
package samples.symbology.common.app6;

import com.luciad.gui.ILcdIcon;
import com.luciad.gui.ILcdObjectIconProvider;
import com.luciad.symbology.app6a.model.ELcdAPP6Standard;
import com.luciad.symbology.app6a.model.ILcdAPP6ACoded;
import com.luciad.symbology.app6a.view.TLcdAPP6AObjectIconProvider;
import com.luciad.symbology.app6a.view.gxy.ILcdAPP6AStyle;
import com.luciad.symbology.app6a.view.gxy.ILcdAPP6AStyled;
import com.luciad.symbology.app6a.view.gxy.TLcdDefaultAPP6AStyle;

/**
 * An icon provider that supports the use of Strings instead of objects. This icon provider is not
 * used directly by this sample, but may be used by other projects instead.
 * <p/>
 * The Objects supported by this provider are arrays with minimum length 1, where the first element is the APP6a
 * symbol code in the form of a <code>String</code>. An optional second element in the array is the
 * desired size of the icon in pixels, encoded as an <code>Integer</code>. The default size of icons
 * is 64 pixels.
 */
public class SymbolCodeIconProvider implements ILcdObjectIconProvider {

  private TLcdAPP6AObjectIconProvider fIconProvider = new TLcdAPP6AObjectIconProvider();

  /**
   * Creates a new icon provider.
   */
  public SymbolCodeIconProvider() {
    fIconProvider.getDefaultStyle().setSizeSymbol(64);
  }

  @Override
  public boolean canGetIcon(Object o) {
    return o instanceof Object[] && ((Object[]) o).length >= 1 && ((Object[]) o)[0] instanceof String;
  }

  @Override
  public ILcdIcon getIcon(Object o) throws IllegalArgumentException {
    String code = (String) ((Object[]) o)[0];
    Integer size = null;
    if (((Object[]) o).length >= 2 && ((Object[]) o)[1] instanceof Integer) {
      size = (Integer) ((Object[]) o)[1];
    }
    return fIconProvider.getIcon(createCoded(code, size));
  }

  private ILcdAPP6ACoded createCoded(String aSymbolCode, Integer aSymbolSize) {
    if (aSymbolSize != null && aSymbolSize > 0) {
      TLcdDefaultAPP6AStyle app6AStyle = TLcdDefaultAPP6AStyle.getNewInstance();
      app6AStyle.setSizeSymbol(aSymbolSize);
      return new DummyCodedAndStyled(aSymbolCode, app6AStyle);
    }
    return new DummyCoded(aSymbolCode);
  }

  private static class DummyCoded implements ILcdAPP6ACoded {
    private String fCode;

    private DummyCoded(String fCode) {
      this.fCode = fCode;
    }

    @Override
    public String getAPP6ACode() {
      return fCode;
    }

    @Override
    public ELcdAPP6Standard getAPP6Standard() {
      return ELcdAPP6Standard.APP_6A;
    }

    @Override
    public int getTextModifierCount() {
      return 0;
    }

    @Override
    public String getTextModifierKey(int i) {
      return null;
    }

    @Override
    public String getTextModifierKeyDisplayName(String s) {
      return null;
    }

    @Override
    public String getTextModifierValue(String s) {
      return null;
    }

    @Override
    public String getTextModifierValue(int i) {
      return null;
    }
  }

  private static class DummyCodedAndStyled extends DummyCoded implements ILcdAPP6AStyled {
    private ILcdAPP6AStyle fAPP6AStyle;

    private DummyCodedAndStyled(String aCode, ILcdAPP6AStyle aAPP6AStyle) {
      super(aCode);
      fAPP6AStyle = aAPP6AStyle;
    }

    @Override
    public ILcdAPP6AStyle getAPP6AStyle() {
      return fAPP6AStyle;
    }
  }
}

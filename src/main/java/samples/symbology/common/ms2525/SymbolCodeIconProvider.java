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
package samples.symbology.common.ms2525;

import com.luciad.gui.ILcdIcon;
import com.luciad.gui.ILcdObjectIconProvider;
import com.luciad.symbology.milstd2525b.model.ELcdMS2525Standard;
import com.luciad.symbology.milstd2525b.model.ILcdMS2525bCoded;
import com.luciad.symbology.milstd2525b.view.TLcdMS2525bObjectIconProvider;
import com.luciad.symbology.milstd2525b.view.gxy.ILcdMS2525bStyle;
import com.luciad.symbology.milstd2525b.view.gxy.ILcdMS2525bStyled;
import com.luciad.symbology.milstd2525b.view.gxy.TLcdDefaultMS2525bStyle;

/**
 * An icon provider that supports the use of Strings instead of objects. This icon provider is not
 * used directly by this sample, but may be used by other projects instead.
 * <p/>
 * The Objects supported by this provider are arrays with minimum length 1, where the first element
 * is the MS2525 symbol code in the form of a <code>String</code>. An optional second element in the array is the
 * desired size of the icon in pixels, encoded as an <code>Integer</code>. The default size of icons is 64 pixels.
 */
public class SymbolCodeIconProvider implements ILcdObjectIconProvider {

  private TLcdMS2525bObjectIconProvider fIconProvider = new TLcdMS2525bObjectIconProvider();

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

  private ILcdMS2525bCoded createCoded(String aSymbolCode, Integer aSymbolSize) {
    if (aSymbolSize != null && aSymbolSize > 0) {
      TLcdDefaultMS2525bStyle ms2525bStyle = TLcdDefaultMS2525bStyle.getNewInstance();
      ms2525bStyle.setSizeSymbol(aSymbolSize);
      return new DummyCodedAndStyled(aSymbolCode, ms2525bStyle);
    }
    return new DummyCoded(aSymbolCode);
  }

  private static class DummyCoded implements ILcdMS2525bCoded {
    private String fCode;

    private DummyCoded(String fCode) {
      this.fCode = fCode;
    }

    @Override
    public String getMS2525Code() {
      return fCode;
    }

    @Override
    public ELcdMS2525Standard getMS2525Standard() {
      return ELcdMS2525Standard.MIL_STD_2525c;
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

  private static class DummyCodedAndStyled extends DummyCoded implements ILcdMS2525bStyled {
    private ILcdMS2525bStyle fMS2525bStyle;

    private DummyCodedAndStyled(String aCode, ILcdMS2525bStyle aMS2525bStyle) {
      super(aCode);
      fMS2525bStyle = aMS2525bStyle;
    }

    @Override
    public ILcdMS2525bStyle getMS2525bStyle() {
      return fMS2525bStyle;
    }
  }
}

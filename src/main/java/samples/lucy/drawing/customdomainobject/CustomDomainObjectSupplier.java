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
package samples.lucy.drawing.customdomainobject;

import java.awt.Color;
import java.util.List;

import com.luciad.datamodel.TLcdDataProperty;
import com.luciad.format.xml.bind.schema.ILcdXMLSchemaBasedDecoderLibrary;
import com.luciad.format.xml.bind.schema.ILcdXMLSchemaBasedEncoderLibrary;
import com.luciad.gui.ILcdIcon;
import com.luciad.gui.TLcdIconFactory;
import com.luciad.gui.TLcdSymbol;
import com.luciad.lucy.ILcyLucyEnv;
import com.luciad.lucy.addons.drawing.format.ALcyDomainObjectSupplier;
import com.luciad.lucy.addons.drawing.model.TLcyDrawingDataModelDescriptor;
import com.luciad.lucy.gui.customizer.ILcyCustomizerPanel;
import com.luciad.lucy.gui.customizer.ILcyCustomizerPanelFactory;
import com.luciad.lucy.model.ALcyDomainObjectConverter;
import com.luciad.lucy.util.context.TLcyDomainObjectContext;
import com.luciad.lucy.util.properties.ALcyProperties;
import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelDescriptor;
import com.luciad.reference.ILcdGeodeticReference;
import com.luciad.shape.ILcdShape;
import com.luciad.util.ILcdFilter;
import com.luciad.view.gxy.ILcdGXYEditorProvider;
import com.luciad.view.gxy.ILcdGXYPainterProvider;
import com.luciad.view.gxy.painter.TLcdGXYIconPainter;

import samples.lucy.util.DomainObjectCustomizerWrapper;

/**
 * {@link ALcyDomainObjectSupplier} extension for custom domain objects.
 */
public class CustomDomainObjectSupplier extends ALcyDomainObjectSupplier {

  private ILcyLucyEnv fLucyEnv;
  private ILcdFilter fDomainObjectFilter = new ILcdFilter() {

    @Override
    public boolean accept(Object aObject) {
      return aObject instanceof CustomDomainObject;
    }

  };

  public CustomDomainObjectSupplier(String aDomainObjectID, ALcyProperties aProperties, ILcyLucyEnv aLucyEnv) {
    super(aDomainObjectID, aProperties);
    this.fLucyEnv = aLucyEnv;
  }

  @Override
  public boolean canHandle(Object aDomainObject) {
    return fDomainObjectFilter.accept(aDomainObject);
  }

  @Override
  public ILcdShape createDomainObject(ILcdModel aForModel) {
    //we know the TLcyDrawingDataModelDescriptor only contains one data type
    return new CustomDomainObject(createInitialPropertyValues(aForModel),
                                  ((TLcyDrawingDataModelDescriptor) aForModel.getModelDescriptor()).getDataType());
  }

  @Override
  public ALcyDomainObjectConverter createDomainObjectConverter() {
    return new CustomDomainObjectConverter(this);
  }

  @Override
  public ILcyCustomizerPanelFactory createDomainObjectCustomizerPanelFactory() {
    return new ILcyCustomizerPanelFactory() {

      private ILcdFilter fDomainObjectContextFilter = new ILcdFilter() {

        @Override
        public boolean accept(Object aObject) {
          return aObject instanceof TLcyDomainObjectContext && canHandle(((TLcyDomainObjectContext) aObject).getDomainObject());
        }

      };

      @Override
      public boolean canCreateCustomizerPanel(Object aObject) {
        return fDomainObjectContextFilter.accept(aObject);
      }

      // we wrap with a domain object customizer to get locking and model change event
      // propagation
      @Override
      public ILcyCustomizerPanel createCustomizerPanel(Object aObject) {
        return new DomainObjectCustomizerWrapper(fDomainObjectContextFilter, new CustomDomainObjectCustomizerPanel(fDomainObjectContextFilter, fLucyEnv));
      }

    };
  }

  /**
   * Uses an existing icon painter with a filled circle icon 
   * as {@link ILcdGXYEditorProvider}.
   */
  @Override
  public ILcdGXYEditorProvider createGXYEditorProvider() {
    return createIconPainter();
  }

  /**
   * Uses an existing icon painter 
   * as {@link ILcdGXYPainterProvider}. We only make sure that the selection icon
   * is always larger than the icon that is inside it.
   */
  @Override
  public ILcdGXYPainterProvider createGXYPainterProvider() {
    return createIconPainter();
  }

  private TLcdGXYIconPainter createIconPainter() {
    TLcdGXYIconPainter iconPainter = new TLcdGXYIconPainter();
    ILcdIcon icon = TLcdIconFactory.create(TLcdIconFactory.DRAW_SPHERE_ICON);
    iconPainter.setIcon(icon);
    int size = Math.max(icon.getIconWidth(), icon.getIconHeight()) + 2;
    iconPainter.setSelectionIcon(new TLcdSymbol(TLcdSymbol.RECT, size, Color.MAGENTA));
    return iconPainter;
  }

  @Override
  public ILcdXMLSchemaBasedEncoderLibrary[] createXMLSchemaBasedEncoderLibraries() {
    return new ILcdXMLSchemaBasedEncoderLibrary[]{CustomDomainObjectXMLSupport.createXMLSchemaBasedEncoderLibrary()};
  }

  @Override
  public ILcdXMLSchemaBasedDecoderLibrary[] createXMLSchemaBasedDecoderLibraries() {
    return new ILcdXMLSchemaBasedDecoderLibrary[]{
        CustomDomainObjectXMLSupport.createXMLSchemaBasedDecoderLibrary(this)
    };
  }

  @Override
  public boolean canCreateDomainObject(ILcdModel aForModel) {
    return aForModel.getModelReference() instanceof ILcdGeodeticReference;
  }

  private static Object[] createInitialPropertyValues(ILcdModel aTargetModel) {
    ILcdModelDescriptor aModelDescriptor = aTargetModel.getModelDescriptor();
    Object[] result = null;
    if (aModelDescriptor instanceof TLcyDrawingDataModelDescriptor) {
      TLcyDrawingDataModelDescriptor dataModelDescriptor = (TLcyDrawingDataModelDescriptor) aModelDescriptor;
      List<TLcdDataProperty> dataProperties = dataModelDescriptor.getDataType().getProperties();
      result = new Object[dataProperties.size()];
      for (int i = 0; i < dataProperties.size(); i++) {
        TLcdDataProperty property = dataProperties.get(i);
        Class<?> clazz = property.getType().getInstanceClass();
        if (String.class.equals(clazz)) {
          result[i] = "";
        } else if (Integer.class.equals(clazz)) {
          result[i] = 0;
        } else if (Long.class.equals(clazz)) {
          result[i] = (long) 0;
        } else if (Float.class.equals(clazz)) {
          result[i] = (float) 0;
        } else if (Double.class.equals(clazz)) {
          result[i] = (double) 0;
        } else if (Boolean.class.equals(clazz)) {
          result[i] = false;
        }
      }
    }
    return result;
  }

}

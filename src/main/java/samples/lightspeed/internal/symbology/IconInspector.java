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
package samples.lightspeed.internal.symbology;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import samples.symbology.common.EMilitarySymbology;
import samples.symbology.common.app6.APP6ModelDescriptor;
import samples.symbology.common.app6.StyledEditableAPP6Object;
import samples.symbology.common.gui.SymbolCustomizerFactory;
import samples.symbology.common.gui.customizer.AbstractSymbolCustomizer;
import samples.symbology.common.ms2525.MS2525ModelDescriptor;
import samples.symbology.common.ms2525.StyledEditableMS2525Object;
import samples.symbology.common.util.MilitarySymbolFacade;
import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelListener;
import com.luciad.model.TLcdModelChangedEvent;
import com.luciad.model.TLcdVectorModel;
import com.luciad.shape.shape2D.ALcd2DEditablePolypoint;
import com.luciad.shape.shape2D.TLcdXYPoint;
import com.luciad.symbology.app6a.model.ELcdAPP6Standard;
import com.luciad.symbology.app6a.model.ILcdAPP6ACoded;
import com.luciad.symbology.app6a.model.TLcdAPP6ANode;
import com.luciad.symbology.app6a.model.TLcdEditableAPP6AObject;
import com.luciad.symbology.app6a.view.lightspeed.TLspAPP6ALayerBuilder;
import com.luciad.symbology.milstd2525b.model.ELcdMS2525Standard;
import com.luciad.symbology.milstd2525b.model.TLcdEditableMS2525bObject;
import com.luciad.symbology.milstd2525b.model.TLcdMS2525bNode;
import com.luciad.symbology.milstd2525b.view.lightspeed.TLspMS2525bLayerBuilder;
import com.luciad.util.ILcdChangeListener;
import com.luciad.util.ILcdFireEventMode;
import com.luciad.util.TLcdChangeEvent;
import com.luciad.view.lightspeed.TLspAWTView;
import com.luciad.view.lightspeed.TLspViewBuilder;
import com.luciad.view.lightspeed.camera.TLspViewXYZWorldTransformation2D;
import com.luciad.view.lightspeed.layer.ILspLayer;

import samples.common.model.CartesianReference;

public class IconInspector extends JFrame {
  private final EnumMap<EMilitarySymbology, List<String>> fFlatHierarchies;

  private final CartesianReference fReference = CartesianReference.getInstance();
  private final TLspAWTView fMapView;
  private final AbstractSymbolCustomizer fSymbolCustomizer;
  private final EnumMap<EMilitarySymbology, ILspLayer> fLayers;

  private JTextField fSIDC;
  private JButton fGo;
  private JButton fPrev;
  private JButton fNext;

  private EMilitarySymbology fActiveSymbology;
  private ILspLayer fActiveLayer;
  private ILcdModel fActiveModel;
  private Object fActiveSymbol;
  private int fActiveSymbolIndex = -1;

  private IconInspector() {
    fFlatHierarchies = createFlatHierarchies();

    fMapView = TLspViewBuilder.newBuilder()
                              .size(300, 300)
                              .worldReference(fReference)
                              .buildAWTView();
    fMapView.setController(null);
    ((TLspViewXYZWorldTransformation2D) fMapView.getViewXYZWorldTransformation())
        .lookAt(new TLcdXYPoint(0, 0), new Point(150, 150), 1, 0);
    fSymbolCustomizer = SymbolCustomizerFactory.createCustomizer(EnumSet.of(SymbolCustomizerFactory.Part.REGULAR), true, null, true, null, null);
    fLayers = createLayers();
    createGUI();
  }

  private void createGUI() {
    final SymbolChangeListener symbolChangeListener = new SymbolChangeListener();
    fSymbolCustomizer.addChangeListener(symbolChangeListener);

    setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    setTitle("Icon Inspector");
    setLayout(new BorderLayout());
    KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(symbolChangeListener);

    JPanel mapViewContainer = new JPanel();
    mapViewContainer.add(fMapView.getHostComponent());
    add(mapViewContainer, BorderLayout.WEST);

    add(fSymbolCustomizer.getComponent(), BorderLayout.CENTER);

    JPanel controls = new JPanel();
    controls.setLayout(new BoxLayout(controls, BoxLayout.X_AXIS));
    controls.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, Color.BLACK));
    add(controls, BorderLayout.NORTH);
    JComboBox<EMilitarySymbology> symbologySelector = new JComboBox<>(EMilitarySymbology.values());
    symbologySelector.addActionListener(new MySymbologySelectionListener());
    fSIDC = new JTextField();
    fSIDC.setToolTipText("Enter a symbol code. Enter to load, ESC to revert to the current symbol.");
    fSIDC.addKeyListener(symbolChangeListener);
    fGo = new JButton("\u23CE");
    fGo.setToolTipText("Load selected symbol");
    fGo.addActionListener(symbolChangeListener);
    fPrev = new JButton("\u25c0");
    fPrev.setToolTipText("Previous symbol in hierarchy (PgUp)");
    fPrev.addActionListener(symbolChangeListener);
    fNext = new JButton("\u25b6");
    fNext.setToolTipText("Next symbol in hierarchy (PgDn)");
    fNext.addActionListener(symbolChangeListener);
    controls.add(symbologySelector);
    controls.add(fSIDC);
    controls.add(fGo);
    controls.add(fPrev);
    controls.add(fNext);

    fActiveSymbology = (EMilitarySymbology) symbologySelector.getSelectedItem();
    changeLayer(fActiveSymbology);

    pack();
    setVisible(true);
  }

  private static EnumMap<EMilitarySymbology, List<String>> createFlatHierarchies() {
    EnumMap<EMilitarySymbology, List<String>> flatHierarchies = new EnumMap<>(EMilitarySymbology.class);
    for (EMilitarySymbology symbology : EMilitarySymbology.values()) {
      List<String> flatHierarchy = new ArrayList<>();
      if (symbology == EMilitarySymbology.APP6A ||
          symbology == EMilitarySymbology.APP6B ||
          symbology == EMilitarySymbology.APP6C) {
        final List<TLcdAPP6ANode> nodes = new ArrayList<>();
        flatten(TLcdAPP6ANode.getRoot(app6(symbology)), nodes);
        for (TLcdAPP6ANode node : nodes) {
          final String codeMask = node.getCodeMask();
          if (codeMask != null) {
            flatHierarchy.add(codeMask);
          }
        }
      } else {
        final List<TLcdMS2525bNode> nodes = new ArrayList<>();
        flatten(TLcdMS2525bNode.getRoot(ms2525(symbology)), nodes);
        for (TLcdMS2525bNode node : nodes) {
          final String codeMask = node.getCodeMask();
          if (codeMask != null) {
            flatHierarchy.add(codeMask);
          }
        }
      }
      flatHierarchies.put(symbology, flatHierarchy);
    }
    return flatHierarchies;
  }

  private static void flatten(TLcdAPP6ANode aRoot, List<TLcdAPP6ANode> aTreeSFCT) {
    aTreeSFCT.add(aRoot);
    for (TLcdAPP6ANode child : aRoot.getChildren()) {
      flatten(child, aTreeSFCT);
    }
  }

  private static void flatten(TLcdMS2525bNode aRoot, List<TLcdMS2525bNode> aTreeSFCT) {
    aTreeSFCT.add(aRoot);
    for (TLcdMS2525bNode child : aRoot.getChildren()) {
      flatten(child, aTreeSFCT);
    }
  }

  private EnumMap<EMilitarySymbology, ILspLayer> createLayers() {
    EnumMap<EMilitarySymbology, ILspLayer> layers = new EnumMap<>(EMilitarySymbology.class);
    for (EMilitarySymbology symbology : EMilitarySymbology.values()) {
      final ILcdModel model = createModel(symbology);
      switch (symbology) {
      case APP6A:
      case APP6B:
      case APP6C:
        layers.put(symbology, TLspAPP6ALayerBuilder.newBuilder().model(model).build());
        break;
      case MILSTD_2525B:
      case MILSTD_2525C:
        layers.put(symbology, TLspMS2525bLayerBuilder.newBuilder().model(model).build());
        break;
      }
    }
    return layers;
  }

  private ILcdModel createModel(EMilitarySymbology aSymbology) {
    final TLcdVectorModel model = new TLcdVectorModel();
    model.setModelReference(fReference);
    fActiveSymbolIndex = 0;
    final String symbolCode = fFlatHierarchies.get(aSymbology).get(fActiveSymbolIndex);
    switch (aSymbology) {
    case APP6A:
    case APP6B:
    case APP6C:
      model.setModelDescriptor(new APP6ModelDescriptor(null, aSymbology.name(), "APP-6", null, aSymbology));
      final ELcdAPP6Standard app6Standard = app6(aSymbology);
      final StyledEditableAPP6Object app6Object = new StyledEditableAPP6Object(symbolCode, app6Standard);
      app6Object.getAPP6AStyle().setSizeSymbol(180);
      app6Object.move2DPoint(0, 0, -50);
      model.addElement(app6Object, ILcdModel.NO_EVENT);
      model.addModelListener(new AffiliationThumbnails(model, app6Object));
      break;
    case MILSTD_2525B:
    case MILSTD_2525C:
      model.setModelDescriptor(new MS2525ModelDescriptor(null, aSymbology.name(), "MIL-STD 2525", null, aSymbology));
      final ELcdMS2525Standard ms2525Standard = ms2525(aSymbology);
      final StyledEditableMS2525Object ms2525Object = new StyledEditableMS2525Object(symbolCode, ms2525Standard);
      ms2525Object.getMS2525bStyle().setSizeSymbol(180);
      ms2525Object.move2DPoint(0, 0, -50);
      model.addElement(ms2525Object, ILcdModel.NO_EVENT);
      model.addModelListener(new AffiliationThumbnails(model, ms2525Object));
      break;
    }
    return model;
  }

  private static ELcdAPP6Standard app6(EMilitarySymbology aSymbology) {
    return aSymbology == EMilitarySymbology.APP6A ? ELcdAPP6Standard.APP_6A :
           aSymbology == EMilitarySymbology.APP6B ? ELcdAPP6Standard.APP_6B :
           aSymbology == EMilitarySymbology.APP6C ? ELcdAPP6Standard.APP_6C :
           null;
  }

  private static ELcdMS2525Standard ms2525(EMilitarySymbology aSymbology) {
    return aSymbology == EMilitarySymbology.MILSTD_2525B ? ELcdMS2525Standard.MIL_STD_2525b :
           aSymbology == EMilitarySymbology.MILSTD_2525C ? ELcdMS2525Standard.MIL_STD_2525c :
           null;
  }

  private void changeLayer(EMilitarySymbology aSymbology) {
    int index = Math.max(0, fMapView.layerCount() - 1);
    if (fActiveLayer != null) {
      index = fMapView.indexOf(fActiveLayer);
      fActiveLayer.clearSelection(ILcdFireEventMode.FIRE_NOW);
      fMapView.removeLayer(fActiveLayer);
    }
    final ILspLayer layer = fLayers.get(aSymbology);
    if (layer != null) {
      fMapView.addLayer(layer);
      fMapView.moveLayerAt(index, layer);
      // Fitting causes symbols to be cut off (bounds don't take symbol size into account)
      // FitUtil.fitOnLayers(this, fMapView, false, layer);
      fActiveModel = layer.getModel();
      fActiveSymbol = fActiveModel.elements().nextElement();
      final String sidc = MilitarySymbolFacade.getSIDC(fActiveSymbol);
      fActiveSymbolIndex = symbolIndex(sidc);
      fSymbolCustomizer.setSymbol(MilitarySymbolFacade.retrieveSymbology(fActiveSymbol), fActiveModel, fActiveSymbol);
      fSIDC.setText(sidc);
    }
    fActiveLayer = layer;
  }

  // We can't use indexOf because of masking ('*' in the codes)
  private int symbolIndex(String aSIDC) {
    List<String> codeMasks = fFlatHierarchies.get(fActiveSymbology);
    int count = codeMasks.size();
    int index;
    // It's very likely we're at the same symbol or one of its neighbours
    for (index = fActiveSymbolIndex - 1; index <= fActiveSymbolIndex + 1; ++index) {
      if (index >= 0 && index < count) {
        if (matches(aSIDC, codeMasks.get(index))) {
          return index;
        }
      }
    }
    // Bad luck, scan the whole list
    for (index = 0; index < count; ++index) {
      if (matches(aSIDC, codeMasks.get(index))) {
        return index;
      }
    }
    return -1;
  }

  private boolean matches(String aSIDC, String aCodeMask) {
    switch (fActiveSymbology) {
    case APP6A:
    case APP6B:
    case APP6C:
      final ELcdAPP6Standard app6 = app6(fActiveSymbology);
      return TLcdAPP6ANode.get(aSIDC, app6) == TLcdAPP6ANode.get(aCodeMask, app6);
    case MILSTD_2525B:
    case MILSTD_2525C:
      final ELcdMS2525Standard ms2525 = ms2525(fActiveSymbology);
      return TLcdMS2525bNode.get(aSIDC, ms2525) == TLcdMS2525bNode.get(aCodeMask, ms2525);
    }
    return false;
  }

  private static Object cloneSymbol(Object aSymbol, int aSize) {
    if (aSymbol instanceof TLcdEditableAPP6AObject) {
      final StyledEditableAPP6Object object =
          new StyledEditableAPP6Object(((TLcdEditableAPP6AObject) aSymbol).getAPP6ACode(),
                                       ((TLcdEditableAPP6AObject) aSymbol).getAPP6Standard());
      object.getAPP6AStyle().setSizeSymbol(aSize);
      return object;
    } else if (aSymbol instanceof TLcdEditableMS2525bObject) {
      final StyledEditableMS2525Object object =
          new StyledEditableMS2525Object(((TLcdEditableMS2525bObject) aSymbol).getMS2525Code(),
                                         ((TLcdEditableMS2525bObject) aSymbol).getMS2525Standard());
      object.getMS2525bStyle().setSizeSymbol(aSize);
      return object;
    }
    throw new IllegalArgumentException();
  }

  private static void updateSymbolPosition(Object aActiveSymbol, double aX, double aY, double aDelta) {
    switch (MilitarySymbolFacade.getPointCount(aActiveSymbol)) {
    case 1:
      MilitarySymbolFacade.move2DPoint(aActiveSymbol, 0, aX, aY);
      break;
    case 2:
      MilitarySymbolFacade.move2DPoint(aActiveSymbol, 0, aX - aDelta, aY);
      MilitarySymbolFacade.move2DPoint(aActiveSymbol, 1, aX + aDelta, aY);
      break;
    case 3:
      MilitarySymbolFacade.move2DPoint(aActiveSymbol, 0, aX - aDelta, aY);
      MilitarySymbolFacade.move2DPoint(aActiveSymbol, 1, aX, aY + aDelta);
      MilitarySymbolFacade.move2DPoint(aActiveSymbol, 2, aX + aDelta, aY);
      break;
    case 4:
      MilitarySymbolFacade.move2DPoint(aActiveSymbol, 0, aX - aDelta, aY);
      MilitarySymbolFacade.move2DPoint(aActiveSymbol, 1, aX - aDelta, aY + aDelta);
      MilitarySymbolFacade.move2DPoint(aActiveSymbol, 2, aX + aDelta, aY + aDelta);
      MilitarySymbolFacade.move2DPoint(aActiveSymbol, 3, aX + aDelta, aY);
      break;
    default:
      throw new IllegalArgumentException("Unsupported point count");
    }
  }

  public static void main(String[] args) {
    new IconInspector();
  }

  private class MySymbologySelectionListener implements ActionListener {
    @Override
    public void actionPerformed(ActionEvent e) {
      fActiveSymbology = (EMilitarySymbology) ((JComboBox) e.getSource()).getSelectedItem();
      changeLayer(fActiveSymbology);
    }
  }

  private class AffiliationThumbnails implements ILcdModelListener {
    private final String[] fAffiliations = new String[]{"Unknown", "Friend", "Hostile", "Neutral"};
    private final ILcdModel fModel;
    private final Object fParentSymbol;
    private final Vector<Object> fThumbnails;

    private AffiliationThumbnails(ILcdModel aModel, Object aParentSymbol) {
      fModel = aModel;
      fParentSymbol = aParentSymbol;
      fThumbnails = new Vector<>(fAffiliations.length);
      for (int i = 0; i < fAffiliations.length; ++i) {
        final Object symbol = cloneSymbol(fParentSymbol, 64);
        assert symbol != null;
        ((ALcd2DEditablePolypoint) symbol).move2DPoint(0, -100 + i * 64, 250);
        fThumbnails.add(symbol);
      }
      updateThumbnails(MilitarySymbolFacade.getSIDC(fParentSymbol));
      fModel.addElements(fThumbnails, ILcdModel.NO_EVENT);
    }

    @Override
    public void modelChanged(final TLcdModelChangedEvent aEvent) {
      if (aEvent.containsElement(fParentSymbol)) {
        final String sidc = MilitarySymbolFacade.getSIDC(fParentSymbol);
        fSIDC.setText(sidc);
        fSIDC.setForeground(Color.BLACK);
        fActiveSymbolIndex = symbolIndex(sidc);
        updateThumbnails(sidc);
        SwingUtilities.invokeLater(new Runnable() {
          @Override
          public void run() {
            fModel.elementsChanged(fThumbnails, ILcdModel.FIRE_NOW);
          }
        });
      }
    }

    private void updateThumbnails(final String aSIDC) {
      // APP-6C encodes the affiliation in StandardIdentity2
      final MilitarySymbolFacade.Modifier stdId2 = MilitarySymbolFacade.getModifier(fParentSymbol, ILcdAPP6ACoded.sStandardIdentity2);
      if (stdId2 != null) {
        for (int i = 0; i < fAffiliations.length; ++i) {
          final Object symbol = fThumbnails.get(i);
          MilitarySymbolFacade.setSIDC(symbol, aSIDC);
          if (stdId2.getPossibleValues().contains(fAffiliations[i])) {
            MilitarySymbolFacade.setModifierValue(symbol, stdId2, fAffiliations[i]);
          }
          updateSymbolPosition(symbol, -100 + i * 64, 250, 20);
        }
      } else {
        final Collection<String> possible = MilitarySymbolFacade.getPossibleAffiliationValues(fParentSymbol);
        for (int i = 0; i < fAffiliations.length; ++i) {
          final Object symbol = fThumbnails.get(i);
          MilitarySymbolFacade.setSIDC(symbol, aSIDC);
          if (possible.contains(fAffiliations[i])) {
            MilitarySymbolFacade.setAffiliationValue(symbol, fAffiliations[i]);
          }
          updateSymbolPosition(symbol, -100 + i * 64, 250, 20);
        }
      }
    }
  }

  private class SymbolChangeListener implements KeyEventDispatcher, KeyListener, ActionListener, ILcdChangeListener {
    @Override
    public void keyTyped(KeyEvent aKeyEvent) {
    }

    @Override
    public void keyPressed(KeyEvent aKeyEvent) {
    }

    @Override
    public void keyReleased(KeyEvent aKeyEvent) {
      if (aKeyEvent.getSource() == fSIDC) {
        if (aKeyEvent.getKeyCode() == KeyEvent.VK_ENTER) {
          setHierarchy();
        } else if (aKeyEvent.getKeyCode() == KeyEvent.VK_ESCAPE) {
          fSIDC.setText(MilitarySymbolFacade.getSIDC(fActiveSymbol));
          fSIDC.setForeground(Color.BLACK);
        }
      }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent aKeyEvent) {
      if (aKeyEvent.getKeyCode() == KeyEvent.VK_PAGE_UP) {
        // Only handle KEY_RELEASED, but always consume the event to avoid breaking components
        if (aKeyEvent.getID() == KeyEvent.KEY_RELEASED) {
          navigateHierarchy(-1);
        }
        return true;
      } else if (aKeyEvent.getKeyCode() == KeyEvent.VK_PAGE_DOWN) {
        if (aKeyEvent.getID() == KeyEvent.KEY_RELEASED) {
          navigateHierarchy(1);
        }
        return true;
      }
      return false;
    }

    @Override
    public void actionPerformed(ActionEvent aActionEvent) {
      if (aActionEvent.getSource() == fGo) {
        setHierarchy();
      } else if (aActionEvent.getSource() == fPrev) {
        navigateHierarchy(-1);
      } else if (aActionEvent.getSource() == fNext) {
        navigateHierarchy(1);
      }
    }

    @Override
    public void stateChanged(TLcdChangeEvent aChangeEvent) {
      updateSymbolPosition(fActiveSymbol, 0, -50, 50);
    }

    private void setHierarchy() {
      try {
        final String sidc = fSIDC.getText().trim();
        if (sidc.length() <= 8) {
          MilitarySymbolFacade.setSIDC(fActiveSymbol,
                                       String.format("1000%s0000%s%s",
                                                     sidc.substring(0, 2),
                                                     sidc.substring(2),
                                                     "000000000000".substring(0, 12 - sidc.length())));
        } else {
          MilitarySymbolFacade.setSIDC(fActiveSymbol, sidc);
        }
        updateSymbolPosition(fActiveSymbol, 0, -50, 50);
        fActiveModel.elementChanged(fActiveSymbol, ILcdModel.FIRE_NOW);
      } catch (Exception e) {
        e.printStackTrace();
        fSIDC.setForeground(Color.RED);
      }
    }

    private void navigateHierarchy(int aDelta) {
      List<String> codeMasks = fFlatHierarchies.get(fActiveSymbology);
      int index = fActiveSymbolIndex + aDelta;
      if (index >= 0 && index < codeMasks.size()) {
        MilitarySymbolFacade.changeHierarchy(fActiveSymbol, codeMasks.get(index));
        updateSymbolPosition(fActiveSymbol, 0, -50, 50);
        fActiveModel.elementChanged(fActiveSymbol, ILcdModel.FIRE_NOW);
      }
    }
  }
}

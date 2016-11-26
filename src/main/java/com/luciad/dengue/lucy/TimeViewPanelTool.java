package com.luciad.dengue.lucy;

import com.luciad.dengue.timeview.TimeSlider;
import com.luciad.dengue.timeview.TimeView;
import com.luciad.lucy.gui.ALcyApplicationPaneTool;
import com.luciad.lucy.util.properties.ALcyProperties;

import javax.swing.*;
import java.awt.*;

//import dengue.controller.PermitHoverController;
//import dengue.format.permit.PermitModelDescriptor;
//import dengue.format.permit.PermitTimeModelFactory;
//import dengue.model.Permit;
//import dengue.view.PermitBarLayerFactory;
//import dengue.view.PermitLayerFactory;
//import dengue.view.filter.PermitCategoryFilter;
//import dengue.view.filter.PermitFilter;
//import dengue.view.style.PermitCategoryLabelStyler;

/**
 * Created by tomc on 23/05/2016.
 */
public class TimeViewPanelTool extends ALcyApplicationPaneTool {

  private JPanel viewPanel;
  private TimeView timeView;

  public TimeViewPanelTool(ALcyProperties aProperties,
                           String aLongPrefix,
                           String aShortPrefix) {
    super(aProperties, aLongPrefix + "time.", aShortPrefix + "time.");
    timeView = new TimeView();
    viewPanel = createPanel(timeView);
  }

//  public void initialize(ILspLayer aPermitLayer, PermitFilter aPermitFilter) {
//    long[] times = findStartAndEnd(aPermitLayer.getModel());
//
//    timeView.updateTimeRange(times[0], times[1]);
//    timeView.addChangeListener(new ILcdChangeListener() {
//      @Override
//      public void stateChanged(TLcdChangeEvent aTLcdChangeEvent) {
//        aPermitFilter.setTime(timeView.getTime(), timeView.getIntervalStart(), timeView.getIntervalEnd());
//      }
//    });
//
//    PermitTimeModelFactory permitTimeModelFactory = new PermitTimeModelFactory(aPermitLayer.getModel(), timeView.getTimeHeightReference());
//    ILspLayer barLayer = new PermitBarLayerFactory().createLayer(permitTimeModelFactory.createBarModel());
//    timeView.getView().addLayer(barLayer);
//    ILspLayer timeLayer = new PermitLayerFactory(true).createLayer(permitTimeModelFactory.createTimeModel());
//    timeView.getView().addLayer(timeLayer);
//
////    PermitHoverController permitHoverController = new PermitHoverController(timeView.getView(), timeLayer);
////    permitHoverController.appendController(timeView.getView().getController());
////    timeView.getView().setController(permitHoverController);
//
//    syncSelection(aPermitLayer, timeLayer);
//    syncSelection(timeLayer, aPermitLayer);
//
//    PermitCategoryFilter permitCategoryFilter = aPermitFilter.getCategoryFilter();
//    ((TLspLayer)timeLayer).setFilter(permitCategoryFilter);
//    addBarFilterListener(permitCategoryFilter, barLayer);
//    addCategoryFilter(timeView.getView(), permitCategoryFilter);
//  }

//  private void addBarFilterListener(PermitCategoryFilter aPermitCategoryFilter, ILspLayer aBarLayer) {
//    aPermitCategoryFilter.addChangeListener(new ILcdChangeListener() {
//      @Override
//      public void stateChanged(TLcdChangeEvent aTLcdChangeEvent) {
//        HashMap<Permit.Category, Boolean> categoryFlags = aPermitCategoryFilter.getCategoryFlags();
//        Enumeration elements = aBarLayer.getModel().elements();
//        while (elements.hasMoreElements()) {
//          Object nextElement = elements.nextElement();
//          if (nextElement instanceof PermitTimeModelFactory.PermitBar) {
//            PermitTimeModelFactory.PermitBar permitBar = (PermitTimeModelFactory.PermitBar) nextElement;
//            permitBar.updateHeight(categoryFlags);
//            aBarLayer.getModel().elementChanged(permitBar, ILcdModel.FIRE_LATER);
//          }
//        }
//        aBarLayer.getModel().fireCollectedModelChanges();
//      }
//    });
//  }
//
//  private void addCategoryFilter(ILspAWTView aView, PermitCategoryFilter aPermitCategoryFilter) {
//    JPanel filterPanel = new JPanel();
//    filterPanel.setLayout(new GridLayout(2, 6));
//    for (Permit.Category category : Permit.Category.values()) {
//      filterPanel.add(createFilterBox(category, aPermitCategoryFilter));
//    }
//
//    aView.getOverlayComponent().add(filterPanel, TLcdOverlayLayout.Location.NORTH_EAST);
//  }
//
//  private Component createFilterBox(Permit.Category aCategory, PermitCategoryFilter aPermitCategoryFilter) {
//    JCheckBox checkBox = new JCheckBox();
//    checkBox.setSelected(true);
//    checkBox.addActionListener(new ActionListener() {
//      @Override
//      public void actionPerformed(ActionEvent e) {
//        aPermitCategoryFilter.changeCategoryFlag(aCategory, checkBox.isSelected());
//      }
//    });
//
//    ILcdIcon icon = PermitCategoryLabelStyler.getIcon(aCategory);
//    TLcdSWIcon swIcon = new TLcdSWIcon(icon);
//    JButton iconButton = new JButton(swIcon);
//    iconButton.setPreferredSize(new Dimension(swIcon.getIconWidth() + 8, swIcon.getIconHeight() + 8));
//    final boolean[] isSelected = {true};
//
//    iconButton.setBackground(ColorPalette.greenTransparant);
//    iconButton.setOpaque(true);
//    iconButton.addActionListener(new ActionListener() {
//      @Override
//      public void actionPerformed(ActionEvent e) {
////        checkBox.doClick();
//
//        if (isSelected[0]) {
//          iconButton.setOpaque(false);
//          isSelected[0] = false;
//        }
//        else {
//          iconButton.setOpaque(true);
//          isSelected[0] = true;
//        }
//
//        aPermitCategoryFilter.changeCategoryFlag(aCategory, isSelected[0]);
//      }
//    });
//
//    JPanel filterPanel = new JPanel();
//    filterPanel.add(checkBox);
//    filterPanel.add(iconButton);
//    return iconButton;
//  }
//
//  private void syncSelection(ILspLayer aLayerOne, final ILspLayer aLayerTwo) {
//    aLayerOne.addSelectionListener(new ILcdSelectionListener() {
//      @Override
//      public void selectionChanged(TLcdSelectionChangedEvent aEvent) {
//        Enumeration selectedElements = aEvent.selectedElements();
//        while ( selectedElements.hasMoreElements()) {
//          aLayerTwo.selectObject(getCorrect(selectedElements.nextElement()), true, ILcdFireEventMode.FIRE_LATER);
//        }
//        Enumeration deselectedElements = aEvent.deselectedElements();
//        while ( deselectedElements.hasMoreElements()) {
//          aLayerTwo.selectObject(getCorrect(deselectedElements.nextElement()), false, ILcdFireEventMode.FIRE_LATER);
//        }
//        aLayerTwo.fireCollectedSelectionChanges();
//      }
//
//      private Object getCorrect(Object aObject) {
//        if (aObject instanceof Permit) {
//          Permit permit = (Permit) aObject;
//          return permit.getTimePermit();
//        }
//        if (aObject instanceof PermitTimeModelFactory.TimePermit) {
//          PermitTimeModelFactory.TimePermit timePermit = (PermitTimeModelFactory.TimePermit) aObject;
//          return timePermit.getPermit();
//        }
//        return null;
//      }
//    });
//  }
//
//  private long[] findStartAndEnd(ILcdModel aPermitModel) {
//    long start = Long.MAX_VALUE;
//    long end = Long.MIN_VALUE;
//
//    Enumeration elements = aPermitModel.elements();
//    while (elements.hasMoreElements()) {
//      Object nextElement = elements.nextElement();
//      if (nextElement instanceof Permit) {
//        Permit permit = (Permit) nextElement;
//
//        long thisStart = (long) permit.getValue(PermitModelDescriptor.START);
//        long thisEnd = (long) permit.getValue(PermitModelDescriptor.END);
//        if (thisStart < start) {
//          start = thisStart;
//        }
//        if (thisEnd > end) {
//          end = thisEnd;
//        }
//      }
//    }
//
//    return new long[]{start, end};
//  }

  private JPanel createPanel(TimeView aTimeView) {
    return new TimeSlider(aTimeView);
  }

  public TimeView getTimeView() {
    return timeView;
  }

  @Override
  protected Component createContent() {
    return viewPanel;
  }
}

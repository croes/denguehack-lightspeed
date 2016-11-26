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
package samples.common;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager2;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;


/**
 * <code>LayoutManager</code> used by the {@link TwoColumnLayoutBuilder}.
 * The <code>LayoutManager</code> is only public to allow combining the UI of several panels.
 */
public final class TwoColumnLayoutManager implements LayoutManager2 {

  private FormLayout fDelegate;
  private List<ComponentConstraintsContainer> fComponentConstraintsContainers;

  public TwoColumnLayoutManager(List<ComponentConstraintsContainer> aComponentConstraintsContainers, Container aContainer) {
    fComponentConstraintsContainers = new ArrayList<ComponentConstraintsContainer>(aComponentConstraintsContainers.size());
    fComponentConstraintsContainers.addAll(aComponentConstraintsContainers);
    Collections.sort(fComponentConstraintsContainers);
    aContainer.removeAll();

    initFormLayout();
    aContainer.setLayout(this);
    addComponents(aContainer);
  }

  private void addComponents(Container aContainer) {
    CellConstraints cc = new CellConstraints();

    for (ComponentConstraintsContainer componentConstraintsContainer : fComponentConstraintsContainers) {
      int row = componentConstraintsContainer.fRow * 2 - 1;//due to the white-space rows

      int column = componentConstraintsContainer.fColumn == 1 ? 2 : 6;//keep the whitespace column at the start in mind
      if (!(componentConstraintsContainer.fLabelComponent)) {
        column += 2;
      }
      int span = 1;
      switch (componentConstraintsContainer.fSpan) {
      case 1:
        span = 1;
        break;
      case 2:
        span = 3;
        break;
      case 4:
        span = 7;
        break;
      }
      aContainer.add(componentConstraintsContainer.fComponent, cc.xyw(column, row, span));
    }
  }

  private void initFormLayout() {
    StringBuilder columnBuilder = new StringBuilder();
    String labelColumn = "right:p:none";
    String whitespaceBetweenColumns = "3dlu:none";
    String editorColumn = "fill:default:grow";//fill all the available horizontal space and can be resized until its minimal size
    String whitespaceBeforeAfter = "2dlu:none";
    columnBuilder.append(whitespaceBeforeAfter).append(",");
    columnBuilder.append(labelColumn).append(",");
    columnBuilder.append(whitespaceBetweenColumns).append(",");
    columnBuilder.append(editorColumn).append(",");
    columnBuilder.append(whitespaceBetweenColumns).append(",");
    columnBuilder.append(labelColumn).append(",");
    columnBuilder.append(whitespaceBetweenColumns).append(",");
    columnBuilder.append(editorColumn).append(",");
    columnBuilder.append(whitespaceBeforeAfter);

    StringBuilder rowBuilder = new StringBuilder();
    int numberOfRows = fComponentConstraintsContainers.get(fComponentConstraintsContainers.size() - 1).fRow;
    for (int i = 0; i < numberOfRows; i++) {
      List<ComponentConstraintsContainer> constraints = findConstraints(fComponentConstraintsContainers, i + 1);
      boolean aGrowVertically = constraints.size() > 0 && constraints.get(0).fGrowVertically;
      if (i != 0) {
        rowBuilder.append(",");
      }
      if (aGrowVertically) {
        rowBuilder.append("fill:default:grow");
      } else {
        rowBuilder.append("center:default:none");
      }
      //add some whitespace between the rows
      if (i != numberOfRows - 1) {
        rowBuilder.append(",").append("1dlu");
      }
    }

    fDelegate = new FormLayout(
        columnBuilder.toString(),
        rowBuilder.toString()
    );
  }

  private List<ComponentConstraintsContainer> findConstraints(List<ComponentConstraintsContainer> aListToSearch, int aRowNumber) {
    List<ComponentConstraintsContainer> result = new ArrayList<ComponentConstraintsContainer>();
    for (ComponentConstraintsContainer componentConstraintsContainer : aListToSearch) {
      if (componentConstraintsContainer.fRow == aRowNumber) {
        result.add(componentConstraintsContainer);
      } else if (componentConstraintsContainer.fRow > aRowNumber) {
        //since they are ordered, no need to keep on searching
        return result;
      }
    }
    return result;
  }

  @Override
  public void addLayoutComponent(Component comp, Object constraints) {
    fDelegate.addLayoutComponent(comp, constraints);
  }

  @Override
  public Dimension maximumLayoutSize(Container target) {
    return fDelegate.maximumLayoutSize(target);
  }

  @Override
  public float getLayoutAlignmentX(Container target) {
    return fDelegate.getLayoutAlignmentX(target);
  }

  @Override
  public float getLayoutAlignmentY(Container target) {
    return fDelegate.getLayoutAlignmentY(target);
  }

  @Override
  public void invalidateLayout(Container target) {
    fDelegate.invalidateLayout(target);
  }

  @Override
  public void addLayoutComponent(String name, Component comp) {
    fDelegate.addLayoutComponent(name, comp);
  }

  @Override
  public void removeLayoutComponent(Component comp) {
    fDelegate.removeLayoutComponent(comp);
    //remove from the list of constraints as well
    Iterator<ComponentConstraintsContainer> iterator = fComponentConstraintsContainers.iterator();
    while (iterator.hasNext()) {
      ComponentConstraintsContainer constraintsContainer = iterator.next();
      if (constraintsContainer.fComponent == comp) {
        iterator.remove();
      }
    }
  }

  @Override
  public Dimension preferredLayoutSize(Container parent) {
    return fDelegate.preferredLayoutSize(parent);
  }

  @Override
  public Dimension minimumLayoutSize(Container parent) {
    return fDelegate.minimumLayoutSize(parent);
  }

  @Override
  public void layoutContainer(Container parent) {
    fDelegate.layoutContainer(parent);
  }

  /**
   * Add all components contained in this layout manager to <code>aBuilderSFCT</code>
   *
   * @param aBuilderSFCT The builder
   */
  public void addComponentsToOtherBuilder(TwoColumnLayoutBuilder aBuilderSFCT) {
    List<ComponentConstraintsContainer> componentConstraintsContainers = new ArrayList<ComponentConstraintsContainer>();
    componentConstraintsContainers.addAll(fComponentConstraintsContainers);
    Collections.sort(componentConstraintsContainers);
    int numberOfRows = componentConstraintsContainers.get(componentConstraintsContainers.size() - 1).fRow;
    for (int i = 0; i < numberOfRows; i++) {
      List<ComponentConstraintsContainer> constraints = findConstraints(componentConstraintsContainers, i + 1);
      if (constraints.size() == 1 && constraints.get(0).fSpan == 4) {
        aBuilderSFCT.row().spanBothColumns(constraints.get(0).fComponent).growVertically(constraints.get(0).fGrowVertically).build();
      } else if (constraints.size() > 0) {
        List<Component> firstColumnComponents = new ArrayList<Component>();
        List<Component> secondColumnComponents = new ArrayList<Component>();
        addComponentsToComponentArray(constraints, 1, firstColumnComponents);
        addComponentsToComponentArray(constraints, 2, secondColumnComponents);
        TwoColumnLayoutBuilder.RowBuilder rowBuilder = aBuilderSFCT.row();
        if (firstColumnComponents.size() == 1) {
          rowBuilder.columnOne(firstColumnComponents.get(0));
        } else if (firstColumnComponents.size() == 2) {
          if (firstColumnComponents.get(0) != null || firstColumnComponents.get(1) != null) {
            rowBuilder.columnOne(firstColumnComponents.get(0), firstColumnComponents.get(1));
          }
        }
        if (secondColumnComponents.size() == 1) {
          rowBuilder.columnTwo(secondColumnComponents.get(0));
        } else if (secondColumnComponents.size() == 2) {
          if (secondColumnComponents.get(0) != null || secondColumnComponents.get(1) != null) {
            rowBuilder.columnTwo(secondColumnComponents.get(0), secondColumnComponents.get(1));
          }
        }
        rowBuilder.build();
      }
    }
  }

  private void addComponentsToComponentArray(List<ComponentConstraintsContainer> aConstraints,
                                             int aColumnIndex,
                                             List<Component> aComponentArraySFCT) {
    for (ComponentConstraintsContainer componentConstraintsContainer : aConstraints) {
      if (componentConstraintsContainer.fColumn == aColumnIndex) {
        aComponentArraySFCT.add(componentConstraintsContainer.fComponent);
        if (!(componentConstraintsContainer.fLabelComponent) && aComponentArraySFCT.size() == 1) {
          aComponentArraySFCT.add(0, null);
        }
        if (componentConstraintsContainer.fSpan == 2) {
          return;
        }
      }
    }
    while (aComponentArraySFCT.size() < 2) {
      aComponentArraySFCT.add(null);
    }
  }

  public static class ComponentConstraintsContainer implements Comparable<ComponentConstraintsContainer> {
    private final int fRow;//row information of the builder, starts at 1
    private final int fColumn;//column information of the builder, either 1 or 2
    private final boolean fLabelComponent;
    private final Component fComponent;
    private final boolean fGrowVertically;
    private final int fSpan;//either 1, 2 or 4

    public ComponentConstraintsContainer(int aRow, int aColumn, boolean aLabelComponent, Component aComponent, boolean aGrowVertically) {
      fRow = aRow;
      fColumn = aColumn;
      fLabelComponent = aLabelComponent;
      fComponent = aComponent;
      fGrowVertically = aGrowVertically;
      fSpan = 1;
    }

    public ComponentConstraintsContainer(int aRow, int aColumn, int aSpan, Component aComponent, boolean aGrowVertically) {
      fRow = aRow;
      fColumn = aColumn;
      fSpan = aSpan;
      fComponent = aComponent;
      fGrowVertically = aGrowVertically;
      fLabelComponent = true;//span always start from the label position
    }

    @Override
    public int compareTo(ComponentConstraintsContainer other) {
      if (other == null) {
        return -1;
      }
      int rowDifference = this.fRow - other.fRow;
      if (rowDifference == 0) {
        return this.fColumn - other.fColumn;
      }
      return rowDifference;
    }
  }
}

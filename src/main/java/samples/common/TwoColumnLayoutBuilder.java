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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.LayoutManager;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;

/**
 * <p>Class which facilitates the population of a <code>Container</code> with <code>Component</code>s
 * in a two-column layout. Each of those columns can contain a "label" and "editor" component. Methods
 * are also available to add separators or components which span a column or the whole width of the
 * <code>Container</code>.</p>
 *
 * <p>Once all <code>Component</code>s have been added to this builder they can be added to the
 * <code>Container</code> by calling the {@link #populate(Container) populate} method. Afterwards
 * this builder can no longer be used.</p>
 *
 * <p>An example usage of this builder: </p>
 *
 * <pre class="code">
 *  //create a frame for the panel
 JFrame frame = new JFrame( "TwoColumnLayoutBuilder demo" );
 frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );

 //use the builder to add components
 TwoColumnLayoutBuilder builder = TwoColumnLayoutBuilder.newBuilder();

 builder.addTitledSeparator( "Label-editor" );
 builder.row().
 columnOne( new JLabel( "First name"), new JTextField( 15 ) ).
 columnTwo( new JLabel( "Last name" ), new JTextField( 15 ) ).build();


 builder.addTitledSeparator( "Full width span" );
 JPanel textAreaPanel = new JPanel( new BorderLayout() );
 textAreaPanel.add( new JScrollPane( new JTextArea(15,50) ), BorderLayout.CENTER );
 builder.row().
 spanBothColumns( textAreaPanel ).
 growVertically( true ).
 build();

 builder.addTitledSeparator( "Half width span" );
 builder.row().
 columnOne( new JTextField( "Left span", 30 ) ).
 build();

 builder.row().
 columnTwo( new JTextField( "Right span", 30) ).
 build();


 //create the panel and use the builder to add the components and initialize the layout
 JPanel panel = new JPanel(  );
 builder.populate( panel );

 //add the panel to the frame and show the UI
 frame.getContentPane().add( panel );

 frame.pack();
 frame.setVisible( true );
 * </pre>
 *
 */
public final class TwoColumnLayoutBuilder {
  /**
   * Create a new builder instance
   * @return a new builder instance
   */
  public static TwoColumnLayoutBuilder newBuilder() {
    return new TwoColumnLayoutBuilder();
  }

  private int fRowCounter = 1;
  private List<TwoColumnLayoutManager.ComponentConstraintsContainer> fConstraintsContainers = new ArrayList<TwoColumnLayoutManager.ComponentConstraintsContainer>();
  private boolean fPopulated = false;

  private TwoColumnLayoutBuilder() {
  }

  /**
   * Adds a titled separator which will span the whole width
   *
   * @param aSeparatorTitle The title for the separator. May be empty but must not be
   *                        <code>null</code>
   *
   * @return this builder instance, allowing to chain method calls
   */
  public TwoColumnLayoutBuilder addTitledSeparator(String aSeparatorTitle) {
    checkPopulated();
    Component titledSeparator = new TitledSeparator(aSeparatorTitle);
    JPanel panel = new JPanel(new BorderLayout());
    panel.add(titledSeparator, BorderLayout.NORTH);
    panel.setBorder(BorderFactory.createEmptyBorder(0, 3, 0, 3));
    return addRow(panel, false);
  }

  private void checkPopulated() {
    if (fPopulated) {
      throw new RuntimeException("Once this builder is used to populate a Container, it is no longer possible to add components to it");
    }
  }

  /**
   * Add a row to this builder
   *
   * @param aFirstColumnComponents  An array containing the components for the first column. The
   *                                array may contain <code>null</code> but must not be
   *                                <code>null</code>. The array should either contain one element
   *                                (which will span the label and editing component locations), or
   *                                two elements (the label component first, the editor component
   *                                second).
   * @param aSecondColumnComponents An array containing the components for the second column. The
   *                                array may contain <code>null</code> but must not be
   *                                <code>null</code>. The array should either contain one element
   *                                (which will span the label and editing component locations), or
   *                                two elements (the label component first, the editor component
   *                                second).
   * @param aGrowVertically         <code>true</code> when the row should grow vertically when the
   *                                container grows vertically, <code>false</code> if the height of
   *                                the row should remain constant (respecting the preferred size of
   *                                the contained components).
   *
   * @return this builder instance, allowing to chain method calls
   */
  private TwoColumnLayoutBuilder addRow(Component[] aFirstColumnComponents, Component[] aSecondColumnComponents, boolean aGrowVertically) {
    checkPopulated();
    handleComponentArray(aFirstColumnComponents, 1, aGrowVertically);
    handleComponentArray(aSecondColumnComponents, 2, aGrowVertically);
    fRowCounter++;
    return this;
  }

  private void handleComponentArray(Component[] aComponentArray, int aColumn, boolean aGrowVertically) {
    if (aComponentArray == null) {
      throw new NullPointerException("Component array must not be null.");
    }
    if (aComponentArray.length > 2 || aComponentArray.length == 0) {
      throw new IllegalArgumentException("Component array maximum length is 2 and minimum length is 1. The length of [" + Arrays.toString(aComponentArray) + "] is [" + aComponentArray.length + "]");
    }
    if (aComponentArray.length == 2) {
      for (int i = 0; i < aComponentArray.length; i++) {
        Component component = aComponentArray[i];
        if (component != null) {
          fConstraintsContainers.add(new TwoColumnLayoutManager.ComponentConstraintsContainer(fRowCounter, aColumn, i == 0, component, aGrowVertically));
        }
      }
    } else if (aComponentArray.length == 1) {
      Component component = aComponentArray[0];
      if (component != null) {
        fConstraintsContainers.add(new TwoColumnLayoutManager.ComponentConstraintsContainer(fRowCounter, aColumn, 2, component, aGrowVertically));
      }
    }
  }

  /**
   * Add a component which will take the same width as the parent container
   *
   * @param aSpanComponent  The component. Must not be <code>null</code>
   * @param aGrowVertically <code>true</code> when the row should grow vertically when the container
   *                        grows vertically, <code>false</code> if the height of the row should
   *                        remain constant (respecting the preferred size of the contained
   *                        components).
   *
   * @return this builder instance, allowing to chain method calls
   */
  private TwoColumnLayoutBuilder addRow(Component aSpanComponent, boolean aGrowVertically) {
    checkPopulated();
    fConstraintsContainers.add(new TwoColumnLayoutManager.ComponentConstraintsContainer(fRowCounter, 1, 4, aSpanComponent, aGrowVertically));
    fRowCounter++;
    return this;
  }

  /**
   * <p>Populate <code>aContainer</code> with all <code>Component</code>s previously added to this
   * builder. The layout of those components will be as specified when the components were added to
   * this builder.</p>
   *
   * <p>After calling this method, this builder can no longer be used.</p>
   *
   * <p><strong>Warning:</strong> this method will remove all <code>Component</code>s which were previously added
   * to <code>aContainer</code>.</p>
   *
   * @param aContainer The container. Must not be <code>null</code>
   */
  public void populate(Container aContainer) {
    if (fPopulated) {
      throw new IllegalArgumentException("The populate method of the builder can only be called once");
    }
    if (aContainer == null) {
      throw new NullPointerException("The container must not be null");
    }
    new TwoColumnLayoutManager(fConstraintsContainers, aContainer);
  }

  /**
   * <p>Returns a builder for one row of this layout. Once all the components are specified on the
   * <code>RowBuilder</code>, use the {@link RowBuilder#build()
   * RowBuilder#build()} method to get a reference back to the updated version of this builder.</p>
   *
   * <p>The class javadoc contains a sample snippet illustrating the use of this method.</p>
   *
   * @return a row builder for this builder
   *
   * @see RowBuilder#build()
   */
  public RowBuilder row() {
    return new RowBuilder(this);
  }

  /**
   * Class following the Builder pattern which allows to add a row to a two column layout.
   */
  public static final class RowBuilder {

    private final TwoColumnLayoutBuilder fBuilder;

    private boolean fGrowVertically = false;
    private Component[] fFirstColumnComponents = null;
    private Component[] fSecondColumnComponents = null;
    private Component fSpanComponent = null;

    private RowBuilder(TwoColumnLayoutBuilder aBuilder) {
      fBuilder = aBuilder;
    }

    /**
     * Add a label and/or component to the first column of this row
     * @param aLabel The label component. May be <code>null</code> in case <code>aComponent</code> is not <code>null</code>
     * @param aComponent The editor component. May be <code>null</code> in case <code>aLabel</code> is not <code>null</code>
     * @return this
     */
    public RowBuilder columnOne(Component aLabel, Component aComponent) {
      fFirstColumnComponents = new Component[]{aLabel, aComponent};
      checkConstraints();
      return this;
    }

    /**
     * Add a component which spans the whole width of the first column
     * @param aComponent The component. Must not be <code>null</code>
     * @return this
     */
    public RowBuilder columnOne(Component aComponent) {
      fFirstColumnComponents = new Component[]{aComponent};
      checkConstraints();
      return this;
    }

    /**
     * Add a label and/or component to the second column of this row
     * @param aLabel The label component. May be <code>null</code> in case <code>aComponent</code> is not <code>null</code>
     * @param aComponent The editor component. May be <code>null</code> in case <code>aLabel</code> is not <code>null</code>
     * @return this
     */
    public RowBuilder columnTwo(Component aLabel, Component aComponent) {
      fSecondColumnComponents = new Component[]{aLabel, aComponent};
      checkConstraints();
      return this;
    }

    /**
     * Add a component which spans the whole width of the second column
     * @param aComponent The component. Must not be <code>null</code>
     * @return this
     */
    public RowBuilder columnTwo(Component aComponent) {
      fSecondColumnComponents = new Component[]{aComponent};
      checkConstraints();
      return this;
    }

    /**
     * <p>Add a component which spans the whole width of the two columns.</p>
     *
     * <p>In case <code>aComponent</code> is a container where the child components were added using
     * the <code>TwoColumnLayoutBuilder</code>, the layout of <code>aComponent</code> and the
     * layout of the panel for which this builder is used will be aligned.</p>
     *
     * @param aComponent The component
     *
     * @return this
     */
    public RowBuilder spanBothColumns(Component aComponent) {
      if (aComponent == null) {
        throw new NullPointerException("It is not possible to add a null Component to span the whole width");
      }
      fSpanComponent = aComponent;
      checkConstraints();
      return this;
    }

    /**
     * Specify whether the row should grow vertically or not. The default value is <code>false</code>
     * @param aGrowVertically <code>true</code> when the row should grow vertically, <code>false</code> otherwise
     * @return this
     */
    public RowBuilder growVertically(boolean aGrowVertically) {
      fGrowVertically = aGrowVertically;
      checkConstraints();
      return this;
    }

    private void checkConstraints() {
      if (fSpanComponent != null) {
        if (!(fFirstColumnComponents == null)) {
          throw new IllegalArgumentException("It is not possible to have a component span the whole width, and having components in the first column at the same time.");
        }
        if (!(fSecondColumnComponents == null)) {
          throw new IllegalArgumentException("It is not possible to have a component span the whole width, and having components in the second column at the same time.");
        }
      }
      checkComponentArray(fFirstColumnComponents);
      checkComponentArray(fSecondColumnComponents);

    }

    private void checkComponentArray(Component[] aComponents) {
      if (aComponents != null) {
        if (aComponents.length == 1) {
          if (aComponents[0] == null) {
            throw new NullPointerException("A component spanning the whole width of a column cannot be null.");
          }
        } else if (aComponents.length == 2) {
          if (aComponents[0] == null && aComponents[1] == null) {
            throw new IllegalArgumentException("It is not possible to have a label and component which are both null");
          }
        }
      }
    }

    /**
     * Call this method when all components for the row are specified. This will return the outer
     * builder instance
     * @return the outer builder instance for which this row builder is used.
     */
    public TwoColumnLayoutBuilder build() {
      checkConstraints();
      if (fSpanComponent != null) {
        Component componentToAdd = findRelevantChildComponent(fSpanComponent);
        if (componentToAdd instanceof Container &&
            ((Container) componentToAdd).getLayout() instanceof TwoColumnLayoutManager) {
          ((TwoColumnLayoutManager) ((Container) componentToAdd).getLayout()).addComponentsToOtherBuilder(fBuilder);
        } else {
          fBuilder.addRow(fSpanComponent, fGrowVertically);
        }
      } else {
        if (fFirstColumnComponents == null) {
          fFirstColumnComponents = new Component[]{null, null};
        }
        if (fSecondColumnComponents == null) {
          fSecondColumnComponents = new Component[]{null, null};
        }
        fBuilder.addRow(fFirstColumnComponents, fSecondColumnComponents, fGrowVertically);
      }
      return fBuilder;
    }

    /**
     * Somethings a component is wrapped with another component, which does not affect the layout (for
     * example when added to the center of a BorderLayout, without any other components added to that
     * BorderLayout). This method unwraps components in such case and returns the first with a TwoColumnLayoutManager,
     * or the component itself when no such component can be found
     */
    @SuppressWarnings("JavaDoc")
    private Component findRelevantChildComponent(Component aComponent) {
      if (aComponent instanceof JComponent) {
        LayoutManager layout = ((JComponent) aComponent).getLayout();
        if (layout instanceof TwoColumnLayoutManager) {
          return aComponent;
        }
        //the panel might be wrapped with a wrapper that does not affect the layout
        else if (((Container) aComponent).getComponentCount() == 1) {
          Rectangle parentBounds = new Rectangle(0, 0, 150, 150);
          aComponent.setBounds(parentBounds);
          if (layout != null) {
            layout.layoutContainer((Container) aComponent);
          }
          Component childComponent = ((Container) aComponent).getComponent(0);
          Rectangle bounds = childComponent.getBounds();
          if (parentBounds.equals(bounds)) {
            return findRelevantChildComponent(childComponent);
          }
        }
      }
      return aComponent;
    }
  }
}

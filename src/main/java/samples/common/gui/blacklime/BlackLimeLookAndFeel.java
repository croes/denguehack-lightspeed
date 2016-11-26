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
package samples.common.gui.blacklime;

import static samples.common.gui.blacklime.ColorPalette.*;

import java.awt.*;
import java.awt.font.TextAttribute;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.swing.*;
import javax.swing.plaf.BorderUIResource;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.DimensionUIResource;
import javax.swing.plaf.InsetsUIResource;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;
import javax.swing.plaf.synth.Region;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;

import com.luciad.gui.TLcdIconFactory;
import com.luciad.gui.TLcdResizeableIcon;
import com.luciad.gui.TLcdSymbol;
import com.luciad.gui.swing.TLcdSWIcon;
import com.luciad.io.TLcdInputStreamFactory;
import com.luciad.util.TLcdSystemPropertiesUtil;
import com.luciad.util.logging.ILcdLogger;
import com.luciad.util.logging.TLcdLoggerFactory;

/**
 * <p>Customization of the Nimbus look & feel that uses flat, dark backgrounds, and lime green as an accent color.</p>
 *
 * <p>To enable it, call the {@link #install()} method. It does not support switching look and feels at runtime.</p>
 *
 * <p>It uses a different font, changes background rendering of buttons, combo boxes, scroll bars etc. It is not so
 * sophisticated as the unmodified Nimbus look & feel. It is for example not fully tuned for differences between
 * mouse-over, pressing and focus ... It doesn't support internal frames.</p>
 *
 * <p>To change the color scheme, see {@link ColorPalette}.</p>
 *
 * <p>The Lucy map centric front-end uses this look and feel by default, so as the Lightspeed demo and some samples.
 * To enable it in combination with the Lucy docking front-end (JIDE-based), care must be taken to install it
 * very early in the initialization process (see also {@code DockableHolderFactory}), for example like so:</p>
 * <pre>
 *  public static void main(String[] args) {
 *    TLcdAWTUtil.invokeAndWait(new Runnable() {
 *      public void run() {
 *        BlackLimeLookAndFeel.install();
 *      }
 *    });
 *    TLcyMain.main(args);
 *  }
 * </pre>
 */
public class BlackLimeLookAndFeel extends MacNimbusLookAndFeel {
  /**
   * The name of this look and feel, see also {@link #getName()}.
   */
  public static final String NAME = "BlackLime";

  private static final ILcdLogger LOGGER = TLcdLoggerFactory.getLogger(BlackLimeLookAndFeel.class);

  /**
   * JTextField's with this name are styled differently.
   */
  private static final String EMPHASIZED_TEXT_FIELD_NAME = "emphasizedTextField";

  // Relevant 'contentMargins' as found on:
  //   http://docs.oracle.com/javase/tutorial/uiswing/lookandfeel/_nimbusDefaults.html
  private static final String[] TOUCH_CONTENT_MARGINS = new String[]{
      "ArrowButton.contentMargins",
      "Button.contentMargins",
      "CheckBox.contentMargins",
      "CheckBoxMenuItem.contentMargins",
      "CheckBoxMenuItem:MenuItemAccelerator.contentMargins",
      "ComboBox:\"ComboBox.listRenderer\".contentMargins",
      "ComboBox:\"ComboBox.renderer\".contentMargins",
      "ComboBox:\"ComboBox.textField\".contentMargins",
      "EditorPane.contentMargins",
      "FormattedTextField.contentMargins",
      "InternalFrame:InternalFrameTitlePane:\"InternalFrameTitlePane.closeButton\".contentMargins",
      "InternalFrame:InternalFrameTitlePane:\"InternalFrameTitlePane.iconifyButton\".contentMargins",
      "InternalFrame:InternalFrameTitlePane:\"InternalFrameTitlePane.maximizeButton\".contentMargins",
      "InternalFrame:InternalFrameTitlePane:\"InternalFrameTitlePane.menuButton\".contentMargins",
      "List:\"List.cellRenderer\".contentMargins",
      "Menu.contentMargins",
      "MenuBar.contentMargins",
      "MenuBar:Menu.contentMargins",
      "MenuItem.contentMargins",
      "PasswordField.contentMargins",
      "RadioButton.contentMargins",
      "RadioButtonMenuItem.contentMargins",
      "ScrollBar:\"ScrollBar.button\".contentMargins",
      "ScrollBar:ScrollBarThumb.contentMargins",
      "ScrollBar:ScrollBarTrack.contentMargins",
      "Slider.contentMargins",
      "Slider:SliderThumb.contentMargins",
      "Slider:SliderTrack.contentMargins",
      "Spinner:\"Spinner.editor\".contentMargins",
      "Spinner:\"Spinner.nextButton\".contentMargins",
      "Spinner:\"Spinner.previousButton\".contentMargins",
      "Spinner:Panel:\"Spinner.formattedTextField\".contentMargins",
      "SplitPane:SplitPaneDivider.contentMargins",
      "TabbedPane:TabbedPaneTab.contentMargins",
      "Table:\"Table.cellRenderer\".contentMargins",
      "\"Table.editor\".contentMargins",
      "TableHeader.contentMargins",
      "TableHeader:\"TableHeader.renderer\".contentMargins",
      "TextArea.contentMargins",
      "TextField.contentMargins",
      "TextPane.contentMargins",
      "ToggleButton.contentMargins",
      "ToolBar:Button.contentMargins",
      "ToolBar:ToggleButton.contentMargins",
      "Tree:\"Tree.cellRenderer\".contentMargins",
      "\"Tree.cellEditor\".contentMargins",
      "Tree:TreeCell.contentMargins"
  };

  private static double sEnlargeFactor = 1;

  private final boolean fTouch;

  /**
   * Equivalent to {@link #install(boolean)} without touch support.
   */
  public static void install() {
    install(false);
  }

  /**
   * After calling this method, the Swing UI uses this look and feel. Must be called from the Event Dispatch Thread
   * (EDT).
   * @param aTouch {@code true} to enable touch support, which increases various margins to accommodate for the larger
   *               pointing area of fingers. The default is {@code false}.
   */
  public static void install(boolean aTouch) {
    if (!EventQueue.isDispatchThread()) {
      throw new IllegalStateException("Must be called from the Event Dispatch Thread, not " + Thread.currentThread());
    }
    try {
      UIManager.setLookAndFeel(new BlackLimeLookAndFeel(aTouch));

      // Change the look and feel of all existing windows
      for (Window w : Window.getWindows()) {
        SwingUtilities.updateComponentTreeUI(w);
      }

    } catch (UnsupportedLookAndFeelException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Returns the name of this look and feel: "{@value NAME}".
   * @return "{@value NAME}"
   */
  @Override
  public String getName() {
    return NAME;
  }

  /**
   * Returns true if this look and feel is currently in use.
   * @return true if this look and feel is currently in use.
   */
  public static boolean isInstalled() {
    // Do not check the class name as this class exists both in sample code and inside the product .jar
    return NAME.equals(UIManager.getLookAndFeel().getName());
  }

  public static double getEnlargeFactor() {
    return sEnlargeFactor;
  }

  /**
   * Allows to scale up font and other sizes using the given factor. A factor of 1 is the default. Sensible values
   * are approximately between 0.5 and 2. It allows to scale up a UI, for example for purposes of using it on a
   * projector or a touch device. Note that the dpi setting of the system is already taken into account, regardless
   * this factor.
   * @param aEnlargeFactor The factor to enlarge font and other sizes with.
   */
  public static void setEnlargeFactor(double aEnlargeFactor) {
    sEnlargeFactor = aEnlargeFactor;
  }

  /**
   * Converts the given pixel size at 96 dpi to pixels for the current screen resolution.
   * @param aPxAt96dpi The pixel size at 96 dpi.
   * @return The size in pixels for the current screen resolution.
   */
  private static int px(double aPxAt96dpi) {
    //On Mac, the JDK already contains workarounds to ensure that fonts are renderer at a decent size on retina screens
    //There is no need to apply our own DPI correction factor
    int dpi = !TLcdSystemPropertiesUtil.isMacOS() ? Toolkit.getDefaultToolkit().getScreenResolution() : 96;
    //Do not consider dpi's lower than 96 (for example when using a large TV screen),
    //as those screens are typically viewed from a distance -> no need to make the font size smaller in such cases
    dpi = Math.max(dpi, 96);
    return (int) Math.round((aPxAt96dpi / 96d * dpi) * sEnlargeFactor);
  }

  private BlackLimeLookAndFeel(boolean aTouch) {
    fTouch = aTouch;
  }

  @Override
  public void initialize() {
    super.initialize();

    //See NimbusDefaults class for a list of relevant property keys
    UIDefaults d = getDefaults();

    hideDialogDecorations(d);
    installDarkTheme(d);
    installFonts(d);
    installButtonDefaults(d);
    installToggleButtonDefaults(d);
    installCheckBoxDefaults(d);
    installRadioButtonDefaults(d);
    installToolbarDefaults(d);
    installComboBoxDefaults(d);
    installScrollBarDefaults(d, fTouch);
    installSpinnerDefaults(d);
    installSplitPaneDefaults(d);
    installToolTipDefaults(d);
    installTabbedPaneDefaults(d);
    installTableDefaults(d);
    installTreeDefaults(d);
    installMenuDefaults(d);
    installSeparatorDefaults(d);
    installSliderDefaults(d);
    installTextFieldDefaults(d);
    installTextAreaDefaults(d);
    installProgressBarDefaults(d);
    installHyperlinkDefaults(d);
    installEditorPaneDefaults();

    workAroundBug_JDK_8057791(this);

    if (fTouch) {
      installTouchDefaults(d);
    }
  }

  public static void workAroundBug_JDK_8057791(NimbusLookAndFeel aLookAndFeel) {
    UIDefaults d = aLookAndFeel.getDefaults();

    // If true, background is not painted on Linux (JDK 1.7 and 1.8), so selected items cannot be distinguished from
    // normal ones. Also, text might become unreadable.
    d.put("List.rendererUseListColors", false);
    d.put("ComboBox.rendererUseListColors", false);

    // Workaround for wrong colors in JList and the popup of JComboBox. It occurs on Linux with Java 1.7 and 1.8,
    // on Windows only for Java 1.8.0_20.
    // See the below links. The 'false' parameter is vital.
    //   http://stackoverflow.com/questions/25685269/wrong-colors-in-jlist-when-using-nimbus-and-java-8u20/
    //   https://bugs.openjdk.java.net/browse/JDK-8057791
    d.put("List[Selected].textForeground",
          aLookAndFeel.getDerivedColor("nimbusSelectedText", 0.0f, 0.0f, 0.0f, 0, false));
    d.put("List[Selected].textBackground",
          aLookAndFeel.getDerivedColor("nimbusSelectionBackground", 0.0f, 0.0f, 0.0f, 0, false));
    d.put("List[Disabled+Selected].textBackground",
          aLookAndFeel.getDerivedColor("nimbusSelectionBackground", 0.0f, 0.0f, 0.0f, 0, false));
    d.put("List[Disabled].textForeground",
          aLookAndFeel.getDerivedColor("nimbusDisabledText", 0.0f, 0.0f, 0.0f, 0, false));
    d.put("List:\"List.cellRenderer\"[Disabled].background",
          aLookAndFeel.getDerivedColor("nimbusSelectionBackground", 0.0f, 0.0f, 0.0f, 0, false));
  }

  public static void installTouchDefaults(UIDefaults aDefaultsSFCT) {
    // Increase all content margins
    Insets margins = new Insets(px(8), px(8), px(8), px(8));
    for (String componentKey : TOUCH_CONTENT_MARGINS) {
      aDefaultsSFCT.put(componentKey, margins);
    }

    aDefaultsSFCT.put("Slider.thumbWidth", px(23));
    aDefaultsSFCT.put("Slider.thumbHeight", px(23));

    aDefaultsSFCT.put("ColorChooser.swatchesSwatchSize", new Dimension(px(23), px(23)));
    aDefaultsSFCT.put("ColorChooser.swatchesRecentSwatchSize", new Dimension(px(23), px(23)));
  }

  public static void fixTreeIcon(UIDefaults aDefaultsSFCT) {
    // JTree's expanded/collapsed icon's don't adapt well to the above colors, making it nearly
    // invisible. Overrule with a custom icon with a different color.
    aDefaultsSFCT.put("Tree.expandedIcon", new TreeIcon(false));
    aDefaultsSFCT.put("Tree.collapsedIcon", new TreeIcon(true));
  }

  @Override
  public boolean getSupportsWindowDecorations() {
    //needed for hideDialogDecorations
    return true;
  }

  private static void hideDialogDecorations(UIDefaults d) {
    JDialog.setDefaultLookAndFeelDecorated(true);
    d.put("RootPaneUI", BlackLimeMetalRootPaneUI.class.getName());

    // Tune the metal look&feel, it is only used for the root panes
    ColorUIResource bg = darkestGrey;
    ColorUIResource fg = text;
    d.put("activeCaption", bg);
    d.put("inactiveCaption", bg);
    d.put("activeCaptionText", fg);
    d.put("activeCaptionBorder", bg);
    d.put("inactiveCaptionText", disabledText);
    d.put("OptionPane.errorDialog.titlePane.background", bg);
    d.put("OptionPane.errorDialog.titlePane.foreground", fg);
    d.put("OptionPane.errorDialog.titlePane.shadow", bg);
    d.put("OptionPane.questionDialog.titlePane.background", bg);
    d.put("OptionPane.questionDialog.titlePane.foreground", fg);
    d.put("OptionPane.questionDialog.titlePane.shadow", bg);
    d.put("OptionPane.warningDialog.titlePane.background", bg);
    d.put("OptionPane.warningDialog.titlePane.foreground", fg);
    d.put("OptionPane.warningDialog.titlePane.shadow", bg);

    BorderUIResource rootPaneBorder = new BorderUIResource(BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(ColorPalette.dialogBorder), BorderFactory.createEmptyBorder(2, 2, 2, 2)));
    d.put("RootPane.frameBorder", rootPaneBorder);
    d.put("RootPane.plainDialogBorder", rootPaneBorder);
    d.put("RootPane.informationDialogBorder", rootPaneBorder);
    d.put("RootPane.errorDialogBorder", rootPaneBorder);
    d.put("RootPane.colorChooserDialogBorder", rootPaneBorder);
    d.put("RootPane.fileChooserDialogBorder", rootPaneBorder);
    d.put("RootPane.questionDialogBorder", rootPaneBorder);
    d.put("RootPane.warningDialogBorder", rootPaneBorder);

    TLcdSWIcon closeIcon = new TLcdSWIcon(new TLcdResizeableIcon(new TLcdSymbol(TLcdSymbol.CROSS, px(8), text), px(16), px(16)));
    d.put("InternalFrame.closeIcon", closeIcon);
  }

  private static void installDarkTheme(UIDefaults aDefaultsSFCT) {
    // Use the white icons, they look much better with the dark backgrounds
    TLcdIconFactory.setDefaultTheme(TLcdIconFactory.Theme.WHITE_THEME);

    aDefaultsSFCT.put("control", darkGrey);
    aDefaultsSFCT.put("info", darkGrey);
    aDefaultsSFCT.put("nimbusBase", darkGrey);
    aDefaultsSFCT.put("nimbusAlertYellow", lime);
    aDefaultsSFCT.put("nimbusDisabledText", disabledText);
    aDefaultsSFCT.put("nimbusFocus", blueGrey);
    aDefaultsSFCT.put("nimbusInfoBlue", blue);
    aDefaultsSFCT.put("nimbusLightBackground", darkestGrey);
    aDefaultsSFCT.put("nimbusOrange", lime);
    aDefaultsSFCT.put("nimbusRed", red); //unsure where these are used
    aDefaultsSFCT.put("nimbusSelectedText", text);
    aDefaultsSFCT.put("nimbusSelectionBackground", blueGrey);
    aDefaultsSFCT.put("DatePickerUI.background", darkGrey);
    aDefaultsSFCT.put("text", text);

    aDefaultsSFCT.put("nimbusGreen", new ColorUIResource(176, 179, 50)); //only used by internal frames, not tested.

    // Secondary color derived from nimbusBase. Darken it, otherwise disabled text fields etc. are not readable. Also,
    // use a neutral grey color as otherwise certain colors with a different hue are derived.
    aDefaultsSFCT.put("nimbusBlueGrey", neutralGrey);

    // Another derived color update
    aDefaultsSFCT.put("nimbusSelection", text);

    // Lucy specific colors
    aDefaultsSFCT.put("com.luciad.lucy.lookandfeel.validation.errorBackgroundColor", new Color(red.getRed(), red.getGreen(), red.getBlue())); //see Painters.Background
    aDefaultsSFCT.put("OnMapPanel.backgroundGradientStart", darkestGrey);
    aDefaultsSFCT.put("OnMapPanel.backgroundGradientEnd", darkestGrey);
    aDefaultsSFCT.put("OnMapPanel.borderThickness", 0);
    aDefaultsSFCT.put("MapCentric.dividerSize", 0); //invisible
  }

  @Override
  public Color getDerivedColor(String uiDefaultParentName, float hOffset, float sOffset, float bOffset, int aOffset, boolean uiResource) {
    ColorMapping.Descriptor descriptor = new ColorMapping.Descriptor(uiDefaultParentName, hOffset, sOffset, bOffset, aOffset);
    ColorMapping.Descriptor newColor = ColorMapping.MAPPING.get(descriptor);
    if (newColor != null) {
      return super.getDerivedColor(newColor.fParentColorName, newColor.fHOffset, newColor.fSOffset, newColor.fBOffset, newColor.fOffset, uiResource);
    } else {
      return super.getDerivedColor(uiDefaultParentName, hOffset, sOffset, bOffset, aOffset, uiResource);
    }
  }


  private static void installFonts(UIDefaults d) {
    Font font = new Font(Font.SANS_SERIF, Font.PLAIN, px(13));
    Font emphasizedFont = deriveFont(font, px(15), true);

    // Use the Noto font if possible. The Noto font doesn't have all chars such as Chinese, or the Mac command char.
    // Noto does have a full version with all those chars, but that is over 100MB, too large to use here.
    if (isLatin() && !TLcdSystemPropertiesUtil.isMacOS()) {
      try {
        font = loadFont("samples/common/gui/blacklime/NotoSans-Regular.ttf", px(13), false);
        emphasizedFont = loadFont("samples/common/gui/blacklime/NotoSans-UpperCase.ttf", px(13), true);
      } catch (FontFormatException | IOException e) {
        //ignore, stick to default fonts
      }
    }

    d.put("defaultFont", font);
    d.put("Button.font", emphasizedFont);
    d.put("ToggleButton.font", emphasizedFont);
    d.put("InternalFrame.titleFont", emphasizedFont); //used for dialog titles
    d.put("OnMapPanel.font", emphasizedFont); // Lucy specific
  }

  private static boolean isLatin() {
    // Using an explicit limited list of supported languages because Locale.getScript may return an empty string
    java.util.List<String> langs = Arrays.asList("en", "fr", "de", "nl", "es", "pt", "it", "da", "sv", "no", "fi");
    return Locale.getDefault().getScript().equals("Latn") || langs.contains(Locale.getDefault().getLanguage());
  }

  private static Font loadFont(String aFontFile, float aSize, boolean aLoose) throws IOException, FontFormatException {
    try (InputStream in = new TLcdInputStreamFactory().createInputStream(aFontFile)) {
      return deriveFont(Font.createFont(Font.TRUETYPE_FONT, in), aSize, aLoose);
    } catch (FontFormatException | IOException e) {
      LOGGER.warn("Could not load font " + aFontFile + ", falling back to default font.", e);
      throw e;
    }
  }

  private static Font deriveFont(Font aFont, float aSize, boolean aLoose) {
    Map<TextAttribute, Object> attribs = new HashMap<>();
    attribs.put(TextAttribute.SIZE, aSize);
    if (aLoose) {
      attribs.put(TextAttribute.TRACKING, .07f);
    }
    return aFont.deriveFont(attribs);
  }

  private static void installButtonDefaults(UIDefaults d) {
    Painter limeFill = new Painters.Background(lime);
    Painter limeFillEmph = new Painters.Background(limeEmph);
    Painter border = new Painters.Border(buttonBorder, 1);
    Painter fillEmph = new Painters.Composite(new Painters.Background(blueGreySubtle), border);

    d.put("Button[Default+Enabled].textForeground", textOnLimeBackground);
    d.put("Button[Enabled].textForeground", text);

    d.put("Button[Default+Disabled].backgroundPainter", border);
    d.put("Button[Default+Disabled+Focused].backgroundPainter", border);
    d.put("Button[Default+Enabled].backgroundPainter", limeFill);
    d.put("Button[Default+Enabled+Focused].backgroundPainter", limeFill);
    d.put("Button[Default+MouseOver].backgroundPainter", limeFillEmph);
    d.put("Button[Default+Focused+MouseOver].backgroundPainter", limeFillEmph);
    d.put("Button[Default+Pressed].backgroundPainter", limeFillEmph);
    d.put("Button[Default+Focused+Pressed].backgroundPainter", limeFillEmph);

    d.put("Button[Disabled].backgroundPainter", border);
    d.put("Button[Enabled].backgroundPainter", border);
    d.put("Button[Focused].backgroundPainter", border);
    d.put("Button[MouseOver].backgroundPainter", fillEmph);
    d.put("Button[Focused+MouseOver].backgroundPainter", fillEmph);
    d.put("Button[Pressed].backgroundPainter", fillEmph);
    d.put("Button[Focused+Pressed].backgroundPainter", fillEmph);
  }

  private static void installToggleButtonDefaults(UIDefaults d) {
    Painter border = new Painters.Border(buttonBorder, 1);
    Painter fillEmph = new Painters.Composite(new Painters.Background(blueGreySubtle), border);
    Painter selected = new Painters.Background(blueGrey);
    Painter selectedEmph = new Painters.Background(blueGreyEmph);

    d.put("ToggleButton[Disabled].backgroundPainter", null);
    d.put("ToggleButton[Enabled].backgroundPainter", border);
    d.put("ToggleButton[Focused].backgroundPainter", border);
    d.put("ToggleButton[MouseOver].backgroundPainter", fillEmph);
    d.put("ToggleButton[Focused+MouseOver].backgroundPainter", fillEmph);
    d.put("ToggleButton[Pressed].backgroundPainter", fillEmph);
    d.put("ToggleButton[Focused+Pressed].backgroundPainter", fillEmph);
    d.put("ToggleButton[Selected].backgroundPainter", selected);
    d.put("ToggleButton[Focused+Selected].backgroundPainter", selected);
    d.put("ToggleButton[Pressed+Selected].backgroundPainter", selectedEmph);
    d.put("ToggleButton[Focused+Pressed+Selected].backgroundPainter", selectedEmph);
    d.put("ToggleButton[MouseOver+Selected].backgroundPainter", selectedEmph);
    d.put("ToggleButton[Focused+MouseOver+Selected].backgroundPainter", selectedEmph);
    d.put("ToggleButton[Disabled+Selected].backgroundPainter", selected);
  }

  private static void installCheckBoxDefaults(UIDefaults d) {
    Painter fill = new Painters.Background(checkBoxBg, 2);
    Painter fillEmph = new Painters.Background(checkBoxBgEmph, 2);
    Painter disabledFill = fill;
    Painter tick = new Painters.CheckBoxTick(text);
    Painter disabledTick = new Painters.CheckBoxTick(disabledText);

    Painter selected = new Painters.Composite(fill, tick);
    Painter selectedEmph = new Painters.Composite(fillEmph, tick);
    Painter disabledSelected = new Painters.Composite(disabledFill, disabledTick);

    d.put("CheckBox[Disabled+Selected].iconPainter", disabledSelected);
    d.put("CheckBox[Disabled].iconPainter", disabledFill);
    d.put("CheckBox[Enabled].iconPainter", fill);
    d.put("CheckBox[Focused+MouseOver+Selected].iconPainter", selectedEmph);
    d.put("CheckBox[Focused+MouseOver].iconPainter", fillEmph);
    d.put("CheckBox[Focused+Pressed+Selected].iconPainter", selectedEmph);
    d.put("CheckBox[Focused+Pressed].iconPainter", fillEmph);
    d.put("CheckBox[Focused+Selected].iconPainter", selected);
    d.put("CheckBox[Focused].iconPainter", fill);
    d.put("CheckBox[MouseOver+Selected].iconPainter", selectedEmph);
    d.put("CheckBox[MouseOver].iconPainter", fillEmph);
    d.put("CheckBox[Pressed+Selected].iconPainter", selectedEmph);
    d.put("CheckBox[Pressed].iconPainter", fillEmph);
    d.put("CheckBox[Selected].iconPainter", selected);
  }

  private static void installRadioButtonDefaults(UIDefaults d) {
    Painter fill = new Painters.Circle(checkBoxBg, 15);
    Painter fillEmph = new Painters.Circle(checkBoxBgEmph, 15);
    Painter disabledFill = fill;
    int selectedSize = 7;
    Painter tick = new Painters.Circle(text, selectedSize);
    Painter disabledTick = new Painters.Circle(disabledText, selectedSize);

    Painter selected = new Painters.Composite(fill, tick);
    Painter selectedEmph = new Painters.Composite(fillEmph, tick);
    Painter disabledSelected = new Painters.Composite(disabledFill, disabledTick);

    d.put("RadioButton[Disabled+Selected].iconPainter", disabledSelected);
    d.put("RadioButton[Disabled].iconPainter", disabledFill);
    d.put("RadioButton[Enabled].iconPainter", fill);
    d.put("RadioButton[Focused+MouseOver+Selected].iconPainter", selectedEmph);
    d.put("RadioButton[Focused+MouseOver].iconPainter", fillEmph);
    d.put("RadioButton[Focused+Pressed+Selected].iconPainter", selectedEmph);
    d.put("RadioButton[Focused+Pressed].iconPainter", fillEmph);
    d.put("RadioButton[Focused+Selected].iconPainter", selected);
    d.put("RadioButton[Focused].iconPainter", fill);
    d.put("RadioButton[MouseOver+Selected].iconPainter", selectedEmph);
    d.put("RadioButton[MouseOver].iconPainter", fillEmph);
    d.put("RadioButton[Pressed+Selected].iconPainter", selectedEmph);
    d.put("RadioButton[Pressed].iconPainter", fillEmph);
    d.put("RadioButton[Selected].iconPainter", selected);
  }

  private static void installToolbarDefaults(UIDefaults d) {
    Painter fillEmph = new Painters.Background(blueGreySubtle);
    Painter selected = new Painters.Background(blueGrey);
    Painter selectedEmph = new Painters.Background(blueGreyEmph);

    d.put("ToolBar[North].borderPainter", null);
    d.put("ToolBar[South].borderPainter", null);
    d.put("ToolBar[East].borderPainter", null);
    d.put("ToolBar[West].borderPainter", null);
    d.put("ToolBar:Button.contentMargins", new InsetsUIResource(4, 4, 4, 4));
    d.put("ToolBar:Button[Focused].backgroundPainter", null);
    d.put("ToolBar:Button[MouseOver].backgroundPainter", fillEmph);
    d.put("ToolBar:Button[Focused+MouseOver].backgroundPainter", fillEmph);
    d.put("ToolBar:Button[Pressed].backgroundPainter", fillEmph);
    d.put("ToolBar:Button[Focused+Pressed].backgroundPainter", fillEmph);
    d.put("ToolBar:ToggleButton[Focused].backgroundPainter", null);
    d.put("ToolBar:ToggleButton[MouseOver].backgroundPainter", fillEmph);
    d.put("ToolBar:ToggleButton[Focused+MouseOver].backgroundPainter", fillEmph);
    d.put("ToolBar:ToggleButton[Pressed].backgroundPainter", fillEmph);
    d.put("ToolBar:ToggleButton[Focused+Pressed].backgroundPainter", fillEmph);
    d.put("ToolBar:ToggleButton[Selected].backgroundPainter", selected);
    d.put("ToolBar:ToggleButton[Focused+Selected].backgroundPainter", selected);
    d.put("ToolBar:ToggleButton[Pressed+Selected].backgroundPainter", selectedEmph);
    d.put("ToolBar:ToggleButton[Focused+Pressed+Selected].backgroundPainter", selectedEmph);
    d.put("ToolBar:ToggleButton[MouseOver+Selected].backgroundPainter", selectedEmph);
    d.put("ToolBar:ToggleButton[Focused+MouseOver+Selected].backgroundPainter", selectedEmph);
    d.put("ToolBar:ToggleButton[Disabled+Selected].backgroundPainter", selected);

    d.put("ToolBar:Button[Disabled].textForeground", disabledText);
    d.put("ToolBar:ToggleButton[Disabled].textForeground", disabledText);
  }

  private static void installComboBoxDefaults(UIDefaults d) {
    Painter fill = new Painters.Background(ColorPalette.darkestGrey);
    Painter limeBorder = new Painters.Border(ColorPalette.lime, 1);
    Painter disabledTriangle = new Painters.Triangle(ColorPalette.disabledText, SwingConstants.WEST);
    Painter triangle = new Painters.Triangle(ColorPalette.text, SwingConstants.WEST);
    Painter limeTriangle = new Painters.Triangle(ColorPalette.lime, SwingConstants.WEST);

    d.put("ComboBox[Disabled].backgroundPainter", fill);
    d.put("ComboBox[Disabled+Pressed].backgroundPainter", fill);
    d.put("ComboBox[Enabled].backgroundPainter", fill);
    d.put("ComboBox[Focused].backgroundPainter", fill);
    d.put("ComboBox[Focused+MouseOver].backgroundPainter", fill);
    d.put("ComboBox[MouseOver].backgroundPainter", fill);
    d.put("ComboBox[Focused+Pressed].backgroundPainter", fill);
    d.put("ComboBox[Pressed].backgroundPainter", fill);
    d.put("ComboBox[Enabled+Selected].backgroundPainter", fill);
    d.put("ComboBox[Disabled+Editable].backgroundPainter", fill);
    d.put("ComboBox[Editable+Enabled].backgroundPainter", fill);
    d.put("ComboBox[Editable+Focused].backgroundPainter", null); // let the text field paint the border, see below
    d.put("ComboBox[Editable+MouseOver].backgroundPainter", fill);
    d.put("ComboBox[Editable+Pressed].backgroundPainter", fill);
    d.put("ComboBox:\"ComboBox.arrowButton\"[Disabled+Editable].backgroundPainter", fill);
    d.put("ComboBox:\"ComboBox.arrowButton\"[Editable+Enabled].backgroundPainter", fill);
    d.put("ComboBox:\"ComboBox.arrowButton\"[Editable+MouseOver].backgroundPainter", fill);
    d.put("ComboBox:\"ComboBox.arrowButton\"[Editable+Pressed].backgroundPainter", fill);
    d.put("ComboBox:\"ComboBox.arrowButton\"[Editable+Selected].backgroundPainter", fill);
    d.put("ComboBox:\"ComboBox.arrowButton\"[Enabled].foregroundPainter", triangle);
    d.put("ComboBox:\"ComboBox.arrowButton\"[MouseOver].foregroundPainter", limeTriangle);
    d.put("ComboBox:\"ComboBox.arrowButton\"[Disabled].foregroundPainter", disabledTriangle);
    d.put("ComboBox:\"ComboBox.arrowButton\"[Pressed].foregroundPainter", limeTriangle);
    d.put("ComboBox:\"ComboBox.arrowButton\"[Selected].foregroundPainter", triangle);

    d.put("ComboBox:\"ComboBox.textField\"[Disabled].backgroundPainter", fill);
    d.put("ComboBox:\"ComboBox.textField\"[Enabled].backgroundPainter", fill);
    d.put("ComboBox:\"ComboBox.textField\"[Focused].backgroundPainter", limeBorder);

    d.put("ComboBox.popupInsets", new Insets(-1, 1, 0, 1));
  }

  private static void installScrollBarDefaults(UIDefaults d, boolean aTouch) {
    Painters.Background track = new Painters.Background(darkestGrey, 0);
    Painters.Background thumb = new Painters.Background(blueGrey, 0);
    Painters.Background thumbEmph = new Painters.Background(lightGrey, 0);

    if (!aTouch) {
      d.put("ScrollBar.thumbHeight", px(10));
      d.put("ScrollBar.minimumThumbSize", new Dimension(px(10), px(10)));
      d.put("ScrollBar:\"ScrollBar.button\".size", px(8));
    }
    d.put("ScrollBar:\"ScrollBar.button\"[Enabled].foregroundPainter", null);
    d.put("ScrollBar:\"ScrollBar.button\"[Disabled].foregroundPainter", null);
    d.put("ScrollBar:\"ScrollBar.button\"[MouseOver].foregroundPainter", null);
    d.put("ScrollBar:\"ScrollBar.button\"[Pressed].foregroundPainter", null);
    d.put("ScrollBar:ScrollBarThumb[Enabled].backgroundPainter", thumb);
    d.put("ScrollBar:ScrollBarThumb[MouseOver].backgroundPainter", thumbEmph);
    d.put("ScrollBar:ScrollBarThumb[Pressed].backgroundPainter", thumbEmph);
    d.put("ScrollBar:ScrollBarTrack[Disabled].backgroundPainter", track);
    d.put("ScrollBar:ScrollBarTrack[Enabled].backgroundPainter", track);

    d.put("ScrollPane[Enabled].borderPainter", null);
    d.put("ScrollPane[Enabled+Focused].borderPainter", null);
    d.put("ScrollPane[Disabled].borderPainter", null);
  }

  private static void installSpinnerDefaults(UIDefaults d) {
    Painter border = new Painters.Background(darkestGrey);
    Painter limeBorder = new Painters.Border(lime, 1);

    d.put("Spinner:\"Spinner.previousButton\"[Disabled].backgroundPainter", null);
    d.put("Spinner:\"Spinner.previousButton\"[Enabled].backgroundPainter", border);
    d.put("Spinner:\"Spinner.previousButton\"[Focused].backgroundPainter", border);
    d.put("Spinner:\"Spinner.previousButton\"[Focused+MouseOver].backgroundPainter", border);
    d.put("Spinner:\"Spinner.previousButton\"[Focused+Pressed].backgroundPainter", border);
    d.put("Spinner:\"Spinner.previousButton\"[MouseOver].backgroundPainter", border);
    d.put("Spinner:\"Spinner.previousButton\"[Pressed].backgroundPainter", border);

    d.put("Spinner:\"Spinner.nextButton\"[Disabled].backgroundPainter", null);
    d.put("Spinner:\"Spinner.nextButton\"[Enabled].backgroundPainter", border);
    d.put("Spinner:\"Spinner.nextButton\"[Focused].backgroundPainter", border);
    d.put("Spinner:\"Spinner.nextButton\"[Focused+MouseOver].backgroundPainter", border);
    d.put("Spinner:\"Spinner.nextButton\"[Focused+Pressed].backgroundPainter", border);
    d.put("Spinner:\"Spinner.nextButton\"[MouseOver].backgroundPainter", border);
    d.put("Spinner:\"Spinner.nextButton\"[Pressed].backgroundPainter", border);

    Painters.Triangle triangleUp = new Painters.Triangle(text, SwingConstants.SOUTH, 1);
    Painters.Triangle disabledTriangleUp = new Painters.Triangle(disabledText, SwingConstants.SOUTH, 1);
    Painters.Triangle limeTriangleUp = new Painters.Triangle(lime, SwingConstants.SOUTH, 1);

    d.put("Spinner:\"Spinner.previousButton\"[Disabled].foregroundPainter", disabledTriangleUp);
    d.put("Spinner:\"Spinner.previousButton\"[Enabled].foregroundPainter", triangleUp);
    d.put("Spinner:\"Spinner.previousButton\"[Focused].foregroundPainter", triangleUp);
    d.put("Spinner:\"Spinner.previousButton\"[Focused+MouseOver].foregroundPainter", limeTriangleUp);
    d.put("Spinner:\"Spinner.previousButton\"[Focused+Pressed].foregroundPainter", limeTriangleUp);
    d.put("Spinner:\"Spinner.previousButton\"[MouseOver].foregroundPainter", limeTriangleUp);
    d.put("Spinner:\"Spinner.previousButton\"[Pressed].foregroundPainter", limeTriangleUp);

    Painters.Triangle triangleDown = new Painters.Triangle(text, SwingConstants.NORTH, 1);
    Painters.Triangle disabledTriangleDown = new Painters.Triangle(disabledText, SwingConstants.NORTH, 1);
    Painters.Triangle limeTriangleDown = new Painters.Triangle(lime, SwingConstants.NORTH, 1);

    d.put("Spinner:\"Spinner.nextButton\"[Disabled].foregroundPainter", disabledTriangleDown);
    d.put("Spinner:\"Spinner.nextButton\"[Enabled].foregroundPainter", triangleDown);
    d.put("Spinner:\"Spinner.nextButton\"[Focused].foregroundPainter", triangleDown);
    d.put("Spinner:\"Spinner.nextButton\"[Focused+MouseOver].foregroundPainter", limeTriangleDown);
    d.put("Spinner:\"Spinner.nextButton\"[Focused+Pressed].foregroundPainter", limeTriangleDown);
    d.put("Spinner:\"Spinner.nextButton\"[MouseOver].foregroundPainter", limeTriangleDown);
    d.put("Spinner:\"Spinner.nextButton\"[Pressed].foregroundPainter", limeTriangleDown);

    d.put("Spinner:Panel:\"Spinner.formattedTextField\"[Disabled].backgroundPainter", border);
    d.put("Spinner:Panel:\"Spinner.formattedTextField\"[Enabled].backgroundPainter", border);
    d.put("Spinner:Panel:\"Spinner.formattedTextField\"[Focused+Selected].backgroundPainter", border);
    d.put("Spinner:Panel:\"Spinner.formattedTextField\"[Focused].backgroundPainter", limeBorder);
    d.put("Spinner:Panel:\"Spinner.formattedTextField\"[Selected].backgroundPainter", border);
  }

  private static void installSplitPaneDefaults(UIDefaults d) {
    d.put("SplitPane:SplitPaneDivider[Enabled].backgroundPainter", null);
    d.put("SplitPane:SplitPaneDivider[Focused].backgroundPainter", null);

    Painter grip = new Painters.CenterBox(blueGrey, px(12), px(2));
    Painter verticalGrip = new Painters.CenterBox(blueGrey, px(2), px(12));
    d.put("SplitPane:SplitPaneDivider[Enabled].foregroundPainter", grip);
    d.put("SplitPane:SplitPaneDivider[Enabled+Vertical].foregroundPainter", verticalGrip);
  }

  private static void installToolTipDefaults(UIDefaults d) {
    Painter border = new Painters.Border(blueGrey);
    Painter fill = new Painters.Background(darkestGrey, 0);
    Painter borderAndFill = new Painters.Composite(fill, border);
    d.put("ToolTip[Disabled].backgroundPainter", borderAndFill);
    d.put("ToolTip[Enabled].backgroundPainter", borderAndFill);
    d.put("ToolTip.contentMargins", new Insets(8, 8, 8, 8));
  }

  private static void installTabbedPaneDefaults(UIDefaults d) {
    Painter fillEmph = new Painters.Background(blueGreySubtle);
    Painter selected = new Painters.Background(blueGrey);
    Painter selectedEmph = new Painters.Background(blueGreyEmph);

    int thickness = 2;
    Painter tabArea = new Painters.BottomLine(blueGrey, thickness, new Insets(0, 1, 0, 1));

    d.put("TabbedPane:TabbedPaneTab[Enabled].backgroundPainter", null);
    d.put("TabbedPane:TabbedPaneTab[Enabled+MouseOver].backgroundPainter", fillEmph);
    d.put("TabbedPane:TabbedPaneTab[Enabled+Pressed].backgroundPainter", fillEmph);
    d.put("TabbedPane:TabbedPaneTab[Disabled].backgroundPainter", null);
    d.put("TabbedPane:TabbedPaneTab[Disabled+Selected].backgroundPainter", selected);
    d.put("TabbedPane:TabbedPaneTab[Selected].backgroundPainter", selected);
    d.put("TabbedPane:TabbedPaneTab[MouseOver+Selected].backgroundPainter", selectedEmph);
    d.put("TabbedPane:TabbedPaneTab[Pressed+Selected].backgroundPainter", selectedEmph);
    d.put("TabbedPane:TabbedPaneTab[Focused+Selected].backgroundPainter", selected);
    d.put("TabbedPane:TabbedPaneTab[Focused+MouseOver+Selected].backgroundPainter", selectedEmph);
    d.put("TabbedPane:TabbedPaneTab[Focused+Pressed+Selected].backgroundPainter", selectedEmph);
    d.put("TabbedPane:TabbedPaneTab.contentMargins", new InsetsUIResource(2, 12, 3, 12));

    d.put("TabbedPane:TabbedPaneTabArea.contentMargins", new Insets(3, 0, thickness, 0));
    d.put("TabbedPane:TabbedPaneTabArea[Disabled].backgroundPainter", tabArea);
    d.put("TabbedPane:TabbedPaneTabArea[Enabled+MouseOver].backgroundPainter", tabArea);
    d.put("TabbedPane:TabbedPaneTabArea[Enabled+Pressed].backgroundPainter", tabArea);
    d.put("TabbedPane:TabbedPaneTabArea[Enabled].backgroundPainter", tabArea);
  }

  private static void installTableDefaults(UIDefaults d) {
    // Workaround Swing issue, see http://stackoverflow.com/questions/15568054/how-to-change-jtable-row-height-globally
    // Using a bit more than the original 16 px to improve readability. Note that JXTable has its own fixes, this only
    // applies to regular JTable's.
    d.put("Table.rowHeight", px(20));

    d.put("Table[Enabled+Selected].textForeground", text);

    Painters.LeftLine leftLine = new Painters.LeftLine(darkestGrey, 1, new Insets(0, 0, 0, 0));
    Painters.Composite mouseOver = new Painters.Composite(new Painters.Background(blueGreySubtle), leftLine);
    d.put("TableHeader:\"TableHeader.renderer\"[Disabled].backgroundPainter", leftLine);
    d.put("TableHeader:\"TableHeader.renderer\"[Enabled].backgroundPainter", leftLine);
    d.put("TableHeader:\"TableHeader.renderer\"[Enabled+Focused].backgroundPainter", leftLine);
    d.put("TableHeader:\"TableHeader.renderer\"[MouseOver].backgroundPainter", mouseOver);
    d.put("TableHeader:\"TableHeader.renderer\"[Pressed].backgroundPainter", mouseOver);
    d.put("TableHeader:\"TableHeader.renderer\"[Enabled+Sorted].backgroundPainter", leftLine);
    d.put("TableHeader:\"TableHeader.renderer\"[Enabled+Sorted+MouseOver].backgroundPainter", mouseOver);
    d.put("TableHeader:\"TableHeader.renderer\"[Enabled+Focused+Sorted].backgroundPainter", leftLine);
    d.put("TableHeader:\"TableHeader.renderer\"[Disabled+Sorted].backgroundPainter", leftLine);

    Painters.Triangle upIcon = new Painters.Triangle(text, SwingConstants.NORTH, 0, px(5));
    Painters.Triangle downIcon = new Painters.Triangle(text, SwingConstants.SOUTH, 0, px(5));
    d.put("TableHeader[Enabled].ascendingSortIconPainter", upIcon);
    d.put("TableHeader[Enabled].descendingSortIconPainter", downIcon);
  }

  private static void installTreeDefaults(UIDefaults d) {
    fixTreeIcon(d);
  }

  private void installMenuDefaults(UIDefaults d) {
    Painter border = new Painters.Background(blueGrey, 0);
    Color panel = d.getColor("background");
    Painters.Background darkFill = new Painters.Background(panel, 0);
    Color text = ColorPalette.text;

    d.put("MenuBar[Enabled].backgroundPainter", darkFill);
    d.put("MenuBar:Menu[Enabled].textForeground", text);
    d.put("MenuBar:Menu[Selected].textForeground", text);
    d.put("MenuBar:Menu[Selected].backgroundPainter", border);

    d.put("MenuItem[Enabled].textForeground", text);
    d.put("MenuItem[MouseOver].textForeground", text);
    d.put("MenuItem[MouseOver].backgroundPainter", border);
    d.put("MenuItem:MenuItemAccelerator[MouseOver].textForeground", text);

    d.put("RadioButtonMenuItem[Enabled].textForeground", text);
    d.put("RadioButtonMenuItem[MouseOver].textForeground", text);
    d.put("RadioButtonMenuItem[MouseOver].backgroundPainter", border);
    d.put("RadioButtonMenuItem[MouseOver+Selected].textForeground", text);
    d.put("RadioButtonMenuItem[MouseOver+Selected].backgroundPainter", border);
    d.put("RadioButtonMenuItem:MenuItemAccelerator[MouseOver].textForeground", text);

    d.put("CheckBoxMenuItem[Enabled].textForeground", text);
    d.put("CheckBoxMenuItem[MouseOver].textForeground", text);
    d.put("CheckBoxMenuItem[MouseOver].backgroundPainter", border);
    d.put("CheckBoxMenuItem[MouseOver+Selected].textForeground", text);
    d.put("CheckBoxMenuItem[MouseOver+Selected].backgroundPainter", border);
    d.put("CheckBoxMenuItem:MenuItemAccelerator[MouseOver].textForeground", text);

    d.put("Menu[Enabled].textForeground", text);
    d.put("Menu[Enabled+Selected].textForeground", text);
    d.put("Menu[Enabled+Selected].backgroundPainter", border);
    d.put("Menu:MenuItemAccelerator[MouseOver].textForeground", text);

    d.put("PopupMenu[Disabled].backgroundPainter", darkFill);
    d.put("PopupMenu[Enabled].backgroundPainter", darkFill);
  }

  private void installSeparatorDefaults(UIDefaults d) {
    int gap = px(16);
    d.put("PopupMenuSeparator.thickness", gap);
    d.put("Separator.thickness", gap);
    d.put("ToolBar.separatorSize", new DimensionUIResource(gap, gap));

    d.put("PopupMenuSeparator[Enabled].backgroundPainter", null);
    d.put("ToolBarSeparator[Enabled].backgroundPainter", null);
    d.put("Separator[Enabled].backgroundPainter", null);
  }

  private void installSliderDefaults(UIDefaults d) {
    Color text = ColorPalette.text;
    d.put("Slider.tickColor", text);

    d.put("Slider.thumbHeight", 13);
    d.put("Slider.thumbWidth", 13);

    Painters.Circle disabledThumb = new Painters.Circle(disabledText);
    Painters.Circle thumbPainter = new Painters.Circle(lime);
    Painters.Circle thumbPainterEmph = new Painters.Circle(limeEmph);
    d.put("Slider:SliderThumb.backgroundPainter", thumbPainter);
    d.put("Slider:SliderThumb[Disabled].backgroundPainter", disabledThumb);
    d.put("Slider:SliderThumb[Enabled].backgroundPainter", thumbPainter);
    d.put("Slider:SliderThumb[Focused].backgroundPainter", thumbPainter);
    d.put("Slider:SliderThumb[Focused+MouseOver].backgroundPainter", thumbPainterEmph);
    d.put("Slider:SliderThumb[Focused+Pressed].backgroundPainter", thumbPainterEmph);
    d.put("Slider:SliderThumb[MouseOver].backgroundPainter", thumbPainterEmph);
    d.put("Slider:SliderThumb[Pressed].backgroundPainter", thumbPainterEmph);
    d.put("Slider:SliderThumb[ArrowShape+Enabled].backgroundPainter", thumbPainter);
    d.put("Slider:SliderThumb[ArrowShape+Disabled].backgroundPainter", thumbPainter);
    d.put("Slider:SliderThumb[ArrowShape+MouseOver].backgroundPainter", thumbPainterEmph);
    d.put("Slider:SliderThumb[ArrowShape+Pressed].backgroundPainter", thumbPainterEmph);
    d.put("Slider:SliderThumb[ArrowShape+Focused].backgroundPainter", thumbPainter);
    d.put("Slider:SliderThumb[ArrowShape+Focused+MouseOver].backgroundPainter", thumbPainterEmph);
    d.put("Slider:SliderThumb[ArrowShape+Focused+Pressed].backgroundPainter", thumbPainterEmph);

    Painter disabledTrack = new Painters.SliderTrack(disabledText, disabledText);
    Painter track = new Painters.SliderTrack(lime, ColorPalette.text);
    d.put("Slider:SliderTrack[Disabled].backgroundPainter", disabledTrack);
    d.put("Slider:SliderTrack[Enabled].backgroundPainter", track);
  }

  private void installTextFieldDefaults(UIDefaults d) {
    Painter heavyBorder = new Painters.Border(blueGrey, 2);
    Painter limeBorder = new Painters.Border(lime, 2);
    Painter fill = new Painters.Background(darkestGrey);

    d.put("TextField[Enabled].borderPainter", null);
    d.put("TextField[Disabled].borderPainter", null);
    d.put("TextField[Focused].borderPainter", limeBorder);
    d.put("TextField[Enabled].backgroundPainter", fill);
    d.put("TextField[Disabled].backgroundPainter", fill);
    d.put("TextField[Selected].backgroundPainter", fill);

    d.put("FormattedTextField[Enabled].borderPainter", null);
    d.put("FormattedTextField[Disabled].borderPainter", null);
    d.put("FormattedTextField[Focused].borderPainter", limeBorder);
    d.put("FormattedTextField[Enabled].backgroundPainter", fill);
    d.put("FormattedTextField[Disabled].backgroundPainter", fill);
    d.put("FormattedTextField[Selected].backgroundPainter", fill);

    d.put("PasswordField[Enabled].borderPainter", null);
    d.put("PasswordField[Disabled].borderPainter", null);
    d.put("PasswordField[Focused].borderPainter", limeBorder);
    d.put("PasswordField[Enabled].backgroundPainter", fill);
    d.put("PasswordField[Disabled].backgroundPainter", fill);
    d.put("PasswordField[Selected].backgroundPainter", fill);

    // Register a special kind of text fields that are visually more distinguishing. Mainly used
    // for the search box. It copies all properties of a regular text field, and then overwrites a few.
    String empthTextField = "\"" + EMPHASIZED_TEXT_FIELD_NAME + "\"";
    register(Region.TEXT_FIELD, empthTextField); //see docs of the register method
    for (Object key : new ArrayList<>(d.keySet())) {
      if (key instanceof String) {
        String k = (String) key;
        if (k.startsWith("TextField.") || k.startsWith("TextField[")) {
          d.put(k.replace("TextField", empthTextField), d.get(k));
        }
      }
    }
    d.put(empthTextField + "[Enabled].borderPainter", heavyBorder);
    d.put(empthTextField + "[Disabled].borderPainter", heavyBorder);
    d.put(empthTextField + "[Focused].borderPainter", limeBorder);
    d.put(empthTextField + "[Enabled].backgroundPainter", fill);
    d.put(empthTextField + "[Disabled].backgroundPainter", fill);
    d.put(empthTextField + "[Selected].backgroundPainter", fill);
  }

  private void installTextAreaDefaults(UIDefaults d) {
    Painter limeBorder = new Painters.Border(lime, 2);
    Painter fill = new Painters.Background(darkestGrey);
    d.put("TextArea[Disabled+NotInScrollPane].backgroundPainter", fill);
    d.put("TextArea[Disabled+NotInScrollPane].borderPainter", null);
    d.put("TextArea[Disabled].backgroundPainter", fill);
    d.put("TextArea[Enabled+NotInScrollPane].backgroundPainter", fill);
    d.put("TextArea[Enabled+NotInScrollPane].borderPainter", null);
    d.put("TextArea[Enabled].backgroundPainter", fill);
    d.put("TextArea[Focused+NotInScrollPane].borderPainter", limeBorder);
    d.put("TextArea[Selected].backgroundPainter", limeBorder);
  }

  private void installProgressBarDefaults(UIDefaults d) {
    Painter limeFill = new Painters.Fill(lime);
    Painter disabledFill = new Painters.Fill(disabledText);
    Painter fill = new Painters.Background(darkestGrey);
    d.put("ProgressBar[Disabled].backgroundPainter", fill);
    d.put("ProgressBar[Enabled].backgroundPainter", fill);

    d.put("ProgressBar[Disabled+Finished].foregroundPainter", disabledFill);
    d.put("ProgressBar[Disabled].foregroundPainter", disabledFill);
    d.put("ProgressBar[Enabled+Finished].foregroundPainter", limeFill);
    d.put("ProgressBar[Enabled].foregroundPainter", limeFill);

    d.put("ProgressBar.horizontalSize", new Dimension(150, 8));
    d.put("ProgressBar.verticalSize", new Dimension(8, 150));

    d.put("ProgressBar.tileWidth", 18);
    d.put("ProgressBar.cycleTime", 500);
    d.put("ProgressBar[Disabled+Indeterminate].foregroundPainter", new Painters.Skewed(lime));
    d.put("ProgressBar[Enabled+Indeterminate].foregroundPainter", new Painters.Skewed(lime));

    // Lucy map centric specific property
    d.put("ProgressBar.circleUIBackgroundColor", ColorPalette.buttonBorder);
  }

  private void installHyperlinkDefaults(UIDefaults d) {
    // Colors for JXHyperlink (SwingX)
    d.put("Hyperlink.linkColor", blue);
    d.put("Hyperlink.visitedColor", blue.darker());
  }

  private void installEditorPaneDefaults() {
    HTMLEditorKit htmlEditorKit = new HTMLEditorKit();
    StyleSheet defaultStyleSheet = htmlEditorKit.getStyleSheet();
    defaultStyleSheet.addRule("a {color:" + toHTML(ColorPalette.blue) + "}");
    defaultStyleSheet.addRule("a:visited {color:" + toHTML(ColorPalette.blue.darker()) + "}");
    //Calling setStyleSheet will adjust the default style sheet for all HTMLEditorKit instances
    //See the javadoc of setStyleSheet
    htmlEditorKit.setStyleSheet(defaultStyleSheet);
  }

  private String toHTML(Color aColor) {
    return String.format("#%02x%02x%02x", aColor.getRed(), aColor.getGreen(), aColor.getBlue());
  }

  @Override
  public Icon getDisabledIcon(JComponent component, Icon icon) {
    if (icon == null) {
      return null;
    }
    return new DisabledIcon(icon);
  }

  /**
   * Additionally install Jide settings, the docking framework used by Lucy and other applications.
   * They are not automatically installed as Jide requires a particular call sequence of these methods:
   * <pre>
   *   BlackLimeLookAndFeel.install();
   *   LookAndFeelFactory.installJideExtension();
   *   BlackLimeLookAndFeel.installJideDockingDefaults();
   * </pre>
   *
   * When using Lucy, this is automatically taken care of, see also {@code DockableHolderFactory}.
   */
  public static void installJideDockingDefaults() {
    UIDefaults d = UIManager.getLookAndFeel().getDefaults();
    d.put("DockableFrame.background", darkestGrey);
    d.put("DockableFrame.border", BorderFactory.createEmptyBorder());
    d.put("DockableFrame.floatingBorder", BorderFactory.createEmptyBorder());
    d.put("DockableFrame.slidingEastBorder", BorderFactory.createEmptyBorder());
    d.put("DockableFrame.slidingWestBorder", BorderFactory.createEmptyBorder());
    d.put("DockableFrame.slidingSouthBorder", BorderFactory.createEmptyBorder());
    d.put("DockableFrame.slidingNorthBorder", BorderFactory.createEmptyBorder());
    d.put("DockableFrame.activeTitleBackground", blueGreyEmph);
    d.put("DockableFrame.activeTitleForeground", text);
    d.put("DockableFrame.inactiveTitleBackground", blueGreySubtle);
    d.put("DockableFrame.inactiveTitleForeground", text);
    d.put("DockableFrame.titleBorder", BorderFactory.createEmptyBorder());
    d.put("DockableFrame.activeTitleBorderColor", blueGrey);
    d.put("DockableFrame.inactiveTitleBorderColor", darkestGrey);
    d.put("Contour.color", buttonBorder);
    d.put("DockableFrameTitlePane.font", d.get("InternalFrame.titleFont"));
    d.put("SidePane.foreground", text);
    d.put("SidePane.background", darkestGrey);
    d.put("SidePane.lineColor", darkestGrey);
    d.put("SidePane.buttonBackground", blueGrey);
    d.put("JideTabbedPane.background", darkestGrey);
    d.put("JideTabbedPane.foreground", text);
    d.put("JideTabbedPane.light", blueGrey);
    d.put("JideTabbedPane.highlight", text);
    d.put("JideTabbedPane.shadow", darkestGrey);
    d.put("JideTabbedPane.darkShadow", darkestGrey);
    d.put("JideTabbedPane.contentBorderInsets", new Insets(0, 0, 0, 0));
    d.put("JideTabbedPane.tabAreaBackground", darkestGrey);
    d.put("JideTabbedPane.selectedTabTextForeground", text);
    d.put("JideTabbedPane.unselectedTabTextForeground", text);
    d.put("JideTabbedPane.selectedTabBackground", blueGrey);
    d.put("ContentContainer.background", darkestGrey);
    d.put("Workspace.background", darkestGrey);

    // When floating a dockable panel, JIDE creates a dialog with window decoration style NONE. This means the
    // dialog has no border. So explicitly set the border, using JIDE specific properties.
    d.put("Resizable.resizeBorder", d.getBorder("RootPane.plainDialogBorder"));
  }

  /**
   * Replacement for Nimbus' built-in tree icon, but with a color that is visible on the
   * background of a JTree when using the dark theme.
   */
  private static class TreeIcon implements Icon {
    private static final Polygon COLLAPSED = new Polygon(new int[]{0, 6, 0}, new int[]{0, 3, 6}, 3);
    private static final Polygon EXPANDED = new Polygon(new int[]{0, 6, 3}, new int[]{0, 0, 6}, 3);
    private final boolean fCollapsed;

    public TreeIcon(boolean aCollapsed) {
      fCollapsed = aCollapsed;
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
      g.setColor(UIManager.getColor("text"));
      g.translate(x, y);
      Graphics2D g2d = (Graphics2D) g;
      g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      g2d.fill(fCollapsed ? COLLAPSED : EXPANDED);
      g.translate(-x, -y);
    }

    @Override
    public int getIconWidth() {
      return 18;
    }

    @Override
    public int getIconHeight() {
      return 7;
    }
  }
}

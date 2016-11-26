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

import static com.luciad.util.ILcdFireEventMode.NO_EVENT;

import com.luciad.model.ILcdModel;
import com.luciad.model.TLcdVectorModel;
import com.luciad.reference.TLcdGeodeticReference;
import com.luciad.shape.shape2D.ILcd2DEditablePointList;
import com.luciad.symbology.app6a.model.ELcdAPP6Standard;
import com.luciad.symbology.app6a.model.ILcdAPP6ACoded;
import com.luciad.symbology.app6a.model.TLcdEditableAPP6AObject;

import samples.symbology.common.EMilitarySymbology;

/**
 * Creates a default APP6-A or APP6-B model.
 */
public class APP6ModelFactory {

  private final String fName;

  public APP6ModelFactory() {
    fName = "APP-6";
  }

  /**
   * Create a default model with a few APP-6A shapes added.
   *
   * @return a model with a few APP-6A shapes added.
   */
  public ILcdModel createModel(ELcdAPP6Standard aAPP6Standard) {
    TLcdVectorModel model = new TLcdVectorModel();

    // Model reference and descriptor
    model.setModelReference(new TLcdGeodeticReference());
    model.setModelDescriptor(new APP6ModelDescriptor(null, aAPP6Standard.name(), fName, null,
                                                     aAPP6Standard == ELcdAPP6Standard.APP_6A ? EMilitarySymbology.APP6A :
                                                     aAPP6Standard == ELcdAPP6Standard.APP_6B ? EMilitarySymbology.APP6B :
                                                     EMilitarySymbology.APP6C));

    model.addElement(createStrongPoint(aAPP6Standard), NO_EVENT);
    model.addElement(createHelicopterAttack(aAPP6Standard), NO_EVENT);
    model.addElement(createGroundUnitsMainAttack(aAPP6Standard), NO_EVENT);
    model.addElement(createEngagementArea(aAPP6Standard), NO_EVENT);
    model.addElement(createCarrier(aAPP6Standard), NO_EVENT);
    model.addElement(createNeutralHelicopter(aAPP6Standard), NO_EVENT);
    model.addElement(createNeutralAirplane(aAPP6Standard), NO_EVENT);
    model.addElement(createLightInfantry(aAPP6Standard), NO_EVENT);
    model.addElement(createInfantry(aAPP6Standard), NO_EVENT);
    model.addElement(createMechanizedInfantry(aAPP6Standard), NO_EVENT);
    model.addElement(createMedicalTheatre(aAPP6Standard), NO_EVENT);
    model.addElement(createMotorizedInfantry(aAPP6Standard), NO_EVENT);
    model.addElement(createGroundVehicle(aAPP6Standard), NO_EVENT);
    model.addElement(createWheeledArmor(aAPP6Standard), NO_EVENT);
    model.addElement(createAirport(aAPP6Standard), NO_EVENT);
    model.addElement(createCombat(aAPP6Standard), NO_EVENT);
    model.addElement(createEngineer(aAPP6Standard), NO_EVENT);
    model.addElement(createOutlinedInfantry(aAPP6Standard), NO_EVENT);
    model.addElement(createAttackRotaryWing(aAPP6Standard), NO_EVENT);
    model.addElement(createMilitaryFixedWing(aAPP6Standard), NO_EVENT);
    model.addElement(createCombatantLine(aAPP6Standard), NO_EVENT);
    model.addElement(createMineWarfareVessel(aAPP6Standard), NO_EVENT);
    model.addElement(createCruiser(aAPP6Standard), NO_EVENT);
    return model;
  }

  public TLcdEditableAPP6AObject createCruiser(ELcdAPP6Standard aAPP6Standard) {
    // The 'Cruiser' (ship) symbol, represented as a point symbol (icon).
    String app6aCode = is20Digit(aAPP6Standard) ? "10023000001202020000" : "SFSPCLCC-------";
    TLcdEditableAPP6AObject app6aObject = createStyledAPP6AObject(app6aCode, true, true, true, 50, aAPP6Standard);
    app6aObject.move2DPoint(0, 9.328908011869435, 43.94177304233589);
    return app6aObject;
  }

  private boolean is20Digit(ELcdAPP6Standard aAPP6Standard) {
    return aAPP6Standard == ELcdAPP6Standard.APP_6C;
  }

  public TLcdEditableAPP6AObject createMineWarfareVessel(ELcdAPP6Standard aAPP6Standard) {
    // The 'Mine warfare vessel' (ship) symbol, represented as a point symbol (icon).
    String app6aCode = is20Digit(aAPP6Standard) ? "10023000001204000000" : "SFSPCM---------";
    TLcdEditableAPP6AObject app6aObject = createStyledAPP6AObject(app6aCode, true, true, true, 50, aAPP6Standard);
    app6aObject.move2DPoint(0, 9.096034286725052, 43.763097251646236);
    return app6aObject;
  }

  public TLcdEditableAPP6AObject createCombatantLine(ELcdAPP6Standard aAPP6Standard) {
    // The 'Combatant line' (ship) symbol, represented as a point symbol (icon).
    String app6aCode = is20Digit(aAPP6Standard) ? "10023000001202000000" : "SFSPCL---------";
    TLcdEditableAPP6AObject app6aObject = createStyledAPP6AObject(app6aCode, true, true, true, 50, aAPP6Standard);
    app6aObject.move2DPoint(0, 8.665219758859857, 43.87650593990823);
    return app6aObject;
  }

  public TLcdEditableAPP6AObject createMilitaryFixedWing(ELcdAPP6Standard aAPP6Standard) {
    // The 'Military fixed wing' (airplane) symbol, represented as a point symbol (icon).
    // The fill of the icon frame and the icon frame itself are turned off.
    String app6aCode = is20Digit(aAPP6Standard) ? "10020100001101000000" : "SFAPMF---------";
    TLcdEditableAPP6AObject app6aObject = createStyledAPP6AObject(app6aCode, false, false, true, 64, aAPP6Standard, -50, -50);
    app6aObject.move2DPoint(0, 7.459209530031675, 44.07233851527138);
    return app6aObject;
  }

  public TLcdEditableAPP6AObject createAttackRotaryWing(ELcdAPP6Standard aAPP6Standard) {
    // The 'Attack rotary wing' (helicopter) symbol, represented as a point symbol (icon).
    // The fill of the icon frame and the icon frame itself are turned off.
    String app6aCode = is20Digit(aAPP6Standard) ? "10020100001102000100" : "SFAPMHA--------";
    TLcdEditableAPP6AObject app6aObject = createStyledAPP6AObject(app6aCode, false, false, true, 64, aAPP6Standard, 50, 50);
    app6aObject.move2DPoint(0, 7.645212343292285, 43.947044424658586);
    return app6aObject;
  }

  public TLcdEditableAPP6AObject createOutlinedInfantry(ELcdAPP6Standard aAPP6Standard) {
    // The 'Infantry light' (unit) symbol, represented as a point symbol (icon).
    // The fill of the icon frame is turned off, resulting in an outlined icon.
    String app6aCode = is20Digit(aAPP6Standard) ? "10021000151211000019" : "SFGPUCIL---E---";
    TLcdEditableAPP6AObject app6aObject = createStyledAPP6AObject(app6aCode, false, true, true, 50, aAPP6Standard);
    app6aObject.move2DPoint(0, 7.453009436256322, 45.04312150501273);
    return app6aObject;
  }

  public TLcdEditableAPP6AObject createEngineer(ELcdAPP6Standard aAPP6Standard) {
    // The 'Engineer' (unit) symbol, represented as a point symbol (icon).
    // The fill of the icon frame is turned off, resulting in an outlined icon.
    String app6aCode = is20Digit(aAPP6Standard) ? "10021000121407000000" : "SFGPUCE----B---";
    TLcdEditableAPP6AObject app6aObject = createStyledAPP6AObject(app6aCode, false, true, true, 50, aAPP6Standard);
    app6aObject.move2DPoint(0, 7.814914613274074, 44.98109975858406);
    return app6aObject;
  }

  public TLcdEditableAPP6AObject createCombat(ELcdAPP6Standard aAPP6Standard) {
    // The 'Combat' (unit) symbol, represented as a point symbol (icon).
    // The fill of the icon frame is turned off, resulting in an outlined icon.
    String app6aCode = is20Digit(aAPP6Standard) ? "10021000251209000000" : "SFGPUC-----M---";
    TLcdEditableAPP6AObject app6aObject = createStyledAPP6AObject(app6aCode, false, true, true, 50, aAPP6Standard);
    app6aObject.move2DPoint(0, 7.735389671600583, 45.18911546645908);
    return app6aObject;
  }

  public TLcdEditableAPP6AObject createAirport(ELcdAPP6Standard aAPP6Standard) {
    // The 'Airport - airbase' symbol, represented as a point symbol (icon).
    String app6aCode = is20Digit(aAPP6Standard) ? "10062000001101000000" : "SHGPIBA---H----";
    TLcdEditableAPP6AObject app6aObject = createStyledAPP6AObject(app6aCode, true, true, true, 50, aAPP6Standard);
    app6aObject.move2DPoint(0, 10.69930451091407, 45.081254935220706);
    return app6aObject;
  }

  public TLcdEditableAPP6AObject createWheeledArmor(ELcdAPP6Standard aAPP6Standard) {
    // The 'Armor - wheeled' (unit) symbol, represented as a point symbol (icon).
    String app6aCode = is20Digit(aAPP6Standard) ? "10061002141205000051" : "SHGPUCAW--AD---";
    TLcdEditableAPP6AObject app6aObject = createStyledAPP6AObject(app6aCode, false, true, true, 50, aAPP6Standard);
    app6aObject.move2DPoint(0, 10.800536795252225, 44.74619298043526);
    return app6aObject;
  }

  public TLcdEditableAPP6AObject createGroundVehicle(ELcdAPP6Standard aAPP6Standard) {
    // The 'Ground vehicle' (unit) symbol, represented as a point symbol (icon).
    String app6aCode = is20Digit(aAPP6Standard) ? "10061500342200000000" : "SHGPEV----MR---";
    TLcdEditableAPP6AObject app6aObject = createStyledAPP6AObject(app6aCode, false, true, true, 50, aAPP6Standard);
    app6aObject.move2DPoint(0, 10.63561424332344, 44.60176907392433);
    return app6aObject;
  }

  public TLcdEditableAPP6AObject createMotorizedInfantry(ELcdAPP6Standard aAPP6Standard) {
    // The 'Infantry - motorized' (unit) symbol, represented as a point symbol (icon).
    String app6aCode = is20Digit(aAPP6Standard) ? "10061010171211050000" : "SHGAUCIM---G---";
    TLcdEditableAPP6AObject app6aObject = createStyledAPP6AObject(app6aCode, false, true, true, 50, aAPP6Standard);
    app6aObject.move2DPoint(0, 10.329852948020438, 44.81850538617699);
    return app6aObject;
  }

  public TLcdEditableAPP6AObject createMedicalTheatre(ELcdAPP6Standard aAPP6Standard) {
    // The 'Medical theatre' (unit) symbol, represented as a point symbol (icon).
    String app6aCode = is20Digit(aAPP6Standard) ? "10001000001613030000" : "SUGPUSMT-------";
    TLcdEditableAPP6AObject app6aObject = createStyledAPP6AObject(app6aCode, true, true, true, 40, aAPP6Standard);
    app6aObject.putTextModifier(ILcdAPP6ACoded.sAdditionalInformation, "B");
    app6aObject.move2DPoint(0, 9.285616498149238, 44.79643659661491);
    return app6aObject;
  }

  public TLcdEditableAPP6AObject createMechanizedInfantry(ELcdAPP6Standard aAPP6Standard) {
    // The 'Infantry - mechanized' (unit) symbol, represented as a point symbol (icon).
    String app6aCode = is20Digit(aAPP6Standard) ? "10001000231211020000" : "SUGPUCIZ---K---";
    TLcdEditableAPP6AObject app6aObject = createStyledAPP6AObject(app6aCode, false, true, true, 40, aAPP6Standard);
    app6aObject.putTextModifier(ILcdAPP6ACoded.sAdditionalInformation, "A");
    app6aObject.putTextModifier(ILcdAPP6ACoded.sSpeedLabel, "60km/h");
    app6aObject.move2DPoint(0, 8.56094419751349, 44.79643659661491);
    return app6aObject;
  }

  public TLcdEditableAPP6AObject createInfantry(ELcdAPP6Standard aAPP6Standard) {
    // The 'Infantry' (unit) symbol, represented as a point symbol (icon).
    String app6aCode = is20Digit(aAPP6Standard) ? "10001010161211000000" : "SUGAUCI----F---";
    TLcdEditableAPP6AObject app6aObject = createStyledAPP6AObject(app6aCode, false, true, true, 40, aAPP6Standard, 50, 50);
    app6aObject.putTextModifier(ILcdAPP6ACoded.sUniqueDesignation, "2");
    app6aObject.move2DPoint(0, 8.972032411217977, 44.6992329363481);
    return app6aObject;
  }

  public TLcdEditableAPP6AObject createLightInfantry(ELcdAPP6Standard aAPP6Standard) {
    // The 'Infantry - light' (unit) symbol, represented as a point symbol (icon).
    String app6aCode = is20Digit(aAPP6Standard) ? "10001000141211000019" : "SUGPUCIL---D---";
    TLcdEditableAPP6AObject app6aObject = createStyledAPP6AObject(app6aCode, true, true, true, 40, aAPP6Standard);
    app6aObject.putTextModifier(ILcdAPP6ACoded.sUniqueDesignation, "1");
    app6aObject.move2DPoint(0, 8.972032411217977, 44.941101478155985);
    return app6aObject;
  }

  public TLcdEditableAPP6AObject createNeutralAirplane(ELcdAPP6Standard aAPP6Standard) {
    // An airplane symbol, with neutral affiliation. The affiliation frame is turned off,
    // leaving only the internal icon.
    String app6aCode = is20Digit(aAPP6Standard) ? "10040100001201000101" : "SNAPCF---------";
    TLcdEditableAPP6AObject app6aObject = createStyledAPP6AObject(app6aCode, true, false, true, 64, aAPP6Standard);
    app6aObject.move2DPoint(0, 7.4220089673795515, 43.879812960459056);
    return app6aObject;
  }

  public TLcdEditableAPP6AObject createNeutralHelicopter(ELcdAPP6Standard aAPP6Standard) {
    // A helicopter symbol, with neutral affiliation. The affiliation frame is turned off,
    // leaving only the internal icon.
    String app6aCode = is20Digit(aAPP6Standard) ? "10040100001202000302" : "SNAPCH---------";
    TLcdEditableAPP6AObject app6aObject = createStyledAPP6AObject(app6aCode, true, false, true, 64, aAPP6Standard, -50, 0);
    app6aObject.move2DPoint(0, 7.2670066229957095, 43.987346590714985);
    return app6aObject;
  }

  public TLcdEditableAPP6AObject createCarrier(ELcdAPP6Standard aAPP6Standard) {
    // An armoured personnel carrier.
    // The forces are transported on a rail(e.g. by train);
    // the affiliation is set to Assumed Friend;
    // the status of the icon is Anticipated (or planned);
    // the pixel width is set to 50
    String app6aCode = is20Digit(aAPP6Standard) ? "10021510362303000000" : "SAGAEVAA--MT---";
    TLcdEditableAPP6AObject app6aObject = createStyledAPP6AObject(app6aCode, true, true, true, 50, aAPP6Standard);
    app6aObject.move2DPoint(0, 7.502932149785764, 44.84635100255543);
    return app6aObject;
  }

  public TLcdEditableAPP6AObject createEngagementArea(ELcdAPP6Standard aAPP6Standard) {
    // The 'Engagement area' symbol, which is represented as a rounded area
    // with at least three points.
    String app6aCode = is20Digit(aAPP6Standard) ? "10022510001512000000" : "GFCAMMAE-------";
    TLcdEditableAPP6AObject app6aObject = createStyledAPP6AObject(app6aCode, aAPP6Standard);
    app6aObject.putTextModifier(ILcdAPP6ACoded.sUniqueDesignation, "ALFA");
    ILcd2DEditablePointList pointList = app6aObject.get2DEditablePointList();
    pointList.move2DPoint(0, 8.585694817186367, 44.02235110003209);
    pointList.move2DPoint(1, 8.383823811399814, 43.70367890628065);
    pointList.move2DPoint(2, 9.21577704736864, 43.6504001169902);
    pointList.insert2DPoint(3, 9.246363563396905, 43.81452336857302);
    pointList.insert2DPoint(4, 9.595049846119132, 43.87208076415804);
    pointList.insert2DPoint(5, 9.460469175594765, 44.0576532307999);
    pointList.insert2DPoint(6, 9.166838621723414, 44.04441741380331);
    pointList.insert2DPoint(7, 8.879325371057718, 43.95168329967045);
    return app6aObject;
  }

  public TLcdEditableAPP6AObject createGroundUnitsMainAttack(ELcdAPP6Standard aAPP6Standard) {
    // The 'Ground units - main attack' symbol, represented as
    // a multipoint curved arrow with at least two points.
    String app6aCode = is20Digit(aAPP6Standard) ? "10022500001514000000" : "GFCPMOLAM------";
    TLcdEditableAPP6AObject app6aObject = createStyledAPP6AObject(app6aCode, aAPP6Standard);
    app6aObject.setWidth(7000); // in meters.
    ILcd2DEditablePointList pointList = app6aObject.get2DEditablePointList();
    pointList.move2DPoint(0, 8.01721796981351, 45.17483554290913);
    pointList.move2DPoint(1, 8.50702537806645, 45.056306677957345);
    pointList.insert2DPoint(2, 8.953432129891917, 45.27560909762362);
    pointList.insert2DPoint(3, 9.561041319876578, 45.078275166489966);
    pointList.insert2DPoint(4, 9.951647227723862, 45.209907611144736);
    pointList.insert2DPoint(5, 10.292652385368315, 45.10462613333734);
    return app6aObject;
  }

  public TLcdEditableAPP6AObject createHelicopterAttack(ELcdAPP6Standard aAPP6Standard) {
    // The 'Helicopter attack' symbol, represented as
    // a multipoint curved arrow with at least two points.
    String app6aCode = is20Digit(aAPP6Standard) ? "10022500001513000000" : "GFCPMOLAH------";
    TLcdEditableAPP6AObject app6aObject = createStyledAPP6AObject(app6aCode, aAPP6Standard);
    app6aObject.setWidth(7000); // in meters.
    ILcd2DEditablePointList pointList = app6aObject.get2DEditablePointList();
    pointList.move2DPoint(0, 7.7506139374732985, 44.023147622179884);
    pointList.move2DPoint(1, 8.192148068575053, 44.16985715034313);
    pointList.insert2DPoint(2, 8.619725136260778, 44.49999098960391);
    pointList.insert2DPoint(3, 8.988996240171174, 44.53707186016304);
    pointList.insert2DPoint(4, 9.397137986598457, 44.4025406627216);
    pointList.insert2DPoint(5, 9.928370100995872, 44.46288635979476);
    return app6aObject;
  }

  public TLcdEditableAPP6AObject createStrongPoint(ELcdAPP6Standard aAPP6Standard) {
    // The 'Strong point' symbol, which is represented as a rounded area
    // with at least three points. When the APP-6A code is set on
    // the object, a minimum number of points is created. The most common
    // symbol types with their minimum number of points are:
    // - area symbols: 3 or more points.
    // - polyline symbols: 2 or more points.
    // - point symbols: 1 point.
    // Special point configurations are possible, depending on the symbol;
    // this is typically defined in the APP-6A specification.
    // These predefined points can be moved directly, as illustrated in the code below.
    // If additional points are needed, they have to be inserted.
    String app6aCode = is20Digit(aAPP6Standard) ? "10062510001511000000" : "GHCAMMPSE------";
    TLcdEditableAPP6AObject app6aObject = createStyledAPP6AObject(app6aCode, aAPP6Standard);
    ILcd2DEditablePointList pointList = app6aObject.get2DEditablePointList();
    pointList.move2DPoint(0, 10.41425313245692, 45.01236589023088);
    pointList.move2DPoint(1, 10.647476987558223, 45.32407792048477);
    pointList.move2DPoint(2, 11.047481599947558, 45.06322204817484);
    pointList.insert2DPoint(3, 11.17223066153616, 44.82825715044357);
    pointList.insert2DPoint(4, 10.882573016875996, 44.644955600665995);
    pointList.insert2DPoint(5, 10.963802004321845, 44.381321120853016);
    pointList.insert2DPoint(6, 10.373507390872486, 44.285479793414225);
    pointList.insert2DPoint(7, 10.422854354650742, 44.65942036930641);
    pointList.insert2DPoint(8, 10.069449009455582, 44.6992329363481);
    pointList.insert2DPoint(9, 10.13568019441925, 44.883512787969735);
    return app6aObject;
  }

  /**
   * Creates a new APP-6A object, with an associated style that defines the rendering of
   * the symbol or its labels.
   * <p/>
   * The default domain object <code>TLcdEditableAPP6AObject</code> does not have an associated
   * style; for these objects, the default style that is set on the painter provider and label
   * painter provider is used. To have an associated style, a APP-6A object needs to implement
   * ILcdAPP6AStyled, like <code>StyledAPP6AObject</code>.
   *
   * @param aAPP6ACode The symbol ID code.
   * @param aAPP6Standard the APP6 standard
   * @return A new APP-6A object, with an associated style.
   */
  private StyledEditableAPP6Object createStyledAPP6AObject(String aAPP6ACode, ELcdAPP6Standard aAPP6Standard) {
    // Styled objects.
    return new StyledEditableAPP6Object(aAPP6ACode, aAPP6Standard);
  }

 /**
   * Creates a new APP-6A object, with an associated style that defines the rendering of
   * the symbol or its labels.
   * <p/>
   * The additional parameters specify a set of rendering properties that need to be used in
   * the symbol's associated style. The first three properties determine the rendering of the icon,
   * following the display options defined in the APP-6A specification; these properties are
   * only applicable to framed icons. The size is applicable to all icons.
   *
   * @param aAPP6ACode            The symbol ID code.
   * @param aIsSymbolFillEnabled  Sets whether the framed icon needs to be painted filled or outlined.
   * @param aIsSymbolFrameEnabled Sets whether the affiliation frame of an icon needs to be painted.
   * @param aIsSymbolIconEnabled  Sets whether the internal icon inside the affiliation frame needs to be painted.
   * @param aSize                 Sets the size of the icon.
   * @param aAPP6Standard         The APP6 standard
   * @return A new APP-6A object, with an associated style and the given style properties.
   */
  private StyledEditableAPP6Object createStyledAPP6AObject(String aAPP6ACode,
                                                           boolean aIsSymbolFillEnabled,
                                                           boolean aIsSymbolFrameEnabled,
                                                           boolean aIsSymbolIconEnabled,
                                                           int aSize,
                                                           ELcdAPP6Standard aAPP6Standard) {
    return createStyledAPP6AObject(aAPP6ACode, aIsSymbolFillEnabled, aIsSymbolFrameEnabled, aIsSymbolIconEnabled, aSize, aAPP6Standard, 0, 0);
  }

  /**
   * Creates a new APP-6A object, with an associated style that defines the rendering of
   * the symbol or its labels.
   * <p/>
   * The additional parameters specify a set of rendering properties that need to be used in
   * the symbol's associated style. The first three properties determine the rendering of the icon,
   * following the display options defined in the APP-6A specification; these properties are
   * only applicable to framed icons. The size is applicable to all icons.
   *
   * @param aAPP6ACode            The symbol ID code.
   * @param aIsSymbolFillEnabled  Sets whether the framed icon needs to be painted filled or outlined.
   * @param aIsSymbolFrameEnabled Sets whether the affiliation frame of an icon needs to be painted.
   * @param aIsSymbolIconEnabled  Sets whether the internal icon inside the affiliation frame needs to be painted.
   * @param aSize                 Sets the size of the icon.
   * @param aAPP6Standard         The APP6 standard
   * @param aOffsetX              Sets the X offset of the icon
   * @param aOffsetY              Sets the Y offset of the icon
   * @return A new APP-6A object, with an associated style and the given style properties.
   */
  private StyledEditableAPP6Object createStyledAPP6AObject(String aAPP6ACode,
                                                           boolean aIsSymbolFillEnabled,
                                                           boolean aIsSymbolFrameEnabled,
                                                           boolean aIsSymbolIconEnabled,
                                                           int aSize,
                                                           ELcdAPP6Standard aAPP6Standard,
                                                           int aOffsetX,
                                                           int aOffsetY) {
    // Styled objects.
    StyledEditableAPP6Object object = new StyledEditableAPP6Object(aAPP6ACode, aAPP6Standard);
    object.getAPP6AStyle().setSymbolFillEnabled(aIsSymbolFillEnabled);
    object.getAPP6AStyle().setSymbolFrameEnabled(aIsSymbolFrameEnabled);
    object.getAPP6AStyle().setSymbolIconEnabled(aIsSymbolIconEnabled);
    object.getAPP6AStyle().setSizeSymbol(aSize);
    object.getAPP6AStyle().setOffset(aOffsetX, aOffsetY);
    if (object.getTextModifier(ILcdAPP6ACoded.sMovementDirection) != null) {
      object.putTextModifier(ILcdAPP6ACoded.sMovementDirection, "45");
    }
    return object;
  }
}

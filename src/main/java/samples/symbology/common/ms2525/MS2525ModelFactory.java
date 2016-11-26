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

import static com.luciad.util.ILcdFireEventMode.NO_EVENT;

import samples.symbology.common.EMilitarySymbology;
import samples.symbology.common.ms2525.MS2525ModelDescriptor;
import samples.symbology.common.ms2525.StyledEditableMS2525Object;
import com.luciad.model.ILcdModel;
import com.luciad.model.TLcdVectorModel;
import com.luciad.reference.TLcdGeodeticReference;
import com.luciad.shape.shape2D.ILcd2DEditablePointList;
import com.luciad.symbology.milstd2525b.model.ELcdMS2525Standard;
import com.luciad.symbology.milstd2525b.model.ILcdMS2525bCoded;
import com.luciad.symbology.milstd2525b.model.TLcdEditableMS2525bObject;

/**
 * Creates a default MS2525-B model.
 */
public class MS2525ModelFactory {

  private final ELcdMS2525Standard fMS2525Standard;
  private final String fTypeName;
  private final String fDisplayName;

  public MS2525ModelFactory() {
    this(ELcdMS2525Standard.MIL_STD_2525b);
  }

  public MS2525ModelFactory(ELcdMS2525Standard aMS2525Standard) {
    fMS2525Standard = aMS2525Standard;
    fDisplayName = "MIL-STD 2525";
    fTypeName = aMS2525Standard.name();
  }

  public ILcdModel createModel() {
    TLcdVectorModel model = new TLcdVectorModel();

    // Model reference and descriptor
    model.setModelReference(new TLcdGeodeticReference());
    model.setModelDescriptor(new MS2525ModelDescriptor(null, fTypeName, fDisplayName, null, fMS2525Standard == ELcdMS2525Standard.MIL_STD_2525b ?
                                                                                            EMilitarySymbology.MILSTD_2525B : EMilitarySymbology.MILSTD_2525C));

    model.addElement(createStrongPoint(), NO_EVENT);
    model.addElement(createHelicopterAttack(), NO_EVENT);
    model.addElement(createGroundUnitsMainAttack(), NO_EVENT);
    model.addElement(createBattlePosition(), NO_EVENT);
    model.addElement(createSpecialOpsForce(), NO_EVENT);
    model.addElement(createNeutralHelicopter(), NO_EVENT);
    model.addElement(createNeutralAirplance(), NO_EVENT);
    model.addElement(createLightInfantry(), NO_EVENT);
    model.addElement(createInfantry(), NO_EVENT);
    model.addElement(createMechanizedInfantry(), NO_EVENT);
    model.addElement(createMedicalTheatre(), NO_EVENT);
    model.addElement(createMotorizedInfantry(), NO_EVENT);
    model.addElement(createGroundVehicle(), NO_EVENT);
    model.addElement(createWheeledArmor(), NO_EVENT);
    model.addElement(createAirport(), NO_EVENT);
    model.addElement(createCombat(), NO_EVENT);
    model.addElement(createEngineer(), NO_EVENT);
    model.addElement(createLightInfantry2(), NO_EVENT);
    model.addElement(createAttackRotaryWing(), NO_EVENT);
    model.addElement(createMilitaryFixedWing(), NO_EVENT);
    model.addElement(createCombatantLine(), NO_EVENT);
    model.addElement(createMineWarfareVessel(), NO_EVENT);
    model.addElement(createCruiser(), NO_EVENT);

    // Some symbols from the MIL-STD 2525b Change 1 2004 specification:
    model.addElement(createBioluminiscenceArea(), NO_EVENT);
    model.addElement(createDryDock(), NO_EVENT);
    model.addElement(createOilGasField(), NO_EVENT);
    model.addElement(createPipeline(), NO_EVENT);
    return model;
  }

  public TLcdEditableMS2525bObject createPipeline() {
    // Pipeline, represented as a polyline.
    String ms2525bCode = "WO-DMPA----L---";
    TLcdEditableMS2525bObject ms2525bObject = createStyledMS2525bObject(ms2525bCode);
    ILcd2DEditablePointList pointList = ms2525bObject.get2DEditablePointList();
    pointList.move2DPoint(0, 10.315, 44.14138);
    pointList.move2DPoint(1, 10.55027, 44.135);
    pointList.insert2DPoint(2, 10.63916, 44.02222);
    pointList.insert2DPoint(3, 10.99222, 43.9925);
    return ms2525bObject;
  }

  public TLcdEditableMS2525bObject createOilGasField() {
    // Oil/gas field, represented as an area.
    String ms2525bCode = "WO-DMOA-----A--";
    TLcdEditableMS2525bObject ms2525bObject = createStyledMS2525bObject(ms2525bCode);
    ILcd2DEditablePointList pointList = ms2525bObject.get2DEditablePointList();
    pointList.move2DPoint(0, 10.32861, 44.09055);
    pointList.move2DPoint(1, 10.14527, 44.01027);
    pointList.move2DPoint(2, 9.96777, 44.09944);
    pointList.insert2DPoint(3, 9.9725, 44.26555);
    pointList.insert2DPoint(4, 10.32861, 44.27111);
    return ms2525bObject;
  }

  public TLcdEditableMS2525bObject createDryDock() {
    // Dry dock, represented as an area.
    String ms2525bCode = "WO-DHPMD----A--";
    TLcdEditableMS2525bObject ms2525bObject = createStyledMS2525bObject(ms2525bCode);
    ILcd2DEditablePointList pointList = ms2525bObject.get2DEditablePointList();
    pointList.move2DPoint(0, 10.18638, 43.89472);
    pointList.move2DPoint(1, 10.30416, 43.88138);
    pointList.move2DPoint(2, 10.31638, 43.76972);
    pointList.insert2DPoint(3, 10.22333, 43.68194);
    return ms2525bObject;
  }

  public TLcdEditableMS2525bObject createBioluminiscenceArea() {
    // Bioluminiscence area, represented as an area.
    String ms2525bCode = "WO-DOBVC----A--";
    TLcdEditableMS2525bObject ms2525bObject = createStyledMS2525bObject(ms2525bCode);
    ILcd2DEditablePointList pointList = ms2525bObject.get2DEditablePointList();
    pointList.move2DPoint(0, 8.73861, 44.244722);
    pointList.move2DPoint(1, 8.63777, 44.27833);
    pointList.move2DPoint(2, 8.64527, 44.36166);
    pointList.insert2DPoint(3, 8.92055, 44.38583);
    pointList.insert2DPoint(4, 8.94027, 44.32111);
    pointList.insert2DPoint(5, 9.07944, 44.28472);
    pointList.insert2DPoint(6, 8.96638, 44.21555);
    pointList.insert2DPoint(7, 8.76138, 44.32888);
    return ms2525bObject;
  }

  public TLcdEditableMS2525bObject createCruiser() {
    // The 'Cruiser' (ship) symbol, represented as a point symbol (icon).
    String ms2525bCode = "SFSPCLCC-------";
    TLcdEditableMS2525bObject ms2525bObject = createStyledMS2525bObject(ms2525bCode, true, true, true, 50);
    ms2525bObject.move2DPoint(0, 9.328908011869435, 43.94177304233589);
    return ms2525bObject;
  }

  public TLcdEditableMS2525bObject createMineWarfareVessel() {
    // The 'Mine warfare vessel' (ship) symbol, represented as a point symbol (icon).
    String ms2525bCode = "SFSPCM---------";
    TLcdEditableMS2525bObject ms2525bObject = createStyledMS2525bObject(ms2525bCode, true, true, true, 50);
    ms2525bObject.move2DPoint(0, 9.096034286725052, 43.763097251646236);
    return ms2525bObject;
  }

  public TLcdEditableMS2525bObject createCombatantLine() {
    // The 'Combatant line' (ship) symbol, represented as a point symbol (icon).
    String ms2525bCode = "SFSPCL---------";
    TLcdEditableMS2525bObject ms2525bObject = createStyledMS2525bObject(ms2525bCode, true, true, true, 50);
    ms2525bObject.move2DPoint(0, 8.665219758859857, 43.87650593990823);
    return ms2525bObject;
  }

  public TLcdEditableMS2525bObject createMilitaryFixedWing() {
    // The 'Military fixed wing' (airplane) symbol, represented as a point symbol (icon).
    // The fill of the icon frame and the icon frame itself are turned off.
    String ms2525bCode = "SFAPMF---------";
    TLcdEditableMS2525bObject ms2525bObject = createStyledMS2525bObject(ms2525bCode, false, false, true, 64, -50, -50);
    ms2525bObject.move2DPoint(0, 7.459209530031675, 44.07233851527138);
    return ms2525bObject;
  }

  public TLcdEditableMS2525bObject createAttackRotaryWing() {
    // The 'Attack rotary wing' (helicopter) symbol, represented as a point symbol (icon).
    // The fill of the icon frame and the icon frame itself are turned off.
    String ms2525bCode = "SFAPMHA--------";
    TLcdEditableMS2525bObject ms2525bObject = createStyledMS2525bObject(ms2525bCode, false, false, true, 64, 50, 50);
    ms2525bObject.move2DPoint(0, 7.645212343292285, 43.947044424658586);
    return ms2525bObject;
  }

  public TLcdEditableMS2525bObject createLightInfantry2() {
    // The 'Infantry light' (unit) symbol, represented as a point symbol (icon).
    // The fill of the icon frame is turned off, resulting in an outlined icon.
    String ms2525bCode = "SFGPUCIL---E---";
    TLcdEditableMS2525bObject ms2525bObject = createStyledMS2525bObject(ms2525bCode, false, true, true, 50);
    ms2525bObject.move2DPoint(0, 7.453009436256322, 45.04312150501273);
    return ms2525bObject;
  }

  public TLcdEditableMS2525bObject createEngineer() {
    // The 'Engineer' (unit) symbol, represented as a point symbol (icon).
    // The fill of the icon frame is turned off, resulting in an outlined icon.
    String ms2525bCode = "SFGPUCE----B---";
    TLcdEditableMS2525bObject ms2525bObject = createStyledMS2525bObject(ms2525bCode, false, true, true, 50);
    ms2525bObject.move2DPoint(0, 7.814914613274074, 44.98109975858406);
    return ms2525bObject;
  }

  public TLcdEditableMS2525bObject createCombat() {
    // The 'Combat' (unit) symbol, represented as a point symbol (icon).
    // The fill of the icon frame is turned off, resulting in an outlined icon.
    String ms2525bCode = "SFGPUC-----M---";
    TLcdEditableMS2525bObject ms2525bObject = createStyledMS2525bObject(ms2525bCode, false, true, true, 50);
    ms2525bObject.move2DPoint(0, 7.735389671600583, 45.18911546645908);
    return ms2525bObject;
  }

  public TLcdEditableMS2525bObject createAirport() {
    // The 'Airport - airbase' symbol, represented as a point symbol (icon).
    String ms2525bCode = "SHGPIBA---H----";
    TLcdEditableMS2525bObject ms2525bObject = createStyledMS2525bObject(ms2525bCode, true, true, true, 50);
    ms2525bObject.move2DPoint(0, 10.69930451091407, 45.081254935220706);
    return ms2525bObject;
  }

  public TLcdEditableMS2525bObject createWheeledArmor() {
    // The 'Armor - wheeled' (unit) symbol, represented as a point symbol (icon).
    String ms2525bCode = "SHGPUCAW--AD---";
    TLcdEditableMS2525bObject ms2525bObject = createStyledMS2525bObject(ms2525bCode, false, true, true, 50);
    ms2525bObject.move2DPoint(0, 10.800536795252225, 44.74619298043526);
    return ms2525bObject;
  }

  public TLcdEditableMS2525bObject createGroundVehicle() {
    // The 'Ground vehicle' (unit) symbol, represented as a point symbol (icon).
    String ms2525bCode = "SHGPEV----MR---";
    TLcdEditableMS2525bObject ms2525bObject = createStyledMS2525bObject(ms2525bCode, false, true, true, 50);
    ms2525bObject.move2DPoint(0, 10.63561424332344, 44.60176907392433);
    return ms2525bObject;
  }

  public TLcdEditableMS2525bObject createMotorizedInfantry() {
    // The 'Infantry - motorized' (unit) symbol, represented as a point symbol (icon).
    String ms2525bCode = "SHGAUCIM---G---";
    TLcdEditableMS2525bObject ms2525bObject = createStyledMS2525bObject(ms2525bCode, false, true, true, 50);
    ms2525bObject.move2DPoint(0, 10.329852948020438, 44.81850538617699);
    return ms2525bObject;
  }

  public TLcdEditableMS2525bObject createMedicalTheatre() {
    // The 'Medical theatre' (unit) symbol, represented as a point symbol (icon).
    String ms2525bCode = "SUGPUSMT-------";
    TLcdEditableMS2525bObject ms2525bObject = createStyledMS2525bObject(ms2525bCode, true, true, true, 40);
    ms2525bObject.putTextModifier(ILcdMS2525bCoded.sAdditionalInformation, "B");
    ms2525bObject.move2DPoint(0, 9.285616498149238, 44.79643659661491);
    return ms2525bObject;
  }

  public TLcdEditableMS2525bObject createMechanizedInfantry() {
    // The 'Infantry - mechanized' (unit) symbol, represented as a point symbol (icon).
    String ms2525bCode = "SUGPUCIZ---K---";
    TLcdEditableMS2525bObject ms2525bObject = createStyledMS2525bObject(ms2525bCode, false, true, true, 40);
    ms2525bObject.putTextModifier(ILcdMS2525bCoded.sAdditionalInformation, "A");
    ms2525bObject.putTextModifier(ILcdMS2525bCoded.sSpeedLabel, "60km/h");
    ms2525bObject.move2DPoint(0, 8.56094419751349, 44.79643659661491);
    return ms2525bObject;
  }

  public TLcdEditableMS2525bObject createInfantry() {
    // The 'Infantry' (unit) symbol, represented as a point symbol (icon).
    String ms2525bCode = "SUGAUCI----F---";
    TLcdEditableMS2525bObject ms2525bObject = createStyledMS2525bObject(ms2525bCode, false, true, true, 40, 50, 50);
    ms2525bObject.putTextModifier(ILcdMS2525bCoded.sUniqueDesignation, "2");
    ms2525bObject.move2DPoint(0, 8.972032411217977, 44.6992329363481);
    return ms2525bObject;
  }

  public TLcdEditableMS2525bObject createLightInfantry() {
    // The 'Infantry - light' (unit) symbol, represented as a point symbol (icon).
    String ms2525bCode = "SUGPUCIL---D---";
    TLcdEditableMS2525bObject ms2525bObject = createStyledMS2525bObject(ms2525bCode, true, true, true, 40);
    ms2525bObject.putTextModifier(ILcdMS2525bCoded.sUniqueDesignation, "1");
    ms2525bObject.move2DPoint(0, 8.972032411217977, 44.941101478155985);
    return ms2525bObject;
  }

  public TLcdEditableMS2525bObject createNeutralAirplance() {
    // An airplane symbol, with neutral affiliation. The affiliation frame is turned off,
    // leaving only the internal icon.
    String ms2525bCode = "SNAPCF---------";
    TLcdEditableMS2525bObject ms2525bObject = createStyledMS2525bObject(ms2525bCode, true, false, true, 64);
    ms2525bObject.move2DPoint(0, 7.4220089673795515, 43.879812960459056);
    return ms2525bObject;
  }

  public TLcdEditableMS2525bObject createNeutralHelicopter() {
    // A helicopter symbol, with neutral affiliation. The affiliation frame is turned off,
    // leaving only the internal icon.
    String ms2525bCode = "SNAPCH---------";
    TLcdEditableMS2525bObject ms2525bObject = createStyledMS2525bObject(ms2525bCode, true, false, true, 64, -50, 0);
    ms2525bObject.move2DPoint(0, 7.2670066229957095, 43.987346590714985);
    return ms2525bObject;
  }

  public TLcdEditableMS2525bObject createSpecialOpsForce() {
    // A special operations force unit on ground symbol.
    // The forces are transported on a rail(e.g. by train);
    // the affiliation is set to Assumed Friend;
    // the status of the icon is Anticipated (or planned);
    // the pixel width is set to 50
    String ms2525bCode = "SAFAG-----MT---";
    TLcdEditableMS2525bObject ms2525bObject = createStyledMS2525bObject(ms2525bCode, true, true, true, 50);
    ms2525bObject.move2DPoint(0, 7.502932149785764, 44.84635100255543);
    return ms2525bObject;
  }

  public TLcdEditableMS2525bObject createBattlePosition() {
    // The 'Battle position' symbol, which is represented as a rounded area
    // with at least three points.
    String ms2525bCode = "GFGADAE-------X";
    TLcdEditableMS2525bObject ms2525bObject = createStyledMS2525bObject(ms2525bCode);
    ms2525bObject.putTextModifier(ILcdMS2525bCoded.sUniqueDesignation, "ALFA");
    ILcd2DEditablePointList pointList = ms2525bObject.get2DEditablePointList();
    pointList.move2DPoint(0, 8.585694817186367, 44.02235110003209);
    pointList.move2DPoint(1, 8.383823811399814, 43.70367890628065);
    pointList.move2DPoint(2, 9.21577704736864, 43.6504001169902);
    pointList.insert2DPoint(3, 9.246363563396905, 43.81452336857302);
    pointList.insert2DPoint(4, 9.595049846119132, 43.87208076415804);
    pointList.insert2DPoint(5, 9.460469175594765, 44.0576532307999);
    pointList.insert2DPoint(6, 9.166838621723414, 44.04441741380331);
    pointList.insert2DPoint(7, 8.879325371057718, 43.95168329967045);
    return ms2525bObject;
  }

  public TLcdEditableMS2525bObject createGroundUnitsMainAttack() {
    // The 'Ground units - main attack' symbol, represented as
    // a multipoint curved arrow with at least two points.
    String ms2525bCode = "GFGPOLAGM-----X";
    TLcdEditableMS2525bObject ms2525bObject = createStyledMS2525bObject(ms2525bCode);
    ms2525bObject.setWidth(7000); // in meters.
    ILcd2DEditablePointList pointList = ms2525bObject.get2DEditablePointList();
    pointList.move2DPoint(0, 8.01721796981351, 45.17483554290913);
    pointList.move2DPoint(1, 8.50702537806645, 45.056306677957345);
    pointList.insert2DPoint(2, 8.953432129891917, 45.27560909762362);
    pointList.insert2DPoint(3, 9.561041319876578, 45.078275166489966);
    pointList.insert2DPoint(4, 9.951647227723862, 45.209907611144736);
    pointList.insert2DPoint(5, 10.292652385368315, 45.10462613333734);
    return ms2525bObject;
  }

  public TLcdEditableMS2525bObject createHelicopterAttack() {
    // The 'Helicopter attack' symbol, represented as
    // a multipoint curved arrow with at least two points.
    String ms2525bCode = "GFGPOLAR------X";
    TLcdEditableMS2525bObject ms2525bObject = createStyledMS2525bObject(ms2525bCode);
    ms2525bObject.setWidth(7000); // in meters.
    ILcd2DEditablePointList pointList = ms2525bObject.get2DEditablePointList();
    pointList.move2DPoint(0, 7.7506139374732985, 44.023147622179884);
    pointList.move2DPoint(1, 8.192148068575053, 44.16985715034313);
    pointList.insert2DPoint(2, 8.619725136260778, 44.49999098960391);
    pointList.insert2DPoint(3, 8.988996240171174, 44.53707186016304);
    pointList.insert2DPoint(4, 9.397137986598457, 44.4025406627216);
    pointList.insert2DPoint(5, 9.928370100995872, 44.46288635979476);
    return ms2525bObject;
  }

  public TLcdEditableMS2525bObject createStrongPoint() {
    // The 'Strong point' symbol, which is represented as a rounded area
    // with at least three points. When the MIL-STD 2525b code is set on
    // the object, a minimum number of points is created. The most common
    // symbol types with their minimum number of points are:
    // - area symbols: 3 or more points.
    // - polyline symbols: 2 or more points.
    // - point symbols: 1 point.
    // Special point configurations are possible, depending on the symbol;
    // this is typically defined in the MIL-STD 2525b specification.
    // These predefined points can be moved directly, as illustrated in the code below.
    // If additional points are needed, they have to be inserted.
    String ms2525bCode = "GHMASP--------X";
    TLcdEditableMS2525bObject ms2525bObject = createStyledMS2525bObject(ms2525bCode);
    ILcd2DEditablePointList pointList = ms2525bObject.get2DEditablePointList();
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
    return ms2525bObject;
  }

  /**
   * Creates a new MIL-STD 2525b object, with an associated style that defines the rendering of the
   * symbol or its labels. <p/> The default domain object <code>TLcdEditableMS2525bObject</code>
   * does not have an associated style; for these objects, the default style that is set on the
   * painter provider and label painter provider is used. To have an associated style, a MIL-STD
   * 2525b object needs to implement ILcdMS2525bStyled, like <code>StyledMS2525bObject</code>.
   *
   * @param aMS2525bCode The symbol ID code.
   *
   * @return A new MIL-STD 2525b object, with an associated style.
   */
  private StyledEditableMS2525Object createStyledMS2525bObject(String aMS2525bCode) {
    // Styled objects.
    return new StyledEditableMS2525Object(aMS2525bCode, fMS2525Standard);
  }

  /**
   * Creates a new MIL-STD 2525b object, with an associated style that defines the rendering of the
   * symbol or its labels. <p/> The additional parameters specify a set of rendering properties that
   * need to be used in the symbol's associated style. The first three properties determine the
   * rendering of the icon, following the display options defined in the MIL-STD 2525b
   * specification; these properties are only applicable to framed icons. The size is applicable to
   * all icons.
   *
   * @param aMS2525bCode          The symbol ID code.
   * @param aIsSymbolFillEnabled  Sets whether the framed icon needs to be painted filled or
   *                              outlined.
   * @param aIsSymbolFrameEnabled Sets whether the affiliation frame of an icon needs to be
   *                              painted.
   * @param aIsSymbolIconEnabled  Sets whether the internal icon inside the affiliation frame needs
   *                              to be painted.
   * @param aSize                 Sets the size of the icon.
   *
   * @return A new MIL-STD 2525b object, with an associated style and the given style properties.
   */
  private StyledEditableMS2525Object createStyledMS2525bObject(String aMS2525bCode,
                                                               boolean aIsSymbolFillEnabled,
                                                               boolean aIsSymbolFrameEnabled,
                                                               boolean aIsSymbolIconEnabled,
                                                               int aSize) {

    return createStyledMS2525bObject(aMS2525bCode, aIsSymbolFillEnabled, aIsSymbolFrameEnabled, aIsSymbolIconEnabled, aSize, 0, 0);
  }

  /**
   * Creates a new MIL-STD 2525b object, with an associated style that defines the rendering of the
   * symbol or its labels. <p/> The additional parameters specify a set of rendering properties that
   * need to be used in the symbol's associated style. The first three properties determine the
   * rendering of the icon, following the display options defined in the MIL-STD 2525b
   * specification; these properties are only applicable to framed icons. The size is applicable to
   * all icons.
   *
   * @param aMS2525bCode          The symbol ID code.
   * @param aIsSymbolFillEnabled  Sets whether the framed icon needs to be painted filled or
   *                              outlined.
   * @param aIsSymbolFrameEnabled Sets whether the affiliation frame of an icon needs to be
   *                              painted.
   * @param aIsSymbolIconEnabled  Sets whether the internal icon inside the affiliation frame needs
   *                              to be painted.
   * @param aSize                 Sets the size of the icon.
   * @param aOffsetX              Sets the X offset of the icon
   * @param aOffsetY              Sets the Y offset of the icon
   *
   * @return A new MIL-STD 2525b object, with an associated style and the given style properties.
   */
  private StyledEditableMS2525Object createStyledMS2525bObject(String aMS2525bCode,
                                                               boolean aIsSymbolFillEnabled,
                                                               boolean aIsSymbolFrameEnabled,
                                                               boolean aIsSymbolIconEnabled,
                                                               int aSize,
                                                               int aOffsetX,
                                                               int aOffsetY) {
    // Styled objects.
    StyledEditableMS2525Object object = new StyledEditableMS2525Object(aMS2525bCode, fMS2525Standard);
    object.getMS2525bStyle().setSymbolFillEnabled(aIsSymbolFillEnabled);
    object.getMS2525bStyle().setSymbolFrameEnabled(aIsSymbolFrameEnabled);
    object.getMS2525bStyle().setSymbolIconEnabled(aIsSymbolIconEnabled);
    object.getMS2525bStyle().setSizeSymbol(aSize);
    object.getMS2525bStyle().setOffset(aOffsetX, aOffsetY);
    object.putTextModifier(ILcdMS2525bCoded.sMovementDirection, "45");
    return object;
  }
}

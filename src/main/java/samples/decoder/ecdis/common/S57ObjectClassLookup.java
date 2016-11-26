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
package samples.decoder.ecdis.common;

import static com.luciad.format.s52.ELcdS52DisplayCategory.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.luciad.format.s52.ELcdS52DisplayCategory;
import com.luciad.format.s57.ELcdS57ProductType;
import com.luciad.format.s57.TLcdS57ObjectClassMap;
import com.luciad.format.s57.TLcdS57ProductConfiguration;
import com.luciad.util.collections.TLcdIntArrayList;

/**
 * <p>Lookup containing all known S-57 object classes (and their respective codes) for all possible
 * {@link ELcdS57ProductType S-57 product types}.</p>
 *
 * <p>This lookup also contains some convenience methods like retrieving object classes as object class codes to avoid
 * additional boiler plate code (e.g. retrieve all object classes for a given display category).</p>
 */
public final class S57ObjectClassLookup {

  private static S57ObjectClassLookup sLookup;
  private static List<ObjectClass> sObjectClasses = new ArrayList<>();
  private static List<ObjectClass> sUniqueObjectClasses = new ArrayList<>();

  static {
    // Do this calculation once and cache the result for future usage
    for (ELcdS57ProductType productType : ELcdS57ProductType.values()) {
      addObjectClassesFor(productType, sObjectClasses);
    }

    sUniqueObjectClasses.addAll(filterNonUniqueEntries(sObjectClasses));
  }

  private final Map<ELcdS52DisplayCategory, List<String>> mapping = new HashMap<>();
  private final Map<Integer, List<ELcdS52DisplayCategory>> codeToCategoryMapping = new HashMap<>();
  private final LevelOfDetail levelOfDetail = new LevelOfDetail();

  /**
   * Retrieve a S-57 object class lookup instance. Note that this value is a singleton.
   *
   * @return the S-57 object class lookup
   */
  public static S57ObjectClassLookup getLookup() {
    if (sLookup == null) {
      sLookup = new S57ObjectClassLookup();
    }

    return sLookup;
  }

  private S57ObjectClassLookup() {
    List<String> displayBase = Arrays.asList("waypnt", "PONTON", "cursor", "ICEARE", "CONVYR", "BRIDGE", "PYLONS", "OILBAR", "DOCARE", "OFSPLF", "HULKES", "leglin", "COALNE", "CANALS", "RIVERS", "GATCON", "PILPNT", "DEPARE", "OBSTRN", "PIPOHD", "DAMCON", "MORFAC", "LNDARE", "UNSARE", "CBLOHD", "LOKBSN", "LOGPON", "ownshp", "DRGARE", "FLODOC", "SLCONS");
    List<String> other = Arrays.asList("SOUNDG", "CGUSTA", "RAILWY", "FSHZNE", "M_COVR", "FRPARE", "PRDARE", "RSCSTA", "FORSTC", "SLOGRD", "CHKPNT", "TIDEWY", "HRBARE", "RDOSTA", "RADSTA", "CRANES", "GATCON", "TS_TIS", "SMCFAC", "STSLNE", "LOCMAG", "CTRPNT", "RAPIDS", "WRECKS", "TESARE", "MAGVAR", "HRBFAC", "FNCLNE", "VEGATN", "T_HMON", "SPRING", "CBLSUB", "M_CSCL", "M_QUAL", "T_NHMN", "TS_FEB", "T_TIMS", "WATFAL", "M_NPUB", "ADMARE", "AIRARE", "SILTNK", "PIPSOL", "TUNNEL", "LNDMRK", "BUISGL", "WEDKLP", "COSARE", "DISMAR", "SLOTOP", "BERTHS", "OBSTRN", "MORFAC", "DEPCNT", "SBDARE", "ROADWY", "LNDELV", "GRIDRN", "FSHFAC", "CURENT", "UWTROC", "LOGPON", "WATTUR", "TS_PAD", "CONZNE", "EXEZNE", "CUSZNE", "TS_PRH", "RUNWAY", "DRYDOC", "TS_PNH");
    List<String> standard = Arrays.asList("CBLARE", "BOYCAR", "SPLARE", "NAVLNE", "RTPBCN", "PRDARE", "FORSTC", "RDOCAL", "FSHGRD", "BCNCAR", "ICNARE", "NEWOBJ", "RECTRC", "RETRFL", "CRANES", "GATCON", "SISTAW", "ISTZNE", "SISTAT", "SNDWAV", "RADRFL", "DYKCON", "DAMCON", "BCNLAT", "PRCARE", "TOPMAR", "TSSRON", "FNCLNE", "SUBTLN", "BCNISD", "RADLNE", "DWRTCL", "CBLSUB", "RESARE", "LIGHTS", "LNDRGN", "ACHARE", "LITFLT", "BCNSPP", "BOYSPP", "FERYRT", "FAIRWY", "TWRTPT", "RADRNG", "DAYMAR", "BOYISD", "TSSBND", "AIRARE", "SILTNK", "PILBOP", "TSELNE", "TUNNEL", "ACHBRT", "TSSLPT", "SWPARE", "FOGSIG", "RCRTCL", "LNDMRK", "DMPGRD", "OSPARE", "ARCSLN", "BUISGL", "SEAARE", "BCNSAW", "SLOTOP", "LITVES", "M_NSYS", "MORFAC", "BOYINB", "MARCUL", "TSEZNE", "BOYSAW", "######", "DWRTPT", "CTNARE", "ASLXIS", "TSSCRS", "LAKARE", "MIPARE", "CAUSWY", "BUAARE", "PIPARE", "BOYLAT", "RUNWAY", "CTSARE", "RCTLPT");

    mapping.put(DISPLAY_BASE, displayBase);
    mapping.put(STANDARD, standard);
    mapping.put(OTHER, other);

    List<ObjectClass> allObjectClasses = getUniqueObjectClasses();
    populateCodeToCategoryMap(doGetObjectClassCodesList(DISPLAY_BASE, allObjectClasses));
    populateCodeToCategoryMap(doGetObjectClassCodesList(STANDARD, allObjectClasses));
    populateCodeToCategoryMap(doGetObjectClassCodesList(OTHER, allObjectClasses));
  }

  /**
   * Returns all objects classes for all {@link ELcdS57ProductType S-57 product types}. Do note that there exists
   * overlap between the different product types. Use {@link #getUniqueObjectClasses()} to avoid double entries.
   *
   * @return all possible object class from all possible product types
   */
  public ObjectClass[] getObjectClasses() {
    return sObjectClasses.toArray(new ObjectClass[sObjectClasses.size()]);
  }

  /**
   * Returns all object classes for all {@link ELcdS57ProductType S-57 product types}. This method makes sure that
   * overlapping object classes between different S-57 product types are only added once.
   *
   * @return all object classes from all product types
   */
  public List<ObjectClass> getUniqueObjectClasses() {
    return new ArrayList<>(sUniqueObjectClasses); // Ensure internal representation remains immutable
  }

  /**
   * Returns all existing object class codes for all {@link ELcdS57ProductType S-57 product types}. This method makes
   * sure that overlapping object classes between different S-57 product types are only added once.
   *
   * @return all unique object class codes
   */
  public TLcdIntArrayList getUniqueObjectClassCodes() {
    TLcdIntArrayList result = new TLcdIntArrayList(sUniqueObjectClasses.size());
    for (ObjectClass objectClass : sUniqueObjectClasses) {
      result.add(objectClass.getCode());
    }

    return result;
  }

  /**
   * Returns a list containing the object class codes of object classes that should be visible for the given display
   * category.
   *
   * @param aDisplayCategory the display category
   * @return all object class codes of object class that should be visible for the given category. This includes the
   * object class codes of less detailed display categories
   */
  public TLcdIntArrayList getObjectClassCodesList(ELcdS52DisplayCategory aDisplayCategory) {
    List<ObjectClass> allObjectClasses = getUniqueObjectClasses();
    return doGetObjectClassCodesList(aDisplayCategory, allObjectClasses);
  }

  /**
   * Returns an array containing the object class codes of object classes that should be visible for the given display
   * category.
   *
   * @param aDisplayCategory the display category
   * @return all object class codes of object class that should be visible for the given category. This includes the
   * object class codes of less detailed categories
   */
  public int[] getObjectClassCodes(ELcdS52DisplayCategory aDisplayCategory) {
    TLcdIntArrayList result = getObjectClassCodesList(aDisplayCategory);
    return result.toIntArray();
  }

  /**
   * Checks whether the given category contains the given object code.
   *
   * @param aCategory the display category
   * @param aCode the object class code
   * @return {@code true} if it contains the object class code, {@code false} otherwise
   */
  public boolean categoryContains(ELcdS52DisplayCategory aCategory, int aCode) {
    return codeToCategoryMapping.containsKey(aCode) && codeToCategoryMapping.get(aCode).contains(aCategory);
  }

  private TLcdIntArrayList doGetObjectClassCodesList(ELcdS52DisplayCategory aDisplayCategory, List<ObjectClass> aAllObjectClasses) {
    TLcdIntArrayList result = new TLcdIntArrayList();

    for (ELcdS52DisplayCategory displayCategory : levelOfDetail.getLevels(aDisplayCategory)) {
      doGetClassCodesSFCT(displayCategory, aAllObjectClasses, result);
    }

    return result;
  }

  private void doGetClassCodesSFCT(ELcdS52DisplayCategory aDisplayCategory, List<ObjectClass> aAllObjectClasses,
                                   TLcdIntArrayList aResultSFCT) {
    List<String> mappedObjectClasses = mapping.get(aDisplayCategory);
    for (String objectClass : mappedObjectClasses) {
      for (ObjectClass candidateObjectClass : aAllObjectClasses) {
        if (candidateObjectClass.getString().equalsIgnoreCase(objectClass)) {
          aResultSFCT.add(candidateObjectClass.getCode());
        }
      }
    }
  }

  private void populateCodeToCategoryMap(TLcdIntArrayList aObjectClassCodes) {
    for (Integer code : aObjectClassCodes) {
      if (!codeToCategoryMapping.containsKey(code)) {
        codeToCategoryMapping.put(code, new ArrayList<ELcdS52DisplayCategory>());
      }

      List<ELcdS52DisplayCategory> displayCategories = codeToCategoryMapping.get(code);
      for (ELcdS52DisplayCategory category : ELcdS52DisplayCategory.values()) {
        if (getObjectClassCodesList(category).containsInt(code) && !displayCategories.contains(category)) {
          displayCategories.add(category);
        }
      }
    }
  }

  private static List<ObjectClass> filterNonUniqueEntries(List<ObjectClass> aObjectClasses) {
    ArrayList<ObjectClass> result = new ArrayList<>();
    Map<Integer, ObjectClass> registry = new HashMap<>(aObjectClasses.size());
    for (ObjectClass objectClass : aObjectClasses) {
      if (!registry.containsKey(objectClass.getCode())) { // Don't add object classes twice (ENC, AML, ...)
        registry.put(objectClass.getCode(), objectClass);
        result.add(objectClass);
      } else {
        ObjectClass mappedObjectClass = registry.get(objectClass.getCode());
        mappedObjectClass.addProductTypes(objectClass.getProductTypes());
      }
    }

    return result;
  }

  private static void addObjectClassesFor(ELcdS57ProductType aProductType, List<ObjectClass> aObjectClassesSFCT) {
    TLcdS57ProductConfiguration productConfiguration = TLcdS57ProductConfiguration.getInstance(aProductType);
    TLcdS57ObjectClassMap objectClassMap = productConfiguration.getObjectClassMap();
    List<Integer> objectClassCodes = objectClassMap.getObjectClasses();
    for (int objectClassCode : objectClassCodes) {
      String objectClassString = objectClassMap.getStringFromObjectClass(objectClassCode);
      aObjectClassesSFCT.add(new ObjectClass(aProductType, objectClassCode, objectClassString));
    }
  }

  private final class LevelOfDetail {

    private final List<ELcdS52DisplayCategory> levelOfDetail = Arrays.asList(DISPLAY_BASE, STANDARD, OTHER);

    /**
     * Returns a list of all display categories containing less detail than the given one. This can for instance be
     * used to determine the visible object classes for a given display category. The given display category is also
     * included in the result.
     *
     * @param aMostDetailedLevel the most detailed level
     * @return the display categories containing less details than the given display category, including the given
     * category
     */
    public List<ELcdS52DisplayCategory> getLevels(ELcdS52DisplayCategory aMostDetailedLevel) {
      int detail = levelOfDetail.indexOf(aMostDetailedLevel);

      if (detail < 0) {
        return Collections.emptyList();
      }

      return levelOfDetail.subList(0, detail + 1);
    }
  }
}

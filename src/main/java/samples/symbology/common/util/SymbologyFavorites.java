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
package samples.symbology.common.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import samples.symbology.common.EMilitarySymbology;
import com.luciad.util.TLcdPair;
import com.luciad.util.collections.ILcdCollection;
import com.luciad.util.collections.ILcdCollectionListener;
import com.luciad.util.collections.TLcdArrayList;
import com.luciad.util.collections.TLcdCollectionEvent;

/**
 * An observable collection of often-used symbols.
 * A toolbar could use this collection to display the symbols for rapid creation.
 * A symbol customizer panel could use it to mark the symbol as a favorite.
 */
public class SymbologyFavorites {

  private ILcdCollection<TLcdPair<EMilitarySymbology, String>> fFavorites = new TLcdArrayList<>();
  private Map<EMilitarySymbology, ILcdCollection<String>> fSymbology2Favorites = new HashMap<>();

  public ILcdCollection<String> get(EMilitarySymbology aMilitarySymbology) {
    ILcdCollection<String> filteredFavs = fSymbology2Favorites.get(aMilitarySymbology);
    if (filteredFavs == null) {
      filteredFavs = new TLcdArrayList<>();
      fSymbology2Favorites.put(aMilitarySymbology, filteredFavs);
      filteredFavs.addCollectionListener(new CollectionListener());
    }
    return filteredFavs;
  }

  public ILcdCollection<TLcdPair<EMilitarySymbology, String>> get() {
    return fFavorites;
  }

  /**
   * Loads the collection using the Java preferences mechanism.
   */
  public void load() {
    Preferences preferences = Preferences.userNodeForPackage(getClass());
    String favoritesString = new String(preferences.getByteArray("favorites", new byte[0]));
    fFavorites.clear();
    Pattern pattern = Pattern.compile("([^,\\.]+)\\.([^,\\.]+)");
    Matcher matcher = pattern.matcher(favoritesString);
    while (matcher.find()) {
      try {
        EMilitarySymbology symbology = EMilitarySymbology.valueOf(matcher.group(1));
        String sidc = matcher.group(2);
        get(symbology).add(sidc);
      } catch (IllegalArgumentException e) {
        throw new RuntimeException("Could not restore favorites", e);
      }
    }
  }

  /**
   * Saves the collection using the Java preferences mechanism.
   */
  public void save() {
    Preferences preferences = Preferences.userNodeForPackage(getClass());
    StringBuilder sb = new StringBuilder();
    for (TLcdPair<EMilitarySymbology, String> pair : fFavorites) {
      String sidc = pair.getValue();
      if (sb.length() > 0) {
        sb.append(",");
      }
      sb.append(pair.getKey().name());
      sb.append(".");
      sb.append(sidc);
    }
    // String needs to be saved as a byte array because java can't handle unicode characters
    preferences.putByteArray("favorites", sb.toString().getBytes());
    try {
      preferences.flush();
    } catch (BackingStoreException e) {
      throw new RuntimeException("Could not store favorites", e);
    }
  }

  private class CollectionListener implements ILcdCollectionListener<String> {

    @Override
    public void collectionChanged(TLcdCollectionEvent<String> aCollectionEvent) {
      ILcdCollection<String> source = aCollectionEvent.getSource();
      EMilitarySymbology symbology = getListSymbology(source);
      switch (aCollectionEvent.getType()) {
      case ELEMENT_ADDED:
        fFavorites.add(new TLcdPair<>(symbology, aCollectionEvent.getElement()));
        break;
      case ELEMENT_REMOVED:
        fFavorites.remove(new TLcdPair<>(symbology, aCollectionEvent.getElement()));
        break;
      }
    }

    private EMilitarySymbology getListSymbology(ILcdCollection<String> aSource) {
      Set<EMilitarySymbology> symbologies = fSymbology2Favorites.keySet();
      for (EMilitarySymbology sym : symbologies) {
        //don't use equals because we want an exact match of the lists.
        if (fSymbology2Favorites.get(sym) == aSource) {
          return sym;
        }
      }
      //should not be happened
      throw new RuntimeException("Could not find list in symbology favorites " + aSource);
    }
  }
}

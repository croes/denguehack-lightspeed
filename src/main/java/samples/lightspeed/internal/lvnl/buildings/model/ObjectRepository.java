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
package samples.lightspeed.internal.lvnl.buildings.model;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.luciad.view.lightspeed.painter.mesh.ILsp3DIcon;

/**
 * Date: Jan 25, 2007
 * Time: 8:56:00 AM
 *
 * @author Tom Nuydens
 */
public class ObjectRepository {
  private Map<String, File> fObjectNameToFile;
  private Map<String, ILsp3DIcon> fFileNameToIcon;
  private Map<String, ILsp3DIcon> fObjectNameToIcon;

  private File fDefaultObjectPath;
  private List<ObjectFormatSupport> fObjectFormatSupports;

  public ObjectRepository() {
    this(new Properties(), new File(System.getProperty("user.dir")), false);
  }

  public ObjectRepository(Properties aObjectMap, File aDefaultObjectPath, boolean aPreloadTextures) {
    if (!aDefaultObjectPath.isDirectory() || !aDefaultObjectPath.exists()) {
      throw new IllegalArgumentException("Default object path is not a directory or does not exist.");
    }

    fDefaultObjectPath = aDefaultObjectPath.getAbsoluteFile();

    fObjectFormatSupports = new ArrayList<ObjectFormatSupport>();
    fObjectFormatSupports.add(new OpenFlightObjectFormatSupport(aPreloadTextures));
    //fObjectFormatSupports.add(new XPlaneObjectFormatSupport());
    //fObjectFormatSupports.add(new WavefrontFormatSupport());

    fObjectNameToFile = new HashMap<String, File>();
    fObjectNameToIcon = new HashMap<String, ILsp3DIcon>();
    fFileNameToIcon = new HashMap<String, ILsp3DIcon>();

    for (Map.Entry<Object, Object> entry : aObjectMap.entrySet()) {
      String objectName = (String) entry.getKey();
      String fileName = (String) entry.getValue();
      if (!isAlias(fileName)) {
        addObjectMapping(objectName, new File(fileName));
      }
    }

    for (Map.Entry<Object, Object> entry : aObjectMap.entrySet()) {
      String objectName = (String) entry.getKey();
      String fileName = (String) entry.getValue();
      if (isAlias(fileName)) {
        String alias = resolveAlias(fileName);
        if (fObjectNameToFile.containsKey(alias)) {
          addObjectMapping(objectName, fObjectNameToFile.get(alias));
        } else {
          throw new RuntimeException("Missing alias: " + objectName + "=" + fileName);
        }
      }
    }
  }

  private void addObjectMapping(String aObjectName, File aFile) {
    File objectFile;
    if (aFile.isAbsolute()) {
      objectFile = aFile;
    } else {
      objectFile = new File(fDefaultObjectPath, aFile.getPath());
    }

    if (!objectFile.exists()) {
      throw new RuntimeException("Cannot map " + aObjectName + " to non-existant file " + aFile);
    } else {
      fObjectNameToFile.put(aObjectName, objectFile);
    }
  }

  private String resolveAlias(String aValue) {
    return aValue.substring(1);
  }

  private boolean isAlias(String aValue) {
    return aValue.startsWith("@");
  }

  public void addFormatSupport(ObjectFormatSupport aObjectFormatSupport) {
    if (aObjectFormatSupport != null) {
      fObjectFormatSupports.add(aObjectFormatSupport);
    }
  }

  public void preloadIcons(HierarchicalProgressListener aProgressListener) {
    float progress = 0f;
    float progressPerFile = 1.0f / fObjectNameToFile.size();

    for (String objectName : fObjectNameToFile.keySet()) {
      if (aProgressListener != null) {
        aProgressListener.setProgress(objectName, progress);
      }

      getIcon(objectName);
      progress += progressPerFile;
    }

    if (aProgressListener != null) {
      aProgressListener.setProgress("", 1f);
    }
  }

  private ILsp3DIcon getIcon(String aObjectName) {
    if (!fObjectNameToIcon.containsKey(aObjectName)) {
      if (!fObjectNameToFile.containsKey(aObjectName)) {
        fObjectNameToIcon.put(aObjectName, null);
        return null;
      } else {
        ILsp3DIcon icon;
        try {
          icon = loadIcon(fObjectNameToFile.get(aObjectName));
        } catch (IOException e) {
          icon = null;
        }
        fObjectNameToIcon.put(aObjectName, icon);
        return icon;
      }
    } else {
      return fObjectNameToIcon.get(aObjectName);
    }
  }

  public String getIconSource(String aObject) {
    File file = fObjectNameToFile.get(aObject);
    return file != null ? file.getPath() : null;
  }

  public ILsp3DIcon loadIcon(File aObjectFile) throws IOException {
    String absoluteObjectPath = aObjectFile.getAbsolutePath();
    if (fFileNameToIcon.containsKey(absoluteObjectPath)) {
      return fFileNameToIcon.get(absoluteObjectPath);
    } else {

      ObjectFormatSupport format = null;
      for (ObjectFormatSupport objectFormat : fObjectFormatSupports) {
        if (objectFormat.canDecodeObject(aObjectFile)) {
          format = objectFormat;
          break;
        }
      }

      if (format != null) {
        try {
          return format.decodeObject(aObjectFile);
        } catch (IOException e) {
          throw e;
        }
      } else {
        throw new IOException("File has unknown format (" + aObjectFile.getAbsolutePath() + ")");
      }
    }
  }

  public boolean canGet3DIcon(Object object) {
    return get3DIcon(object) != null;
  }

  public ILsp3DIcon get3DIcon(Object object) {
    if (object != null) {
      if (object instanceof String) {
        return getIcon(((String) object));
      } else {
        return getIcon(object.toString());
      }
    } else {
      return null;
    }
  }
}

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
package samples.wms.server.updater;

import java.util.Iterator;
import java.util.List;

/**
 * This class represents a change in a directory: it can hold an array of file lists, which are added, removed or updated.
 */
public class DirectoryMonitorEvent {

  public static final int FILES_ADDED = 0;
  public static final int FILES_REMOVED = 1;
  public static final int FILES_UPDATED = 2;

  private List[] fFiles;
  private int[] fType;

  /**
   * Creates a new DirectoryMonitorEvent with the given parameters.
   *
   * @param aFiles An array of file lists to which the event applies.
   * @param aType  An array of event types (FILES_ADDED, FILES_REMOVED or FILES_UPDATED) that apply to the array of file lists.
   */
  public DirectoryMonitorEvent(List[] aFiles, int[] aType) {
    fFiles = aFiles.clone();
    fType = aType.clone();
  }

  /**
   * Returns the array of file lists of this event.
   * The event types corresponding to these file lists can be retrieved through {@link #getTypes() getTypes()}.
   *
   * @return the array of file lists of this event.
   */
  public final List[] getFiles() {
    return fFiles.clone();
  }

  /**
   * Returns the array of event types of this event.
   * The file lists to which these event types correspond can be retrieved through {@link #getFiles() getFiles()}.
   *
   * @return the array of event types of this event.
   */
  public final int[] getTypes() {
    return fType.clone();
  }

}

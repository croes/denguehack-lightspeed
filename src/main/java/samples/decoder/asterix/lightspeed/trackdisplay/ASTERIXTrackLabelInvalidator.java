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
package samples.decoder.asterix.lightspeed.trackdisplay;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.WeakHashMap;

import com.luciad.format.asterix.TLcdASTERIXTrack;

/**
 * Extension of TrackLabelInvalidator that makes sure that labels are only invalidated when strictly needed.
 */
class ASTERIXTrackLabelInvalidator {

  // Stores the content that is currently displayed for every object. When the model is changed, the new
  // content is compared with the old content, and only when it is different, we invalidate the label.
  private final Map<TLcdASTERIXTrack, String[]> fLabelContentMap = new WeakHashMap<>();
  private final Map<TLcdASTERIXTrack, String> fCommentsMap = new WeakHashMap<>();

  // The actual map of comments. This map is modified outside this class, so we need an other map to track
  // changes, see fCommentsMap;
  private final ASTERIXTrackAdditionalData fAdditionalData;

  public ASTERIXTrackLabelInvalidator(ASTERIXTrackAdditionalData aAdditionalData) {
    fAdditionalData = aAdditionalData;
  }

  public boolean shouldInvalidateLabel(Object aObject, boolean aShowExtraInfo) {
    if (!(aObject instanceof TLcdASTERIXTrack)) {
      throw new IllegalArgumentException("Object should be a TLcdASTERIXTrack");
    }
    TLcdASTERIXTrack track = (TLcdASTERIXTrack) aObject;
    String[] oldContent = fLabelContentMap.get(aObject);
    String[] newContent = ASTERIXTrackLabelContentProvider.provideContent(track, aShowExtraInfo);
    String oldComment = fCommentsMap.get(aObject);
    String newComment = fAdditionalData.getComment(aObject);
    if (!Arrays.equals(oldContent, newContent) || !Objects.equals(oldComment, newComment)) {
      fLabelContentMap.put(track, newContent);
      fCommentsMap.put(track, newComment);
      return true;
    }
    return false;
  }
}

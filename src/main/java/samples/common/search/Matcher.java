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
package samples.common.search;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Matcher that uses each word (separated by space) to search in a AND fashion.
 * <p/>
 * Copyright (c) 2012, Lennart Schedin All rights reserved.
 * <p/>
 * Redistribution and use in source and binary forms, with or without modification, are permitted
 * provided that the following conditions are met:
 * <p/>
 * Redistributions of source code must retain the above copyright notice, this list of conditions
 * and the following disclaimer. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution. Neither the name of the organization nor the names of
 * its contributors may be used to endorse or promote products derived from this software without
 * specific prior written permission.
 * <p/>
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY
 * WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
public class Matcher {

  private boolean fCaseSensitive;

  public void setCaseSensitive(boolean caseSensitive) {
    fCaseSensitive = caseSensitive;
  }

  public boolean matches(String searchString, String matchCandidate) {

    if (!fCaseSensitive) {
      searchString = searchString.toLowerCase();
      matchCandidate = matchCandidate.toLowerCase();
    }
    String[] words = searchString.split(" ");

    if (words.length == 0) {
      return false;
    }

    for (String word : words) {
      if (!matchCandidate.contains(word)) {
        return false;
      }
    }
    return true;
  }

  /**
   * <p>
   *   Creates a {@code Comparator} which can be used to sort matches on their importance.
   *   For example, if you want to show an exact match above the partial matches, you
   *   can use this comparator
   * </p>
   *
   * @param searchString The search string
   * @return A comparator.
   *         Exact matches will be considered "less than" partial matches.
   */
  public Comparator<String> createMatchComparator(final String searchString) {
    return new Comparator<String>() {
      @Override
      public int compare(String first, String second) {
        return Double.compare(calculateScore(second), calculateScore(first));
      }

      private double calculateScore(String input) {
        if (input == null || input.isEmpty()) {
          return 0;
        }
        int[][] area = matchArea(searchString, input);
        if (area == null) {
          return 0;
        }
        double matchingCharacters = 0;
        for (int[] range : area) {
          matchingCharacters += range[1] - range[0];
        }
        return matchingCharacters / (double) input.length();
      }
    };
  }

  public int[][] matchArea(String searchString, String matchCandidate) {
    String[] words = searchString.split(" ");

    List<Range> matches = new ArrayList<Range>();

    int caseSensitiveFlags = 0;
    if (!fCaseSensitive) {
      caseSensitiveFlags = Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE;
    }

    for (String word : words) {
      boolean hasMatchedWord = matchWord(matchCandidate, matches, caseSensitiveFlags, word);
      if (!hasMatchedWord) {
        return null; //Every word must be matched
      }
    }

    return createMatchArray(matches);
  }

  private boolean matchWord(String matchCandidate, List<Range> matches,
                            int caseSensitiveFlags, String word) {
    Pattern pattern = Pattern.compile(".*?(" + Pattern.quote(word) + ").*?", caseSensitiveFlags);
    java.util.regex.Matcher matcher = pattern.matcher(matchCandidate);

    boolean hasMatched = false;

    while (matcher.find()) {
      hasMatched = true;
      matches.add(new Range(matcher.start(1), matcher.end(1)));
    }
    return hasMatched;
  }

  private static class Range implements Comparable<Range> {
    final int from;
    final int to;

    Range(int from, int to) {
      this.from = from;
      this.to = to;
    }

    /**
     * Joins two ranges. Should only be called if the ranges intersects
     */
    static Range join(Range a1, Range a2) {
      int lowerBounds = Math.min(a1.from, a2.from);
      int upperBounds = Math.max(a1.to, a2.to);

      return new Range(lowerBounds, upperBounds);
    }

    boolean intersects(Range o) {
      if (inside(o.from) || inside(o.to) || o.inside(from) || o.inside(to)) {
        return true;
      }

      return false;
    }

    boolean inside(int point) {
      if (point >= from && point <= to) {
        return true;
      } else {
        return false;
      }
    }

    @Override
    public String toString() {
      return "[" + from + ", " + to + "]";
    }

    @Override
    public int compareTo(Range o) {
      return Integer.valueOf(from).compareTo(o.from);
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + from;
      result = prime * result + to;
      return result;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      }
      if (obj == null) {
        return false;
      }
      if (getClass() != obj.getClass()) {
        return false;
      }
      Range other = (Range) obj;
      if (from != other.from) {
        return false;
      }
      if (to != other.to) {
        return false;
      }
      return true;
    }

  }

  private static int[][] createMatchArray(List<Range> matches) {
    Collections.sort(matches);

    matches = compress(matches);

    int[][] matchArray = new int[matches.size()][];
    for (int i = 0; i < matchArray.length; i++) {
      Range matchArea = matches.get(i);
      matchArray[i] = new int[2];
      matchArray[i][0] = matchArea.from;
      matchArray[i][1] = matchArea.to;
    }
    return matchArray;
  }

  /**
   * Compresses (a sorted list) if some ranges overlap
   */
  private static List<Range> compress(List<Range> matches) {
    ArrayList<Range> compressedList = new ArrayList<Range>();

    Range addCandidate = null;

    for (Range current : matches) {

      if (addCandidate != null) {
        if (addCandidate.intersects(current)) {
          addCandidate = Range.join(current, addCandidate);
          //Wait to add this, since more joins might come later
        } else {
          compressedList.add(addCandidate); //Since there is no intersection the add candidate can be added
          addCandidate = current;
        }
      } else {
        addCandidate = current;
      }
    }

    //Always add the last addCandidate since there are nor more to compare with
    if (addCandidate != null) {
      compressedList.add(addCandidate);
    }

    return compressedList;
  }

}

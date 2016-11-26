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
package samples.symbology.common;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

/**
 * Allows converting the all-capital strings from the military specifications to human-readable display names.
 */
public class CamelCaseConverter {

  private static final Set<String> KEEP_LOWER_CASE;
  private static final Set<String> KEEP_UPPER_CASE;

  static {
    KEEP_LOWER_CASE = new HashSet<String>(Arrays.asList(
        "and", "or", "to", "for", "in", "than", "by"
    ));

    KEEP_UPPER_CASE = new HashSet<String>(Arrays.asList(
        "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X",
        "AAM", "ABM", "ACA", "ACV", "ACP", "AD", "AERV", "AEW", "AO", "AOU", "AP", "APOD", "APOE", "ASM", "ASP", "ASUW", "ASW", "AT", "ATI", "ATP",
        "BKB", "BAS", "BT",
        "CAP", "CASS", "CATK", "CCP", "CFFZ", "CFL", "CFZ", "CID", "CSAR", "C2", "C2V", "C3I",
        "DA", "DICASS", "DIFAR", "DSA",
        "ECM", "EPW", "EZ",
        "FAADEZ", "FARP", "FEBA", "FFA", "FLOT", "FPF", "FSA", "FSCL",
        "HAZMAT", "HIDACZ",
        "JAG", "JIB",
        "LAR", "LC", "LCCP", "LD", "LLTR", "LZ", "LOFAR", "LP", "LRP", "LRS",
        "MAD", "MCC", "MEDEVAC", "MEZ", "MFP", "MILEC", "MILCO", "MIW", "MPA", "MRR", "MWR",
        "NAI", "NFA", "NFL",
        "OP",
        "PAA", "PDF", "PIM", "PKB", "PLD", "PSYOP", "PUP", "PZ",
        "RFA", "RFL", "RHU", "RIP", "RO", "ROM", "ROZ", "RPV", "RMV", "RSA",
        "SAAFR", "SHORADEZ", "SIGINT", "SOF", "SP", "SPOD", "SPOE", "STOL", "SAM", "SS", "SSB", "SSBN", "SSG", "SSGN", "SSM", "SSN", "SURF", "SUV", "SUW",
        "TAI", "TBA", "TBM", "TCP", "TELAR", "TGMF", "TLAR", "TRP", "TVAR",
        "UA", "UAS", "UAV", "USV", "UUV", "UXO",
        "VIP", "VLAD", "VSTOL",
        "ZOR",
        "TF", "HQ", "FD", "MEF" // echelon
    ));
  }

  public static String toTitleCase(String aVal) {
    StringBuilder newToken = new StringBuilder();
    StringTokenizer tok = new StringTokenizer(aVal, " -/()", true);

    while (tok.hasMoreTokens()) {
      String token = tok.nextToken();

      if (token.length() != 1 && !KEEP_UPPER_CASE.contains(token)) {
        token = token.toLowerCase();
        if (!KEEP_LOWER_CASE.contains(token)) {
          StringBuilder b = new StringBuilder(token);
          b.setCharAt(0, Character.toUpperCase(b.charAt(0)));
          token = b.toString();
        }
      }
      newToken.append(token);
    }
    return newToken.toString();
  }

}

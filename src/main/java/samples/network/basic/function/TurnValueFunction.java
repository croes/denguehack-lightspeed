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
package samples.network.basic.function;

import java.util.Hashtable;

import com.luciad.network.function.ALcdTurnValueFunction;
import com.luciad.network.graph.ILcdGraph;
import com.luciad.network.graph.TLcdTraversalDirection;

/**
 * An <code>ILcdEdgeValueFunction</code> that can store and return a value
 * with a turn.
 */
public class TurnValueFunction extends ALcdTurnValueFunction {

  private double fDefaultValue;
  private Hashtable fTurnTable = new Hashtable();

  public TurnValueFunction(double aDefaultValue) {
    fDefaultValue = aDefaultValue;
  }

  public double computeTurnValue(ILcdGraph aGraph,
                                 Object aPreviousEdge,
                                 Object aNode,
                                 Object aNextEdge, TLcdTraversalDirection aTraversalDirection) {

    TurnEntry entry = new TurnEntry(aPreviousEdge, aNode, aNextEdge);
    try {
      return ((Double) fTurnTable.get(entry)).doubleValue();
    } catch (NullPointerException e) {
      return fDefaultValue;
    }
  }

  public void setTurnValue(Object aPreviousEdge,
                           Object aNode,
                           Object aNextEdge,
                           double aValue) {
    TurnEntry entry = new TurnEntry(aPreviousEdge, aNode, aNextEdge);
    fTurnTable.put(entry, new Double(aValue));
  }

  class TurnEntry {
    private Object fPreviousEdge;
    private Object fNode;
    private Object fNextEdge;

    private int fHashCode;

    public TurnEntry(Object aPreviousEdge, Object aNode, Object aNextEdge) {
      fPreviousEdge = aPreviousEdge;
      fNode = aNode;
      fNextEdge = aNextEdge;
      computeHashCode();
    }

    public boolean equals(Object aObject) {
      if (!(aObject instanceof TurnEntry)) {
        return false;
      }

      TurnEntry other = (TurnEntry) aObject;

      return this.fPreviousEdge == other.fPreviousEdge
             && this.fNode == other.fNode
             && this.fNextEdge == other.fNextEdge;
    }

    public int hashCode() {
      return fHashCode;
    }

    private void computeHashCode() {
      fHashCode = 11 * fPreviousEdge.hashCode()
                  + 17 * fNode.hashCode()
                  + 23 * fNextEdge.hashCode();
    }

  }
}

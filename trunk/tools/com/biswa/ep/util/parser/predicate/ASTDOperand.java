/* Generated By:JJTree: Do not edit this line. ASTDOperand.java */

package com.biswa.ep.util.parser.predicate;

public class ASTDOperand extends SimpleNode {
  public ASTDOperand(int id) {
    super(id);
  }

  public ASTDOperand(PredicateBuilder p, int id) {
    super(p, id);
  }


  /** Accept the visitor. **/
  public Object jjtAccept(PredicateBuilderVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }
}

#!/bin/sh
rm com/biswa/ep/util/parser/predicate/SimpleCharStream.java 
rm com/biswa/ep/util/parser/predicate/Token.java 
rm com/biswa/ep/util/parser/predicate/TokenMgrError.java
rm com/biswa/ep/util/parser/predicate/ParseException.java 
jjtree PredicateParser.jjt
javacc -OUTPUT_DIRECTORY="com/biswa/ep/util/parser/predicate" com/biswa/ep/util/parser/predicate/PredicateParser.jj

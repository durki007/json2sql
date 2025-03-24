grammar QueryLang;

@header {
package pl.pwr.antlr;
}

query       : selectClause fromClause whereClause? EOF ;

selectClause: 'SELECT' columnList ;
fromClause  : 'FROM' tableName ;
whereClause : 'WHERE' condition ;

columnList  : columnName (',' columnName)* ;
columnName  : ID ;
tableName   : ID ;

condition   : columnName operator value ;
operator    : '=' | '<' | '>' | '<=' | '>=' | '!=' ;
value       : STRING | NUMBER ;

ID          : [a-zA-Z_][a-zA-Z_0-9]* ;
NUMBER      : [0-9]+ ;
STRING      : '"' (~["\\] | '\\' .)* '"' ;

WS          : [ \t\r\n]+ -> skip ;

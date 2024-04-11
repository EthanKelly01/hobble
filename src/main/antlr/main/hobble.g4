grammar hobble;

@header {
import backend.*;
}

//parser
program returns [Expr ret]
    : value EOF { $ret = new NoneExpr(); }
    ;

value
    : NUMBER
    ;

//lexer
COMMENT : ('/*' .*? '*/' | '//' .*? [\n]) -> skip;

NUMBER : [0-9]+;

WHITESPACE : [ \t\r\n] -> skip;
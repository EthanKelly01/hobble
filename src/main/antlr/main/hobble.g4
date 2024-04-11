grammar hobble;

@header {
import backend.*;
}

//parser
program
    : EOF
    ;

//lexer
WHITESPACE : [ \t\r\n] -> skip;
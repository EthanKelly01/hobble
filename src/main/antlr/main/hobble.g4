grammar hobble;

@header {
import backend.*;
}

//parser
program returns [Expr ret] : scope EOF { $ret = $scope.ret; };

scope returns [Expr ret]
    : { List<Expr> statements = new ArrayList<Expr>(); }
    (statement { statements.add($statement.ret); })*
    { $ret = new Block(statements, false); }
    ;

funcScope returns [Expr ret]
    : { List<Expr> statements = new ArrayList<Expr>(); }
    (statement { statements.add($statement.ret); })*
    { $ret = new Block(statements, true); }
    ;

statement returns [Expr ret]
    : assignment ';'?                         { $ret = $assignment.ret; }
    | expression ';'?                         { $ret = $expression.ret; }
//if statements
    | { Expr first; Expr second = new NoneExpr(); } 'if''(' condition ')' ('{' scope '}' { first = $scope.ret; } | statement { first = $statement.ret; })
    ('else' ('{' scope '}' { second = $scope.ret; } | statement { second = $statement.ret; }))? { $ret = new Check($condition.ret, first, second); }
//function definitions
    | { List<String> args = new ArrayList<String>(); Expr body; } FUNCTION ID '(' (first=ID { args.add($first.text); } (',' iter=ID { args.add($iter.text); })*)? ')'
    ('{' funcScope '}' { body=$funcScope.ret; } | statement { body=$statement.ret; }) { $ret = new FunDef($ID.text, args, body); }
//loop
    | { Expr cr = new NoneExpr(); Expr comp = new NoneExpr(); Expr iter = new NoneExpr(); Expr body = new NoneExpr(); boolean doo = false; }
    ('do' { doo = true; })? '(' ((statement { cr=$statement.ret; })? left=expression CONDITION right=expression (';' assignment { iter=$assignment.ret; })? { comp=new Compare($CONDITION.text, $left.ret, $right.ret); }
    | ID 'in' f=expression '..' s=expression { cr = new Assign($ID.text, $f.ret); comp = new Compare("<=", new Deref($ID.text), $s.ret); iter = new Assign($ID.text, new Modify(new Deref($ID.text), "+++")); })
    ')' ('{' scope '}' { body = $scope.ret; } | statement { body = $statement.ret; }) { $ret = new Loop(cr, comp, body, iter, doo); }
    ;

condition returns [Expr ret]
    : x=expression CONDITION y=expression { $ret = new Compare($CONDITION.text, $x.ret, $y.ret); }
    | left=condition ANDOR right=condition { $ret = new ANDOR($left.ret, $ANDOR.text, $right.ret); }
    | expression { $ret = $expression.ret; } //for int/bool literals???
    ;

assignment returns [Expr ret]
    : ID '=' expression                      { $ret = new Assign($ID.text, $expression.ret); } //maybe change ret to statement?
    | ID OPERATOR '=' expression             { $ret = new Assign($ID.text, new Arith($OPERATOR.text, new Deref($ID.text), $expression.ret)); }
    | ID MODIFIER                            { $ret = new Assign($ID.text, new Modify(new Deref($ID.text), $MODIFIER.text)); }
    | '!' ID                                 { $ret = new Assign($ID.text, new Invert(new Deref($ID.text))); }
    ;

expression returns [Expr ret]
    : '(' expression ')'                     { $ret = $expression.ret; }
//function call
    | { List<Expr> args = new ArrayList<Expr>(); } ID '(' (first=expression { args.add($first.ret); }
    (',' iter=expression { args.add($iter.ret); })*)? ')' { $ret = new FunCall($ID.text, args); }
//arithmetic
    | OPERATOR expression                    { $ret = new Modify($expression.ret, "-"); }
    | x=expression OPERATOR y=expression     { $ret = new Arith($OPERATOR.text, $x.ret, $y.ret); }
    | '!' expression                         { $ret = new Invert($expression.ret); }
    | 'print' '(' condition ')'              { $ret = new Print($condition.ret); }
//literals
    | interrupt                              { $ret = $interrupt.ret; }
    | value                                  { $ret = $value.ret; }
    ;

interrupt returns [Expr ret]
    : { Expr expr = new NoneExpr(); } 'return' (expression {expr = $expression.ret; })? { $ret = new Interrupt(0, expr); }
    | 'break' { $ret = new Interrupt(1, new NoneExpr()); }
    | 'continue' { $ret = new Interrupt(2, new NoneExpr()); }
    ;

value returns [Expr ret]
    : NUMBER                                 { $ret = new IntLiteral($NUMBER.text); }
    | FLOAT                                  { $ret = new FloatLiteral($FLOAT.text); }
    | STRING                                 { $ret = new StringLiteral($STRING.text); }
    | BOOLEAN                                { $ret = new BoolLiteral($BOOLEAN.text); }
    | ID                                     { $ret = new Deref($ID.text); }
    ;

//-------- Lexer --------

COMMENT : ('/*' .*? '*/' | '//' ~[\r\n]*) -> skip;

MODIFIER: '++' | '--';
OPERATOR : '+' | '-' | '*' | '/';
CONDITION : '<' | '<=' | '>' | '>=' | '==' | '!=';
ANDOR : '&&' | '||';

FUNCTION : 'f'('u'('n'('c'('t'('i'('o'('n')?)?)?)?)?)?)?;
BOOLEAN : ('T'|'t') ('R'|'r') ('U'|'u') ('E'|'e') | ('F'|'f') ('A'|'a') ('L'|'l') ('S'|'s') ('E'|'e');

NUMBER : [0-9]+;
FLOAT: ('0'..'9') + ('.' ('0'..'9')+)?;
STRING : '"' ( '\\"' | ~'"' )* '"';
ID : [a-zA-Z_] [a-zA-Z0-9_]*;

WHITESPACE : [ \t\r\n] -> skip;
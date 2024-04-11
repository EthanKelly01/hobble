grammar hobble;

@header {
import backend.*;
}

//parser
program returns [Expr ret] : scope EOF { $ret = $scope.ret; };

scope returns [Expr ret]
    : { List<Expr> statements = new ArrayList<Expr>(); }
    (statement { statements.add($statement.ret); })*
    { $ret = new Block(statements); }
    ;

statement returns [Expr ret]
    : assignment ';'?                         { $ret = $assignment.ret; }
    | expression ';'?                         { $ret = $expression.ret; }
//if statements
    | { Expr first; Expr second = new NoneExpr(); } 'if''(' left=expression CONDITION right=expression ')' ('{' scope '}' { first = $scope.ret; } | statement { first = $statement.ret; })
    ('else' ('{' scope '}' { second = $scope.ret; } | statement { second = $statement.ret; }))? { $ret = new Check(new Compare($CONDITION.text, $left.ret, $right.ret), first, second); }
//function definitions
    | { List<String> args = new ArrayList<String>(); Expr body; } 'function' ID '(' (first=ID { args.add($first.text); } (',' iter=ID { args.add($iter.text); })*)? ')'
    ('{' scope '}' { body=$scope.ret; } | statement { body=$statement.ret; }) { $ret = new FunDef($ID.text, args, body); }
//loop
    | { Expr cr = new NoneExpr(); Expr comp = new NoneExpr(); Expr iter = new NoneExpr(); List<Expr> body = new ArrayList<Expr>(); }
    'loop' '(' ((statement { cr=$statement.ret; })? left=expression CONDITION right=expression (';' assignment { iter=$assignment.ret; })? { comp=new Compare($CONDITION.text, $left.ret, $right.ret); }
    | ID 'in' f=expression '..' s=expression { cr = new Assign($ID.text, $f.ret); comp = new Compare("<=", new Deref($ID.text), $s.ret); iter = new Assign($ID.text, new Modify(new Deref($ID.text), "+++")); })
    ')' ('{' scope '}' { body.add($scope.ret); } | statement { body.add($statement.ret); }) { body.add(iter); $ret = new Loop(cr, comp, new Block(body)); }
    ;

assignment returns [Expr ret]
    : 'let'? ID '=' expression               { $ret = new Assign($ID.text, $expression.ret); } //maybe change ret to statement?
    | ID MODIFIER                            { $ret = new Assign($ID.text, new Modify(new Deref($ID.text), $MODIFIER.text)); }
    ;

expression returns [Expr ret]
    : '(' expression ')'                     { $ret = $expression.ret; }
//function call
    | { List<Expr> args = new ArrayList<Expr>(); } ID '(' (first=expression { args.add($first.ret); }
    (',' iter=expression { args.add($iter.ret); })*)? ')' { $ret = new FunCall($ID.text, args); }
//arithmetic
    | OPERATOR expression                    { $ret = new Modify($expression.ret, "-"); }
    | x=expression OPERATOR y=expression     { $ret = new Arith($OPERATOR.text, $x.ret, $y.ret); }
    | 'print(' expression ')'                { $ret = new Print($expression.ret); }
//literals
    | NUMBER                                 { $ret = new IntLiteral($NUMBER.text); }
    | STRING                                 { $ret = new StringLiteral($STRING.text); }
    | ID                                     { $ret = new Deref($ID.text); }
    ;

//-------- Lexer --------

COMMENT : ('/*' .*? '*/' | '//' .*? '\n') -> skip;

MODIFIER: '++' | '--';
OPERATOR : '+' | '-' | '*' | '/';
CONDITION : '<' | '<=' | '>' | '>=' | '==' | '!=';

NUMBER : [0-9]+;
STRING : '"' .*? (~('\\') '"');
ID : [a-zA-Z_] [a-zA-Z0-9_]*;

WHITESPACE : [ \t\r\n] -> skip;
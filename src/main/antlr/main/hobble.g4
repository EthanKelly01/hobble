grammar hobble;

@header {
import backend.*;
}

//parser
program returns [Expr ret] : scope { $ret = $scope.ret; System.out.println("------------------v Output v------------------"); } EOF;

scope returns [Expr ret]
    : { List<Expr> statements = new ArrayList<Expr>(); }
    (statement { statements.add($statement.ret); })* { $ret = new Block(statements, false); }
    ;

funcScope returns [Expr ret]
    : { List<Expr> statements = new ArrayList<Expr>(); }
    (statement { statements.add($statement.ret); })* { $ret = new Block(statements, true); }
    ;

statement returns [Expr ret]
    : expression ';'?                         { $ret = $expression.ret; }
//if statements
    | { Expr cond=new BoolRandom(); Expr f; Expr s=new NoneExpr(); }
     ('if''(' (expression { cond=$expression.ret; })? ')' ('{' scope '}' { f=$scope.ret; } | statement { f=$statement.ret; })
     | 'would' ('{'scope'}' { f=$scope.ret; } | statement { f=$statement.ret; }) 'if' '(' (expression { cond=$expression.ret; })? ')')
     ('else' ('{' scope '}' { s=$scope.ret; } | statement { s=$statement.ret; }))? { $ret = new Check(cond, f, s); }
//function definitions
    | { List<String> args = new ArrayList<String>(); Expr body; } FUNCTION ID '(' (first=ID { args.add($first.text); } (',' iter=ID { args.add($iter.text); })*)? ')'
    ('{' funcScope '}' { body=$funcScope.ret; } | statement { body=$statement.ret; }) { $ret = new FunDef($ID.text, args, body); }
//loop
    | { Expr cr = new NoneExpr(); Expr comp = new BoolRandom(); Expr iter = new NoneExpr(); Expr body = new NoneExpr(); boolean doo = false; }
    ('do' { doo = true; })? '(' ((statement { cr=$statement.ret; })? (comp=expression { comp=$comp.ret; })? (';' it=expression { iter=$it.ret; })?
    | ID 'in' f=expression '..' s=expression { cr = new Assign($ID.text, $f.ret); comp = new RangeBuilder(new Deref($ID.text), $f.ret, $s.ret); iter = new IterBuilder($ID.text, $f.ret, $s.ret); })
    ')' ('{' scope '}' { body = $scope.ret; } | statement { body = $statement.ret; }) { $ret = new Loop(cr, comp, body, iter, doo); }
    ;

expression returns [Expr ret]
    : '(' expression ')'                     { $ret = $expression.ret; }
    | 'print' '(' expression ')'              { $ret = new Print($expression.ret); }
//assignment
    | { String v=null; } ID ((OPERATOR { v=$OPERATOR.text; })? '=' statement { if(v==null)
      $ret=new Assign($ID.text, $statement.ret); else $ret=new Assign($ID.text, new Arith(v, new Deref($ID.text), $statement.ret)); }
      | MODIFIER { $ret = new Assign($ID.text, new Modify(new Deref($ID.text), $MODIFIER.text)); })
//function call
    | { List<Expr>args=new ArrayList<Expr>(); } ID '(' (f=expression { args.add($f.ret); } (',' it=expression { args.add($it.ret); })*)?
      ')' { $ret=new FunCall($ID.text, args); }
//const
	| 'const' ((ID { $ret=new Const(new Deref($ID.text), null); }) | ({ List<Expr> args=new ArrayList<Expr>(); }
	 ID '(' (first=expression { args.add($first.ret); } (',' iter=expression { args.add($iter.ret); })*)? ')'
	 { $ret=new Const(new Deref($ID.text), args); }))
	| 'deconst' ID ('('')')?		 { $ret=new Deconst(new Deref($ID.text)); }
//arithmetic
    | OPERATOR expression                    { $ret=new Modify($expression.ret, $OPERATOR.text); }
    | x=expression OPERATOR y=expression     { $ret=new Arith($OPERATOR.text, $x.ret, $y.ret); }
//interrupts
    | { Expr expr=new NoneExpr(); } 'return' (expression {expr=$expression.ret; })? { $ret=new Interrupt(0, expr); }
    | 'break' { $ret=new Interrupt(1, new NoneExpr()); }
    | 'continue' { $ret=new Interrupt(2, new NoneExpr()); }
//literals
    | NUMBER                                 { $ret=new IntLiteral($NUMBER.text); }
    | FLOAT                                  { $ret=new FloatLiteral($FLOAT.text); }
    | STRING                                 { $ret=new StringLiteral($STRING.text); }
    | BOOLEAN                                { $ret=new BoolLiteral($BOOLEAN.text); }
    | ID                                     { $ret=new Deref($ID.text); }
    ;

//-------- Lexer --------

COMMENT : ('/*' .*? '*/' | '//' ~[\r\n]*) -> skip;

MODIFIER: '++' | '--';
OPERATOR : '+' | '-' | '*' | '/''-'? | '**' | '%' | '!' | '<' | '<=' | '>' | '>=' | '==' | '!=' | '&&' | '||';

FUNCTION : 'f'('u'('n'('c'('t'('i'('o'('n')?)?)?)?)?)?)?;
BOOLEAN : ('T'|'t') ('R'|'r') ('U'|'u') ('E'|'e') | ('F'|'f') ('A'|'a') ('L'|'l') ('S'|'s') ('E'|'e');

NUMBER : [0-9]+;
FLOAT: ('0'..'9') + ('.' ('0'..'9')+)?;
STRING : '"' ( '\\"' | ~'"' )* '"';
ID : [a-zA-Z_] [a-zA-Z0-9_]*;

WHITESPACE : [ \t\r\n] -> skip;
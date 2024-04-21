grammar hobble;

@header {
import backend.*;
}

//-------- Parser --------
program returns [Expr ret] : { List<Expr>lines=new ArrayList<Expr>(); } (code ';'? { lines.add($code.ret); })* EOF
    { $ret=new Block(lines, false); System.out.println("------------------v Output v------------------"); };

code returns [Expr ret]
    : { List<Expr> lines=new ArrayList<Expr>(); } '{' (code ';'? { lines.add($code.ret); })* '}' { $ret=new Block(lines, false); }
    | 'print' '(' code ')'              { $ret = new Print($code.ret); }
//if statements
    | { Expr cond=new BoolRandom(); Expr f; Expr s=new NoneExpr(); } ('if''(' (code { cond=$code.ret; })? ')' code { f=$code.ret; } |
      'would' code { f=$code.ret; } 'if' '(' (code { cond=$code.ret; })? ')') ('else' code { s=$code.ret; })? { $ret=new Check(cond, f, s); }
//function definitions
    | { List<String>args=new ArrayList<String>(); Expr body=new NoneExpr(); } FUNCTION ID '(' (first=ID { args.add($first.text); } (',' iter=ID { args.add($iter.text); })*)? ')'
      ('{' { List<Expr>lines=new ArrayList<Expr>(); } (code { lines.add($code.ret); })* { body=new Block(lines, true); }
      '}' | code { body=$code.ret; }) { $ret=new FunDef($ID.text, args, body); }
//loop
    | { boolean doo=false; } ('do' { doo=true; })? '(' ({ Expr e1=new NoneExpr(); Expr e2=new BoolRandom(); Expr e3=new NoneExpr(); }
      (code { e2=$code.ret; } | (code ';' { e1=$code.ret; })? e2=code ';' e3=code { e2=$e2.ret; e3=$e3.ret; })? ')' code { $ret=new Loop(e1, e2, $code.ret, e3, doo); }
      | ID 'in' f=code '..' s=code ')' code { $ret=new RangeBuilder($ID.text, $f.ret, $s.ret, $code.ret, doo); })
//assignment
    | { String v=null; } ID ((OPERATOR { v=$OPERATOR.text; })? '=' code { if(v==null)
      $ret=new Assign($ID.text, $code.ret); else $ret=new Assign($ID.text, new Arith(v, new Deref($ID.text), $code.ret)); }
      | MODIFIER { $ret=new Assign($ID.text, new Modify(new Deref($ID.text), $MODIFIER.text)); })
//function call
    | { List<Expr>args=new ArrayList<Expr>(); boolean isConst=false; } ('const' { isConst=true; })? ID '(' (code { args.add($code.ret); }
      (',' code { args.add($code.ret); })*)? ')' { if(isConst) $ret=new Const($ID.text, args); else $ret=new FunCall($ID.text, args); }
//arithmetic
    | x=code OPERATOR y=code     { $ret=new Arith($OPERATOR.text, $x.ret, $y.ret); }
    | OPERATOR code                    { $ret=new Modify($code.ret, $OPERATOR.text); }
//interrupts (productions 10+)
    | { Expr expr=new NoneExpr(); } 'return' (code {expr=$code.ret; })? { $ret=new Interrupt(Interrupts.RETURN, expr); }
    | 'break' { $ret=new Interrupt(Interrupts.BREAK, new NoneExpr()); }
    | 'continue' { $ret=new Interrupt(Interrupts.CONTINUE, new NoneExpr()); }
//literals
    | NUMBER                                 { $ret=new IntLiteral($NUMBER.text); }
    | FLOAT                                  { $ret=new FloatLiteral($FLOAT.text); }
    | STRING                                 { $ret=new StringLiteral($STRING.text); }
    | BOOLEAN                                { $ret=new BoolLiteral($BOOLEAN.text); }
    | ID                                     { $ret=new Deref($ID.text); }
    | 'const' ID                             { $ret=new Const($ID.text, null); }
    | 'deconst' ID ('('')')?		         { $ret=new Deconst($ID.text); }
    ;

//-------- Lexer --------

COMMENT : ('/*' .*? '*/' | '//' ~[\r\n]*) -> skip;

MODIFIER: '++' | '--';
OPERATOR : '+' | '-' | '*' | '/''-'? | '**' | '%' | '!' | '<' | '<=' | '>' | '>=' | '==' | '!=' | '&&' | '||';

FUNCTION : 'f'('u'('n'('c'('t'('i'('o'('n')?)?)?)?)?)?)?;
BOOLEAN : ('T'|'t') ('R'|'r') ('U'|'u') ('E'|'e') | ('F'|'f') ('A'|'a') ('L'|'l') ('S'|'s') ('E'|'e');

NUMBER : [0-9]+;
FLOAT: NUMBER '.' NUMBER;
STRING : '"' ( '\\"' | ~'"' )* '"';
ID : [a-zA-Z_] [a-zA-Z0-9_]*;

WHITESPACE : [ \t\r\n] -> skip;
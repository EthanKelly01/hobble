package backend

abstract class Expr {
    abstract fun eval(runtime:Runtime):Data
}

class NoneExpr(): Expr() {
    override fun eval(runtime:Runtime) = None
}

class IntLiteral(val lexeme:String):Expr() {
    override fun eval(runtime:Runtime):Data = IntData(Integer.parseInt(lexeme))
}

class StringLiteral(val lexeme:String):Expr() {
    override fun eval(runtime:Runtime):Data = if (lexeme != "" && lexeme[0] == '"' && lexeme[lexeme.length-1] == '"') StringData(lexeme.substring(1, lexeme.length-1)) else StringData(lexeme)
}

//-------- Handling Variables --------

class Block(val exprs:List<Expr>):Expr() {
    override fun eval(runtime:Runtime):Data = exprs.map { it.eval(runtime) }.last()
}

class Assign(val name:String, val expr:Expr):Expr() {
    override fun eval(runtime:Runtime):Data {
        runtime.symbolTable.put(name, expr.eval(runtime))
        return None
    }
}

class Deref(val name:String):Expr() {
    override fun eval(runtime:Runtime):Data = runtime.symbolTable[name] ?: None
}

class Print(val output:Expr):Expr() {
    override fun eval(runtime:Runtime):Data {
        System.out.println(output.eval(runtime))
        return None
    }
}

//-------- Arithmetic --------

class Arith(val op:String, val left:Expr, val right:Expr):Expr() {
    override fun eval(runtime:Runtime):Data {
        val x = left.eval(runtime)
        val y = right.eval(runtime)
        if (x is IntData && y is IntData) return IntData(when(op) {
            "+" -> x.v + y.v
            "-" -> x.v - y.v
            "*" -> x.v * y.v
            "/" -> x.v / y.v
            else -> { throw Exception("Invalid operator") }})
        else if (x is StringData && y is StringData && op == "+") return StringData("$x$y")
        else if (x is IntData && y is StringData && op == "*") return StringData(y.v.repeat(x.v))
        else if (x is StringData && y is IntData && op == "*") return StringData(x.v.repeat(y.v))
        throw Exception("Only supports ints and strings")
    }
}

class Modify(val myVal:Expr, val op:String):Expr() {
    override fun eval(runtime:Runtime):Data {
        var v = myVal.eval(runtime)
        if (v is IntData) return IntData(when(op) {
            "++" -> v.v + 1
            "--" -> v.v - 1
            "-" -> - v.v
            else -> { throw Exception("Invalid operator") }})
        throw Exception("Only supports ints so far, work in progress")
    }
}

class Compare(val op:String, val left:Expr, val right:Expr):Expr() {
    override fun eval(runtime:Runtime):Data {
        val x:Data = left.eval(runtime)
        val y:Data = right.eval(runtime)
        if(x is IntData && y is IntData) return BoolData(when(op) {
            "<" -> x.v < y.v
            "<=" -> x.v <= y.v
            ">" -> x.v > y.v
            ">=" -> x.v >= y.v
            "==" -> x.v == y.v
            "!=" -> x.v != y.v
            else -> { throw Exception("Invalid operator") }})
        else throw Exception("Cannot perform comparison")
    }
}

class ANDOR(val left:Expr, val op:String, val right:Expr): Expr() {
    override fun eval(runtime: Runtime): Data {
        val x:Data = left.eval(runtime)
        val y:Data = right.eval(runtime)
        if (x is BoolData && y is BoolData) return BoolData(when(op) {
            "&&" -> x.v && y.v
            "||" -> x.v || y.v
            else -> { throw Exception("Error comparing conditions"); }
        }) else throw Exception("Expressions are not conditions")
    }
}

class Check(val cond:Expr, val trueExpr:Expr, val falseExpr:Expr):Expr() {
    override fun eval(runtime:Runtime):Data = if((cond.eval(runtime) as BoolData).v) trueExpr.eval(runtime) else falseExpr.eval(runtime)
}

//-------- Loops --------

class Loop(val creation:Expr, val cond:Expr, val body:Expr):Expr() {
    override fun eval(runtime:Runtime):Data {
        creation.eval(runtime);
        while((cond.eval(runtime) as BoolData).v) body.eval(runtime)
        return None
    }
}

//-------- Functions --------

class FunDef(val name:String, val args:List<String>, val body:Expr):Expr() {
    override fun eval(runtime:Runtime):Data {
        runtime.symbolTable[name] = FuncData(name, args, body)
        return None //do I want to return this? Probably later
    }
}

class FunCall(val name:String, val args:List<Expr>):Expr() {
    override fun eval(runtime:Runtime):Data {
        val f = runtime.symbolTable[name]
        if (f == null) throw Exception("Function not found")
        if (f !is FuncData) throw Exception("$name is not a function")
        if (args.size != f.args.size) throw Exception("$name expects ${f.args.size} arguments, but ${args.size} given.")

        val argsData = args.map { it.eval(runtime) }
        return f.body.eval(runtime.copy(f.args.zip(argsData).toMap()))
    }
}
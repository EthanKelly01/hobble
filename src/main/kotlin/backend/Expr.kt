package backend

import kotlin.math.pow
import kotlin.math.sqrt

abstract class Expr {
    abstract fun eval(runtime:Runtime):Data
}

class NoneExpr(): Expr() {
    override fun eval(runtime:Runtime) = None
}

class IntLiteral(val lexeme:String):Expr() {
    override fun eval(runtime:Runtime):Data = IntData(Integer.parseInt(lexeme))
}

class FloatLiteral(val lexeme:String):Expr() {
    override fun eval(runtime:Runtime): Data = FloatData(lexeme.toFloat())
}

class StringLiteral(val lexeme:String):Expr() {
    override fun eval(runtime:Runtime):Data = if (lexeme != "" && lexeme[0] == '"' && lexeme[lexeme.length-1] == '"') StringData(lexeme.substring(1, lexeme.length-1)) else StringData(lexeme)
}

class BoolLiteral(val lexeme:String):Expr() {
    override fun eval(runtime: Runtime): Data = if (lexeme.uppercase() == "TRUE") BoolData(true) else BoolData(false)
}

//-------- Handling Variables --------

class Block(val exprs:List<Expr>, val flag:Boolean = false):Expr() {
    override fun eval(runtime:Runtime):Data {
        for (expr in exprs) {
            val data = expr.eval(runtime)
            if (data is InterruptData) if (!flag || data.flag == 0) return data
        }
        return None
    }
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
        var x:Data = Normalize(left).eval(runtime)
        var y:Data = Normalize(right).eval(runtime)
        if (x is FloatData && y is FloatData) return FloatData(when(op) {
            "+" -> x.v + y.v
            "-" -> x.v - y.v
            "*" -> x.v * y.v
            "/" -> x.v / y.v
            "**" -> x.v.pow(y.v)
            "%" -> x.v % y.v
            "/=" -> y.v.pow(1/x.v)
            else -> { throw Exception("Invalid operator") }})
        if (x is StringData && y is StringData) {
            return StringData(when(op) {
                "+" -> "$x$y"
                "/" -> {
                    var newStr = x.v
                    for (i in y.v) newStr = newStr.replace(i.toString(), "")
                    return StringData(newStr)
                }
                else -> { throw Exception("Invalid operator") }
            })
        }
        if (x is FloatData && y is StringData) x = y.also { y = x }
        if (x is StringData && y is FloatData) return StringData(when(op) {
            "+" -> "$x$y"
            "-" -> if ((y as FloatData).toInt().v < x.v.length) x.v.substring(0, x.v.length - (y as FloatData).toInt().v) else ""
            "*" -> x.v.repeat((y as FloatData).toInt().v)
            "/" -> x.v.substring(0, x.v.length / (y as FloatData).toInt().v)
            else -> { throw Exception("Invalid operator") }})
        throw Exception("Only supports bools, ints, floats, and strings")
    }
}

class Modify(val myVal:Expr, val op:String):Expr() {
    override fun eval(runtime:Runtime):Data {
        var v = Normalize(myVal).eval(runtime)
        if (v is FloatData) return FloatData(when(op) {
            "++" -> v.v + 1
            "--" -> v.v - 1
            "-" -> - v.v
            "/-" -> sqrt(v.v)
            else -> { throw Exception("Invalid operator") }})
        throw Exception("Only supports ints so far, work in progress")
    }
}

class Compare(val op:String, val left:Expr, val right:Expr):Expr() {
    override fun eval(runtime:Runtime):Data {
        val x:Data = Normalize(left).eval(runtime)
        val y:Data = Normalize(right).eval(runtime)
        if(x is FloatData && y is FloatData) return BoolData(when(op) {
            "<" -> x.v < y.v
            "<=" -> x.v <= y.v
            ">" -> x.v > y.v
            ">=" -> x.v >= y.v
            "==" -> x.v == y.v
            "!=" -> x.v != y.v
            else -> { throw Exception("Invalid operator") }})
        if (x is StringData && y is StringData) return BoolData(when(op) {
            "==" -> x.v == y.v
            "!=" -> x.v != y.v
            else -> { throw Exception("Invalid operator") }})
        throw Exception("Cannot perform comparison")
    }
}

class ANDOR(val left:Expr, val op:String, val right:Expr): Expr() {
    override fun eval(runtime: Runtime): Data {
        var x:Data = left.eval(runtime)
        var y:Data = right.eval(runtime)
        if (x is IntData) x = x.toBool()
        if (y is IntData) y = y.toBool()
        if (x is BoolData && y is BoolData) return BoolData(when(op) {
            "&&" -> x.v && y.v
            "||" -> x.v || y.v
            else -> { throw Exception("Error comparing conditions") }
        }) else throw Exception("Expressions are not conditions")
    }
}

class Invert(val bool:Expr):Expr() {
    override fun eval(runtime: Runtime): Data {
        var x = bool.eval(runtime)
        if (x is IntData) x = x.toBool()
        if (x !is BoolData) throw Exception("Tried to invert a non-boolean")
        return BoolData(!x.v)
    }
}

class Check(val cond:Expr, val trueExpr:Expr, val falseExpr:Expr):Expr() {
    override fun eval(runtime:Runtime):Data {
        var result = cond.eval(runtime)
        if (result is IntData) result = result.toBool()
        return if(result is BoolData && result.v) trueExpr.eval(runtime) else falseExpr.eval(runtime)
    }
}

//-------- Loops --------

class Loop(val creation:Expr, val cond:Expr, val body:Expr, val iter:Expr, val doo:Boolean):Expr() {
    override fun eval(runtime:Runtime):Data {
        if (doo) body.eval(runtime)
        creation.eval(runtime)
        while((cond.eval(runtime) as BoolData).v) {
            val ret:Data = body.eval(runtime)
            if (ret is InterruptData) {
                if (ret.flag == 0) return ret
                if (ret.flag == 1) break
            }
            iter.eval(runtime)
        }
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
        val f = runtime.symbolTable[name] ?: throw Exception("Function not found")
        if (f !is FuncData) throw Exception("$name is not a function")
        if (args.size != f.args.size) throw Exception("$name expects ${f.args.size} arguments, but ${args.size} given.")

        val ret = f.body.eval(runtime.copy(f.args.zip(args.map { it.eval(runtime) }).toMap()))
        return if (ret is InterruptData) ret.eval(runtime) else ret
    }
}

class Interrupt(val flag:Int = 0, val v: Expr = NoneExpr()):Expr() {
    override fun eval(runtime:Runtime):Data = InterruptData(flag, v)
}
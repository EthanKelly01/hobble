package backend

import java.util.*
import kotlin.math.pow
import kotlin.math.sqrt

abstract class Expr { abstract fun eval(runtime:Runtime):Data }
class NoneExpr: Expr() { override fun eval(runtime:Runtime) = None }
class IntLiteral(val lexeme:String):Expr() { override fun eval(runtime:Runtime):Data = IntData(Integer.parseInt(lexeme)) }
class FloatLiteral(val lexeme:String):Expr() { override fun eval(runtime:Runtime): Data = FloatData(lexeme.toFloat()) }
class StringLiteral(val lexeme:String):Expr() {
    override fun eval(runtime:Runtime):Data = if (lexeme != "" && lexeme[0] == '"' && lexeme[lexeme.length-1] == '"')
        StringData(lexeme.substring(1, lexeme.length-1)) else StringData(lexeme)
}
class BoolLiteral(val lexeme:String):Expr() {
    override fun eval(runtime: Runtime): Data = if (lexeme.uppercase() == "TRUE") BoolData(true) else BoolData(false)
}
class BoolRandom():Expr() { override fun eval(runtime:Runtime):Data = BoolData(Random().nextBoolean()) }

//-------- Handling Variables --------

class Block(val exprs:List<Expr>, val flag:Boolean = false):Expr() {
    override fun eval(runtime:Runtime):Data {
        for (expr in exprs) {
            val data = expr.eval(runtime)
            if (data is InterruptData && (!flag || data.flag == Interrupts.RETURN)) return data
        }
        return None
    }
}

class Assign(val name:String, val expr:Expr):Expr() {
    override fun eval(runtime:Runtime):Data {
        if (runtime.symbolTable[name]?.isConst != true) runtime.symbolTable[name] = expr.eval(runtime)
        return runtime.symbolTable[name] ?: None
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

class Const(val name:String, val args:List<Expr>?):Expr() {
    override fun eval(runtime:Runtime):Data {
        val v = runtime.symbolTable[name]!!
        v.isConst = true
        if (v is FuncData) if (args!!.size == v.args.size) v.constInstance = args
        else throw Exception("$name expects ${v.args.size} arguments, but ${args.size} given.")
        return None
    }
}

class Deconst(val name:String):Expr() {
    override fun eval(runtime:Runtime):Data {
        runtime.symbolTable[name]!!.isConst = false
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
            "/-" -> y.v.pow(1/x.v)
            else -> { return BoolData(when(op) {
                "<" -> x.v < y.v
                "<=" -> x.v <= y.v
                ">" -> x.v > y.v
                ">=" -> x.v >= y.v
                "==" -> x.v == y.v
                "!=" -> x.v != y.v
                "&&" -> x.toInt().toBool().v && y.toInt().toBool().v
                "||" -> x.toInt().toBool().v || y.toInt().toBool().v
                else -> { throw Exception("Compiler error: Invalid operator / float arith") }}) }})
        if (x is StringData && y is StringData) {
            return StringData(when(op) {
                "+" -> "$x$y"
                "/" -> {
                    var newStr = x.v
                    for (i in y.v) newStr = newStr.replace(i.toString(), "")
                    return StringData(newStr)
                }
                else -> { return BoolData(when(op) {
                    "==" -> x.v == y.v
                    "!=" -> x.v != y.v
                    else -> { throw Exception("Compiler error: Invalid operator / string arith") }}) }})
        }
        if (x is FloatData && y is StringData) x = y.also { y = x }
        if (x is StringData && y is FloatData) return StringData(when(op) {
            "+" -> "$x$y"
            "-" -> if ((y as FloatData).toInt().v < x.v.length) x.v.substring(0, x.v.length - (y as FloatData).toInt().v) else ""
            "*" -> x.v.repeat((y as FloatData).toInt().v)
            "/" -> x.v.substring(0, x.v.length / (y as FloatData).toInt().v)
            else -> { throw Exception("Compiler error: Invalid operator / float/string arith") }})
        throw Exception("Only supports bools, ints, floats, and strings")
    }
}

class Modify(val myVal:Expr, val op:String):Expr() {
    override fun eval(runtime:Runtime):Data {
        val v = Normalize(myVal).eval(runtime)
        if (v is FloatData) return if (op=="!") BoolData(!(v.toInt().toBool()).v) else FloatData(when(op) {
            "++" -> v.v + 1
            "--" -> v.v - 1
            "-" -> - v.v
            "/-" -> sqrt(v.v)
            else -> { throw Exception("Compiler error: Invalid operator / modify") }})
        throw Exception("Only supports numbers so far, work in progress")
    }
}

class Check(val cond:Expr, val trueExpr:Expr, val falseExpr:Expr):Expr() {
    override fun eval(runtime:Runtime):Data = if ((Normalize(cond).eval(runtime) as FloatData).toInt().toBool().v)
        trueExpr.eval(runtime) else falseExpr.eval(runtime)
}

//-------- Loops --------

class Loop(val creation:Expr, val cond:Expr, val body:Expr, val iter:Expr, val doo:Boolean):Expr() {
    override fun eval(runtime:Runtime):Data {
        creation.eval(runtime)
        if (doo) body.eval(runtime)
        while((Normalize(cond).eval(runtime) as FloatData).toInt().toBool().v) {
            val ret:Data = body.eval(runtime)
            if (ret is InterruptData) {
                if (ret.flag == Interrupts.RETURN) return ret
                if (ret.flag == Interrupts.BREAK) break
            }
            Normalize(iter).eval(runtime)
        }
        return None
    }
}

class RangeBuilder(val name:String, val first:Expr, val second:Expr, val body:Expr, val doo:Boolean):Expr() {
    override fun eval(runtime:Runtime):Data {
        val x = Normalize(first).eval(runtime)
        val y = Normalize(second).eval(runtime)
        if (x !is FloatData || y !is FloatData) {
            println("Compiler error: Not a valid range, can't iterate.")
            return None
        }
        return Loop(Assign(name, first), if (x.v <= y.v) Arith("<=", Deref(name), second) else Arith(">=", Deref(name), second),
            body, if (x.v <= y.v) Assign(name, Modify(Deref(name), "++")) else Assign(name, Modify(Deref(name), "--")), doo).eval(runtime)
    }
}

//-------- Functions --------

class FunDef(val name:String, val args:List<String>, val body:Expr):Expr() {
    override fun eval(runtime:Runtime):Data {
        runtime.symbolTable[name] = FuncData(name, args, body)
        return None
    }
}

class FunCall(val name:String, val args:List<Expr>):Expr() {
    override fun eval(runtime:Runtime):Data {
        val f = runtime.symbolTable[name] ?: throw Exception("Function not found")
        if (f !is FuncData) throw Exception("$name is not a function")
        val ret = if (f.isConst) f.body.eval(runtime.copy(f.args.zip(f.constInstance!!.map { it.eval(runtime) }).toMap()))
        else if (args.size != f.args.size) throw Exception("$name expects ${f.args.size} arguments, but ${args.size} given.")
        else f.body.eval(runtime.copy(f.args.zip(args.map { it.eval(runtime) }).toMap()))
        return if (ret is InterruptData) ret.eval(runtime) else ret
    }
}

class Interrupt(val flag:Interrupts = Interrupts.RETURN, val v: Expr = NoneExpr()):Expr() {
    override fun eval(runtime:Runtime):Data = InterruptData(flag, v)
}
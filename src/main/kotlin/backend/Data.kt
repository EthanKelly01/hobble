package backend

//for data classes
abstract class Data

object None:Data() {
    override fun toString() = "None"
}

class IntData(val v:Int): Data() {
    override fun toString() = "$v"
    fun toBool() = if (v > 0) BoolData(true) else BoolData(false)
    fun toFloat() = FloatData(v.toFloat())
}

class FloatData(val v: Float): Data() {
    override fun toString() = "$v"
    fun toInt() = IntData(v.toInt())
}

class StringData(val v:String): Data() {
    override fun toString() = "$v"
}

class BoolData(val v:Boolean): Data() {
    override fun toString() = "$v"
    fun toInt() = if (v) IntData(1) else IntData(0)
}

class FuncData(val name:String, val args:List<String>, val body:Expr): Data() {
    override fun toString() = "Function: $name ($args);"
}

class InterruptData(val flag:Int, val v:Expr):Data() {
    override fun toString() = "error/interrupt"
    fun eval(runtime:Runtime):Data = v.eval(runtime)
}
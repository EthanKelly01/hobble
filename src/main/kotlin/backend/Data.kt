package backend

//for data classes
abstract class Data

object None:Data() {
    override fun toString() = "None"
}

class IntData(val v:Int): Data() {
    override fun toString() = "$v"
}

class StringData(val v:String): Data() {
    override fun toString() = "$v"
}

class BoolData(val v:Boolean): Data() {
    override fun toString() = "$v"
}

class FuncData(val name:String, val args:List<String>, val body:Expr): Data() {
    override fun toString() = "Function: $name ($args);"
}
package backend

class Normalize(val v:Expr) {
    fun eval(runtime:Runtime):Data {
        var x = v.eval(runtime)
        if (x is BoolData) x = x.toInt()
        if (x is IntData) x = x.toFloat()
        return x
    }
}
package backend

//for data classes
abstract class Data

object None:Data() {
    override fun toString() = "None"
}

class IntData(val v:Int): Data() {
    override fun toString() = "$v"
}
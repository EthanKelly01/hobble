package backend

class Runtime() {
    val symbols:MutableMap<String, Data> = mutableMapOf()

    override fun toString(): String = symbols.map { entry -> "${entry.key} = ${entry.value}" }.joinToString("; ")
}
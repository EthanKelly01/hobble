package main

import org.antlr.v4.runtime.*
import backend.*

private fun readSampleFile(source:String): CharStream? =
    Thread.currentThread().contextClassLoader.getResourceAsStream(source).use { input -> CharStreams.fromStream(input) }

private fun execute(source:String) {
    val errorlistener = object: BaseErrorListener() {
        override fun syntaxError(
            recognizer: Recognizer<*, *>?,
            offendingSymbol: Any?,
            line: Int,
            charPositionInLine: Int,
            msg: String?,
            e: RecognitionException?
        ) { throw Exception("$e at line:${line}, char:${charPositionInLine}") }
    }
    val parser = hobbleParser(CommonTokenStream(hobbleLexer(readSampleFile(source)).apply {
        removeErrorListeners()
        addErrorListener(errorlistener)
    })).apply {
        removeErrorListeners()
        addErrorListener(errorlistener)
    }
    try { parser.program().ret.eval(Runtime())
    } catch (e:Exception) { println("Error: $e") }
}

fun main() {
    execute("sample.hobl")
}
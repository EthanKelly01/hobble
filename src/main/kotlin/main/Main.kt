package main

import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream

private fun readSampleFile(): org.antlr.v4.runtime.CharStream? {
    val contextClassLoader = Thread.currentThread().contextClassLoader
    return contextClassLoader.getResourceAsStream("sample.hobl").use { input -> CharStreams.fromStream(input) }
}

fun main() {
    val charStream = readSampleFile()
    val lexer = hobbleLexer(charStream)
    val tokens = CommonTokenStream(lexer)
    val parser = hobbleParser(tokens)

    while (parser.currentToken.type != hobbleParser.EOF) {
        println("bruh")
    }
}
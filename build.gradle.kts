import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.9.23"
    java
    antlr
}

group = "main"
version = "1.0"

repositories {
    mavenCentral()
}

dependencies {
    antlr("org.antlr:antlr4:4.10")
    testImplementation(kotlin("test"))
}

tasks.generateGrammarSource {
    outputDirectory = file("${project.buildDir}/generated.sources.main.kotlin/antlr")
    arguments = listOf("-package", "main")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(11)
}

sourceSets {
    main {
        java {
            srcDir(tasks.generateGrammarSource)
        }
    }
}
plugins {
    kotlin("jvm") version "1.9.20"
    kotlin("plugin.serialization") version "1.9.20"
    application
}

group = "com.mcp"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
    testImplementation(kotlin("test"))
}

application {
    mainClass.set("com.mcp.server.MainKt")
}

tasks.named<JavaExec>("run") {
    standardInput = System.`in`
    standardOutput = System.out
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = "com.mcp.server.MainKt"
    }
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}


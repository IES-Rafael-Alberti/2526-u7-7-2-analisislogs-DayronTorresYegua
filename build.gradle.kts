plugins {
    kotlin("jvm") version "2.3.0"
    application
}

group = "org.iesra"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("com.github.ajalt.clikt:clikt:4.2.0")
}

kotlin {
    jvmToolchain(21)
}

application {
    mainClass.set("org.iesra.MainKt")
}

tasks.test {
    useJUnitPlatform()
}
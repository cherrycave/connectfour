plugins {
    kotlin("jvm")
}

group = "dev.boecker.cherrycave"
version = "0.1.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("net.minestom:minestom:2026.06.20-26.1.2")
    implementation("dev.hollowcube:polar:1.16.0")
    implementation(project(":connectfour-core"))

    implementation("ch.qos.logback:logback-classic:1.5.34")
    implementation("io.github.oshai:kotlin-logging-jvm:8.0.4")
}

kotlin {
    jvmToolchain(25)
}
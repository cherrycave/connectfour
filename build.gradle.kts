plugins {
    kotlin("jvm") version "2.3.21"
}

group = "dev.boecker.cherrycave"
version = "0.1.0"

repositories {
    mavenCentral()
}

dependencies {
}

kotlin {
    jvmToolchain(25)
}
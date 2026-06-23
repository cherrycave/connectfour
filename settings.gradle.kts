plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
rootProject.name = "connectfour"
include("connectfour-core")
include("test-server")
plugins {
    kotlin("jvm")
    `maven-publish`
}

group = "dev.boecker.cherrycave"
version = rootProject.version

repositories {
    mavenCentral()
}

dependencies {
    compileOnly("net.minestom:minestom:2026.06.20-26.1.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
}

kotlin {
    jvmToolchain(25)
}

publishing {
    repositories {
        maven {
            setUrl("https://maven.boecker.dev/releases")

            credentials {
                username = System.getenv("MAVEN_USERNAME")
                password = System.getenv("MAVEN_SECRET")
            }
        }
    }

    publications {
        publications {
            create<MavenPublication>(project.name) {
                from(components["kotlin"])
                pom {
                    name.set(project.name)
                    description.set("minestom library for creating and managing connect four games")
                    url.set("https://github.com/cherrycave/connectfour")

                    licenses {
                        license {
                            name.set("AGPL-3.0 License")
                            url.set("https://github.com/cherrycave/connectfour/LICENSE")
                        }
                    }

                    developers {
                        developer {
                            name.set("Lou Emma Böcker")
                            email.set("lou@boecker.dev")
                            organizationUrl.set("https://www.boecker.dev")
                        }
                    }
                }
            }
        }
    }
}
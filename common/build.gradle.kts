plugins {
    id("java-library")
}

group = "space.pietr.lobby.common"
version = "1.1.0"

repositories {
    mavenCentral()
    maven(url = "https://central.sonatype.com/repository/maven-snapshots/") {
        content {
            includeModule("net.minestom", "minestom")
        }
    }
    maven {
        name = "pietrSpaceReleases"
        url = uri("https://maven.pietr.space/releases")
    }
}
dependencies {
    compileOnlyApi("net.minestom:minestom:2026.09.12-26.2")
    compileOnlyApi("net.kyori:adventure-text-minimessage:4.24.0")
    compileOnlyApi("org.slf4j:slf4j-simple:2.0.16")
    compileOnlyApi("nl.devpieter:sees:1.2.1")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }

    withSourcesJar()
    withJavadocJar()
}

plugins {
    id("java")
    id("com.gradleup.shadow") version "8.3.0"
}

group = "nl.devpieter"
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
    implementation(files("C:\\Users\\devpi\\Desktop\\test_server\\Lobby\\PluginApi-1.0.0.jar"))

//    implementation("net.minestom:minestom:2026.08.07-26.2")
    implementation("net.minestom:minestom:2026.09.12-26.2")
    implementation("net.kyori:adventure-text-minimessage:4.24.0")

    implementation("org.slf4j:slf4j-simple:2.0.16")

    implementation("nl.devpieter:sees:1.2.1")

}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

tasks {
    jar {
        manifest {
            attributes["Main-Class"] = "nl.devpieter.Main"
        }
    }

    build {
        dependsOn(shadowJar)
    }
    shadowJar {
        mergeServiceFiles()
        archiveClassifier.set("")
    }
}
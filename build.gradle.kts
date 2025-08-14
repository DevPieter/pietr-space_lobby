plugins {
    id("java")
    id("com.gradleup.shadow") version "8.3.0"
}

group = "nl.devpieter"
version = "1.1.0"

repositories {
    mavenCentral()
}

dependencies {
//    implementation("net.minestom:minestom-snapshots:1_21_4-046d18702d")
//    implementation("net.minestom:minestom-snapshots:1_21_4-6490538291")

    implementation("net.minestom:minestom:2025.08.12-1.21.8")

//    implementation("net.kyori:adventure-api:4.24.0")
    implementation("net.kyori:adventure-text-minimessage:4.24.0")

    implementation("org.slf4j:slf4j-simple:2.0.16")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
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
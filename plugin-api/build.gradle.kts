plugins {
    id("java-library")
    id("com.gradleup.shadow")
}

group = "space.pietr.lobby.api"
version = "1.0.0"

base {
    archivesName.set("PluginApi")
}

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
    api(project(":common"))

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

tasks.named<Jar>("sourcesJar") {
    from(project(":common").file("src/main/java"))
}

tasks.javadoc {
    source(project(":common").fileTree("src/main/java") {
        include("**/*.java")
    })
}

tasks.jar {
    archiveClassifier.set("thin")
    destinationDirectory.set(layout.buildDirectory.dir("libs"))
}

tasks.shadowJar {
    archiveClassifier.set("")
//    destinationDirectory.set(
//        file("C:\\Users\\devpi\\Desktop\\test_server\\Lobby")
//    )
}

tasks.build {
    dependsOn(tasks.shadowJar)
}

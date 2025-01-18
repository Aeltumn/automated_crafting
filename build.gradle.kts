import java.text.SimpleDateFormat
import java.util.Date

plugins {
    `java`
    `idea`
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.14"
    id("xyz.jpenilla.run-paper") version "2.3.1"
}

group = "com.aeltumn"
version = "2.7.0"

repositories {
    maven {
        name = "papermc"
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
}

dependencies {
    paperweight.paperDevBundle("1.21.4-R0.1-SNAPSHOT")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
    withSourcesJar()
    withJavadocJar()
}

tasks {
    runServer {
        minecraftVersion("1.21.4")
    }

    jar {
        manifest {
            attributes["Implementation-Version"] = version
            attributes["Implementation-Vendor"] = "Aeltumn"
            attributes["Built-By"] = System.getProperty("user.name")
            attributes["Created-By"] = SimpleDateFormat("HH:mm dd-MM-yyyy").format(Date())
            attributes["Build-Timestamp"] = "Gradle ${gradle.gradleVersion}"
            attributes["Build-Jdk"] = "${System.getProperty("java.version")} (${System.getProperty("java.vendor")} ${System.getProperty("java.vm.version")})"
            attributes["Build-OS"] = "${System.getProperty("os.name")} ${System.getProperty("os.arch")} ${System.getProperty("os.version")}"
        }
    }
}
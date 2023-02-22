plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
    id("com.gradle.plugin-publish") version "1.1.0"
    kotlin("jvm") version "1.8.10"
}

version = "1.0.0"
group = "io.github.pereduromega"

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://plugins.gradle.org/m2/")
}

dependencies {
    implementation("org.zeroturnaround:zt-exec:1.12")
    implementation("com.google.code.gson:gson:2.10.1")
}

gradlePlugin {
    website.set("https://github.com/PeredurOmega/GradleNpmPlugin")
    vcsUrl.set("https://github.com/PeredurOmega/GradleNpmPlugin")
    plugins {
        create("io.github.pereduromega.npm.plugin") {
            id = "io.github.pereduromega.npm.plugin"
            displayName = "Npm Plugin"
            description = "Simple way to use npm scripts from gradle with scripts defined in package.json being auto-extracted as gradle tasks"
            tags.set(listOf("npm", "package.json", "scripts"))
            implementationClass = "NpmPlugin"
        }
    }
}
plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
    signing
    id("com.gradle.plugin-publish") version "1.1.0"
    kotlin("jvm") version "1.8.10"
}

version = "1.2.5"
group = "io.github.pereduromega"
description = "Simple way to use npm scripts from gradle with scripts defined in package.json being auto-extracted as gradle tasks"

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://plugins.gradle.org/m2/")
}

dependencies {
    implementation("org.zeroturnaround:zt-exec:1.12")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation(gradleApi())
    implementation(kotlin("stdlib"))
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

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

signing {
    sign(publishing.publications)
}

afterEvaluate {
    tasks.withType<GenerateMavenPom>().configureEach {
        pom.name.set("npm.plugin")
        pom.description.set("Simple way to use npm scripts from gradle with scripts defined in package.json being auto-extracted as gradle tasks")
        pom.url.set("github.com/PeredurOmega/GradleNpmPlugin")
        pom.licenses {
            license {
                name.set("MIT License")
                url.set("https://opensource.org/licenses/MIT")
            }
        }
        pom.scm {
            url.set("github.com/PeredurOmega/GradleNpmPlugin")
        }
        pom.developers {
            developer {
                id.set("PeredurOmega")
                name.set("Paul Souteyrat")
                email.set("paul.souteyrat@insa-lyon.fr")
            }
        }
    }
}


publishing {
    repositories {
        maven {
            name = "ossrh"
            url = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
            credentials {
                username = project.findProperty("ossrhUsername") as String?
                password = project.findProperty("ossrhPassword") as String?
            }
        }
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(11))
    }
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}
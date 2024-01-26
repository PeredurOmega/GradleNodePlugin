plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
    signing
    id("com.gradle.plugin-publish") version "1.2.1"
    kotlin("jvm") version "1.9.22"
}

version = "2.0.3"
group = "io.github.pereduromega"
description = "Simple way to use node scripts (npm, yarn, pnpm) from gradle with scripts defined in package.json being auto-extracted as gradle tasks"

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://plugins.gradle.org/m2/")
}

dependencies {
    implementation("org.zeroturnaround:zt-exec:1.12")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("nu.studer:gradle-credentials-plugin:3.0")
    implementation(gradleApi())
    implementation(kotlin("stdlib"))
}

gradlePlugin {
    website.set("https://github.com/PeredurOmega/GradleNodePlugin")
    vcsUrl.set("https://github.com/PeredurOmega/GradleNodePlugin")
    plugins {
        create("io.github.pereduromega.node.plugin") {
            id = "io.github.pereduromega.node.plugin"
            displayName = "Node Plugin"
            description = "Simple way to use node scripts (npm, yarn, pnpm) from gradle with scripts defined in package.json being auto-extracted as gradle tasks"
            tags.set(listOf("node", "npm", "package.json", "scripts"))
            implementationClass = "NodePlugin"
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
        pom.name.set("node.plugin")
        pom.description.set("Simple way to node scripts (npm, yarn, pnpm) from gradle with scripts defined in package.json being auto-extracted as gradle tasks")
        pom.url.set("github.com/PeredurOmega/GradleNodePlugin")
        pom.licenses {
            license {
                name.set("Apache-2.0 License")
                url.set("https://github.com/PeredurOmega/GradleNodePlugin/blob/main/LICENSE")
            }
        }
        pom.scm {
            url.set("github.com/PeredurOmega/GradleNodePlugin")
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
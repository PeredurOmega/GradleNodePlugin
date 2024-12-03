plugins {
    id("io.github.pereduromega.node.plugin") version "2.1.1"
}

repositories {
    nodeRepository()
}

node {
    packageManager.set(PackageManager.PNPM)
}

tasks.register<NpmrcConfigTask>("npmrc") {
    group = "config"
    description = "Configure the .npmrc file for this project"

    setProperty("PROPERTY", "VALUE")
    setEncryptedProperty("ENC_PROPERTY")
    provideProjectCredentials()
}
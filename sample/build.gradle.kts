plugins {
    id("io.github.pereduromega.node.plugin") version "2.2.0"
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

tasks.register<NodeScriptTask>("extraTest") {
    group = "custom-scripts"
    description = "Custom script task"
    args.set(listOf("test"))
}
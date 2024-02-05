plugins {
    id("io.github.pereduromega.node.plugin") version "2.0.4"
}

repositories {
    nodeRepository()
}

node {
    packageManager.set(PackageManager.PNPM)
}
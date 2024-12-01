plugins {
    id("io.github.pereduromega.node.plugin") version "2.1.0"
}

repositories {
    nodeRepository()
}

node {
    packageManager.set(PackageManager.PNPM)
}
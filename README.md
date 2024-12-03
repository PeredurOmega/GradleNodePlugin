# Node Plugin for Gradle

> 📣 &nbsp;&nbsp;**Announcement:** This plugin changed name and artifact starting from **2.0.0** to better
> reflect its purpose. Please use `io.github.pereduromega.node.plugin` from now on.

[![Gradle Plugin Portal](https://img.shields.io/gradle-plugin-portal/v/io.github.pereduromega.node.plugin?label=Gradle%20Plugin%20Portal)
](https://plugins.gradle.org/plugin/io.github.pereduromega.node.plugin)
[![GitHub License](https://img.shields.io/github/license/PeredurOmega/GradleNodePlugin?label=License)
](https://github.com/PeredurOmega/GradleNodePlugin/blob/main/LICENSE)

Plugin aiming to provide a simple way to use node scripts (npm, yarn, pnpm) from gradle with scripts defined in
package.json being auto-extracted as gradle tasks.

**Features:**

* Download and install Node.js
* Download and handle npm, yarn and pnpm
* Install package.json dependencies with up-to-date checks
* Detect scripts defined in package.json and create associated gradle tasks
* Further configuration of node tasks
* Properly kill node processes when gradle is stopped
* Configure .npmrc file with specific properties (and credentials encryption)

**Java 11 or higher required**

## Usage

### Kotlin

```kotlin

import PackageManagerCommandTask.Companion.setDefaultConfig
import jdk.nashorn.internal.runtime.Debug.id
import jdk.tools.jlink.resources.plugins

// Apply the plugin
plugins {
    id("io.github.pereduromega.node.plugin") version "2.1.1"
}

// When downloadNode is set to true you must provide a repository to download node
repositories {
    nodeRepository("https://nodejs.org/dist/") {
        // Can add authentication here
        credentials {
            //...
        }
    }
    // For the default repository, just use
    nodeRepository()
}

// The configuration block node is mandatory even if it is empty
node {
    // All possible configuration options with their default value are shown below
    packageJson.set(project.file("package.json"))
    nodeModules.set(project.file("node_modules"))
    workingDir.set(project.layout.projectDirectory.asFile.absolutePath)
    defaultTaskGroup.set("scripts")
    autoCreateTasksFromPackageJsonScripts.set(true)
    tasksDependingOnNodeInstallByDefault.set(true)
    scriptsDependingOnNodeDevInstall.set(listOf())
    scriptsDependingOnNodeInstall.set(listOf())
    installCommand.set("install")
    nodeVersion.set("18.19.0")
    nodePath.set("")
    downloadNode.set(true)
    verbose.set(true)
    packageManager.set(PackageManager.NPM)
    installCommand.set("install")
    cleanTaskName.set("nodeClean")
}

// Example to further configure tasks extracted from scripts in package.json
tasks.named<NodeScriptTask>("build") {
    // Assign this task to a specific group (default is "scripts")
    group = BasePlugin.BUILD_GROUP

    // Configure the task inputs and outputs to allow for up-to-date checks
    inputs.dir("src")
    outputs.dir("dist")

    // Other optional properties
    ignoreExitValue.set(false) // default true
    command.set("run")
    args.set(listOf("scriptName"))
    packageManager.set(PackageManager.NPM)
}

val task = tasks.register<NodeScriptTask>("gradleTaskName", "nodeCommand")
task.configure {
    // Ensure dependencies are installed before running the task
    requiresDependencyInstall() // requiresDevDependencyInstall() can be used to only install dev dependencies

    // Config the task with the default config from the project extension
    setDefaultConfig(project)
}
```

### Groovy

```groovy
// Apply the plugin
plugins {
    id 'io.github.pereduromega.node.plugin' version '2.1.1'
}

// When downloadNode is set to true you must provide a repository to download node
repositories {
    nodeRepository('https://nodejs.org/dist/') {
        // Can add authentication here
        credentials {
            //...
        }
    }
    // For the default repository, just use
    nodeRepository()
}

// The configuration block node is mandatory even if it is empty
node {
    // All possible configuration options with their default value are shown below
    packageJson.set(project.file('package.json'))
    nodeModules.set(project.file('node_modules'))
    workingDir.set(project.layout.projectDirectory.asFile.absolutePath)
    defaultTaskGroup.set('scripts')
    autoCreateTasksFromPackageJsonScripts.set(true)
    tasksDependingOnNodeInstallByDefault.set(true)
    scriptsDependingOnNodeDevInstall.set(new ArrayList<>())
    scriptsDependingOnNodeInstall.set(new ArrayList<>())
    installCommand.set('install')
    nodeVersion.set('18.19.0')
    nodePath.set('')
    downloadNode.set(true)
    verbose.set(true)
    packageManager.set(PackageManager.NPM)
    installCommand.set('install')
    cleanTaskName.set('nodeClean')
}

// Example to further configure tasks extracted from scripts in package.json
tasks.named('build') {
    // Assign this task to a specific group (default is "scripts")
    group = BasePlugin.BUILD_GROUP

    // Configure the task inputs and outputs to allow for up-to-date checks
    inputs.dir('src')
    outputs.dir('dist')

    // Other optional properties
    args.set(new ArrayList<>())
}

NodeScriptTask task = tasks.register('gradleTaskName', NodeScriptTask, 'nodeCommand')
task.configure {
    // Ensure dependencies are installed before running the task
    requiresDependencyInstall() // requiresDevDependencyInstall() can be used to only install dev dependencies

    // Config the task with the default config from the project extension
    setDefaultConfig(project)
}
```

## Contributing

Please feel free to open issues and pull requests. I will try to respond as soon as possible.
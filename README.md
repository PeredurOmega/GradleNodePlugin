# Node Plugin for Gradle

> 📣 &nbsp;&nbsp;**Announcement:** This plugin changed name and artifact starting from **2.0.0** to better 
> reflect its purpose. The old name was `io.github.pereduromega.npm.plugin` and the new one is 
> `io.github.pereduromega.node.plugin`. This change has been made to reflect the fact that this plugin is not only for
> npm anymore but also other package managers (such as yarn and pnpm). In a similar way all tasks now reflect this
> change. Please refer on the documentation below for more info on how to use this new version.

Plugin aiming to provide a simple way to use node scripts (npm, yarn, pnpm) from gradle with scripts defined in
package.json being auto-extracted as gradle tasks.

**Features:**

* Can download and install Node.js
* Download and handle npm, yarn and pnpm
* Install package.json dependencies with up-to-date checks
* Detect scripts defined in package.json and create gradle tasks for them
* Allows for further configuration of node tasks
* Properly kills node processes when gradle is stopped
* Provide a way to configure .npmrc file with specific properties (and credentials encryptions)

**Java 11 or higher required**

## Usage

### Kotlin

```kotlin

import jdk.nashorn.internal.runtime.Debug.id
import jdk.tools.jlink.resources.plugins

// Apply the plugin
plugins {
    id("io.github.pereduromega.node.plugin") version "2.0.1"
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
    workingDir.set(project.projectDir)
    defaultTaskGroup.set("scripts")
    autoCreateTasksFromPackageJsonScripts.set(true)
    tasksDependingOnNodeInstallByDefault.set(true)
    scriptsDependingOnNodeDevInstall.set(listOf())
    scriptsDependingOnNodeInstall.set(listOf())
    installCommand.set('install')
    nodeVersion.set("18.19.0")
    nodePath.set("")
    downloadNode.set(true)
    verbose.set(true)
    packageManager.set(PackageManager.NPM)
    installCommand.set(if (isCI) "ci" else "install")
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
    args.set("")
    packageManager.set(PackageManager.NPM)
}

val serviceProvider = project.gradle.sharedServices.registrations.getByName(NodeService.NAME) as NodeService

val task = tasks.register<NodeScriptTask>("gradleTaskName", "nodeCommand")
task.configure {
    // Ensure dependencies are installed before running the task
    requiresDependencyInstall() // requiresDevDependencyInstall() can be used to only install dev dependencies

    // Assign a group to the task
    group = "scripts"

    // Ensure that the process is properly destroyed when gradle is stopped
    getNodeService().set(serviceProvider)
    usesService(serviceProvider)
}
```

### Groovy

```groovy
// Apply the plugin
plugins {
    id 'io.github.pereduromega.node.plugin' version '2.0.1'
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
    workingDir.set(project.projectDir)
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
    installCommand.set('ci')
    cleanTaskName.set("nodeClean")
}

// Example to further configure tasks extracted from scripts in package.json
tasks.named('build') {
    // Assign this task to a specific group (default is "scripts")
    group = BasePlugin.BUILD_GROUP

    // Configure the task inputs and outputs to allow for up-to-date checks
    inputs.dir('src')
    outputs.dir('dist')
}

NodeService serviceProvider = (NodeService) project.gradle.sharedServices.registrations.getByName(NodeService.NAME)

NodeScriptTask task = tasks.register('gradleTaskName', NodeScriptTask, 'nodeCommand')
task.configure {
    // Ensure dependencies are installed before running the task
    requiresDependencyInstall() // requiresDevDependencyInstall() can be used to only install dev dependencies

    // Assign a group to the task
    group = 'scripts'

    // Ensure that the process is properly destroyed when gradle is stopped
    getNodeService().set(serviceProvider)
    usesService(serviceProvider)
}
```

## Contributing

Please feel free to open issues and pull requests. I will try to respond as soon as possible.
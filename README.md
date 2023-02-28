# NpmPlugin for Gradle

Plugin aiming to provide a simple way to use npm scripts from gradle with scripts defined in package.json being
auto-extracted as gradle tasks.

Features:
* Install npm dependencies with up-to-date checks
* Detect scripts defined in package.json and create gradle tasks for them
* Allows for further configuration of npm tasks
* Properly kills npm processes when gradle is stopped

**Java 11 or higher required**

## Usage

### Kotlin
```kotlin
// Apply the plugin
plugins {
    id("io.github.pereduromega.npm.plugin") version "1.1.2"
}

// All possible configuration options with their default value
npm {
    packageJson.set(project.file("package.json"))
    nodeModules.set(project.file("node_modules"))
    workingDir.set(project.projectDir)
    npmPath.set("npm")
    defaultTaskGroup.set("scripts")
    includeAllScripts.set(true)
    taskDependingOnNpmInstall.set(true)
    scriptsDependingOnNpmDevInstall.set(listOf())
    scriptsDependingOnNpmInstall.set(listOf())
}

// Example to further configure tasks extracted from scripts in package.json
tasks.named<NpmScriptTask>("build") {
    // Assign this task to a specific group (default is "scripts")
    group = BasePlugin.BUILD_GROUP
    
    // Configure the task inputs and outputs to allow for up-to-date checks
    inputs.dir("src")
    outputs.dir("dist")
}

// To add script task individually
npm {
    includeAllScripts.set(false)
}

val serviceProvider = project.gradle.sharedServices.registrations.getByName("npmService") as NpmService

val task = tasks.register<NpmScriptTask>("gradleTaskName", "npmCommand")
task.configure {
    // Ensure dependencies are installed before running the task
    requiresNpmInstall() // requiresNpmDevInstall() can be used to only install dev dependencies

    // Assign a group to the task
    group = "npm"
    
    // Ensure that the process is properly destroyed when gradle is stopped
    getNpmService().set(serviceProvider)
    usesService(serviceProvider)
}
```
### Groovy
```groovy
// Apply the plugin
plugins {
    id 'io.github.pereduromega.npm.plugin' version '1.1.2'
}

// All possible configuration options with their default value
npm {
    packageJson.set(project.file("package.json"))
    nodeModules.set(project.file("node_modules"))
    workingDir.set(project.projectDir)
    npmPath.set("npm")
    defaultTaskGroup.set("scripts")
    includeAllScripts.set(true)
    taskDependingOnNpmInstall.set(true)
    scriptsDependingOnNpmDevInstall.set(new ArrayList<>())
    scriptsDependingOnNpmInstall.set(new ArrayList<>())
}

// Example to further configure tasks extracted from scripts in package.json
tasks.named("build") {
    // Assign this task to a specific group (default is "scripts")
    group = BasePlugin.BUILD_GROUP
    
    // Configure the task inputs and outputs to allow for up-to-date checks
    inputs.dir("src")
    outputs.dir("dist")
}

// To add script task individually
npm {
    includeAllScripts.set(false)
}

NpmService serviceProvider = (NpmService) project.gradle.sharedServices.registrations.getByName("npmService")

NpmScriptTask task = tasks.register("gradleTaskName", NpmScriptTask, "npmCommand")
task.configure {
    // Ensure dependencies are installed before running the task
    requiresNpmInstall() // requiresNpmDevInstall() can be used to only install dev dependencies
    
    // Assign a group to the task
    group = "npm"
    
    // Ensure that the process is properly destroyed when gradle is stopped
    getNpmService().set(serviceProvider)
    usesService(serviceProvider)
}
```

## Contributing
Please feel free to open issues and pull requests. I will try to respond as soon as possible.
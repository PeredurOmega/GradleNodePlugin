# NpmPlugin for Gradle

Plugin aiming to provide a simple way to use npm scripts from gradle with scripts defined in package.json being
auto-extracted as gradle tasks.

Features:
* Install npm dependencies with up-to-date checks
* Detect scripts defined in package.json and create gradle tasks for them
* Allows for further configuration of npm tasks
* Properly kills npm processes when gradle is stopped

## Usage

### Kotlin
```kotlin
// Apply the plugin
plugins {
    id("io.github.pereduromega.npm.plugin") version "1.0.0"
}

// All possible configuration options with their default value
npm {
    packageJson.set(project.file("package.json"))
    nodeModules.set(project.file("node_modules"))
    workingDir.set(project.projectDir)
    defaultTaskGroup.set("scripts")
    includeAllScripts.set(true)
    taskDependingOnNpmInstall.set(true)
}

// Example to further configure tasks extracted from scripts in package.json
tasks.named("build") {
    // Assign this task to a specific group (default is "scripts")
    group = BasePlugin.BUILD_GROUP
    
    // Configure the task inputs and outputs to allow for up-to-date checks
    inputs.dir("src")
    outputs.dir("dist")
}
```
### Groovy
```groovy
// Apply the plugin
plugins {
    id 'io.github.pereduromega.npm.plugin' version '1.0.0'
}

// All possible configuration options with their default value
npm {
    packageJson.set(project.file("package.json"))
    nodeModules.set(project.file("node_modules"))
    workingDir.set(project.projectDir)
    defaultTaskGroup.set("scripts")
    includeAllScripts.set(true)
    taskDependingOnNpmInstall.set(true)
}

// Example to further configure tasks extracted from scripts in package.json
tasks.named("build") {
    // Assign this task to a specific group (default is "scripts")
    group = BasePlugin.BUILD_GROUP
    
    // Configure the task inputs and outputs to allow for up-to-date checks
    inputs.dir("src")
    outputs.dir("dist")
}
```

## Contributing
Please feel free to open issues and pull requests. I will try to respond as soon as possible.
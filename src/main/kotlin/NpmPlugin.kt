import com.google.gson.Gson
import com.google.gson.JsonObject
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.BasePlugin
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.register
import java.util.regex.Pattern

class NpmPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val extension = createExtension(project)

        NpmExecutor.initNpmPath(project)

        // Register npm install and configuring it to be cached when possible
        project.tasks.register<NpmInstallTask>("npmInstall") {
            inputs.file(extension.packageJson.get())
            outputs.dir(extension.nodeModules.get())
            workingDir.convention(extension.workingDir.get())
            group = extension.defaultTaskGroup.get()
        }

        // Register npm install dev and configuring it to be cached when possible
        project.tasks.register<NpmInstallTask>("npmDevInstall") {
            inputs.file(extension.packageJson.get())
            outputs.dir(extension.nodeModules.get())
            args.convention(listOf("--only=dev"))
            workingDir.convention(extension.workingDir.get())
            group = extension.defaultTaskGroup.get()
        }

        // Register the clean task to delete node_modules
        project.tasks.register<NpmCleanTask>("clean") {
            group = BasePlugin.CLEAN_TASK_NAME
            nodeModules.convention(extension.nodeModules.get())
        }

        // Register service to be able to launch and kill npm processes / subprocess
        val serviceProvider = project.gradle.sharedServices.registerIfAbsent("npmService", NpmService::class.java) {
            parameters.workingDir.set(extension.workingDir.get())
        }

        if (extension.includeAllScripts.get()) {
            // Read package json
            val packageJsonTxt = extension.packageJson.get().readText()
            val packageJson = Gson().fromJson(packageJsonTxt, JsonObject::class.java)
            val scripts = packageJson.get("scripts").asJsonObject

            // Register package.json scripts as gradle tasks
            scripts.keySet().forEach { command ->
                val taskName = command.toGradleName()
                val task = project.tasks.register<NpmScriptTask>(taskName, command)
                task.configure {
                    if (extension.scriptsDependingOnNpmDevInstall.get().contains(taskName)) requiresNpmDevInstall()
                    else if (extension.scriptsDependingOnNpmInstall.get().contains(taskName)) requiresNpmInstall()
                    else if (extension.tasksDependingOnNpmInstallByDefault.get()) requiresNpmInstall()
                    group = extension.defaultTaskGroup.get()
                    getNpmService().set(serviceProvider)
                    usesService(serviceProvider)
                }
            }
        }
    }

    private fun createExtension(project: Project): NpmPluginExtension {
        val extension = project.extensions.create<NpmPluginExtension>("npm")
        extension.packageJson.convention(project.file("package.json"))
        extension.nodeModules.convention(project.file("node_modules"))
        extension.workingDir.convention(project.projectDir)
        extension.npmPath.convention("npm")
        extension.defaultTaskGroup.convention("scripts")
        extension.includeAllScripts.convention(true)
        extension.tasksDependingOnNpmInstallByDefault.convention(true)
        extension.scriptsDependingOnNpmDevInstall.convention(hashSetOf())
        extension.scriptsDependingOnNpmInstall.convention(hashSetOf())
        return extension
    }

    // Pattern to use camel case instead of the following characters: [/, \, :, <, >, ", ?, *, |]
    private val pattern = Pattern.compile("[\\:\\<\\>\"?*|/\\\\]([a-z])")

    private fun String.toGradleName(): String {
        return pattern.matcher(this).replaceAll { it.group(1).uppercase() }
    }
}
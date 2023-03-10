import com.google.gson.Gson
import com.google.gson.JsonObject
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.BasePlugin
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.register
import setup.NodeSetupTask
import setup.PlatformHelper
import java.io.File
import java.util.regex.Pattern

class NpmPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        createExtension(project)

        project.afterEvaluate {
            if (project.tasks.findByName("npmInstall") == null) {
                throw Exception("NpmPlugin must be configured using npm {} block")
            }
        }
    }

    private fun createExtension(project: Project): NpmPluginExtension {
        val extension = project.extensions.create<NpmPluginExtension>("npm")
        extension.packageJson.convention(project.layout.projectDirectory.file("package.json"))
        extension.nodeModules.convention(project.layout.projectDirectory.dir("node_modules"))
        extension.workingDir.convention(project.layout.projectDirectory)
        extension.defaultTaskGroup.convention("scripts")
        extension.autoCreateTasksFromPackageJsonScripts.convention(true)
        extension.tasksDependingOnNpmInstallByDefault.convention(true)
        extension.scriptsDependingOnNpmDevInstall.convention(hashSetOf())
        extension.scriptsDependingOnNpmInstall.convention(hashSetOf())
        extension.nodeVersion.convention("18.15.0")
        extension.nodePath.convention("")
        extension.downloadNode.convention(true)
        return extension
    }

    companion object {
        fun registerTasks(project: Project) {
            val extension = project.extensions.getByType<NpmPluginExtension>()

            if (extension.downloadNode.get() && extension.nodePath.get().isBlank()) {
                extension.nodePath.convention(project.layout.buildDirectory.dir("node").get().asFile.absolutePath)
            }

            configureNodeSetupTask(project, extension)

            // Register npm install and configuring it to be cached when possible
            project.tasks.register<NpmInstallTask>(NpmInstallTask.NAME) {
                dependsOn(NodeSetupTask.NAME)
                inputs.file(extension.packageJson)
                outputs.dir(extension.nodeModules)
                packageJson.convention(extension.packageJson)
                group = extension.defaultTaskGroup.get()
            }

            // Register npm install dev and configuring it to be cached when possible
            project.tasks.register<NpmInstallTask>("npmDevInstall") {
                dependsOn(NodeSetupTask.NAME)
                packageJson.convention(extension.packageJson)
                outputs.dir { extension.nodeModules.get() }
                args.convention(listOf("--only=dev"))
                group = extension.defaultTaskGroup.get()
            }

            // Register the clean task to delete node_modules
            project.tasks.register<NpmCleanTask>(NpmCleanTask.NAME) {
                group = BasePlugin.CLEAN_TASK_NAME
                nodeModules.convention(extension.nodeModules)
            }

            // Register service to be able to launch and kill npm processes / subprocess
            val serviceProvider =
                project.gradle.sharedServices.registerIfAbsent(NpmService.NAME, NpmService::class.java) {
                    parameters.workingDir.convention(extension.workingDir)
                }

            // Read package json
            val packageJsonTxt = extension.packageJson.get().asFile.readText()
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
                    getNpmService().convention(serviceProvider)
                    usesService(serviceProvider)
                }
            }
        }

        private fun configureNodeSetupTask(project: Project, extension: NpmPluginExtension) {
            project.tasks.register<NodeSetupTask>(NodeSetupTask.NAME) {
                onlyIf { extension.downloadNode.get() }
            }
            project.afterEvaluate {
                if (extension.downloadNode.get()) {
                    val nodeArchive = computeNodeArchiveDependency(extension).map { resolveNodeArchiveFile(this, it) }
                    tasks.named<NodeSetupTask>(NodeSetupTask.NAME) {
                        nodeArchiveFile.convention(this@afterEvaluate.layout.file(nodeArchive))
                        nodeDir.convention(this@afterEvaluate.layout.dir(extension.nodePath.map { File(it) }))
                    }
                }
            }
        }

        private fun computeNodeArchiveDependency(nodeExtension: NpmPluginExtension): Provider<String> {
            val platformHelper = PlatformHelper()
            val osName = platformHelper.getOsPrefix()
            val osArch = platformHelper.getArch()
            val type = if (osName == "win") "zip" else "tar.gz"
            return nodeExtension.nodeVersion.map { "org.nodejs:node:$it:$osName-$osArch@$type" }
        }

        private fun resolveNodeArchiveFile(project: Project, name: String): File {
            val dependency = project.dependencies.create(name)
            val configuration = project.configurations.detachedConfiguration(dependency)
            configuration.isTransitive = false
            return configuration.resolve().single()
        }

        // Pattern to use camel case instead of the following characters: [/, \, :, <, >, ", ?, *, |]
        private val pattern = Pattern.compile("[\\:\\<\\>\"?*|/\\\\]([a-z])")

        private fun String.toGradleName(): String {
            return pattern.matcher(this).replaceAll { it.group(1).uppercase() }
        }
    }
}
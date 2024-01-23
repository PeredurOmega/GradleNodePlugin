import com.google.gson.Gson
import com.google.gson.JsonObject
import nu.studer.gradle.credentials.CredentialsPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.BasePlugin
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.register
import setup.NodeSetupTask
import setup.PackageManagerSetupTask
import setup.PlatformHelper
import java.io.File
import java.util.regex.Pattern

class NodePlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.plugins.apply(CredentialsPlugin::class.java)
        createExtension(project)

        project.afterEvaluate {
            if (project.tasks.findByName("installDependencies") == null) {
                throw Exception("NodePlugin must be configured using node {} block")
            }
        }
    }

    private fun createExtension(project: Project): NodePluginExtension {
        val extension = project.extensions.create<NodePluginExtension>("node")
        extension.packageJson.convention(project.layout.projectDirectory.file("package.json"))
        extension.nodeModules.convention(project.layout.projectDirectory.dir("node_modules"))
        extension.workingDir.convention(project.layout.projectDirectory)
        extension.defaultTaskGroup.convention("scripts")
        extension.autoCreateTasksFromPackageJsonScripts.convention(true)
        extension.tasksDependingOnNodeInstallByDefault.convention(true)
        extension.scriptsDependingOnNodeDevInstall.convention(hashSetOf())
        extension.scriptsDependingOnNodeInstall.convention(hashSetOf())
        extension.nodeVersion.convention("18.19.0")
        extension.nodePath.convention("")
        extension.verbose.convention(true)
        extension.downloadNode.convention(true)
        return extension
    }

    companion object {
        fun registerTasks(project: Project) {
            val extension = project.extensions.getByType<NodePluginExtension>()

            if (extension.downloadNode.get() && extension.nodePath.get().isBlank()) {
                extension.nodePath.convention(project.layout.buildDirectory.dir("node").get().asFile.absolutePath)
            }

            configureNodeSetupTask(project, extension)

            // Register service to be able to launch and kill node processes / subprocess
            val serviceProvider =
                    project.gradle.sharedServices.registerIfAbsent(NodeService.NAME, NodeService::class.java) {
                        parameters.workingDir.convention(extension.workingDir)
                    }

            // Read package json
            val packageJsonTxt = extension.packageJson.get().asFile.readText()
            val packageJson = Gson().fromJson(packageJsonTxt, JsonObject::class.java)

            configurePackageManagerSetupTask(project, extension, serviceProvider, packageJson)

            // Register dependencies installation and configuring it to be cached when possible
            project.tasks.register<DependenciesInstallTask>(DependenciesInstallTask.NAME) {
                dependsOn(PackageManagerSetupTask.NAME)
                inputs.file(extension.packageJson)
                outputs.dir(extension.nodeModules)
                this.packageJson.convention(extension.packageJson)
                group = extension.defaultTaskGroup.get()
                getNodeService().convention(serviceProvider)
            }

            // Register dependencies installation dev and configuring it to be cached when possible
            project.tasks.register<DependenciesInstallTask>(DependenciesInstallTask.DEV_NAME) {
                dependsOn(PackageManagerSetupTask.NAME)
                this.packageJson.convention(extension.packageJson)
                outputs.dir { extension.nodeModules.get() }
                args.convention(listOf("--only=dev"))
                group = extension.defaultTaskGroup.get()
                getNodeService().convention(serviceProvider)
            }

            // Register the clean task to delete node_modules
            project.tasks.register<NodeCleanTask>(NodeCleanTask.NAME) {
                group = BasePlugin.CLEAN_TASK_NAME
                nodeModules.convention(extension.nodeModules)
            }

            // Register package.json scripts as gradle tasks
            val scripts = packageJson.get("scripts").asJsonObject
            scripts.keySet().forEach { scriptName ->
                val taskName = scriptName.toGradleName()
                project.tasks.register<NodeScriptTask>(taskName) {
                    if (extension.scriptsDependingOnNodeDevInstall.get().contains(taskName)) requiresDevDependencyInstall()
                    else if (extension.scriptsDependingOnNodeInstall.get().contains(taskName)) requiresDependencyInstall()
                    else if (extension.tasksDependingOnNodeInstallByDefault.get()) requiresDependencyInstall()
                    group = extension.defaultTaskGroup.get()
                    getNodeService().convention(serviceProvider)
                    usesService(serviceProvider)
                    packageManager.convention(extension.packageManager.get())
                    ignoreExitValue.convention(false)
                    command.convention("run")
                    args.convention(scriptName)
                }
            }
        }

        private fun configureNodeSetupTask(project: Project, extension: NodePluginExtension) {
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

        private fun configurePackageManagerSetupTask(project: Project, extension: NodePluginExtension, nodeService: Provider<NodeService>, packageJson: JsonObject) {
            var extractedPackageManager = ""
            if (!extension.packageManager.isPresent) {
                extractedPackageManager = if (packageJson.has("packageManager")) packageJson.get("packageManager").asString else "npm"
                extension.packageManager.convention(PackageManager.fromString(extractedPackageManager))
            }

            project.tasks.register<PackageManagerSetupTask>(PackageManagerSetupTask.NAME) {
                onlyIf { packageManager.get() != PackageManager.NPM }
                dependsOn(NodeSetupTask.NAME)
                packageManager.convention(extension.packageManager)
                group = extension.defaultTaskGroup.get()
                getNodeService().convention(nodeService)
            }

            project.afterEvaluate {
                tasks.named<PackageManagerSetupTask>(PackageManagerSetupTask.NAME) {
                    if (extractedPackageManager.isNotBlank()) packageManagerWithVersion.convention(extractedPackageManager)
                    packageManager.convention(extension.packageManager)
                    nodeDir.convention(this@afterEvaluate.layout.dir(extension.nodePath.map { File(it) }))
                }
            }
        }

        private fun computeNodeArchiveDependency(nodeExtension: NodePluginExtension): Provider<String> {
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
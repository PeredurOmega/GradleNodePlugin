import PackageManagerCommandTask.Companion.setDefaultConfig
import com.google.gson.Gson
import com.google.gson.JsonObject
import nu.studer.gradle.credentials.CredentialsPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.UnknownTaskException
import org.gradle.api.plugins.BasePlugin
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.*
import setup.NodeSetupTask
import setup.PackageManagerSetupTask
import setup.PlatformHelper
import java.io.File
import java.util.regex.Pattern

class NodePlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.plugins.apply(CredentialsPlugin::class)
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
        extension.workingDir.convention(project.layout.projectDirectory.asFile.absolutePath)
        extension.defaultTaskGroup.convention("scripts")
        extension.autoCreateTasksFromPackageJsonScripts.convention(true)
        extension.tasksDependingOnNodeInstallByDefault.convention(true)
        extension.scriptsDependingOnNodeDevInstall.convention(hashSetOf())
        extension.scriptsDependingOnNodeInstall.convention(hashSetOf())
        extension.installCommand.convention("install")
        extension.cleanTaskName.convention(BasePlugin.CLEAN_TASK_NAME)
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

            // Read package json
            val packageJsonTxt = extension.packageJson.get().asFile.readText()
            val packageJson = Gson().fromJson(packageJsonTxt, JsonObject::class.java)

            configurePackageManagerSetupTask(project, extension, packageJson)

            // Register dependencies installation and configuring it to be cached when possible
            project.tasks.register<DependenciesInstallTask>(DependenciesInstallTask.NAME) {
                dependsOn(PackageManagerSetupTask.NAME)
                description = "Install node dependencies"
                installCommand.convention(extension.installCommand)
                ignoreExitValue.convention(false)
                args.convention(listOf())
                inputs.file(extension.packageJson)
                outputs.dir(extension.nodeModules)
                this.packageJson.convention(extension.packageJson)
                group = extension.defaultTaskGroup.get()
                setDefaultConfig(extension)
            }

            // Register dependencies installation dev and configuring it to be cached when possible
            project.tasks.register<DependenciesInstallTask>(DependenciesInstallTask.DEV_NAME) {
                dependsOn(PackageManagerSetupTask.NAME)
                description = "Install dev node dependencies"
                installCommand.convention(extension.installCommand)
                ignoreExitValue.convention(false)
                args.convention(listOf())
                this.packageJson.convention(extension.packageJson)
                outputs.dir { extension.nodeModules.get() }
                args.convention(listOf("--only=dev"))
                group = extension.defaultTaskGroup.get()
                setDefaultConfig(extension)
            }

            // Register the clean task to delete node_modules
            project.tasks.register<NodeCleanTask>(NodeCleanTask.getName(project)) {
                description = "Delete the node modules folder (i.e. clean the dependencies)"
                group = BasePlugin.CLEAN_TASK_NAME
                nodeModules.convention(extension.nodeModules)
            }

            // Register package.json scripts as gradle tasks
            val scripts = packageJson["scripts"].asJsonObject
            scripts.keySet().forEach { scriptName ->
                val taskName = scriptName.toGradleName()
                project.tasks.register<NodeScriptTask>(taskName) {
                    when {
                        extension.scriptsDependingOnNodeDevInstall.get().contains(taskName) -> {
                            requiresDevDependencyInstall()
                        }

                        extension.scriptsDependingOnNodeInstall.get().contains(taskName) -> {
                            requiresDependencyInstall()
                        }

                        extension.tasksDependingOnNodeInstallByDefault.get() -> requiresDependencyInstall()
                    }
                    group = extension.defaultTaskGroup.get()
                    description = "Run node script of the name of the task"
                    setDefaultConfig(extension)
                    ignoreExitValue.convention(false)
                    command.convention("run")
                    args.convention(listOf(scriptName))
                }
            }

            // If cleanTaskName is set, make our clean task a dependency of the default
            // We configure this task afterward to allow for an eventual registration from the scripts of the
            // package.json
            if (extension.cleanTaskName.get() != BasePlugin.CLEAN_TASK_NAME) {
                try {
                    project.tasks.named(BasePlugin.CLEAN_TASK_NAME) {
                        finalizedBy(extension.cleanTaskName.get())
                    }
                } catch (e: UnknownTaskException) {
                    // If the base clean task doesn't exist we just ignore the error as this won't impact the overall
                    // plugin, the clean task still being accessible through the custom name provided.
                }
            }
        }

        private fun configureNodeSetupTask(project: Project, extension: NodePluginExtension) {
            project.tasks.register<NodeSetupTask>(NodeSetupTask.NAME) {
                description = "Download and install a local node/npm version."
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

        private fun configurePackageManagerSetupTask(
            project: Project,
            extension: NodePluginExtension,
            packageJson: JsonObject
        ) {
            var extractedPackageManager = ""
            if (!extension.packageManager.isPresent) {
                extractedPackageManager = if (packageJson.has("packageManager")) {
                    packageJson["packageManager"].asString
                } else "npm"
                extension.packageManager.convention(PackageManager.fromString(extractedPackageManager))
            }

            project.tasks.register<PackageManagerSetupTask>(PackageManagerSetupTask.NAME) {
                description =
                    "Install the package manager defined in the package.json file or explicitly in the build.gradle.kts file."
                onlyIf { packageManager.get() != PackageManager.NPM }
                dependsOn(NodeSetupTask.NAME)
                setDefaultConfig(extension)
                group = extension.defaultTaskGroup.get()
            }

            project.afterEvaluate {
                tasks.named<PackageManagerSetupTask>(PackageManagerSetupTask.NAME) {
                    if (extractedPackageManager.isNotBlank()) {
                        packageManagerWithVersion.convention(extractedPackageManager)
                    }
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
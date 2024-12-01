import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.kotlin.dsl.getByType

abstract class PackageManagerCommandTask : DefaultTask() {

    companion object {
        fun PackageManagerCommandTask.setDefaultConfig(extension: NodePluginExtension) {
            packageManager.convention(extension.packageManager)
            nodePath.convention(extension.nodePath)
            verbose.convention(extension.verbose)
            workingDir.convention(extension.workingDir)

            // Register service to be able to launch and kill node processes / subprocess
            val serviceProvider =
                project.gradle.sharedServices.registerIfAbsent(NodeService.NAME, NodeService::class.java) {}
            nodeService.convention(serviceProvider)
            usesService(serviceProvider)
        }

        // Can be used by plugin users
        @Suppress("unused")
        fun PackageManagerCommandTask.setDefaultConfig(project: Project) =
            setDefaultConfig(project.extensions.getByType<NodePluginExtension>())
    }

    @get:Input
    abstract val packageManager: Property<PackageManager>

    @get:Input
    abstract val nodePath: Property<String>

    @get:Input
    abstract val verbose: Property<Boolean>

    @get:Input
    abstract val workingDir: Property<String>

    @get:Internal
    abstract val nodeService: Property<NodeService>
}
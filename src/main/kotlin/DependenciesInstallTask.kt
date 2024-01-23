import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.getByType

@Suppress("LeakingThis")
abstract class DependenciesInstallTask : DefaultTask() {

    companion object {
        const val NAME = "installDependencies"
        const val DEV_NAME = "installDevDependencies"
    }

    @Internal
    abstract fun getNodeService(): Property<NodeService>

    @get:Input
    abstract val ignoreExitValue: Property<Boolean>

    @get:InputFile
    abstract val packageJson: RegularFileProperty

    @get:Input
    abstract val args: ListProperty<String>

    init {
        description = "Install node dependencies"
        ignoreExitValue.convention(false)
        args.convention(listOf())
    }

    @TaskAction
    fun run() {
        val workingDir = packageJson.get().asFile.parentFile //TODO: use workingDir from NodePluginExtension
        val packageManager = project.extensions.getByType<NodePluginExtension>().packageManager.get()
        val installCommand = project.extensions.getByType<NodePluginExtension>().installCommand.get()
        val process = getNodeService().get().executeCommand(this, packageManager, installCommand, *args.get().toTypedArray())
        process.waitFor()
        if (process.exitValue() != 0) {
            if (!ignoreExitValue.get()) {
                throw RuntimeException("Dependencies installation failed with exit value ${process.exitValue()}")
            } else {
                logger.warn("Dependencies installation failed with exit value ${process.exitValue()}")
            }
        } else {
            logger.info("Dependencies installation completed successfully")
        }
    }
}
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.TaskAction

abstract class DependenciesInstallTask : PackageManagerCommandTask() {

    companion object {
        const val NAME = "installDependencies"
        const val DEV_NAME = "installDevDependencies"
    }

    @get:Input
    abstract val ignoreExitValue: Property<Boolean>

    @get:Input
    abstract val installCommand: Property<String>

    @get:InputFile
    abstract val packageJson: RegularFileProperty

    @get:Input
    abstract val args: ListProperty<String>

    @TaskAction
    fun run() {
        val process = nodeService.get().executeCommand(this, installCommand.get(), *args.get().toTypedArray())
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
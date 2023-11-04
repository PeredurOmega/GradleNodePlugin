import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.getByType
import javax.inject.Inject

abstract class NpmScriptTask @Inject constructor(@Input val command: String) : DefaultTask() {
    @Internal
    abstract fun getNpmService(): Property<NpmService>

    @get:Input
    abstract val ignoreExitValue: Property<Boolean>

    init {
        description = "Run npm script '$command'"
        @Suppress("LeakingThis")
        ignoreExitValue.convention(false)
    }

    @TaskAction
    fun run() {
        val npmService = getNpmService().get()
        val packageManager = project.extensions.getByType<NpmPluginExtension>().packageManager.get()
        val process = npmService.executeCommand(this, packageManager, command)
        process.waitFor()
        if (process.exitValue() != 0) {
            if (!ignoreExitValue.get()) {
                throw RuntimeException("Npm script '$command' failed with exit value ${process.exitValue()}")
            } else {
                logger.warn("Npm script '$command' failed with exit value ${process.exitValue()}")
            }
        } else {
            logger.info("Npm script '$command' completed successfully")
        }
    }
}
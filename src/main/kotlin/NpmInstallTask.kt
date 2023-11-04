import NpmExecutor.createProcess
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.getByType

@Suppress("LeakingThis")
abstract class NpmInstallTask : DefaultTask() {

    companion object {
        const val NAME = "npmInstall"
    }

    @get:Input
    abstract val ignoreExitValue: Property<Boolean>

    @get:InputFile
    abstract val packageJson: RegularFileProperty

    @get:Input
    abstract val args: ListProperty<String>

    init {
        description = "Install npm dependencies"
        ignoreExitValue.convention(false)
        args.convention(listOf())
    }

    @TaskAction
    fun run() {
        val workingDir = packageJson.get().asFile.parentFile
        val packageManager = project.extensions.getByType<NpmPluginExtension>().packageManager.get()
        val executor = createProcess(packageManager, "install", *args.get().toTypedArray()).directory(workingDir).start()
        val process = executor.process
        process.waitFor()
        if (process.exitValue() != 0) {
            if (!ignoreExitValue.get()) {
                throw RuntimeException("Npm install failed with exit value ${process.exitValue()}")
            } else {
                logger.warn("Npm install failed with exit value ${process.exitValue()}")
            }
        } else {
            logger.info("Npm install completed successfully")
        }
    }
}
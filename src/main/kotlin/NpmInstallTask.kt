import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.TaskAction

abstract class NpmInstallTask : DefaultTask() {

    companion object {
        const val NAME = "npmInstall"
    }

    @get:InputFile
    abstract val packageJson: RegularFileProperty

    @get:Input
    abstract val args: ListProperty<String>

    init {
        @Suppress("LeakingThis")
        args.convention(listOf())
        description = "Install npm dependencies"
    }

    @TaskAction
    fun run() {
        val workingDir = packageJson.get().asFile.parentFile
        val executor = NpmExecutor.create("install", *args.get().toTypedArray()).directory(workingDir).start()
        executor.process.waitFor()
    }
}
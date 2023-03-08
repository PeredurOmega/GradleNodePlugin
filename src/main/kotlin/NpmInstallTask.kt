import NpmExecutor.workingDir
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.TaskAction

abstract class NpmInstallTask : DefaultTask() {

    companion object {
        const val NAME = "npmInstall"
    }

    @get:InputDirectory
    abstract val workingDir: DirectoryProperty

    @get:Input
    abstract val args: ListProperty<String>

    init {
        @Suppress("LeakingThis")
        args.convention(listOf())
        description = "Install npm dependencies"
    }

    @TaskAction
    fun run() {
        val executor = NpmExecutor.create("install", *args.get().toTypedArray()).workingDir(workingDir).start()
        executor.process.waitFor()
    }
}
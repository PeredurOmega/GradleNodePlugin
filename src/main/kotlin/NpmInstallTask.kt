import org.gradle.api.DefaultTask
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import java.io.File

abstract class NpmInstallTask : DefaultTask() {

    @get:Input
    abstract val workingDir: Property<File>

    @get:Input
    abstract val args: ListProperty<String>

    init {
        @Suppress("LeakingThis")
        args.convention(listOf())
    }

    @TaskAction
    fun run() {
        val executor = NpmExecutor.create("install", *args.get().toTypedArray()).directory(workingDir.get()).start()
        executor.process.waitFor()
    }
}
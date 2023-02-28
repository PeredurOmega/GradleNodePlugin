import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import java.io.File

abstract class NpmCleanTask : DefaultTask() {

    @get:Input
    abstract val nodeModules: Property<File>

    @TaskAction
    fun run() {
        nodeModules.get().deleteRecursively()
    }
}
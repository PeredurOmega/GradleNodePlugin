import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.TaskAction

abstract class NpmCleanTask : DefaultTask() {

    companion object {
        const val NAME = "clean"
    }

    @get:InputDirectory
    abstract val nodeModules: DirectoryProperty

    init {
        description = "Delete the node modules folder (i.e. clean the dependencies)"
    }

    @TaskAction
    fun run() {
        nodeModules.get().asFile.deleteRecursively()
    }
}
import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.Delete
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.getByType

abstract class NodeCleanTask : Delete() {

    companion object {
        fun getName(project: Project): String {
            return project.extensions.getByType<NodePluginExtension>().cleanTaskName.get()
        }
    }

    @get:InputDirectory
    abstract val nodeModules: DirectoryProperty

    init {
        description = "Delete the node modules folder (i.e. clean the dependencies)"
    }

    @TaskAction
    fun run() {
        delete(nodeModules.get().asFile)
    }
}
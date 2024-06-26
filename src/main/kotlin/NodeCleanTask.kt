import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.Destroys
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.getByType

abstract class NodeCleanTask : DefaultTask() {

    companion object {
        fun getName(project: Project): String {
            return project.extensions.getByType<NodePluginExtension>().cleanTaskName.get()
        }
    }

    @get:Destroys
    abstract val nodeModules: DirectoryProperty

    init {
        description = "Delete the node modules folder (i.e. clean the dependencies)"
    }

    @TaskAction
    fun run() {
        project.delete {
            delete(nodeModules.get().asFile)
        }
    }
}
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileSystemOperations
import org.gradle.api.tasks.Destroys
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.getByType
import javax.inject.Inject

abstract class NodeCleanTask : DefaultTask() {

    @get:Inject
    abstract val fs: FileSystemOperations

    companion object {
        fun getName(project: Project): String {
            return project.extensions.getByType<NodePluginExtension>().cleanTaskName.get()
        }
    }

    @get:Destroys
    abstract val nodeModules: DirectoryProperty

    @TaskAction
    fun run() {
        fs.delete {
            delete(nodeModules.get().asFile)
        }
    }
}
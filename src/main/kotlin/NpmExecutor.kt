import org.apache.tools.ant.taskdefs.condition.Os
import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.kotlin.dsl.getByType
import org.zeroturnaround.exec.ProcessExecutor

object NpmExecutor {
    private var npm = osNpmPath("npm")

    fun initNpmPath(project: Project) {
        npm = osNpmPath(project.extensions.getByType<NpmPluginExtension>().npmPath.get())
    }

    fun create(vararg commands: String): ProcessExecutor {
        val process = ProcessExecutor(npm, *commands)
        process.redirectOutput(System.out)
        process.redirectError(System.err)
        return process
    }

    private fun osNpmPath(npmPath: String) = if (Os.isFamily(Os.FAMILY_WINDOWS)) "$npmPath.cmd" else npmPath

    fun ProcessExecutor.workingDir(directory: DirectoryProperty): ProcessExecutor =
        this.directory(directory.get().asFile)
}
import org.apache.tools.ant.taskdefs.condition.Os
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.getByType
import org.zeroturnaround.exec.ProcessExecutor

object NpmExecutor {
    private lateinit var npm: Provider<String>

    fun initNpmPath(project: Project) {
        npm = project.extensions.getByType<NpmPluginExtension>().nodePath.map { osNpmPath(it) }
    }

    fun create(vararg commands: String): ProcessExecutor {
        val process = ProcessExecutor(npm.get(), *commands)
        process.redirectOutput(System.out)
        process.redirectError(System.err)
        return process
    }

    private fun osNpmPath(nodeDir: String) = if (Os.isFamily(Os.FAMILY_WINDOWS)) "$nodeDir/npm.cmd" else "$nodeDir/npm"
}
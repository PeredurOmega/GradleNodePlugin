import org.apache.tools.ant.taskdefs.condition.Os
import org.gradle.api.Task
import org.gradle.kotlin.dsl.getByType
import org.zeroturnaround.exec.ProcessExecutor
import org.zeroturnaround.exec.stream.slf4j.Slf4jStream
import java.io.File

object NpmExecutor {

    fun Task.createProcess(vararg commands: String): ProcessExecutor {
        val nodePath = project.extensions.getByType<NpmPluginExtension>().nodePath.get()

        val isWindows = Os.isFamily(Os.FAMILY_WINDOWS)
        var npm = if (isWindows) "npm.cmd" else "npm"
        var setEnvPath = false
        if (nodePath.isNotBlank()) {
            npm = if (isWindows) "$nodePath/$npm" else "$nodePath/bin/$npm"
            setEnvPath = true
        }
        val process = ProcessExecutor(npm, *commands)

        if (setEnvPath) {
            val env = mutableMapOf<String, String>()
            env += System.getenv()
            val pathList = arrayListOf<String>(if (isWindows) nodePath else "$nodePath/bin")
            pathList.add(env["PATH"] ?: env["Path"] ?: "")
            env["PATH"] = pathList.joinToString(File.pathSeparator)
            process.environment(env)
            logger.debug("Setting process env PATH to ${env["PATH"]}")
        }

        process.redirectOutput(Slf4jStream.of(logger).asInfo())
        process.redirectError(Slf4jStream.of(logger).asError())

        return process
    }
}
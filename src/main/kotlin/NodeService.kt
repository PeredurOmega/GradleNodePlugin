import org.apache.tools.ant.taskdefs.condition.Os
import org.gradle.api.Task
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.services.BuildService
import org.gradle.api.services.BuildServiceParameters
import org.gradle.kotlin.dsl.getByType
import org.zeroturnaround.exec.ProcessExecutor
import org.zeroturnaround.exec.stream.slf4j.Slf4jStream
import java.io.File

abstract class NodeService : BuildService<BuildServiceParameters.None>, AutoCloseable {

    companion object {
        const val NAME = "nodeService"
    }

    private fun Task.createProcess(packageManager: PackageManager, vararg commands: String): ProcessExecutor {
        val extensions = project.extensions.getByType<NodePluginExtension>()
        val nodePath = extensions.nodePath.get()

        val isWindows = Os.isFamily(Os.FAMILY_WINDOWS)
        var setEnvPath = false
        var executorPath = packageManager.toString()
        if (nodePath.isNotBlank()) {
            executorPath = if (isWindows) "$nodePath/$packageManager.cmd" else "$nodePath/bin/$packageManager"
            setEnvPath = true
        }

        logger.lifecycle("Executing: {}", listOf(executorPath, *commands).joinToString(" "))
        val process = ProcessExecutor(executorPath, *commands)

        if (setEnvPath) {
            val env = mutableMapOf<String, String>()
            env += System.getenv()
            val pathList = arrayListOf<String>(if (isWindows) nodePath else "$nodePath/bin")
            pathList.add(env["PATH"] ?: env["Path"] ?: "")
            env["PATH"] = pathList.joinToString(File.pathSeparator)
            process.environment(env)
            logger.debug("Setting process env PATH to ${env["PATH"]}")
        }
        val outputStream = if (extensions.verbose.get()) LifecycleLoggerStream.of(logger) else Slf4jStream.of(logger).asInfo()
        process.redirectOutput(outputStream)
        process.redirectError(Slf4jStream.of(logger).asError())
        process.directory(extensions.workingDir.get().asFile)
        return process
    }

    private val processes = arrayListOf<Process>()

    fun executeCommand(task: Task, packageManager: PackageManager, vararg command: String): Process {
        val nodeExecutor = task.createProcess(packageManager, *command)
        /*workingDir?.let {
            nodeExecutor.directory(it)
        }*/
        val process = nodeExecutor.start().process
        processes.add(process)
        return process
    }

    override fun close() {
        processes.kill()
    }

    private fun ArrayList<Process>.kill() {
        forEach {
            it.descendants().forEach { p ->
                p.destroy()
            }
            if (it.isAlive) it.destroy()
        }
    }
}
import org.apache.tools.ant.taskdefs.condition.Os
import org.gradle.api.services.BuildService
import org.gradle.api.services.BuildServiceParameters
import org.zeroturnaround.exec.ProcessExecutor
import org.zeroturnaround.exec.stream.slf4j.Slf4jStream
import java.io.File

abstract class NodeService : BuildService<BuildServiceParameters.None>, AutoCloseable {

    companion object {
        const val NAME = "nodeService"
    }

    private fun PackageManagerCommandTask.createProcess(
        packageManager: PackageManager = this.packageManager.get(),
        vararg commands: String
    ): ProcessExecutor {
        val nodePath = nodePath.get()
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
            val pathList = arrayListOf(if (isWindows) nodePath else "$nodePath/bin")
            pathList.add(env["PATH"] ?: env["Path"] ?: "")
            env["PATH"] = pathList.joinToString(File.pathSeparator)
            process.environment(env)
            logger.debug("Setting process env PATH to ${env["PATH"]}")
        }
        val outputStream =
            if (verbose.get()) LifecycleLoggerStream.of(logger) else Slf4jStream.of(logger).asInfo()
        process.redirectOutput(outputStream)
        process.redirectError(Slf4jStream.of(logger).asError())
        process.directory(File(workingDir.get()))
        return process
    }

    private val processes = arrayListOf<Process>()

    fun executeCommand(task: PackageManagerCommandTask, vararg command: String) =
        executeCommand(task, task.packageManager.get(), command = command)

    fun executeCommand(
        task: PackageManagerCommandTask,
        packageManager: PackageManager = task.packageManager.get(),
        vararg command: String
    ): Process {
        val nodeExecutor = task.createProcess(packageManager, commands = command)
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
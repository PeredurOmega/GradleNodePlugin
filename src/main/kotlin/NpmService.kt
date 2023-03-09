import org.gradle.api.file.DirectoryProperty
import org.gradle.api.services.BuildService
import org.gradle.api.services.BuildServiceParameters

abstract class NpmService : BuildService<NpmService.Params>, AutoCloseable {

    companion object {
        const val NAME = "npmService"
    }

    internal interface Params : BuildServiceParameters {
        val workingDir: DirectoryProperty
    }

    private val processes = arrayListOf<Process>()

    fun executeCommand(command: String): Process {
        val npmExecutor = NpmExecutor.create("run", command)
        npmExecutor.directory(parameters.workingDir.get().asFile)
        val process = npmExecutor.start().process
        processes.add(process)
        return process
    }

    override fun close() {
        processes.kill()
    }

    private fun ArrayList<Process>.kill() {
        forEach {
            if (it.isAlive) {
                it.descendants().forEach { p ->
                    p.destroy()
                }
                it.destroy()
            }
        }
    }
}
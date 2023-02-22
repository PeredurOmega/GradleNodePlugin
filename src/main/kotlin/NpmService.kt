import org.gradle.api.provider.Property
import org.gradle.api.services.BuildService
import org.gradle.api.services.BuildServiceParameters
import java.io.File

abstract class NpmService : BuildService<NpmService.Params>, AutoCloseable {

    internal interface Params : BuildServiceParameters {
        val workingDir: Property<File>
    }

    private val processes = arrayListOf<Process>()

    fun executeCommand(command: String): Process {
        val processBuilder = NpmExecutor.create("run", command)
        processBuilder.directory(parameters.workingDir.get())
        val process = processBuilder.start().process
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
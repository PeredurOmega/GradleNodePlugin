import org.apache.tools.ant.taskdefs.condition.Os
import org.zeroturnaround.exec.ProcessExecutor

object NpmExecutor {
    private val npm = if (Os.isFamily(Os.FAMILY_WINDOWS)) "npm.cmd" else "npm"

    fun create(vararg commands: String): ProcessExecutor {
        val process = ProcessExecutor(npm, *commands)
        process.redirectOutput(System.out)
        process.redirectError(System.err)
        return process
    }
}
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import javax.inject.Inject

abstract class NpmScriptTask @Inject constructor(@Input val command: String) : DefaultTask() {
    @Internal
    abstract fun getNpmService(): Property<NpmService>

    @Suppress("MemberVisibilityCanBePrivate")
    var ignoreExitValue = false

    init {
        description = "Run npm script '$command'"
    }

    @TaskAction
    fun run() {
        val angular: NpmService = getNpmService().get()
        val process = angular.executeCommand(command)
        process.waitFor()
        process.exitValue()
        @Suppress("MemberVisibilityCanBePrivate")
        if (!ignoreExitValue && process.exitValue() != 0) {
            throw RuntimeException("Npm script '$command' failed")
        }
    }
}
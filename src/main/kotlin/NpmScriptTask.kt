import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import javax.inject.Inject

abstract class NpmScriptTask @Inject constructor(@Input val command: String) : DefaultTask() {
    @Internal
    abstract fun getNpmService(): Property<NpmService>

    @TaskAction
    fun run() {
        val angular: NpmService = getNpmService().get()
        angular.executeCommand(command).waitFor()
    }
}
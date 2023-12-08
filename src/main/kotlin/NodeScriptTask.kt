import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.getByType
import javax.inject.Inject

abstract class NodeScriptTask @Inject constructor(@Input val command: String) : DefaultTask() {
    @Internal
    abstract fun getNodeService(): Property<NodeService>

    @get:Input
    abstract val ignoreExitValue: Property<Boolean>

    init {
        description = "Run node script '$command'"
        @Suppress("LeakingThis")
        ignoreExitValue.convention(false)
    }

    @TaskAction
    fun run() {
        val nodeService = getNodeService().get()
        val packageManager = project.extensions.getByType<NodePluginExtension>().packageManager.get()
        val process = nodeService.executeCommand(this, packageManager, "run", command)
        process.waitFor()
        if (process.exitValue() != 0) {
            if (!ignoreExitValue.get()) {
                throw RuntimeException("Script '$command' failed with exit value ${process.exitValue()}")
            } else {
                logger.warn("Script '$command' failed with exit value ${process.exitValue()}")
            }
        } else {
            logger.info("Script '$command' completed successfully")
        }
    }
}
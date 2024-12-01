import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

abstract class NodeScriptTask : PackageManagerCommandTask() {

    @get:Input
    abstract val ignoreExitValue: Property<Boolean>

    @get:Input
    abstract val command: Property<String>

    @get:Input
    abstract val args: ListProperty<String>

    @TaskAction
    fun run() {
        val process = nodeService.get().executeCommand(this, command.get(), *args.get().toTypedArray())
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
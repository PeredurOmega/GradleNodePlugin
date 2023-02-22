import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

abstract class NpmInstallTask : DefaultTask() {

    @TaskAction
    fun run() {
        NpmExecutor.create("install").start()
    }
}
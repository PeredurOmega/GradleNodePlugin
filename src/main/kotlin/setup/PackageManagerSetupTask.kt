package setup

import NpmExecutor.createProcess
import NpmPlugin
import PackageManager
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.getByType

abstract class PackageManagerSetupTask : DefaultTask() {

    companion object {
        const val NAME = "packageManagerSetup"
    }

    @get:Input
    abstract val packageManager: Property<PackageManager>

    @get:Input
    @get:Optional
    abstract val packageManagerWithVersion: Property<String>

    @get:OutputDirectory
    abstract val nodeDir: DirectoryProperty

    init {
        description = "Install the package manager defined in the package.json file or explicitly in the build.gradle.kts file."
    }

    @TaskAction
    fun run() {
        val packageManager = if (packageManagerWithVersion.isPresent) packageManagerWithVersion.get() else packageManager.get().toString()
        val executor = createProcess(PackageManager.NPM, "install", "-g", packageManager).start()
        val process = executor.process
        process.waitFor()
        if (process.exitValue() != 0) {
            throw RuntimeException("Npm install failed with exit value ${process.exitValue()}")
        } else {
            logger.info("Npm install completed successfully")
        }
    }
}
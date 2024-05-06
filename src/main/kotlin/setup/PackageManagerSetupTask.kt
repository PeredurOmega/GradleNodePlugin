package setup

import NodeService
import PackageManager
import org.apache.tools.ant.taskdefs.condition.Os
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import java.nio.file.Files

abstract class PackageManagerSetupTask : DefaultTask() {

    companion object {
        const val NAME = "packageManagerSetup"
    }

    @Internal
    abstract fun getNodeService(): Property<NodeService>

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
        val process = getNodeService().get().executeCommand(this, PackageManager.NPM, "install", "-g", packageManager)
        process.waitFor()
        if (process.exitValue() != 0) {
            throw RuntimeException("Package manager '$packageManager' installation failed with exit value ${process.exitValue()}")
        } else {
            logger.info("Package manager '$packageManager' was successfully installed")
        }

        fixPackageManagerLink(PackageManager.fromString(packageManager).toString())
    }

    private fun fixPackageManagerLink(packageManager: String) {
        // Node manager link with corepack for linux
        if (!Os.isFamily(Os.FAMILY_WINDOWS)) {
            val binPath = nodeDir.dir("bin").get().asFile.toPath()
            val scriptPath = binPath.resolve(packageManager)
            Files.deleteIfExists(scriptPath)
            val targetPath = nodeDir.dir("lib/node_modules/corepack/dist/$packageManager.js").get().asFile.toPath()
            val fixedScriptPath = binPath.relativize(targetPath)
            Files.createSymbolicLink(scriptPath, fixedScriptPath)
            logger.debug("Fixed broken symlink: {} with target {}", name, fixedScriptPath)
        }
    }
}
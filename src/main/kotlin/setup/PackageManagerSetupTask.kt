package setup

import PackageManager
import PackageManagerCommandTask
import org.apache.tools.ant.taskdefs.condition.Os
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.nio.file.Files

abstract class PackageManagerSetupTask : PackageManagerCommandTask() {

    companion object {
        const val NAME = "packageManagerSetup"
    }

    @get:Input
    @get:Optional
    abstract val packageManagerWithVersion: Property<String>

    @get:OutputDirectory
    abstract val nodeDir: DirectoryProperty

    @TaskAction
    fun run() {
        val packageManager =
            if (packageManagerWithVersion.isPresent) packageManagerWithVersion.get()
            else packageManager.get().toString()
        val process = nodeService.get().executeCommand(this, PackageManager.NPM, "install", "-g", packageManager)
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
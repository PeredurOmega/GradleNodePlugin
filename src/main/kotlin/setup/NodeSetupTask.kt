package setup

import org.apache.tools.ant.taskdefs.condition.Os
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileTree
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.file.RelativePath
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.nio.file.Files

abstract class NodeSetupTask : DefaultTask() {

    companion object {
        const val NAME = "nodeSetup"
    }

    @get:InputFile
    abstract val nodeArchiveFile: RegularFileProperty

    @get:OutputDirectory
    abstract val nodeDir: DirectoryProperty

    init {
        description = "Download and install a local node/npm version."
    }

    @TaskAction
    fun exec() {
        deleteExistingNode()
        unpackNodeArchive()
        setExecutableFlag()
    }

    private fun deleteExistingNode() {
        project.delete {
            delete(nodeDir)
        }
        logger.debug("{} cleaned", nodeDir)
    }

    private fun unpackNodeArchive() {
        val archiveFile = nodeArchiveFile.get().asFile
        if (archiveFile.name.endsWith("zip")) copyNodeInstallContent(project.zipTree(archiveFile))
        else { // i.e. not windows
            copyNodeInstallContent(project.tarTree(archiveFile))

            // Fix broken symlink
            fixBrokenSymlink("npm")
            fixBrokenSymlink("npx")
        }
    }

    private fun copyNodeInstallContent(archiveTree: FileTree) {
        logger.debug("Extracting node archive: {} into {}", archiveTree, nodeDir)
        project.copy {
            from(archiveTree) {
                eachFile {
                    relativePath = RelativePath(true, *relativePath.segments.drop(1).toTypedArray())
                    logger.debug("Extracting file: {}", relativePath)
                }
                includeEmptyDirs = false
            }
            into(nodeDir)
        }
    }

    // Unused on windows
    private fun fixBrokenSymlink(name: String) {
        val binPath = nodeDir.dir("bin").get().asFile.toPath()
        val scriptPath = binPath.resolve(name)
        if (Files.deleteIfExists(scriptPath)) {
            val targetPath = nodeDir.dir("lib/node_modules/npm/bin/$name-cli.js").get().asFile.toPath()
            val fixedScriptPath = binPath.relativize(targetPath)
            Files.createSymbolicLink(scriptPath, fixedScriptPath)
            logger.debug("Fixed broken symlink: {} with target {}", name, fixedScriptPath)
        }
    }

    // Unused on windows
    private fun setExecutableFlag() {
        if (!Os.isFamily(Os.FAMILY_WINDOWS)) {
            val nodeExec = nodeDir.file("bin/node").get().asFile
            nodeExec.setExecutable(true, false)
            logger.debug("Set executable flag on {}", nodeExec)
        }
    }
}
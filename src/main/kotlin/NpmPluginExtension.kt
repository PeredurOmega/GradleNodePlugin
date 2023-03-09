import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty

interface NpmPluginExtension {
    val packageJson: RegularFileProperty
    val nodeModules: DirectoryProperty
    val workingDir: DirectoryProperty
    val defaultTaskGroup: Property<String>
    val autoCreateTasksFromPackageJsonScripts: Property<Boolean>
    val tasksDependingOnNpmInstallByDefault: Property<Boolean>
    val scriptsDependingOnNpmDevInstall: SetProperty<String>
    val scriptsDependingOnNpmInstall: SetProperty<String>
    val nodeVersion: Property<String>
    val nodePath: Property<String>
    val downloadNode: Property<Boolean>
}
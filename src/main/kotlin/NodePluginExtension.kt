import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty

interface NodePluginExtension {
    val packageJson: RegularFileProperty
    val nodeModules: DirectoryProperty
    val workingDir: DirectoryProperty
    val defaultTaskGroup: Property<String>
    val autoCreateTasksFromPackageJsonScripts: Property<Boolean>
    val tasksDependingOnNodeInstallByDefault: Property<Boolean>
    val scriptsDependingOnNodeDevInstall: SetProperty<String>
    val scriptsDependingOnNodeInstall: SetProperty<String>
    val nodeVersion: Property<String>
    val nodePath: Property<String>
    val downloadNode: Property<Boolean>
    val verbose: Property<Boolean>
    val packageManager: Property<PackageManager>
}
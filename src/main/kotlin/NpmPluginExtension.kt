import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty

interface NpmPluginExtension {
    val packageJson: RegularFileProperty
    val nodeModules: DirectoryProperty
    val workingDir: DirectoryProperty
    val npmPath: Property<String>
    val includeAllScripts: Property<Boolean>
    val defaultTaskGroup: Property<String>
    val tasksDependingOnNpmInstallByDefault: Property<Boolean>
    val scriptsDependingOnNpmDevInstall: SetProperty<String>
    val scriptsDependingOnNpmInstall: SetProperty<String>
}
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import java.io.File

interface NpmPluginExtension {
    val packageJson: Property<File>
    val nodeModules: Property<File>
    val workingDir: Property<File>
    val npmPath: Property<String>
    val includeAllScripts: Property<Boolean>
    val defaultTaskGroup: Property<String>
    val tasksDependingOnNpmInstallByDefault: Property<Boolean>
    val scriptsDependingOnNpmDevInstall: SetProperty<String>
    val scriptsDependingOnNpmInstall: SetProperty<String>
}
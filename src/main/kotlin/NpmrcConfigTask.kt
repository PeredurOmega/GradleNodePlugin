import nu.studer.gradle.credentials.domain.CredentialsContainer
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import java.io.File

abstract class NpmrcConfigTask : DefaultTask() {

    @get:Input
    protected val properties: MutableMap<String, String> = mutableMapOf()

    @get:Input
    protected val encryptedProperties: MutableList<String> = mutableListOf()

    @get:Input
    abstract val nodePath: Property<String>

    @get:Input
    abstract val credentials: Property<CredentialsContainer>

    fun provideProjectCredentials() {
        credentials.set(project.credentials)
    }

    fun setProperty(name: String, v: String) {
        properties[name] = v
    }

    fun setEncryptedProperty(key: String) {
        encryptedProperties.add(key)
    }

    @TaskAction
    fun run() {
        val nodePath = nodePath.get()
        val allProperties = properties + encryptedProperties.map {
            it to (credentials.get().forKey(it) ?: requestInput(it))
        }
        if (allProperties.isEmpty()) logger.info("No additional properties to set")
        else {
            val s = StringBuilder()
            allProperties.forEach { (k, v) ->
                s.appendLine("$k=$v")
            }
            if (nodePath.isBlank()) throw Exception("To use the npmrc auto config you must use embedded node")
            logger.info("Writing .npmrc file to '$nodePath'.\r\n Content:\r\n$s")
            val etc = File(nodePath, "etc")
            etc.mkdirs()
            val npmrc = etc.resolve("npmrc")
            npmrc.createNewFile()
            npmrc.writeText(s.toString())
        }
    }

    private fun requestInput(key: String): String {
        throw Exception("Missing property. Please add credentials using the gradle 'addCredentials' task for the key '$key'")
        /*TODO val value = JOptionPane.showInputDialog("Please specify the value for '$key'")
        val save = JOptionPane.showConfirmDialog(
            null,
            "Do you want to save the value of '$key' (with simple encryption) for future use?",
            "Save value ?",
            JOptionPane.YES_NO_OPTION
        )
        if (save == JOptionPane.YES_OPTION) {
            project.credentials.forKey(key, value)
        }
        return value*/
    }
}
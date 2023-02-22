import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.plugin.devel.GradlePluginDevelopmentExtension

fun Project.`npm`(configure: Action<NpmPluginExtension>): Unit = this.extensions.configure("npm", configure)
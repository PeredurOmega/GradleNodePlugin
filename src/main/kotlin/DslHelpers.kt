@file:Suppress("unused")

import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.artifacts.dsl.RepositoryHandler
import org.gradle.api.artifacts.repositories.AuthenticationSupported

fun Project.node(configure: Action<NodePluginExtension>) {
    extensions.configure("node", configure)
    NodePlugin.registerTasks(this)
}

fun NodeScriptTask.requiresDevDependencyInstall() = dependsOn(DependenciesInstallTask.DEV_NAME)

fun NodeScriptTask.requiresDependencyInstall() = dependsOn(DependenciesInstallTask.NAME)

fun RepositoryHandler.nodeRepository(
    url: String = "https://nodejs.org/dist/",
    auth: Action<AuthenticationSupported> = Action { }
) {
    ivy {
        name = "Node.js"
        setUrl(url)
        patternLayout {
            artifact("v[revision]/[artifact](-v[revision]-[classifier]).[ext]")
        }
        metadataSources {
            artifact()
        }
        content {
            includeModule("org.nodejs", "node")
        }
        auth.execute(this)
    }
}
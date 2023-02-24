@file:Suppress("unused")

import org.gradle.api.Action
import org.gradle.api.Project

fun Project.npm(configure: Action<NpmPluginExtension>): Unit = this.extensions.configure("npm", configure)
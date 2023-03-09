package setup

/**
 * https://github.com/node-gradle/gradle-node-plugin/blob/master/src/main/kotlin/com/github/gradle/node/util/PlatformHelper.kt
 */
class PlatformHelper {
    fun getOsPrefix(): String {
        val name = System.getProperty("os.name").lowercase()
        return when {
            name.contains("windows") -> "win"
            name.contains("mac") -> "darwin"
            name.contains("linux") -> "linux"
            name.contains("freebsd") -> "linux"
            name.contains("sunos") -> "sunos"
            else -> error("Unsupported OS: $name")
        }
    }

    fun getArch(): String {
        val arch = System.getProperty("os.arch").lowercase()
        return when {
            arch.startsWith("arm") || arch.startsWith("aarch") -> "arm64"
            arch == "ppc64le" -> "ppc64le"
            arch == "s390x" -> "s390x"
            arch.contains("64") -> "x64"
            else -> "x86"
        }
    }
}
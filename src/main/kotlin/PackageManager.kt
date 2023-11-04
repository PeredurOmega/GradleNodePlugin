enum class PackageManager {
    NPM,
    YARN,
    PNPM;

    companion object {
        fun fromString(packageManager: String): PackageManager {
            return when {
                packageManager.contains("pnpm") -> PNPM
                packageManager.contains("yarn") -> YARN
                packageManager.contains("npm") -> NPM
                else -> throw Exception("Unsupported package manager: $packageManager, only npm, pnpm and yarn are supported")
            }
        }
    }

    override fun toString(): String {
        return super.toString().lowercase()
    }
}
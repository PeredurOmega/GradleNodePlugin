import org.gradle.api.logging.Logger
import org.zeroturnaround.exec.stream.slf4j.Slf4jOutputStream

class LifecycleLoggerStream(private val logger: Logger) : Slf4jOutputStream(logger) {
    override fun processLine(line: String?) {
        logger.lifecycle(line)
    }

    companion object {
        fun of(logger: Logger) = LifecycleLoggerStream(logger)
    }
}
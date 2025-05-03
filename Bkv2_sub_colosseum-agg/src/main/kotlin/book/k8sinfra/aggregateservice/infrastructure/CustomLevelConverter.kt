package book.k8sinfra.aggregateservice.infrastructure

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.pattern.DynamicConverter;

class CustomLevelConverter : DynamicConverter<ILoggingEvent>() {
    override fun convert(event: ILoggingEvent): String {
        val level = event.level
        return if (level == Level.WARN) "WARNING" else level.toString()
    }
}
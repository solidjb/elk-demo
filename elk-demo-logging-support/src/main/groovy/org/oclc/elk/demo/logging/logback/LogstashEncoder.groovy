package org.oclc.elk.demo.logging.logback

import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.classic.spi.IThrowableProxy
import ch.qos.logback.classic.spi.ThrowableProxyUtil
import ch.qos.logback.core.Context
import ch.qos.logback.core.CoreConstants
import ch.qos.logback.core.encoder.EncoderBase
import com.fasterxml.jackson.core.JsonGenerator.Feature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import org.apache.commons.collections.CollectionUtils
import org.apache.commons.io.IOUtils
import org.apache.commons.lang.StringUtils
import org.apache.commons.lang.time.FastDateFormat
import org.slf4j.Marker

import static org.apache.commons.io.IOUtils.write


/**
 * A Logback Encoder which writes JSON log events in a known Logstash schema.
 */
class LogstashEncoder extends EncoderBase<ILoggingEvent> {

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .configure(Feature.ESCAPE_NON_ASCII, true)
            .configure(SerializationFeature.WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED, false)
            .configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false)
            .configure(SerializationFeature.WRITE_EMPTY_JSON_ARRAYS, false)
            .configure(SerializationFeature.FLUSH_AFTER_WRITE_VALUE, false)

    private static final FastDateFormat ISO_DATETIME_TIME_ZONE_FORMAT_WITH_MILLIS = FastDateFormat.getInstance("yyyy-MM-dd'T'HH:mm:ss.SSSZZ")

    private static final StackTraceElement DEFAULT_CALLER_DATA = new StackTraceElement("", "", "", 0)

    private static final String SCHEMA_VERSION = "1.0"

    private boolean immediateFlush = true

    private String sourceApplication

    private List<String> userDefinedTags = new LinkedList<String>()

    @Override
    void doEncode(ILoggingEvent event) throws IOException {
        final Map<String, Object> eventNode = new HashMap<String, Object>()

        putFromMdc(event, eventNode)

        putTimestamp(eventNode, event)
        putVersion(eventNode)
        putTags(eventNode, event)
        putMessage(eventNode, event)
        putSourceApplication(eventNode)

        putFromContext(eventNode)

        putLevel(eventNode, event)
        putLoggerName(eventNode, event)
        putThreadName(eventNode, event)
        putCallerInfo(eventNode, event)
        putStackTrace(eventNode, event)
        putType(eventNode)

        writeEventNode(eventNode)
    }

    private void putType(Map<String, Object> eventNode) {
        eventNode.put("type", "application")
    }

    private void writeEventNode(Map<String, Object> eventNode) throws IOException {
        write(MAPPER.writeValueAsBytes(eventNode), outputStream)
        write(CoreConstants.LINE_SEPARATOR, outputStream)

        if (immediateFlush) {
            outputStream.flush()
        }
    }

    private void putFromMdc(ILoggingEvent event, Map<String, Object> eventNode) {
        addPropertiesAsFields(eventNode, event.getMDCPropertyMap())
    }

    private void putFromContext(Map<String, Object> eventNode) {
        Context context = getContext()
        if (context != null) {
            addPropertiesAsFields(eventNode, context.getCopyOfPropertyMap())
        }
    }

    private void putStackTrace(Map<String, Object> eventNode, ILoggingEvent event) {
        IThrowableProxy throwableProxy = event.getThrowableProxy()
        if (throwableProxy != null) {
            put(eventNode, "stack_trace", ThrowableProxyUtil.asString(throwableProxy))
        }
    }

    private void putCallerInfo(Map<String, Object> eventNode, ILoggingEvent event) {
        final StackTraceElement callerData = extractCallerData(event)

        put(eventNode, "caller_class_name", callerData.getClassName())
        put(eventNode, "caller_method_name", callerData.getMethodName())
        put(eventNode, "caller_file_name", callerData.getFileName())
        put(eventNode, "caller_line_number", callerData.getLineNumber())
    }

    private void putThreadName(Map<String, Object> eventNode, ILoggingEvent event) {
        put(eventNode, "thread_name", event.getThreadName())
    }

    private void putLoggerName(Map<String, Object> eventNode, ILoggingEvent event) {
        put(eventNode, "logger_name", event.getLoggerName())
    }

    private void putLevel(Map<String, Object> eventNode, ILoggingEvent event) {
        put(eventNode, "level", event.getLevel().toString())
    }

    private void putSourceApplication(Map<String, Object> eventNode) {
        if (StringUtils.isNotBlank(sourceApplication)) {
            put(eventNode, "source_application", sourceApplication)
        }
    }

    private void putVersion(Map<String, Object> eventNode) {
        put(eventNode, "@version", SCHEMA_VERSION)
    }

    private void putTimestamp(Map<String, Object> eventNode, ILoggingEvent event) {
        put(eventNode, "@timestamp", ISO_DATETIME_TIME_ZONE_FORMAT_WITH_MILLIS.format(event.getTimeStamp()))
    }

    private void putTags(Map<String, Object> eventNode, ILoggingEvent event) {
        final Collection<String> tags = new LinkedList<String>()
        final Collection<String> markers = buildMarkers(event)

        if (CollectionUtils.isNotEmpty(markers)) {
            tags.addAll(markers)
        }

        if (CollectionUtils.isNotEmpty(this.userDefinedTags)) {
            tags.addAll(this.userDefinedTags)
        }

        put(eventNode, "tags", tags)
    }

    private void putMessage(Map<String, Object> eventNode, ILoggingEvent event) {
        put(eventNode, "message", event.getFormattedMessage())
    }

    private void put(Map<String, Object> eventNode, String key, String value) {
        if (StringUtils.isNotEmpty(value)) {
            eventNode.put(key, value)
        }
    }

    private void put(Map<String, Object> eventNode, String key, Collection<?> value) {
        if (CollectionUtils.isNotEmpty(value)) {
            eventNode.put(key, value)
        }
    }

    private void put(Map<String, Object> eventNode, String key, Object value) {
        eventNode.put(key, value)
    }

    private Collection<String> buildMarkers(ILoggingEvent event) {
        Collection<String> node = null
        final Marker marker = event.getMarker()

        if (marker != null) {
            node = new LinkedList<String>()
            node.add(marker.getName())

            if (marker.hasReferences()) {
                final Iterator<?> i = event.getMarker().iterator()

                while (i.hasNext()) {
                    Marker next = (Marker) i.next()

                    // attached markers will never be null as provided by the MarkerFactory.
                    node.add(next.getName())
                }
            }
        }

        return node
    }

    private StackTraceElement extractCallerData(final ILoggingEvent event) {
        final StackTraceElement[] ste = event.getCallerData()
        if (ste == null || ste.length == 0) {
            return DEFAULT_CALLER_DATA
        }
        return ste[0]
    }


    private void addPropertiesAsFields(final Map<String, Object> fieldsNode, final Map<String, String> properties) {
        if (properties != null) {
            for (Map.Entry<String, String> entry : properties.entrySet()) {
                final String key = entry.getKey()
                final String value = entry.getValue()

                fieldsNode.put(key, value)
            }
        }
    }

    /**
     * Close.
     *
     * @throws IOException In case of IOException.
     */
    @Override
    void close() throws IOException {
        IOUtils.write(CoreConstants.LINE_SEPARATOR, outputStream)
    }

    /**
     * Set the source application of all log events.
     *
     * @param sourceApplication The source application.
     */
    void setSourceApplication(String sourceApplication) {
        this.sourceApplication = sourceApplication
    }

    /**
     * Add a persistent tag.
     *
     * @param tag The tag.
     */
    void addTag(String tag) {
        this.userDefinedTags.add(tag)
    }

    void setImmediateFlush(boolean immediateFlush) {
        this.immediateFlush = immediateFlush
    }
}
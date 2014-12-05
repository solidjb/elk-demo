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

    // The following 3 values are the only logstash defined fields.
    private static final String TIMESTAMP_FIELD_NAME = "@timestamp"
    private static final String MESSAGE_FIELD_NAME = "message"
    public static final String VERSION_FIELD_NAME = "@version"

    private static final String EXCEPTION_CLASS_FIELD_NAME = 'exception_class'
    private static final String STACK_TRACE_FIELD_NAME = 'stack_trace'
    private static final String CALLER_CLASS_NAME_FIELD_NAME = 'caller_class_name'
    private static final String CALLER_METHOD_NAME_FIELD_NAME = 'caller_method_name'
    private static final String CALLER_FILE_NAME_FIELD_NAME = 'caller_file_name'
    private static final String CALLER_LINE_NUMBER_FIELD_NAME = 'caller_line_number'
    private static final String THREAD_NAME_FIELD_NAME = 'thread_name'
    private static final String LOGGER_NAME_FIELD_NAME = 'logger_name'
    private static final String LEVEL_FIELD_NAME = 'level'
    private static final String SOURCE_APPLICATION_FIELD_NAME = 'source_application'
    private static final String TAGS_FIELD_NAME = 'tags'
    private static final String TYPE_FIELD_NAME = 'type'

    private static final String EXCEPTION_OCCURRED_TAG = 'EXCEPTION_OCCURRED'

    private static final String APPLICATION_TYPE_VALUE = 'application'

    private static final StackTraceElement DEFAULT_CALLER_DATA = new StackTraceElement('', '', '', 0)

    private static final String SCHEMA_VERSION = '1.0'

    private boolean immediateFlush = true

    private String sourceApplication

    private List<String> userDefinedTags = new LinkedList<String>()

    @Override
    void doEncode(ILoggingEvent event) throws IOException {
        final Map<String, Object> eventNode = new HashMap<String, Object>()

        putFromMdc(event, eventNode)

        boolean hasException = putStackTrace(eventNode, event)

        putTimestamp(eventNode, event)
        putVersion(eventNode)
        putTags(eventNode, event, hasException)
        putMessage(eventNode, event)
        putSourceApplication(eventNode)

        putFromContext(eventNode)

        putLevel(eventNode, event)
        putLoggerName(eventNode, event)
        putThreadName(eventNode, event)
        putCallerInfo(eventNode, event)
        putType(eventNode)

        writeEventNode(eventNode)
    }

    private void putType(Map<String, Object> eventNode) {
        eventNode.put(TYPE_FIELD_NAME, APPLICATION_TYPE_VALUE)
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

    private boolean putStackTrace(Map<String, Object> eventNode, ILoggingEvent event) {
        IThrowableProxy throwableProxy = event.getThrowableProxy()

        boolean hasException = throwableProxy != null

        if (hasException) {

            put(eventNode, EXCEPTION_CLASS_FIELD_NAME, throwableProxy.getClassName())

            put(eventNode, STACK_TRACE_FIELD_NAME, ThrowableProxyUtil.asString(throwableProxy))
        }

        return hasException
    }

    private void putCallerInfo(Map<String, Object> eventNode, ILoggingEvent event) {
        final StackTraceElement callerData = extractCallerData(event)

        put(eventNode, CALLER_CLASS_NAME_FIELD_NAME, callerData.getClassName())
        put(eventNode, CALLER_METHOD_NAME_FIELD_NAME, callerData.getMethodName())
        put(eventNode, CALLER_FILE_NAME_FIELD_NAME, callerData.getFileName())
        put(eventNode, CALLER_LINE_NUMBER_FIELD_NAME, callerData.getLineNumber())
    }

    private void putThreadName(Map<String, Object> eventNode, ILoggingEvent event) {
        put(eventNode, THREAD_NAME_FIELD_NAME, event.getThreadName())
    }

    private void putLoggerName(Map<String, Object> eventNode, ILoggingEvent event) {
        put(eventNode, LOGGER_NAME_FIELD_NAME, event.getLoggerName())
    }

    private void putLevel(Map<String, Object> eventNode, ILoggingEvent event) {
        put(eventNode, LEVEL_FIELD_NAME, event.getLevel().toString())
    }

    private void putSourceApplication(Map<String, Object> eventNode) {
        if (StringUtils.isNotBlank(sourceApplication)) {
            put(eventNode, SOURCE_APPLICATION_FIELD_NAME, sourceApplication)
        }
    }

    private void putVersion(Map<String, Object> eventNode) {
        put(eventNode, VERSION_FIELD_NAME, SCHEMA_VERSION)
    }

    private void putTimestamp(Map<String, Object> eventNode, ILoggingEvent event) {
        put(eventNode, TIMESTAMP_FIELD_NAME, ISO_DATETIME_TIME_ZONE_FORMAT_WITH_MILLIS.format(event.getTimeStamp()))
    }

    private void putTags(Map<String, Object> eventNode, ILoggingEvent event, boolean hasException) {
        final Collection<String> tags = new LinkedList<String>()
        final Collection<String> markers = buildMarkers(event)

        if (CollectionUtils.isNotEmpty(markers)) {
            tags.addAll(markers)
        }

        if (CollectionUtils.isNotEmpty(this.userDefinedTags)) {
            tags.addAll(this.userDefinedTags)
        }

        if(hasException) {
            tags.add(EXCEPTION_OCCURRED_TAG)
        }

        put(eventNode, TAGS_FIELD_NAME, tags)
    }

    private void putMessage(Map<String, Object> eventNode, ILoggingEvent event) {
        put(eventNode, MESSAGE_FIELD_NAME, event.getFormattedMessage())
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
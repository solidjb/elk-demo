/*
 *  Copyright (c) 2013 OCLC, Inc. All Rights Reserved.
 *
 *  OCLC proprietary information: the enclosed materials contain proprietary information of OCLC Online Computer
 *  Library Center, Inc. and shall not be disclosed in whole or in any part to any third party or used by any person
 *  for any purpose, without written consent of OCLC, Inc.  Duplication of any portion of these materials shall include
 *  this notice.
 */

package org.oclc.elk.demo.logging.logback.access

import static org.apache.commons.io.IOUtils.LINE_SEPARATOR
import static org.apache.commons.io.IOUtils.write

import ch.qos.logback.access.spi.IAccessEvent
import ch.qos.logback.core.Context
import ch.qos.logback.core.CoreConstants
import ch.qos.logback.core.encoder.EncoderBase
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import org.apache.commons.lang.StringUtils
import org.apache.commons.lang.time.FastDateFormat
import org.oclc.elk.demo.logging.uniqueid.RequestIdContainer


/**
 * This class will encode a logback IAccessEvent into a logstash json event.
 */
class LogstashEncoder extends EncoderBase<IAccessEvent> {
    private static final ObjectMapper MAPPER = new ObjectMapper().configure(JsonGenerator.Feature.ESCAPE_NON_ASCII, true)

    private static final FastDateFormat ISO_DATETIME_TIME_ZONE_FORMAT_WITH_MILLIS = FastDateFormat.getInstance("yyyy-MM-dd'T'HH:mm:ss.SSSZZ")
    private static final String LOGSTASH_SCHEMA_VERSION = "1.0"
    static final String REFERRER_REQUEST_HEADER_KEY = "Referer"
    static final String USER_AGENT_REQUEST_HEADER_KEY = "User-Agent"
    static final String ACCEPT_LANGUAGE_HEADER_KEY = "Accept-Language"
    static final String ACCEPT_ENCODING_HEADER_KEY = "Accept-Encoding"
    static final String CONTENT_TYPE_HEADER_KEY = "Content-Type"
    static final String CONTENT_ENCODING_HEADER_KEY = "Content-Encoding"
    static final String CONTENT_LANGUAGE_HEADER_KEY = "Content-Language"
    static final String ACCEPT_HEADER_KEY = "Accept"
    static final String CLIENT_ID_HEADER_KEY = "X-DEMO-ClientId"
    static final String LOGSTASH_TIMESTAMP_KEY = "@timestamp"
    static final String LOGSTASH_SCHEMA_VERSION_KEY = "@version"
    static final String LOGSTASH_TYPE_KEY = "type"
    static final String LOGSTASH_SOURCE_APPLICATION_KEY = "source_application"
    static final String LOGSTASH_SOURCE_REQUEST_ID_KEY = "source_request_id"
    static final String LOGSTASH_PARENT_REQUEST_ID_KEY = "parent_request_id"
    static final String LOGSTASH_SELF_REQUEST_ID_KEY = "self_request_id"
    static final String LOGSTASH_REQUEST_DURATION_KEY = "request_duration"
    static final String LOGSTASH_URL_PATH_KEY = "url_path"
    static final String LOGSTASH_URL_QUERY_KEY = "url_query"
    static final String LOGSTASH_HTTP_STATUS_KEY = "http_status"
    static final String LOGSTASH_HTTP_VERSION_KEY = "http_version"
    static final String LOGSTASH_TOTAL_REQUEST_BYTES_KEY = "total_request_bytes"
    static final String LOGSTASH_HTTP_METHOD_KEY = "http_method"
    static final String LOGSTASH_REQUEST_REFERRER_KEY = "request_referrer"
    static final String LOGSTASH_REQUEST_USER_AGENT_KEY = "request_user_agent"
    static final String LOGSTASH_REQUEST_ACCEPT_LANGUAGE_KEY = "request_accept_language"
    static final String LOGSTASH_REQUEST_ACCEPT_ENCODING_KEY = "request_accept_encoding"
    static final String LOGSTASH_REQUEST_ACCEPT_KEY = "request_accept"
    static final String LOGSTASH_REQUEST_CONTENT_TYPE_KEY = "request_content_type"
    static final String LOGSTASH_RESPONSE_CONTENT_TYPE_KEY = "response_content_type"
    static final String LOGSTASH_RESPONSE_CONTENT_ENCODING_KEY = "response_content_encoding"
    static final String LOGSTASH_RESPONSE_CONTENT_LANGUAGE_KEY = "response_content_language"
    static final String LOGSTASH_TAGS_KEY = "tags"
    static final String LOGSTASH_ACCESS_TYPE_VALUE = "access"
    static final String LOGSTASH_SOURCE_CLIENT_ID = "source_client_id"

    private boolean immediateFlush = true

    private String sourceApplication

    private List<String> userDefinedTags = new ArrayList<>()

    @Override
    void doEncode(IAccessEvent event) throws IOException {
        ObjectNode eventNode = MAPPER.createObjectNode()
        putEntries(eventNode, event)
        writeEventNode(eventNode)
    }

    protected void putEntries(ObjectNode eventNode, IAccessEvent event) {
        putAccessLogEntries(eventNode, event)
        putTags(eventNode)
        putFromContext(eventNode)
    }

    private void putAccessLogEntries(ObjectNode eventNode, IAccessEvent event) {
        addValueIfNotEmpty(LOGSTASH_TIMESTAMP_KEY, ISO_DATETIME_TIME_ZONE_FORMAT_WITH_MILLIS.format(event.getTimeStamp()), eventNode)
        addValueIfNotEmpty(LOGSTASH_SCHEMA_VERSION_KEY, LOGSTASH_SCHEMA_VERSION, eventNode)
        addValueIfNotEmpty(LOGSTASH_TYPE_KEY, LOGSTASH_ACCESS_TYPE_VALUE, eventNode)
        addValueIfNotEmpty(LOGSTASH_SOURCE_APPLICATION_KEY, sourceApplication, eventNode)
        addValueIfNotEmpty(LOGSTASH_SOURCE_REQUEST_ID_KEY, getSourceRequestId(event), eventNode)
        addValueIfNotEmpty(LOGSTASH_PARENT_REQUEST_ID_KEY, getParentRequestId(event), eventNode)
        addValueIfNotEmpty(LOGSTASH_SELF_REQUEST_ID_KEY, getSelfRequestId(event), eventNode)
        eventNode.put(LOGSTASH_REQUEST_DURATION_KEY, event.getElapsedTime())
        addValueIfNotEmpty(LOGSTASH_URL_PATH_KEY, event.getRequestURI(), eventNode)
        addValueIfNotEmpty(LOGSTASH_URL_QUERY_KEY, event.getRequest().getQueryString(), eventNode)
        eventNode.put(LOGSTASH_HTTP_STATUS_KEY, event.getStatusCode())
        addValueIfNotEmpty(LOGSTASH_HTTP_VERSION_KEY, event.getProtocol(), eventNode)
        eventNode.put(LOGSTASH_TOTAL_REQUEST_BYTES_KEY, event.getContentLength())
        addValueIfNotEmpty(LOGSTASH_HTTP_METHOD_KEY, event.getMethod(), eventNode)
        addValueIfNotEmpty(LOGSTASH_SOURCE_CLIENT_ID, getRequestHeader(CLIENT_ID_HEADER_KEY, event), eventNode)
        addValueIfNotEmpty(LOGSTASH_REQUEST_REFERRER_KEY, getRequestHeader(REFERRER_REQUEST_HEADER_KEY, event), eventNode)
        addValueIfNotEmpty(LOGSTASH_REQUEST_USER_AGENT_KEY, getRequestHeader(USER_AGENT_REQUEST_HEADER_KEY, event), eventNode)
        addValueIfNotEmpty(LOGSTASH_REQUEST_ACCEPT_LANGUAGE_KEY, getRequestHeader(ACCEPT_LANGUAGE_HEADER_KEY, event), eventNode)
        addValueIfNotEmpty(LOGSTASH_REQUEST_ACCEPT_ENCODING_KEY, getRequestHeader(ACCEPT_ENCODING_HEADER_KEY, event), eventNode)
        addValueIfNotEmpty(LOGSTASH_REQUEST_ACCEPT_KEY, getRequestHeader(ACCEPT_HEADER_KEY, event), eventNode)
        addValueIfNotEmpty(LOGSTASH_REQUEST_CONTENT_TYPE_KEY, getRequestHeader(CONTENT_TYPE_HEADER_KEY, event), eventNode)
        addValueIfNotEmpty(LOGSTASH_RESPONSE_CONTENT_TYPE_KEY, getResponseHeader(CONTENT_TYPE_HEADER_KEY, event), eventNode)
        addValueIfNotEmpty(LOGSTASH_RESPONSE_CONTENT_ENCODING_KEY, getResponseHeader(CONTENT_ENCODING_HEADER_KEY, event), eventNode)
        addValueIfNotEmpty(LOGSTASH_RESPONSE_CONTENT_LANGUAGE_KEY, getResponseHeader(CONTENT_LANGUAGE_HEADER_KEY, event), eventNode)
    }

    private void putTags(ObjectNode eventNode) {
        addValueIfNotEmpty(LOGSTASH_TAGS_KEY, createTags(userDefinedTags), eventNode)
    }

    private void putFromContext(ObjectNode eventNode) {
        Context context = getContext()
        if (context != null) {
            addPropertiesAsFields(eventNode, context.getCopyOfPropertyMap())
        }
    }

    private void writeEventNode(ObjectNode eventNode) throws IOException {
        write(MAPPER.writeValueAsBytes(eventNode), outputStream)
        write(CoreConstants.LINE_SEPARATOR, outputStream)

        if (immediateFlush) {
            outputStream.flush()
        }
    }

    private void addValueIfNotEmpty(String key, String value, ObjectNode eventNode) {
        if (StringUtils.isNotBlank(value)) {
            eventNode.put(key, value)
        }
    }

    private void addValueIfNotEmpty(String key, ArrayNode value, ObjectNode eventNode) {
        if (value != null) {
            eventNode.put(key, value)
        }
    }

    private String getSourceRequestId(IAccessEvent event) {
        String uniqueRequestId = getRequestHeader(RequestIdContainer.UNIQUE_ID_HEADER_NAME, event)
        if (StringUtils.isBlank(uniqueRequestId)) {
            uniqueRequestId = getResponseHeader(RequestIdContainer.UNIQUE_ID_HEADER_NAME, event)
        }
        return uniqueRequestId
    }

    private String getParentRequestId(IAccessEvent event) {
        return getRequestHeader(RequestIdContainer.PARENT_ID_HEADER_NAME, event)
    }

    private String getSelfRequestId(IAccessEvent event) {
        return getResponseHeader(RequestIdContainer.SELF_ID_HEADER_NAME, event)
    }


    private ArrayNode createTags(List<String> tags) {
        ArrayNode node = null

        if (!tags.isEmpty()) {
            node = MAPPER.createArrayNode()
            for (String tag : tags) {
                node.add(tag)
            }
        }

        return node
    }

    private void addPropertiesAsFields(final ObjectNode fieldsNode, final Map<String, String> properties) {
        if (properties != null) {
            for (Map.Entry<String, String> entry : properties.entrySet()) {
                String key = entry.getKey()
                String value = entry.getValue()
                fieldsNode.put(key, value)
            }
        }
    }

    private String getRequestHeader(String key, IAccessEvent event) {
        String requestHeader = event.getRequestHeader(key)
        if (IAccessEvent.NA.equals(requestHeader)) {
            return null
        } else {
            return requestHeader
        }
    }

    private String getResponseHeader(String key, IAccessEvent event) {
        String responseHeader = event.getResponseHeader(key)
        if (IAccessEvent.NA.equals(responseHeader)) {
            return null
        } else {
            return responseHeader
        }
    }

    @Override
    void close() throws IOException {
        write(LINE_SEPARATOR, outputStream)
    }

    void setSourceApplication(String sourceApplication) {
        this.sourceApplication = sourceApplication
    }

    /**
     * The add method is used by logback infrastructure to configure user defined tags.
     *
     * @param tag String an individual static tag, the configuration wants added to the event.
     */
    void addTag(String tag) {
        this.userDefinedTags.add(tag)
    }

    boolean isImmediateFlush() {
        return immediateFlush
    }

    void setImmediateFlush(boolean immediateFlush) {
        this.immediateFlush = immediateFlush
    }
}

/*
 *  Copyright (c) 2014 OCLC, Inc. All Rights Reserved.
 *
 *  OCLC proprietary information: the enclosed materials contain proprietary information of OCLC Online Computer
 *  Library Center, Inc. and shall not be disclosed in whole or in any part to any third party or used by any person
 *  for any purpose, without written consent of OCLC, Inc.  Duplication of any portion of these materials shall include
 *  this notice.
 */
package org.oclc.elk.demo.logging.http

import static org.apache.http.client.utils.URIUtils.rewriteURI

import groovy.util.logging.Slf4j
import org.apache.http.HttpException
import org.apache.http.HttpRequest
import org.apache.http.HttpRequestInterceptor
import org.apache.http.HttpResponse
import org.apache.http.HttpResponseInterceptor
import org.apache.http.protocol.HttpContext
import org.slf4j.Marker
import org.slf4j.MarkerFactory
import org.springframework.util.StopWatch

/**
 * Logs the request before it happens.
 */
@Slf4j
class LoggingRequestAndResponseInterceptor implements HttpRequestInterceptor, HttpResponseInterceptor {

    private static final HTTP_MARKER = MarkerFactory.getDetachedMarker("HTTP");
    private static final DURATION_MARKER = MarkerFactory.getDetachedMarker("DURATION");
    private static final REQUEST_TIMER = 'requestTimer'

    @Override
    void process(HttpRequest request, HttpContext context) throws HttpException, IOException {
        StopWatch sw = new StopWatch()
        sw.start()

        context.setAttribute(REQUEST_TIMER, sw)

        log.debug(HTTP_MARKER, "Making request {}", rewriteURI(requestLineAsUri(request), targetHost(context)));
    }

    @Override
    void process(HttpResponse response, HttpContext context) throws HttpException, IOException {
        StopWatch sw = getStopwatchFrom(context)
        // Tighten this up, there may not be a sw...
        sw.stop()

        log.debug(
                linkMarkers(DURATION_MARKER, HTTP_MARKER),
                'Duration of {} ms for the call {}',
                sw.totalTimeMillis,
                rewriteURI(makeUriForLogStatement(context), targetHost(context))
        )
    }

    Marker linkMarkers(Marker base, Marker second) {
        base.add(second)

        return base
    }

    StopWatch getStopwatchFrom(HttpContext httpContext) {
        httpContext.removeAttribute(REQUEST_TIMER)
    }

    private targetHost(HttpContext context) {
        context.getHttpRoute().getTargetHost()
    }

    private requestLineAsUri(HttpRequest request) {
        request.requestLine.uri.toURI()
    }

    private makeUriForLogStatement(HttpContext context) {
        requestLineAsUri(context.getRequest())
    }
}

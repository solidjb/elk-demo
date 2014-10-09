/*
 *  Copyright (c) 2013 OCLC, Inc. All Rights Reserved.
 *
 *  OCLC proprietary information: the enclosed materials contain proprietary information of OCLC Online Computer
 *  Library Center, Inc. and shall not be disclosed in whole or in any part to any third party or used by any person
 *  for any purpose, without written consent of OCLC, Inc.  Duplication of any portion of these materials shall include
 *  this notice.
 */

package org.oclc.elk.demo.logging.filter

import org.oclc.elk.demo.logging.uniqueid.RequestIdContainer
import org.oclc.elk.demo.logging.uniqueid.RequestIdTuple
import org.slf4j.MDC

import javax.servlet.Filter
import javax.servlet.FilterChain
import javax.servlet.FilterConfig
import javax.servlet.ServletException
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.http.HttpServletRequest

/**
 * This filter will populate some important fields into the Slf4j MappedDiagnosticContext (MDC).  These fields
 * will then be available in every log event that happens on this thread.
 */
class CommonFieldMDCPopulatingFilter implements Filter {

    static final String SOURCE_HOST_MDC_KEY = "source_host"
    static final String SOURCE_REQUEST_ID_MDC_KEY = "source_request_id"
    static final String PARENT_REQUEST_ID_MDC_KEY = "parent_request_id"
    static final String SELF_REQUEST_ID_MDC_KEY = "self_request_id"

    @Override
    void init(FilterConfig filterConfig) throws ServletException {
        //nothing to do here
    }

    @Override
    void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest

        MDC.put(SOURCE_HOST_MDC_KEY, httpServletRequest.getServerName())

        RequestIdTuple requestIdTuple = RequestIdContainer.get()
        MDC.put(SOURCE_REQUEST_ID_MDC_KEY, requestIdTuple.getSourceRequestId())
        MDC.put(PARENT_REQUEST_ID_MDC_KEY, requestIdTuple.getParentRequestId())
        MDC.put(SELF_REQUEST_ID_MDC_KEY, requestIdTuple.getSelfRequestId())

        try {
            filterChain.doFilter(servletRequest, servletResponse)
        } finally {
            MDC.remove(SOURCE_HOST_MDC_KEY)
            MDC.remove(SOURCE_REQUEST_ID_MDC_KEY)
            MDC.remove(PARENT_REQUEST_ID_MDC_KEY)
            MDC.remove(SELF_REQUEST_ID_MDC_KEY)
        }
    }

    @Override
    void destroy() {
        //nothing to do here
    }
}

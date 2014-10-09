/*
 *  Copyright (c) 2013 OCLC, Inc. All Rights Reserved.
 *
 *  OCLC proprietary information: the enclosed materials contain proprietary information of OCLC Online Computer
 *  Library Center, Inc. and shall not be disclosed in whole or in any part to any third party or used by any person
 *  for any purpose, without written consent of OCLC, Inc.  Duplication of any portion of these materials shall include
 *  this notice.
 */
package org.oclc.elk.demo.logging.filter

import org.apache.commons.lang.StringUtils
import org.oclc.elk.demo.logging.uniqueid.RequestIdContainer
import org.oclc.elk.demo.logging.uniqueid.RequestIdTuple

import javax.servlet.Filter
import javax.servlet.FilterChain
import javax.servlet.FilterConfig
import javax.servlet.ServletException
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * This filter ensures that this request has a unique id.  The filter will generate and set the unique request Id in the response
 * if it is not present in the request header.  If the request header is present, then the id from the header will be used.
 *
 * @see import org.oclc.elk.demo.logging.uniqueid.RequestIdContainer
 */
public class RequestIdGeneratingFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        //nothing to do here.
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        String uniqueRequestId = httpRequest.getHeader(RequestIdContainer.UNIQUE_ID_HEADER_NAME);
        String incomingParentRequestId = httpRequest.getHeader(RequestIdContainer.PARENT_ID_HEADER_NAME);

        if (StringUtils.isNotBlank(uniqueRequestId)) {
            RequestIdContainer.set(uniqueRequestId, incomingParentRequestId);
        } else {
            RequestIdContainer.generate();
        }

        RequestIdTuple requestIdTuple = RequestIdContainer.get();

        httpResponse.setHeader(RequestIdContainer.UNIQUE_ID_HEADER_NAME, requestIdTuple.getSourceRequestId());
        httpResponse.setHeader(RequestIdContainer.SELF_ID_HEADER_NAME, requestIdTuple.getSelfRequestId());

        if (requestIdTuple.getParentRequestId() != null) {
            httpResponse.setHeader(RequestIdContainer.PARENT_ID_HEADER_NAME, requestIdTuple.getParentRequestId());
        }

        try {
            filterChain.doFilter(httpRequest, httpResponse);
        } finally {
            RequestIdContainer.reset();
        }
    }

    @Override
    public void destroy() {
        //nothing to do here.
    }
}

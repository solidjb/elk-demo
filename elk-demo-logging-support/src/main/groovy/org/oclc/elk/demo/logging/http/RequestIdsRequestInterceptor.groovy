/*
 *  Copyright (c) 2014 OCLC, Inc. All Rights Reserved.
 *
 *  OCLC proprietary information: the enclosed materials contain proprietary information of OCLC Online Computer
 *  Library Center, Inc. and shall not be disclosed in whole or in any part to any third party or used by any person
 *  for any purpose, without written consent of OCLC, Inc.  Duplication of any portion of these materials shall include
 *  this notice.
 */
package org.oclc.elk.demo.logging.http

import org.apache.http.HttpException
import org.apache.http.HttpRequest
import org.apache.http.HttpRequestInterceptor
import org.apache.http.protocol.HttpContext
import org.oclc.elk.demo.logging.uniqueid.RequestIdContainer
import org.oclc.elk.demo.logging.uniqueid.RequestIdTuple

/**
 * An implementation of HttpRequestInterceptor which propagates unique request ids
 * from the current request context to the target host.
 */
class RequestIdsRequestInterceptor implements HttpRequestInterceptor {

    @Override
    void process(HttpRequest request, HttpContext context) throws HttpException, IOException {
        if (RequestIdContainer.exists()) {
            RequestIdTuple requestIdTuple = RequestIdContainer.get();

            request.setHeader(RequestIdContainer.UNIQUE_ID_HEADER_NAME, requestIdTuple.getSourceRequestId());
            request.setHeader(RequestIdContainer.PARENT_ID_HEADER_NAME, requestIdTuple.getSelfRequestId());
        }
    }
}

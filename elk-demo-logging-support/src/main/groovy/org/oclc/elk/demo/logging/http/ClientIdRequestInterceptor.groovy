/*
 *  Copyright (c) 2014 OCLC, Inc. All Rights Reserved.
 *
 *  OCLC proprietary information: the enclosed materials contain proprietary information of OCLC Online Computer
 *  Library Center, Inc. and shall not be disclosed in whole or in any part to any third party or used by any person
 *  for any purpose, without written consent of OCLC, Inc.  Duplication of any portion of these materials shall include
 *  this notice.
 */
package org.oclc.elk.demo.logging.http
import com.google.common.base.Preconditions
import org.apache.commons.lang.StringUtils
import org.apache.http.HttpRequest
import org.apache.http.HttpRequestInterceptor
import org.apache.http.protocol.HttpContext

/**
 * Interceptor to inject X-DEMO-ClientId to http header.
 */
class ClientIdRequestInterceptor implements HttpRequestInterceptor {

    private String clientId

    /**
     * constructor with a clientId.
     *
     * @param clientId clientId value indicates who is using the client
     */
    public ClientIdRequestInterceptor(String clientId) {

        Preconditions.checkArgument(
                StringUtils.isNotBlank(clientId),
                'Invalid http header: X-DEMO-ClientId, the value should be an un-blank string.'
        )

        this.clientId = clientId
    }

    @Override
    public void process(HttpRequest request, HttpContext context) {
        request.addHeader("X-DEMO-ClientId", clientId)
    }
}

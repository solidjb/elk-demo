/*
 *  Copyright (c) 2014 OCLC, Inc. All Rights Reserved.
 *
 *  OCLC proprietary information: the enclosed materials contain proprietary information of OCLC Online Computer
 *  Library Center, Inc. and shall not be disclosed in whole or in any part to any third party or used by any person
 *  for any purpose, without written consent of OCLC, Inc.  Duplication of any portion of these materials shall include
 *  this notice.
 */
package org.oclc.elk.demo.logging.uniqueid

class RequestIdTuple {

    private final String sourceRequestId
    private final String parentRequestId
    private final String selfRequestId

    private RequestIdTuple(String sourceRequestId, String parentRequestId, String selfRequestId) {
        assert sourceRequestId, 'sourceRequestId is required'
        assert selfRequestId, 'selfRequestId is required'

        this.sourceRequestId = sourceRequestId
        this.parentRequestId = parentRequestId
        this.selfRequestId = selfRequestId
    }

    String getSourceRequestId() {
        return sourceRequestId
    }

    String getParentRequestId() {
        return parentRequestId
    }

    String getSelfRequestId() {
        return selfRequestId
    }

/*package-protected*/static final RequestIdTuple createTuple(String sourceRequestId, String parentRequestId, String selfRequestId) {
        return new RequestIdTuple(sourceRequestId, parentRequestId, selfRequestId)
    }
}

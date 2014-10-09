/*
 *  Copyright (c) 2013 OCLC, Inc. All Rights Reserved.
 *
 *  OCLC proprietary information: the enclosed materials contain proprietary information of OCLC Online Computer
 *  Library Center, Inc. and shall not be disclosed in whole or in any part to any third party or used by any person
 *  for any purpose, without written consent of OCLC, Inc.  Duplication of any portion of these materials shall include
 *  this notice.
 */
package org.oclc.elk.demo.logging.uniqueid

import groovy.util.logging.Slf4j

/**
 * This class will encapsulate logic for storing a unique request id, so other classes can get access to the id
 * without low level logic.
 * <p/>
 * Please note that this class makes use of InheritableThreadLocal.
 */
@Slf4j
public class RequestIdContainer {

    public static final String UNIQUE_ID_HEADER_NAME = "X-DEMO-RequestId"
    public static final String PARENT_ID_HEADER_NAME = "X-DEMO-ParentId"
    public static final String SELF_ID_HEADER_NAME = "X-DEMO-SelfId"

    private static InheritableThreadLocal<RequestIdTuple> uniqueIdHolder = new InheritableThreadLocal<>()

    /**
     * Set the unique request ids.
     *
     * @param requestId The over-arching id for this request.
     * @param parentId The parent id for the calling request
     */
    public static void set(String requestId, String parentId) {
        String selfId = getRandomString()
        log.debug("Generating New self Request Id {}", selfId)

        uniqueIdHolder.set(RequestIdTuple.createTuple(requestId, parentId, selfId))
    }

    /*package-protected*/static void set(RequestIdTuple tuple) {
        uniqueIdHolder.set(tuple)
    }

    /**
     * The method used to generate a new unique id.  Please note that this method also stores the newly generated id.
     *
     * @return String a new id.
     */
    public static RequestIdTuple generate() {
        String sourceRequestId = getRandomString()
        String selfId = getRandomString()

        uniqueIdHolder.set(RequestIdTuple.createTuple(sourceRequestId, null, selfId))

        log.debug("Generating New over-arching Request Id {}", sourceRequestId)
        log.debug("Generating New self Request Id {}", selfId)

        return uniqueIdHolder.get()
    }

    private static String getRandomString() {
        UUID randomUUID = UUID.randomUUID()
        return randomUUID.toString()
    }

    /**
     * Does a unique id already exist in this container.
     *
     * @return boolean
     */
    public static boolean exists() {
        return uniqueIdHolder.get() != null
    }

    /**
     * Get the unique request id.  If one is not currently set, this method will not generate a new Id.
     *
     * @return String The id for this request.
     */
    public static RequestIdTuple get() {
        return uniqueIdHolder.get()
    }

    /**
     * Resets the unique request id so that when the thread is re-used by the container, it can get a new value.
     */
    public static void reset() {
        uniqueIdHolder.remove()
    }
}
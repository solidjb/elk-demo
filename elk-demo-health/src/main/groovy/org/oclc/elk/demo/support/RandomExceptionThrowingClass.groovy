/*
 *  Copyright (c) 2014 OCLC, Inc. All Rights Reserved.
 *
 *  OCLC proprietary information: the enclosed materials contain proprietary information of OCLC Online Computer
 *  Library Center, Inc. and shall not be disclosed in whole or in any part to any third party or used by any person
 *  for any purpose, without written consent of OCLC, Inc.  Duplication of any portion of these materials shall include
 *  this notice.
 */
package org.oclc.elk.demo.support

import com.codahale.metrics.annotation.ExceptionMetered
import com.codahale.metrics.annotation.Timed
import groovy.json.JsonBuilder
import groovy.util.logging.Slf4j
import org.slf4j.MarkerFactory

@Slf4j
class RandomExceptionThrowingClass {

    private static final JSON_MARKER = MarkerFactory.getDetachedMarker('JSON_MSG')

    private static final EXCEPTION_LIST = [
            new IllegalArgumentException('The argument you passed is exceptionally illegal.'),
            new IllegalStateException('The argument you passed is not stately enough for my tastes.'),
            new ArithmeticException('No, 1 + 1 does not equal 3.')
    ]

    @Timed
    @ExceptionMetered
    void possiblyThrowException() {
        def snapshot = System.currentTimeMillis()

        if (determineIfErrorOccurs(snapshot)) {

            def exception = determineError(snapshot)

            //Spring logs nothing, so we have to...
            log.warn('We blew up.', exception)

            throw exception
        }

        def waitTime = determineWaitTime(snapshot)

        Thread.sleep(waitTime)

        log.info(JSON_MARKER, buildJsonMessage())
    }

    String buildJsonMessage() {
        def builder = new JsonBuilder()
        def root = builder {
                weight Math.random()
                firstName 'Guillame'
                lastName 'Laforge'
                // Named arguments are valid values for objects too
                address(
                        city: 'Paris',
                        country: 'France',
                        zip: 12345,
                )
                married true
                // a list of values
                conferences 'JavaOne', 'Gr8conf'

        }

        builder.toString()
    }

    private def determineWaitTime(def snapshot) {
        snapshot % 15000
    }

    private boolean determineIfErrorOccurs(def snapshot) {
        snapshot % 50 == 0
    }

    private int determineErrorOffset(def snapshot) {
        snapshot % 3
    }

    private Throwable determineError(def snapshot) {
        EXCEPTION_LIST[determineErrorOffset(snapshot)]
    }
}

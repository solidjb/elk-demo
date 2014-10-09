/*
 *  Copyright (c) 2014 OCLC, Inc. All Rights Reserved.
 *
 *  OCLC proprietary information: the enclosed materials contain proprietary information of OCLC Online Computer
 *  Library Center, Inc. and shall not be disclosed in whole or in any part to any third party or used by any person
 *  for any purpose, without written consent of OCLC, Inc.  Duplication of any portion of these materials shall include
 *  this notice.
 */
package org.oclc.elk.demo.health

import groovy.util.logging.Slf4j
import org.springframework.boot.actuate.health.AbstractHealthIndicator
import org.springframework.boot.actuate.health.Health

@Slf4j
class RandomHealthIndicator extends AbstractHealthIndicator {

    @Override
    protected void doHealthCheck(Health.Builder builder) throws Exception {

        log.info('Checking Random Health')

        def snapshot = System.currentTimeMillis()

        if (determineError(snapshot)) {
            throw new IllegalArgumentException('The argument you passed is exceptionally illegal.')
        }

        def waitTime = determineWaitTime(snapshot)

        Thread.sleep(waitTime)

        builder.up()
    }

    def determineWaitTime(def snapshot) {
        snapshot % 15000
    }

    boolean determineError(def snapshot) {
        snapshot % 1000 == 0
    }
}

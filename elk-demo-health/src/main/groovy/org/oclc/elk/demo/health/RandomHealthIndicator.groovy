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
import org.oclc.elk.demo.support.RandomExceptionThrowingClass
import org.springframework.boot.actuate.health.AbstractHealthIndicator
import org.springframework.boot.actuate.health.Health

@Slf4j
class RandomHealthIndicator extends AbstractHealthIndicator {

    private RandomExceptionThrowingClass myClass

    void setMyClass(RandomExceptionThrowingClass myClass) {
        this.myClass = myClass
    }

    @Override
    protected void doHealthCheck(Health.Builder builder) throws Exception {

        log.info('Checking Random Health')

        myClass.possiblyThrowException()

        builder.up()
    }
}

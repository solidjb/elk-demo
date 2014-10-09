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
import org.springframework.web.client.RestTemplate

@Slf4j
class ServiceCallHealthIndicator extends AbstractHealthIndicator {

    String serviceUri
    RestTemplate restTemplate

    @Override
    protected void doHealthCheck(Health.Builder builder) throws Exception {

        log.info('Checking Service Call Health')

        def result = restTemplate.getForObject(serviceUri, String)

        builder.up().withDetail('service-call-result', result)
    }
}

/*
 *  Copyright (c) 2014 OCLC, Inc. All Rights Reserved.
 *
 *  OCLC proprietary information: the enclosed materials contain proprietary information of OCLC Online Computer
 *  Library Center, Inc. and shall not be disclosed in whole or in any part to any third party or used by any person
 *  for any purpose, without written consent of OCLC, Inc.  Duplication of any portion of these materials shall include
 *  this notice.
 */
package org.oclc.elk.demo.springboot

import org.oclc.elk.demo.health.RandomHealthIndicator
import org.oclc.elk.demo.health.ServiceCallHealthIndicator
import org.oclc.elk.demo.support.RandomExceptionThrowingClass
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.actuate.health.HealthIndicator
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class HealthConfig {

    @Value('${service.b.url}')
    def serviceBUrl

    @Value('${service.c.url}')
    def serviceCUrl

    @Autowired()
    @Qualifier('restTemplate')
    def restTemplate

    @Bean
    HealthIndicator randomHealthIndicator() {
        new RandomHealthIndicator(
                myClass: new RandomExceptionThrowingClass()
        )
    }

    @Bean
    HealthIndicator serviceBHealthIndicator() {
        new ServiceCallHealthIndicator(restTemplate: restTemplate, serviceUri: serviceBUrl)
    }

    @Bean
    HealthIndicator serviceCHealthIndicator() {
        new ServiceCallHealthIndicator(restTemplate: restTemplate, serviceUri: serviceCUrl)
    }
}

/*
 *  Copyright (c) 2014 OCLC, Inc. All Rights Reserved.
 *
 *  OCLC proprietary information: the enclosed materials contain proprietary information of OCLC Online Computer
 *  Library Center, Inc. and shall not be disclosed in whole or in any part to any third party or used by any person
 *  for any purpose, without written consent of OCLC, Inc.  Duplication of any portion of these materials shall include
 *  this notice.
 */
package org.oclc.elk.demo.conf.trace

import com.codahale.metrics.annotation.Timed
import org.springframework.aop.Advisor
import org.springframework.aop.interceptor.CustomizableTraceInterceptor
import org.springframework.aop.support.DefaultPointcutAdvisor
import org.springframework.aop.support.annotation.AnnotationMatchingPointcut
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.EnableAspectJAutoProxy

@Configuration
@EnableAspectJAutoProxy(proxyTargetClass = true)
class TraceConfig {

    @Bean
    public CustomizableTraceInterceptor customizableTraceInterceptor() {
        new CustomizableTraceInterceptor(
                useDynamicLogger: false,
                loggerName: 'org.oclc.tracing.timed',
                enterMessage: 'Entering $[targetClassShortName]:$[methodName] method with arguments $[arguments]',
                exitMessage: 'Method execution duration was $[invocationTime] ms for $[targetClassShortName]:$[methodName] with arguments $[arguments]'
        )
    }

    @Bean
    public Advisor timedMethods() {
        new DefaultPointcutAdvisor(new AnnotationMatchingPointcut(null, Timed), customizableTraceInterceptor())
    }

}

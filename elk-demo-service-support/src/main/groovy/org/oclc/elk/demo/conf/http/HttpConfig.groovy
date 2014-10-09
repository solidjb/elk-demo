/*
 *  Copyright (c) 2014 OCLC, Inc. All Rights Reserved.
 *
 *  OCLC proprietary information: the enclosed materials contain proprietary information of OCLC Online Computer
 *  Library Center, Inc. and shall not be disclosed in whole or in any part to any third party or used by any person
 *  for any purpose, without written consent of OCLC, Inc.  Duplication of any portion of these materials shall include
 *  this notice.
 */
package org.oclc.elk.demo.conf.http

import com.codahale.metrics.JmxReporter
import com.codahale.metrics.MetricRegistry
import com.codahale.metrics.httpclient.InstrumentedHttpClients
import org.apache.http.HttpRequestInterceptor
import org.apache.http.HttpResponseInterceptor
import org.apache.http.client.HttpClient
import org.oclc.elk.demo.logging.http.ClientIdRequestInterceptor
import org.oclc.elk.demo.logging.http.LoggingRequestAndResponseInterceptor
import org.oclc.elk.demo.logging.http.RequestIdsRequestInterceptor
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory
import org.springframework.web.client.RestTemplate

@Configuration
class HttpConfig {

    @Value('${http.clientId}')
    String clientId

    @Autowired
    MetricRegistry metricRegistry

    @Bean
    RestTemplate restTemplate() {
        new RestTemplate(
                new HttpComponentsClientHttpRequestFactory(
                        httpClient()
                )
        )
    }

    @Bean
    JmxReporter jmxStatsReporter() {
        final JmxReporter reporter = JmxReporter.forRegistry(metricRegistry).build()
        reporter.start()

        reporter
    }

    @Bean
    HttpClient httpClient() {
        def httpClientBuilder = InstrumentedHttpClients.custom(metricRegistry)
        httpClientBuilder.disableAutomaticRetries()

        httpClientBuilder.addInterceptorFirst(clientIdRequestInterceptor())
        httpClientBuilder.addInterceptorFirst(requestIdsRequestInterceptor())

        HttpRequestInterceptor loggingInterceptor = loggingRequestInterceptor()

        httpClientBuilder.addInterceptorLast(loggingInterceptor as HttpRequestInterceptor)
        httpClientBuilder.addInterceptorLast(loggingInterceptor as HttpResponseInterceptor)


        httpClientBuilder.build()
    }

    HttpRequestInterceptor clientIdRequestInterceptor() {
        new ClientIdRequestInterceptor(clientId)
    }

    HttpRequestInterceptor requestIdsRequestInterceptor() {
        new RequestIdsRequestInterceptor()
    }

    HttpRequestInterceptor loggingRequestInterceptor() {
        new LoggingRequestAndResponseInterceptor()
    }
}

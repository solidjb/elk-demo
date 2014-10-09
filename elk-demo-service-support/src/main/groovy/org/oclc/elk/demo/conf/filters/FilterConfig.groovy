/*
 *  Copyright (c) 2014 OCLC, Inc. All Rights Reserved.
 *
 *  OCLC proprietary information: the enclosed materials contain proprietary information of OCLC Online Computer
 *  Library Center, Inc. and shall not be disclosed in whole or in any part to any third party or used by any person
 *  for any purpose, without written consent of OCLC, Inc.  Duplication of any portion of these materials shall include
 *  this notice.
 */
package org.oclc.elk.demo.conf.filters

import org.eclipse.jetty.servlets.GzipFilter
import org.oclc.elk.demo.logging.filter.CommonFieldMDCPopulatingFilter
import org.oclc.elk.demo.logging.filter.RequestIdGeneratingFilter
import org.springframework.boot.context.embedded.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.filter.CharacterEncodingFilter

/**
 * Spring JavaConfig file to build the standard OCLC filters.
 */
@Configuration
class FilterConfig {

    @Bean
    public FilterRegistrationBean gzipFilterBean() {
        new FilterRegistrationBean(filter: new GzipFilter(), order: 1)
    }

    @Bean
    public FilterRegistrationBean charEncodingFilter() {
        new FilterRegistrationBean(filter: new CharacterEncodingFilter(), order: 2)
    }

    @Bean
    public FilterRegistrationBean requestIdFilter() {
        new FilterRegistrationBean(filter: new RequestIdGeneratingFilter(), order: 3)
    }

    @Bean
    public FilterRegistrationBean mdcPopulatingFilter() {
        new FilterRegistrationBean(filter: new CommonFieldMDCPopulatingFilter(), order: 4)
    }
}
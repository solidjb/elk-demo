package org.oclc.elk.demo.springboot

import com.ryantenney.metrics.spring.config.annotation.EnableMetrics
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.EnableMBeanExport
import org.springframework.context.annotation.ImportResource

/**
 * Spring boot bootstrap file.
 */
@Configuration
@EnableAutoConfiguration
@ComponentScan(
        [
                'org.oclc.elk.demo.springboot',
                'org.oclc.elk.demo.conf.filters',
                'org.oclc.elk.demo.conf.trace',
                'org.oclc.elk.demo.conf.jetty'
        ]
)
@EnableMetrics(proxyTargetClass = true)
class Application {

    /**
     * Main method to boot up app.
     * @param args Arguments
     */
    static void main(String[] args) {
        SpringApplication.run(Application, args)
    }
}
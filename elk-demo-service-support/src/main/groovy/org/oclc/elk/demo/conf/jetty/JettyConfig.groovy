package org.oclc.elk.demo.conf.jetty

import ch.qos.logback.access.jetty.RequestLogImpl
import org.eclipse.jetty.server.Handler
import org.eclipse.jetty.server.RequestLog
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.handler.RequestLogHandler
import org.springframework.boot.context.embedded.ConfigurableEmbeddedServletContainer
import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer
import org.springframework.boot.context.embedded.jetty.JettyEmbeddedServletContainerFactory
import org.springframework.boot.context.embedded.jetty.JettyServerCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Spring JavaConfig file to manipulate jetty.
 */
@Configuration
public class JettyConfig {

    @Bean
    EmbeddedServletContainerCustomizer containerCustomizer() {

        [
                customize: { ConfigurableEmbeddedServletContainer container ->
                    if (container instanceof JettyEmbeddedServletContainerFactory) {
                        JettyEmbeddedServletContainerFactory containerFactory = (JettyEmbeddedServletContainerFactory) container
                        containerFactory.addServerCustomizers(setupHandlers())
                    }
                }

        ] as EmbeddedServletContainerCustomizer
    }

    @Bean
    JettyServerCustomizer setupHandlers() {
        [
                customize: { Server server ->
                    Handler originalHandler = server.handler

                    RequestLogHandler logHandler = setupLogHandler()

                    logHandler.setHandler(originalHandler)

                    server.handler = logHandler
                }
        ] as JettyServerCustomizer
    }

    @Bean
    RequestLog makeRequestLog() {
        new RequestLogImpl(resource: '/logback-access.xml')
    }

    @Bean
    RequestLogHandler setupLogHandler() {
        new RequestLogHandler(requestLog: makeRequestLog())
    }
}
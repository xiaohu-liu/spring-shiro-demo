package com.sojson.core.shiro.config;// package com.sojson.core.shiro.config;

import org.springframework.boot.web.server.ConfigurableWebServerFactory;
import org.springframework.boot.web.server.ErrorPage;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;

@Configuration
public class ContainerConfig {
    @Bean
    public WebServerFactoryCustomizer<ConfigurableWebServerFactory> webServerFactoryCustomizer() {
        return factory -> {
            // factory.setPort(8081);
            //状态码 ：HttpStatus.NOT_FOUND（404）       错误页面的存储路径：/WEB-INF/views/common/error_404.jsp
            ErrorPage errorPage400 = new ErrorPage(HttpStatus.NOT_FOUND, "/open/404.shtml");
            ErrorPage errorPage404 = new ErrorPage(HttpStatus.FORBIDDEN, "/open/404.shtml");
            ErrorPage errorPage500 = new ErrorPage(HttpStatus.INTERNAL_SERVER_ERROR, "/open/404.shtml");
            factory.addErrorPages(errorPage400, errorPage404, errorPage500);
        };
    }
}

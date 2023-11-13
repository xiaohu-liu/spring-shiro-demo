package com.sojson.core.shiro.config;

import com.alibaba.druid.support.http.StatViewServlet;
import com.sojson.core.freemarker.extend.FreeMarkerConfigExtend;
import com.sojson.core.tags.APITemplateModel;
import freemarker.template.utility.XmlEscape;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;
import org.springframework.web.servlet.view.freemarker.FreeMarkerViewResolver;

import javax.servlet.http.HttpServlet;
import java.nio.charset.Charset;
import java.util.*;

@Configuration
public class MvcConfig implements WebMvcConfigurer {
    @Bean(value = "viewResolverCommon")
    public InternalResourceViewResolver viewResolverCommon() {
        InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
        viewResolver.setPrefix("/WEB-INF/views/");
        viewResolver.setSuffix(".jsp");
        viewResolver.setViewClass(org.springframework.web.servlet.view.InternalResourceView.class);
        viewResolver.setOrder(1);
        return viewResolver;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/images/**").addResourceLocations("classpath:/images/");
        registry.addResourceHandler("/demo/**").addResourceLocations("classpath:/demo/");
        registry.addResourceHandler("/css/**").addResourceLocations("classpath:/css/");
        registry.addResourceHandler("/js/**").addResourceLocations("classpath:/js/");
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/").setViewName("/user/index");
        // registry.addViewController("/u/login.shtml").setViewName("/u/login");
    }

    /*@Bean
    public ServletRegistrationBean servletRegistrationBean(DispatcherServlet dispatcherServlet) {
        ServletRegistrationBean<DispatcherServlet> servletServletRegistrationBean = new ServletRegistrationBean<>(dispatcherServlet);
        servletServletRegistrationBean.addUrlMappings("*.shtml");
        return servletServletRegistrationBean;
    }*/


    @Bean
    public MappingJackson2HttpMessageConverter mappingJacksonHttpMessageConverter() {
        MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter = new MappingJackson2HttpMessageConverter();
        //设置中文编码格式
        List<MediaType> list = new ArrayList<MediaType>();
        list.add(new MediaType("text", "plain", Charset.forName("UTF-8")));
        list.add(new MediaType("*", "*", Charset.forName("UTF-8")));
        list.add(new MediaType("text", "*", Charset.forName("UTF-8")));
        list.add(new MediaType("application", "json", Charset.forName("UTF-8")));
        mappingJackson2HttpMessageConverter.setSupportedMediaTypes(list);
        return mappingJackson2HttpMessageConverter;
    }

    @Bean
    @Primary
    public RequestMappingHandlerAdapter requestMappingHandlerAdapter(List<HttpMessageConverter<?>> messageConverters) {
        RequestMappingHandlerAdapter requestMappingHandlerAdapter = new RequestMappingHandlerAdapter();
        requestMappingHandlerAdapter.setMessageConverters(messageConverters);
        return requestMappingHandlerAdapter;
    }

    @Bean
    public MultipartResolver multipartResolver() {
        CommonsMultipartResolver multipartResolver = new CommonsMultipartResolver();
        multipartResolver.setDefaultEncoding("utf-8");
        multipartResolver.setMaxUploadSize(50048000);
        return multipartResolver;
    }

    @Bean
    public XmlEscape fmXmlEscape() {
        return new freemarker.template.utility.XmlEscape();
    }

    @Bean(value = "viewResolverFtl")
    public FreeMarkerViewResolver viewResolverFtl() {
        FreeMarkerViewResolver freeMarkerViewResolver = new FreeMarkerViewResolver();
        freeMarkerViewResolver.setViewClass(com.sojson.core.freemarker.extend.FreeMarkerViewExtend.class);
        freeMarkerViewResolver.setContentType("text/html; charset=utf-8");
        freeMarkerViewResolver.setCache(false);
        freeMarkerViewResolver.setSuffix(".ftl");
        freeMarkerViewResolver.setOrder(0);
        return freeMarkerViewResolver;
    }

    @Bean(value = "viewResolver")
    public InternalResourceViewResolver viewResolver() {
        InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
        viewResolver.setPrefix("/WEB-INF/views/");
        viewResolver.setSuffix(".jsp");
        viewResolver.setViewClass(org.springframework.web.servlet.view.JstlView.class);
        viewResolver.setOrder(2);
        return viewResolver;

    }

    @Bean
    public FreeMarkerConfigurer freemarkerConfig(XmlEscape fmXmlEscape, APITemplateModel api) {
        FreeMarkerConfigurer freeMarkerConfigExtend = new FreeMarkerConfigExtend();
        freeMarkerConfigExtend.setTemplateLoaderPath("/WEB-INF/ftl/");
        Map<String, Object> variables = new HashMap<>();
        variables.put("xml_escape", fmXmlEscape);
        variables.put("api", api);
        freeMarkerConfigExtend.setFreemarkerVariables(variables);
        freeMarkerConfigExtend.setDefaultEncoding("utf-8");
        Properties properties = new Properties();
        properties.put("template_update_delay", "0");
        properties.put("defaultEncoding", "UTF-8");
        properties.put("url_escaping_charset", "UTF-8");
        properties.put("locale", "zh_CN");
        properties.put("boolean_format", "true,false");
        properties.put("datetime_format", "yyyy-MM-dd HH:mm:ss");
        properties.put("date_format", "yyyy-MM-dd");
        properties.put("time_format", "HH:mm:ss");
        properties.put("number_format", "#");
        properties.put("whitespace_stripping", "true");
        properties.put("auto_import", "/common/config/top.ftl as _top,/common/config/left.ftl as _left");
        freeMarkerConfigExtend.setFreemarkerSettings(properties);
        return freeMarkerConfigExtend;
    }


    /* @Bean
    public ContentNegotiatingViewResolver contentNegotiatingViewResolver(ContentNegotiationManager manager) {
        ContentNegotiatingViewResolver resolver = new ContentNegotiatingViewResolver();
        resolver.setContentNegotiationManager(manager);
        List<ViewResolver> resolvers = new ArrayList<ViewResolver>();
        resolvers.add(viewResolverFtl());
        resolvers.add(viewResolverCommon());
        resolvers.add(viewResolver());
        resolver.setViewResolvers(resolvers);
        return resolver;
    } */


    @Bean
    public ServletRegistrationBean<HttpServlet> stateServlet() {
        ServletRegistrationBean<HttpServlet> servRegBean = new ServletRegistrationBean<>();
        servRegBean.setServlet(new StatViewServlet());
        servRegBean.addUrlMappings("/druid/*");
        servRegBean.setLoadOnStartup(1);
        return servRegBean;
    }

    /*@Bean(name = "dispatcherServlet")
    public DispatcherServlet dispatcherServlet() {
        return new DispatcherServlet();
    }

    @Bean
    public ServletRegistrationBean servletRegistrationBean(DispatcherServlet dispatcherServlet) {
        ServletRegistrationBean<DispatcherServlet> servletServletRegistrationBean = new ServletRegistrationBean<>(dispatcherServlet);
        servletServletRegistrationBean.addUrlMappings("*.shtml");
        return servletServletRegistrationBean;
    }*/
}

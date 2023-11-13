package com.sojson.core.shiro.config;


import com.sojson.common.utils.LoggerUtils;
import com.sojson.core.config.INI4j;
import com.sojson.core.shiro.CustomShiroSessionDAO;
import com.sojson.core.shiro.filter.*;
import com.sojson.core.shiro.service.ShiroManager;
import com.sojson.core.shiro.service.impl.ShiroManagerImpl;
import com.sojson.core.shiro.token.SampleRealm;
import org.apache.shiro.cache.CacheManager;
import org.apache.shiro.codec.Base64;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.session.SessionListener;
import org.apache.shiro.session.mgt.ExecutorServiceSessionValidationScheduler;
import org.apache.shiro.session.mgt.SessionManager;
import org.apache.shiro.session.mgt.SessionValidationScheduler;
import org.apache.shiro.session.mgt.ValidatingSessionManager;
import org.apache.shiro.session.mgt.eis.JavaUuidSessionIdGenerator;
import org.apache.shiro.session.mgt.eis.SessionIdGenerator;
import org.apache.shiro.spring.LifecycleBeanPostProcessor;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.CookieRememberMeManager;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.servlet.SimpleCookie;
import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;
import org.springframework.beans.factory.config.MethodInvokingFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import javax.servlet.Filter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Configuration
public class ShiroConfig {
    @Bean
    public SimpleCookie rememberMeCookie() {
        SimpleCookie simpleCookie = new SimpleCookie("v_v-re-baidu");
        simpleCookie.setHttpOnly(true);
        simpleCookie.setMaxAge(2592000);
        return simpleCookie;
    }

    @Bean
    public SimpleCookie sessionIdCookie() {
        SimpleCookie sessionIdCookie = new SimpleCookie("v_v-s-baidu");
        sessionIdCookie.setHttpOnly(true);
        sessionIdCookie.setMaxAge(-1);
        return sessionIdCookie;
    }


    @Bean
    public CookieRememberMeManager rememberMeManager(SimpleCookie rememberMeCookie) {
        CookieRememberMeManager cookieRememberMeManager = new CookieRememberMeManager();
        cookieRememberMeManager.setCipherKey(Base64.decode("'3AvVhmFLUs0KTA3Kprsdag=='"));
        cookieRememberMeManager.setCookie(rememberMeCookie);
        return cookieRememberMeManager;
    }

    @Bean
    public SecurityManager securityManager(
            SessionManager sessionManager,
            CookieRememberMeManager rememberMeManager,
            SampleRealm sampleRealm,
            CacheManager shiroCacheManager) {
        DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();
        securityManager.setCacheManager(shiroCacheManager);
        securityManager.setRememberMeManager(rememberMeManager);
        securityManager.setRealm(sampleRealm);
        securityManager.setSessionManager(sessionManager);
        return securityManager;
    }

    @Bean
    public ValidatingSessionManager sessionManager(
            CustomShiroSessionDAO customShiroSessionDAO,
            List<SessionListener> sessionListeners,
            SimpleCookie sessionIdCookie) {
        DefaultWebSessionManager sessionManager = new DefaultWebSessionManager();
        sessionManager.setSessionValidationInterval(1800000);
        sessionManager.setGlobalSessionTimeout(1800000);
        sessionManager.setSessionDAO(customShiroSessionDAO);
        sessionManager.setSessionListeners(sessionListeners);
        // sessionManager.setSessionValidationScheduler(sessionValidationScheduler);
        sessionManager.setSessionValidationSchedulerEnabled(true);
        sessionManager.setDeleteInvalidSessions(true);
        sessionManager.setSessionIdCookie(sessionIdCookie);
        return sessionManager;
    }


    @Bean
    public KickoutSessionFilter kickoutSessionFilter() {
        KickoutSessionFilter.setKickoutUrl("/u/login.shtml?kickout");
        KickoutSessionFilter kickoutSessionFilter = new KickoutSessionFilter();
        return kickoutSessionFilter;
    }

    // @Bean
    public ShiroManager shiroManager() {
        return new ShiroManagerImpl();
    }

    @Bean
    public ShiroFilterFactoryBean shiroFilter(SecurityManager securityManager) {
        ShiroFilterFactoryBean shiroFilterFactoryBean = new ShiroFilterFactoryBean();
        shiroFilterFactoryBean.setSecurityManager(securityManager);
        shiroFilterFactoryBean.setLoginUrl("/u/login.shtml");
        shiroFilterFactoryBean.setSuccessUrl("/");
        shiroFilterFactoryBean.setUnauthorizedUrl("/?login");
        // shiroFilterFactoryBean.setFilterChainDefinitions(shiroManager.loadFilterChainDefinitions());
        shiroFilterFactoryBean.setFilterChainDefinitions(loadFilterChainDefinitions());
        // 注册filter
        Map<String, Filter> filters = new HashMap<>();
        filters.put("login", new LoginFilter());
        filters.put("role", new RoleFilter());
        filters.put("simple", new SimpleAuthFilter());
        filters.put("permission", new PermissionFilter());
        filters.put("kickout", kickoutSessionFilter());
        shiroFilterFactoryBean.setFilters(filters);

        return shiroFilterFactoryBean;
    }

    @Bean
    public SessionIdGenerator sessionIdGenerator() {
        return new JavaUuidSessionIdGenerator();
    }

    @Bean
    public SessionValidationScheduler sessionValidationScheduler(ValidatingSessionManager sessionManager) {
        ExecutorServiceSessionValidationScheduler executorServiceSessionValidationScheduler = new ExecutorServiceSessionValidationScheduler();
        executorServiceSessionValidationScheduler.setInterval(18000000);
        executorServiceSessionValidationScheduler.setSessionManager(sessionManager);
        return executorServiceSessionValidationScheduler;
    }

    @Bean
    public LifecycleBeanPostProcessor lifecycleBeanPostProcessor() {
        return new LifecycleBeanPostProcessor();
    }

    @Bean
    public MethodInvokingFactoryBean methodInvokingFactoryBean(SecurityManager securityManager) {
        MethodInvokingFactoryBean factoryBean = new MethodInvokingFactoryBean();
        factoryBean.setStaticMethod("org.apache.shiro.SecurityUtils.setSecurityManager");
        factoryBean.setArguments(securityManager);
        return factoryBean;
    }


    public String loadFilterChainDefinitions() {
        StringBuffer sb = new StringBuffer();
        sb.append(getFixedAuthRule());//固定权限，采用读取配置文件
        return sb.toString();
    }

    /**
     * 从配额文件获取固定权限验证规则串
     */
    private String getFixedAuthRule() {
        String fileName = "shiro_base_auth.ini";
        ClassPathResource cp = new ClassPathResource(fileName);
        INI4j ini = null;
        try {
            ini = new INI4j(cp.getFile());
        } catch (IOException e) {
            LoggerUtils.fmtError(getClass(), e, "加载文件出错。file:[%s]", fileName);
        }
        String section = "base_auth";
        Set<String> keys = ini.get(section).keySet();
        StringBuffer sb = new StringBuffer();
        for (String key : keys) {
            String value = ini.get(section, key);
            sb.append(key).append(" = ").append(value).append(CRLF);
        }

        return sb.toString();

    }

    private static final String CRLF = "\r\n";

}

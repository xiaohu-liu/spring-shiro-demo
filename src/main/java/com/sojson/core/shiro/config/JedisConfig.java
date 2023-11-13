package com.sojson.core.shiro.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

@Configuration
public class JedisConfig {

    @Bean
    public JedisPoolConfig jedisPoolConfig() {
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxIdle(100);
        jedisPoolConfig.setMinIdle(10);
        jedisPoolConfig.setTestOnBorrow(true);
        return jedisPoolConfig;
    }

    @Bean
    public JedisPool jedisPool() {
        JedisPool jedisPool = new JedisPool(jedisPoolConfig(), "localhost", 6379, 5000);
        return jedisPool;
    }
}

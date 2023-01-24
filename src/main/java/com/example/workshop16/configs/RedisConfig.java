package com.example.workshop16.configs;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {
    
    @Value("${spring.redis.host}")
    private String redisHost;

    @Value("${spring.redis.port}")
    private String redisPort;

    @Value("${spring.redis.username}")
    private String redisUsername;

    @Value("${spring.redis.password}")
    private String redisPassword;

    @Bean
    public RedisTemplate<String, Object> redisTemplate() {
        final RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();

        config.setHostName(redisHost);
        config.setPort(Integer.parseInt(redisPort));

        if (!redisUsername.isEmpty() && !redisPassword.isEmpty()) {
            config.setUsername(redisUsername);
            config.setPassword(redisPassword);
        }
        config.setDatabase(0);

        final JedisClientConfiguration jedisClient = JedisClientConfiguration
                .builder()
                .build();
        final JedisConnectionFactory jedisFac = new JedisConnectionFactory(config, jedisClient);
        jedisFac.afterPropertiesSet();
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<String, Object>();
        // associate with the redis connection
        redisTemplate.setConnectionFactory(jedisFac);

        redisTemplate.setKeySerializer(new StringRedisSerializer());
        // set the map key/value serialization type to String
        redisTemplate.setHashKeySerializer(redisTemplate.getKeySerializer());
        redisTemplate.setValueSerializer(new StringRedisSerializer());
        redisTemplate.setHashValueSerializer(new StringRedisSerializer());

        return redisTemplate;
    }

}

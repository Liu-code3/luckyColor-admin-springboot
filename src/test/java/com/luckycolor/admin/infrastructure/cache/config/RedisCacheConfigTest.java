package com.luckycolor.admin.infrastructure.cache.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

class RedisCacheConfigTest {

    @Test
    void shouldConfigureRedisTemplateSerializers() {
        RedisCacheConfig config = new RedisCacheConfig();
        RedisConnectionFactory connectionFactory = Mockito.mock(RedisConnectionFactory.class);

        RedisTemplate<String, Object> redisTemplate = config.redisTemplate(connectionFactory, new ObjectMapper());

        assertThat(redisTemplate.getConnectionFactory()).isSameAs(connectionFactory);
        assertThat(redisTemplate.getKeySerializer()).isInstanceOf(StringRedisSerializer.class);
        assertThat(redisTemplate.getHashKeySerializer()).isInstanceOf(StringRedisSerializer.class);
        assertThat(redisTemplate.getValueSerializer()).isInstanceOf(GenericJackson2JsonRedisSerializer.class);
        assertThat(redisTemplate.getHashValueSerializer()).isInstanceOf(GenericJackson2JsonRedisSerializer.class);
    }
}

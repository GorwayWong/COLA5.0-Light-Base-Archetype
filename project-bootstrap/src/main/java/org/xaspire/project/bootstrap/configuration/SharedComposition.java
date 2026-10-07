package org.xaspire.project.bootstrap.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.xaspire.project.shared.id.IdGenerator;
import org.xaspire.project.shared.id.UuidGenerator;
import org.xaspire.project.shared.infrastructure.redis.RedisLock;
import org.xaspire.project.shared.infrastructure.redis.RedisStrings;
import org.xaspire.project.shared.json.JsonCodec;

@Configuration(proxyBeanMethods = false)
public class SharedComposition {
    @Bean
    IdGenerator idGenerator() { return new UuidGenerator(); }

    @Bean
    Clock clock() { return Clock.systemUTC(); }

    @Bean
    JsonCodec jsonCodec(ObjectMapper mapper) { return new JsonCodec(mapper); }

    @Bean
    RedisStrings redisStrings(StringRedisTemplate redis) { return new RedisStrings(redis); }

    @Bean
    RedisLock redisLock(StringRedisTemplate redis) { return new RedisLock(redis); }
}

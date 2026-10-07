package org.xaspire.project.sample.domain.service;
import org.springframework.data.redis.core.StringRedisTemplate;
public record InvalidFrameworkAccess(StringRedisTemplate redis) { }

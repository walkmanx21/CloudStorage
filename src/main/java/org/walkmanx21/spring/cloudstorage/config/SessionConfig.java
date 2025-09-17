package org.walkmanx21.spring.cloudstorage.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

@EnableRedisHttpSession(maxInactiveIntervalInSeconds = 7 * 24 * 60 * 60)
@Configuration
public class SessionConfig {
}

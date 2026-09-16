package org.xaspire.tolink.bootstrap;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.test.context.TestConfiguration;

@TestConfiguration(proxyBeanMethods = false)
@MapperScan("org.xaspire.tolink.bootstrap.persistence")
public class TestPersistenceConfiguration {
}

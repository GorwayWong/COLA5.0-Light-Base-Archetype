package org.xaspire.tolink.infrastructure;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.test.context.TestConfiguration;

@TestConfiguration(proxyBeanMethods = false)
@MapperScan("org.xaspire.tolink.infrastructure.persistence")
public class TestPersistenceConfiguration {
}

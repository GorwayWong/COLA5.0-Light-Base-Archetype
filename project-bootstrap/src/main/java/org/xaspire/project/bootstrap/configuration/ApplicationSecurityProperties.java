package org.xaspire.project.bootstrap.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("project.security")
public record ApplicationSecurityProperties(boolean permitApiDocs) {
}

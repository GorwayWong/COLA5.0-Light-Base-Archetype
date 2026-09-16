package org.xaspire.tolink.bootstrap.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "tolink.security")
public class ApplicationSecurityProperties {

    private boolean permitApiDocs;

    public boolean isPermitApiDocs() {
        return permitApiDocs;
    }

    public void setPermitApiDocs(boolean permitApiDocs) {
        this.permitApiDocs = permitApiDocs;
    }
}

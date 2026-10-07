package org.xaspire.project.consumer.domain;
import org.springframework.stereotype.Component;
import org.xaspire.project.provider.api.PublicContract;
@Component
public record InvalidDomain(PublicContract contract) { }

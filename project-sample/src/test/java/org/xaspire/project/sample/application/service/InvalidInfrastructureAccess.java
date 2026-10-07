package org.xaspire.project.sample.application.service;
import org.xaspire.project.sample.infrastructure.persistence.repository.AllowedRepository;
public record InvalidInfrastructureAccess(AllowedRepository repository) { }

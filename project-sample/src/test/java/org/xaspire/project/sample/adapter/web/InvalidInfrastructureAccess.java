package org.xaspire.project.sample.adapter.web;
import org.xaspire.project.sample.infrastructure.persistence.repository.AllowedRepository;
public record InvalidInfrastructureAccess(AllowedRepository repository) { }

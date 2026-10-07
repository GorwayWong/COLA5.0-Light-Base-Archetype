package org.xaspire.project.sample.application.service;
import org.xaspire.project.sample.api.facade.PublicContract;
import org.xaspire.project.sample.domain.model.valobj.CoreValue;
public record AllowedService(CoreValue value) implements PublicContract { }

package org.xaspire.project.architecture;

import com.tngtech.archunit.core.importer.ClassFileImporter;
import org.junit.jupiter.api.Test;
import org.xaspire.project.consumer.application.AllowedApiAccess;
import org.xaspire.project.consumer.application.InvalidInfrastructureAccess;
import org.xaspire.project.consumer.application.InvalidBootstrapDependency;
import org.xaspire.project.consumer.domain.InvalidDomain;
import org.xaspire.project.consumer.domain.InvalidApplicationDependency;
import org.xaspire.project.consumer.application.AllowedApplicationService;
import org.xaspire.project.consumer.application.InvalidApplicationConfiguration;
import org.xaspire.project.consumer.infrastructure.HiddenConfiguration;
import org.xaspire.project.shared.fixture.InvalidSharedDependency;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ArchitectureRulesTest {
    @Test
    void crossModuleApiIsAllowed() {
        var classes = new ClassFileImporter().importClasses(AllowedApiAccess.class);
        assertFalse(ArchitectureTest.moduleAndLayerBoundaries.evaluate(classes).hasViolation());
    }

    @Test
    void crossModuleInfrastructureIsRejected() {
        var classes = new ClassFileImporter().importClasses(InvalidInfrastructureAccess.class);
        assertTrue(ArchitectureTest.moduleAndLayerBoundaries.evaluate(classes).hasViolation());
    }

    @Test
    void domainCannotDependOnSpringOrAnotherModule() {
        var classes = new ClassFileImporter().importClasses(InvalidDomain.class);
        assertTrue(ArchitectureTest.domainIsPureJava.evaluate(classes).hasViolation());
        assertTrue(ArchitectureTest.moduleAndLayerBoundaries.evaluate(classes).hasViolation());
    }

    @Test
    void sharedCannotDependOnBusinessModules() {
        var classes = new ClassFileImporter().importClasses(InvalidSharedDependency.class);
        assertTrue(ArchitectureTest.moduleAndLayerBoundaries.evaluate(classes).hasViolation());
    }

    @Test
    void infrastructureCannotHideBeanConfiguration() {
        var classes = new ClassFileImporter().importClasses(HiddenConfiguration.class);
        assertTrue(ArchitectureTest.onlyBootstrapConfiguresBeans.evaluate(classes).hasViolation());
    }

    @Test
    void domainCannotDependOnItsOwnApplicationLayer() {
        var classes = new ClassFileImporter().importClasses(InvalidApplicationDependency.class);
        assertTrue(ArchitectureTest.moduleAndLayerBoundaries.evaluate(classes).hasViolation());
        assertTrue(ArchitectureTest.domainIsPureJava.evaluate(classes).hasViolation());
    }

    @Test
    void businessCannotDependOnBootstrap() {
        var classes = new ClassFileImporter().importClasses(InvalidBootstrapDependency.class);
        assertTrue(ArchitectureTest.moduleAndLayerBoundaries.evaluate(classes).hasViolation());
    }

    @Test
    void applicationServiceAnnotationIsAllowed() {
        var classes = new ClassFileImporter().importClasses(AllowedApplicationService.class);
        assertFalse(ArchitectureTest.onlyBootstrapConfiguresBeans.evaluate(classes).hasViolation());
    }

    @Test
    void applicationCannotHideConfigurationBehindServiceAnnotation() {
        var classes = new ClassFileImporter().importClasses(InvalidApplicationConfiguration.class);
        assertTrue(ArchitectureTest.onlyBootstrapConfiguresBeans.evaluate(classes).hasViolation());
    }
}

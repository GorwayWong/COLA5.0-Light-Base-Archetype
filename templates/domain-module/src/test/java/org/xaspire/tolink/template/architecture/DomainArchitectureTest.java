package org.xaspire.tolink.template.architecture;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.core.importer.ImportOption.DoNotIncludeTests;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(packages = "org.xaspire.tolink.template", importOptions = DoNotIncludeTests.class)
class DomainArchitectureTest {

    @ArchTest
    static final ArchRule domainMustNotDependOnOuterLayers = noClasses()
            .that()
            .resideInAnyPackage("org.xaspire.tolink.template.domain..")
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage(
                    "org.xaspire.tolink.template.adapter..",
                    "org.xaspire.tolink.template.infrastructure..",
                    "org.springframework..",
                    "com.baomidou.mybatisplus..",
                    "org.springframework.data.redis..",
                    "com.aliyun..",
                    "io.agentscope..");

    @ArchTest
    static final ArchRule applicationMustNotDependOnAdaptersOrInfrastructure = noClasses()
            .that()
            .resideInAnyPackage("org.xaspire.tolink.template.application..")
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage(
                    "org.xaspire.tolink.template.adapter..",
                    "org.xaspire.tolink.template.infrastructure..");

    @ArchTest
    static final ArchRule adapterMustNotDependOnDomainOrInfrastructure = noClasses()
            .that()
            .resideInAnyPackage("org.xaspire.tolink.template.adapter..")
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage(
                    "org.xaspire.tolink.template.domain..",
                    "org.xaspire.tolink.template.infrastructure..");

    @ArchTest
    static final ArchRule infrastructureMustNotDependOnAdapters = noClasses()
            .that()
            .resideInAnyPackage("org.xaspire.tolink.template.infrastructure..")
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage("org.xaspire.tolink.template.adapter..");
}

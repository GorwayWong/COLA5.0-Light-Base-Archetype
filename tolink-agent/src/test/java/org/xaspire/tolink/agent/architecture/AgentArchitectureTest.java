package org.xaspire.tolink.agent.architecture;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.core.importer.ImportOption.DoNotIncludeTests;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(packages = "org.xaspire.tolink.agent", importOptions = DoNotIncludeTests.class)
class AgentArchitectureTest {

    @ArchTest
    static final ArchRule domainMustNotDependOnOuterLayers = noClasses()
            .that()
            .resideInAnyPackage("org.xaspire.tolink.agent.domain..")
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage(
                    "org.xaspire.tolink.agent.adapter..",
                    "org.xaspire.tolink.agent.infrastructure..",
                    "org.springframework..",
                    "com.baomidou.mybatisplus..",
                    "org.springframework.data.redis..",
                    "com.aliyun..",
                    "io.agentscope..");

    @ArchTest
    static final ArchRule applicationMustNotDependOnAdaptersOrInfrastructure = noClasses()
            .that()
            .resideInAnyPackage("org.xaspire.tolink.agent.application..")
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage(
                    "org.xaspire.tolink.agent.adapter..",
                    "org.xaspire.tolink.agent.infrastructure..");

    @ArchTest
    static final ArchRule adapterMustNotDependOnDomainOrInfrastructure = noClasses()
            .that()
            .resideInAnyPackage("org.xaspire.tolink.agent.adapter..")
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage(
                    "org.xaspire.tolink.agent.domain..",
                    "org.xaspire.tolink.agent.infrastructure..");

    @ArchTest
    static final ArchRule infrastructureMustNotDependOnAdapters = noClasses()
            .that()
            .resideInAnyPackage("org.xaspire.tolink.agent.infrastructure..")
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage("org.xaspire.tolink.agent.adapter..");
}

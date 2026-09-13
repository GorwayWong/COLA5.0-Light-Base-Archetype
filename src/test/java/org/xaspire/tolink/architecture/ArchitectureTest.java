package org.xaspire.tolink.architecture;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.core.importer.ImportOption.DoNotIncludeTests;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(packages = "org.xaspire.tolink", importOptions = DoNotIncludeTests.class)
class ArchitectureTest {

    @ArchTest
    static final ArchRule domainMustNotDependOnTechnicalLayers = noClasses()
            .that()
            .resideInAnyPackage("org.xaspire.tolink.domain..")
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage(
                    "org.xaspire.tolink.adapter..",
                    "org.xaspire.tolink.infrastructure..",
                    "com.baomidou.mybatisplus..",
                    "org.springframework.data.redis..",
                    "com.aliyun..",
                    "io.agentscope..");
}

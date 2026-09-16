package org.xaspire.tolink.architecture;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.core.importer.ImportOption.DoNotIncludeTests;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;

import java.util.Set;
import java.util.regex.Pattern;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

@AnalyzeClasses(packages = "org.xaspire.tolink", importOptions = DoNotIncludeTests.class)
class ArchitectureTest {

    @ArchTest
    static final ArchRule domainMustNotDependOnOuterLayers = noClasses()
            .that()
            .resideInAnyPackage("org.xaspire.tolink..domain..")
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage(
                    "org.xaspire.tolink..adapter..",
                    "org.xaspire.tolink..infrastructure..",
                    "org.springframework..",
                    "com.baomidou.mybatisplus..",
                    "org.springframework.data.redis..",
                    "com.aliyun..",
                    "io.agentscope..")
            .allowEmptyShould(true);

    @ArchTest
    static final ArchRule applicationMustNotDependOnAdaptersOrInfrastructure = noClasses()
            .that()
            .resideInAnyPackage("org.xaspire.tolink..application..")
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage(
                    "org.xaspire.tolink..adapter..",
                    "org.xaspire.tolink..infrastructure..")
            .allowEmptyShould(true);

    @ArchTest
    static final ArchRule adapterMustNotDependOnDomainOrInfrastructure = noClasses()
            .that()
            .resideInAnyPackage("org.xaspire.tolink..adapter..")
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage(
                    "org.xaspire.tolink..domain..",
                    "org.xaspire.tolink..infrastructure..")
            .allowEmptyShould(true);

    @ArchTest
    static final ArchRule infrastructureMustNotDependOnAdapters = noClasses()
            .that()
            .resideInAnyPackage("org.xaspire.tolink..infrastructure..")
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage("org.xaspire.tolink..adapter..")
            .allowEmptyShould(true);

    @ArchTest
    static final ArchRule sharedMustRemainFrameworkIndependent = noClasses()
            .that()
            .resideInAnyPackage("org.xaspire.tolink.shared..")
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage(
                    "org.springframework..",
                    "org.xaspire.tolink..domain..",
                    "org.xaspire.tolink..infrastructure..")
            .allowEmptyShould(true);

    @ArchTest
    static final ArchRule domainSlicesMustBeFreeOfCycles = slices()
            .matching("org.xaspire.tolink.(*)..")
            .should()
            .beFreeOfCycles();

    @ArchTest
    static final ArchRule domainsMustNotDependOnAnotherDomainsInternals = classes()
            .that()
            .resideInAnyPackage(
                    "org.xaspire.tolink..adapter..",
                    "org.xaspire.tolink..application..",
                    "org.xaspire.tolink..domain..",
                    "org.xaspire.tolink..infrastructure..")
            .should(notDependOnAnotherDomainInternals())
            .allowEmptyShould(true);

    private static ArchCondition<JavaClass> notDependOnAnotherDomainInternals() {
        return new ArchCondition<>("not depend on another domain's internal classes") {
            @Override
            public void check(JavaClass item, ConditionEvents events) {
                DomainLayer source = DomainLayer.from(item.getPackageName());
                if (source == null) {
                    return;
                }
                item.getDirectDependenciesFromSelf().forEach(dependency -> {
                    DomainLayer target = DomainLayer.from(dependency.getTargetClass().getPackageName());
                    if (target == null || source.domain().equals(target.domain())) {
                        return;
                    }
                    boolean publicApplicationApi = target.layer().equals("application")
                            && target.packageName().matches(".*\\.application\\.api(?:\\..*)?");
                    if (!publicApplicationApi) {
                        events.add(SimpleConditionEvent.violated(
                                item,
                                item.getName() + " depends on " + dependency.getTargetClass().getName()
                                        + "; cross-domain dependencies must target application.api or shared contracts"));
                    }
                });
            }
        };
    }

    private record DomainLayer(String domain, String layer, String packageName) {

        private static final Set<String> NON_DOMAIN_ROOTS = Set.of("bootstrap", "shared", "architecture");
        private static final Set<String> COLA_LAYERS = Set.of("adapter", "application", "domain", "infrastructure");
        private static final Pattern ROOT = Pattern.compile("org\\.xaspire\\.tolink\\.([^.]+)\\.([^.]+)(?:\\..*)?");

        private static DomainLayer from(String packageName) {
            var matcher = ROOT.matcher(packageName);
            if (!matcher.matches()
                    || NON_DOMAIN_ROOTS.contains(matcher.group(1))
                    || !COLA_LAYERS.contains(matcher.group(2))) {
                return null;
            }
            return new DomainLayer(matcher.group(1), matcher.group(2), packageName);
        }
    }
}

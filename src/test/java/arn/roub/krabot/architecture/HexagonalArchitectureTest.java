package arn.roub.krabot.architecture;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.library.Architectures;
import io.quarkus.scheduler.Scheduled;
import io.smallrye.config.ConfigMapping;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Singleton;
import jakarta.ws.rs.Path;
import org.eclipse.microprofile.health.HealthCheck;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.*;

@AnalyzeClasses(packages = "arn.roub.krabot", importOptions = com.tngtech.archunit.core.importer.ImportOption.DoNotIncludeTests.class)
public class HexagonalArchitectureTest {

    // Définition des couches
    static final String DOMAIN_LAYER = "arn.roub.krabot.domain..";
    static final String APPLICATION_LAYER = "arn.roub.krabot.application..";
    static final String INFRASTRUCTURE_LAYER = "arn.roub.krabot.infrastructure..";
    static final String SHARED_LAYER = "arn.roub.krabot.shared..";

    // ══════════════════════════════════════════════════════════════
    // RÈGLE GLOBALE D'ARCHITECTURE EN COUCHES
    // ══════════════════════════════════════════════════════════════

    @ArchTest
    static final ArchRule hexagonal_architecture =
        Architectures.layeredArchitecture()
            .consideringAllDependencies()
            .layer("Domain").definedBy("..domain..")
            .layer("Application").definedBy("..application..")
            .layer("Infrastructure").definedBy("..infrastructure..")
            .layer("Shared").definedBy("..shared..")
            .whereLayer("Domain").mayOnlyBeAccessedByLayers("Application", "Infrastructure")
            .whereLayer("Application").mayOnlyBeAccessedByLayers("Infrastructure")
            .whereLayer("Infrastructure").mayNotBeAccessedByAnyLayer()
            .whereLayer("Shared").mayOnlyBeAccessedByLayers("Domain", "Application", "Infrastructure");

    // ══════════════════════════════════════════════════════════════
    // RÈGLES DU DOMAIN
    // ══════════════════════════════════════════════════════════════

    @ArchTest
    static final ArchRule domain_should_not_depend_on_application =
        noClasses().that().resideInAPackage(DOMAIN_LAYER)
            .should().dependOnClassesThat().resideInAPackage(APPLICATION_LAYER)
            .because("Le Domain ne doit jamais dépendre de l'Application");

    @ArchTest
    static final ArchRule domain_should_not_depend_on_infrastructure =
        noClasses().that().resideInAPackage(DOMAIN_LAYER)
            .should().dependOnClassesThat().resideInAPackage(INFRASTRUCTURE_LAYER)
            .because("Le Domain ne doit jamais dépendre de l'Infrastructure");

    @ArchTest
    static final ArchRule domain_should_not_use_frameworks =
        noClasses().that().resideInAPackage(DOMAIN_LAYER)
            .should().dependOnClassesThat().resideInAnyPackage(
                "jakarta..",
                "javax..",
                "io.quarkus..",
                "org.jsoup..",
                "com.fasterxml.."
            )
            .because("Le Domain doit être indépendant des frameworks");

    // ══════════════════════════════════════════════════════════════
    // RÈGLES DE L'APPLICATION
    // ══════════════════════════════════════════════════════════════

    @ArchTest
    static final ArchRule application_should_not_depend_on_infrastructure =
        noClasses().that().resideInAPackage(APPLICATION_LAYER)
            .should().dependOnClassesThat().resideInAPackage(INFRASTRUCTURE_LAYER)
            .because("L'Application ne doit pas dépendre de l'Infrastructure");

    @ArchTest
    static final ArchRule application_should_only_depend_on_allowed_packages =
        classes().that().resideInAPackage(APPLICATION_LAYER)
            .should().onlyDependOnClassesThat()
            .resideInAnyPackage(
                DOMAIN_LAYER,
                APPLICATION_LAYER,
                SHARED_LAYER,
                "java..",
                "org.slf4j.."
            )
            .because("L'Application ne peut dépendre que du Domain, Shared et des librairies standard");

    // ══════════════════════════════════════════════════════════════
    // RÈGLES DES PORTS
    // ══════════════════════════════════════════════════════════════

    @ArchTest
    static final ArchRule ports_should_be_interfaces =
        classes().that().resideInAPackage("..domain.port..")
            .should().beInterfaces()
            .because("Les ports doivent être des interfaces");

    @ArchTest
    static final ArchRule primary_ports_should_be_usecases =
        classes().that().resideInAPackage("..domain.port.in..")
            .should().haveSimpleNameEndingWith("UseCase")
            .because("Les ports primaires définissent des Use Cases");

    @ArchTest
    static final ArchRule secondary_ports_should_have_port_suffix =
        classes().that().resideInAPackage("..domain.port.out..")
            .should().haveSimpleNameEndingWith("Port")
            .because("Les ports secondaires doivent avoir le suffixe Port");

    // ══════════════════════════════════════════════════════════════
    // RÈGLES DES ADAPTERS
    // ══════════════════════════════════════════════════════════════

    @ArchTest
    static final ArchRule outbound_adapters_should_implement_ports =
        classes().that().resideInAPackage("..infrastructure.adapter.out..")
            .and().haveSimpleNameEndingWith("Adapter")
            .should().implement(JavaClass.Predicates.resideInAPackage("..domain.port.out.."))
            .because("Les adapters sortants doivent implémenter un port secondaire");

    @ArchTest
    static final ArchRule usecases_should_implement_primary_ports =
        classes().that().resideInAPackage("..application.usecase..")
            .and().haveSimpleNameEndingWith("UseCaseImpl")
            .should().implement(JavaClass.Predicates.resideInAPackage("..domain.port.in.."))
            .because("Les implémentations de use cases doivent implémenter un port primaire");

    // ══════════════════════════════════════════════════════════════
    // RÈGLES DU DOMAIN MODEL
    // ══════════════════════════════════════════════════════════════

    @ArchTest
    static final ArchRule domain_models_should_be_immutable =
        classes().that().resideInAPackage("..domain.model..")
            .should().beRecords()
            .orShould().haveModifier(JavaModifier.FINAL)
            .because("Les modèles du domain doivent être immutables (records ou final)");

    @ArchTest
    static final ArchRule domain_models_should_not_have_setters =
        noMethods().that().areDeclaredInClassesThat()
            .resideInAPackage("..domain.model..")
            .should().haveNameMatching("set.*")
            .because("Les modèles du domain ne doivent pas avoir de setters");

    @ArchTest
    static final ArchRule domain_services_should_be_stateless =
        fields().that().areDeclaredInClassesThat()
            .resideInAPackage("..domain.service..")
            .should().beFinal()
            .allowEmptyShould(true)
            .because("Les services du domain doivent être stateless");

    // ══════════════════════════════════════════════════════════════
    // RÈGLES DE L'INFRASTRUCTURE
    // ══════════════════════════════════════════════════════════════

    @ArchTest
    static final ArchRule only_infrastructure_or_shared_uses_cdi_annotations =
        classes().that().areAnnotatedWith(ApplicationScoped.class)
            .or().areAnnotatedWith(Singleton.class)
            .or().areAnnotatedWith(Dependent.class)
            .should().resideInAnyPackage("..infrastructure..", "..shared..")
            .because("Seules l'Infrastructure et Shared peuvent utiliser les annotations CDI");

    @ArchTest
    static final ArchRule rest_endpoints_in_correct_package =
        classes().that().areAnnotatedWith(Path.class)
            .should().resideInAPackage("..infrastructure.adapter.in.rest..")
            .because("Les endpoints REST doivent être dans infrastructure.adapter.in.rest");

    @ArchTest
    static final ArchRule schedulers_in_correct_package =
        methods().that().areAnnotatedWith(Scheduled.class)
            .should().beDeclaredInClassesThat()
            .resideInAPackage("..infrastructure.adapter.in.scheduler..")
            .because("Les méthodes @Scheduled doivent être dans infrastructure.adapter.in.scheduler");

    @ArchTest
    static final ArchRule healthchecks_in_correct_package =
        classes().that().implement(HealthCheck.class)
            .should().resideInAPackage("..infrastructure.health..")
            .because("Les HealthChecks doivent être dans infrastructure.health");

    @ArchTest
    static final ArchRule configs_in_correct_package =
        classes().that().areAnnotatedWith(ConfigMapping.class)
            .should().resideInAPackage("..infrastructure.config..")
            .because("Les configurations @ConfigMapping doivent être dans infrastructure.config");
}

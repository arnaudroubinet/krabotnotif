package arn.roub.krabot.architecture;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

@AnalyzeClasses(packages = "arn.roub.krabot", importOptions = com.tngtech.archunit.core.importer.ImportOption.DoNotIncludeTests.class)
public class NamingConventionsTest {

    // ══════════════════════════════════════════════════════════════
    // DOMAIN MODEL - Entités et Value Objects
    // ══════════════════════════════════════════════════════════════

    @ArchTest
    static final ArchRule records_should_be_in_domain_model =
        classes().that().areRecords()
            .and().resideInAPackage("arn.roub.krabot..")
            .and().resideOutsideOfPackage("..infrastructure..")
            .should().resideInAPackage("..domain.model..")
            .because("Les records (entités/value objects) doivent être dans domain.model");

    // ══════════════════════════════════════════════════════════════
    // PORTS - Interfaces du Domain
    // ══════════════════════════════════════════════════════════════

    @ArchTest
    static final ArchRule usecase_suffix_only_in_port_in =
        classes().that().haveSimpleNameEndingWith("UseCase")
            .should().resideInAPackage("..domain.port.in..")
            .because("Les classes *UseCase doivent être dans domain.port.in");

    @ArchTest
    static final ArchRule port_suffix_only_in_port_out =
        classes().that().haveSimpleNameEndingWith("Port")
            .should().resideInAPackage("..domain.port.out..")
            .because("Les classes *Port doivent être dans domain.port.out");

    // ══════════════════════════════════════════════════════════════
    // APPLICATION - Use Case Implementations
    // ══════════════════════════════════════════════════════════════

    @ArchTest
    static final ArchRule usecaseimpl_suffix_only_in_application_usecase =
        classes().that().haveSimpleNameEndingWith("UseCaseImpl")
            .should().resideInAPackage("..application.usecase..")
            .because("Les classes *UseCaseImpl doivent être dans application.usecase");

    @ArchTest
    static final ArchRule orchestrator_suffix_only_in_application_service =
        classes().that().haveSimpleNameEndingWith("Orchestrator")
            .should().resideInAPackage("..application.service..")
            .because("Les classes *Orchestrator doivent être dans application.service");

    // ══════════════════════════════════════════════════════════════
    // INFRASTRUCTURE ADAPTERS - Nommage par type
    // ══════════════════════════════════════════════════════════════

    @ArchTest
    static final ArchRule adapter_suffix_only_in_infrastructure_adapter =
        classes().that().haveSimpleNameEndingWith("Adapter")
            .should().resideInAPackage("..infrastructure.adapter..")
            .because("Les classes *Adapter doivent être dans infrastructure.adapter");

    @ArchTest
    static final ArchRule scheduler_suffix_only_in_scheduler_package =
        classes().that().haveSimpleNameEndingWith("Scheduler")
            .should().resideInAPackage("..infrastructure.adapter.in.scheduler..")
            .because("Les classes *Scheduler doivent être dans infrastructure.adapter.in.scheduler");

    @ArchTest
    static final ArchRule restadapter_suffix_only_in_rest_package =
        classes().that().haveSimpleNameEndingWith("RestAdapter")
            .should().resideInAPackage("..infrastructure.adapter.in.rest..")
            .because("Les classes *RestAdapter doivent être dans infrastructure.adapter.in.rest");

    @ArchTest
    static final ArchRule client_suffix_only_in_infrastructure =
        classes().that().haveSimpleNameEndingWith("Client")
            .should().resideInAPackage("..infrastructure.adapter.out..")
            .because("Les classes *Client doivent être dans infrastructure.adapter.out");

    @ArchTest
    static final ArchRule parser_suffix_only_in_infrastructure =
        classes().that().haveSimpleNameEndingWith("Parser")
            .should().resideInAPackage("..infrastructure.adapter.out..")
            .because("Les classes *Parser doivent être dans infrastructure.adapter.out");

    @ArchTest
    static final ArchRule repository_suffix_only_in_persistence =
        classes().that().haveSimpleNameEndingWith("Repository")
            .and().areNotInterfaces()
            .should().resideInAPackage("..infrastructure.adapter.out.persistence..")
            .because("Les classes *Repository (implémentations) doivent être dans infrastructure.adapter.out.persistence");

    // ══════════════════════════════════════════════════════════════
    // INFRASTRUCTURE CONFIG
    // ══════════════════════════════════════════════════════════════

    @ArchTest
    static final ArchRule config_suffix_only_in_config_package =
        classes().that().haveSimpleNameEndingWith("Config")
            .should().resideInAPackage("..infrastructure.config..")
            .because("Les classes *Config doivent être dans infrastructure.config");

    @ArchTest
    static final ArchRule configuration_suffix_only_in_config_package =
        classes().that().haveSimpleNameEndingWith("Configuration")
            .should().resideInAPackage("..infrastructure.config..")
            .because("Les classes *Configuration doivent être dans infrastructure.config");

    // ══════════════════════════════════════════════════════════════
    // INFRASTRUCTURE HEALTH
    // ══════════════════════════════════════════════════════════════

    @ArchTest
    static final ArchRule healthcheck_suffix_only_in_health_package =
        classes().that().haveSimpleNameEndingWith("HealthCheck")
            .should().resideInAPackage("..infrastructure.health..")
            .because("Les classes *HealthCheck doivent être dans infrastructure.health");

    // ══════════════════════════════════════════════════════════════
    // DOMAIN SERVICES
    // ══════════════════════════════════════════════════════════════

    @ArchTest
    static final ArchRule domainservice_suffix_only_in_domain_service =
        classes().that().haveSimpleNameEndingWith("DomainService")
            .should().resideInAPackage("..domain.service..")
            .because("Les classes *DomainService doivent être dans domain.service");

    // ══════════════════════════════════════════════════════════════
    // SHARED - Exceptions
    // ══════════════════════════════════════════════════════════════

    @ArchTest
    static final ArchRule exceptions_should_end_with_exception =
        classes().that().resideInAPackage("..exception..")
            .should().haveSimpleNameEndingWith("Exception")
            .because("Les classes dans les packages exception doivent avoir le suffixe Exception");

    @ArchTest
    static final ArchRule exception_classes_in_exception_package =
        classes().that().haveSimpleNameEndingWith("Exception")
            .and().areNotAnonymousClasses()
            .should().resideInAPackage("..exception..")
            .because("Les classes *Exception doivent être dans un package exception");

    // ══════════════════════════════════════════════════════════════
    // SHARED - Services Cross-Cutting
    // ══════════════════════════════════════════════════════════════

    @ArchTest
    static final ArchRule monitoringservice_in_shared =
        classes().that().haveSimpleNameEndingWith("MonitoringService")
            .should().resideInAPackage("..shared..")
            .because("Les classes *MonitoringService doivent être dans shared");
}

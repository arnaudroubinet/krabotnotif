package arn.roub.krabot.infrastructure.config;

import arn.roub.krabot.application.service.NotificationOrchestrator;
import arn.roub.krabot.application.usecase.CheckKramailsUseCaseImpl;
import arn.roub.krabot.application.usecase.CheckReleaseUseCaseImpl;
import arn.roub.krabot.application.usecase.CheckSleepUseCaseImpl;
import arn.roub.krabot.application.usecase.DelayKramailCheckUseCaseImpl;
import arn.roub.krabot.application.usecase.GetCurrentStateUseCaseImpl;
import arn.roub.krabot.application.usecase.UploadCharacteristicsUseCaseImpl;
import arn.roub.krabot.domain.model.Account;
import arn.roub.krabot.domain.port.in.ApplicationLifecycleUseCase;
import arn.roub.krabot.domain.port.in.CheckKramailsUseCase;
import arn.roub.krabot.domain.port.in.CheckReleaseUseCase;
import arn.roub.krabot.domain.port.in.CheckSleepUseCase;
import arn.roub.krabot.domain.port.in.DelayKramailCheckUseCase;
import arn.roub.krabot.domain.port.in.GetCurrentStateUseCase;
import arn.roub.krabot.domain.port.in.ReportErrorUseCase;
import arn.roub.krabot.domain.port.in.UploadCharacteristicsUseCase;
import arn.roub.krabot.domain.port.out.CharacteristicsPort;
import arn.roub.krabot.domain.port.out.GithubReleasePort;
import arn.roub.krabot.domain.port.out.KralandScrapingPort;
import arn.roub.krabot.domain.port.out.KramailCheckTimerPort;
import arn.roub.krabot.domain.port.out.NotificationPort;
import arn.roub.krabot.domain.port.out.StateRepositoryPort;
import arn.roub.krabot.domain.service.NotificationDomainService;
import arn.roub.krabot.infrastructure.adapter.out.notification.DiscordNotificationAdapter;
import arn.roub.krabot.infrastructure.adapter.out.notification.DiscordWebhookClient;
import arn.roub.krabot.infrastructure.adapter.out.persistence.CharacteristicsMemoryRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.enterprise.inject.Typed;
import jakarta.inject.Singleton;

/**
 * Composition root : assemble le domaine, l'application et les adapters.
 * <p>
 * Le domaine et l'application ne connaissent pas CDI ; c'est ici, et uniquement ici,
 * que les implémentations sont instanciées et reliées à leurs ports.
 */
@ApplicationScoped
public class BeanConfiguration {

    // ── Configuration ────────────────────────────────────────────

    @Produces
    @Singleton // un record est final : pas de proxy possible pour un scope normal
    public Account kralandAccount(KralandConfig kralandConfig) {
        return new Account(kralandConfig.user(), kralandConfig.password());
    }

    // ── Adapters sortants ────────────────────────────────────────

    @Produces
    @ApplicationScoped
    public DiscordWebhookClient discordWebhookClient(DiscordConfig discordConfig) {
        return new DiscordWebhookClient(
                discordConfig.url(),
                discordConfig.username(),
                discordConfig.avatarUrl()
        );
    }

    @Produces
    @ApplicationScoped
    public NotificationPort notificationPort(DiscordWebhookClient webhookClient, DiscordConfig discordConfig) {
        return new DiscordNotificationAdapter(
                webhookClient,
                discordConfig.firstMessage(),
                discordConfig.lastMessage(),
                discordConfig.messageKramail(),
                discordConfig.messageNotification(),
                discordConfig.release(),
                discordConfig.errorPrefixMessage()
        );
    }

    @Produces
    @ApplicationScoped
    public CharacteristicsPort characteristicsPort() {
        return new CharacteristicsMemoryRepository();
    }

    // ── Domaine ──────────────────────────────────────────────────

    @Produces
    @ApplicationScoped
    public NotificationDomainService notificationDomainService() {
        return new NotificationDomainService();
    }

    // ── Use cases (ports primaires) ──────────────────────────────

    /**
     * Expose l'orchestrateur sous ses ports primaires
     * ({@link ApplicationLifecycleUseCase} et {@link ReportErrorUseCase}).
     */
    @Produces
    @ApplicationScoped
    @Typed({ApplicationLifecycleUseCase.class, ReportErrorUseCase.class})
    public NotificationOrchestrator notificationOrchestrator(
            NotificationPort notificationPort,
            GithubReleasePort githubReleasePort,
            StateRepositoryPort stateRepositoryPort
    ) {
        return new NotificationOrchestrator(notificationPort, githubReleasePort, stateRepositoryPort);
    }

    @Produces
    @ApplicationScoped
    public CheckKramailsUseCase checkKramailsUseCase(
            KralandScrapingPort kralandScrapingPort,
            NotificationPort notificationPort,
            StateRepositoryPort stateRepositoryPort,
            NotificationDomainService notificationDomainService,
            Account account
    ) {
        return new CheckKramailsUseCaseImpl(
                kralandScrapingPort,
                notificationPort,
                stateRepositoryPort,
                notificationDomainService,
                account
        );
    }

    @Produces
    @ApplicationScoped
    public CheckReleaseUseCase checkReleaseUseCase(
            GithubReleasePort githubReleasePort,
            NotificationPort notificationPort,
            StateRepositoryPort stateRepositoryPort
    ) {
        return new CheckReleaseUseCaseImpl(githubReleasePort, notificationPort, stateRepositoryPort);
    }

    @Produces
    @ApplicationScoped
    public CheckSleepUseCase checkSleepUseCase(KralandScrapingPort kralandScrapingPort, Account account) {
        return new CheckSleepUseCaseImpl(kralandScrapingPort, account);
    }

    @Produces
    @ApplicationScoped
    public DelayKramailCheckUseCase delayKramailCheckUseCase(
            StateRepositoryPort stateRepositoryPort,
            KramailCheckTimerPort kramailCheckTimerPort
    ) {
        return new DelayKramailCheckUseCaseImpl(stateRepositoryPort, kramailCheckTimerPort);
    }

    @Produces
    @ApplicationScoped
    public GetCurrentStateUseCase getCurrentStateUseCase(StateRepositoryPort stateRepositoryPort) {
        return new GetCurrentStateUseCaseImpl(stateRepositoryPort);
    }

    @Produces
    @ApplicationScoped
    public UploadCharacteristicsUseCase uploadCharacteristicsUseCase(CharacteristicsPort characteristicsPort) {
        return new UploadCharacteristicsUseCaseImpl(characteristicsPort);
    }
}

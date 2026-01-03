package arn.roub.krabot.infrastructure.config;

import arn.roub.krabot.application.service.NotificationOrchestrator;
import arn.roub.krabot.application.usecase.CheckKramailsUseCaseImpl;
import arn.roub.krabot.application.usecase.CheckReleaseUseCaseImpl;
import arn.roub.krabot.application.usecase.GetCurrentStateUseCaseImpl;
import arn.roub.krabot.domain.model.Account;
import arn.roub.krabot.domain.port.in.CheckKramailsUseCase;
import arn.roub.krabot.domain.port.in.CheckReleaseUseCase;
import arn.roub.krabot.domain.port.in.GetCurrentStateUseCase;
import arn.roub.krabot.domain.port.out.GithubReleasePort;
import arn.roub.krabot.domain.port.out.KralandScrapingPort;
import arn.roub.krabot.domain.port.out.NotificationPort;
import arn.roub.krabot.domain.port.out.StateRepositoryPort;
import arn.roub.krabot.domain.service.NotificationDomainService;
import arn.roub.krabot.infrastructure.adapter.out.notification.DiscordNotificationAdapter;
import arn.roub.krabot.infrastructure.adapter.out.notification.DiscordWebhookClient;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

/**
 * Configuration CDI pour le wiring des beans.
 */
@ApplicationScoped
public class BeanConfiguration {

    private final DiscordConfig discordConfig;
    private final KralandConfig kralandConfig;
    private final KralandScrapingPort kralandScrapingPort;
    private final GithubReleasePort githubReleasePort;
    private final StateRepositoryPort stateRepositoryPort;

    private NotificationOrchestrator notificationOrchestrator;

    public BeanConfiguration(
            DiscordConfig discordConfig,
            KralandConfig kralandConfig,
            KralandScrapingPort kralandScrapingPort,
            GithubReleasePort githubReleasePort,
            StateRepositoryPort stateRepositoryPort
    ) {
        this.discordConfig = discordConfig;
        this.kralandConfig = kralandConfig;
        this.kralandScrapingPort = kralandScrapingPort;
        this.githubReleasePort = githubReleasePort;
        this.stateRepositoryPort = stateRepositoryPort;
    }

    @PostConstruct
    void initialize() {
        notificationOrchestrator().initialize();
    }

    @PreDestroy
    void shutdown() {
        if (notificationOrchestrator != null) {
            notificationOrchestrator.shutdown();
        }
    }

    @Produces
    @ApplicationScoped
    public DiscordWebhookClient discordWebhookClient() {
        return new DiscordWebhookClient(
                discordConfig.url(),
                discordConfig.username(),
                discordConfig.avatarUrl()
        );
    }

    @Produces
    @ApplicationScoped
    public NotificationPort notificationPort(DiscordWebhookClient webhookClient) {
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
    public NotificationDomainService notificationDomainService() {
        return new NotificationDomainService();
    }

    @Produces
    @ApplicationScoped
    public CheckKramailsUseCase checkKramailsUseCase(
            NotificationPort notificationPort,
            NotificationDomainService notificationDomainService
    ) {
        Account account = new Account(kralandConfig.user(), kralandConfig.password());
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
    public CheckReleaseUseCase checkReleaseUseCase(NotificationPort notificationPort) {
        return new CheckReleaseUseCaseImpl(
                githubReleasePort,
                notificationPort,
                stateRepositoryPort
        );
    }

    @Produces
    @ApplicationScoped
    public GetCurrentStateUseCase getCurrentStateUseCase() {
        return new GetCurrentStateUseCaseImpl(stateRepositoryPort);
    }

    @Produces
    @ApplicationScoped
    public NotificationOrchestrator notificationOrchestrator() {
        if (notificationOrchestrator == null) {
            NotificationPort notificationPort = notificationPort(discordWebhookClient());
            notificationOrchestrator = new NotificationOrchestrator(
                    notificationPort,
                    githubReleasePort,
                    stateRepositoryPort
            );
        }
        return notificationOrchestrator;
    }
}

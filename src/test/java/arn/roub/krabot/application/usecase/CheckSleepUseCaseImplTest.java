package arn.roub.krabot.application.usecase;

import arn.roub.krabot.domain.model.Account;
import arn.roub.krabot.domain.model.ScrapingResult;
import arn.roub.krabot.domain.port.out.KralandScrapingPort;
import arn.roub.krabot.shared.exception.ScrapingException;
import org.junit.jupiter.api.Test;

import java.util.ArrayDeque;
import java.util.Deque;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CheckSleepUseCaseImplTest {

    private static final Account ACCOUNT = new Account("user", "pass");

    @Test
    void execute_whenSleepAvailable_submitsOrderOnce() {
        FakeKralandScrapingPort port = new FakeKralandScrapingPort();
        port.results.add(() -> true);
        CheckSleepUseCaseImpl useCase = new CheckSleepUseCaseImpl(port, ACCOUNT);

        useCase.execute();

        assertEquals(1, port.callCount);
    }

    @Test
    void execute_whenSleepNotAvailable_doesNothingElse() {
        FakeKralandScrapingPort port = new FakeKralandScrapingPort();
        port.results.add(() -> false);
        CheckSleepUseCaseImpl useCase = new CheckSleepUseCaseImpl(port, ACCOUNT);

        useCase.execute();

        assertEquals(1, port.callCount);
    }

    @Test
    void execute_whenFailsTwiceThenSucceeds_retriesAndSucceeds() {
        FakeKralandScrapingPort port = new FakeKralandScrapingPort();
        port.results.add(() -> {
            throw new ScrapingException("boom");
        });
        port.results.add(() -> {
            throw new ScrapingException("boom again");
        });
        port.results.add(() -> true);
        CheckSleepUseCaseImpl useCase = new CheckSleepUseCaseImpl(port, ACCOUNT);

        useCase.execute();

        assertEquals(3, port.callCount);
    }

    @Test
    void execute_whenAlwaysFails_throwsAfterMaxRetries() {
        FakeKralandScrapingPort port = new FakeKralandScrapingPort();
        for (int i = 0; i < 5; i++) {
            port.results.add(() -> {
                throw new ScrapingException("boom");
            });
        }
        CheckSleepUseCaseImpl useCase = new CheckSleepUseCaseImpl(port, ACCOUNT);

        assertThrows(ScrapingException.class, useCase::execute);
        assertEquals(3, port.callCount);
    }

    private interface SleepResult {
        boolean get();
    }

    private static class FakeKralandScrapingPort implements KralandScrapingPort {

        private final Deque<SleepResult> results = new ArrayDeque<>();
        private int callCount = 0;

        @Override
        public ScrapingResult scrape(Account account) {
            throw new UnsupportedOperationException("not used by this test");
        }

        @Override
        public boolean sleepIfAvailable(Account account) {
            callCount++;
            return results.poll().get();
        }
    }
}

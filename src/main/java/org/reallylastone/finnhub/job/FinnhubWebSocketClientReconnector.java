package org.reallylastone.finnhub.job;

import org.reallylastone.finnhub.FinnhubWebSocketClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZonedDateTime;

public class FinnhubWebSocketClientReconnector implements Runnable {
    public static final int RECONNECT_ATTEMPT_INTERVAL_SECONDS = 20;
    private static final Logger log = LoggerFactory.getLogger(FinnhubWebSocketClientReconnector.class);
    private final FinnhubWebSocketClient client;
    private ZonedDateTime lastAttempt;

    public FinnhubWebSocketClientReconnector(FinnhubWebSocketClient client) {
        this.client = client;
    }

    @Override
    public void run() {
        if (client.isClosed() && (lastAttempt == null || lastAttempt.isBefore(ZonedDateTime.now().minusSeconds(RECONNECT_ATTEMPT_INTERVAL_SECONDS)))) {
            log.info("Finnhub client is closed, reconnecting");
            try {
                lastAttempt = ZonedDateTime.now();
                client.reconnectBlocking();
            } catch (InterruptedException e) {
                log.error("Error when trying to reconnect to a Finnhub server", e);
            }
        }
    }
}

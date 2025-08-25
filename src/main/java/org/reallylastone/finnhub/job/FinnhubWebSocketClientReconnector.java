package org.reallylastone.finnhub.job;

import org.reallylastone.finnhub.FinnhubWebSocketClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZonedDateTime;

public class FinnhubWebSocketClientReconnector implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(FinnhubWebSocketClientReconnector.class);
    private final FinnhubWebSocketClient client;
    private ZonedDateTime lastFail;

    public FinnhubWebSocketClientReconnector(FinnhubWebSocketClient client) {
        this.client = client;
    }

    @Override
    public void run() {
        if (client.isClosed() && (lastFail == null || lastFail.isBefore(ZonedDateTime.now().minusMinutes(1)))) {
            log.info("Finnhub client is closed, reconnecting");
            try {
                client.reconnectBlocking();
            } catch (InterruptedException e) {
                lastFail = ZonedDateTime.now();
                log.error("Error when trying to reconnect to a Finnhub server", e);
            }
        }
    }
}

package org.reallylastone.finnhub.job;

import org.reallylastone.finnhub.FinnhubWebSocketClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FinnhubWebSocketClientReconnector implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(FinnhubWebSocketClientReconnector.class);
    private final FinnhubWebSocketClient client;

    public FinnhubWebSocketClientReconnector(FinnhubWebSocketClient client) {
        this.client = client;
    }

    @Override
    public void run() {
        if (client.isClosed()) {
            log.info("Finnhub client is closed, reconnecting");
            try {
                client.reconnectBlocking();
            } catch (InterruptedException e) {
                log.error("Error when trying to reconnect to a Finnhub server", e);
            }
        }
    }
}

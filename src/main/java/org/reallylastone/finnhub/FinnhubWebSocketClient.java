package org.reallylastone.finnhub;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.reallylastone.trade.domain.Trade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.List;
import java.util.concurrent.BlockingQueue;

public class FinnhubWebSocketClient extends WebSocketClient {
    public static final List<String> STOCKS = List.of("AAPL", "BINANCE:BTCUSDT");
    private static final Logger log = LoggerFactory.getLogger(FinnhubWebSocketClient.class);
    private final ObjectMapper mapper = new ObjectMapper();
    private final BlockingQueue<Trade> messageQueue;

    public FinnhubWebSocketClient(URI serverUri, BlockingQueue<Trade> messageQueue) {
        super(serverUri);
        this.messageQueue = messageQueue;
        setConnectionLostTimeout(-1);
    }

    @Override
    public void onOpen(ServerHandshake handshake) {
        log.info("Connected to Finnhub server");
        STOCKS.forEach(stock -> send("{\"type\":\"subscribe\",\"symbol\":\"" + stock + "\"}"));
        log.info("Subscribed to {} stocks", STOCKS);
    }

    @Override
    public void onMessage(String message) {
        try {
            FinnhubMessageEvent event = mapper.readValue(message, FinnhubMessageEvent.class);
            if (event.type().equals("ping")) return;
            for (Trade e : event.data()) {
                messageQueue.put(e);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        log.info("Connection to Finnhub server closed for reason {}, code {}, remote {}", reason, code, remote);
    }

    @Override
    public void onError(Exception ex) {
        ex.printStackTrace();
    }
}

package org.reallylastone.finnhub;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.reallylastone.trade.domain.Trade;

import java.net.URI;
import java.util.concurrent.BlockingQueue;

public class FinnhubWebSocketClient extends WebSocketClient {
    private final ObjectMapper mapper = new ObjectMapper();
    private final BlockingQueue<Trade> messageQueue;

    public FinnhubWebSocketClient(URI serverUri, BlockingQueue<Trade> messageQueue) {
        super(serverUri);
        this.messageQueue = messageQueue;
        setConnectionLostTimeout(-1);
    }

    @Override
    public void onOpen(ServerHandshake handshake) {
        System.out.println("Connected to server");
        send("{\"type\":\"subscribe\",\"symbol\":\"AAPL\"}");
        send("{\"type\":\"subscribe\",\"symbol\":\"BINANCE:BTCUSDT\"}");
    }

    @Override
    public void onMessage(String message) {
        try {
            FinnhubMessageEvent event = mapper.readValue(message, FinnhubMessageEvent.class);
            if (event.type().equals("ping")) return;
            boolean changed = messageQueue.addAll(event.data());
            if (!changed) {
                System.out.println("Trades queue not changed!");
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        System.out.println("Connection closed: " + reason);
    }

    @Override
    public void onError(Exception ex) {
        ex.printStackTrace();
    }
}

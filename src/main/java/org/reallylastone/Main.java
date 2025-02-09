package org.reallylastone;

import org.reallylastone.common.ConnectionPool;
import org.reallylastone.common.Registry;
import org.reallylastone.finnhub.FinnhubWebSocketClient;
import org.reallylastone.leadership.gateway.LeadershipGateway;
import org.reallylastone.leadership.jobs.AcquireLeadershipJob;
import org.reallylastone.statistics.gateway.TradeStatisticsGateway;
import org.reallylastone.statistics.jobs.CalculateStatisticsJob;
import org.reallylastone.trade.domain.Trade;
import org.reallylastone.trade.gateway.TradeGateway;
import org.reallylastone.trade.jobs.TradeWriter;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class Main {
    private static final String FINNHUB_URL = "wss://ws.finnhub.io?token=";
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(7);
    private static final Logger logger = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) throws Exception {
        String finnhubToken = System.getProperty("finnhub.token", null);
        Objects.requireNonNull(finnhubToken, "finnhub token is required");

        Properties properties = loadProperties();
        String url = (String) properties.get("datasource.url");
        String username = (String) properties.get("datasource.username");
        String password = (String) properties.get("datasource.password");

        Objects.requireNonNull(url, "datasource.url cannot be null");
        Objects.requireNonNull(username, "datasource.username cannot be null");
        Objects.requireNonNull(password, "datasource.password cannot be null");

        Registry.setProperties(properties);
        Registry.setConnectionPool(new ConnectionPool(url, username, password, 7));
        Registry.setLeadershipGateway(new LeadershipGateway(Registry.getConnectionPool().getConnection()));
        Registry.setTradeGateway(new TradeGateway(Registry.getConnectionPool().getConnection()));
        Registry.setTradeStatisticsGateway(new TradeStatisticsGateway(Registry.getConnectionPool().getConnection()));

        scheduler.scheduleAtFixedRate(new AcquireLeadershipJob(Registry.getLeadershipGateway()), 0, 15, TimeUnit.SECONDS);
        scheduler.scheduleAtFixedRate(new CalculateStatisticsJob(Registry.getTradeStatisticsGateway(), Registry.getTradeGateway()), 0, 1, TimeUnit.MINUTES);

        BlockingQueue<Trade> queue = new ArrayBlockingQueue<>(10000);
        TradeWriter writer = new TradeWriter(queue, Boolean.parseBoolean((String) properties.get("slowMode.enabled")), Registry.getTradeGateway());
        FinnhubWebSocketClient client = new FinnhubWebSocketClient(new URI(FINNHUB_URL + finnhubToken), queue);

        client.connect();
        writer.run();

        Thread.currentThread().join();
    }

    private static Properties loadProperties() throws IOException {
        Properties properties = new Properties();

        try (InputStream baseInput = Main.class.getClassLoader().getResourceAsStream("application.properties")) {
            if (baseInput != null) properties.load(baseInput);
        }
        return properties;
    }

}
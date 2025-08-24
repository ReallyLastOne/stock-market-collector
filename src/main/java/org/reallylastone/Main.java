package org.reallylastone;

import org.reallylastone.common.ConnectionPool;
import org.reallylastone.common.Registry;
import org.reallylastone.finnhub.FinnhubWebSocketClient;
import org.reallylastone.finnhub.job.FinnhubWebSocketClientReconnector;
import org.reallylastone.leadership.gateway.LeadershipGateway;
import org.reallylastone.leadership.jobs.AcquireLeadershipJob;
import org.reallylastone.statistics.gateway.TradeStatisticsGateway;
import org.reallylastone.statistics.jobs.CalculateStatisticsJob;
import org.reallylastone.trade.gateway.TradeGateway;
import org.reallylastone.trade.jobs.TradeWriter;
import org.reallylastone.trade.jobs.TradeWriterJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Main {
    private static final String FINNHUB_URL = "wss://ws.finnhub.io?token=";
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(7);
    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws Exception {
        String url = "jdbc:postgresql://stock-market-collector-db:5432/stock-collector";
        String finnhubToken = System.getenv("FINNHUB_TOKEN");
        String username = System.getenv("STOCK_COLLECTOR_POSTGRES_USER");
        String password = System.getenv("STOCK_COLLECTOR_POSTGRES_PASSWORD");

        log.info("Connecting to {} with user {}", url, username);

        Objects.requireNonNull(finnhubToken, "finnhub token is required");
        Objects.requireNonNull(username, "datasource.username cannot be null");
        Objects.requireNonNull(password, "datasource.password cannot be null");

        initializeRegistry(url, username, password, finnhubToken);

        log.info("Process ID {}", ProcessHandle.current().pid());

        Registry.getFinnhubWebSocketClient().connect();
        scheduler.scheduleAtFixedRate(new AcquireLeadershipJob(Registry.getLeadershipGateway()), 0, 15, TimeUnit.SECONDS);
        scheduler.scheduleAtFixedRate(new CalculateStatisticsJob(Registry.getTradeStatisticsGateway(), Registry.getTradeGateway(), Registry.getLeadershipGateway()), 0, 1, TimeUnit.MINUTES);
        scheduler.scheduleAtFixedRate(new FinnhubWebSocketClientReconnector(Registry.getFinnhubWebSocketClient()), 0, 1, TimeUnit.SECONDS);

        TradeWriterJob writerJob = new TradeWriterJob(new TradeWriter(Registry.getQueue(), false, Registry.getTradeGateway()), Registry.getLeadershipGateway());
        writerJob.run();

        Thread.currentThread().join();
    }

    private static void initializeRegistry(String url, String username, String password, String finnhubToken) throws SQLException, InterruptedException {
        Registry.setConnectionPool(new ConnectionPool(url, username, password, 7));
        Registry.setLeadershipGateway(new LeadershipGateway(Registry.getConnectionPool().getConnection()));
        Registry.setTradeGateway(new TradeGateway(Registry.getConnectionPool().getConnection()));
        Registry.setTradeStatisticsGateway(new TradeStatisticsGateway(Registry.getConnectionPool().getConnection()));
        Registry.setQueue(new ArrayBlockingQueue<>(10_000_000));
        Registry.setFinnhubWebSocketClient(new FinnhubWebSocketClient(URI.create(FINNHUB_URL + finnhubToken), Registry.getQueue()));
    }
}
package org.reallylastone.common;

import org.reallylastone.finnhub.FinnhubWebSocketClient;
import org.reallylastone.leadership.gateway.LeadershipGateway;
import org.reallylastone.statistics.gateway.TradeStatisticsGateway;
import org.reallylastone.trade.domain.Trade;
import org.reallylastone.trade.gateway.TradeGateway;

import java.util.concurrent.BlockingQueue;

public class Registry {
    private static ConnectionPool connectionPool;
    private static LeadershipGateway leadershipGateway;
    private static TradeGateway tradeGateway;
    private static TradeStatisticsGateway tradeStatisticsGateway;
    private static FinnhubWebSocketClient finnhubWebSocketClient;
    private static BlockingQueue<Trade> queue;

    public static ConnectionPool getConnectionPool() {
        return connectionPool;
    }

    public static void setConnectionPool(ConnectionPool connectionPool) {
        Registry.connectionPool = connectionPool;
    }

    public static LeadershipGateway getLeadershipGateway() {
        return leadershipGateway;
    }

    public static void setLeadershipGateway(LeadershipGateway leadershipGateway) {
        Registry.leadershipGateway = leadershipGateway;
    }

    public static TradeGateway getTradeGateway() {
        return tradeGateway;
    }

    public static void setTradeGateway(TradeGateway tradeGateway) {
        Registry.tradeGateway = tradeGateway;
    }

    public static TradeStatisticsGateway getTradeStatisticsGateway() {
        return tradeStatisticsGateway;
    }

    public static void setTradeStatisticsGateway(TradeStatisticsGateway tradeStatisticsGateway) {
        Registry.tradeStatisticsGateway = tradeStatisticsGateway;
    }

    public static FinnhubWebSocketClient getFinnhubWebSocketClient() {
        return finnhubWebSocketClient;
    }

    public static void setFinnhubWebSocketClient(FinnhubWebSocketClient finnhubWebSocketClient) {
        Registry.finnhubWebSocketClient = finnhubWebSocketClient;
    }

    public static BlockingQueue<Trade> getQueue() {
        return queue;
    }

    public static void setQueue(BlockingQueue<Trade> queue) {
        Registry.queue = queue;
    }
}

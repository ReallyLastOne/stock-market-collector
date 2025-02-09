package org.reallylastone.statistics.gateway;


import org.reallylastone.statistics.domain.TradeStatistics;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class TradeStatisticsGateway {
    private static final String INSERT_TRADE_STATS_SQL = "INSERT INTO TRADE_STATISTICS (start_timestamp," + " end_timestamp, volume, max_price, min_price, avg_price, open_price, close_price, symbol) " + "values (?, ?, ?, ?, ?, ?, ?, ?, ?) ON CONFLICT (start_timestamp, end_timestamp) do update " + "set volume = excluded.volume, max_price = excluded.max_price, min_price = excluded.min_price," + "avg_price = excluded.avg_price, open_price = excluded.open_price, close_price = excluded.close_price";
    private final Connection connection;

    public TradeStatisticsGateway(Connection connection) {
        this.connection = connection;
    }

    public void insertTradeStatistics(TradeStatistics tradeStatistics) {
        try (PreparedStatement stmt = connection.prepareStatement(INSERT_TRADE_STATS_SQL)) {
            stmt.setObject(1, tradeStatistics.getStartTimestamp().toOffsetDateTime());
            stmt.setObject(2, tradeStatistics.getEndTimestamp().toOffsetDateTime());
            stmt.setBigDecimal(3, tradeStatistics.getVolume());
            stmt.setBigDecimal(4, tradeStatistics.getMaxPrice());
            stmt.setBigDecimal(5, tradeStatistics.getMinPrice());
            stmt.setBigDecimal(6, tradeStatistics.getAvgPrice());
            stmt.setBigDecimal(7, tradeStatistics.getOpenPrice());
            stmt.setBigDecimal(8, tradeStatistics.getClosePrice());
            stmt.setString(9, tradeStatistics.getSymbol());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

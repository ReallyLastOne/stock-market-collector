package org.reallylastone.statistics.gateway;


import org.reallylastone.Main;
import org.reallylastone.statistics.domain.TradeStatistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class TradeStatisticsGateway {
    private static final Logger log = LoggerFactory.getLogger(TradeStatisticsGateway.class);
    //@formatter:off
    private static final String INSERT_TRADE_STATS_SQL = "INSERT INTO TRADE_STATISTICS (start_timestamp,"
            + " end_timestamp, volume, max_price, min_price, avg_price, open_price, close_price, symbol) "
            + "values (?, ?, ?, ?, ?, ?, ?, ?, ?) ON CONFLICT (start_timestamp, end_timestamp, symbol) do update "
            + "set volume = excluded.volume, max_price = excluded.max_price, min_price = excluded.min_price,"
            + "avg_price = excluded.avg_price, open_price = excluded.open_price, close_price = excluded.close_price";
    //@formatter:on

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
            log.error("Exception on inserting trade statistics");
        }
    }
}

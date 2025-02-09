package org.reallylastone.trade.gateway;

import org.reallylastone.trade.domain.Trade;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

public class TradeGateway {
    private static final String INSERT_SQL = "INSERT INTO finnhub_trades (c, p, s, t, v) VALUES (?::jsonb, ?, ?, ?, ?)";
    private static final String SELECT_TRADES_SQL = "SELECT * FROM finnhub_trades WHERE t >= ? AND t <= ?";
    private final Connection connection;

    public TradeGateway(Connection connection) {
        this.connection = connection;
    }

    public void insertTrade(Trade trade) {
        try (PreparedStatement stmt = connection.prepareStatement(INSERT_SQL)) {
            stmt.setString(1, String.valueOf(trade.c()));
            stmt.setBigDecimal(2, trade.p());
            stmt.setString(3, trade.s());
            stmt.setLong(4, trade.t());
            stmt.setBigDecimal(5, trade.v());
            stmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<Trade> getTradesBetween(ZonedDateTime start, ZonedDateTime end) {
        return getTradesBetween(start.toEpochSecond() * 1000, end.toEpochSecond() * 1000);
    }

    public List<Trade> getTradesBetween(long start, long end) {
        List<Trade> trades = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(SELECT_TRADES_SQL)) {
            stmt.setLong(1, start);
            stmt.setLong(2, end);
            ResultSet resultSet = stmt.executeQuery();
            while (resultSet.next()) {
                Trade trade = new Trade(resultSet.getObject("c"), resultSet.getBigDecimal("p"), resultSet.getString("s"), resultSet.getLong("t"), resultSet.getBigDecimal("v"));
                trades.add(trade);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return trades;
    }

}
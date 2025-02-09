package org.reallylastone.statistics.domain;

import org.reallylastone.trade.domain.Trade;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.List;


public class TradeStatistics {
    private final BigDecimal maxPrice;
    private final BigDecimal minPrice;
    private final BigDecimal avgPrice;
    private BigDecimal openPrice;
    private BigDecimal closePrice;
    private final ZonedDateTime startTimestamp;
    private final ZonedDateTime endTimestamp;
    private final BigDecimal volume;
    private final String symbol;

    public TradeStatistics(List<Trade> trades, ZonedDateTime startTimestamp, ZonedDateTime endTimestamp, String symbol) {
        List<Trade> mismatched = trades.stream()
                .filter(trade -> {
                    ZonedDateTime tradeTimestamp = ZonedDateTime.ofInstant(Instant.ofEpochSecond(trade.t()), ZoneId.systemDefault());
                    return tradeTimestamp.isBefore(startTimestamp) || tradeTimestamp.isAfter(endTimestamp);
                })
                .toList();

        if (mismatched.size() != trades.size()) {
            System.out.printf("%d trades mismatch the interval [%s, %s]%n", mismatched.size(), startTimestamp, endTimestamp);
        }
        if (!trades.isEmpty()) {
            this.openPrice = trades.getFirst().p();
            this.closePrice = trades.getLast().p();
        }
        this.symbol = symbol;
        this.startTimestamp = startTimestamp;
        this.endTimestamp = endTimestamp;
        this.volume = trades.stream().map(Trade::v).reduce(BigDecimal.ZERO, BigDecimal::add);
        this.avgPrice = trades.stream().map(Trade::p).reduce(BigDecimal.ZERO, BigDecimal::add).divide(volume, RoundingMode.HALF_UP);
        this.maxPrice = trades.stream().map(Trade::p).max(Comparator.naturalOrder()).orElse(BigDecimal.valueOf(0));
        this.minPrice = trades.stream().map(Trade::p).min(Comparator.naturalOrder()).orElse(BigDecimal.valueOf(0));
    }

    public BigDecimal getMaxPrice() {
        return maxPrice;
    }

    public BigDecimal getMinPrice() {
        return minPrice;
    }

    public BigDecimal getAvgPrice() {
        return avgPrice;
    }

    public BigDecimal getOpenPrice() {
        return openPrice;
    }

    public BigDecimal getClosePrice() {
        return closePrice;
    }

    public ZonedDateTime getStartTimestamp() {
        return startTimestamp;
    }

    public ZonedDateTime getEndTimestamp() {
        return endTimestamp;
    }

    public BigDecimal getVolume() {
        return volume;
    }

    @Override
    public String toString() {
        return "TradeStatistics{" +
                "maxPrice=" + maxPrice +
                ", minPrice=" + minPrice +
                ", avgPrice=" + avgPrice +
                ", openPrice=" + openPrice +
                ", closePrice=" + closePrice +
                ", startTimestamp=" + startTimestamp +
                ", endTimestamp=" + endTimestamp +
                ", volume=" + volume +
                '}';
    }

    public String getSymbol() {
        return symbol;
    }
}

package org.reallylastone.statistics.jobs;

import org.reallylastone.statistics.domain.TradeStatistics;
import org.reallylastone.statistics.gateway.TradeStatisticsGateway;
import org.reallylastone.trade.domain.Trade;
import org.reallylastone.trade.gateway.TradeGateway;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CalculateStatisticsJob implements Runnable {
    private final TradeStatisticsGateway tradeStatisticsGateway;
    private final TradeGateway tradeGateway;

    public CalculateStatisticsJob(TradeStatisticsGateway tradeStatisticsGateway, TradeGateway tradeGateway) {
        this.tradeStatisticsGateway = tradeStatisticsGateway;
        this.tradeGateway = tradeGateway;
    }

    @Override
    public void run() {
        ZonedDateTime start = ZonedDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        ZonedDateTime end = start.plusMinutes(1);

        List<Trade> tradeCurrentMinute = tradeGateway.getTradesBetween(start, end);
        calculateStatistics(start, start.plusMinutes(1), tradeCurrentMinute);
        List<Trade> tradeMinuteBefore = tradeGateway.getTradesBetween(start.minusMinutes(1), start);
        calculateStatistics(start.minusMinutes(1), start, tradeMinuteBefore);
    }

    private void calculateStatistics(ZonedDateTime start, ZonedDateTime end, List<Trade> trades) {
        Map<String, List<Trade>> symbolTrades = new HashMap<>();
        trades.forEach(trade -> symbolTrades.computeIfAbsent(trade.s(), _ -> new ArrayList<>()).add(trade));
        symbolTrades.forEach((k, l) -> {
            TradeStatistics tradeStatistics = new TradeStatistics(l, start, end, k);
            tradeStatisticsGateway.insertTradeStatistics(tradeStatistics);
        });
    }

}

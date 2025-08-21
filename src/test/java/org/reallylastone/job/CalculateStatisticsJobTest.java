package org.reallylastone.job;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.reallylastone.leadership.gateway.LeadershipGateway;
import org.reallylastone.statistics.domain.TradeStatistics;
import org.reallylastone.statistics.gateway.TradeStatisticsGateway;
import org.reallylastone.statistics.jobs.CalculateStatisticsJob;
import org.reallylastone.trade.domain.Trade;
import org.reallylastone.trade.gateway.TradeGateway;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

public class CalculateStatisticsJobTest {

    private TradeGateway tradeGateway;
    private TradeStatisticsGateway statsGateway;
    private CalculateStatisticsJob job;
    private LeadershipGateway leadershipGateway;

    @BeforeEach
    void setup() {
        // given
        tradeGateway = mock(TradeGateway.class);
        statsGateway = mock(TradeStatisticsGateway.class);
        leadershipGateway = mock(LeadershipGateway.class);
        job = new CalculateStatisticsJob(statsGateway, tradeGateway, leadershipGateway);
        when(leadershipGateway.amILeader()).thenReturn(true);
    }

    private List<Trade> generateTrades(List<String> symbols, int tradesPerSymbol, ZonedDateTime timestamp) {
        List<Trade> trades = new ArrayList<>();
        int counter = 0;
        for (String symbol : symbols) {
            for (int i = 0; i < tradesPerSymbol; i++) {
                trades.add(new Trade("id" + counter++, BigDecimal.valueOf(50 + i * 10L), symbol, timestamp.toEpochSecond() * 1000, BigDecimal.valueOf(1 + i)));
            }
        }
        return trades;
    }

    @Test
    void testMultipleSymbolsMultipleTrades() {
        // given
        ZonedDateTime now = ZonedDateTime.now();
        List<String> symbols = List.of("AAPL", "MSFT", "GOOGL", "AMZN", "TSLA");
        List<Trade> tradesCurrent = generateTrades(symbols, 50, now);
        List<Trade> tradesPrevious = generateTrades(symbols, 30, now.minusMinutes(1));
        when(tradeGateway.getTradesBetween(any(), any())).thenReturn(tradesCurrent).thenReturn(tradesPrevious);

        // when
        job.run();

        // then
        ArgumentCaptor<TradeStatistics> captor = ArgumentCaptor.forClass(TradeStatistics.class);
        verify(statsGateway, atLeast(symbols.size())).insertTradeStatistics(captor.capture());
        List<TradeStatistics> inserted = captor.getAllValues();
        symbols.forEach(symbol -> {
            TradeStatistics stat = inserted.stream().filter(s -> s.getSymbol().equals(symbol)).findFirst().orElseThrow();
            assertTrue(stat.getMaxPrice().compareTo(stat.getMinPrice()) >= 0);
            assertEquals(symbol, stat.getSymbol());
            assertTrue(stat.getVolume().compareTo(BigDecimal.ZERO) > 0);
        });
    }

    @Test
    void testSingleSymbolManyTrades() {
        // given
        ZonedDateTime now = ZonedDateTime.now();
        List<Trade> trades = generateTrades(List.of("AAPL"), 50, now);
        when(tradeGateway.getTradesBetween(any(), any())).thenReturn(trades).thenReturn(List.of());

        // when
        job.run();

        // then
        ArgumentCaptor<TradeStatistics> captor = ArgumentCaptor.forClass(TradeStatistics.class);
        verify(statsGateway, atLeastOnce()).insertTradeStatistics(captor.capture());
        TradeStatistics stat = captor.getValue();
        assertEquals("AAPL", stat.getSymbol());
        assertEquals(BigDecimal.valueOf(50), stat.getOpenPrice());
        assertEquals(BigDecimal.valueOf(540), stat.getClosePrice());
        // sum of arithmetic sequence divided by number of elements
        assertEquals(BigDecimal.valueOf(((50 + 540) / 2 * 50) / 50), stat.getAvgPrice());
        assertEquals(BigDecimal.valueOf(50 * 51 / 2), stat.getVolume());
    }

    @Test
    void testNonIntegralVolume() {
        // given
        ZonedDateTime now = ZonedDateTime.now();
        List<Trade> trades = generateTrades(List.of("AAPL"), 50, now);
        when(tradeGateway.getTradesBetween(any(), any())).thenReturn(trades.stream().map(e -> new Trade(e.c(), e.p(), e.s(), e.t(), BigDecimal.valueOf(0.5))).toList()).thenReturn(List.of());

        // when
        job.run();

        // then
        ArgumentCaptor<TradeStatistics> captor = ArgumentCaptor.forClass(TradeStatistics.class);
        verify(statsGateway, atLeastOnce()).insertTradeStatistics(captor.capture());
        TradeStatistics stat = captor.getValue();
        assertEquals("AAPL", stat.getSymbol());
        assertEquals(BigDecimal.valueOf(50), stat.getOpenPrice());
        assertEquals(BigDecimal.valueOf(540), stat.getClosePrice());
        // sum of arithmetic sequence divided by number of elements
        assertEquals(BigDecimal.valueOf(((50 + 540) / 2 * 50) / 50), stat.getAvgPrice());
        assertEquals(BigDecimal.valueOf(25.0), stat.getVolume());
    }

    @Test
    void testNoTrades() {
        // given
        when(tradeGateway.getTradesBetween(any(), any())).thenReturn(List.of()).thenReturn(List.of());

        // when
        job.run();

        // then
        verify(statsGateway, never()).insertTradeStatistics(any());
    }

    @Test
    void testTradesOutsideInterval() {
        // given
        ZonedDateTime now = ZonedDateTime.now();
        Trade inInterval = new Trade("id0", BigDecimal.valueOf(100), "AAPL", now.toEpochSecond() * 1000, BigDecimal.ONE);
        Trade outInterval = new Trade("id1", BigDecimal.valueOf(200), "AAPL", now.minusMinutes(5).toEpochSecond() * 1000, BigDecimal.ONE);
        when(tradeGateway.getTradesBetween(any(), any())).thenReturn(List.of(inInterval, outInterval)).thenReturn(List.of());

        // when
        job.run();

        // then
        ArgumentCaptor<TradeStatistics> captor = ArgumentCaptor.forClass(TradeStatistics.class);
        verify(statsGateway, atLeastOnce()).insertTradeStatistics(captor.capture());
        TradeStatistics stat = captor.getValue();
        assertEquals("AAPL", stat.getSymbol());
        assertEquals(BigDecimal.valueOf(100), stat.getOpenPrice());
        assertEquals(BigDecimal.valueOf(100), stat.getClosePrice());
        assertEquals(BigDecimal.valueOf(100), stat.getMaxPrice());
        assertEquals(BigDecimal.valueOf(100), stat.getMinPrice());
        assertEquals(BigDecimal.ONE, stat.getVolume());
    }
}

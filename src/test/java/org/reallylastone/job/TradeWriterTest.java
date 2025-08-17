package org.reallylastone.job;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.reallylastone.trade.domain.Trade;
import org.reallylastone.trade.gateway.TradeGateway;
import org.reallylastone.trade.jobs.TradeWriter;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class TradeWriterTest {

    private TradeGateway tradeGateway;
    private BlockingQueue<Trade> queue;
    private TradeWriter writer;

    @BeforeEach
    void setup() {
        tradeGateway = mock(TradeGateway.class);
        queue = new ArrayBlockingQueue<>(50);
        writer = new TradeWriter(queue, false, tradeGateway);
    }

    @Test
    void testMultipleTradesSingleRun() throws InterruptedException {
        // given
        for (int i = 0; i < 10; i++) {
            queue.put(new Trade("c" + i, BigDecimal.valueOf(10 + i), "AAPL", System.currentTimeMillis() / 1000, BigDecimal.ONE));
        }

        // when
        writer.run();

        // then
        ArgumentCaptor<List<Trade>> captor = ArgumentCaptor.forClass(List.class);
        verify(tradeGateway, atLeastOnce()).insertTrades(captor.capture());
        List<Trade> inserted = captor.getValue();
        assertEquals(10, inserted.size());
        assertEquals(10, writer.getTotalInserted());
    }

    @Test
    void testMultipleSymbols() throws InterruptedException {
        // given
        for (int i = 0; i < 5; i++)
            queue.put(new Trade("c" + i, BigDecimal.TEN, "AAPL", System.currentTimeMillis() / 1000, BigDecimal.ONE));
        for (int i = 0; i < 5; i++)
            queue.put(new Trade("c" + i, BigDecimal.TEN, "GOOG", System.currentTimeMillis() / 1000, BigDecimal.ONE));

        // when
        writer.run();

        // then
        ArgumentCaptor<List<Trade>> captor = ArgumentCaptor.forClass(List.class);
        verify(tradeGateway, atLeastOnce()).insertTrades(captor.capture());
        List<Trade> inserted = captor.getValue();
        assertEquals(10, inserted.size());
        assertEquals(10, writer.getTotalInserted());
    }

    @Test
    void testConcurrentAccess() throws InterruptedException {
        // given
        ExecutorService executor = Executors.newFixedThreadPool(2);
        for (int i = 0; i < 20; i++)
            queue.put(new Trade("c" + i, BigDecimal.ONE, "AAPL", System.currentTimeMillis() / 1000, BigDecimal.ONE));

        // when
        executor.submit(() -> writer.run());
        executor.shutdown();
        while (!executor.isTerminated()) Thread.sleep(50);

        // then
        assertEquals(20, writer.getTotalInserted());
    }
}

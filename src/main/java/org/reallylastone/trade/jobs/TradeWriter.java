package org.reallylastone.trade.jobs;

import org.reallylastone.trade.domain.Trade;
import org.reallylastone.trade.gateway.TradeGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class TradeWriter implements Runnable {
    public static final double THRESHOLD = 0.05;
    private static final Logger log = LoggerFactory.getLogger(TradeWriter.class);
    private final BlockingQueue<Trade> queue;
    private final boolean slowMode;
    private final TradeGateway tradeGateway;
    private final AtomicLong totalInserted = new AtomicLong(0);
    private long lastLogTime = System.currentTimeMillis();

    public TradeWriter(BlockingQueue<Trade> queue, boolean slowMode, TradeGateway tradeGateway) {
        this.queue = queue;
        this.slowMode = slowMode;
        this.tradeGateway = tradeGateway;
    }

    @Override
    public void run() {
        List<Trade> tradesToInsert = new ArrayList<>();
        Trade trade;
        while ((trade = queue.poll()) != null) {
            if (!slowMode || Math.random() <= THRESHOLD) {
                tradesToInsert.add(trade);
            }
        }

        if (!tradesToInsert.isEmpty()) {
            tradeGateway.insertTrades(tradesToInsert);
            totalInserted.addAndGet(tradesToInsert.size());
        }

        if (System.currentTimeMillis() - lastLogTime >= TimeUnit.HOURS.toMillis(1)) {
            log.info("Total inserted trades {}", totalInserted.get());
            lastLogTime = System.currentTimeMillis();
        }
    }

    public long getTotalInserted() {
        return totalInserted.get();
    }
}

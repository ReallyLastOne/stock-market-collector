package org.reallylastone.trade.jobs;

import org.reallylastone.trade.domain.Trade;
import org.reallylastone.trade.gateway.TradeGateway;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class TradeWriter implements Runnable {
    public static final double THRESHOLD = 0.05;
    public static final int INSERT_INTERVAL_MILLIS = 5000;
    private final BlockingQueue<Trade> queue;
    private final boolean slowMode;
    private final TradeGateway tradeGateway;

    public TradeWriter(BlockingQueue<Trade> queue, boolean slowMode, TradeGateway tradeGateway) {
        this.queue = queue;
        this.slowMode = slowMode;
        this.tradeGateway = tradeGateway;
    }

    @Override
    public void run() {
        List<Trade> tradesToInsert = new ArrayList<>();

        while (true) {
            try {
                long startTime = System.currentTimeMillis();

                while (System.currentTimeMillis() - startTime < INSERT_INTERVAL_MILLIS) {
                    Trade trade = queue.poll(100, TimeUnit.MILLISECONDS);
                    if (trade != null && (!slowMode || Math.random() <= THRESHOLD)) {
                        tradesToInsert.add(trade);
                    }
                }

                if (!tradesToInsert.isEmpty()) {
                    tradeGateway.insertTrades(tradesToInsert);
                    tradesToInsert.clear();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}


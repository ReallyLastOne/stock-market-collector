package org.reallylastone.trade.jobs;

import org.reallylastone.trade.domain.Trade;
import org.reallylastone.trade.gateway.TradeGateway;

import java.util.concurrent.BlockingQueue;

public class TradeWriter implements Runnable {
    public static final double THRESHOLD = 0.05;
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
        try {
            while (true) {
                Trade trade = queue.take();
                if (!slowMode || Math.random() <= THRESHOLD) {
                    tradeGateway.insertTrade(trade);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}


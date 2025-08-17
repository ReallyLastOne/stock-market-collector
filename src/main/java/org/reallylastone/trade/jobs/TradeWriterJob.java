package org.reallylastone.trade.jobs;

public class TradeWriterJob implements Runnable {
    private static final int INSERT_INTERVAL_MILLIS = 5000;
    private final TradeWriter writer;

    public TradeWriterJob(TradeWriter writer) {
        this.writer = writer;
    }

    @Override
    public void run() {
        while (true) {
            writer.run();
            try {
                Thread.sleep(INSERT_INTERVAL_MILLIS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
}

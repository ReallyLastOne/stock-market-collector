package org.reallylastone.trade.jobs;

import org.reallylastone.leadership.gateway.LeadershipGateway;

public class TradeWriterJob implements Runnable {
    private static final int INSERT_INTERVAL_MILLIS = 5000;
    private final TradeWriter writer;
    private final LeadershipGateway leadershipGateway;

    public TradeWriterJob(TradeWriter writer, LeadershipGateway leadershipGateway) {
        this.writer = writer;
        this.leadershipGateway = leadershipGateway;
    }

    @Override
    public void run() {
        while (true) {
            if (leadershipGateway.amILeader()) {
                writer.run();
            }
            try {
                Thread.sleep(INSERT_INTERVAL_MILLIS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
}

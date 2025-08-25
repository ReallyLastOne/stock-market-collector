package org.reallylastone.leadership.jobs;

import org.reallylastone.leadership.gateway.LeadershipGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AcquireLeadershipJob implements Runnable {
    public static final int RENEWAL_INTERVAL_SECONDS = 15;
    private static final Logger log = LoggerFactory.getLogger(AcquireLeadershipJob.class);
    private final long pid;
    private final LeadershipGateway leadershipGateway;

    public AcquireLeadershipJob(LeadershipGateway leadershipGateway) {
        this.pid = ProcessHandle.current().pid();
        this.leadershipGateway = leadershipGateway;
    }

    @Override
    public void run() {
        if (leadershipGateway.amILeader()) {
            leadershipGateway.updateHeartbeat(pid);
        } else if (leadershipGateway.tryAcquireLeadership(pid)) {
            log.info("{} acquired leadership", pid);
        } else {
            log.trace("{} not a leader", pid);
        }
    }
}

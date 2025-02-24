package org.reallylastone.leadership.jobs;

import org.reallylastone.leadership.domain.Leader;
import org.reallylastone.leadership.gateway.LeadershipGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZonedDateTime;

public class AcquireLeadershipJob implements Runnable {
    public static final int RENEWAL_INTERVAL_SECONDS = 15;
    private static final Logger log = LoggerFactory.getLogger(AcquireLeadershipJob.class);
    private final long pid;
    private final LeadershipGateway leadershipGateway;

    public AcquireLeadershipJob(LeadershipGateway leadershipGateway) {
        this.pid = ProcessHandle.current().pid();
        this.leadershipGateway = leadershipGateway;
    }

    private static boolean shouldIAcquireLeadership(Leader leader) {
        ZonedDateTime now = ZonedDateTime.now();
        return now.minusSeconds(RENEWAL_INTERVAL_SECONDS).isAfter(leader.renewTimestamp());
    }

    @Override
    public void run() {
        Leader leader = leadershipGateway.getLeader();
        if (amILeader(leader)) {
            leadershipGateway.updateHeartbeat();
        } else if (shouldIAcquireLeadership(leader)) {
            log.info("Process {} acquired leadership", pid);
            leadershipGateway.acquireLeadership(pid);
        }
    }

    private boolean amILeader(Leader leader) {
        return leader.processId() == pid;
    }
}

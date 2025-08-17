package org.reallylastone.job;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.reallylastone.leadership.domain.Leader;
import org.reallylastone.leadership.gateway.LeadershipGateway;
import org.reallylastone.leadership.jobs.AcquireLeadershipJob;

import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class AcquireLeadershipJobTest {
    LeadershipGateway leadershipGateway;
    AcquireLeadershipJob job;
    long pid;

    @BeforeEach
    void setup() {
        leadershipGateway = mock(LeadershipGateway.class);
        job = new AcquireLeadershipJob(leadershipGateway);
        pid = ProcessHandle.current().pid();
    }

    @Test
    void testAlreadyLeaderUpdatesHeartbeat() {
        // given
        Leader leader = mock(Leader.class);
        when(leader.processId()).thenReturn(pid);
        when(leadershipGateway.getLeader()).thenReturn(leader);

        // when
        job.run();

        // then
        verify(leadershipGateway).updateHeartbeat();
        verify(leadershipGateway, never()).acquireLeadership(anyLong());
    }

    @Test
    void testCanAcquireLeadership() {
        // given
        Leader leader = mock(Leader.class);
        when(leader.processId()).thenReturn(pid + 1);
        when(leader.renewTimestamp()).thenReturn(ZonedDateTime.now().minusSeconds(AcquireLeadershipJob.RENEWAL_INTERVAL_SECONDS + 1));
        when(leadershipGateway.getLeader()).thenReturn(leader);

        // when
        job.run();

        // then
        ArgumentCaptor<Long> captor = ArgumentCaptor.forClass(Long.class);
        verify(leadershipGateway).acquireLeadership(captor.capture());
        assertEquals(pid, captor.getValue());
        verify(leadershipGateway, never()).updateHeartbeat();
    }

    @Test
    void testCannotAcquireIfRecentHeartbeat() {
        // given
        Leader leader = mock(Leader.class);
        when(leader.processId()).thenReturn(pid + 1);
        when(leader.renewTimestamp()).thenReturn(ZonedDateTime.now());
        when(leadershipGateway.getLeader()).thenReturn(leader);

        // when
        job.run();

        // then
        verify(leadershipGateway, never()).acquireLeadership(anyLong());
        verify(leadershipGateway, never()).updateHeartbeat();
    }

    @Test
    void testTwoProcessesCannotAcquireAtOnce() {
        // given
        LeadershipGateway gateway1 = mock(LeadershipGateway.class);
        LeadershipGateway gateway2 = mock(LeadershipGateway.class);
        long pid1 = pid;
        long pid2 = pid + 1000;

        Leader leader = mock(Leader.class);
        when(leader.processId()).thenReturn(pid1 - 1);
        when(leader.renewTimestamp()).thenReturn(ZonedDateTime.now().minusSeconds(AcquireLeadershipJob.RENEWAL_INTERVAL_SECONDS + 1));
        when(gateway1.getLeader()).thenReturn(leader);
        when(gateway2.getLeader()).thenReturn(leader);

        AcquireLeadershipJob job1 = new AcquireLeadershipJob(gateway1);
        AcquireLeadershipJob job2 = new AcquireLeadershipJob(gateway2);

        // when
        job1.run();
        job2.run();

        // then
        verify(gateway1).acquireLeadership(pid1);
        verify(gateway2, never()).acquireLeadership(pid2);
    }
}

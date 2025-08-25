package org.reallylastone.job;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.reallylastone.leadership.gateway.LeadershipGateway;
import org.reallylastone.leadership.jobs.AcquireLeadershipJob;

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
        when(leadershipGateway.amILeader()).thenReturn(true);
        when(leadershipGateway.updateHeartbeat(pid)).thenReturn(true);

        job.run();

        verify(leadershipGateway).updateHeartbeat(pid);
        verify(leadershipGateway, never()).tryAcquireLeadership(anyLong());
    }

    @Test
    void testCanAcquireLeadership() {
        when(leadershipGateway.amILeader()).thenReturn(false);
        when(leadershipGateway.tryAcquireLeadership(pid)).thenReturn(true);

        job.run();

        ArgumentCaptor<Long> captor = ArgumentCaptor.forClass(Long.class);
        verify(leadershipGateway).tryAcquireLeadership(captor.capture());
        assertEquals(pid, captor.getValue());
        verify(leadershipGateway, never()).updateHeartbeat(anyLong());
    }

    @Test
    void testCannotAcquireIfRecentHeartbeat() {
        when(leadershipGateway.amILeader()).thenReturn(false);
        when(leadershipGateway.tryAcquireLeadership(pid)).thenReturn(false);

        job.run();

        verify(leadershipGateway).tryAcquireLeadership(pid);
        verify(leadershipGateway, never()).updateHeartbeat(anyLong());
    }

    @Test
    void testTwoProcessesCannotAcquireAtOnce() {
        LeadershipGateway gateway1 = mock(LeadershipGateway.class);
        LeadershipGateway gateway2 = mock(LeadershipGateway.class);
        long pid1 = pid;
        long pid2 = pid + 1000;

        when(gateway1.amILeader()).thenReturn(false);
        when(gateway2.amILeader()).thenReturn(false);
        when(gateway1.tryAcquireLeadership(pid1)).thenReturn(true);
        when(gateway2.tryAcquireLeadership(pid2)).thenReturn(false);

        AcquireLeadershipJob job1 = new AcquireLeadershipJob(gateway1);
        AcquireLeadershipJob job2 = new AcquireLeadershipJob(gateway2);

        job1.run();
        job2.run();

        verify(gateway1).tryAcquireLeadership(pid1);
        verify(gateway2, never()).tryAcquireLeadership(pid2);
    }
}

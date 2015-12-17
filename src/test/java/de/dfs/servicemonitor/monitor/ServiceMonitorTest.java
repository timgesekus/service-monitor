package de.dfs.servicemonitor.monitor;

import static org.hamcrest.MatcherAssert.assertThat;

import java.time.Duration;

import org.hamcrest.core.IsNull;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.when;

import static org.hamcrest.Matchers.*;
import de.dfs.servicemonitor.etcd.EtcdClient;
import de.dfs.servicemonitor.etcd.EtcdClient.Connection;

@RunWith(MockitoJUnitRunner.class)
public class ServiceMonitorTest {

  private static final String SOCKET_KEY = "socket";

  private static final String SESSION_1 = "Session-1";

  @Mock
  private EtcdClient etcdClient;

  @Mock
  private ServicePinger servicePinger;

  @Mock
  private ServiceMonitorConfig config;

  @Mock
  private Connection connection;

  
  
  @Before
  public void setUp() {
    when(config.getDurationBetweenConnectionRetries()).thenReturn(Duration.ofMillis(10));
    when(config.getTimeoutForConnection()).thenReturn(Duration.ofMillis(10));
    when(config.getMaxNumberOfConnectionRetries()).thenReturn(5);
    when(config.getWaitForSocketPublishing()).thenReturn(Duration.ofMillis(10));
    when(config.getSocketKey()).thenReturn(SOCKET_KEY);
  }

  @After
  public void tearDown() {

  }

  @Test
  public void testNormalFlow() {
    when(connection.get(SESSION_1 + "/" + SOCKET_KEY)).thenReturn(null);
    when(etcdClient.createConnection()).thenReturn(connection);
    ServiceMonitor serviceMonitor = new ServiceMonitor(etcdClient, servicePinger, SESSION_1, config);
    assertThat(serviceMonitor, notNullValue());
  }
}

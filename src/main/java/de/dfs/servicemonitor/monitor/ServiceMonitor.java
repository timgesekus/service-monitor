package de.dfs.servicemonitor.monitor;

import java.time.Duration;

import de.dfs.servicemonitor.etcd.EtcdClient;
import de.dfs.servicemonitor.etcd.EtcdClient.Connection;
import de.dfs.servicemonitor.etcd.responsemodel.Response;
import de.dfs.servicemonitor.monitor.ServicePinger.PingResult;
import play.libs.F.Promise;
import play.libs.ws.WSClient;
import play.libs.ws.WSRequest;

public class ServiceMonitor {
  private final Duration millisBetweenConnectionRetries;
  private final Duration timeoutForConnection;
  private final int maxNumberOfConnectionRetries;
  private final Duration waitForSocketPublishing;
  private EtcdClient etcdClient;
  private String session;
  private ServicePinger servicePinger;
  private String etcdSocketKey;

  public ServiceMonitor(EtcdClient etcdClient, ServicePinger servicePinger, String sessionId,
      ServiceMonitorConfig config) {
    this.etcdClient = etcdClient;
    this.servicePinger = servicePinger;
    session = sessionId;
    millisBetweenConnectionRetries = config.getDurationBetweenConnectionRetries();
    timeoutForConnection = config.getTimeoutForConnection();
    maxNumberOfConnectionRetries = config.getMaxNumberOfConnectionRetries();
    waitForSocketPublishing = config.getWaitForSocketPublishing();
    etcdSocketKey = config.getSocketKey();
  }

  public void start(String socketKey, long waitForSocket) {
    waitForSocketPublishing();
    tryInitialConnect();
    monitorService();
  }

  private void waitForSocketPublishing() {
    Connection request = etcdClient.createConnection();
    request.setWait();
    Promise<Response> promise = request.get(session + "/" + "socket");
    try {
      promise.get(waitForSocketPublishing.toMillis());
    } catch (Throwable t) {
      publishDeadState();
      exit("Wait for socket failed");
    }
  }

  private void tryInitialConnect() {
    boolean isConnected = false;
    int numberOfRetries = 0;
    do {
      PingResult pingResult = servicePinger.ping(timeoutForConnection);
      if (!pingResult.isError()) {
        isConnected = true;
        publishAliveState();
      } else {
        numberOfRetries++;
      }
    } while (!isConnected || numberOfRetries < maxNumberOfConnectionRetries);
    if (!isConnected) {
      publishDeadState();
      exit("Initial connect failed");
    }
  }

  public void monitorService() {
    boolean isConnected = true;
    do {
      PingResult pingResult = servicePinger.ping(timeoutForConnection);
      if (!pingResult.isError()) {
        isConnected = true;
        publishAliveState();
        try {
          Thread.sleep(millisBetweenConnectionRetries.toMillis());
        } catch (InterruptedException e) {
          isConnected = false;
        }
      } else {
        isConnected = false;
      }
    } while (isConnected);
    publishDeadState();
    exit("Initial connect failed");
  }

  private void exit(String errorMsg) {
    System.err.println(errorMsg);
  }

  public void publishDeadState() {
    Connection create = etcdClient.createConnection();
    create.set(session + "/state", "dead");
  }

  public void publishAliveState() {
    Connection connection = etcdClient.createConnection();
    connection.setTTL(1000);
    connection.set(session + "/state", "alive");
  }
}
